package kamon.monix.instrumentation

import kamon.Kamon
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.{Around, Aspect}

import scala.concurrent.duration.TimeUnit

@Aspect
class MonixSchedulerInstrumentation {

  @Around("execution(* monix.execution.Scheduler+.execute(..)) && args(runnable)")
  def aroundExecute(pjp: ProceedingJoinPoint, runnable: Runnable): AnyRef = {
    pjp.proceed(Array(new ContextAwareRunnable(runnable)))
  }

  @Around("execution(* monix.execution.Scheduler+.scheduleOnce(..)) && args(initialDelay, unit, runnable)")
  def aroundScheduleOnce(pjp: ProceedingJoinPoint, initialDelay: Long, unit: TimeUnit, runnable: Runnable): AnyRef = {
    pjp.proceed(Array(initialDelay: java.lang.Long, unit, new ContextAwareRunnable(runnable)))
  }

  @Around("execution(* monix.execution.Scheduler+.scheduleWithFixedDelay(..)) && args(initialDelay, unit, runnable)")
  def aroundScheduleWithFixedDelay(pjp: ProceedingJoinPoint,
                                   initialDelay: Long,
                                   unit: TimeUnit,
                                   runnable: Runnable): AnyRef = {
    pjp.proceed(Array(initialDelay: java.lang.Long, unit, new ContextAwareRunnable(runnable)))
  }

  @Around("execution(* monix.execution.Scheduler+.scheduleAtFixedRate(..)) && args(initialDelay, unit, runnable)")
  def aroundScheduleAtFixedRate(pjp: ProceedingJoinPoint,
                                initialDelay: Long,
                                unit: TimeUnit,
                                runnable: Runnable): AnyRef = {
    pjp.proceed(Array(initialDelay: java.lang.Long, unit, new ContextAwareRunnable(runnable)))
  }
}

class ContextAwareRunnable(underlying: Runnable) extends Runnable {
  private val context = Kamon.currentContext

  override def run(): Unit = Kamon.withContext(context) {
    underlying.run()
  }
}
