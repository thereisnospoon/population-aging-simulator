# About

This program is a simple simulation of population aging. 

The whole population is divided in three groups: children (people who not yet work), workers and elderly (retired).
During runtime population count in every group is being updated based on aging (1 sec = 1 year for this simulation) 
and supplied birth/death rates.

# Usage

This is *sbt* project based on Scala and Akka. 

You can configure simulation parameters in `src/main/resource/application.conf` files.

In order to start simulation execute: `sbt run`
