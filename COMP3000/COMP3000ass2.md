
    
# Macquarie University<br>Department of Computing

    
## COMP3000 Programming Languages 2020

    
## Assignment Two

    
### Syntax Analysis

    
Due: see submission page  
&#10;      Worth: 15% of unit assessment


&#10;Marks breakdown:


- Code: 50% (of which tests are worth 10%)
- Report: 50% (of which test description is worth 10%)



Submit a notice of disruption via [ask.mq.edu.au](https://ask.mq.edu.au/) if you are unable to submit on time
for medical or other legitimate reasons.


Late penalty without proper justification: 20% of the full marks for the
assessment per day or part thereof late.

#### Overview

 
This assignment asks you to develop a lexical analyser, parser  and tree
builder for a simple functional programming language  called FunLang. We will
build on these components in the third assignment to complete a full
implementation of this language.

 
Building this implementation will give you insight into the  way that
programming language implementations work in general,  as well as specific
experience with how functional language  programs are written, how they are
compiled, and how they are  executed.

 
This kind of task often arises in programming situations other  than
language implementation. For example, many applications have  configuration
files that are written in simple languages. The  application must be able to
read these files reliably and  understand their structure, just as a compiler
must read program  files and understand them.

 
#### FunLang

FunLang is a language that contains elements from mainstream functional
languages such as Haskell, ML and Scala; it uses a Scala-like syntax.
The necessary information about FunLang will be provided in this document.


The description here is a brief overview of the FunLang language.
Aspects such as checking the validity of names or types and translating the
program into an executable form are beyond the scope of syntax analysis and
hence are not considered in this assignment.


The basic unit of a FunLang program is the *expression*; there are
no statements.
In fact, a program is just a single expression.
For example, here is a simple program that returns the result of a simple
arithmetic operation:

<pre>2 + 3 * 4
</pre>

When this program is run it will print the value of the expression:
the number 14.


*Block expressions* are used to build programs out of smaller
expressions.
A block expression is a pair of curly braces containing one or more
definitions, followed by a single expression.
The idea is that the definitions can give names to values and functions.
The value of a block expression is given by its final expression, which can
use the defined names.
For example, here is a program consisting of a block expression that uses
two values:

<pre>{
   val a = 5
   val b = a + 1
   a * b
}
</pre>

This program will print the result of multiplying `a` by
`b`, so 30 will be printed.
(The name `a` can be used in the definition of `b`
since `b` is defined later, but that is a name analysis issue,
so we don't need to worry about it here.)
There are no assignment statements, so the value bound to a particular
occurrence of a name cannot be changed.


Definitions can also define *functions*.
For example, here is a program that defines a value, a function and calls
the function passing the value as a parameter:

<pre>{
  val x = 100
  def inc (a : Int) = a + 1
  inc (x)
}
</pre>

This program will print 101.


All of these programs have just one expression at the top level.
In fact, that's the definition of a program in FunLang: a single expression.
Expression forms are interchangeable as long as they have the correct type.
E.g., anywhere we can put a number can also take a block or some other kind
of expression that evaluates to a number.
For example, here is an artificial program that uses blocks nested inside
an arithmetic operation.

<pre>{
  val a = 3
  a + 1
} *
{
  val b = 10
  b - 1
}
</pre>

This program will print 36 since it is multiplying 4 times 9.


We've seen a few different forms of expression: numbers, addition
expressions, multiplication expressions and function call expressions.
There are also other arithmetic operations, Boolean values, Boolean
literals, relational and logical operators, and conditional expressions.
The complete syntax of FunLang is given below.


Finally, FunLang comments are as in Java: either beginning with two
slashes and continuing to the end of the line, or surrounded by
`/*` and `*/`.

<pre>val z = 3 // This is a comment
z + 7  /* So is this */
</pre>

#### The syntax of FunLang

To guide your implementation, here is a context-free grammar for the
FunLang language on which you can base your parser.


First, the syntax of programs and expressions:

<pre>program : exp.

exp : app
    | block
    | cond
    | exp "==" exp
    | exp "&lt;" exp
    | exp "+" exp
    | exp "-" exp
    | exp "*" exp
    | exp "/" exp
    | "false"
    | "true"
    | idnuse
    | integer
    | "(" exp ")".

app : idnuse "(" exp ")".

block : "{" definitions exp "}".

cond : "if" "(" exp ")" "then" exp "else" exp.
</pre>

Now the syntax of definitions that can occur in blocks:

<pre>definitions : defngroup+.

defngroup : fundefn+
          | valdefn.

fundefn : "def" idndef "(" arg ")" "=" exp.

arg : idndef ":" tipe.

valdefn : "val" idndef "=" exp.
</pre>

Functions that are defined adjacent to each other are grouped together
and they can call each other. The node class `FunGroup` is used
for this purpose and each function is represented by a `Fun`
node. Values are not grouped (or equivalently, each value group consists
of a single value) and are represented by `Val` nodes.


Finally, the syntax of types:

<pre>tipe : "Bool"
     | "Int"
     | tipe "=&gt;" tipe
     | "(" tipe ")".
</pre>

We use the word "tipe" instead of "type" since the latter is a Scala
keyword which prevents us from using it as the name of a parser in our
code.
A function type is specified using the arrow `=&gt;` and describes
the type of a function that takes a value of the type on the left of the
arrow and returns a value of the type on the right of the arrow.
(Type analysis issues are also out of scope for this assignment.)


The grammar above is not immediately suitable for encoding as a parser.
The `exp` and `tipe` non-terminals are ambiguous
since they make no allowance for precedence and associativity of the
operators.
You should rewrite the grammar productions to implement the following
precedence and associativity rules:


    
- The following expression constructs have precedence as shown from
        lowest to highest with constructs on the same line having the same
        precedence:
            
                        1. conditional expressions (if ... then ... else ...)
                        1. equal and less than
                        1. addition and subtraction
                        1. multiplication and division
                        1. all other kinds of expression
                
    
            

    
- All binary expression operators are left associative, except for the
        relational operators (equality and less than) which are not associative.

    
- The function type (`=&gt;`) is right associative.



The parser skeleton you are given already handles the lexical issues
such as parsing integers, identifiers (both defining occurrences
`idndef` and applied occurrences `idnuse`) and comments.

#### What you have to do

You have to write, document and test a Scala syntax analyser including
tree builder for FunLang.


You are strongly advised not to try to solve the whole assignment in one
go.
It is best to write code to handle the parsing and tree construction for
some simple constructs first and then build up to the full language.


Your code must use the Scala parsing library as discussed in lectures
and practicals.
You should use the expression language syntax analyser and tree builder
from the mixed classes as a guide for your implementation.


The associated assignment code bundle contains a skeleton sbt project for
the assignment. The modules are very similar to those used in the practical exercises for
Week 5 onwards.
The skeleton contains the modules you will need.
Some of the parsing and tree construction is given to you as an
illustration; you must provide the rest (look for FIXME in the code).


As well as lexing and parsing the input, your program should construct a
suitable source program tree to represent the parsed result.
See `FunLangTree.scala` in the skeleton for the full definition
and description of the tree structures that you must use.
Do not modify the tree classes, just create instances in your parser code.


As an example of the desired tree structure, here is the tree that
should be produced from the first program above:

<pre>PlusExp (IntExp (2), StarExp (IntExp (3), IntExp (4)))
</pre>

Notice that the higher precedence of multiplication over addition has
been taken into account in this tree.


As a more complex example, here is the tree for the `inc` function
program above:

<pre>BlockExp (
    Vector (
        Val (IdnDef ("x"), IntExp (100)),
        FunGroup (
            Vector (
                Fun (
                    IdnDef ("inc"),
                    Arg (IdnDef ("a"), IntType ()),
                    PlusExp (IdnUse ("a"), IntExp (1)))))),
    AppExp (IdnUse ("inc"), IdnUse ("x")))
</pre>

The tree contains a single block with two definitions: one for "x" and one
for "inc". The function definition contains three children: one for the name,
one for the argument name and type, and one for the body expression. Finally,
the block has the function call as its value expression.

#### Running the syntax analyser and testing it

The skeleton for this assignment is designed to be run from within sbt.
For example, to compile your project and run it on the file
`test/simple.fun` you use the command

<pre>  run test/simple.fun
</pre>

Assuming your code compiles and runs, the run will print the tree that
has been constructed (for correct input), or will print a syntax error
message (for incorrect input).


The project is also set up to do automatic testing. See the file
`SyntaxAnalysisTests.scala` which provides the necessary
definitions to test the syntax analyser on some sample inputs. Note
that the tests we provide are *not* sufficient to test all of
your code. You must augment them with other tests.


You can run the tests using the `test` command in sbt. This
command will build the project and then run each test in turn,
comparing the output produced by your program with the expected
output. Any deviations will be reported as test failures.



#### What you must hand in and how



1.  
    
    A zip file containing only:
         
                
    -  
        
        SyntaxAnalysis.scala 
        
        
                
    -  
        
        SyntaxAnalysisTests.scala 
        
        
                
    -  
        
        your report as a PDF 
        
        
             
    
    
        
1. Do not add any new files or include multiple versions
        of your files. Do not include any libraries or generated files. We will
        compile your files using sbt and the original directory structure,
        so you should avoid any other build mechanisms.

    
1. Your submission should include all of the tests that you have
        used to make sure that your program is working correctly. Note
        that just testing one or two simple cases is not enough for many
        marks. You should test as comprehensively as you can.

     
1.  
    
    Your report should describe how you have
         achieved the goals of the assignment. Do not neglect the report
         since it is worth 50% of the marks for the assignment.
    
         
    Your report should contain the following sections:
    
          
        
              
    - A title page or heading that gives the assignment details,
              your name and student number.
        
        
              
    - A brief introduction that summarises the aim of the
             assignment and the structure of the rest of the report.
        
        
              
    - A description of the design and implementation work that you
              have done to achieve the goals of the assignment. Listing some
              code fragments may be useful to illustrate your description, but
              don't give a long listing. Leaving out obvious stuff is OK,
              as long as what you have done is clear. A good rule of thumb is
              to include enough detail to allow a fellow student to understand it if
              they are at the stage you were at when you started work on the
              assignment.
        
        
             
    - A description of the testing that you carried out. You should
             demonstrate that you have used a properly representative set of
             test cases to be confident that you have covered all the bases.
             Include details of the tests that you used and the rationale
             behind why they were chosen. Do not just print the tests out
             without explanation.
        
        
            



Submit your code and report electronically as a single zip file called
`ass2.zip` using the appropriate submission link on the COMP3000
iLearn website by the due date and time. Your report should be in PDF
format.


DO NOT SUBMIT YOUR ASSIGNMENT OR DOCUMENTATION IN ANY OTHER FORMAT THAN ZIP
and PDF, RESPECTIVELY. Use of any other format slows down the marking and may
result in a mark deduction.

#### Marking

The assignment will be assessed according to the assessment
standards for the unit learning outcomes.


Marks will be allocated equally to the code and to the report. Your
code will be assessed for correctness and quality with respect to the
assignment description. Marking of the report will assess the clarity
and accuracy of your description and the adequacy of your testing.
20% of the marks for the assignment will be allocated to testing.



