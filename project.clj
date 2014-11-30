(defproject
  org.clojars.ludug3r0/gnugo-gtp "0.2.0-SNAPSHOT"
  :description "A Clojure library designed to communicate with GNU Go through the GTP protocol."
  :url "https://github.com/ludug3r0/gnugo-gtp"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [me.raynes/conch "0.8.0"]
                 [org.clojars.ludug3r0/go-rules "0.0.1-SNAPSHOT"]
                 [prismatic/schema "0.3.3"]]
  :scm {:name "git"
        :url  "https://github.com/ludug3r0/gnugo-gtp"}
  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins      [[lein-midje "3.1.1"]]}})
