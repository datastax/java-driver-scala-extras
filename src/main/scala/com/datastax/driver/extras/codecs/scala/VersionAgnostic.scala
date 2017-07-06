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

import java.nio.ByteBuffer

import com.datastax.driver.core.{ProtocolVersion, TypeCodec}

import scala.reflect.ClassTag

trait VersionAgnostic[T] {

  this: TypeCodec[T] =>

  def serialize(value: T)(implicit protocolVersion: ProtocolVersion, marker: ClassTag[T]): ByteBuffer =
    this.serialize(value, protocolVersion)

  def deserialize(bytes: ByteBuffer)(implicit protocolVersion: ProtocolVersion, marker: ClassTag[T]): T =
    this.deserialize(bytes, protocolVersion)

}
