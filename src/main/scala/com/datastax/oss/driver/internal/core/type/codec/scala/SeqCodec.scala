package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.reflect.{ GenericType, GenericTypeParameter }
import com.datastax.oss.driver.shaded.guava.common.reflect.TypeToken

class SeqCodec[T](inner: TypeCodec[T], frozen: Boolean)
    extends AbstractSeqCodec[T, Seq](inner, frozen) {

  // Doing this here, TypeToken complains on construction if is `M[T]`.
  // TODO investigate is there a way to doing in the parent class
  override val getJavaType: GenericType[Seq[T]] =
    GenericType
      .of(new TypeToken[Seq[T]]() {}.getType)
      .where(new GenericTypeParameter[T] {}, inner.getJavaType.wrap())
      .asInstanceOf[GenericType[Seq[T]]]
}

object SeqCodec {
  def frozen[T](inner: TypeCodec[T]): SeqCodec[T] = new SeqCodec[T](inner, true)
}
