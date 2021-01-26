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

package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.{ MappingCodec, TypeCodecs }
import com.datastax.oss.driver.api.core.`type`.reflect.GenericType

object BigIntCodec
    extends MappingCodec[java.math.BigInteger, BigInt](
      TypeCodecs.VARINT,
      GenericType.of(classOf[BigInt])
    ) {

  override def innerToOuter(value: java.math.BigInteger): BigInt =
    if (value == null) null else BigInt(value)

  override def outerToInner(value: BigInt): java.math.BigInteger =
    if (value == null) null else value.bigInteger
}
