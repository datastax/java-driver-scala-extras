package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.reflect.{ GenericType, GenericTypeParameter }
import com.datastax.oss.driver.shaded.guava.common.reflect.TypeToken

class VectorCodec[T](inner: TypeCodec[T], frozen: Boolean)
    extends AbstractSeqCodec[T, Vector](inner, frozen) {

  // Doing this here, TypeToken complains on construction if is `M[T]`.
  // TODO investigate is there a way to doing in the parent class
  override val getJavaType: GenericType[Vector[T]] =
    GenericType
      .of(new TypeToken[Vector[T]]() {}.getType)
      .where(new GenericTypeParameter[T] {}, inner.getJavaType.wrap())
      .asInstanceOf[GenericType[Vector[T]]]

}

object VectorCodec {
  def frozen[T](inner: TypeCodec[T]): VectorCodec[T] = new VectorCodec[T](inner, true)
}
