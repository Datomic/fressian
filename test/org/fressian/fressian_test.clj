;; Copyright (c) Metadata Partners, LLC.
;; All rights reserved.

(ns org.fressian.fressian-test
  (:use [clojure.test.generative :only (defspec) :as test]
        [org.fressian.test-helpers :only (assert=)])
  (:require [org.fressian.generators :as gen]
            [org.fressian.api :as fressian])
  (:import
   [org.fressian StreamingWriter]
   [org.fressian.impl BytesOutputStream]))

(set! *warn-on-reflection* true)

(defn roundtrip
  "Fressian and defressian o"
  ([o]
     (-> o fressian/byte-buf fressian/defressian))
  ([o write-handlers read-handlers]
     (-> o
         (fressian/byte-buf :handlers write-handlers)
         (fressian/defressian :handlers read-handlers))))

(defn roundtrip-footer
  "Fressian and defressian o with footer"
  ([o]
     (-> o (fressian/byte-buf :footer true) fressian/defressian))
  ([o write-handlers read-handlers]
     (-> o
         (fressian/byte-buf :handlers write-handlers :footer true)
         (fressian/defressian :handlers read-handlers))))

(defn roundtrip-open-list-footer
  "Fressian and defressian o inside an open list with footer"
  [o]
  (let [baos (BytesOutputStream.)
        fw (fressian/create-writer baos)]
    (.beginOpenList ^StreamingWriter fw)
    (.writeObject fw o)
    (.endList ^StreamingWriter fw)
    (.writeFooter fw)
    (first (fressian/defressian (fressian/bytestream->buf baos)
             :footer true))))

(defspec fressian-character-encoding
  roundtrip
  [^{:tag `gen/single-char-string} s]
  (assert (= s %)))

(defspec fressian-scalars
  roundtrip
  [^{:tag `gen/scalar} s]
  (assert= s %))

;; terminology TBD: using "builtin" here to describe all out-of-box
;; fressian types
(defspec fressian-builtins
  roundtrip
  [^{:tag `gen/fressian-builtin} s]
  (assert= s %))

(defspec fressian-builtins-with-footer
  roundtrip-footer
  [^{:tag `gen/fressian-builtin} s]
  (assert= s %))

(defspec fressian-open-list-with-footer
  roundtrip-open-list-footer
  [^{:tag `gen/fressian-builtin} s]
  (assert= s %))

(defspec fressian-int-packing
  roundtrip
  [^{:tag `gen/long-near-power-of-2} input]
  (assert (= input %)))

(defspec fressian-names
  (fn [o]  
    (roundtrip o fressian/clojure-write-handlers fressian/clojure-read-handlers))
  [^{:tag `gen/symbolic} s]
  (assert= s %))

(defn size
  "Measure the size of a fressianed object. Returns a map of
  :size, :second, :caching-size, and :cached-size.
  (:second will differ from size if there is internal caching.)"
  ([o] (size o nil))
  ([o write-handlers]
     (let [baos (BytesOutputStream.)
           writer (fressian/create-writer baos write-handlers)]
       (.writeObject writer o)
       (let [size (.length baos)]
         (.writeObject writer o)
         (let [second (- (.length baos) size)]
           (.writeObject writer o true)
           (let [caching-size (- (.length baos) second size)]
             (.writeObject writer o true)
             {:size size
              :second second
              :caching-size caching-size
              #_:bytes #_(seq (.internalBuffer baos))
              :cached-size (- (.length baos) caching-size second size)}))))))
