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

    lazy val exp : PackratParser[Exp] =
        app |
        conditional |
        calcPnM |
        calcMnD |
        // block | 
        factor
    
    lazy val conditional : PackratParser[Exp] = 
        exp ~ ("==" ~> exp) ^^ {case e1 ~ e2 => EqualExp(e1, e2)} |
        exp ~ ("<" ~> exp) ^^ {case e1 ~ e2 => LessExp(e1, e2)}
        
    lazy val calcPnM : PackratParser[Exp] = 
        exp ~ ("+" ~> exp) ^^ {case e1 ~ e2 => PlusExp(e1, e2)} |
        exp ~ ("-" ~> exp) ^^ {case e1 ~ e2 => MinusExp(e1, e2)}

    lazy val calcMnD : PackratParser[Exp] = 
        exp ~ ("*" ~> exp) ^^ {case e1 ~ e2 => StarExp(e1, e2)} |
        exp ~ ("/" ~> exp) ^^ {case e1 ~ e2 => SlashExp(e1, e2)}

    lazy val app : PackratParser[Exp] = 
        (idnuse ~ ("(" ~> exp <~ ")") ) ^^ {case i ~ e => AppExp(i, e)}


    lazy val factor : PackratParser[Exp] =
        "false" ^^ (_ => BoolExp (false)) |
        "true" ^^ (_ => BoolExp (true)) |
        idnuse |
        integer ^^ (s => IntExp (s.toInt)) |
        "(" ~> exp <~ ")" |
        failure ("exp expected")

    lazy val cond : PackratParser[Exp] = 
        (("if" ~> "(" ~> exp <~ ")") ~ ("then" ~> exp <~ "else") ~ exp) ^^ 
            {case e1 ~ e2 ~ e3 => IfExp(e1, e2, e3)}

    // lazy val definitions : PackratParser[DefnGroup] = defngroup+

    // lazy val defngroup : PackratParser[Fun] = (fundefn+)
    // NOTE: the second lines for block, valdefn, fundefn, tipe need to be
    //       completely replaced; they are there just to keep the compiler quiet
    // lazy val block : PackratParser[Exp] =
    //     ("{" ~> definitions ~ exp <~"}") ^^ 
    //     {case d ~ e => BlockExp(d, e)}  // FIXME

    lazy val valdefn : PackratParser[Val] =
        "val" ^^^ Val(IdnDef("fred"), IntExp(0)) // FIXME

    lazy val fundefn : PackratParser[Fun] =
        "def" ^^^ Fun(IdnDef("mary"), Arg(IdnDef("joe"), IntType()), IntExp(0)) // FIXME

    lazy val tipe : PackratParser[Type] =
        "some type" ^^^ IntType()

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
