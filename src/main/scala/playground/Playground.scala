package playground

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.concurrent.Future

/**
  * @author ndesai on 2021-05-14
  *
 */
 
 
object Playground extends App {

  implicit val system = ActorSystem("JayBahucharMataji")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher

  val source = Source(1 to 10)
  val flow = Flow[Int].map(x => {println(x); x})
  val sink = Sink.fold[Int, Int](0)(_ + _)

  // connect the Source to the Sink, obtaining a Runnable
  val runnable:RunnableGraph[Future[Int]] = source.via(flow).toMat(sink)(Keep.right)

  // materialize the flow and get the value of the FoldSink
  val sum: Future[Int] = runnable.run()
  sum.onComplete(x => println(s"Sum: $x"))

}
