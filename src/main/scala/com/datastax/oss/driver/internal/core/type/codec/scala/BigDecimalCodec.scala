package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.{ MappingCodec, TypeCodecs }
import com.datastax.oss.driver.api.core.`type`.reflect.GenericType

object BigDecimalCodec
    extends MappingCodec[java.math.BigDecimal, BigDecimal](
      TypeCodecs.DECIMAL,
      GenericType.of(classOf[BigDecimal])
    ) {

  override def innerToOuter(value: java.math.BigDecimal): BigDecimal =
    if (value == null) null else BigDecimal(value)

  override def outerToInner(value: BigDecimal): java.math.BigDecimal =
    if (value == null) null else value.bigDecimal
}
