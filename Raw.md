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
`foo.bar`, list construction `[a, b, c]`, table construction `{k : v}`, anonymous functions `function(x, y) {...}`, and
table/list indexing `a[e]`. As primitives, you have boolean `true, false`, floats and ints, and strings like `"Hello World"`.
Finally, you also have variables like `x, y, foo_bAr133`.

Every Yuck expression can serve as a statement as well, regardless of whether they have any effect or not. In addition,
you can have variable declarations (either `var id;` or `var id = e;`), function declarations
`function foo() {...}`, while statements `while e {...}`, for loops `for x in e {...}`, if statements
(`if e {...}` or `if e {...} else {...}`), empty statements `;`, and class declarations of the form

```python
class foo {
  var x;
  var y = bar;
  function meh() {
    ...
  }
}
```

While this is natural, as programming language developers, we should be a bit less wishy washy about all of this. 
Let's formalize this grammar in an extension of [BNF](https://en.wikipedia.org/wiki/Backus%E2%80%93Naur_form). In particular, 
we will allow constructs like $e^*$, $e^+$, and $e?$ (which denotes a production 0 or more times, 
a production 1 or more times, and a production 0 or 1 time).

\begin{align*}
\mathrm{expr} &\to \boxed{\tiny \mathrm{level1}} \\
\mathrm{level1} &\to \boxed{\tiny \mathrm{level2}} \left(\mathrm{or} ~ \boxed{\tiny \mathrm{level2}}\right)* \\
\mathrm{level2} &\to \boxed{\tiny \mathrm{level3}} \left(\mathrm{and} ~ \boxed{\tiny \mathrm{level3}}\right)* \\
\mathrm{level3} &\to \boxed{\tiny \mathrm{level4}} \left(\left(< \mid > \mid \le \mid \ge \mid \ne \mid \equiv \right) \boxed{\tiny \mathrm{level4}}\right)* \\
\mathrm{level4} &\to \boxed{\tiny \mathrm{level5}} \left(\mathrm{to} ~ \boxed{\tiny \mathrm{level4}}\right)? \\
\mathrm{level5} &\to \boxed{\tiny \mathrm{level6}} \left(\left(+ \mid -\right) \boxed{\tiny \mathrm{level6}}\right)* \\
\mathrm{level6} &\to \boxed{\tiny \mathrm{level7}} \left(\left(\times \mid / \mid \mathrm{mod}\right) \boxed{\tiny \mathrm{level7}}\right)* \\
\mathrm{level7} &\to \left(- \mid \mathrm{not}\right) \boxed{\tiny \mathrm{level7}} \\
\mathrm{level7} &\to \boxed{\tiny \mathrm{level8}} \\
\mathrm{level8} &\to \boxed{\tiny \mathrm{level9}} \left(\mathrm{pow} ~ \boxed{\tiny \mathrm{level8}}\right)? \\
\mathrm{level9}' &\to \mathrm{.} ~ \mathrm{id} \\
\mathrm{level9}' &\to \boldsymbol{(} \boxed{\tiny \mathrm{args}} \boldsymbol{)} \\
\mathrm{level9}' &\to [ \boxed{\tiny \mathrm{expr}} ] \\
\mathrm{level9} &\to \boxed{\tiny \mathrm{term}} ~ \boxed{\tiny \mathrm{level9}'}* \\
\mathrm{args} &\to \epsilon \\
\mathrm{args} &\to \boxed{\tiny \mathrm{expr}} \left(, \boxed{\tiny \mathrm{expr}}\right)* \\
\mathrm{term} &\to \left(\mathrm{num} \mid \mathrm{string} \mid \mathrm{true} \mid \mathrm{false} \mid \mathrm{id}\right) \\
\mathrm{term} &\to \boldsymbol{(} \boxed{\tiny \mathrm{expr}} \boldsymbol{)} \\
\mathrm{term} &\to \left[ \boxed{\tiny \mathrm{expr}} \left(, \boxed{\tiny \mathrm{expr}}\right)* \right] \\
\mathrm{term} &\to \left[ ~ \right] \\
\mathrm{term} &\to \left\{ \boxed{\tiny \mathrm{expr}} : \boxed{\tiny \mathrm{expr}} \left(, \boxed{\tiny \mathrm{expr}} : \boxed{\tiny \mathrm{expr}}\right)* \right\} \\
\mathrm{term} &\to \left\{ ~ \right\} \\
\mathrm{term} &\to \mathrm{new} \left(\mathrm{id} \left(\mathrm{.} ~ \mathrm{id}\right)*\right) \boldsymbol{(} \boxed{\tiny \mathrm{args}} \boldsymbol{)} \\
\mathrm{term} &\to \mathrm{function} \boldsymbol{(} \boxed{\tiny \mathrm{parameters}} \boldsymbol{)} { \left(\boxed{\tiny \mathrm{statement}}\right)* } \\
\mathrm{parameters} &\to \epsilon \\
\mathrm{parameters} &\to \mathrm{id} \left(, \mathrm{id}\right)* \\
\mathrm{statement} &\to \boxed{\tiny \mathrm{expr}} ; \\
\mathrm{vardecl} &\to \mathrm{var} ~ \mathrm{id} \left(= \boxed{\tiny \mathrm{expr}}\right)? \\
\mathrm{statement} &\to \boxed{\tiny \mathrm{vardecl}} ; \\
\mathrm{fundecl} &\to \mathrm{function} ~ \mathrm{id} \boldsymbol{(} \boxed{\tiny \mathrm{parameters}} \boldsymbol{)} { \left(\boxed{\tiny \mathrm{statement}}\right)* } \\
\mathrm{statement} &\to \boxed{\tiny \mathrm{fundecl}} \\
\mathrm{statement} &\to \mathrm{while} \boxed{\tiny \mathrm{expr}} { \left(\boxed{\tiny \mathrm{statement}}\right)* } \\
\mathrm{statement} &\to \mathrm{for} ~ \mathrm{id} ~ \mathrm{in} \boxed{\tiny \mathrm{expr}} { \left(\boxed{\tiny \mathrm{statement}}\right)* } \\
\mathrm{statement} &\to \mathrm{if} \boxed{\tiny \mathrm{expr}} { \left(\boxed{\tiny \mathrm{statement}}\right)* } \left(\mathrm{else} \left(\boxed{\tiny \mathrm{statement}} \mid { \left(\boxed{\tiny \mathrm{statement}}\right)* } \right)\right)? \\
\mathrm{statement} &\to \mathrm{class} ~ \mathrm{id} { \left(\boxed{\tiny{\mathrm{vardecl}}} ; \mid \boxed{\tiny \mathrm{fundecl}}\right)* } \\
\mathrm{statement} &\to ;
\end{align*}

Here, the grammar we've specified is mostly free of 1-lookahead conflicts, so it's amenable to a LL1 grammar with
explicit conflict resolution. In particular, you will need to resolve conflicts for 

* $\boxed{term}$ at `[`, since it doesn't know whether you want `[]` or `[...]`.  You can resolve this by looking at the
  next character and shifting to `[]` if it's a `]`, and `[expr, (, expr)*]` otherwise.
* $\boxed{term}$ at `{`, which is the same problem as above for `{}` versus `{...}`.
* $\boxed{statement}$ for the token `function`. Here, we're not sure if we want to shift to an expression-statement
  `function(){ ... };` or a function declaration `function id() {...}`. While it's perfectly fine to just ignore the
  first form (since it's effectively a NOP), we can resolve this easily by just looking at the next character, and shifting
  to the expression-statement production iff it's an open parenthesis `(`.
* Within $\textrm{else} \left(\boxed{statement} \mid \left( \{ \boxed{statement}* \} \right)\right)$. For the token
  `{`, it's not entirely clearly whether we should shift to the expression-statement for a table or continue the `else {...}` clause.
  Here, we'll just always shift to the else clause.