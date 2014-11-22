(ns gnugo-gtp.test.engine
  (:require [midje.sweet :refer :all]
            [gnugo-gtp.engine :refer :all :as gtp]))

(facts "about extracting responses"
       (fact "empty success messages"
             (#'gtp/extract-responses "= \n\n") => [{:status :success
                                                     :id nil
                                                     :message ""}])
       (fact "usual success message"
             (#'gtp/extract-responses "= description\n\n") => [{:status :success
                                                                :id nil
                                                                :message "description"}])
       (fact "usual error message"
             (#'gtp/extract-responses "? description\n\n") => [{:status :failure
                                                                :id nil
                                                                :message "description"}])
       (fact "messages with id"
             (#'gtp/extract-responses "=42 response\n\n") => [{:status :success
                                                               :id "42"
                                                               :message "response"}])

       (fact "multiline messages"
             (#'gtp/extract-responses "= one\ntwo\n\n") => [{:status :success
                                                             :id nil
                                                             :message "one\ntwo"}])
       (fact "multiple messages"
             (#'gtp/extract-responses "= one\n\n?42 two\n\n") => [{:status :success
                                                                 :id nil
                                                                 :message "one"}
                                                                {:status :failure
                                                                 :id "42"
                                                                 :message "two"}])
       (fact "multiple messages with multilines"
             (#'gtp/extract-responses "? one\ntwo\n\n= three\nfour\nfive\n\n") => [{:status :failure
                                                                                    :id nil
                                                                                    :message "one\ntwo"}
                                                                                   {:status :success
                                                                                    :id nil
                                                                                    :message "three\nfour\nfive"}]))


(fact "engine is capable of generating moves"
      (let [engine (new-engine)]
        (-> (genmove engine "black")
            last-message) => "Q16"))

(fact "engine is aware of previous generated moves"
      (let [engine (new-engine)]
        (-> engine
            (genmove "black")
            (genmove "black")
            last-message) => "Q17"))

(fact "engine is aware of previous played moves"
      (let [engine (new-engine)]
        (-> engine
            (play "black" "Q16")
            (play "white" "D4")
            (genmove "black")
            last-message) => "Q4"))