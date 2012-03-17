(set! *warn-on-reflection* true)

(use '[clojure.test.generative])
(try
 (binding [*msec* 10000]
   (let [futures (test-dirs "test")]
     (doseq [f futures]
       @f)))
 (catch Throwable t
   (.printStackTrace t)
   (System/exit -1))
 (finally
  (shutdown-agents)))


