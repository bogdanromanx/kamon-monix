package kamon.monix.app
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}
import kamon.Kamon

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

object Main {

  def main(args: Array[String]): Unit = {
    Kamon.loadReportersFromConfig()

    implicit val as: ActorSystem      = ActorSystem()
    implicit val ec: ExecutionContext = as.dispatcher
    implicit val mt: Materializer     = ActorMaterializer()

    val log = Logging(as, Main.getClass)

    Http().bindAndHandle(Routes.routes, "0.0.0.0", 8080) onComplete {
      case Success(binding) =>
        log.info("Bound to {}: {}", binding.localAddress.getHostString, binding.localAddress.getPort)
      case Failure(th) =>
        log.error(th, "Failed to perform the http binding.")
        Await.result(as.terminate(), 10 seconds)
    }

    as.registerOnTermination {
      Await.result(Kamon.stopAllReporters(), 5 seconds)
    }

    val _ = sys.addShutdownHook {
      Await.result(as.terminate().map(_ => ()), 10 seconds)
    }
  }

}
