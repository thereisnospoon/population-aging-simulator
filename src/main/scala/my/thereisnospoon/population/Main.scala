package my.thereisnospoon.population

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object Main extends App {

  val config = ConfigFactory.load()
  val actorSystem = ActorSystem.create()
  actorSystem.actorOf(Props(classOf[StatisticsController], config))
}
