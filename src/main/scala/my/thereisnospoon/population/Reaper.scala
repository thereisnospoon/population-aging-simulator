package my.thereisnospoon.population

import akka.actor.{Actor, ReceiveTimeout}

import scala.concurrent.duration._

class Reaper(birthRate: Int,
             deathRate: Int,
             pathToChildrenSupervisor: String,
             pathToElderlySupervisor: String) extends Actor {

  import GroupSupervisor._

  context.setReceiveTimeout(1.second)

  override def receive: Receive = {

    case ReceiveTimeout =>

      for (_ <- 1 to birthRate) {
        context.actorSelection(pathToChildrenSupervisor) ! CreateNewMember
      }

      for (_ <- 1 to deathRate) {
        context.actorSelection(pathToElderlySupervisor) ! KillMember
      }
  }
}
