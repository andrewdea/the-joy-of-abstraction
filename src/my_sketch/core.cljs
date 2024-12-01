(ns my-sketch.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

;; (defn setup []
;;   ; Set frame rate to 30 frames per second.
;;   (q/frame-rate 30)
;;   ; Set color mode to HSB (HSV) instead of default RGB.
;;   (q/color-mode :hsb)
;;   ; setup function returns initial state. It contains
;;   ; circle color and position.
;;   {:color 0
;;    :angle 0})

(defn setup []
  (q/smooth)
  ;; (q/frame-rate 20)
  ;; (q/text-font (q/create-font "DejaVu Sans" 28 true))
  (q/background 20) ; dark screen background
  ;; Set color mode to HSB (HSV) instead of default RGB.
  (q/color-mode :hsb)
  {:num 4 :start (q/millis)})

(defn within-plus-circle? []
  (and (> (q/mouse-x) 325)
       (< (q/mouse-x) 395)
       (> (q/mouse-y) 515)
       (< (q/mouse-y) 585)))

(defn within-minus-circle? []
  (and (> (q/mouse-x) 105)
       (< (q/mouse-x) 175)
       (> (q/mouse-y) 515)
       (< (q/mouse-y) 585)))

(defn update-state [{:keys [num start] :as state}]
  ;; TODO use millis to slow down the rate of change here: one click
  ;; should reasonably be one inc/decrement
  (if (and (q/mouse-pressed?) (> (- (q/millis) start) 100))
    (cond
      (within-plus-circle?) {:num (+ 1 num)
                             :start (q/millis)}
      (within-minus-circle?) {:num (if (> num 0)
                                     (- num 1)
                                     num)
                              :start (q/millis)}
      :else state)
    state))

;; (defn draw-state [state]
;;   ; Clear the sketch by filling it with light-grey color.
;;   (q/background 240)
;;   ; Set circle color.
;;   (q/fill (:color state) 255 255)
;;   ; Calculate x and y coordinates of the circle.
;;   (let [angle (:angle state)
;;         x (* 150 (q/cos angle))
;;         y (* 150 (q/sin angle))]
;;     ; Move origin point to the center of the sketch.
;;     (q/with-translation [(/ (q/width) 2)
;;                          (/ (q/height) 2)]
;;       ; Draw the circle.
;;       (q/ellipse x y 100 100))))

(defn draw-circle-with-text [x y radius text]
  ;; (q/translate x y)
  (apply q/fill [0,0,139])
  (q/ellipse x y radius radius)
  (apply q/fill [255,0,0])
  (q/text-align :center)
  (q/text text x y))


;; (defn circles-around [num outer-radius]
;;   (let [radius 70] ; Radius of each smaller circle
;;     (doseq [i (range 1 (+ 1 num))]
;;       (let [angle (* (/ Math/PI (/ num 2)) i)  ; Angle for each circle
;;             x (+ 250 (* outer-radius (Math/cos angle)))
;;             y (+ 250 (* outer-radius (Math/sin angle)))]
;;         (q/stroke 255)
;;         ;; (apply q/fill [255,165,0])
;;         (q/fill nil)
;;         (q/ellipse x y
;;                    radius radius)
;;         (apply q/fill [255,0,0])
;;         (q/text-align :center)
;;         (q/text i x y)))))

(defn circles-around [num outer-radius]
  (let [radius 70] ; Radius of each smaller circle
    (doseq [i (range num)]
      (let [angle
            (+ (/ Math/PI -2) ; Start at the top (-90 degrees)
                   (* i (/ (* 2 Math/PI) num))) ; Increment clockwise
            x (+ 250 (* outer-radius (Math/cos angle)))
            y (+ 250 (* outer-radius (Math/sin angle)))
            circle-color-value
            (/ (* (+ (/ Math/PI 2) angle) (/ 255 Math/PI)) 2)
            text (str (if (= 0 i)
                  num
                  i))]
        ;; draw the outline of the small circle
        (q/stroke 255)
        ;; (q/fill nil)
        (q/fill circle-color-value 255 255)
        (q/ellipse x y
                   radius radius)
        ;; write the circle's number
        (q/fill 0 0 0)
        (q/text-size 20)
        (q/text-align :center)
        (q/text text x y)))))

(defn box-with-num [num]
  ;; rectangle
  (q/fill 0 0 200)
  (q/rect 175 500 150 100 20)
  ;; write number
  (q/text-size 20)
  (apply q/fill [0,0,0])
  (q/text-align :center)
  (q/text num 250 550)
  ;; circles (buttons to-be) on each side
  (q/fill 0 0 180)
  (q/ellipse 360 550 70 70)
  (apply q/fill [0,0,0])
  (q/text-size 20)
  (q/text-align :center)
  (q/text "+" 360 550)

  (q/fill 0 0 180)
  (q/ellipse 140 550 70 70)
  (apply q/fill [0,0,0])
  (q/text-size 20)
  (q/text-align :center)
  (q/text "-" 140 550))

(defn draw-state [{:keys [num]}]
  ; Clear the sketch by filling it with light-grey color.
  (q/background 180 100 100)
  ; Set circle color.
  (q/fill 0 0 255)
  ;; draw circle
  (q/ellipse 250 250 500 500)
  ;; (q/text-size 35)

  ;; 2 clock
  ;; (draw-circle-with-text 250 50 20 "1")
  ;; (draw-circle-with-text 250 450 20 "2")

  (circles-around num 200)
  (box-with-num num))

; this function is called in index.html
(defn ^:export run-sketch []
  (q/defsketch my-sketch
    :host "my-sketch"
    :size [500 700]
    ; setup function called only once, during sketch initialization.
    :setup setup
    ; update-state is called on each iteration before draw-state.
    :update update-state
    :draw draw-state
    ; This sketch uses functional-mode middleware.
    ; Check quil wiki for more info about middlewares and particularly
    ; fun-mode.
    :middleware [m/fun-mode]
    ))

; uncomment this line to reset the sketch:
; (run-sketch)
