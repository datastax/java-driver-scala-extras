package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.reflect.{ GenericType, GenericTypeParameter }
import com.datastax.oss.driver.shaded.guava.common.reflect.TypeToken

class ListCodec[T](inner: TypeCodec[T], frozen: Boolean)
    extends AbstractSeqCodec[T, List](inner, frozen) {

  // Doing this here, TypeToken complains on construction if is `M[T]`.
  // TODO investigate is there a way to doing in the parent class
  override val getJavaType: GenericType[List[T]] =
    GenericType
      .of(new TypeToken[List[T]]() {}.getType)
      .where(new GenericTypeParameter[T] {}, inner.getJavaType.wrap())
      .asInstanceOf[GenericType[List[T]]]
}

object ListCodec {
  def frozen[T](inner: TypeCodec[T]): ListCodec[T] = new ListCodec[T](inner, true)
}
