(ns gnugo-gtp.engine
  (:require [me.raynes.conch :refer [with-programs]]
            [clojure.string :refer [join]]
            [go.schema :as schema]
            [go.models :as m]
            [schema.core :as s]))

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

(s/defn color->str :- s/Str
  [move :- schema/move]
  (-> move
      m/color
      name))

(s/defn stone->vertex-str :- s/Str
  [stone :- schema/placement]
  (let [vertex (m/move stone)
        column (first vertex)
        line (second vertex)
        column-str (get "abcdefghjklmnopqrst" (dec column))
        line-str (str line)]
    (str column-str line-str)))

(s/defn move->vertex-str :- s/Str
  [move :- schema/move]
  (cond
    (m/is-placement? move) (stone->vertex-str move)
    (m/is-pass? move) "pass"
    :else (throw (ex-info "cannot undestand move" {:move move}))))

(s/defn ->genmove-command :- s/Str
  [color :- schema/color]
  (format "genmove %s" (name color)))

(s/defn ->play-command :- s/Str
  [move :- schema/move]
  (let [color (color->str move)
        vertex (move->vertex-str move)]
    (format "play %s %s" color vertex)))

(defn- ->gtp-commands
  [game]
  (vec (map ->play-command game)))

(defn- run-command
  [game command]
  (let [previous-commands (->gtp-commands game)]
    (with-programs
      [gnugo]
      (let [all-commands (conj previous-commands command)
            gnugo-input (join "\n" all-commands)
            ;; gnugo is called with a hardcoded seed to (hopefully) force referential transparency
            gnugo-output (gnugo "--mode" "gtp" "--seed" "1234" {:in gnugo-input :timeout 10000})
            all-responses (extract-responses gnugo-output)
            response (last all-responses)
            ]
        (if-let [error-message (some error-response? all-responses)]
          (throw (ex-info (format "%s: %s" command error-message) {:previous-commands previous-commands :command command}))
          (-> response :message))))))

(s/defn vertex->coordinates :- schema/vertex
  [vertex :- s/Str]
  (let [column (-> vertex first str keyword)
        line (->> vertex rest (apply str) Integer.)
        column-mapper {:A 1 :B 2 :C 3 :D 4 :E 5 :F 6 :G 7 :H 8
                       :J 9 :K 10 :L 11 :M 12 :N 13 :O 14 :P 15 :Q 16 :R 17 :S 18 :T 19}]
    [(-> column column-mapper) line]))

(s/defn genmove-response->move :- schema/move
  [color :- schema/color
   vertex :- s/Str]
  (case vertex
    "pass" [color :pass]
    "resign" [color :resign]
    [color (vertex->coordinates vertex)]))

(s/defn genmove :- schema/game
  "Generates a move for a given color"
  [game :- schema/game
   color :- schema/color]
  (let [genmove-command (->genmove-command color)
        genmove-response (run-command game genmove-command)
        move (genmove-response->move color genmove-response)]
    (conj game move)))



