(ns stuytorrent.core
  (:require [stuytorrent.bencode :as b :refer [parse]])
  (:gen-class))

(defn -main
  [& args]
  (-> args
      first
      parse
      println
      )
  )
