(ns pixi.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [pixilib :as lib]
            [cljs.core.async :refer [chan <! put!]]))

(def P js/PIXI)
(def -Stage (.-Stage P))
(def -Texture (.-Texture P))
(def -Sprite (.-Sprite P))

(defn stage [background-color]
  (-Stage. background-color))

(defn autoDetectRenderer [width height & options]
  (.autoDetectRenderer P width height (clj->js options)))

(defn texture-from-image [path] (.fromImage -Texture path))

(defn sprite [state key-path texture]
  (let [s (-Sprite. texture)
        current-state (get-in @state key-path)
        {:keys [position anchor rotation] :or {position {:x 0 :y 0}
                                               anchor {:x 0 :y 0}
                                               rotation 0}} current-state]
    (set! (.-x (.-position s)) (:x position))
    (set! (.-y (.-position s)) (:y position))
    (set! (.-x (.-anchor s)) (:x anchor))
    (set! (.-y (.-anchor s)) (:y anchor))
    (set! (.-rotation s) rotation)

    (add-watch state :test (fn [k r o n]
                             (let [{:keys [position anchor rotation] :or {position {:x 0 :y 0} anchor {:x 0 :y 0} rotation 0}} (get-in n key-path)]
                               (set! (.-x (.-position s)) (:x position))
                               (set! (.-y (.-position s)) (:y position))
                               (set! (.-x (.-anchor s)) (:x anchor))
                               (set! (.-y (.-anchor s)) (:y anchor))
                               (set! (.-rotation s) rotation))))

    s))

(def ^:dynamic *game-state*)
(def ^:dynamic *update-state*)
(def ^:dynamic *stage*)
(def ^:dynamic *renderer*)


(defn- mutate-ge! [game-entity entity-state]
  (let [{:keys [position anchor rotation]} entity-state]
    (set! (.-x (.-position game-entity)) (:x position))
    (set! (.-y (.-position game-entity)) (:y position))
    (set! (.-x (.-anchor game-entity)) (:x anchor))
    (set! (.-y (.-anchor game-entity)) (:y anchor))
    (set! (.-rotation game-entity) rotation)))

(defn update-ge! [game-entity entity-state]
  (mutate-ge! game-entity entity-state))

(defn make-entity [entity-state]
  (let [{:keys [texture]} entity-state
        tex (texture-from-image texture)
        sprite (-Sprite. tex)]
    (mutate-ge! sprite entity-state)
    sprite))

(def game-entities (atom {}))

(defn render! [{:keys [entities]}]
  (->> entities
       (map (fn [[id e]]
              (let [ge (get @game-entities id)]
                (if ge
                  (update-ge! ge e)
                  (let [new-ent (make-entity e)]
                    (.addChild *stage* new-ent)
                    (swap! game-entities assoc id new-ent))))))
       dorun)
  (.render *renderer* *stage*))

(defn update-world! [time]
  (if *game-state*
    (let [new-state (swap! *game-state* *update-state* time)]
      (render! new-state))))

(def loop-chan (chan))

(defn start! [state update & {:keys [background-color width height]
                              :or {background-color 0x66FF99
                                   width 400
                                   height 300}}]
  (go
    (let [stage (stage background-color)
          renderer (autoDetectRenderer width height)]
      (.appendChild (.-body js/document) (.-view renderer))
      (loop [time (<! loop-chan)]
        (binding [*game-state* state
                  *update-state* update
                  *renderer* renderer
                  *stage* stage]
          (update-world! time))
        (recur (<! loop-chan)))))
  (letfn [(t [time]
             (js/requestAnimationFrame t)
             (put! loop-chan time))]
    (js/requestAnimationFrame t)))


