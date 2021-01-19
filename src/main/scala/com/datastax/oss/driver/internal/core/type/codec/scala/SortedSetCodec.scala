package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.reflect.{ GenericType, GenericTypeParameter }
import com.datastax.oss.driver.shaded.guava.common.reflect.TypeToken

import scala.collection.immutable.SortedSet

class SortedSetCodec[T: Ordering](inner: TypeCodec[T], frozen: Boolean)
    extends AbstractSetCodec[T, SortedSet](inner, frozen) {

  // Doing this here, TypeToken complains on construction if is `M[T]`.
  // TODO investigate is there a way to doing in the parent class
  override val getJavaType: GenericType[SortedSet[T]] =
    GenericType
      .of(new TypeToken[SortedSet[T]]() {}.getType)
      .where(new GenericTypeParameter[T] {}, inner.getJavaType.wrap())
      .asInstanceOf[GenericType[SortedSet[T]]]
}

object SortedSetCodec {
  def frozen[T: Ordering](inner: TypeCodec[T]): SortedSetCodec[T] =
    new SortedSetCodec[T](inner, true)
}
