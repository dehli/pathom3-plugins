(ns dehli.pathom3.plugins-test
  (:require [clojure.test :as t]
            [com.wsscode.pathom3.connect.indexes :as pci]
            [com.wsscode.pathom3.connect.operation :as pco]
            [com.wsscode.pathom3.connect.runner :as pcr]
            [com.wsscode.pathom3.interface.eql :as p.a.eql]
            [com.wsscode.pathom3.plugin :as p.plugin]
            [dehli.pathom3.plugins :as plugins]
            [spec-tools.data-spec :as ds]))

(pco/defmutation mutate
  [params]
  {::plugins/params-spec (ds/spec ::spec {::id keyword?})}
  {::params params})

(def ^:private registry
  (-> (p.plugin/register [plugins/params-spec])
      (pci/register [mutate])))

(t/deftest params-spec
  (t/testing "calls select-spec"
    (let [res (p.a.eql/process registry
                               `[(mutate {::id :a ::extra true})])]
      (t/is (= res `{mutate {::params {::id :a}}}))))

  (t/testing "fails when doesn't fulfill spec"
    (let [res (p.a.eql/process registry
                               `[(mutate {::id "fail"})])]
      (t/is (some? (get-in res [`mutate ::pcr/mutation-error]))))))