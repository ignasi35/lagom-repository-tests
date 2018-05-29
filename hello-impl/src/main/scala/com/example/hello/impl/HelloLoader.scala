package com.example.hello.impl

import akka.persistence.cassandra.session.scaladsl.CassandraSession
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.{ CassandraPersistenceComponents, CassandraReadSide, ReadSideCassandraPersistenceComponents }
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.example.hello.api.HelloService
import com.lightbend.lagom.internal.persistence.cassandra.{ CassandraOffsetStore, CassandraReadSideSettings }
import com.lightbend.lagom.internal.scaladsl.persistence.cassandra.{ CassandraReadSideImpl, ScaladslCassandraOffsetStore }
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.spi.persistence.OffsetStore
import com.softwaremill.macwire._

class HelloLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new HelloApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new HelloApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[HelloService])
}

trait MyReadSideComponents extends ReadSideCassandraPersistenceComponents  {
  override lazy val jsonSerializerRegistry = HelloSerializerRegistry
  lazy val myRepo = wire[MyRepo]
}

abstract class HelloApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents
    with MyReadSideComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[HelloService](wire[HelloServiceImpl])


  // Register the Hello persistent entity
  persistentEntityRegistry.register(wire[HelloEntity])
}
