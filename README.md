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
we will allow constructs like <img src="https://rawgit.com/leegao/Yuck/svgs/svgs/d66eba85a07754fffc77d5d7ef776683.svg?invert_in_darkmode" align=middle width=13.88376pt height=23.41515pt/>, <img src="https://rawgit.com/leegao/Yuck/svgs/svgs/03a892e3e68adf14d113697f25e49790.svg?invert_in_darkmode" align=middle width=17.240025pt height=26.95407pt/>, and <img src="https://rawgit.com/leegao/Yuck/svgs/svgs/4ca8e8b195527553d70a633a638e623a.svg?invert_in_darkmode" align=middle width=14.911215pt height=23.60787pt/> (which denotes a production 0 or more times, 
a production 1 or more times, and a production 0 or 1 time).

#### LL(1) Grammar

<p align="center"><img src="https://rawgit.com/leegao/Yuck/svgs/svgs/db695ad5bc3c64c2bcb87e5697228a77.svg?invert_in_darkmode" align=middle width=490.42785pt height=982.872pt/></p>

Here, the grammar we've specified is mostly free of 1-lookahead conflicts, so it's amenable to a LL1 grammar with
explicit conflict resolution. In particular, you will need to resolve conflicts for 

* <img src="https://rawgit.com/leegao/Yuck/svgs/svgs/40281274305ef80557e3f30d8799ea5b.svg?invert_in_darkmode" align=middle width=68.154405pt height=31.12527pt/> at `[`, since it doesn't know whether you want `[]` or `[...]`.  You can resolve this by looking at the
  next character and shifting to `[]` if it's a `]`, and `[expr, (, expr)*]` otherwise.
* <img src="https://rawgit.com/leegao/Yuck/svgs/svgs/40281274305ef80557e3f30d8799ea5b.svg?invert_in_darkmode" align=middle width=68.154405pt height=31.12527pt/> at `{`, which is the same problem as above for `{}` versus `{...}`.
* <img src="https://rawgit.com/leegao/Yuck/svgs/svgs/61c502c1036b052d8c0e140e2c44c797.svg?invert_in_darkmode" align=middle width=106.06926pt height=31.12527pt/> for the token `function`. Here, we're not sure if we want to shift to an expression-statement
  `function(){ ... };` or a function declaration `function id() {...}`. While it's perfectly fine to just ignore the
  first form (since it's effectively a NOP), we can resolve this easily by just looking at the next character, and shifting
  to the expression-statement production iff it's an open parenthesis `(`.
* Within <img src="https://rawgit.com/leegao/Yuck/svgs/svgs/ecb5a286f428e11b9d191994b3381270.svg?invert_in_darkmode" align=middle width=276.080475pt height=37.55235pt/>. For the token
  `{`, it's not entirely clearly whether we should shift to the expression-statement for a table or continue the `else {...}` clause.
  Here, we'll just always shift to the else clause.
  
#### Natural Grammar

For the sake of analysis, it's often easier to give a grammar specification that, while ambiguous, captures just
the structure of our language. Here, we will give the specification of our language as an inductive class over the
set of <img src="https://rawgit.com/leegao/Yuck/svgs/svgs/87f0455f890dfea46fc3cf15ec227baa.svg?invert_in_darkmode" align=middle width=39.912345pt height=25.05855pt/>, expressions, and <img src="https://rawgit.com/leegao/Yuck/svgs/svgs/6d1f75a1c2c1cc462878d9e62e281d4e.svg?invert_in_darkmode" align=middle width=39.96366pt height=25.05855pt/>, statements.

The expressions are given by
<p align="center"><img src="https://rawgit.com/leegao/Yuck/svgs/svgs/03a97234d0278e14e923232a8af47f7f.svg?invert_in_darkmode" align=middle width=334.1481pt height=96.054915pt/></p>
where <img src="https://rawgit.com/leegao/Yuck/svgs/svgs/45848451c711deba755da6422f9e68c6.svg?invert_in_darkmode" align=middle width=12.86109pt height=18.90339pt/> denotes binary arithmetic operators, <img src="https://rawgit.com/leegao/Yuck/svgs/svgs/2cfa3b62e25c55e1f2b62ff472d5fd09.svg?invert_in_darkmode" align=middle width=11.03454pt height=17.99028pt/> denotes logical binary operators, and <img src="https://rawgit.com/leegao/Yuck/svgs/svgs/1397b3a9f0e28824b4fd341b38a1760f.svg?invert_in_darkmode" align=middle width=12.86109pt height=20.63391pt/> denotes
binary comparison operators.

Similarly, the statements are given by
<p align="center"><img src="https://rawgit.com/leegao/Yuck/svgs/svgs/4afc5d41c490d9202b6786cd68d49830.svg?invert_in_darkmode" align=middle width=373.46925pt height=93.86322pt/></p>

While this grammar may not be easily implementable using your everyday flavor of parser generators, it does have
the advantage that it is compact and it gives you an inductive construction. We can take the structure defined here
and use it to construct an operational semantic for this language to reveal the types of information that we will
have to carry around in order to fully execute this program.

### Semantics

#### Simple Operational Semantics (Big Step)

We will give the operational semantics in terms of inferences rules. Here, the sequent
<p align="center"><img src="https://rawgit.com/leegao/Yuck/svgs/svgs/97c2c895f7c87675e5a8a0f1c7e9aeec.svg?invert_in_darkmode" align=middle width=104.300295pt height=35.56146pt/></p>
says that if <img src="https://rawgit.com/leegao/Yuck/svgs/svgs/3dde922ef7af0f94b6c5c96835fc9e54.svg?invert_in_darkmode" align=middle width=53.23428pt height=22.19118pt/> all hold, then we can deduce <img src="https://rawgit.com/leegao/Yuck/svgs/svgs/78ec2b7008296ce0561cf83393cb746d.svg?invert_in_darkmode" align=middle width=14.14182pt height=22.19118pt/>. As we will see, it's very natural to specify the semantics
of a language in terms of these inference rules.

Let <img src="https://rawgit.com/leegao/Yuck/svgs/svgs/3269f2678ddb29b67eb84abbae4eeb9a.svg?invert_in_darkmode" align=middle width=111.462285pt height=24.44145pt/> denote the "execution" of a Yuck expression <img src="https://rawgit.com/leegao/Yuck/svgs/svgs/8cd34385ed61aca950a6b06d09fb50ac.svg?invert_in_darkmode" align=middle width=7.7297715pt height=13.88079pt/> in contexts <img src="https://rawgit.com/leegao/Yuck/svgs/svgs/8cda31ed38c6d59d14ebefa440099572.svg?invert_in_darkmode" align=middle width=10.058565pt height=13.88079pt/>
(for local variables) and <img src="https://rawgit.com/leegao/Yuck/svgs/svgs/85e60dfc14844168fd12baa5bfd2517d.svg?invert_in_darkmode" align=middle width=8.0237355pt height=22.55649pt/> (for the heap of objects). Since expressions may, in general, have side-effects, we also have to output
the potentially altered contexts. Their semantics are given by
<p align="center"><img src="https://rawgit.com/leegao/Yuck/svgs/svgs/a1e8eacd2fc5b2f7826e09ed8e4e2934.svg?invert_in_darkmode" align=middle width=677.4636pt height=610.1898pt/></p>

For statements, we also have a similar reduction <img src="https://rawgit.com/leegao/Yuck/svgs/svgs/7e22bf7c897eca471172ea96e9d32e34.svg?invert_in_darkmode" align=middle width=111.372195pt height=23.94843pt/> which outputs the next
set of contexts for the next instruction.
<p align="center"><img src="https://rawgit.com/leegao/Yuck/svgs/svgs/3de7325ba4c12008ea32bfa7ac40d15a.svg?invert_in_darkmode" align=middle width=565.02435pt height=444.0282pt/></p>