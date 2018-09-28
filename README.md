Kamon Monix
-----------

*Kamon Monix* module provides bytecode instrumentation to gather metrics and perform automatic `Context` propagation on
your behalf.

### Adding the Module

The module currently works with monix `3.0.0-RC1`. To get started add the following to your `build.sbt`.

```scala
resolvers           += Resolver.bintrayRepo("bogdanromanx", "maven")
libraryDependencies += "com.github.bogdanromanx" %% "kamon-monix" % "1.0.0-M1"
```

### Run

The `kamon-monix` module requires you to start your application using the AspectJ Weaver Agent. You can achieve that
quickly with Kamon's [sbt-aspectj-runner] plugin or take a look at the Kamon [documentation] for other options.

Just by adding this module in the project dependencies, the Kamon context is propagated across the different threads
where the Monix Tasks are executed.

You can also define custom spans around Monix tasks to trace various parts of the codebase:

```scala
import kamon.monix.Monix._
import monix.eval.Task
import monix.execution.Scheduler
import scala.concurrent.duration._
import scala.language.postfixOps

// required when attempting to run a task
implicit val s: Scheduler = Scheduler.global

val task = withSpan("my-span", Map("span.kind" -> "application")) {
  Task.gatherUnordered(1 to 5 map { idx => Task.sleep((idx * 10) millis) })
}

task.runAsync
```

The module also provides additional _withSpan_ methods that can wrap cats transformers for Monix's Task:
`withSpanEitherT`, `withSpanOptionT`, `withSpanIorT`, `withSpanIdT` and `withSpanStateT`.

[sbt-aspectj-runner]: https://github.com/kamon-io/sbt-aspectj-runner
[documentation]: http://kamon.io/documentation/1.x/recipes/adding-the-aspectj-weaver/