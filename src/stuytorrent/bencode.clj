(ns stuytorrent.bencode
  (:require [clojure.string :as str :refer [split]])
  (:gen-class))

(declare i s l d)

(defn chr->parser
  "returns the correct parsing function"
  [chr]
  (or
    ({\i i, \l l, \d d} chr)
    (and (#{\0 \1 \2 \3 \4 \5 \6 \7 \8 \9} chr) s)))

(defn parse
  "recursively parse an entire bencoded string"
  ([s]
   (let [out (fnext (parse s [])) ]
     (if (= (count out) 1)
       (first out)
       out)))
  ([s soFar]
   (let [fun (chr->parser (first s))]
     (if fun
       (apply parse (fun s soFar))
       [s soFar]))))

(defn i
  "parse integer"
  [s soFar]
  (let [[head tail] (split s #"e" 2)
        value (read-string (subs head 1))
        newString tail
        newSoFar (conj soFar value) ]
    [newString newSoFar]))

(defn s
  "parse string"
  [s soFar]
  (let [[head tail] (split s #":" 2)
        length (read-string head)
        value (subs tail 0 length)
        newString (subs tail length)
        newSoFar (conj soFar value) ]
    [newString newSoFar]))

(defn l
  "parse list"
  [s soFar]
  (let [[newStringWithE contents] (parse (subs s 1) [])
        newString (subs newStringWithE 1)
        newSoFar (conj soFar contents) ]
    [newString newSoFar]))

(defn d
  "parse dictionary"
  [s soFar]
  (let [[newStringWithE contents] (parse (subs s 1) [])
        newString (subs newStringWithE 1)
        newSoFar (conj soFar (apply hash-map contents)) ]
    [newString newSoFar]))
