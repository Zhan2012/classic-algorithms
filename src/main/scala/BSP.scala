class BspGraph(m: Array[Array[Int]]) extends Graph(m) {
  import scala.actors.Actor
  import scala.actors.Actor._

  sealed abstract class Messages
  case class UPDATE(value: Int) extends Messages
  case class STOP extends Messages
  case class HALT extends Messages

  class Vertex(index: Int) extends Actor {
    var value = Int.MaxValue

    val edgesIn = m.map(_(index)).zipWithIndex.filter(_._1 != 0)
    val edgesOut = m(index).zipWithIndex.filter(_._1 != 0)

    def act() = {
      loop {
        react {
          case UPDATE(newValue) => if (newValue < value) {
            value = newValue
            if (!edgesIn.isEmpty) for ((length, next) <- edgesIn) {
              println(s"$index->$next:$value+$length")
              vertices(next) ! UPDATE(value + length)
            }
            else monitor ! HALT
          } else monitor ! HALT
          case STOP => exit
        }
      }
    }
  }

  val vertices = Array.tabulate(vertexTotal)(new Vertex(_))

  class Monitor extends Actor {
    def act() = {
      loop {
        react {
          case HALT =>
            println("HALT")
            if (vertices.forall(_.getState == State.Suspended)) {
              vertices.foreach(_ ! STOP)
              println("SHUTDOWN")
              exit
            }
        }
      }
    }
  }

  val monitor = new Monitor()

  def shortestPath(target: Int) = {
    monitor.start
    vertices.foreach(_.start)
    vertices(target) ! UPDATE(0)
  }
}

object BspShortestPath {
  import scala.util.Random

  def main(args: Array[String]) = {
    val vertexTotal = args.head.toInt
    val m = Array.fill(vertexTotal, vertexTotal)(Random.nextInt(2))
    val g = new BspGraph(m)
    println(g)
    g.shortestPath(vertexTotal - 1)
  }
}