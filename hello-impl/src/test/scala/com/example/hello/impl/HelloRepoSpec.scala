package com.example.hello.impl

import akka.{ Done, NotUsed }
import com.example.hello.api.{ GreetingMessageChanged, _ }
import com.lightbend.lagom.scaladsl.api.Service.{ named, pathCall, topic }
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{ KafkaProperties, PartitionKeyStrategy }
import com.lightbend.lagom.scaladsl.api._
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.persistence.{ PersistentEntityRegistry, ReadSide }
import com.lightbend.lagom.scaladsl.server.{ LagomApplication, LagomServer, LocalServiceLocator }
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.softwaremill.macwire.wire
import com.typesafe.config.ConfigFactory
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, Matchers }
import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSComponents

class HelloRepoSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  // Disabled Lagom's ConfigSessionProvided based on ServiceLocators.
  // See https://www.lagomframework.com/documentation/current/scala/ProductionOverview.html#Using-static-Cassandra-contact-points
  val configString =
    """
      |cassandra.default {
      |  contact-points = ["127.0.0.1"]
      |  session-provider = akka.persistence.cassandra.ConfigSessionProvider
      |}
    """.stripMargin

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
  ) { ctx =>
    new  LagomApplication(ctx)
      with MyReadSideComponents
      with AhcWSComponents
      with ProvidesAdditionalConfiguration {
      override def additionalConfiguration: AdditionalConfiguration =
        super.additionalConfiguration ++
          Configuration(ConfigFactory.parseString(configString))

      override lazy val lagomServer = serverFor[EmptyService](wire[EmptyServiceImpl])
      override def persistentEntityRegistry: PersistentEntityRegistry = ???
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }
  }


  val client = server.serviceClient.implement[HelloService]

  override protected def afterAll() = server.stop()

  "MyRepo" should {
    "have a cassandraSession injected" in {
      server.application.myRepo.exists should be(true)
    }
  }
}

trait EmptyService extends Service {
  override final def descriptor = {
    import Service._
    named("empty")
  }
}

class EmptyServiceImpl extends EmptyService{

}