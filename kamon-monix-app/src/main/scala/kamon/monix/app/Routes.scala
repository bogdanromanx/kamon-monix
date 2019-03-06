package kamon.monix.app

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.data.{EitherT, OptionT}
import kamon.akka.http.KamonTraceDirectives.operationName
import kamon.monix.Monix._
import monix.eval.Task
import monix.execution.Scheduler

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

object Routes {

  private implicit val s: Scheduler = Scheduler.global

  def routes: Route = {
    (path("query") & get) {
      operationName("query") {
        onSuccess(task.runToFuture) { r =>
          complete(r)
        }
      }
    }
  }

  private def task: Task[String] = {
    withSpan("task", Map("type" -> "task")) {
      for {
        _ <- Task.pure(1)
        _ <- withSpan("future") { Task.defer(Task.fromFuture(Future { Thread.sleep(50); 1 })) }
        _ <- many("many", 3, par = false).asyncBoundary
        _ <- many("many", 3, par = true)
        _ <- left("leftie").value
        _ <- right("rightie").value.asyncBoundary
        _ <- some("something").value
        _ <- fail("boom").onErrorRecover { case _ => () }
        _ <- some("somethingElse").value
      } yield "done"
    }
  }

  private def many(name: String, count: Int, par: Boolean): Task[Unit] = {
    withSpan(name) {
      if (par) {
        // parallel execution of sub tasks
        val tasks = 1 to count map { idx =>
          one(s"one_$idx")
        }
        Task.gatherUnordered(tasks).map(_ => ())
      } else {
        // sequential execution of sub tasks
        (1 to count).foldLeft(Task.pure(())) {
          case (t, idx) => t.flatMap(_ => one(s"one_$idx"))
        }
      }
    }
  }

  private def left(name: String): EitherT[Task, String, String] =
    withSpanEitherT(name) {
      EitherT(Task.sleep(10 millis).map(_ => Left("left"): Either[String, String]))
    }

  private def right(name: String): EitherT[Task, String, String] =
    withSpanEitherT(name) {
      EitherT(Task.sleep(10 millis).map(_ => Right("right"): Either[String, String]))
    }

  private def some(name: String): OptionT[Task, String] =
    withSpanOptionT(name) {
      OptionT(Task.sleep(10 millis).map(_ => Option("value")))
    }

  private def one(name: String): Task[Unit] = {
    withSpan(name) {
      Task.sleep(50 millis).map(_ => ())
    }
  }

  private def fail(name: String): Task[Unit] = {
    withSpan(name) {
      Task.sleep(10 millis).flatMap(_ => Task.raiseError(new RuntimeException))
    }
  }

}
