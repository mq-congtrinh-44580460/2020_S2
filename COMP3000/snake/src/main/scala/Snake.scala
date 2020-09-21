/*
 * This file is part of COMP3000 Assignment 1.
 *
 * Copyright (C) 2020 Kym Haines, Macquarie University.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mq.snake
import scala.collection.mutable.Map

object CubeState {

  /**
    * snake segments
    */
  val snake = List(3, 1, 1, 2, 1, 2, 1, 1, 2, 2, 1, 1, 1, 2, 2, 2, 2)
  val snake02 = List(3,2,2,2,1,1,1,2,2,1,1,2,1,2,1,1,2)

  /**
    * Case class for the directions
    * note: getPossibleDirections must always return directions in the order below
    */
  sealed abstract class Direction
  case object XPos extends Direction
  case object XNeg extends Direction
  case object YPos extends Direction
  case object YNeg extends Direction
  case object ZPos extends Direction
  case object ZNeg extends Direction

  /**
    * Case class for the puzzle/cube state
    * Start state is before anything has been put in the cube; it contains:
    *  - the segments of the snake
    *  - the start position in the cube of the snake
    * OtherState is the puzzle state after the first move; it contains:
    *  - the segments of the snake
    *  - what parts of the cube are occupied
    *  - the end position of the last move to date
    *  - the direction of the last move to date
    *  - the number of moves to date
    */
  sealed abstract class CubeState
  case class StartState(segments: List[Int], start: (Int, Int, Int))
      extends CubeState
  case class OtherState(
      segments: List[Int],
      occupied: Set[(Int, Int, Int)],
      end: (Int, Int, Int),
      dir: Direction,
      numMoves: Int
  ) extends CubeState

  /**
    * Solve the snake cube puzzle with the supplied snake segments
    *
    * @param segments the snake segments
    * @return a list of solutions where each solution looks like:
    *  ((x, y, z), List(direction1, direction2, ... , directionk))
    * where (x, y, z) is the start position in the cube of the snake and directioni are the
    * sequence of directions for each snake segment
    */
  // hint: for, map, match
  def solveSnake(
      segments: List[Int]
  ): List[((Int, Int, Int), List[Direction])] = {

    // FIXME
    val positions = toSet(
      List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1)
    ).toList.filter(x => { (x._2 < 2) && (x._3 == 0) })

    positions
      .map(pos => {
        prependStart(
          pos,
          getSolutions(StartState(segments, pos))
            .getOrElse(List(List()))
            .filter(x => { x.size == segments.size })
        )
      })
      .flatten

  }

  // hint: map
  def prependDir(
      dir: Direction,
      solutions: List[List[Direction]]
  ): List[List[Direction]] = {
    solutions.map(x => x.::(dir))
  }

  // hint: map
  def prependStart(
      xyz: (Int, Int, Int),
      solutions: List[List[Direction]]
  ): List[((Int, Int, Int), List[Direction])] = {
    isValid(xyz) match {
      case true  => solutions.map(x => (xyz, x))
      case false => List()
    }
  }

  // hint: match, filter
  def getSolutions(state: CubeState): Option[List[List[Direction]]] = {
    val sizeSnake = snake.size
    state match {
      case StartState(snake, start) => {
        tryMove(state, XPos) match {
          case Some(x) => {
            getSolutions(x) match {
              case Some(sol) => Some(prependDir(XPos, sol))
              case None      => None
            }
            // Some(prependDir(XPos, getSolutions(x).get))
          }
          case _ => None
        }
      }
      case OtherState(snake, occupied, end, dir, numMoves) => {
        // println(occupied)
        numMoves match {
          case 1 => {
            tryMove(state, YPos) match {
              case Some(x) => {
                getSolutions(x) match {
                  case Some(sol) => {
                    Some(prependDir(YPos, sol))
                  }
                  case _ => None
                }
                // Some(prependDir(YPos, getSolutions(x).get))
              }
              case _ => None
            }
          }
          // case 17 => Some(List(List()))
          case `sizeSnake` => {
            if (occupied.size == 27) {
              Some(List(List()))
            } else None
          }
          case countMove if countMove < `sizeSnake` => {
            // println(occupied.size + " " + countMove)
            val directions = getPossibleDirections(state)

            val validDirections = directions.filter(x => {
              tryMove(state, x).getOrElse(None) != None
            })
            val solutions = validDirections
              .map(direction => {
                getSolutions(tryMove(state, direction).get) match {
                  case None      => List(List())
                  case Some(sol) => prependDir(direction, sol)
                }
              })
              .filter(x => { x != List(List()) })
            if (solutions == List())
              None
            else Some(solutions.flatten)
            // val result = solutions.filter(x => {x != None})
            // if (result.size != 0)
            //   println(result)
            // else None
            // None
          }
          // validDirections match {
          //   case x if x.size == 1 => {
          //     Some(prependDir(x(0), getSolutions(tryMove(state, x(0)).get).getOrElse((List(List())))))
          //   }
          //   case x if x.size == 2 => {
          //     Some(prependDir(x(0), getSolutions(tryMove(state, x(0)).get).getOrElse((List(List()))))
          //     ++ prependDir(x(1), getSolutions(tryMove(state, x(1)).get).getOrElse((List(List()))))
          //     )
          //   }
          //   case x if x.size == 3 => {
          //     Some(prependDir(x(0), getSolutions(tryMove(state, x(0)).get).getOrElse((List(List()))))
          //     ++ prependDir(x(1), getSolutions(tryMove(state, x(1)).get).getOrElse((List(List()))))
          //     ++ prependDir(x(2), getSolutions(tryMove(state, x(2)).get).getOrElse((List(List()))))
          //     )
          //   }
          //   case x if x.size == 4 => {
          //     Some(prependDir(x(0), getSolutions(tryMove(state, x(0)).get).getOrElse((List(List()))))
          //     ++ prependDir(x(1), getSolutions(tryMove(state, x(1)).get).getOrElse((List(List()))))
          //     ++ prependDir(x(2), getSolutions(tryMove(state, x(2)).get).getOrElse((List(List()))))
          //     ++ prependDir(x(3), getSolutions(tryMove(state, x(3)).get).getOrElse((List(List())))))
          //   }
          //   case x if x.size == 0 => {
          //     None
          //   }
          // }
        }
      }
    }
  }

  // hint: match
  def getIncrement(dir: Direction): (Int, Int, Int) =
    dir match {
      case XPos => (1, 0, 0)
      case XNeg => (-1, 0, 0)
      case YPos => (0, 1, 0)
      case YNeg => (0, -1, 0)
      case ZPos => (0, 0, 1)
      case ZNeg => (0, 0, -1)
      case _    => (0, 0, 0)
    }

  // based only on the state's direction and numMoves
  // hint: match
  def getPossibleDirections(state: CubeState): List[Direction] =
    state match {
      case StartState(_, start) => List(XPos)
      case OtherState(_, s, end, dir, numMoves) =>
        if (numMoves == 1) {
          List(YPos)
        } else {
          dir match {
            case XPos | XNeg => List(YPos, YNeg, ZPos, ZNeg)
            case YPos | YNeg => List(XPos, XNeg, ZPos, ZNeg)
            case ZPos | ZNeg => List(XPos, XNeg, YPos, YNeg)
          }
        }
    }

  def getMove(move: Int, dir: Direction): (Int, Int, Int) = {
    val direction = getIncrement(dir)
    // dir match{
    //   case XPos => (move*direction._1 - 1, move*direction._2, move*direction._3)
    //   case XNeg => (move*direction._1 + 1, move*direction._2, move*direction._3)
    //   case YPos => (move*direction._1, move*direction._2 - 1, move*direction._3)
    //   case YNeg => (move*direction._1, move*direction._2 + 1, move*direction._3)
    //   case ZPos => (move*direction._1, move*direction._2, move*direction._3 - 1)
    //   case ZNeg => (move*direction._1, move*direction._2, move*direction._3 + 1)
    // }
    (move * direction._1, move * direction._2, move * direction._3)
  }

  def getOccupied(
      state: CubeState,
      old: (Int, Int, Int),
      newPosition: (Int, Int, Int),
      dir: Direction
  ): Set[(Int, Int, Int)] = {
    val newPos = addList(old, getIncrement(dir))
    isValid(newPosition) match {
      case true => {
        state match {
          case StartState(segments, start) => {
            if (old != newPosition)
              Set(old) ++ getOccupied(state, newPos, newPosition, dir)
            else Set(newPosition)
          }
          case OtherState(segments, occupied, end, direction, numMoves) => {
            if (newPos != newPosition)
              Set(newPos) ++ getOccupied(state, newPos, newPosition, dir)
            else Set(newPos)
          }
        }
      }
      case false => Set()
    }
  }

  def isValid(x: (Int, Int, Int)): Boolean = {
    x match {
      case x if (x._1 < 0) => false
      case x if (x._1 > 2) => false
      case x if (x._2 < 0) => false
      case x if (x._2 > 2) => false
      case x if (x._3 < 0) => false
      case x if (x._3 > 2) => false
      case _               => true
    }
  }

  def addList(x: (Int, Int, Int), y: (Int, Int, Int)): (Int, Int, Int) = {
    // dir match{
    //   case XPos => (x._1 + y._1 - 1, x._2 + y._2, x._3 + y._3)
    //   case XNeg => (x._1 + y._1 + 1, x._2 + y._2, x._3 + y._3)
    //   case YPos => (x._1 + y._1, x._2 + y._2 - 1, x._3 + y._3)
    //   case YNeg => (x._1 + y._1, x._2 + y._2 + 1, x._3 + y._3)
    //   case ZPos => (x._1 + y._1, x._2 + y._2, x._3 + y._3 - 1)
    //   case ZNeg => (x._1 + y._1, x._2 + y._2, x._3 + y._3 + 1)
    // }
    (x._1 + y._1, x._2 + y._2, x._3 + y._3)
  }
  //
  // def getMove(move: Int, dir: Direction): (Int, Int, Int) = {
  //   val increment = getIncrement(dir)
  //   (move*increment._1, move*increment._2, move*increment._3)
  // }

  // hint: match
  def tryMove(state: CubeState, move: Direction): Option[CubeState] = {
    state match {
      case StartState(snake, start) => {
        val newPosition = addList(start, (1 * snake(0) - 1, 0, 0))
        // println("Start")
        // println(start)
        // println("new Position")
        // println(newPosition)
        // println("New")
        // println(getOccupied(state, start, newPosition, XPos))
        if (isValid(newPosition))
          Some(
            OtherState(
              snake,
              getOccupied(state, start, newPosition, XPos),
              newPosition,
              XPos,
              1
            )
          )
        else None
      }
      case OtherState(snake, occupied, end, dir, numMoves) => {
        occupied.size match {
          case 27 => None
          case _ => {
            // println(move)
            val newPosition = addList(end, getMove(snake(numMoves), move))
            // print(newPosition)
            // println(newPosition)
            if (isValid((newPosition))) {
              val newOccupies = getOccupied(state, end, newPosition, move)
              newOccupies.intersect(occupied) match {
                case st if st.size == 0 => {
                  val tmp = newOccupies ++ occupied
                  Some(OtherState(snake, tmp, newPosition, move, numMoves + 1))
                }
                case _ => None
              }

            } else {
              None
            }
          }
        }
      }
      case _ => None
    }
  }

  def show(state: CubeState) {
    state match {
      case StartState(_, start) => println("start " + start)
      case OtherState(_, s, end, dir, numMoves) =>
        def d(x: Int, y: Int, z: Int): String =
          if (s.contains((x, y, z))) "1" else "0"
        println(
          d(0, 2, 0) + d(1, 2, 0) + d(2, 2, 0) + " "
            + d(0, 2, 1) + d(1, 2, 1) + d(2, 2, 1) + " "
            + d(0, 2, 2) + d(1, 2, 2) + d(2, 2, 2) + " " + end
        )
        println(
          d(0, 1, 0) + d(1, 1, 0) + d(2, 1, 0) + " "
            + d(0, 1, 1) + d(1, 1, 1) + d(2, 1, 1) + " "
            + d(0, 1, 2) + d(1, 1, 2) + d(2, 1, 2) + " " + dir
        )
        println(
          d(0, 0, 0) + d(1, 0, 0) + d(2, 0, 0) + " "
            + d(0, 0, 1) + d(1, 0, 1) + d(2, 0, 1) + " "
            + d(0, 0, 2) + d(1, 0, 2) + d(2, 0, 2) + " " + numMoves
        )
    }
  }

  def show(state: Option[CubeState]) {
    state match {
      case Some(st) => show(st)
      case None     => println("None")
    }
  }

  def toSet(a: List[Int]): Set[(Int, Int, Int)] = {
    if (a.size != 27)
      throw new Exception(
        "toSet list must have 27 elements (not " + a.size + ")"
      )
    var b = Set[(Int, Int, Int)]()
    // d(x, y, z, i) adds (x, y, z) to the set if a(i) is 1
    def d(x: Int, y: Int, z: Int, i: Int) { if (a(i) == 1) b = b + ((x, y, z)) }
    d(0, 2, 0, 0)
    d(1, 2, 0, 1)
    d(2, 2, 0, 2)
    d(0, 2, 1, 3)
    d(1, 2, 1, 4)
    d(2, 2, 1, 5)
    d(0, 2, 2, 6)
    d(1, 2, 2, 7)
    d(2, 2, 2, 8)
    d(0, 1, 0, 9)
    d(1, 1, 0, 10)
    d(2, 1, 0, 11)
    d(0, 1, 1, 12)
    d(1, 1, 1, 13)
    d(2, 1, 1, 14)
    d(0, 1, 2, 15)
    d(1, 1, 2, 16)
    d(2, 1, 2, 17)
    d(0, 0, 0, 18)
    d(1, 0, 0, 19)
    d(2, 0, 0, 20)
    d(0, 0, 1, 21)
    d(1, 0, 1, 22)
    d(2, 0, 1, 23)
    d(0, 0, 2, 24)
    d(1, 0, 2, 25)
    d(2, 0, 2, 26)
    b
  }
}
