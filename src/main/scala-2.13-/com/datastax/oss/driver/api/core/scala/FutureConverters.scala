package com.datastax.oss.driver.api.core.scala

import java.util.concurrent.CompletionStage

import scala.concurrent.Future

object FutureConverters {

  implicit class CompletionStageOps[T](private val cs: CompletionStage[T]) extends AnyVal {
    def asScala: Future[T] = scala.compat.java8.FutureConverters.toScala(cs)
  }

}
