package recfun

import scala.annotation.tailrec

object RecFun extends RecFunInterface:

  def main(args: Array[String]): Unit =
    def sum(a: Int, b: Int): Int =
      if a>b then 0 else 

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int =
    if (c == 0 || c == r)
      1
    else
      pascal(c-1,r-1)+pascal(c,r-1)



  /**
   * Exercise 2
   */
  def balance(chars: List[Char]): Boolean =
    @tailrec
    def checker(arr: List[Char], x: Int): Boolean =
      if (x < 0) false
      else if (x == 0 && arr.isEmpty) true
      else if (arr.head == '(')
        checker(arr.tail, x+1)
      else if (arr.head == ')')
        checker(arr.tail, x-1)
      else
        checker(arr.tail,x)

    checker(chars, 0)

  /**
   * Exercise 3
   */
  def countChange(money: Int, coins: List[Int]): Int =
    if (money == 0)
      1
    else if (money > 0 && coins.nonEmpty)
      countChange(money - coins.head, coins) + countChange(money, coins.tail)
    else
      0

