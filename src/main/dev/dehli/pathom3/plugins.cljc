(ns dev.dehli.pathom3.plugins
  (:require [clojure.spec.alpha :as s]
            [com.wsscode.pathom3.connect.indexes :as pci]
            [com.wsscode.pathom3.connect.runner :as pcr]
            [com.wsscode.pathom3.format.eql :as pf.eql]
            [com.wsscode.pathom3.plugin :as p.plugin]
            [spec-tools.core :as st]))

(p.plugin/defplugin id-remaps
  "This plugin allows you to specify an ::id-remaps key on your defmutation.
  By specifying this, if the consumer passes up a temporary id, :app/id-remaps
  will be automatically merged in.

  Example Usage:

  (pco/defmutation create-foo
    []
    {::pco/op-name 'foo/create
     ::plugins/id-remaps :foo/id}
    {:foo/id \"bar\"})

  (foo/create {:foo/id \"tmp-id\"})

  {'foo/create {:foo/id \"bar\"
                :app/id-remaps {\"tmp-id\" \"bar\"}}}

  Note: If the client doesn't specify the key, :app/id-remaps won't be merged.
  "
  {::pf.eql/wrap-map-select-entry
   (fn [original]
     (fn [env source {:keys [key params] :as ast}]
       (let [;; If it's not a symbol, then it's not an operation
             {::keys [id-remaps]} (when (symbol? key)
                                    (pci/operation-config env key))
             temp-id (get params id-remaps)
             actual-id (get-in source [key id-remaps])]

         (cond-> (original env source ast)
           (and id-remaps temp-id actual-id)
           (update 1 assoc :app/id-remaps {temp-id actual-id})))))})

(p.plugin/defplugin params-spec
  "This plugin allows you to specify a ::params-spec key on your defmutation.
  By specifying this, the mutation will only be invoked if the params fulfills
  the spec contract. Additionally, select-spec will be called on params so that
  extra data isn't sent to the mutation.
  "
  {::pcr/wrap-mutate
   (fn [mutate]
     (fn [env {:keys [key] :as ast}]
       (let [{::keys [params-spec]} (pci/mutation-config env key)

             ;; Strips extra keys from params so we don't
             ;; validate keys that will end up getting stripped.
             params (cond->> (:params ast)
                      (some? params-spec)
                      (st/select-spec params-spec))]

         (when (and (some? params-spec) (not (s/valid? params-spec params)))
           (throw (ex-info "Invalid params"
                           {:message (s/explain-str params-spec params)
                            :params params})))

         (mutate env (assoc ast :params params)))))})
