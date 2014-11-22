(ns gnugo-gtp.engine
  (:require [me.raynes.conch :refer [with-programs]]
            [clojure.string :refer [join]]))

(defn- extract-responses
  [raw-output]
  (->> raw-output
       ;; this regex splits multiple gtp responses in status, id and message
       ;; see tests for a better explanation
       (re-seq #"([?=]?)(-?[0-9].*)? (?s)(.*?)\n\n")
       (map (fn [[_ status id message]]
              {:status  (condp = status
                          "=" :success
                          :failure)
               :id      id
               :message message}))))

(defn- error-response?
  [{:keys [status message]}]
  (when (= status :failure)
    message))

(defn- run-command
  [gtp-engine command]
  (let [previous-commands (->> gtp-engine :history (map :command) vec)]
    (with-programs
      [gnugo]
      (let [all-commands (conj previous-commands command)
            gnugo-input  (join "\n" all-commands)
            ;; gnugo is called with a hardcoded seed to (hopefully) force referential transparency
            gnugo-output (gnugo "--mode" "gtp" "--seed" "1234" {:in gnugo-input :timeout 10000})
            all-responses (extract-responses gnugo-output)
            response (last all-responses)]
        (if-let [error-message (some error-response? all-responses)]
          (throw (ex-info (format "%s: %s" command error-message) gtp-engine))
          (update-in gtp-engine [:history] conj {:command command :response response}))))))

(defn new-engine
  "Creates a new GTP engine with an empty history"
  []
  {:history []})

(defn last-message
  "Returns the last response message from the GTP engine"
  [gtp-engine]
  (-> gtp-engine :history last :response :message))

(defn play
  "Puts a stone of a given color on a given vertex"
  [gtp-engine color vertex]
  (let [play-command (format "play %s %s" color vertex)]
    (run-command gtp-engine play-command)))

(defn genmove
  "Generates a move for a given color"
  [gtp-engine color]
  (let [genmove-command (format "genmove %s" color)]
    (run-command gtp-engine genmove-command)))
