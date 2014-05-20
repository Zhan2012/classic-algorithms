object SeqOps {
  implicit class SeqWrapper[T](s: Seq[T]) {
    def pack: Seq[Seq[T]] = {
      if (s.isEmpty) Seq(Seq[T]())
      else {
        val (packed, next) = s span { _ == s.head }
        if (next == Nil) Seq(packed)
        else Seq(packed) ++ next.pack
      }
    }
  }
}

class PlayBoard(size: Int, color: Int) {
  import SeqOps._
  val tiles = Array.fill(size, size)(util.Random.nextInt(color))
  def check = {
    val rowMatches = tiles.map(_.toSeq.pack.filter(_.length > 2))
    val colMatches = tiles.transpose.map(_.toSeq.pack.filter(_.length > 2))
    (rowMatches, colMatches)
  }
  override def toString = {
    tiles.map(row => row.mkString(" ")).mkString("\n")
  }
}

object PlayBoard {
  def apply(size: Int, color: Int) = new PlayBoard(size, color)
  def apply() = new PlayBoard(8, 7)
}

object Bejeweled {
  def main(args: Array[String]) = {
    val size = args(0).toInt
    val color = args(1).toInt
    val pb = PlayBoard(size, color)
    println(pb)
    val (rows, cols) = pb.check
    val rowMatches = rows.map(_.map(_.mkString(", ")).mkString("|")).mkString("\n")
    val colMatches = cols.map(_.map(_.mkString(", ")).mkString("|")).mkString("\n")
    println(rowMatches + "---\n" + colMatches)
  }
}