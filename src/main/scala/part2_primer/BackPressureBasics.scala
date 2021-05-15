package part2_primer

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}

/**
  * @author ndesai on 2021-05-15
  *
 */
 
 
object BackPressureBasics extends App {

  implicit val system = ActorSystem("BackpressureBasics")
  implicit val materializer = ActorMaterializer()

  val fastSource = Source(1 to 1000)
  val slowSink = Sink.foreach[Int]{ x =>
    // simulate a long processing
    Thread.sleep(1000)
    println(s"Sink: $x")
  }

  // fastSource.to(slowSink).run() // fusing?!
  // not backpressure

  //fastSource.async.to(slowSink).run()
  // ^^^^^^ This is BackPressure: Elements flow as respond to demand from Consumers

  val simpleflow = Flow[Int].map { x =>
    println(s"Incoming: $x")
    x + 1
  }

  fastSource.async
    .via(simpleflow).async
    .to(slowSink)
    //.run()


  /*
     reactions to backpressure (in order):
     - try to slow down if possible
     - buffer elements until there's more demand
     - drop down elements from the buffer if it overflows
     - tear down/kill the whole stream (failure)
   */
  val bufferedFlow = simpleflow.buffer(10, overflowStrategy = OverflowStrategy.dropHead)
  fastSource.async // runs on first actor
    .via(bufferedFlow).async // runs on other actor
    .to(slowSink).async  // runs on other actor
    .run()

  /*
     1 - 16: nobody is backpressured
     17 - 16: flow will buffer, flow will start dropping at the next element
     26 - 1000: flow will always drop the oldest element
        => 991 - 1000 => 992 - 1001 => sink
   */

  /*
     overflow strategies:
       - drop head = oldest
       - drop tail = newest
       - drop new = exact element to be added = keeps the buffer
       - drop the entire buffer
       - backpressure signal
       - fail
   */

  // throttling
  import scala.concurrent.duration._
  fastSource.throttle(2, 1 second).runWith(Sink.foreach(println))
}
