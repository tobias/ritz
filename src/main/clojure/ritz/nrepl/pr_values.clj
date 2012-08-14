(ns ^{:author "Chas Emerick"}
     ritz.nrepl.pr-values
  (:require [clojure.tools.nrepl.transport :as t])
  (:import clojure.tools.nrepl.transport.Transport))

(defn pr-values
  "Middleware that returns a handler which transforms any :value slots
   in messages sent via the request's Transport to strings via `pr`,
   delegating all actual message handling to the provided handler.

   Requires that results of eval operations are sent in messages in a
   :value slot."
  ([ops]
     (fn [h]
       (fn [{:keys [op ^Transport transport] :as msg}]
         (if (ops op)
           (h (assoc msg
                :transport (reify Transport
                             (recv [this] (.recv transport))
                             (recv [this timeout] (.recv transport timeout))
                             (send [this resp]
                               (.send transport
                                      (if-let [[_ v] (find resp :value)]
                                        (assoc resp :value (pr-str v))
                                        resp))))))
           (h msg)))))
  ([] (pr-values #{"eval"})))