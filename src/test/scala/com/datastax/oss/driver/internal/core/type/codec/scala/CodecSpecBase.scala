package com.datastax.oss.driver.internal.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.protocol.internal.util.Bytes

trait CodecSpecBase[T] {

  protected val codec: TypeCodec[T]

  protected def encode(t: T, protocolVersion: ProtocolVersion): Option[String] =
    Option(codec.encode(t, protocolVersion)).map(Bytes.toHexString)

  protected def encode(t: T): Option[String] = encode(t, ProtocolVersion.DEFAULT)

  protected def decode(hexString: String, protocolVersion: ProtocolVersion): Option[T] =
    Option(hexString).map(Bytes.fromHexString).flatMap(hex => Option(codec.decode(hex, protocolVersion))) // FIXME, this is wrong, if hexString is null, then nothing happens

  protected def decode(hexString: String): Option[T] = decode(hexString, ProtocolVersion.DEFAULT)

  protected def format(t: T): String = codec.format(t)

  protected def parse(s: String): T = codec.parse(s)
}
