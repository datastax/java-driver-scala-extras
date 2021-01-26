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

class ListCodec[T](inner: TypeCodec[T], frozen: Boolean)
    extends AbstractSeqCodec[T, List](inner, frozen) {

  override val getJavaType: GenericType[List[T]] =
    GenericType
      .of(new TypeToken[List[T]]() {}.getType)
      .where(new GenericTypeParameter[T] {}, inner.getJavaType.wrap())
      .asInstanceOf[GenericType[List[T]]]
}

object ListCodec {
  def apply[T](inner: TypeCodec[T], frozen: Boolean): ListCodec[T] =
    new ListCodec(inner, frozen)

  def frozen[T](inner: TypeCodec[T]): ListCodec[T] = new ListCodec[T](inner, true)
}
