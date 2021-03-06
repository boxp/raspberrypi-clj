(ns raspberrypi.core
  (:require [raspberrypi.twitter :as tw])
  (:use clojure.java.shell))

(defn gpio [& {:keys [operation pin value]
               :or [operation "help"
                    pin ""
                    value ""]}]
  (sh "gpio" operation pin value))

(defn get-home
  "ホームディレクトリを取得"
  []
  (let [home (System/getenv "HOME")]
    (if home
      home
      (System/getProperty "user.home"))))

(defn read-all []
  "check all pin's state"
  (:out (gpio :operation "readall")))

(defn talk! [txt]
  (sh "sh" "-c" 
    (str (get-home) "/aquestalkpi/AquesTalkPi " txt " | aplay")))

(defprotocol IPIN
  (set-out! [this])
  (set-in! [this])
  (set-pwm! [this])
  (pwm [this value])
  (write [this value])
  (read [this]))

(defrecord PIN
  [pin]
  IPIN
  (set-out! [this]
    (gpio :operation "mode"
          :pin (:pin this)
          :value "out"))
  (set-in! [this]
    (gpio :operation "mode"
          :pin (:pin this)
          :value "in"))
  (set-pwm! [this]
    (gpio :operation "mode"
          :pin (:pin this)
          :value "pwm"))
  (pwm [this value]
    (gpio :operation "pwm"
          :pin (:pin this)
          :value value))
  (write [this value]
    (gpio :operation "write"
          :pin (:pin this)
          :value (str value)))
  (read [this]
    (gpio :operation "read"
          :pin (:pin this))))

(defn -main [& args]
  (-> (tw/read-token)
    tw/twitterstream
    (tw/start! (tw/status-listener :on-status #(-> % .getText talk!)))))
