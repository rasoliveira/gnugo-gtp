(defproject
  org.clojars.ludug3r0/gnugo-gtp "0.1.0-SNAPSHOT"
  :description "A Clojure library designed to communicate with GNU Go through the GTP protocol."
  :url "https://github.com/ludug3r0/gnugo-gtp"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [me.raynes/conch "0.8.0"]]
  :scm {:name "git"
        :url  "https://github.com/ludug3r0/gnugo-gtp"}
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}})
