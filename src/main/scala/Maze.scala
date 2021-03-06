class Maze(height: Int, width: Int) {
  val vMat = Array.fill(height + 1, width + 1)(1)
  val hMat = Array.fill(height + 1, width + 1)(1)

  def render(vchar: String = "|", hchar: String = "-", joint: String = "+") =
    Iterator.from(0).map(_ % 10).take(width).mkString("      ",  " ", " " + "\n") +
    (0 to height).map { r =>
      val vLine = vMat(r).map { case 1 => vchar; case _ => " " }.mkString("%03d".format(r) + "  ", " ", "\n")
      val hLine = hMat(r).map { case 1 => hchar; case _ => " " }.mkString("    ", joint, joint + "\n")
      vLine + hLine
    }.mkString

  def toFile(filename: String) = {
    import java.io._
    val pw = new PrintWriter(new File(filename))
    pw.write(render("█", "█", "█"))
    pw.close
  }

  override def toString = render()

  def solve(from: (Int, Int), to: (Int, Int), visited: List[(Int, Int)]): List[(Int, Int)] =
    if (from == to) visited ++ List(to)
    else {
      val (r, c) = from
      if (r >= 1 && r <= height && c >= 1 && c <= width && !visited.contains(from)) {
        val possibleMoves =
          (if (vMat(r)(c) == 0) Iterable((r, c + 1)) else Nil) ++
            (if (hMat(r)(c) == 0) Iterable((r + 1, c)) else Nil) ++
            (if (hMat(r)(c - 1) == 0) Iterable((r, c - 1)) else Nil) ++
            (if (vMat(r - 1)(c) == 0) Iterable((r - 1, c)) else Nil)
        possibleMoves.map(solve(_, to, visited ++ List(from))).filter(!_.isEmpty)
          .headOption.getOrElse(List[(Int, Int)]())
      } else List[(Int, Int)]()
    }

  def toGraph = {
    val cellTotal = height * width
    val g = Array.fill(cellTotal, cellTotal)(0)
    for (i <- (0 to height - 1); j <- (0 to width - 1)) {
      val thisCell = i * width + j
      val southCell = (i + 1) % height * width + j
      val eastCell = i * width + ((j + 1) % width)
      if (vMat(i + 1)(j + 1) == 0) { g(thisCell)(eastCell) = 1; g(eastCell)(thisCell) = 1 }
      if (hMat(i + 1)(j + 1) == 0) { g(thisCell)(southCell) = 1; g(southCell)(thisCell) = 1 }
    }
    g
  }
}

class SimpleMaze(height: Int, width: Int) extends Maze(height, width) {
  private def wall = if (util.Random.nextInt(10) > 3) 0 else 1

  for (r <- 0 to height) hMat(r)(0) = 0; for (r <- 1 to height - 1) hMat(r)(width) = wall
  for (c <- 0 to width) vMat(0)(c) = 0; for (c <- 1 to width - 1) vMat(height)(c) = wall
  for (r <- 1 to height - 1; c <- 1 to width - 1) { vMat(r)(c) = wall; hMat(r)(c) = wall }
}

class ConnectedMaze(height: Int, width: Int) extends Maze(height, width) {
  import scala.util.Random

  (0 to height).foreach { hMat(_)(0) = 0 }
  (0 to width).foreach { vMat(0)(_) = 0 }

  val visited = Array.fill(height + 1, width + 1)(false)
  var travel = List[(Int, Int, Int)]()
  val r0 = height / 2
  val c0 = width / 2
  visited(r0)(c0) = true
  Random.shuffle(0 to 3).foreach { d => travel +:= (r0, c0, d) }

  while(travel.nonEmpty) {
    val (r, c, d) = travel.head
    travel = travel.tail
    d match {
      case 0 if (c < width)  && !visited(r)(c + 1) =>
        vMat(r)(c) = 0
        visited(r)(c + 1) = true
        Random.shuffle(Seq(0, 1, 3)).foreach { d => travel +:= (r, c + 1, d) }
      case 1 if (r < height) && !visited(r + 1)(c) =>
        hMat(r)(c) = 0
        visited(r + 1)(c) = true
        Random.shuffle(Seq(0, 1, 2)).foreach { d => travel +:= (r + 1, c, d) }
      case 2 if (c > 1)      && !visited(r)(c - 1) =>
        vMat(r)(c-1) = 0
        visited(r)(c - 1) = true
        Random.shuffle(Seq(1, 2, 3)).foreach { d => travel +:= (r, c - 1, d) }
      case 3 if (r > 1)      && !visited(r - 1)(c) =>
        hMat(r-1)(c) = 0
        visited(r - 1)(c) = true
        Random.shuffle(Seq(0, 2, 3)).foreach { d => travel +:= (r - 1, c, d) }
      case _ =>
    }
  }
}

object SimpleMazeGenerator {
  def main(args: Array[String]) = {
    val height = args.headOption.getOrElse("8").toInt
    val width = args.lastOption.getOrElse("8").toInt
    val m = new SimpleMaze(height, width)
    println(m)
    val path = m.solve((1, 1), (height, width), List[(Int, Int)]())
    println(path)
    val g = new Graph(m.toGraph)
    val vertexTotal = height * width
    println(g.reachable(0, vertexTotal - 1, Array.fill(vertexTotal)(false)))
  }
}

object ConnectedMazeGenerator {
  def main(args: Array[String]) = {
    val height = args.headOption.getOrElse("8").toInt
    val width = args.lastOption.getOrElse("8").toInt
    val m = new ConnectedMaze(height, width)
    println(m)
    val g = new Graph(m.toGraph)
    val vertexTotal = height * width
    println(g.dijkstra(List((0, List(0))), vertexTotal - 1, Set[Int]()))
  }
}
