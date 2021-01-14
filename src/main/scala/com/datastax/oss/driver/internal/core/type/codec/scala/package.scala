package com.datastax.oss.driver.internal.core.`type`.codec

import java.nio.ByteBuffer

import com.datastax.oss.driver.api.core.ProtocolVersion

package object scala {
  // TODO check if this could be more Scala-y
  // TODO check if we could skip the second iteration
  // TODO `elements` seems unnecessary given that we've an array
  def pack(buffers: Array[ByteBuffer], elements: Int, version: ProtocolVersion): ByteBuffer = {
    var size = 0
    for (idx <- buffers.indices) {
      size += sizeOfValue(buffers(idx), version)
    }
    val result = ByteBuffer.allocate(sizeOfCollectionSize(version) + size)
    writeSize(result, elements, version)
    for (idx <- buffers.indices) {
      writeValue(result, buffers(idx), version)
    }

    result.flip().asInstanceOf[ByteBuffer]
  }

  def readSize(input: ByteBuffer, version: ProtocolVersion): Int = version match {
    case ProtocolVersion.DSE_V1 | ProtocolVersion.DSE_V2 =>
      // getUnsignedShort
      val length = (input.get() & 0xff) << 8
      length | (input.get() & 0xff)

    case ProtocolVersion.V3 | ProtocolVersion.V4 | ProtocolVersion.V5 =>
      input.getInt()

    case _ =>
      throw new UnsupportedOperationException()
  }

  def writeSize(output: ByteBuffer, size: Int, version: ProtocolVersion): Unit = version match {
    case ProtocolVersion.DSE_V1 | ProtocolVersion.DSE_V2 =>
      if (size > 65535)
        throw new IllegalArgumentException(
          s"Native protocol version $version supports up to 65535 elements in any collection - but collection contains $size elements"
        )
      output.putShort(size.toShort)

    case ProtocolVersion.V3 | ProtocolVersion.V4 | ProtocolVersion.V5 =>
      output.putInt(size)

    case _ =>
      throw new UnsupportedOperationException()
  }

  def readValue(input: ByteBuffer, version: ProtocolVersion): ByteBuffer = {
    val size = readSize(input, version)
    if (size < 0) null else readBytes(input, size)
  }

  def writeValue(output: ByteBuffer, value: ByteBuffer, version: ProtocolVersion): Unit =
    version match {
      case ProtocolVersion.DSE_V1 | ProtocolVersion.DSE_V2 =>
        output.putShort(value.remaining().toShort)
        output.put(value.duplicate())

      case ProtocolVersion.V3 | ProtocolVersion.V4 | ProtocolVersion.V5 =>
        if (value == null) {
          output.putInt(-1)
        } else {
          output.putInt(value.remaining().toShort)
          output.put(value.duplicate())
        }

      case _ =>
        throw new UnsupportedOperationException()
    }

  def readBytes(input: ByteBuffer, length: Int): ByteBuffer = {
    val copy = input.duplicate()
    copy.limit(copy.position() + length)
    input.position(input.position() + length)
    copy
  }

  def sizeOfCollectionSize(version: ProtocolVersion): Int = version match {
    case ProtocolVersion.DSE_V1 | ProtocolVersion.DSE_V2 =>
      2

    case ProtocolVersion.V3 | ProtocolVersion.V4 | ProtocolVersion.V5 =>
      4

    case _ =>
      throw new UnsupportedOperationException()
  }

  def sizeOfValue(value: ByteBuffer, version: ProtocolVersion): Int = version match {
    case ProtocolVersion.DSE_V1 | ProtocolVersion.DSE_V2 =>
      val elemSize = value.remaining()
      if (elemSize > 65535)
        throw new IllegalArgumentException(
          s"Native protocol version $version supports up to 65535 elements in any collection - but collection contains $elemSize elements"
        )
      2 + elemSize

    case ProtocolVersion.V3 | ProtocolVersion.V4 | ProtocolVersion.V5 =>
      if (value == null) 4 else 4 + value.remaining()

    case _ =>
      throw new UnsupportedOperationException()
  }
}
