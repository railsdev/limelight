;- Copyright © 2008-2011 8th Light, Inc. All Rights Reserved.
;- Limelight and all included source files are distributed under terms of the MIT License.

(ns limelight.scene-spec
  (:use
    [speclj.core]
    [limelight.spec-helper]
    [limelight.scene])
  (:import
    [limelight.scene Scene]
    [limelight.casting PlayerRecruiter]))

(describe "Scene"

  (it "has limelight lineage"
    (should (isa? Scene limelight.model.api.SceneProxy)))

  (it "can be created"
    (let [scene (new-scene (hash-map :name "Bill"))]
      (should-not= nil @(.peer scene))
      (should= @(.peer scene) (.getPeer scene))
      (should= limelight.ui.model.ScenePanel (type @(.peer scene)))
      (should= PlayerRecruiter (type @(.player-recruiter scene)))
      (should= @(.player-recruiter scene) (.getPlayerRecruiter @(.peer scene)))))
  )

(run-specs)

