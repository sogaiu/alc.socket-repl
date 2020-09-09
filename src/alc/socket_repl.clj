(ns alc.socket-repl
  (:require
    [clojure.main :as cm]
    [clojure.core.server :as ccs]))

;; XXX: option to write a dot file with port number?
(defn start
  [{:keys [address name port repl]}]
  ;; XXX: check that port is a sensible number?
  (let [port (or port 7789)
        name (or name (str port))]
    (if-let [prop (System/getProperty "clojure.server.repl")]
      (println "Socket repl detected? " prop)
      (do
        ;; XXX: only do this if repl creation succeeds
        (System/setProperty "clojure.server.repl"
                            (str "{"
                                 "\\:port," port ","
                                 "\\:accept,clojure.core.server/repl"
                                 "}"))
        (ccs/start-server {:address address
                           :port port
                           :name name
                           :accept 'clojure.core.server/repl})
        (println "Socket repl port:" port)))
    (when repl
      (cm/main "--repl"))))
