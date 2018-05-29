package com.example.hello.impl

import akka.actor.ActorSystem
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.typesafe.config.{ Config, ConfigFactory }
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, Matchers }

class HelloRepoSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  // Overriding the session-provider is required because Lagom sets the default
  // value to Lagom's `ServiceLocatorServiceProvider` which is not what we want
  // in these tests.
  val configString =
    """
      |cassandra.default {
      |  contact-points = ["127.0.0.1"]
      |  session-provider = akka.persistence.cassandra.ConfigSessionProvider
      |}
    """.stripMargin

  val config: Config = ConfigFactory.load().withFallback(ConfigFactory.parseString(configString))
  val system = ActorSystem("MyRepo-spec", Some(config))
  val casSession :CassandraSession = new CassandraSession(system)
  val myRepo = new MyRepo(casSession)

  "MyRepo" should {
    "have a cassandraSession injected" in {
      myRepo.exists should be(true)
    }
  }
}

