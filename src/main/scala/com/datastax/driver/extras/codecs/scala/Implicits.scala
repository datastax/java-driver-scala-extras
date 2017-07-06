/*
 * Copyright 2017 DataStax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datastax.driver.extras.codecs.scala

import java.lang.reflect

import com.datastax.driver.core._
import com.google.common.reflect.TypeToken

object Implicits {

  implicit val protocolVersion = ProtocolVersion.NEWEST_SUPPORTED

  implicit val codecRegistry = CodecRegistry.DEFAULT_INSTANCE

  import scala.reflect.runtime.universe._

  implicit class RowOps(val self: Row) {

    def getImplicitly[T](i: Int)(implicit typeTag: TypeTag[T]): T = {
      val javaType: reflect.Type = TypeConversions.toJavaType(typeTag.tpe)
      self.get(i, TypeToken.of(javaType).wrap().asInstanceOf[TypeToken[T]])
    }

    def getImplicitly[T](name: String)(implicit typeTag: TypeTag[T]): T = {
      val javaType: reflect.Type = TypeConversions.toJavaType(typeTag.tpe)
      self.get(name, TypeToken.of(javaType).wrap().asInstanceOf[TypeToken[T]])
    }

  }

  implicit class BoundStatementOps(val self: BoundStatement) {

    def setImplicitly[T](i: Int, value: T)(implicit typeTag: TypeTag[T]): BoundStatement = {
      val javaType: reflect.Type = TypeConversions.toJavaType(typeTag.tpe)
      self.set(i, value, TypeToken.of(javaType).wrap().asInstanceOf[TypeToken[T]])
    }

    def setImplicitly[T](name: String, value: T)(implicit typeTag: TypeTag[T]): BoundStatement = {
      val javaType: reflect.Type = TypeConversions.toJavaType(typeTag.tpe)
      self.set(name, value, TypeToken.of(javaType).wrap().asInstanceOf[TypeToken[T]])
    }

  }

}
