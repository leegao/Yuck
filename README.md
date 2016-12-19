# Yuck
### Learning to implement an imperative language in the twenty-first century.

#### Imperative languages? What is this, 1996?

#### Why are you using Java for the Frontend? Are you a masochist?

#### Roadmap

- [ ] Specification of the `yuck` language.
- [x] Turning text into trees.
- [ ] Turning trees into "ycode" programs.
- [ ] Interpreting "ycode" directly.
- [ ] A few linters and static analyzers.
- [ ] Simple peep-hole optimizations.
- [ ] Gradually typing yucky code.
- [ ] Smell-proofing yucky code: contracts and type refinements.
- [ ] Instrumentation and profiling support, or how I stopped worrying about the smell and learned to love yuck.
- [ ] JITing machine code directly from yuck-code: a crash course on contemporary tracing dynamic compilers.

## Formal Specification

### Yuck Grammar

Yuck is a simple imperative language. It is obnoxiously intuitive and natural for those who are familiar with
the mainstream dynamic imperative languages of the twenty-first century.

At a first glance, Yuck has statements and expressions. Expressions are computations that outputs some
value whereas statements do not.

Within Yuck expressions, you'll find your usual binary operations such as the arithmetic operators `+, -, *, /, mod, **`,
logical operator `and, or`, comparisons `<, >, ==, !=, etc`, and a builtin range construct `a to b`. Additionally, you
have other compound expressions like unary operators for `-` and `not`, function calls `f(e, e)`, 
object instantiations `new Foo(e)`, attribute selection
`foo.bar = e`, list construction `[a, b, c]`, table construction `{k : v}`, anonymous functions `function(x, y) {...}`, and
table/list indexing `a[e]`. As primitives, you have boolean `true, false`, floats and ints, and strings like `"Hello World"`.
Finally, you also have variables like `x, y, foo_bAr133`.