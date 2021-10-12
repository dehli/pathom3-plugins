(ns dehli.pathom3.plugins-test
  (:require [clojure.test :as t]
            [com.wsscode.pathom3.connect.indexes :as pci]
            [com.wsscode.pathom3.connect.operation :as pco]
            [com.wsscode.pathom3.interface.eql :as p.eql]
            [com.wsscode.pathom3.plugin :as p.plugin]
            [dehli.pathom3.plugins :as plugins]
            [spec-tools.data-spec :as ds]))

(pco/defmutation my-mutate
  [params]
  {::plugins/id-remaps ::id
   ::plugins/params-spec (ds/spec ::spec {::body keyword?})}
  {::id :foo
   ::params params})

(pco/defresolver my-resolve
  [{::keys [id]}]
  {::resolved (keyword (str (name id) "-bar"))})

(def ^:private registry
  (-> (p.plugin/register [plugins/params-spec
                          plugins/id-remaps])

      (pci/register [my-mutate
                     my-resolve])))

(t/deftest params-spec
  (t/testing "calls select-spec"
    (let [res (p.eql/process registry `[(my-mutate {::body :a ::extra true})])]
      (t/is (= res {`my-mutate {::id :foo
                                ::params {::body :a}}}))))

  (t/testing "fails when doesn't fulfill spec"
    (t/is (thrown-with-msg?
           Exception
           #"Invalid params"
           (p.eql/process registry `[(my-mutate {::id "fail"})])))))

(t/deftest id-remaps
  (t/testing "Merges :app/id-remaps when temp-id supplied"
    (let [res (p.eql/process registry `[{(my-mutate {::body :a ::id :temp-id})
                                         [::resolved]}])]

      (t/is (= res {`my-mutate {::resolved :foo-bar
                                :app/id-remaps {:temp-id :foo}}})))))
