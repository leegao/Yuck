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

Function declaration follows this syntax:

``` javascript
function(arg1, arg2, arg3){
	...
}
```

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