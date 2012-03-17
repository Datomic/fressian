;; Copyright (c) Metadata Partners, LLC.
;; All rights reserved.

(ns org.fressian.codegen
  (:require [clojure.string :as str]))

(def sizes
  (into (sorted-map)
        {:PACKED_2 13
         :PACKED_3 20
         :PACKED_4 26
         :PACKED_5 34
         :PACKED_6 42
         :PACKED_7 50}))

(def codes
  [[:PRIORITY_CACHE_PACKED [0x80 0xA0]]
   [:STRUCT_CACHE_PACKED [0xA0 0xB0]]
   [:LONG_ARRAY 0xB0]
   [:DOUBLE_ARRAY 0xB1]
   [:BOOLEAN_ARRAY 0xB2]
   [:INT_ARRAY 0xB3]
   [:FLOAT_ARRAY 0xB4]
   [:OBJECT_ARRAY 0xB5]
   [:MAP 0xC0]
   [:SET 0xC1]
   [:UUID 0xC3]
   [:REGEX 0xC4]
   [:URI 0xC5]
   [:BIGINT 0xC6]
   [:BIGDEC 0xC7]
   [:INST 0xC8]
   [:SYM 0xC9]
   [:KEY 0xCA]
   [:GET_PRIORITY_CACHE 0xCC]
   [:PUT_PRIORITY_CACHE 0xCD]
   [:PRECACHE 0xCE]   
   [:FOOTER 0xCF]
   [:FOOTER_MAGIC 0xCFCFCFCF]
   [:BYTES_PACKED_LENGTH [0xD0 0xD8]]
   [:BYTES_CHUNK 0xD8]
   [:BYTES 0xD9]
   [:STRING_PACKED_LENGTH [0xDA 0xE2]]
   [:STRING_CHUNK 0xE2]
   [:STRING 0xE3]
   [:LIST_PACKED_LENGTH [0xE4 0xEC]]
   [:LIST 0xEC]
   [:BEGIN_CLOSED_LIST 0xED]
   [:BEGIN_OPEN_LIST 0xEE]
   [:STRUCTTYPE 0xEF]
   [:STRUCT 0xF0]
   [:META 0xF1]
   [:ANY 0xF4]   
   [:TRUE 0xF5]
   [:FALSE 0xF6]
   [:NULL 0xF7]
   [:INT 0xF8]
   [:FLOAT 0xF9]
   [:DOUBLE 0xFA]
   [:DOUBLE_0 0xFB]
   [:DOUBLE_1 0xFC]
   [:END_COLLECTION 0xFD]
   [:RESET_CACHES 0xFE]

   [:INT_PACKED_1 [0xFF 0x40]]
   [:INT_PACKED_2 [0x40 0x50 0x60]]
   [:INT_PACKED_3 [0x60 0x68 0x70]]
   [:INT_PACKED_4 [0x70 0x72 0x74]]
   [:INT_PACKED_5 [0x74 0x76 0x78]]
   [:INT_PACKED_6 [0x78 0x7A 0x7C]]
   [:INT_PACKED_7 [0x7C 0x7E 0x80]]])

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
