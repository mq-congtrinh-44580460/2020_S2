{
    def add (a : Bool) = a + 5
    val a = add(4)
    val b = if (true) then add(a) else add(5)
    a * b
}