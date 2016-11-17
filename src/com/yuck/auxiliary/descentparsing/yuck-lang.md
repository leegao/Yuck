The Yuck Language
=================

This is probably going to be the first cut.

The Yuck language is an experimental language with a syntax derived from C. 
The language features variables whose types are determined dynamically during runtime. 
The aim of the project is simplicity, so there are only 6 primitive types and 

*	number : int, float
*	list
*   table
*	function
*	boolean

In addition, we'll also have general classes and objects.

There are 10 arithmetic operators, they are:

``` c
+ - * / % & | ^ << >>
```

Furthermore, the traditional comparison operators from the number domain to booleans are:

``` c
< <= > >=
```

while these will work on everything else as well:

``` c
== !=
```

Operators are resolved from their underlying objects, so

``` c
(a + b) == a.add(b)
```

Function declaration follows this syntax:

``` javascript
function(arg1, arg2, arg3){
	...
}
```

We can also specify notations of the form

``` javascript
notation add (x) to (y) = y + x (priority 100)
```

where notations, if they conflict with some built-in feature of the language, have lower priority.

and variable declaration:

``` javascript
var identifier;
var identifier = xxx;
```
Conditional statements are also supported. The syntax of a while loop is:

``` javascript
while expr {
	...
}
```

and the for loop:

``` javascript
// Using a counter
for i in a to b {
	...
}

// Foreach
for el in iterable {
	...
}
```

The if statements:

``` javascript
if expr {
	...
} else if expr {
	...
} else {
	...
}
```