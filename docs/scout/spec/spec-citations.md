# Cite the specification by anchor, never by section number

How to reference the GraphQL specification in comments and test headers; a wrong reference here rots invisibly.

Newer code cites `https://spec.graphql.org/draft/#sec-<Slug>` — an anchor, for example `#sec-Handling-Field-Errors` or `#sec-OneOf-Input-Objects.Input-Coercion`. Use that form. The specification's own source carries no literal section numbers: they are generated from heading depth, so inserting one heading renumbers everything after it and a citation like "§2.10" silently comes to point at unrelated prose. Older test headers under `modules/language/tests/spec/` still carry that style (`// GraphQL Spec §2.10 — Input Values`); those numbers are not trustworthy and new ones should not be added.

Four different citation targets coexist in the tree — `spec.graphql.org/draft`, `spec.graphql.org/October2021`, `graphql.github.io/graphql-spec/draft` and `graphql.github.io/graphql-spec/June2018` (the last still on many execution-algorithm comments, such as the `ExecuteRequest()`/`CompleteValue()` markers in `DefaultExecutor` and `DefaultFieldSelectionExecutor`). Do not assume a nearby comment's revision is the one you should follow: recent work cites `spec.graphql.org/draft`, and a June 2018 marker next to code is a leftover rather than a statement that the code implements that revision.

Related: graphql-js-parity.md.
