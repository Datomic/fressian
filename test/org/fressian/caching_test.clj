;; Copyright (c) Metadata Partners, LLC.
;; All rights reserved.

(ns org.fressian.caching-test
  (:use [clojure.test.generative :only (defspec) :as test]
        [org.fressian.test-helpers :only (assert=) :as th])
  (:require [org.fressian.generators :as gen]
            [clojure.data.generators :as tgen]
            [org.fressian.api :as fressian])
  (:import [org.fressian.impl BytesOutputStream]))

(set! *warn-on-reflection* true)

(defn cache-session->fressian
  "write-args are a series of [fressianble cache?] pairs."
  [write-args]
  (let [baos (BytesOutputStream.)
        writer (fressian/create-writer baos)]
    (doseq [[idx [obj cache]] (map-indexed vector write-args)]
      (let [_ (.writeObject writer obj cache)])
      (when (= 39 (mod idx 40)) (.writeFooter writer)))
    (fressian/bytestream->buf baos)))

(defn roundtrip-cache-session
  "Roundtrip cache-session through fressian and back."
  [cache-session]
  (-> cache-session cache-session->fressian fressian/create-reader fressian/read-batch))

(defspec strings-with-caching
  roundtrip-cache-session
  [^{:tag (`gen/cache-session `tgen/string)} args]
  (assert (= (map first args) %)))

(defspec fressian-with-caching
  roundtrip-cache-session
  [^{:tag (`gen/cache-session `gen/fressian-builtin)} args]
  (assert= (map first args) %))

(defn compare-cache-and-uncached-versions
  "For each o in objects, print o, its uncached value, and its cached value.
   Used to verify cache skipping"
  [objects]
  (doseq [o objects]
    (println o
             " [Uncached:  "(fressian/byte-buffer-seq (cache-session->fressian [[o false]])) "]"
             " [Cached: " (fressian/byte-buffer-seq (cache-session->fressian [[o true]])) "]")))

(comment
(set! *warn-on-reflection* true)
(in-ns 'user)
(in-ns 'org.fressian.fressian-test)

(require :reload 'org.fressian.fressian-test)

(import org.fressian.CachedObject)
(compare-cache-and-uncached-versions [true false nil -1 0 1 1000 10000 "" "FOO" 1.0 2.0
                                      (CachedObject. 0) (CachedObject. "BAR")])
)
