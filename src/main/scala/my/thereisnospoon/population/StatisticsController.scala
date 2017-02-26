package my.thereisnospoon.population

import akka.actor.{Actor, Props, ReceiveTimeout}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.Config

import scala.concurrent.Future
import scala.concurrent.duration._

class StatisticsController(config: Config) extends Actor {

  import GroupSupervisor._
  import StatisticsController._

  import context.dispatcher

  implicit val timeout = Timeout(1.second)
  private val populationConfig = config.getConfig("population")

  private val elderly =  context.actorOf(GroupSupervisor.props(populationConfig.getInt("elders"),
    populationConfig.getInt("retirement_age"), Int.MaxValue, None),
    "eldrely")

  private val workers = context.actorOf(GroupSupervisor.props(populationConfig.getInt("workers"),
    populationConfig.getInt("adulthood_age"), populationConfig.getInt("retirement_age"), Some(elderly.path.toString)),
    "workers")

  private val children = context.actorOf(GroupSupervisor.props(populationConfig.getInt("children"), 0,
    populationConfig.getInt("adulthood_age"), Some(workers.path.toString)),
    "children")

  context.actorOf(Props(classOf[Reaper],
    populationConfig.getInt("birth_rate"), populationConfig.getInt("death_rate"),
    children.path.toString, elderly.path.toString))

  private val groups = List(children, workers, elderly)

  context.setReceiveTimeout(5.seconds)

  override def receive: Receive = {
    case ReceiveTimeout =>
      Future.sequence(groups.map(_ ? GetMembersCount)).onSuccess({

        case membersCounts: List[MembersCount] =>
          val populationCount = membersCounts.map(_.count).sum
          println(s"Population: $populationCount")
          membersCounts.sortBy(_.group).foreach {membersCount =>
            println(membersCount + s" Ratio: ${membersCount.count * 1d / populationCount}")
          }
          println()
      })
  }
}

object StatisticsController {
  case class MembersCount(count: Int, group: String)
}