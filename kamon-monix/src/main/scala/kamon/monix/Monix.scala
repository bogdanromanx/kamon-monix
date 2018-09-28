package kamon.monix
import cats.data._
import kamon.Kamon
import kamon.trace.Span
import monix.eval.Task

object Monix {

  def withSpan[A](name: String, tags: Map[String, String] = Map.empty)(f: => Task[A]): Task[A] =
    Task
      .delay(buildSpan(name, tags, Kamon.currentSpan()))
      .bracket(_ => f)(span => Task.now(span.finish()))

  def withSpanEitherT[A, E](name: String, tags: Map[String, String] = Map.empty)(
      f: => EitherT[Task, E, A]): EitherT[Task, E, A] =
    EitherT(withSpan(name, tags)(f.value))

  def withSpanOptionT[A](name: String, tags: Map[String, String] = Map.empty)(
      f: => OptionT[Task, A]): OptionT[Task, A] =
    OptionT(withSpan(name, tags)(f.value))

  def withSpanIorT[A, B](name: String, tags: Map[String, String] = Map.empty)(
      f: => IorT[Task, A, B]): IorT[Task, A, B] =
    IorT(withSpan(name, tags)(f.value))

  def withSpanIdT[A](name: String, tags: Map[String, String] = Map.empty)(f: => IdT[Task, A]): IdT[Task, A] =
    IdT(withSpan(name, tags)(f.value))

  def withSpanStateT[S, A](name: String, tags: Map[String, String] = Map.empty)(
      f: => StateT[Task, S, A]): StateT[Task, S, A] =
    StateT.applyF(withSpan(name, tags)(f.runF))

  private def buildSpan(name: String, tags: Map[String, String], parent: Span): Span =
    tags
      .foldLeft(Kamon.buildSpan(name).asChildOf(parent)) {
        case (builder, (key, value)) => builder.withTag(key, value)
      }
      .start()
}
