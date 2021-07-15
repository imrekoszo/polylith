(ns polylith.clj.core.lib.size
  (:require [polylith.clj.core.lib.git-size :as git-size]
            [polylith.clj.core.lib.mvn-size :as mvn-size]
            [polylith.clj.core.lib.local-size :as local-size]))

(defn with-size [[name {:keys [mvn/version local/root git/url sha] :as value}] entity-root-path user-home]
  (let [[lib-name lib] (cond
                         version (mvn-size/with-size name version value)
                         root (local-size/with-size-and-version name root value entity-root-path)
                         url (git-size/with-size-and-version name sha value user-home))]
    [(str lib-name) lib]))

(defn with-sizes-vec [entity-root-path libraries user-home]
  (mapv #(with-size % entity-root-path user-home)
       libraries))
