package com.thoughtstream.betting.actor

import akka.actor.{ActorLogging, Actor}

/**
 *
 * @author Sateesh
 * @since 12/12/2014
 */
trait InformativeActor extends Actor with ActorLogging{
  def name:String = self.path.name

  override def preStart(): Unit = {
    super.preStart()
    log info "Starting Actor [%s]".format(name)
  }

  override def postStop(): Unit = {
    super.postStop()
    log info "Stopped Actor [%s]".format(name)
  }
}