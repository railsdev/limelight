(ns limelight.core-test
  (:use [lazytest.describe :only (describe testing it with)]
    [lazytest.context :only (fn-context)]))

(describe "Limelight"
  (it (= 1 1)))
