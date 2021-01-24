package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.reflect.{ GenericType, GenericTypeParameter }
import com.datastax.oss.driver.shaded.guava.common.reflect.TypeToken

class SetCodec[T](inner: TypeCodec[T], frozen: Boolean)
    extends AbstractSetCodec[T, Set](inner, frozen) {

  override val getJavaType: GenericType[Set[T]] =
    GenericType
      .of(new TypeToken[Set[T]]() {}.getType)
      .where(new GenericTypeParameter[T] {}, inner.getJavaType.wrap())
      .asInstanceOf[GenericType[Set[T]]]
}

object SetCodec {
  def frozen[T](inner: TypeCodec[T]): SetCodec[T] = new SetCodec[T](inner, true)
}
