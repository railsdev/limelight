;- Copyright © 2008-2011 8th Light, Inc. All Rights Reserved.
;- Limelight and all included source files are distributed under terms of the MIT License.

(ns limelight.clojure.production
  (:use
    [limelight.clojure.core]
    [limelight.clojure.theater]
    [limelight.clojure.stage-building :only (build-stages)]
    [limelight.clojure.style-building :only (build-styles)]
    [limelight.clojure.scene :only (new-scene)]
    [limelight.clojure.util :only (read-src map-for-clojure)]
    [limelight.clojure.theater :only (new-theater)])
  (:require
    [limelight.clojure.production-player]
    [limelight.clojure.prop-building]))

(defn- load-production-helper [production]
  (let [helper-path (resource-path production "helper.clj")
        fs (limelight.Context/fs)
        helper-src (if (.exists fs helper-path) (.readTextFile fs helper-path) nil)]
    (when helper-src
      (binding [*ns* (._helper-ns production)]
        (refer 'clojure.core)
        (refer 'limelight.clojure.core)
        (read-src helper-path helper-src)))))

(defn- prod-illuminate [production]
  (let [player-path (resource-path production "production.clj")
        fs (limelight.Context/fs)
        player-src (if (.exists fs player-path) (.readTextFile fs player-path) nil)]
    (when player-src
      (binding [*ns* (._ns production)
                limelight.clojure.production-player/*production* production]
        (refer 'clojure.core)
        (refer 'limelight.clojure.core)
        (refer 'limelight.clojure.production-player)
        (refer (.getName (._helper-ns production)))
        (read-src player-path player-src)))))

(defn- prod-load-stages [production]
  (let [stages-path (resource-path production "stages.clj")
        fs (limelight.Context/fs)
        stages-src (if (.exists fs stages-path) (.readTextFile fs stages-path) nil)]
    (when stages-src
      (build-stages @(._theater production) stages-src stages-path))))

(defn- prod-load-scene [production scene-path options]
  (let [fs (limelight.Context/fs)
        options (map-for-clojure options)
        backstage (or (:backstage options) {})
        prop-params (or (:prop-params options) {})
        options (dissoc options :prop-params)
        prop-params (merge backstage prop-params)
        scene (new-scene options :production production)
        props-path (resource-path scene "props.clj")
        props-src (if (.exists fs props-path) (.readTextFile fs props-path) nil)]
    (when props-src
      (build scene props-src (assoc prop-params :source-file props-path :root-path (path production))))
    scene))

(defn- prod-load-styles [production path extendable-styles]
  (let [styles-path (str path "/styles.clj")
        fs (limelight.Context/fs)
        styles-src (if (.exists fs styles-path) (.readTextFile fs styles-path) nil)]
    (if styles-src
      (build-styles {} styles-src styles-path extendable-styles)
      {})))

(deftype Production [_peer _theater _ns _helper-ns]

  limelight.model.api.ProductionProxy
  (send [this name args] nil)
  (getTheater [this] @_theater)
  (illuminate [this] (prod-illuminate this))
  (loadLibraries [this])
  (loadStages [this] (prod-load-stages this))
  (loadScene [this scene-path options] (prod-load-scene this scene-path options))
  (loadStyles [this path extendable-styles] (prod-load-styles this path extendable-styles))

  limelight.clojure.core.ResourceRoot
  (resource-path [this resource] (.pathTo (limelight.Context/fs) (.getPath _peer) resource))

  limelight.clojure.core.Pathed
  (path [this] (.getPath _peer))

  clojure.lang.Named
  (getName [this] (.getName _peer))

  limelight.clojure.core.TheaterSource
  (theater [this] @_theater)

  limelight.clojure.core.ProductionSource
  (production [this] this)

  limelight.clojure.core.Backstaged
  (backstage [this] (.getBackstage _peer))
  (backstage-get [this key] (.get (.getBackstage _peer) key))
  (backstage-put [this key value] (.put (.getBackstage _peer) key value))
  )

(defn new-production [peer]
  (let [ns (create-ns (gensym "limelight.dynamic.production.player-"))
        helper-ns (create-ns (gensym "limelight.dynamic.production.helper-"))
        production (Production. peer (atom nil) ns helper-ns)]
    (reset! (._theater production) (new-theater (.getTheater peer) production))
    (.setProxy peer production)
    (load-production-helper production)
    production))