(ns polylith.clj.core.migrator.brick-deps
  (:require [polylith.clj.core.common.interface :as common]
            [polylith.clj.core.migrator.shared :as shared]))

(defn as-tools-deps [{:keys [type version path git/url sha]}]
  (case type
    "maven" (shared/hmap {:mvn/version version})
    "local" (shared/hmap {:local/root path})
    "git"   (shared/hmap {:git/url url, :sha sha})
    "missing" (shared/hmap {:mvn/version "INSERT-VERSION-HERE"})))

(defn lib-dep [[lib dep] lib->dep]
  [(symbol lib)
   (as-tools-deps (if (empty? dep)
                    (lib->dep lib {:type "missing"})
                    dep))])

(defn deps-content [{:keys [name paths lib-deps]} lib->dep]
  (let [src-paths (:src paths)
        test-paths (:test paths)
        deps (shared/hmap (into {} (map #(lib-dep % lib->dep)
                                        (:src lib-deps))))]
    (shared/format-content name
                           [:paths :deps :aliases]
                           {:paths src-paths
                            :deps deps
                            :aliases {:test {:extra-paths test-paths
                                             :extra-deps {}}}})))

(defn create-config-file [ws-dir {:keys [name type] :as brick} lib->dep]
  (let [path (str ws-dir "/" type "s/" name "/deps.edn")
        content (deps-content brick lib->dep)]
    (spit path content)))

(defn create-config-files [ws-dir {:keys [bases components projects]}]
  (let [lib->dep (-> (common/find-project "development" projects) :lib-deps :src)]
    (doseq [brick (concat bases components)]
      (create-config-file ws-dir brick lib->dep))))
