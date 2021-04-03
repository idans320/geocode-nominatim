(ns geocodenominatim.core
  (:require [clj-http.client :as client]
            [clojure.java.io :as io] [clojure.data.csv :as csv]  
            [clojure.data.json :as json]
            [ring.util.codec :as codec])
  (:gen-class))


(def NOMINATIM_URL "https://nominatim.openstreetmap.org/search?")


(defn process-csv [file]
  (with-open [rdr (io/reader file)]
    (doall (csv/read-csv rdr))))

(defn csv-data->maps [csv-data]
  (map zipmap
       (->> (first csv-data) ;; First row is the header
            (map keyword) ;; Drop if you want string keys instead
            repeat)
       (rest csv-data)))


(defn write-csv [path row-data]
  (let [columns (keys (first row-data))
        headers (map name columns)
        rows (mapv #(mapv % columns) row-data)]
    (with-open [file (io/writer path)]
      (csv/write-csv file (cons headers rows)))))

(defn geocode [data] 
  
  (let [request_url (str NOMINATIM_URL (codec/form-encode data) "&format=json")] 
   (first (json/read-str (:body (client/get request_url {:accept :json}))))
  ) 
  )

(defn -main
  "Geocode CSV"
  [x y]
  (write-csv y (remove nil? (map geocode (csv-data->maps (process-csv x)))) )
)

