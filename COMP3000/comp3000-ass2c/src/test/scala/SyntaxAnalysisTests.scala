/**
 * FunLang syntax analysis tests.
 *
 * Copyright 2020, Anthony Sloane, Kym Haines, Macquarie University, All rights reserved.
 */

package funlang

import org.bitbucket.inkytonik.kiama.util.ParseTests
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.bitbucket.inkytonik.kiama.util.{StringSource, FileSource, Positions}

/**
 * Tests that check that the syntax analyser works correctly.  I.e., it accepts
 * correct input and produces the appropriate trees, and it rejects illegal input.
 */
@RunWith(classOf[JUnitRunner])
class SyntaxAnalysisTests extends ParseTests {

    import FunLangTree._

    val parsers = new SyntaxAnalysis (positions)
    import parsers._

    // Tests of parsing terminals

    test ("parsing an identifier of one letter produces the correct tree") {
        idnuse ("x") should parseTo[FunLangNode] (IdnUse("x"))
    }

    test ("parsing an identifier as an identifier produces the correct tree") {
        idnuse ("count") should parseTo[FunLangNode] (IdnUse("count"))
    }

    test ("parsing an identifier containing digits and underscores produces the correct tree") {
        idnuse ("x1_2_3") should parseTo[FunLangNode] (IdnUse("x1_2_3"))
    }

    test ("parsing an integer as an identifier gives an error") {
        idnuse ("42") should failParseAt[FunLangNode] (1, 1,
                                                      "identifier expected")
    }

    test ("parsing a non-identifier as an identifier gives an error (digit)") {
        idnuse ("4foo") should failParseAt[FunLangNode] (1, 1,
                                                      "identifier expected")
    }

    test ("parsing a non-identifier as an identifier gives an error (underscore)") {
        idnuse ("_f3") should failParseAt[FunLangNode] (1, 1,
                                                      "identifier expected")
    }

    test ("parsing a keyword as an identifier gives an error") {
        idnuse ("def") should failParseAt[FunLangNode] (1, 1,
                                                      "failure of not")
    }

    test ("parsing a keyword prefix as an identifier produces the correct tree") {
        idnuse ("defat") should parseTo[FunLangNode] (IdnUse("defat"))
    }

    test ("parsing an integer of one digit as an integer produces the correct tree") {
        factor ("8") should parseTo[FunLangNode] (IntExp(8))
    }

    test ("parsing an integer as an integer produces the correct tree") {
        integer ("99") should parseTo[String] ("99")
    }

    test ("parsing a non-integer as an integer gives an error") {
        integer ("total") should failParseAt[String] (1, 1,
            "string matching regex '[0-9]+' expected but 't' found")
    }

    // Tests of parsing basic expressions

    test ("parsing an equal expression produces the correct tree") {
        exp ("a == 1") should parseTo[FunLangNode] (EqualExp (IdnUse ("a"), IntExp (1)))
    }

    test ("parsing a less than expression produces the correct tree") {
        exp ("a < 1") should parseTo[FunLangNode] (LessExp (IdnUse ("a"), IntExp (1)))
    }

    test ("parsing an addition expression produces the correct tree") {
        exp ("a + 1") should parseTo[FunLangNode] (PlusExp (IdnUse ("a"), IntExp (1)))
    }

    test ("parsing a subtraction expression produces the correct tree") {
        exp ("a - 1") should parseTo[FunLangNode] (MinusExp (IdnUse ("a"), IntExp (1)))
    }

    test ("parsing a multiplication expression produces the correct tree") {
        exp ("a * 1") should parseTo[FunLangNode] (StarExp (IdnUse ("a"), IntExp (1)))
    }

    test ("parsing a division expression produces the correct tree") {
        exp ("a / 1") should parseTo[FunLangNode] (SlashExp (IdnUse ("a"), IntExp (1)))
    }

    test ("parsing an integer expression produces the correct tree") {
        exp ("823") should parseTo[FunLangNode] (IntExp (823))
    }

    test ("parsing a true expression produces the correct tree") {
        exp ("true") should parseTo[FunLangNode] (BoolExp (true))
    }

    test ("parsing a false expression produces the correct tree") {
        exp ("false") should parseTo[FunLangNode] (BoolExp (false))
    }

    test ("parsing an identifier expression produces the correct tree") {
        exp ("v123") should parseTo[FunLangNode] (IdnUse ("v123"))
    }

    test ("parsing a parenthesized expression produces the correct tree") {
        exp ("(a + 5)") should parseTo[FunLangNode] (PlusExp (IdnUse ("a"), IntExp (5)))
    }

    test ("parsing an application expression produces the correct tree") {
        exp ("a (b)") should parseTo[FunLangNode] (AppExp (IdnUse ("a"), IdnUse ("b")))
    }

    test ("parsing an if expression produces the correct tree") {
        exp ("if (true) then 3 else 4") should parseTo[FunLangNode] (IfExp (BoolExp (true), IntExp (3), IntExp (4)))
    }

    // MY TEST, DON'T TOUCH
    //Testing of parsing terminals 
    test ("parsing a non-identifier as an identifier gives an error (star)") {
        idnuse ("*star") should failParseAt[FunLangNode] (1, 1,
                                                      "identifier expected")
    }

    test ("parsing a non-identifier as an identifier gives an error (hyphen)") {
            idnuse ("-if") should failParseAt[FunLangNode] (1, 1,
                                                        "identifier expected")
        }

    test ("parsing a non-identifier as an identifier gives an error (@)") {
        idnuse ("@") should failParseAt[FunLangNode] (1, 1,
                                                      "identifier expected")
    }

    test ("parsing a negative integer of one digit as an integer produces the correct tree") {
        factor ("-8") should failParseAt[FunLangNode] (1, 1,
                                                      "exp expected")
    }

    test ("parsing a float gives an error") {
        factor ("8.8") should parseTo[FunLangNode] (IntExp(8))
    }

    //Testing of expressions
    test ("parsing a float to exp should give an error but it does not") {
        exp ("8.8") should parseTo[FunLangNode] (IntExp(8))
    }

    test ("parsing a negative to exp should give an error") {
        exp ("-8") should failParseAt[FunLangNode] (1, 1,
                                                      "exp expected")
    }

    test ("parsing a non-identifier as an exp gives an error (@)") {
        exp ("@") should failParseAt[FunLangNode] (1, 1,
                                                      "exp expected")
    }
    //Cond
    test ("parsing an if expression with conditional expression produces the correct tree") {
        exp ("if (3 < 5) then 3 else 4") should parseTo[FunLangNode] (IfExp(LessExp(IntExp(3),IntExp(5)),IntExp(3),IntExp(4)))
    }

    test ("parsing an if expression with integer expression produces the correct tree") {
        exp ("if(4) then 5 else 6") should parseTo[FunLangNode] (IfExp (IntExp (4), IntExp (5), IntExp (6)))
    }

    test ("parsing an if, block and start expression produces the correct tree") {
        exp ("""if (true) then 
{
    val a = 5
    val b = 2
    a * b
  }  else a * b""") should parseTo[FunLangNode] (IfExp (
    BoolExp (true),
    BlockExp (
        Vector (
            Val (IdnDef ("a"), IntExp (5)),
            Val (IdnDef ("b"), IntExp (2))),
        StarExp (IdnUse ("a"), IdnUse ("b"))),
    StarExp (IdnUse ("a"), IdnUse ("b"))))
    }

    test ("parsing an nested if, block and star expression produces the correct tree") {
        exp ("""if ((if (true) then true else false)) then 
{
    val a = 5
    val b = 2
    a * b
  }  else a * b""") should parseTo[FunLangNode] (IfExp (
    IfExp (BoolExp (true), BoolExp (true), BoolExp (false)),
    BlockExp (
        Vector (
            Val (IdnDef ("a"), IntExp (5)),
            Val (IdnDef ("b"), IntExp (2))),
        StarExp (IdnUse ("a"), IdnUse ("b"))),
    StarExp (IdnUse ("a"), IdnUse ("b"))))
    }

    test ("parsing plus expresion with cond and start expression produces the correct tree") {
        exp ("15 + if (true) then 3 else 4") should parseTo[FunLangNode] (IntExp(15))
    }

    val testCond0 = new StringSource("15 + if (true) then 3 else 4")
    test ("parsing plus expresion with cond and start expression produces the correct tree workaround") {
        parsers.parse(parsers.parser,testCond0) should failParseAt[FunLangNode] (1,4, "end of input expected")
    }


    //Conditional
    val testConditional0 = new StringSource("a == c == b")
    test ("parsing nested equal expressions gives error with workaround"){
        parsers.parse(parsers.parser,testConditional0) should failParseAt[FunLangNode] (1, 8, "'-' expected but '=' found")
    }

    test ("parsing nested equal expressions gives partial correct tree"){
        exp("a == b == c") should parseTo[FunLangNode] (EqualExp(IdnUse("a"),IdnUse("b")))
    }

    test ("parsing nested equal, +, * expressions gives correct tree"){
        exp("a == b + 2 * c ") should parseTo[FunLangNode] (EqualExp (
    IdnUse ("a"),
    PlusExp (IdnUse ("b"), StarExp (IntExp (2), IdnUse ("c")))))
    }

    //Calc*
    test ("parsing nested plus and minus expressions produces the correct tree") {
        exp ("1 + 1 - 5 + 3") should parseTo[FunLangNode] (PlusExp(MinusExp (PlusExp (IntExp (1), IntExp (1)), IntExp (5)), IntExp (3)))
    }

    test ("parsing nested plus, minus, start, slash expressions produces the correct tree") {
        exp ("10 - 1 * 5 + 3 / 3") should parseTo[FunLangNode] (PlusExp(MinusExp (IntExp (10), StarExp (IntExp (1), IntExp (5))), SlashExp (IntExp (3), IntExp (3))))
    }

    test ("parsing nested equal, less, plus, minus, start, slash expressions produces the correct tree") {
        exp ("10 < 1 * 5 + 3 / 3") should parseTo[FunLangNode] (LessExp (
    IntExp (10),
    PlusExp (
        StarExp (IntExp (1), IntExp (5)),
        SlashExp (IntExp (3), IntExp (3)))))
    }

    //Block
    test ("parsing block expressions produces the correct tree") {
        exp ("""{
   val x = 99
   val y = x + 1
   y / 2
}""") should parseTo[FunLangNode] (BlockExp (
    Vector (
        Val (IdnDef ("x"), IntExp (99)),
        Val (IdnDef ("y"), PlusExp (IdnUse ("x"), IntExp (1)))),
    SlashExp (IdnUse ("y"), IntExp (2))))
    }
    
    test ("parsing nested block expressions produces the correct tree") {
        exp ("""{
val a = 
  {
    val b = 4
    b
  }
  a
}""") should parseTo[FunLangNode] (BlockExp (
    Vector (
        Val (
            IdnDef ("a"),
            BlockExp (Vector (Val (IdnDef ("b"), IntExp (4))), IdnUse ("b")))),
    IdnUse ("a")))
    }

    test ("parsing nested block expressions gives error") {
        exp ("""{
val a = 2
  {
    val b = 4
    val c = 8
    b + c
  }
a + 2
}""") should failParseAt[FunLangNode] (8, 1, "'}' expected but 'a' found")
    }

    //App
    test ("parsing normal app expression with idnuse f and idnuse g expressions produce correct tree") {
        exp ("f(g)") should parseTo[FunLangNode] (AppExp (IdnUse ("f"), IdnUse ("g")))
    }
    
    test ("parsing normal app expression with idnuse f and integer 0 expressions produce correct tree") {
        exp ("f(0)") should parseTo[FunLangNode] (AppExp (IdnUse ("f"), IntExp (0)))
    }

    test ("parsing normal app expression with integer 2 and integer 0 expressions produce partial correct tree") {
        exp ("2(0)") should parseTo[FunLangNode] (IntExp(2))
    }
    
    val testApp0 = new StringSource("2(0)")
    test ("parsing normal app expression with integer 2 and integer 0 expressions gives error") {
        parsers.parse(parsers.parser, testApp0) should failParseAt[FunLangNode] (1,2, "end of input expected")
    }

    test ("parsing nested app expressions produces the correct tree") {
        exp ("a(b(c(d(e(f)))))") should parseTo[FunLangNode] (AppExp (
    IdnUse ("a"),
    AppExp (
        IdnUse ("b"),
        AppExp (
            IdnUse ("c"),
            AppExp (IdnUse ("d"), AppExp (IdnUse ("e"), IdnUse ("f")))))))
    }

    test ("parsing nested block, app expressions produces the correct tree") {
        exp ("""a(
    {
        val a = 0
        val b = 1
        a + b
    }
)""") should parseTo[FunLangNode] (AppExp (
    IdnUse ("a"),
    BlockExp (
        Vector (
            Val (IdnDef ("a"), IntExp (0)),
            Val (IdnDef ("b"), IntExp (1))),
        PlusExp (IdnUse ("a"), IdnUse ("b")))))
    }

    //fundefn
    test ("parsing a block expression with one val expression, two function definitions and one expression produces the correct tree") {
        exp ("""{
  val x = 100
  def decr (a : Int) = x + a
  def mul (a : Int) = x * a
  inc (x)
}""") should parseTo[FunLangNode] (BlockExp (
    Vector (
        Val (IdnDef ("x"), IntExp (100)),
            FunGroup (
                    Vector (
                        Fun (
                                IdnDef ("decr"),
                                Arg (IdnDef ("a"), IntType ()),
                                PlusExp (IdnUse ("x"),IdnUse ("a"))),
                        Fun (
                                IdnDef ("mul"),
                                Arg (IdnDef ("a"), IntType ()),
                                StarExp (IdnUse ("x"), IdnUse ("a")))))),
    AppExp (IdnUse ("inc"), IdnUse ("x"))))
    }

    test ("parsing a block expression with one val expression, one function definition and one expression produces the correct tree") {
        exp ("""{
  val x = 100
  def 0 (a : Int) = x + a
  0(x)
}""") should failParseAt[FunLangNode] (3,3, "exp expected")}

    //valdefn
    test ("parsing a block expression with 2 val expression, one function definition and one expression gives error") {
        exp ("""{
    def add (a : Bool) = a + 5
    val a = add(4)
    val b = if (true) then add(a) else add(5)
    a * b
}""") should parseTo[FunLangNode] (BlockExp (
    Vector (
            FunGroup (
                    Vector (
                        Fun (
                                IdnDef ("add"),
                            Arg (IdnDef ("a"), BoolType ()),
                            PlusExp (IdnUse ("a"), IntExp (5))))),
        Val (IdnDef ("a"), AppExp (IdnUse ("add"), IntExp (4))),
        Val (
                IdnDef ("b"),
                IfExp (
                    BoolExp (true),
                    AppExp (IdnUse ("add"), IdnUse ("a")),
                    AppExp (IdnUse ("add"), IntExp (5))))),
    StarExp (IdnUse ("a"), IdnUse ("b")))
)}

    test ("parsing a block expression with one invalid val expression, one function definition and one expression gives error") {
        exp ("""{
    def add (a : Bool) = a + 5
    val 2 = add(4)
    2
}
""") should failParseAt[FunLangNode] (3,5, "exp expected")}

    //Tipe
    test ("parsing nested tipe functions produces the correct tree") {
        exp ("""{
  def f (a : Int => Int => Bool) = a + 1
  f (0)
}""") should parseTo[FunLangNode] (BlockExp (
    Vector (
        FunGroup (
            Vector (
                Fun (
                    IdnDef ("f"),
                    Arg (
                        IdnDef ("a"),
                        FunType (
                            IntType (),
                            FunType (IntType (), BoolType ()))),
                    PlusExp (IdnUse ("a"), IntExp (1)))))),
    AppExp (IdnUse ("f"), IntExp (0))))
    }

    test ("parsing nested tipe functions with bracket produces the correct tree") {
        exp ("""{
  def f (a : (Int) => Bool) = a + 1
  f (0)
}
""") should parseTo[FunLangNode] (BlockExp (
    Vector (
            FunGroup (
                    Vector (
                        Fun (
                                IdnDef ("f"),
                                Arg (IdnDef ("a"), FunType (IntType (), BoolType ())),
                                PlusExp (IdnUse ("a"), IntExp (1)))))),
    AppExp (IdnUse ("f"), IntExp (0))))
    }
}
