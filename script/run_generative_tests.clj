(set! *warn-on-reflection* true)
(when-not (System/getProperty "clojure.test.generative.msec")
  (System/setProperty "clojure.test.generative.msec" "30000"))
(require '[clojure.test.generative.runner :as runner])
(runner/-main "test")



