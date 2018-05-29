package com.example.hello.impl

import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

/**
  *
  */
class MyRepo(cassandraSession: CassandraSession) {
  require(cassandraSession !=null)


  def exists = cassandraSession != null

}
