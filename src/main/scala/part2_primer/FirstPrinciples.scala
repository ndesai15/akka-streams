package part2_primer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Future

/**
  * @author ndesai on 2021-05-14
  *
 */
 
 
object FirstPrinciples extends App {

  implicit val system = ActorSystem("FirstPrinciples")
  implicit val materializer = ActorMaterializer()

  // Source : -> Transform the elements
  val source = Source(1 to 10)
  // Sink : <- Receive the elements
  val sink = Sink.foreach[Int](println)

  val graph = source.to(sink)
  graph.run()

  // flows transform elements
  val flow = Flow[Int].map(x => x + 1)
  val sourceWithFlow = source.via(flow)
  val sinkWithFlow = flow.to(sink)

  // sourceWithFlow.to(sink)
  // source.to(sinkWithFlow)
  // source.via(flow).to(sink)

  // nulls are NOT allowed
//  val illegalSource = Source.single[String](null)
 // illegalSource.to(Sink.foreach(println)).run()
  // use Options instead

  // various kinds of sources
  val finiteSource = Source.single(1)
  val anotherFiniteSource = Source(List(1,2,3))
  val emptySource = Source.empty[Int]
  val infiniteSource = Source(Stream.from(1)) // do not confuse an Akka Stream with a "collection" Stream
  import scala.concurrent.ExecutionContext.Implicits.global
  val futureSource = Source.fromFuture(Future(42))

  // sinks
  val theMostBoringSink = Sink.ignore
  val foreachSink = Sink.foreach[String](println)
  val headSink = Sink.head[Int] // retrieves head and then closes the stream
  val foldSink = Sink.fold[Int, Int](0)(_ + _)

  // flows - usually mapped to collection operators
  val mapFlow = Flow[Int].map(x => 2 * x)
  val takeFlow = Flow[Int].take(5) // Can convert an infinite
  // drop, filter
  // NOT have flatMap

  // source -> flow -> flow -> ... -> sink
  val doubleFlowGraph = source.via(mapFlow).via(takeFlow).to(sink)
  doubleFlowGraph.run()

  // syntactic sugars
  val mapSource = Source(1 to 10).map(x => x * 2) // Source(1 to 10).via(Flow[Int].map(x => x * 2))
  // run streams directly
  mapSource.runForeach(println) // mapSource.to(Sink.foreach[Int](println)).run()

  // operators = components

  /**
    * Exercise: Create a stream that takes the names of persons, then you will keep the first 2 names with length > 5 characters
    */

  val inputList = List("Jay bahuchar Mataji", "Jay Ambe", "abc", "def")
  val filteredSource = Source(inputList).filter(_.length >5).take(2)
  filteredSource.runForeach(println)


}
