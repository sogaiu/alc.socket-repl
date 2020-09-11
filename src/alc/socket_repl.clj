(ns alc.socket-repl
  (:require
    [clojure.main :as cm]
    [clojure.core.server :as ccs])
  (:import
   [java.net InetAddress ServerSocket UnknownHostException]))

(defn check-port
  [port]
  (when-let [addr
             (try
               ;; XXX: possibly want to allow address specification
               (InetAddress/getByAddress (bytes (byte-array [127 0 0 1])))
               (catch UnknownHostException _
                 ;; XXX
                 (println "failed to determine localhost address")
                 nil))]
    (when-let [^ServerSocket sock
               (try
                 (ServerSocket. port 0 addr)
                 (catch java.io.IOException _
                   ;; XXX
                   (println "failed to create socket for:" port)
                   nil))]
      (let [received-port (.getLocalPort sock)]
        (.close sock)
        received-port))))

(defn find-port
  []
  (check-port 0))

;; XXX: option to write a dot file with port number?
(defn start
  [{:keys [address name port repl]}]
  (let [port (or port (find-port))
        _ (assert port (str "Failed to obtain port"))
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
