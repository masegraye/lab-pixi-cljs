(ns pixicljs.core
  (:require [pixi.core :as p]))

(def game-state (atom {:entities {:bunny {:texture "resources/bunny.png"
                                          :position {:x 200 :y 150}
                                          :anchor {:x 0.5 :y 0.5}
                                          :rotation 0}
                                  :bunny-two {:texture "resources/bunny.png"
                                              :position {:x 150 :y 150}
                                              :anchor {:x 0.5 :y 0.5}
                                              :rotation 0.5}}}))

(defn update-state [state time]

  (letfn [(rot [by s] (+ by s))]
    (-> state
        (update-in [:entities :bunny :rotation] (partial rot 0.1))
        (update-in [:entities :bunny-two :rotation] (partial rot 0.2)))))

(defn main []
    (p/start! game-state update-state :width 500 :height 400))
