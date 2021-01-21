package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.reflect.{ GenericType, GenericTypeParameter }
import com.datastax.oss.driver.shaded.guava.common.reflect.TypeToken

import scala.collection.immutable.SortedMap

class SortedMapCodec[K: Ordering, V](
    keyInner: TypeCodec[K],
    valueInner: TypeCodec[V],
    frozen: Boolean
) extends AbstractMapCodec[K, V, SortedMap](keyInner, valueInner, frozen) {

  override val getJavaType: GenericType[SortedMap[K, V]] =
    GenericType
      .of(new TypeToken[SortedMap[K, V]]() {}.getType)
      .where(new GenericTypeParameter[K] {}, keyInner.getJavaType.wrap())
      .where(new GenericTypeParameter[V] {}, valueInner.getJavaType.wrap())
      .asInstanceOf[GenericType[SortedMap[K, V]]]
}

object SortedMapCodec {
  def frozen[K: Ordering, V](
      keyInner: TypeCodec[K],
      valueInner: TypeCodec[V]
  ): SortedMapCodec[K, V] =
    new SortedMapCodec(keyInner, valueInner, true)
}
