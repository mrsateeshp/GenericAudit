package com.thoughtstream.betting.actor

import akka.actor.Actor

/**
 *
 * @author Sateesh
 * @since 16/12/2014
 */
trait SelfQueuableActor extends Actor{
  def queueMyself(message: AnyRef, afterDelayInSeconds : Int): Unit ={
    import scala.concurrent.duration._
    context.system.scheduler.scheduleOnce(afterDelayInSeconds seconds) {
      self ! message
    }(context.system.dispatcher)
  }
}
