/**
 * FunLang syntax analyser.
 *
 * Copyright 2020, Anthony Sloane, Kym Haines, Macquarie University, All rights reserved.
 */

package funlang

import org.bitbucket.inkytonik.kiama.parsing.Parsers
import org.bitbucket.inkytonik.kiama.util.Positions

/**
 * Module containing parsers for FunLang.
 */
class SyntaxAnalysis (positions : Positions) extends Parsers (positions) {

    import FunLangTree._
    import scala.language.postfixOps

    lazy val parser : PackratParser[Exp] =
        phrase (program)

    lazy val program : PackratParser[Exp] =
        exp

    lazy val exp : PackratParser[Exp] = cond
        // block |
        // app |
        // calcMnD | 
        // calcPnM |
        // conditional |
        // cond |
        // factor 

    // lazy val block : PackratParser[Exp] =
    //     "{" ~> rep(definitions) ~ (block <~ "}") ^^ {case _1 ~ _2 => BlockExp(_1, _2)} |
    //     app

    // lazy val app : PackratParser[Exp] = 
    //     idnuse ~ ("(" ~> app <~ ")")  ^^ {case _1 ~ _2 => AppExp(_1, _2)} |
    //     cond
        
    lazy val cond : PackratParser[Exp] = 
        ("if" ~> "(" ~> conditional <~ ")") ~ ("then" ~> conditional) ~ ("else" ~> conditional)^^ 
            {case e1 ~ e2 ~ e3 => IfExp(e1, e2, e3)} |
        conditional

    lazy val conditional : PackratParser[Exp] = 
        calcPnM ~ ("==" ~> calcPnM) ^^ {case _1 ~ _2 => EqualExp(_1, _2)} |
        calcPnM ~ ("<" ~> calcPnM) ^^ {case _1 ~ _2 => LessExp(_1, _2)} |
        calcPnM

    lazy val calcPnM : PackratParser[Exp] = 
        calcPnM ~ ("+" ~> calcMnD) ^^ {case _1 ~ _2 => PlusExp(_1, _2)} |
        calcPnM ~ ("-" ~> calcMnD) ^^ {case _1 ~ _2 => MinusExp(_1, _2)} |
        calcMnD

    lazy val calcMnD : PackratParser[Exp] = 
        calcMnD ~ ("*" ~> factor) ^^ {case _1 ~ _2 => StarExp(_1, _2)} |
        calcMnD ~ ("/" ~> factor) ^^ {case _1 ~ _2 => SlashExp(_1, _2)} |
        factor

    lazy val block : PackratParser[Exp] =
        "{" ~> definitions ~ (exp <~ "}") ^^ {case _1 ~ _2 => BlockExp(_1, _2)} 

    lazy val app : PackratParser[Exp] = 
        idnuse ~ ("(" ~> exp <~ ")")  ^^ {case _1 ~ _2 => AppExp(_1, _2)} 

    lazy val factor : PackratParser[Exp] = 
        block |
        app |
        "false" ^^ (_ => BoolExp (false)) |
        "true" ^^ (_ => BoolExp (true)) |
        idnuse |
        integer ^^ (s => IntExp (s.toInt)) |
        "(" ~> exp <~ ")" |
        failure ("exp expected")

    lazy val definitions : PackratParser[Vector[DefnGroup]] = rep1(defngroup)

    lazy val defngroup : PackratParser[DefnGroup] = fundefn |
        valdefn
    // NOTE: the second lines for block, valdefn, fundefn, tipe need to be
    //       completely replaced; they are there just to keep the compiler quiet

    lazy val valdefn : PackratParser[Val] =
        ("val" ~> idndef <~ "=") ~ exp ^^ {case _1 ~ _2 => Val(_1, _2)}

    lazy val fundefn : PackratParser[DefnGroup] =
        rep1(fun) ^^ {case _1 => FunGroup(_1)} 

    lazy val fun : PackratParser[Fun] = 
        ("def" ~> idndef <~ "(") ~ (arg <~ ")") ~ ("=" ~> exp) ^^ 
            {case _1 ~ _2 ~ _3 => Fun(_1, _2, _3)}

    lazy val arg : PackratParser[Arg] = 
        idndef ~ (":" ~> function) ^^ 
            {case _1 ~ _2 => Arg(_1, _2)}
    
    lazy val function : PackratParser[Type] =
        (tipe <~ "=>") ~ function ^^ 
            {case _1 ~ _2 => FunType(_1, _2)} | tipe

    lazy val tipe : PackratParser[Type] =
        "Bool" ^^^ BoolType() |
        "Int" ^^^ IntType() |
        "(" ~> tipe <~ ")" 

    // NOTE: You should not need to change anything below here...

    lazy val integer =
        regex ("[0-9]+".r)

    lazy val idndef =
        identifier ^^ IdnDef

    lazy val idnuse =
        identifier ^^ IdnUse

    val keywordStrings =
        List ("def", "else", "false", "if", "then", "true", "val")

    lazy val keyword =
        keywords ("[^a-zA-Z0-9_]".r, keywordStrings)

    lazy val identifier =
        not (keyword) ~> identifierBase

    lazy val identifierBase =
        regex ("[a-zA-Z][a-zA-Z0-9_]*".r) |
        failure ("identifier expected")

    override val whitespace : PackratParser[Any] =
        rep ( """\s""".r | comment)

    lazy val comment : PackratParser[Any] =
        "/*" ~ rep (not ("*/") ~ (comment | any)) ~ "*/" |
        "//.*\n".r

}
