(ns raspberrypi.twitter
  (:require [clojure.java.io :as io])
  (:import (twitter4j TwitterFactory TwitterStreamFactory
                      StatusListener StatusUpdate)
           (twitter4j.auth AccessToken)))

(def consumers {:key "DdM4ZiO5n1j09Zd7Xbsbuxdxu"
                :secret "ErHJyjOt0cChKdlbx4QIcKRjaSfuSuSl5s6rl16BPv7M7CX3JV"})

(defn read-token []
  "read token from resources/token.clj"
  (-> "token.clj" io/resource .getPath load-file))

(defn token->AccessToken
  "convert token-map to twitter4j.auth.AccessToken"
  [token]
  (AccessToken. (:token token) (:secret token)))

(defn twitter
  "gen twitter4j.Twitter from token-map"
  [token]
  (doto (.getInstance (TwitterFactory.))
    (.setOAuthConsumer (:key consumers) (:secret consumers))
    (.setOAuthAccessToken (token->AccessToken token))))

(defn twitterstream
  "gen twitter4j.TwitterStream from token-map"
  [token]
  (doto (.getInstance (TwitterStreamFactory.))
    (.setOAuthConsumer (:key consumers) (:secret consumers))
    (.setOAuthAccessToken (token->AccessToken token))))
    
(defn status-listener
  "gen twitter.StatusListener"
  [& {:keys [on-delete
             on-scrub
             on-stall-warn
             on-status
             on-limit
             on-exception]
      :or {on-delete #()
           on-scrub #()
           on-stall-warn #()
           on-status #()
           on-limit #()
           on-exception #(throw %)}}]
  (reify StatusListener
    (onDeletionNotice [this statusDeletionNotice]
      (on-delete statusDeletionNotice))
    (onScrubGeo [this userId upToStatusId]
      (on-scrub userId upToStatusId))
    (onStallWarning [this warning]
      (on-stall-warn warning))
    (onStatus [this status]
      (on-status status))
    (onTrackLimitationNotice [this numberOfLimitedStatuses]
      (on-limit numberOfLimitedStatuses))
    (onException [this ex]
      (on-exception ex))))

(defn start!
  [twitterstream listener]
  (doto twitterstream
    (.addListener listener)
    (.user)))

(defn post
  [twitter txt]
  (->> txt (take 140) (apply str) (.updateStatus twitter)))

(defn reply
  [twitter status txt]
  (as-> txt $
        (StatusUpdate. $) 
        (.inReplyToStatusId $ (.getId status)) 
        (.updateStatus twitter $)))
