;; Copyright (c) Metadata Partners, LLC.
;; All rights reserved.

(ns org.fressian.test-helpers
  (:use clojure.data clojure.pprint)
  (:require [clojure.walk :as walk]
            [clojure.java.io :as io]
            [clojure.data :as data]
            [clojure.data.generators :as gen]
            [clojure.java.shell :as sh]
            [org.fressian.api :as fressian])
  (:import [java.io IOException Closeable]
           [java.nio ByteBuffer]
           [org.fressian.impl BytesOutputStream]))

(set! *warn-on-reflection* true)

;; for the bad classes that don't do value equality (e.g. float)
;; or don't work with data/diff (e.g. ByteBuffer)
(defprotocol EqualityDelegate
  (eqd [_] "nominate an object (usually this) to be used for equality comparison."))

(extend-protocol EqualityDelegate
  nil
  (eqd [n] n)
  
  org.fressian.TaggedObject
  (eqd [o] {:org.fressian/tag (.tag o)
            :org.fressian/value (into [] (.value o))})

  java.util.regex.Pattern
  (eqd [p] (.pattern p))
  
  java.lang.Float
  (eqd [f] (if (.isNaN f) ::float-nan f))

  java.lang.Double
  (eqd [f] (if (.isNaN f) ::double-nan f))

  java.nio.ByteBuffer
  (eqd [f] (into [] (fressian/byte-buffer-seq f)))
  
  Object
  (eqd [o] o))

(defmacro assert=
  ([a b] `(assert= ~a ~b nil))
  ([a b context]
     `(let [a# (walk/prewalk eqd ~a) b# (walk/prewalk eqd ~b)]
        (when-not (= a# b#)
          (let [[d1# d2# s#] (clojure.data/diff a# b#)]
            (when (or d1# d2#)
              (throw (ex-info "Items different"
                              {:in-a d1# :in-b d2# :in-both s# :context ~context}))))))))

;; work around limitations of walk/prewalk
(extend-type (class (float-array 0))
  EqualityDelegate
  (eqd [p] (into [] p)))

(extend-type (class (double-array 0))
  EqualityDelegate
  (eqd [p] (into [] p)))

(extend-type (class (byte-array 0))
  EqualityDelegate
  (eqd [p] (into [] p)))

(extend-type (class (object-array 0))
  EqualityDelegate
  (eqd [p] (into [] p)))

(extend-type (class (long-array 0))
  EqualityDelegate
  (eqd [p] (into [] p)))

(extend-type (class (int-array 0))
  EqualityDelegate
  (eqd [p] (into [] p)))

(extend-type (class (boolean-array 0))
  EqualityDelegate
  (eqd [p] (into [] p)))


