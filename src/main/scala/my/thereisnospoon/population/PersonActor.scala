package my.thereisnospoon.population

import akka.actor.{Actor, Props, ReceiveTimeout}

import scala.concurrent.duration._

class PersonActor(startingAge: Int, transitionAge: Int) extends Actor {

  import GroupSupervisor._

  context.setReceiveTimeout(1.second)

  private var age = startingAge

  override def receive: Receive = {
    case ReceiveTimeout =>
      age += 1
      if (age == transitionAge) {
        context.parent ! TransitionAgeReached
      }
  }
}

object PersonActor {

  def props(startingAge: => Int, transitionAge: Int) = {
    Props(new PersonActor(startingAge, transitionAge))
  }
}
