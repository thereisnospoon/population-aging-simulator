package my.thereisnospoon.population

import akka.actor.{Actor, PoisonPill, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}

import scala.util.Random

class GroupSupervisor(initMembers: Int,
                      initAge: Int,
                      transitionAge: Int,
                      pathToNextAgeGroupSupervisor: Option[String]) extends Actor {

  import GroupSupervisor._
  import StatisticsController._

  private var membersCount = initMembers

  private def randomAge = initAge + Random.nextInt(transitionAge - initAge)

  private val router = {
    val routees = Vector.fill(initMembers) {
      ActorRefRoutee(context.actorOf(PersonActor.props(randomAge, transitionAge)))
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  override def receive: Receive = {

    case  TransitionAgeReached =>
      sender() ! PoisonPill
      membersCount -= 1
      pathToNextAgeGroupSupervisor.foreach(context.actorSelection(_) ! CreateNewMember)

    case CreateNewMember =>
      val newMember = context.actorOf(PersonActor.props(randomAge, transitionAge))
      router.addRoutee(newMember)
      membersCount += 1

    case KillMember =>
      router.route(PoisonPill, self)
      membersCount -= 1

    case GetMembersCount =>
      sender() ! MembersCount(membersCount, self.path.name)
  }
}

object GroupSupervisor {
  case object CreateNewMember
  case object TransitionAgeReached
  case object GetMembersCount
  case object KillMember

  def props(initMembers: Int,
            initAge: Int,
            transitionAge: Int,
            pathToNextAgeGroupSupervisor: Option[String]) = {

    Props(new GroupSupervisor(initMembers, initAge, transitionAge, pathToNextAgeGroupSupervisor))
  }
}