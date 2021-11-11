# Pathom3 Plugins

This repository exposes some plugins that I use with [`pathom3`][pathom3].
Feel free to include the project via `deps.edn` or copy the plugins directly
([source][source]).

``` clojure
{:deps {dehli/pathom3-plugins {:git/url "git@github.com:dehli/pathom3-plugins.git"
                               :sha "<COMMIT_SHA>"}}}
```

### Id Remaps Plugin

This plugin allows you to specify an `::id-remaps` key on your `defmutation`.
By specifying this, if the consumer passes up a temporary id, `:app/id-remaps`
will be automatically merged in.

### Params Spec Plugin

This plugin allows you to specify a `::params-spec` key on your `defmutation`.
By specifying this, the mutation will only be invoked if the params fulfills
the spec contract. Additionally, [`select-spec`][select-spec] will be called
on params so that extra data isn't sent to the mutation.

[pathom3]: https://pathom3.wsscode.com
[select-spec]: https://cljdoc.org/d/metosin/spec-tools/0.10.5/api/spec-tools.core#select-spec
[source]: https://github.com/dehli/pathom3-plugins/blob/main/src/main/dev/dehli/pathom3/plugins.cljc

# Development

```bash
clj -M:outdated # View outdated dependencies
clj -M:test     # Run tests
```
