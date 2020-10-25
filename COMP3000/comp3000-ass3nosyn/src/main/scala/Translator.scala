/**
 * FunLang to SEC translator.
 *
 * Copyright 2020, Anthony Sloane, Kym Haines, Macquarie University, All rights reserved.
 */

package funlang

/**
 * Translator from FunLang source programs to SEC target programs.
 */
object Translator {

    import SECTree._
    import FunLangTree._
    import scala.collection.mutable.ListBuffer
    import SymbolTable._

    /**
     * Return a frame that represents the SEC instructions for a FunLang program.
     */
    def translate (program : Program) : Frame = {

        // An instruction buffer for accumulating the program instructions
        val programInstrBuffer = new ListBuffer[Instr] ()

        /**
         * Translate the program by translating its expression.
         */
        val expInstrs = translateExpression (program.exp)
        programInstrBuffer.appendAll (expInstrs)
        programInstrBuffer.append (IPrint ())

        // Gather the program's instructions and return them
        programInstrBuffer.result ()

    }

    /**
     * Translate an expression and return a list of the instructions that
     * form the translation.
     */
    def translateExpression (exp : Exp) : Frame = {

        // An instruction buffer for accumulating the expression instructions
        val expInstrBuffer = new ListBuffer[Instr] ()

        /**
         * Generate an instruction by appending it to the instruction buffer.
         */
        def gen (instr : Instr) {
            expInstrBuffer.append (instr)
        }

        /**
         * Generate a sequence of instructions by appending them all to the
         * instruction buffer.
         */
        def genall (frame : Frame) {
            expInstrBuffer.appendAll (frame)
        }

        /**
         * Generate code to make a closure (argName => body).
         */
        def genMkClosure (argName : String, body : Exp) {
            val bodyInstrs = translateExpression (body)
            gen (IClosure (argName, bodyInstrs :+ IPopEnv ()))
        }

        exp match {

            case IntExp (value) =>
                gen (IInt (value))

            case IdnUse(idn) => 
                gen(IVar(idn))
            
            case BoolExp (value) =>
                gen (IBool(value))

            case PlusExp (l, r) =>
                genall (translateExpression (l))
                genall (translateExpression (r))
                gen (IAdd ())

            case MinusExp(l, r) =>
                genall(translateExpression (l))
                genall(translateExpression (r))
                gen (ISub())

            case SlashExp(l, r) =>
                genall(translateExpression (l))
                genall(translateExpression (r))
                gen (IDiv())

            case StarExp(l, r) =>
                genall(translateExpression (l))
                genall(translateExpression (r))
                gen (IMul())

            case EqualExp(l, r) => 
                genall(translateExpression (l))
                genall(translateExpression (r))
                gen (IEqual())

            case LessExp(l, r) => 
                genall(translateExpression (l))
                genall(translateExpression (r))
                gen (ILess())

            case IfExp(cond, th, el) =>
                genall(translateExpression (cond))
                val thFrame = translateExpression(th)
                val elFrame = translateExpression(el)
                gen(IBranch(thFrame, elFrame))

            case AppExp(fn, arg) => 
                genall(translateExpression (fn))
                genall(translateExpression (arg))
                gen(ICall())

            case BlockExp(defs, exp) => {
                defs match {
                    case x : Vector[DefnGroup] => {
                        x.head match {
                            case Val(idndef, expVal) => {
                                val name = idndef.idn
                                if (x.tail.isEmpty){
                                    genMkClosure(name, exp)
                                    genall(translateExpression(expVal))
                                    gen(ICall())
                                }
                                else{
                                    genMkClosure(name, BlockExp(x.tail, exp))
                                    genall(translateExpression (expVal))
                                    gen(ICall())
                                }
                            }

                            case FunGroup(funs) => {
                                funs match {
                                    case y:Vector[Fun] => {
                                        y.head match {
                                            case Fun(idndefF, lam) => {
                                                val app = lam match{
                                                    case Lam(arg, body) => {
                                                        val argu = arg match {
                                                            case Arg(idndef, tipe) => {
                                                                if (x.tail.isEmpty){
                                                                    genMkClosure(idndefF.idn, exp)
                                                                    genMkClosure(idndef.idn, body)
                                                                    gen(ICall())

                                                                }
                                                                else{
                                                                    genMkClosure(idndefF.idn, BlockExp(x.tail, exp))
                                                                    genMkClosure(idndef.idn, body)
                                                                    gen(ICall())
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            case _ => 
                        }
                    }

                    case _ => {
                    }
                }
            }

            case _ =>
                // FIXME: Add cases for other kinds of expression...

        }



        // Gather the expression's instructions and return them
        expInstrBuffer.result ()

    }

}
