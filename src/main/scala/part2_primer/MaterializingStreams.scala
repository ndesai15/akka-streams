package part2_primer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * @author ndesai on 2021-05-14
  *
 */
 
 
object MaterializingStreams extends App {

  implicit val system = ActorSystem("MaterializingStreams")
  implicit val materializer = ActorMaterializer() // Allocates resources for running Akka Streams
  import system.dispatcher

  val simpleGraph = Source(1 to 10).to(Sink.foreach(println))

  val source = Source(1 to 10)
  val sink: Sink[Int, Future[Int]] = Sink.reduce[Int]((a, b) => a + b)
  val sumFuture = source.runWith(sink) // source.to(sink).run()

  sumFuture.onComplete{
    case Success(value) => println(s"The sum of all elements is: $value")
    case Failure(exception) => println(s"Exception is $exception")
  }

  // choosing materialized values
  val simpleSource = Source(1 to 10)
  val simpleFlow = Flow[Int].map(x => x + 1)
  val simpleSink = Sink.foreach(println)
  val graph = simpleSource.viaMat(simpleFlow)(Keep.right).toMat(simpleSink)(Keep.right)
  graph.run().onComplete{
    case Success(_) => println("Stream processing completed")
    case Failure(exception) => println(s"Stream processing failed with: $exception")
  }

  // sugars
  Source(1 to 10).runWith(Sink.reduce[Int](_ + _)) // Source.to(Sink.reduce)(Keep.right)
  Source(1 to 10).runReduce(_ + _)

  // backwards
  Sink.foreach[Int](println).runWith(Source.single(42)) // source(42).to(sink..)run()
  // bothways
  Flow[Int].map(x => 2 * x).runWith(simpleSource, simpleSink)

  /**
    * - return the last element out of a source
    * - compute the total word count out of a stream of a sentences
    *   - map, fold, reduce
    *
    */
  val f1 = source.toMat(Sink.last)(Keep.right).run()
  val f2 = source.runWith(Sink.last)

  val sentenceSource = Source(List(
    "Akka is awesome",
    "I love streams",
    "materialize is so cool"
  ))
  val wordCountSink = Sink.fold[Int, String](0)((currentWords, newSentence) => currentWords + newSentence.split(" ").length)
  val g1 = sentenceSource.toMat(wordCountSink)(Keep.right).run()
  val g2 = sentenceSource.runWith(wordCountSink)
  val g3 = sentenceSource.runFold(0)((currentWords, newSentence) => currentWords + newSentence.split(" ").length)

  val wordCountFlow = Flow[String].fold[Int](0)((currentWords, newSentence) => currentWords + newSentence.split(" ").length)
  val g4 = sentenceSource.via(wordCountFlow).toMat(Sink.head)(Keep.right).run()
  val g5 = sentenceSource.viaMat(wordCountFlow)(Keep.left).toMat(Sink.head)(Keep.right).run()
  val g6 = sentenceSource.via(wordCountFlow).runWith(Sink.head)
  val g7 = wordCountFlow.runWith(sentenceSource, Sink.head)._2
}
