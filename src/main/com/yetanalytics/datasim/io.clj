(ns com.yetanalytics.datasim.io
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [com.yetanalytics.datasim.protocols :as p]
            [clojure.data.json :as json]))

(defn read-loc-json
  "Reads in a file from the given location and parses it."
  [record loc]
  (try (with-open [r (io/reader loc)]
         (try
           (p/read-body-fn
            record
            (json/read r
                       :key-fn (partial p/read-key-fn record)
                       :value-fn (partial p/read-value-fn record)))
           (catch Exception e
             (throw (ex-info "Parse Error"
                             {:type ::parse-error
                              :location loc}
                             e)))))
       (catch java.io.IOException e
         (throw (ex-info "I/O Error"
                         {:type ::io-error
                          :location loc}
                         e)))))

(defn write-loc-json
  "Write a record to a location"
  [record loc]
  (try (with-open [w (io/writer loc)]
         (try
           (json/write (p/write-body-fn record)
                       w
                       :key-fn (partial p/write-key-fn record)
                       :value-fn (partial p/write-value-fn record)
                       :escape-slash false
                       :escape-unicode false)
           (catch Exception e
             (throw (ex-info "Parse Error"
                             {:type ::unparse-error
                              :location loc}
                             e)))))
       (catch java.io.IOException e
         (throw (ex-info "I/O Error"
                         {:type ::io-error
                          :location loc}
                         e)))))

(defn write-file-json
  "Write a record to a file"
  [record loc]
  (let [file (io/file loc)]
    (io/make-parents file)
    (write-loc-json record file)))
