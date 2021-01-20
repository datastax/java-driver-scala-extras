package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.{ MappingCodec, TypeCodecs }
import com.datastax.oss.driver.api.core.`type`.reflect.GenericType

object BigIntCodec
    extends MappingCodec[java.math.BigInteger, BigInt](
      TypeCodecs.VARINT,
      GenericType.of(classOf[BigInt])
    ) {

  override def innerToOuter(value: java.math.BigInteger): BigInt =
    if (value == null) null else BigInt(value)

  override def outerToInner(value: BigInt): java.math.BigInteger =
    if (value == null) null else value.bigInteger
}
