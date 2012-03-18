;; Copyright (c) Metadata Partners, LLC.
;; All rights reserved.

(ns org.fressian.api
  (:refer-clojure :exclude [pr read])
  (:require [clojure.java.io :as io])
  (:import
   [java.io InputStream OutputStream EOFException]
   java.nio.ByteBuffer
   java.nio.charset.Charset
   [org.fressian FressianWriter StreamingWriter FressianReader Writer Reader]
   [org.fressian.handlers WriteHandler ReadHandler ILookup  WriteHandlerLookup]
   [org.fressian.impl ByteBufferInputStream BytesOutputStream]))

;; move into get, a la Clojure lookup?
(defn as-lookup
  "Normalize ILookup or map into an ILookup."
  [o]
  (if (map? o)
    (reify ILookup
           (valAt [_ k] (get o k)))
    o))

(defn write-handler-lookup
  "Returns a fressian write handler lookup that combines fressian's built-in
   handlers with custom-lookup. custom-lookup can be a map or an ILookup,
   keyed by class and returning a single-entry map of tag->write handler.
   Use this to create custom validators, not to create FressianWriters, as
   the latter already call customWriteHandlers internally."
  [custom-lookup]
  (WriteHandlerLookup/createLookupChain (as-lookup custom-lookup)))

(defn ^Writer create-writer
  "Create a fressian writer targetting out. lookup can be an ILookup or
   a nested map of type => tag => WriteHandler."
  ;; TODO: make symmetric with create-reader, using io/output-stream?
  ([out] (create-writer out nil))
  ([out lookup]
     (FressianWriter. out (as-lookup lookup))))

(defn ^Reader create-reader
  "Create a fressian reader targetting in, which must be compatible
   with clojure.java.io/input-stream.  lookup can be an ILookup or
   a map of tag => ReadHandler."
  ([in] (create-reader in nil))
  ([in lookup] (create-reader in lookup true))
  ([in lookup validate-checksum]
     (FressianReader. (io/input-stream in) (as-lookup lookup) validate-checksum)))

(defn fressian
  "Fressian obj to output-stream compatible out.

   Options:
      :handlers    fressian handler lookup
      :footer      true to write footer"
  [out obj & {:keys [handlers footer]}]
  (with-open [os (io/output-stream out)]
    (let [writer (create-writer os handlers)]
      (.writeObject writer obj)
      (when footer
        (.writeFooter writer)))))

(defn defressian
  "Read single fressian object from input-stream-compatible in.

   Options:
      :handlers    fressian handler lookup
      :footer      true to validate footer"
  ([in & {:keys [handlers footer]}]
     (let [fin (create-reader in handlers)
           result (.readObject fin)]
       (when footer (.validateFooter fin))
       result)))

(defn ^ByteBuffer bytestream->buf
  "Return a readable buf over the current internal state of a
   BytesOutputStream."
  [^BytesOutputStream stream]
  (ByteBuffer/wrap (.internalBuffer stream) 0 (.length stream)))

(defn byte-buffer-seq
  "Return a lazy seq over the remaining bytes in the buffer.
   Not fast: intented for REPL usage.
   Works with its own duplicate of the buffer."
  [^ByteBuffer bb]
  (lazy-seq
   (when (.hasRemaining bb)
     (let [next-slice (.slice bb)]
       (cons (.get next-slice) (byte-buffer-seq next-slice))))))

(defn ^ByteBuffer byte-buf
  "Return a byte buffer with the fressianed form of object.
   See fressian for options."
  [obj & options]
  (let [baos (BytesOutputStream.)]
    (apply fressian baos obj options)
    (bytestream->buf baos)))

(defn read-batch
  "Read a fressian reader fully (until eof), returning a (possibly empty)
   vector of results."
  [^Reader fin]
  (let [sentinel (Object.)]
    (loop [objects []]
      (let [obj (try (.readObject fin) (catch EOFException e sentinel))]
        (if (= obj sentinel)
          objects
          (recur (conj objects obj)))))))

(def clojure-write-handlers
  {clojure.lang.Keyword
   {"key"
    (reify WriteHandler (write [_ w s]
                               (.writeTag w "key" 2)
                               (.writeObject w (namespace s))
                               (.writeObject w (name s))))}
   clojure.lang.Symbol
   {"sym"
    (reify WriteHandler (write [_ w s]
                               (.writeTag w "sym" 2)
                               (.writeObject w (namespace s))
                               (.writeObject w (name s))))}})

(def clojure-read-handlers
  {"key"
   (reify ReadHandler (read [_ rdr tag component-count]
                            (keyword (.readObject rdr) (.readObject rdr))))
   "sym"
   (reify ReadHandler (read [_ rdr tag component-count]
                            (symbol (.readObject rdr) (.readObject rdr))))
   "map"
   (reify ReadHandler (read [_ rdr tag component-count]
                            (let [kvs ^java.util.List (.readObject rdr)]
                              (if (< (.size kvs) 16)
                                (clojure.lang.PersistentArrayMap. (.toArray kvs))
                                (clojure.lang.PersistentHashMap/create (seq kvs))))))})

(extend ByteBuffer
  io/IOFactory
  (assoc io/default-streams-impl
    :make-input-stream (fn [x opts] (io/make-input-stream
                                     (ByteBufferInputStream. x) opts))))
