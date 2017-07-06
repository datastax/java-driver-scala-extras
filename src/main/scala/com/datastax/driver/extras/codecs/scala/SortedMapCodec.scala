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

import com.datastax.driver.core._

import scala.collection.immutable.SortedMap

class SortedMapCodec[K, V](keyCodec: TypeCodec[K], valueCodec: TypeCodec[V])(implicit ordering: Ordering[K])
  extends AbstractMapCodec[K, V, SortedMap[K, V]](
    DataType.map(keyCodec.getCqlType, valueCodec.getCqlType),
    TypeTokens.sortedMapOf(keyCodec.getJavaType, valueCodec.getJavaType),
    keyCodec,
    valueCodec) {
}

object SortedMapCodec {

  def apply[K, V](keyCodec: TypeCodec[K], valueCodec: TypeCodec[V])(implicit ordering: Ordering[K]): SortedMapCodec[K, V] =
    new SortedMapCodec[K, V](keyCodec, valueCodec)

  import scala.reflect.runtime.universe._

  def apply[K, V](implicit keyTag: TypeTag[K], valueTag: TypeTag[V], ordering: Ordering[K]): SortedMapCodec[K, V] = {
    val keyCodec = TypeConversions.toCodec[K](keyTag.tpe)
    val valueCodec = TypeConversions.toCodec[V](valueTag.tpe)
    apply(keyCodec, valueCodec)
  }

}
