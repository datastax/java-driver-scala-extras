package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.reflect.{ GenericType, GenericTypeParameter }
import com.datastax.oss.driver.shaded.guava.common.reflect.TypeToken

class MapCodec[K, V](keyInner: TypeCodec[K], valueInner: TypeCodec[V], frozen: Boolean)
    extends AbstractMapCodec[K, V, Map](keyInner, valueInner, frozen) {

  override val getJavaType: GenericType[Map[K, V]] =
    GenericType
      .of(new TypeToken[Map[K, V]]() {}.getType)
      .where(new GenericTypeParameter[K] {}, keyInner.getJavaType.wrap())
      .where(new GenericTypeParameter[V] {}, valueInner.getJavaType.wrap())
      .asInstanceOf[GenericType[Map[K, V]]]
}

object MapCodec {
  def frozen[K, V](keyInner: TypeCodec[K], valueInner: TypeCodec[V]): MapCodec[K, V] =
    new MapCodec(keyInner, valueInner, true)
}
