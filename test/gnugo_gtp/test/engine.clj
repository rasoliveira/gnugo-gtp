(ns gnugo-gtp.test.engine
  (:require [midje.sweet :refer :all]
            [schema.core :as s]
            [gnugo-gtp.engine :refer :all :as gtp]))



(facts "about extracting responses"
       (fact "empty success messages"
             (#'gtp/extract-responses "= \n\n") => [{:status  :success
                                                     :id      nil
                                                     :message ""}])
       (fact "usual success message"
             (#'gtp/extract-responses "= description\n\n") => [{:status  :success
                                                                :id      nil
                                                                :message "description"}])
       (fact "usual error message"
             (#'gtp/extract-responses "? description\n\n") => [{:status  :failure
                                                                :id      nil
                                                                :message "description"}])
       (fact "messages with id"
             (#'gtp/extract-responses "=42 response\n\n") => [{:status  :success
                                                               :id      "42"
                                                               :message "response"}])

       (fact "multiline messages"
             (#'gtp/extract-responses "= one\ntwo\n\n") => [{:status  :success
                                                             :id      nil
                                                             :message "one\ntwo"}])
       (fact "multiple messages"
             (#'gtp/extract-responses "= one\n\n?42 two\n\n") => [{:status  :success
                                                                   :id      nil
                                                                   :message "one"}
                                                                  {:status  :failure
                                                                   :id      "42"
                                                                   :message "two"}])
       (fact "multiple messages with multilines"
             (#'gtp/extract-responses "? one\ntwo\n\n= three\nfour\nfive\n\n") => [{:status  :failure
                                                                                    :id      nil
                                                                                    :message "one\ntwo"}
                                                                                   {:status  :success
                                                                                    :id      nil
                                                                                    :message "three\nfour\nfive"}]))

(s/with-fn-validation
  (fact "engine is capable of generating moves on a empty game"
        (genmove [] :black) => [[:black [16 16]]])

  (fact "engine can generated move on a game with previous moves"
        (-> [[:black [16 16]] [:white [4 4]]]
            (genmove :black)) => [[:black [16 16]] [:white [4 4]] [:black [16 4]]])

  (fact "engine is aware of previous generated moves"
        (-> []
            (genmove :black)
            (genmove :white)
            (genmove :black)) => [[:black [16 16]] [:white [4 4]] [:black [16 4]]])
  (fact "engine cannot understand resign (gtp limitation)"
        (-> [[:black [16 16]] [:white [4 4]] [:black :resign]]
            (genmove :white)) => (throws Exception)))