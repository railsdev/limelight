;- Copyright © 2008-2011 8th Light, Inc. All Rights Reserved.
;- Limelight and all included source files are distributed under terms of the MIT License.

(ns limelight.clojure.scene
  (:use
    [limelight.clojure.casting :only (new-player-recruiter)]
    [limelight.clojure.util :only (map-for-java)]
    [limelight.clojure.core]))

(deftype Scene [peer player-recruiter]

  limelight.model.api.SceneProxy
  (getPeer [this] @peer)
  (applyOptions [this options] nil)

  ResourceRoot
  (resource-path [this resource]
    (.pathTo (limelight.Context/fs) (.getPath @peer) resource)))


(defn new-scene [options]
  (let [scene (Scene. (atom nil) (atom nil))
        player-recruiter (new-player-recruiter scene)
        peer (limelight.ui.model.ScenePanel. scene)]
    (reset! (.peer scene) peer)
    (reset! (.player-recruiter scene) player-recruiter)
    (.addOptions peer (map-for-java options))
    (.setPlayerRecruiter peer player-recruiter)
    scene))
