package kamon.monix.instrumentation

import kamon.Kamon
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.{Around, Aspect}
import java.lang.{Long => JLong}

import scala.concurrent.duration.TimeUnit

@Aspect
class MonixSchedulerInstrumentation {

  @Around("execution(* monix.execution.Scheduler+.execute(..)) && args(command)")
  def aroundExecute(pjp: ProceedingJoinPoint, command: Runnable): AnyRef = {
    pjp.proceed(Array(new ContextAwareRunnable(command)))
  }

  @Around("execution(* monix.execution.Scheduler+.scheduleOnce(..)) && args(initialDelay, unit, r)")
  def aroundScheduleOnce(pjp: ProceedingJoinPoint, initialDelay: Long, unit: TimeUnit, r: Runnable): AnyRef = {
    pjp.proceed(Array(initialDelay: JLong, unit, new ContextAwareRunnable(r)))
  }

  @Around("execution(* monix.execution.Scheduler+.scheduleWithFixedDelay(..)) && args(initialDelay, delay, unit, r)")
  def aroundScheduleWithFixedDelay(pjp: ProceedingJoinPoint,
                                   initialDelay: Long,
                                   delay: Long,
                                   unit: TimeUnit,
                                   r: Runnable): AnyRef = {
    pjp.proceed(Array(initialDelay: JLong, delay: JLong, unit, new ContextAwareRunnable(r)))
  }

  @Around("execution(* monix.execution.Scheduler+.scheduleAtFixedRate(..)) && args(initialDelay, period, unit, r)")
  def aroundScheduleAtFixedRate(pjp: ProceedingJoinPoint,
                                initialDelay: Long,
                                period: Long,
                                unit: TimeUnit,
                                r: Runnable): AnyRef = {
    pjp.proceed(Array(initialDelay: JLong, period: JLong, unit, new ContextAwareRunnable(r)))
  }
}

class ContextAwareRunnable(underlying: Runnable) extends Runnable {
  private val context = Kamon.currentContext

  override def run(): Unit = Kamon.withContext(context) {
    underlying.run()
  }
}
