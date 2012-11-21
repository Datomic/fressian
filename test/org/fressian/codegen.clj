;; Copyright (c) Metadata Partners, LLC.
;; All rights reserved.

(ns org.fressian.codegen
  (:require [clojure.string :as str]
            [clojure.pprint :refer (print-table)]))

(def sizes
  (into (sorted-map)
        {:PACKED_2 13
         :PACKED_3 20
         :PACKED_4 26
         :PACKED_5 34
         :PACKED_6 42
         :PACKED_7 50}))

(def codes
  [[:PRIORITY_CACHE_PACKED [0x80 0xA0] "Single byte codes for user-cached data"]
   [:STRUCT_CACHE_PACKED [0xA0 0xB0] "Single byte codes for tagged structure descriptors."]
   [:LONG_ARRAY 0xB0 "Array of primitive longs"]
   [:DOUBLE_ARRAY 0xB1 "Array of primitive doubles"]
   [:BOOLEAN_ARRAY 0xB2 "Array of primitive booleans"]
   [:INT_ARRAY 0xB3 "Array of primitive ints"]
   [:FLOAT_ARRAY 0xB4 "Array of primitive floats"]
   [:OBJECT_ARRAY 0xB5 "Array of objects"]
   [:MAP 0xC0 "Heterogeneous map"]
   [:SET 0xC1 "Heterogeneous set"]
   [:UUID 0xC3 "UUID"]
   [:REGEX 0xC4 "Regular expression"]
   [:URI 0xC5 "URI"]
   [:BIGINT 0xC6 "Arbitrary precision integer"]
   [:BIGDEC 0xC7 "Arbitrary precision decimal"]
   [:INST 0xC8 "Instant in time "]
   [:SYM 0xC9 "Namespaced symbol"]
   [:KEY 0xCA "Namespaced keyword"]
   [:GET_PRIORITY_CACHE 0xCC "Lead byte for multibyte-encoded user cache entries."]
   [:PUT_PRIORITY_CACHE 0xCD "Object that follows should be added to priority cache"]
   [:PRECACHE 0xCE "Cache an object now, without actually making it appear to reader"]   
   [:FOOTER 0xCF "Optional footer signalling end of fressian data"]
   [:FOOTER_MAGIC 0xCFCFCFCF "Repetition of footer code"]
   [:BYTES_PACKED_LENGTH [0xD0 0xD8] "Packed byte array"]
   [:BYTES_CHUNK 0xD8 "Chunk of a byte arrays"]
   [:BYTES 0xD9 "Unpacked byte array"]
   [:STRING_PACKED_LENGTH [0xDA 0xE2] "Packed string"]
   [:STRING_CHUNK 0xE2 "Chunk of a string"]
   [:STRING 0xE3 "Unpacked string"]
   [:LIST_PACKED_LENGTH [0xE4 0xEC] "Packed string"]
   [:LIST 0xEC "Unpacked list"]
   [:BEGIN_CLOSED_LIST 0xED "Variable length list, expect termination with END_COLLECTION"]
   [:BEGIN_OPEN_LIST 0xEE "Variable length list, terminate with END_COLLECTION or end of stream"]
   [:STRUCTTYPE 0xEF "Structure, followed by tag and component count"]
   [:STRUCT 0xF0 "Reference to a cached structure"]
   [:META 0xF1 "Metadata (currently unused)"]
   [:ANY 0xF4 "Placeholder code for data that could be anything"]   
   [:TRUE 0xF5 "Boolean true"]
   [:FALSE 0xF6 "Boolean false"]
   [:NULL 0xF7 "Null / nil"]
   [:INT 0xF8 "Unpacked int"]
   [:FLOAT 0xF9 "Float"]
   [:DOUBLE 0xFA "Double"]
   [:DOUBLE_0 0xFB "The double value 0.0"]
   [:DOUBLE_1 0xFC "The double value 1.0"]
   [:END_COLLECTION 0xFD "End an open collection"]
   [:RESET_CACHES 0xFE "Reset cache codes"]

   [:INT_PACKED_1 [0xFF 0x40] "Integer packed into a single byte"]
   [:INT_PACKED_2 [0x40 0x50 0x60] "Integer packed into two bytes"]
   [:INT_PACKED_3 [0x60 0x68 0x70] "Integer packed into three bytes"]
   [:INT_PACKED_4 [0x70 0x72 0x74] "Integer packed into four bytes"]
   [:INT_PACKED_5 [0x74 0x76 0x78] "Integer packed into five bytes"]
   [:INT_PACKED_6 [0x78 0x7A 0x7C] "Integer packed into six bytes"]
   [:INT_PACKED_7 [0x7C 0x7E 0x80] "Integer packed into seven bytes"]])

(defn hex
  [x]
  (if (number? x)
    (Long/toHexString x)
    (str/join " - " (mapv hex x))))

(defn codes->org
  [codes]
  (println "#+OPTIONS: ^:nil")
  (->> (map
        (fn [[code range doc]]
          {"Code" (name code)
           "Range" (hex range)
           "Notes" doc})
        codes)
       (print-table ["Code" "Range" "Notes"])))

(defn code-type
  [code]
  (let [arg (second code)]
    (cond
     (number? arg) :single-code
     (= 2 (count arg)) :code-range
     (= 3 (count arg)) :code-range-with-zero)))

(defmulti print-code code-type)

(defmethod print-code :single-code
  [[n c]]
  (println (format "public static final int %s = 0x%02X;" (name n) c)))

(defmethod print-code :code-range
  [[n [start end]]]
  (println (format "public static final int %s_START = 0x%02X;" (name n) (unchecked-byte start)))
  (println (format "public static final int %s_END = 0x%02X;" (name n) (unchecked-byte end))))

(defmethod print-code :code-range-with-zero
  [[n [start zero end]]]
  (println (format "public static final int %s_START = 0x%02X;" (name n) (unchecked-byte start)))
  (println (format "public static final int %s_ZERO = 0x%02X;" (name n) (unchecked-byte zero)))
  (println (format "public static final int %s_END = 0x%02X;" (name n) (unchecked-byte end)))  )

(defmulti print-case code-type)

(defmethod print-case :single-code [_] false)

(defmethod print-case :code-range [[n [start end]]]
  (dotimes [i (- end start)]
    (println (format "case Codes.%s_START + %d:" (name n) i)))
  true)

(defmethod print-case :code-range-with-zero [[n [start _ end]]]
  (dotimes [i (- end start)]
    (println (format "case Codes.%s_START + %d:" (name n) i)))
  true)

(defn print-codes
  []
  (doseq [code codes] (print-code code)))

(defn print-cases
  []
  (doseq [code codes]
    (when (print-case code) (println))))

(defmulti print-range code-type)

(defmethod print-range :single-code [_] )

(defmethod print-range :code-range [[n [start end]]]
  (when-not (.startsWith (name n) "INT")
    (println (format "public static final int %s_END = %d;" (name n) (- end start)))))

(defmethod print-range :code-range-with-zero [_])

(defn print-ranges
  []
  (doseq [code codes]
    (print-range code)))

(defn print-packed-ranges
  []
  (doseq [[k v] sizes]
    (println (format "public static final long %s_START = 0x%016XL;" (name k) (- (long (Math/pow 2 (dec v))))))
    (println (format "public static final long %s_END = 0x%016XL;" (name k) (long (Math/pow 2 (dec v)))))))

(defn print-all-cases
  [prefix n]
  (dotimes [i n]
    (println (format "case %s + %d:" prefix i))))

(def array-types [:int :long :float :boolean :double])

(defn print-array-cases
  []
  (doseq [t array-types]
    (println (format "case Codes.%s_ARRAY:\nresult=handleStruct(\"%s[]\", 2);\nbreak;\n" (.toUpperCase (name t)) (name t)))))

(defn substitute
  [str maps]
  (reduce
   (fn [s [k v]]
     (str/replace s k v))
   str
   maps))

(defn print-array-write-handlers
  []
  (doseq [t array-types]
    (println (substitute  "installHandler(handlers, (new <TYPE>[]{}).getClass(), \"<TYPE>[]\", new WriteHandler() {
            public void write(Writer w, Object instance) throws IOException {
                <TYPE>[] <TYPE>s = (<TYPE>[]) instance;
                w.writeTag(\"<TYPE>[]\", 2);
                w.writeInt(<TYPE>s.length);
                for (int n=0; n<<TYPE>s.length; n++) {
                    w.write<CTYPE>(<TYPE>s[n]);
                }
            }
        });
" {"<TYPE>" (name t)
   "<CTYPE>" (str/capitalize (name t))}))))

(defn print-array-read-handlers
  []
  (doseq [t array-types]
    (when-not (= t :int) ;; int is a little different, coded for cast
        (println (substitute "handlers.put(\"<TYPE>[]\", new ReadHandler() {
            public Object read(Reader r, Object tag, int componentCount) throws IOException {
                int size = intCast(r.readInt());
                <TYPE>[] result = new <TYPE>[size];
                for (int n=0; n < size; n++) {
                    result[n] = r.read<CTYPE>();
                }
                return result;
            }
        });
" {"<TYPE>" (name t)
   "<CTYPE>" (str/capitalize (name t))})))))

(comment

  (use :reload 'org.fressian.codegen)
  (in-ns 'org.fressian.codegen)

  ;; Codes.java
  (print-codes)

  ;; Ranges.java
  (print-packed-ranges)
  (print-ranges)  

  ;; FressianReader.java
  (print-all-cases "Codes.PRIORITY_CACHE_PACKED_START" 32)
  (print-array-cases)

  ;; Handlers.java
  ;; IIRC not fully automated, these require hand-tweaking if re-run
  (print-array-write-handlers)
  (print-array-read-handlers)

)
