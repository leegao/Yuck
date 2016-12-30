# Yuck
### Learning to implement an imperative language in the twenty-first century.

#### Imperative languages? What is this, 1996?

#### Why are you using Java for the Frontend? Are you a masochist?

#### Roadmap

- [x] Specification of the `yuck` language.
- [x] Turning text into trees.
- [x] Turning trees into "ycode" programs.
- [x] Interpreting "ycode" directly.
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

#### LL(1) Grammar

\begin{align*}
\mathrm{expr} &\to \boxed{\scriptstyle \mathrm{level1}} \\
\mathrm{level1} &\to \boxed{\scriptstyle \mathrm{level2}} \left(\mathrm{or} ~ \boxed{\scriptstyle \mathrm{level2}}\right)* \\
\mathrm{level2} &\to \boxed{\scriptstyle \mathrm{level3}} \left(\mathrm{and} ~ \boxed{\scriptstyle \mathrm{level3}}\right)* \\
\mathrm{level3} &\to \boxed{\scriptstyle \mathrm{level4}} \left(\left(< \mid > \mid \le \mid \ge \mid \ne \mid \equiv \right) \boxed{\scriptstyle \mathrm{level4}}\right)* \\
\mathrm{level4} &\to \boxed{\scriptstyle \mathrm{level5}} \left(\mathrm{to} ~ \boxed{\scriptstyle \mathrm{level4}}\right)? \\
\mathrm{level5} &\to \boxed{\scriptstyle \mathrm{level6}} \left(\left(+ \mid -\right) \boxed{\scriptstyle \mathrm{level6}}\right)* \\
\mathrm{level6} &\to \boxed{\scriptstyle \mathrm{level7}} \left(\left(\times \mid / \mid \mathrm{mod}\right) \boxed{\scriptstyle \mathrm{level7}}\right)* \\
\mathrm{level7} &\to \left(- \mid \mathrm{not}\right) \boxed{\scriptstyle \mathrm{level7}} \\
\mathrm{level7} &\to \boxed{\scriptstyle \mathrm{level8}} \\
\mathrm{level8} &\to \boxed{\scriptstyle \mathrm{level9}} \left(\mathrm{pow} ~ \boxed{\scriptstyle \mathrm{level8}}\right)? \\
\mathrm{level9}' &\to \mathrm{.} ~ \mathrm{id} \\
\mathrm{level9}' &\to \boldsymbol{(} \boxed{\scriptstyle \mathrm{args}} \boldsymbol{)} \\
\mathrm{level9}' &\to [ \boxed{\scriptstyle \mathrm{expr}} ] \\
\mathrm{level9} &\to \boxed{\scriptstyle \mathrm{term}} ~ \boxed{\scriptstyle \mathrm{level9}'}* \\
\mathrm{args} &\to \epsilon \\
\mathrm{args} &\to \boxed{\scriptstyle \mathrm{expr}} \left(, \boxed{\scriptstyle \mathrm{expr}}\right)* \\
\mathrm{term} &\to \left(\mathrm{nil} \mid \mathrm{num} \mid \mathrm{string} \mid \mathrm{true} \mid \mathrm{false} \mid \mathrm{id}\right) \\
\mathrm{term} &\to \boldsymbol{(} \boxed{\scriptstyle \mathrm{expr}} \boldsymbol{)} \\
\mathrm{term} &\to \left[ \boxed{\scriptstyle \mathrm{expr}} \left(, \boxed{\scriptstyle \mathrm{expr}}\right)* \right] \\
\mathrm{term} &\to \left[ ~ \right] \\
\mathrm{term} &\to \left\{ \boxed{\scriptstyle \mathrm{expr}} : \boxed{\scriptstyle \mathrm{expr}} \left(, \boxed{\scriptstyle \mathrm{expr}} : \boxed{\scriptstyle \mathrm{expr}}\right)* \right\} \\
\mathrm{term} &\to \left\{ ~ \right\} \\
\mathrm{term} &\to \mathrm{new} \left(\mathrm{id} \left(\mathrm{.} ~ \mathrm{id}\right)*\right) \boldsymbol{(} \boxed{\scriptstyle \mathrm{args}} \boldsymbol{)} \\
\mathrm{term} &\to \mathrm{function} \boldsymbol{(} \boxed{\scriptstyle \mathrm{parameters}} \boldsymbol{)} { \left(\boxed{\scriptstyle \mathrm{statement}}\right)* } \\
\mathrm{parameters} &\to \epsilon \\
\mathrm{parameters} &\to \mathrm{id} \left(, \mathrm{id}\right)* \\
\mathrm{statement} &\to \boxed{\scriptstyle \mathrm{expr}} ; \\
\mathrm{vardecl} &\to \mathrm{var} ~ \mathrm{id} \left(= \boxed{\scriptstyle \mathrm{expr}}\right)? \\
\mathrm{statement} &\to \boxed{\scriptstyle \mathrm{vardecl}} ; \\
\mathrm{fundecl} &\to \mathrm{function} ~ \mathrm{id} \boldsymbol{(} \boxed{\scriptstyle \mathrm{parameters}} \boldsymbol{)} { \left(\boxed{\scriptstyle \mathrm{statement}}\right)* } \\
\mathrm{statement} &\to \boxed{\scriptstyle \mathrm{fundecl}} \\
\mathrm{statement} &\to \mathrm{while} \boxed{\scriptstyle \mathrm{expr}} { \left(\boxed{\scriptstyle \mathrm{statement}}\right)* } \\
\mathrm{statement} &\to \mathrm{for} ~ \mathrm{id} ~ \mathrm{in} \boxed{\scriptstyle \mathrm{expr}} { \left(\boxed{\scriptstyle \mathrm{statement}}\right)* } \\
\mathrm{statement} &\to \mathrm{if} \boxed{\scriptstyle \mathrm{expr}} { \left(\boxed{\scriptstyle \mathrm{statement}}\right)* } \left(\mathrm{else} \left(\boxed{\scriptstyle \mathrm{statement}} \mid { \left(\boxed{\scriptstyle \mathrm{statement}}\right)* } \right)\right)? \\
\mathrm{statement} &\to \mathrm{class} ~ \mathrm{id} { \left(\boxed{\scriptstyle{\mathrm{vardecl}}} ; \mid \boxed{\scriptstyle \mathrm{fundecl}}\right)* } \\
\mathrm{statement} &\to ;
\end{align*}

Here, the grammar we've specified is mostly free of 1-lookahead conflicts, so it's amenable to a LL1 grammar with
explicit conflict resolution. In particular, you will need to resolve conflicts for 

* $\to\boxed{term}$ at `[`, since it doesn't know whether you want `[]` or `[...]`.  You can resolve this by looking at the
  next character and shifting to `[]` if it's a `]`, and `[expr, (, expr)*]` otherwise.
* $\to\boxed{term}$ at `{`, which is the same problem as above for `{}` versus `{...}`.
* $\to\boxed{statement}$ for the token `function`. Here, we're not sure if we want to shift to an expression-statement
  `function(){ ... };` or a function declaration `function id() {...}`. While it's perfectly fine to just ignore the
  first form (since it's effectively a NOP), we can resolve this easily by just looking at the next character, and shifting
  to the expression-statement production iff it's an open parenthesis `(`.
* Within $\textrm{else} \left(\boxed{statement} \mid \left( \{ \boxed{statement}* \} \right)\right)$. For the token
  `{`, it's not entirely clearly whether we should shift to the expression-statement for a table or continue the `else {...}` clause.
  Here, we'll just always shift to the else clause.
  
#### Natural Grammar

For the sake of analysis, it's often easier to give a grammar specification that, while ambiguous, captures just
the structure of our language. Here, we will give the specification of our language as an inductive class over the
set of $\to\boxed{e}$, expressions, and $\to\boxed{s}$, statements.

The expressions are given by
\begin{align*}
e &\to x \in \mathcal{V} \mid \mathrm{nil} \mid n \in \mathbb{R} \mid \textrm{true} \mid \textrm{false} \mid \left[ e^* \right] \\
& \mid \left\{ \left(e_k : e_v\right)^* \right\} \mid \mathrm{new} ~ x(e^*) \mid \mathrm{function}(x^*) \{ s^* \} \\
& \mid e.x \mid e_f\left(e_{\scriptstyle \mathrm{args}}^*\right) \mid e_0\left[e_{\scriptstyle\mathrm{index}}\right] \\
& \mid e_0 \oplus e_1 \mid \neg e \mid -e \mid e_0 \sqcup e_1 \mid e_0 ~\mathrm{to}~ e_1 \mid e_0 \sqsubseteq e_1
\end{align*}
where $\oplus$ denotes binary arithmetic operators, $\sqcup$ denotes logical binary operators, and $\sqsubseteq$ denotes
binary comparison operators.

Similarly, the statements are given by
\begin{align*}
s &\to e \in \mathrm{expr} \mid \mathrm{var}~ x \mid \mathrm{var}~ x = e \mid \mathrm{function}~ f(x^*) \{ s^* \} \\
&\mid \mathrm{while}~e_{c}~\left\{ s^* \right\} \mid \mathrm{for}~x~\mathrm{in}~e~\left\{ s^* \right\} \\
&\mid \mathrm{if}~e_c ~\left\{ s^* \right\} \mid \mathrm{if}~e_c ~\left\{ s^* \right\}~\mathrm{else}~\left\{s^*\right\} \\
&\mid \mathrm{class}~C~\left\{ \left(\mathrm{var}~ x = e? \mid \mathrm{function}~ f(x^*) \{ s^* \}\right)^* \right\}
\end{align*}

While this grammar may not be easily implementable using your everyday flavor of parser generators, it does have
the advantage that it is compact and it gives you an inductive construction. We can take the structure defined here
and use it to construct an operational semantic for this language to reveal the types of information that we will
have to carry around in order to fully execute this program.

### Semantics

#### Simple Operational Semantics (Big Step)

We will give the operational semantics in terms of inferences rules. Here, the sequent
$$
\inferrule{A \\ B \\ C}{D}
$$
says that if $A, B, C$ all hold, then we can deduce $D$. As we will see, it's very natural to specify the semantics
of a language in terms of these inference rules.

Let $\sigma, \xi \vdash e \Downarrow_e v \mid \xi'$ denote the "execution" of a Yuck expression $e$ in contexts $\sigma$
(for local variables) and $\xi$ (for the heap of objects). Since expressions may, in general, have side-effects, we also have to output
the potentially altered contexts. Their semantics are given by
\begin{mathpar}
\inferrule*[right=Var]{\sigma(x) = v}{\sigma, \xi \vdash x \in \mathcal{V} \Downarrow_e v \mid \xi} \and
\inferrule*[right=Num]{~}{\sigma, \xi \vdash n \in \mathbb{R} \Downarrow_e n \mid \xi} \and
\inferrule*[right=True]{~}{\sigma, \xi \vdash \mathrm{true} \Downarrow_e \mathrm{true} \mid \xi} \and
\inferrule*[right=False]{~}{\sigma, \xi \vdash \mathrm{false} \Downarrow_e \mathrm{false}\mid \xi} \and
\inferrule*[right=List]{\sigma, \xi \vdash e_0 \Downarrow_e v_0 \mid \xi_0 \\ \cdots \\ \sigma, \xi_{n-1} \vdash e_n \Downarrow_e v_n \mid \xi_n}{\sigma, \xi \vdash \left[ e_0, \cdots, e_n \right] \Downarrow_e \left[ v_0, \cdots, v_n \right] \mid \xi_n} \and
\inferrule*[right=Table]{\sigma, \xi \vdash e_{k_0} \Downarrow_e v_{k_0} \mid \xi'_0  ~~~
\sigma, \xi'_0 \vdash e_{v_0} \Downarrow_e v_{v_0} \mid \xi_0 \\\\
\cdots \\\\
\sigma, \xi_{n-1} \vdash e_{k_n} \Downarrow_e v_{k_n} \mid \xi'_n ~~~~
\sigma, \xi'_n \vdash e_{v_n} \Downarrow_e v_{v_n} \mid \xi_n}
{\sigma, \xi \vdash \left\{ e_{k_0} : e_{v_0}, \cdots, e_{k_n} : e_{v_n} \right\} \Downarrow_e \left[ v_{k_0} : v_{v_0}, \cdots, v_{k_n} : v_{v_n} \right] \mid \xi_n} \and
\inferrule*[right=New]{\sigma, \xi_{k-1} \vdash e_k \Downarrow_e v_k \mid \xi_k \\ \mathrm{Bar} \in \mathcal{C}(\sigma) \\ v_{o}, \xi' = \mathrm{malloc}(\xi_n, \mathrm{Bar}) \\ \sigma, \xi' \vdash v_o.\mathrm{init}(v_0, \cdots, v_n) \Downarrow_s \sigma, \xi_{out}}{\sigma, \xi \vdash \mathrm{new}~ \mathrm{Bar}(e_0, \dots, e_n) \Downarrow_e v_{o} \mid \xi_{out})} \and
\inferrule*[right=Fun]{\mathrm{fvs}(s_0, \dots, s_n) - \{x_0, \dots, x_k\} \subseteq \mathrm{dom}(\sigma) \\ f, \xi_{out} = \mathrm{malloc}(\xi, \mathrm{function} \cdots) \\ f.\mathrm{bind}(\sigma)}{\sigma, \xi \vdash \mathrm{function}(x_0, \cdots, x_k){s_0, \cdots, s_k \Downarrow_e f \mid \xi_{out}}} \and
\inferrule*[right=Dot]{\sigma, \xi \vdash e \Downarrow_e o \in \mathrm{dom}(\xi') \mid \xi' \\ v = \xi'(o).x}{\sigma, \xi \vdash e.x \Downarrow_e v \mid \xi'} \and
\inferrule*[right=Call]{
  \sigma, \xi \vdash e_f \Downarrow_e f \in \mathrm{dom}(\xi') \mid \xi' \\ 
  \sigma, \xi'_{k-1} \vdash e_k \Downarrow_e v_k \mid \xi'_k \\ 
  v_{out}, \xi_{out} = \xi'(f)(v_0, \dots, v_k)
}
{
  \sigma, \xi \vdash e_f(e_0, \dots, e_n) \Downarrow_e v_{out} \mid \xi_{out}
} \and
\inferrule*[right=Index]{
  \sigma, \xi \vdash e_0 \Downarrow_e v_0 \in \mathrm{dom}(\xi_0) \mid \xi_0 \\
  \sigma, \xi' \vdash e_1 \Downarrow_e v_1 \mid \xi' \\
  v = \xi_0(v_0)
}{
  \sigma, \xi \vdash e_0[e_1] \Downarrow_e v \mid \xi'
} \and
\inferrule*[right=Binary]{
  \sigma, \xi \vdash e_0 \Downarrow_e v_0 \mid \xi_0 \\
  \sigma, \xi_0 \vdash e_1 \Downarrow_e v_1 \mid \xi' \\
  \xi' \vdash v_0 \oplus v_1 = v
}{
  \sigma, \xi \vdash e_0 \oplus e_1 \Downarrow_e v \mid \xi'
}
\end{mathpar}

For statements, we also have a similar reduction $\sigma, \xi \vdash s \Downarrow_s \sigma', \xi'$ which outputs the next
set of contexts for the next instruction.
\begin{mathpar}
\inferrule*[right=Expr]
{\sigma, \xi \vdash e \Downarrow_e v \mid \xi'}
{\sigma, \xi \vdash e \Downarrow_s \sigma, \xi'} \and
\inferrule*[right=VarNil]
{\sigma' = \sigma[x \mapsto \mathrm{nil}]}
{\sigma, \xi \vdash \mathrm{var}~x \Downarrow_s \sigma', \xi} \and
\inferrule*[right=Var]
{
  \sigma, \xi \vdash e \Downarrow_e v \mid \xi' \\
  \sigma' = \sigma[x \mapsto v]
}
{\sigma, \xi \vdash \mathrm{var}~x = e \Downarrow_s \sigma', \xi'} \and
\inferrule*[right=Fun]
{
  \sigma \sqcup [f], \xi \vdash \mathrm{function}(x_0, \dots, x_k) \{s_0, \dots, s_n\} \Downarrow_e v_f \mid \xi' \\
  \sigma' = \sigma[f \mapsto v_f]
}
{\sigma, \xi \vdash \mathrm{function}~f(x_0, \dots, x_k) \{s_0, \dots, s_n\} \Downarrow_s \sigma', \xi' } \and
\inferrule*[right=WhileTrue]
{
  \sigma = \sigma_{-1}, \xi \vdash e_c \Downarrow_e \textrm{true} \mid \xi_{-1} \\
  \sigma_{k-1}, \xi_{k-1} \vdash s_k \Downarrow_s \sigma_k, \xi_k \\
  \sigma_k, \xi_k \vdash \mathrm{while} ~ e_c ~ \{ s_0, \dots, s_n \} \Downarrow_s \sigma', \xi'
}
{\sigma, \xi \vdash \mathrm{while} ~ e_c ~ \{ s_0, \dots, s_n \} \Downarrow_s \sigma', \xi'} \and
\inferrule*[right=WhileFalse]
{
  \sigma, \xi \vdash e_c \Downarrow_e \textrm{false} \mid \xi'
}
{\sigma, \xi \vdash \mathrm{while} ~ e_c ~ \{ s_0, \dots, s_n \} \Downarrow_s \sigma, \xi'} \and
\inferrule*[right=IfTrue]
{
  \sigma = \sigma_{-1}, \xi \vdash e_c \Downarrow_e \textrm{true} \mid \xi_{-1} \\
  \sigma_{k-1}, \xi_{k-1} \vdash s_k \Downarrow_s \sigma_k, \xi_k \\
}
{\sigma, \xi \vdash \mathrm{if} ~ e_c ~ \{s^*\} ~ \mathrm{else} ~ \{s'^*\} \Downarrow_s \sigma_n, \xi_n} \and
\inferrule*[right=IfFalse]
{
  \sigma = \sigma_{-1}, \xi \vdash e_c \Downarrow_e \textrm{false} \mid \xi_{-1} \\
  \sigma_{k-1}, \xi_{k-1} \vdash s'_k \Downarrow_s \sigma_k, \xi_k \\
}
{\sigma, \xi \vdash \mathrm{if} ~ e_c ~ \{s^*\} ~ \mathrm{else} ~ \{s'^*\} \Downarrow_s \sigma_n, \xi_n} \and
\end{mathpar}