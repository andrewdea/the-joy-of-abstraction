(ns n-hour-clock.core
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
  (let [num 4]
    {:num num
     :goal num
     :start (q/millis)
     :displayed-num (str num)
     :awaiting-input false}))

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

(defn within-text-box? []
  (and (> (q/mouse-x) 175)
       (< (q/mouse-x) 275)
       (> (q/mouse-y) 450)
       (< (q/mouse-y) 650)))


(defn check-time [start & {:keys [wait] :or {wait 100}}]
  (> (- (q/millis) start) wait))

(defn check-keyboard [{:keys [num goal displayed-num start awaiting-input]
                       :as state}]
  ;; (js/console.log "in check keyboard")
  ;; (js/console.log "awaiting-input : " awaiting-input)
  ;; (js/console.log "displayed-num : " displayed-num)
  ;; (js/console.log "(type displayed-num) : " (type displayed-num))
  ;; (js/console.log "(re-matches digit displayed-num) : " (re-matches
  ;; #"\d*" displayed-num))
  (if (and awaiting-input (q/key-pressed?) (check-time start :gap
                                                       200))
    (let [key (q/raw-key)
          parsed (if (re-matches #"\d*" displayed-num) (js/parseInt displayed-num) num)]
      ;; (js/console.log "(q/raw-key) : " (q/raw-key))
      ;; (js/console.log "key-code : " key-code)
      ;; (js/console.log "(= 8 key-code) : " (= 8 key-code))
      ;; (js/console.log "displayed-num : " displayed-num)
      ;; (js/console.log "parsed: " (if (re-matches #"\d*" displayed-num) (js/parseInt displayed-num) num))
      (cond
        (= "\r" key)
        {:num num
         :goal parsed
         :displayed-num
         (if (re-matches #"\d*" displayed-num) displayed-num (str num))
         :start (q/millis)
         :awaiting-input false}
        (= "Backspace" key)
        (if (= "..." displayed-num)
          {:num num
           :goal goal
           :displayed-num (str num)
           :start (q/millis)
           :awaiting-input false}
          {:num num
           :goal goal
           :displayed-num (if (> (.-length displayed-num) 1)
                            (subs displayed-num 0 (- (.-length displayed-num) 1))
                            "...")
           :start (q/millis)
           :awaiting-input awaiting-input})
        (re-matches #"\d*" key)
        {:num num
         :goal goal
         :displayed-num (str
                         (if (re-matches #"\d*" displayed-num) displayed-num "")
                         key)
         :start (q/millis)
         :awaiting-input awaiting-input}
        :else
        {:num num
         :goal goal
         :displayed-num (str num)
         :start start
         :awaiting-input false}
        ))
    state))

(defn increment [{:keys [num goal]}]
  (let [updated-num (+ 1 num)
        updated-goal (if (> goal updated-num) goal updated-num)]
    {:num updated-num
     :start (q/millis)
     :displayed-num (str updated-goal)
     :goal updated-goal
     :awaiting-input false}))

(defn decrement [{:keys [num goal]}]
  (let [updated-num (if (> num 0)
                      (- num 1)
                      num)
        updated-goal (if (< goal updated-num) goal updated-num)]
    {:num updated-num
     :start (q/millis)
     :goal updated-goal
     :displayed-num (str updated-goal)
     :awaiting-input false}))

(defn check-mouse [{:keys [num start goal] :as state}]
  (cond
      (within-plus-circle?) (increment state)
      (within-minus-circle?) (decrement state)
      (within-text-box?) {:num num
                          :start start
                          :goal goal
                          :displayed-num "..."
                          :awaiting-input true}
      :else state))

(defn check-goal [{:keys [goal num] :as state}]
  (cond
    (> goal num)
    (increment state)
    (< goal num)
    (decrement state)
    :else
    state))

(defn update-state [{:keys [awaiting-input start] :as state}]
  (cond
    (and (q/mouse-pressed?) (check-time start))
    (check-mouse state)

    (and awaiting-input (q/key-pressed?)
         (check-time start :gap 300))
    (check-keyboard state)

    :else
    (check-goal state)))

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
  (js/console.log "At line number: 164; num : " num)
  (let [radius 70] ; Radius of each smaller circle
    (doseq [i (range num)]
      (let [angle
            (+ (/ Math/PI -2) ; Start at the top (-90 degrees)
                   (* i (/ (* 2 Math/PI) num))) ; Increment clockwise
            x (+ 250 (* outer-radius (Math/cos angle)))
            y (+ 250 (* outer-radius (Math/sin angle)))
            circle-color-value
            (/ (* (+ (/ Math/PI 2) angle) (/ 255 Math/PI)) 2)
            text (str (+ 1 i))]
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

(defn box-with-num [displayed-num awaiting-input]
  ;; rectangle
  (if awaiting-input
    (q/fill 0 0 255)
    (q/fill 0 0 200))
  (q/rect 175 500 150 100 20)
  ;; write number
  (q/text-size 20)
  (apply q/fill [0,0,0])
  (q/text-align :center)
  (q/text displayed-num 250 550)
  ;; circles (buttons to-be) on each side
  (if (and (q/mouse-pressed?) (within-plus-circle?))
    (q/fill 0 0 200)
    (q/fill 0 0 180))
  (q/ellipse 360 550 70 70)
  (q/fill 0 0 0)
  (q/text-size 20)
  (q/text-align :center)
  (q/text "+" 360 550)

  (if (and (q/mouse-pressed?) (within-minus-circle?))
    (q/fill 0 0 200)
    (q/fill 0 0 180))
  (q/ellipse 140 550 70 70)
  (apply q/fill [0,0,0])
  (q/text-size 20)
  (q/text-align :center)
  (q/text "-" 140 550))

(defn draw-state [{:keys [num displayed-num awaiting-input]}]
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
  (box-with-num displayed-num awaiting-input))

; this function is called in index.html
(defn ^:export run-sketch []
  (q/defsketch n-hour-clock
    :host "n-hour-clock"
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
