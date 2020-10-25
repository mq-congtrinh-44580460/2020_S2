{
  val x = 100
  def bait (b: Int) = b + 3
  val b = 5
  def inc (a : Int) = a + 1
  inc (x) + bait(b)
}