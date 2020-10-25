## Assignment 3 spec: Translation

## Assignment Three description

# Macquarie University Department of Computing

## COMP3000 Programming Languages 2020

## Assignment Three

### FunLang Translation and Execution

Due: see submission page.<br>
Worth: 15%

Marks breakdown:

* Code: 50% (of which tests are worth 10%)
* Report: 50% (of which test description is worth 10%)

Submit a notice of disruption via [ask.mq.edu.au](https://ask.mq.edu.au/) if you are unable to submit on time
                            for medical or other legitimate reasons.

Late penalty without proper justification: 20% of the full marks for the assessment per day
                            or part thereof late.

#### Overview

This assignment asks you to develop a translator for the FunLang programming language. The
                            translator will target a simple abstract machine called the _SEC machine_. An
                            implementation of this machine is provided, so once your translator is working you will be
                            able to run FunLang programs.

You will build on the parsing and tree construction done in Assignment Two and the semantic
                            analysis outlined in week 8. We only translate programs that have no errors in the earlier
                            phases of the compiler.

Building this translator will give you insight into the way that source language features
                            correspond to target language features. It will show you how abstract machines can be used
                            to provide a run-time system for languages. You will also gain specific experience with how
                            FunLang is compiled and executed, which is similar in many respects to the implementation of
                            full general-purpose functional programming languages. Once you have completed this
                            assignment you will have a working implementation of FunLang.

#### The SEC machine

The "SEC" in the name SEC machine stands for Stack, Environment and Code. The interpreter
                            that implements our SEC machine can be found in the file `SECMachine.scala`.

The stack, environment and code components form the state of the SEC machine. The name is
                            somewhat confusing because the environment component of the state is a stack as well. We
                            will refer to the _operand stack_ and the _environment                                stack_ to make it clear which one we are referring to.

The operand stack of the machine is used to keep track of values that are being operated on
                            by instructions. In this respect, the SEC machine is similar to the stack machine which we
                            use in the classes in Weeks 10 and 11. When execution begins the operand stack is empty.

The operand stack can contain _machine values_. There are three types of value:
                            integers, Booleans and closures. The last of these is used to represent functions and will
                            be explained more below.

The environment stack of the machine is used to keep track of bindings of names to values.
                            For example, when a function is called the value passed to the function will be bound to the
                            name of the function argument. When the body of the function refers to the name we will look
                            it up in the current environment to get its value. The environment stack is initialised to
                            contain a single environment that is empty and at any time the topmost environment on the
                            stack defines the bindings that are accessible to the running code. In our implementation an
                            environment is a map from names to machine values.

The code component of the machine state is the sequence of SEC machine instructions that is
                            being executed. When a program begins executing, the code component is initialised to the
                            code sequence that comprises the compiled program. At each step of the execution the machine
                            interpreter will look at the first instruction in the code sequence and decide what to do
                            based on what that instruction is. In each case the instruction will update the machine
                            state in some way. Execution will then continue with the next instruction in the sequence.

#### SEC machine instructions

The SEC machine instructions are defined in the file `SECTree.scala`. There
                            are the following instruction types:

* Push value instructions (`IBool` and `IInt`): Push a
                                    single given Boolean or integer value onto the operand stack.
* Push variable instruction (`IVar`): Push the value of a given variable
                                    onto the operand stack. This variable must be bound in the current environment,
                                    otherwise the machine halts with an _unknown variable_ error.
* Arithmetic instructions:
                                    (`IAdd`, `IDiv`, `IMul` and `ISub`):
                                    Pop two integers off the operand stack, perform the given operation on them and push
                                    the result onto the operand stack. The first value popped will be the right operand
                                    of the operation and the second value popped will be the left operand. The machine
                                    will halt if the top of the operand stack does not contain two integer values. If
                                    the instruction is `IDiv` the machine will halt with an error
                                    if the right operand is zero.
* Equality instruction: (`IEqual`): Pop two values off the operand stack,
                                    compare them for equality and push the result of the comparison as a Boolean onto
                                    the operand stack. The machine will halt if the top of the operand stack does not
                                    contain two values that are either both integers or both Booleans.
* Less than instruction: (`ILess`): Pop two values off the operand stack,
                                    compare them for equality and push the result of the comparison as a Boolean onto
                                    the operand stack. The first value popped will be the right operand of the operation
                                    and the second value popped will be the left operand. The machine will halt if the
                                    top of the operand stack does not contain two integer values.
* Print instruction: (`IPrint`): Pop a value off the operand stack and print
                                    it. The machine will halt if there is no value on the operand stack.
* Branch instruction: (`IBranch`): The instruction contains two code
                                    sequences (also called _frames_ in the code). Pop a Boolean value
                                    from the operand stack. If the popped value is true continue execution with the
                                    first code sequence from the instruction; otherwise continue with the second code
                                    sequence. The machine will halt if the top of the operand stack does not contain a
                                    Boolean value.
* Closure instruction: (`IClosure`): The instruction contains an optional
                                    function name, an argument name, and a sequence of instructions that constitute the
                                    body of a function. Push a closure value onto the operand stack that represents this
                                    function. The closure will contain the argument name, body and the current
                                    environment (top of the environment stack). The environment value is
                                    therefore _captured_ so that it can be used later when the function
                                    is called. If a function name is supplied, the environment will be augmented with a
                                    binding between the function name and the closure, so that recursive function calls
                                    are possible. The machine will halt if the environment stack is empty since in that
                                    case there will be no environment to capture.
* Call instruction: (`ICall`): Pop a value from the operand stack; this
                                    value will be the argument to the call. Then pop a closure value from the operand
                                    stack; this will be the function that is called. Continue execution in the body of
                                    the closure with the closure's argument bound to the argument value. The machine
                                    will halt if the value or the closure are not present on the operand stack.
* Pop environment instruction: (`IPopEnv`): Pop the current environment from
                                    the environment stack and discard it. The machine will halt if the environment stack
                                    is empty.

Note that in normal operation of the SEC machine we would not expect to trigger any of the
                            error conditions. The code that we generate for the machine must be constructed so that none
                            of the error conditions can occur. For example, if we generate
                            an `IAdd` instruction then we must previously generate code to push its
                            two integer operands onto the operand stack.

#### Translating FunLang to the SEC machine

This section describes how the constructs from the FunLang language should be translated to
                            SEC machine instructions.

1. The top-level expression in a program should translate into the code that evaluates
                                    the expression, followed by a print instruction.
2. Boolean expressions, integer expressions and applied occurrences of identifiers
                                    should translate into pushes of the relevant values.
3. Arithmetic operations, equality and less-then comparisons should translate into the
                                    code for their operands, followed by the relevant machine operation.
4. An IF expression should translate into code to evaluate the condition followed by a
                                    branch instruction containing the code for the THEN and ELSE expressions of the IF
                                    expression.
5. An application expression should translate into the translation of the left-hand side
                                    function-valued expression, followed by the translation of the right-hand side
                                    argument expression, followed by a call instruction.
6. _Due to time restrictions, for the purposes of this assignment you do not have to                                        handle programs that contain either recursive functions or references to
                                        functions that occur earlier in a definition list. In other words, you can
                                        assume that all value and function definitions are only used in code later in
                                        the block where the definition occurs. The following translation descriptions
                                        should be interpreted in the context of this condition._
7. A block consisting of a single value definition and a block body is translated as if
                                    it was an application of an anonymous function. In other words, the body is packaged
                                    in a closure `x => body` which takes the value name as an
                                    argument. This closure is called passing the expression that defines the value as
                                    the argument. I.e.,

```
{
    val x = exp
    body
}
```

translates to the equivalent of the translation of this application expression:

```
(x => body) (exp)
```

If there are other definitions after the value definition then they are collected
                                    together with the block body and translated as a new (smaller) block. I.e.,

```
{
    val x = exp
    ... defns ...
    body
}
```

translates as if it were

```
{
    val x = exp
    {
        ... defns ...
        body
    }
}
```
8. A block consisting of a single function definition is translated similarly to a
                                    single value definition. The difference is that instead of passing the defined value
                                    as the argument to the function closure, we pass a closure as the argument. I.e.,

```
{
    def f (x : Int) = exp
    body
}
```

translates to equivalent of the translation of this application expression:

```
(f => body) (x => exp)
```

Blocks with multiple function definitions are handled as for blocks with multiple
                                    value definitions as described above.

_To assist with implementing these translations, the skeleton provides a                                routine `genMkClosure` that when called with a                                name `s` and an expression `e` generates code                                to push the closure `s => e`._

#### Translation Example

As a simple example of translation, consider this FunLang program:

```
3 * (12 / 4)
```

This program would be translated into the followed SEC machine code:

```
IInt(3), IInt(12), IInt(4), IDiv(), IMul(), IPrint()
```

As a more complex translation example, consider the FunLang program found
                            in `test/function.fun` in the skeleton:

```
{
  val x = 100
  def inc (a : Int) = a + 1
  inc (x)
}
```

This program should print 101 when executed.

Here is the SEC machine code that is the translation of this program. The comments in the
                            code are there to assist your understanding; they are not part of the code.

```
IClosure (                 // Closure "x => (inc => inc (x)) (a => a + 1)"
  "x",
  List (
    IClosure (             // Closure "inc => inc (x)"
      "inc",
      List (
        IVar ("inc"),
        IVar ("x"),
        ICall (),          // inc (x)
        IPopEnv ())),
    IClosure (             // Closure "a => a + 1"
      "a",
      List (
        IVar ("a"),
        IInt (1),
        IAdd (),
        IPopEnv ())),
    ICall (),              // (inc => inc (x)) (a => a + 1)
    IPopEnv ())),
IInt(100),                 // Push 100
ICall(),                   // (x => (inc => inc (x)) (a => a + 1)) (100)
IPrint()                   // Print the value of the expression
```

#### What you have to do

You have to write, document and test a Scala translator for the FunLang language, according
                            to the description above.

You are strongly advised to complete portions of the assignment before moving onto the next
                            one, rather than trying to code the whole solution in one go.

In the associated code bundle, the translation module is very similar to those used in the
                            practical exercises, particularly for Weeks 10 and 11. For this assignment you should not
                            have to modify any parts of the implementation except the translator
                            (`Translator.scala`) and the related tests (`ExecTests.scala`).

Some of the translation and associated testing code is given to get you started; you must
                            provide the rest, particularly the implementations of the actual translation of FunLang
                            constructs into SEC machine instructions (look for FIXME in the code for some places where
                            new code has to go).

#### Running the translator and testing it

The skeleton for this assignment is designed to be run from within sbt. For example, to compile
                        your project and run it on the file `test/function.fun` you use the command

```
run test/function.fun
```

Assuming your code compiles and runs, this will print any semantic errors that have been
                            found in the program contained within the file. If no errors are detected, the FunLang
                            program will be passed to the translator to produce SEC machine code. Then the SEC code will
                            passed to the SEC interpreter for execution. You should see the result values of the
                            top-level printed out. For example, once you have written your translator, if you compile
                            and run the function program, you should see the number 101 printed out.

The project is also set up to do automatic testing. See the
                            file `ExecTests.scala` that provide some useful definitions for
                            translation and execution testing, and some tests that we have written for you. Note that
                            the tests we provide are _not_ sufficient to test all of your code. You
                            must augment them with other tests.

You can run the tests using the `test` command in sbt. This command will
                            build the project and then run each test in turn, comparing the output produced by your
                            program with the expected output. Any deviations will be reported as test failures.

Running all of the tests can be time-consuming since it also runs the parsing and semantic
                            analysis tests. To just run the translation and execution tests you can use the
                            command `test-only *ExecTests`.

#### What you must hand in and how

1. A zip file containing only:<br>
<br>
Translator.scala<br>
ExecTests.scala<br>
your report
                                    as a PDF<br>
<br>
Do not add any new files or include multiple versions of your files.
                                    Do not include any libraries or generated files. We will compile your files using
                                    sbt and the original directory structure, so you should avoid any other build
                                    mechanisms.
2. Submit _every source and build file_ that is needed to build your
                                    program from source, including files in the skeleton that you have not changed. Do
                                    not add any new files or include multiple versions of your files. Do not include any
                                    libraries or generated files (run the sbt "clean" command before you zip your
                                    project). We will compile all of the files that you submit using sbt, so you should
                                    avoid any other build mechanisms.
3. Your submission should include all of the tests that you have used to make sure that
                                    your program is working correctly. Note that just testing one or two simple cases is
                                    not enough for many marks. You should test as comprehensively as you can.
4. Your report should describe how you have achieved the goals of the assignment. Do not
                                    neglect the report since it is worth 50% of the marks for the assignment.
  - A title page or heading that gives the assignment details, your name and student                                        number.
  - A brief introduction that summarises the aim of the assignment and the structure                                        of the rest of the report.
  - A description of the design and implementation work that you have done to                                        achieve the goals of the assignment. Listing some code fragments may be useful                                        to illustrate your description, but don't just give a long listing. Leaving out                                        obvious stuff is OK, as long as what you have done is clear. A good rule of                                        thumb is to include enough detail to allow a student to understand it if they                                        are at the stage you were at when you started work on the assignment.
  - A description of the testing that you carried out. You should demonstrate that                                        you have used a properly representative set of test cases to be confident that                                        you have covered all the bases. Include details of the tests that you used and                                        the rationale behind why they were chosen. Do not just print the tests out                                        without explanation.

Submit your code and report electronically as a single zip file
                            called `ass3.zip` using the appropriate submission link on the COMP3000
                            iLearn website by the due date and time. Your report should be in PDF format.

DO NOT SUBMIT YOUR ASSIGNMENT OR DOCUMENTATION IN ANY OTHER FORMAT THAN ZIP and PDF,
                            RESPECTIVELY. Use of any other format or file names slows down the marking and will result
                            in a mark deduction.

#### Marking

The assignment will be assessed according to the assessment standards for the unit learning
                            outcomes.

Marks will be allocated equally to the code and to the report. Your code will be assessed for
                            correctness and quality with respect to the assignment description. Marking of the report
                            will assess the clarity and accuracy of your description and the adequacy of your testing.
                            20% of the marks for the assignment will be allocated to testing.
Last modified: Friday, 9 October 2020, 12:31 PM
