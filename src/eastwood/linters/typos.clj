(ns eastwood.linters.typos
  (:require [eastwood.util :as util])
  (:import [name.fraser.neil.plaintext diff_match_patch]))

(def ^:private ^diff_match_patch dmp (diff_match_patch.))

(defn levenshtein [s1 s2]
  (.diff_levenshtein dmp (.diff_main dmp s1 s2)))

(defn keyword-typos [{:keys [asts]}]
  (let [freqs (->> asts
                   (mapcat util/ast-nodes)
                   (filter #(and (= :const (:op %))
                                 (true? (:literal? %))
                                 (= :keyword (:type %))))
                   (map :form)
                   frequencies)]
    (for [[kw1 n] freqs
          [kw2 _] freqs
          :let [s1 (name kw1)
                s2 (name kw2)]
          :when (and (= n 1)
                     (not= s1 s2)
                     (< 3 (count s1))
                     (< (levenshtein s1 s2) 2))]
      {:linter :keyword-typos
       :msg (format "Possible keyword typo: %s instead of %s ?" kw1 kw2)})))
