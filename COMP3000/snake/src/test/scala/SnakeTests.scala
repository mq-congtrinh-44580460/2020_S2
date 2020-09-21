/*
 * This file is part of COMP3000 Assignment 1.
 *
 * Copyright (C) 2020 Kym Haines, Macquarie University.
 * 15 Aug 20
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Tests of the Snake puzzle solver.
 * Uses the ScalaTest `FlatSpec` style for writing tests. See
 *
 *      http://www.scalatest.org/user_guide
 *
 * For more info on writing ScalaTest tests.
 */

package org.mq.snake

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class SnakeTests extends FlatSpec with Matchers {

  import CubeState._

  val st0 = OtherState(snake, toSet(List(0, 0, 0,  0, 0, 0,  0, 0, 0,
                                         1, 1, 1,  0, 0, 0,  0, 0, 0,
                                         0, 0, 0,  0, 0, 0,  0, 0, 0)),
                       (2, 1, 0), XPos, 1)

  val st1 = OtherState(snake, toSet(List(0, 0, 0,  0, 0, 0,  0, 0, 0,
                                         1, 1, 1,  1, 0, 1,  0, 0, 0,
                                         1, 0, 0,  0, 0, 0,  0, 1, 1)),
                       (0, 0, 0), YNeg, 7)

  val st2 = OtherState(snake, toSet(List(0, 0, 0,  0, 0, 0,  0, 0, 0,
                                         1, 1, 1,  0, 0, 0,  0, 0, 0,
                                         0, 0, 0,  0, 0, 0,  0, 0, 0)),
                       (2, 1, 0), XPos, 1)

  val st3 = OtherState(snake, toSet(List(1, 1, 1,  1, 1, 1,  1, 1, 1,
                                         1, 1, 1,  1, 1, 1,  1, 1, 1,
                                         1, 1, 1,  1, 1, 1,  1, 1, 1)),
                       (1, 2, 2), YPos, 17)

  val st4 = OtherState(snake, toSet(List(1, 1, 1,  1, 1, 1,  1, 1, 1,
                                         1, 1, 1,  1, 1, 1,  1, 1, 0,
                                         1, 1, 1,  1, 1, 1,  1, 1, 0)),
                       (2, 2, 2), ZPos, 16)

  val st5 = OtherState(snake, toSet(List(1, 1, 1,  1, 1, 1,  1, 1, 0,
                                         1, 1, 1,  1, 1, 1,  1, 1, 1,
                                         1, 1, 1,  1, 1, 1,  1, 1, 0)),
                       (2, 2, 1), YPos, 16)
  //My generated states
  val st6 = StartState(snake, (0,0,0))

  val st10 = StartState(snake02, (0,0,0))

  "Direction" should "be prepended to each sublist" in {
    assert(prependDir(XPos, List(List(YPos, ZPos), List(ZNeg))) ==
                         List(List(XPos, YPos, ZPos), List(XPos, ZNeg)))
  }

  "3D increments" should "be created for XPos" in {
    assert(getIncrement(XPos) == (1, 0, 0))
  }

  it should "be created for ZNeg" in {
    assert(getIncrement(ZNeg) == (0, 0, -1))
  }

  "getPossibleDirections" should "produce possible directions from a state" in {
    assert(getPossibleDirections(st1).toSet.equals(Set(XPos, XNeg, ZPos, ZNeg)))
  }

  it should "produce only YPos for second move" in {
    assert(getPossibleDirections(st2).toSet.equals(Set(YPos)))
  }

  "tryMove" should "work with the start state" in {
    assert(!tryMove(StartState(snake, (0, 1, 0)), XPos).isEmpty)
  }

  it should "work with the start state: end" in {
    assert(tryMove(StartState(snake, (0, 1, 0)), XPos).get.asInstanceOf[OtherState].end
              == (2, 1, 0))
  }

  it should "work with the start state: numMoves" in {
    assert(tryMove(StartState(snake, (0, 1, 0)), XPos).get.asInstanceOf[OtherState].numMoves
              == 1)
  }

  it should "work with the start state: occupied" in {
    assert(((a: Set[(Int,Int,Int)]) => (a.contains((0, 1, 0)),
                                        a.contains((1, 1, 0)),
                                        a.contains((2, 1, 0))))
       (tryMove(StartState(snake, (0, 1, 0)), XPos).get.asInstanceOf[OtherState].occupied)
              == (true, true, true))
  }

  it should "work with other state" in {
    assert(!tryMove(st0, ZPos).isEmpty)
  }

  it should "work with other state: end" in {
    assert(tryMove(st0, ZPos).get.asInstanceOf[OtherState].end
              == (2, 1, 1))
  }

  it should "work with other state: numMoves" in {
    assert(tryMove(st0, ZPos).get.asInstanceOf[OtherState].numMoves
              == 2)
  }

  it should "work with other state: occupied" in {
    assert(tryMove(st0, ZPos).get.asInstanceOf[OtherState].occupied.contains((2, 1, 1)))
  }

  "getSolutions" should "produce a list of the empty list when cube is fully occupied" in {
    assert(getSolutions(st3) == Some(List(List())))
  }

  it should "produce the final move" in {
    assert(getSolutions(st4) == Some(List(List(YNeg))))
  }

  it should "fail on the final move" in {
    assert(getSolutions(st5) == None)
  }

  // FIXME Add more tests here.

  //My tests
  "prependDir" should "able to add direction into empty list" in {
    assert(prependDir(XPos, List(List())) == List(List(XPos)))
  }

  "prependStart" should "able to add start position into empty list" in {
    assert(prependStart((0,0,0), List(List())) == List(((0,0,0),List())))
  }

  it should "able to detect invalid start position into empty list" in {
    assert(prependStart((3,0,0), List(List())) == List())
  }

  "getPossibleDirections"should "produce only XPos for first state" in {
    assert(getPossibleDirections(st6).toSet.equals(Set(XPos)))
  }

  "getOccupied" should "work with StartState" in {
    assert(!getOccupied(st6, (0,0,0), (2,0,0), XPos).isEmpty)
  }

  it should "work with OtherState" in {
    assert(!getOccupied(st5, (0,0,0), (2,0,0), XPos).isEmpty)
  }

  it should " not work with invalid new position" in {
    assert(getOccupied(st5, (0,0,0), (-1,0,0), XPos).isEmpty)
  }
  
  "isValid" should "return true with valid position" in {
    assert(isValid(2,2,2) == true)
  }

  it should "return false with invalid position" in {
    assert(isValid(-1,-1,2) == false)
  }

  "addList" should "add positive number correctly" in {
    assert(addList((1,3,4),(3,4,5)) == (4,7,9))
  }
  
  it should "be able to add negative number" in {
    assert(addList((-1,3,-4),(3,4,5)) == (2,7,1))
  }

  it should "be adble to add two negative numbers" in {
    assert(addList((-1,-1,-1),(-2,-2,-2)) == (-3,-3,-3))
  }

  "tryMove" should "always treat StartState differently " in {
    assert(tryMove(st6, YPos) == Some(OtherState(List(3, 1, 1, 2, 1, 2, 1, 1, 2, 2, 1, 1, 1, 2, 2, 2, 2),Set((0,0,0), (1,0,0), (2,0,0)),(2,0,0),XPos,1)))
  }

  it should "return none when all the positions are occupied" in {
    assert(tryMove(st3, XPos) == None)
  }

  "getSolutions" should "fail on the st1" in {
    assert(getSolutions(st1) == None)
  }

  it should "fail on the st2" in {
    assert(getSolutions(st2) == None)
  }

  it should "produce solution on the st6" in {
    assert(getSolutions(st6) == Some(List(List(XPos, YPos, XNeg, ZPos, YPos, ZNeg, XPos, ZPos, YNeg, XNeg, YPos, ZNeg, YPos, ZPos, YNeg, XPos, YPos))))
  }

  it should "be successful on the st10" in {
    assert(getSolutions(st10) == Some(List(List(XPos, YPos, XNeg, ZPos, XPos, ZNeg, XPos, YNeg, XNeg, ZPos, YPos, ZNeg, XPos, ZPos, YNeg, XPos, YPos))))
  }

  it should  "fail on non-solution with snake02" in {
    assert(
      getSolutions(
        StartState(snake02,(0,1,0))
        ) == None)
  }

  "solveSnake" should "produce one solution for given snake" in {
    assert(solveSnake(snake) == List(((0,0,0),List(XPos, YPos, XNeg, ZPos, YPos, ZNeg, XPos, ZPos, YNeg, XNeg, YPos, ZNeg, YPos, ZPos, YNeg, XPos, YPos))))
  }

  it should "also produce one solution on new snake02" in{
    assert(solveSnake(snake02) == List(((0,0,0),List(XPos, YPos, XNeg, ZPos, XPos, ZNeg, XPos, YNeg, XNeg, ZPos, YPos, ZNeg, XPos, ZPos, YNeg, XPos, YPos))))
  }

  // it should "produce two solutions if the symmetrical restriction is turned off" in {
  //   assert(solveSnake(snake) == List(((0,0,0),List(XPos, YPos, XNeg, ZPos, XPos, ZNeg, XPos, YNeg, XNeg, ZPos, YPos, ZNeg, XPos, ZPos, YNeg, XPos, YPos)), ((0,0,2),List(XPos, YPos, XNeg, ZNeg, XPos, ZPos, XPos, YNeg, XNeg, ZNeg, YPos, ZPos, XPos, ZNeg, YNeg, XPos, YPos))))
  // }

}
