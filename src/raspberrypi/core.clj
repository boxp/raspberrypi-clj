(ns raspberrypi.core
  (:use clojure.java.shell))

(defn gpio [& {:keys [operation pin value]
               :or [operation "help"
                    pin ""
                    value ""]}]
  (sh "gpio" operation pin value))

(defn read-all []
  "すべてのピンの状態を表示する"
  (:out (gpio :operation "readall")))

(defn talk! [txt]
  (sh "AquesTalkPi" txt "|" "aplay"))

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
          :pin (:pin this))
          :value "out")
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

