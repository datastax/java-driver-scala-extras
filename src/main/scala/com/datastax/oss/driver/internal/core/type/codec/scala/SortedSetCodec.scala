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

import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.reflect.{ GenericType, GenericTypeParameter }
import com.datastax.oss.driver.shaded.guava.common.reflect.TypeToken

import scala.collection.immutable.SortedSet

class SortedSetCodec[T: Ordering](inner: TypeCodec[T], frozen: Boolean)
    extends AbstractSetCodec[T, SortedSet](inner, frozen) {

  override val getJavaType: GenericType[SortedSet[T]] =
    GenericType
      .of(new TypeToken[SortedSet[T]]() {}.getType)
      .where(new GenericTypeParameter[T] {}, inner.getJavaType.wrap())
      .asInstanceOf[GenericType[SortedSet[T]]]
}

object SortedSetCodec {
  def apply[T: Ordering](inner: TypeCodec[T], frozen: Boolean): SortedSetCodec[T] =
    new SortedSetCodec(inner, frozen)

  def frozen[T: Ordering](inner: TypeCodec[T]): SortedSetCodec[T] =
    new SortedSetCodec[T](inner, true)
}
