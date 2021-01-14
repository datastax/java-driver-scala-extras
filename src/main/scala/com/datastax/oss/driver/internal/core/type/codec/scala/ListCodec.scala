package com.datastax.oss.driver.internal.core.`type`.codec.scala

import java.nio.ByteBuffer

import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.`type`.DataType
import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.reflect.{ GenericType, GenericTypeParameter }
import com.datastax.oss.driver.internal.core.`type`.DefaultListType
import com.datastax.oss.driver.internal.core.`type`.codec.ParseUtils
import com.datastax.oss.driver.shaded.guava.common.reflect.TypeToken

import scala.collection.mutable.ListBuffer

class ListCodec[T](inner: TypeCodec[T], frozen: Boolean) extends TypeCodec[List[T]] {

  override def accepts(value: Any): Boolean = value match {
    case l: List[_] => l.headOption.fold(true)(inner.accepts)
    case _ => false
  }

  override val getJavaType: GenericType[List[T]] =
    GenericType
      .of(new TypeToken[List[T]]() {}.getType)
      .where(new GenericTypeParameter[T] {}, inner.getJavaType.wrap())
      .asInstanceOf[GenericType[List[T]]]

  override val getCqlType: DataType = new DefaultListType(inner.getCqlType, frozen)

  override def encode(value: List[T], protocolVersion: ProtocolVersion): ByteBuffer =
    if (value == null) null
    else {
      // FIXME this seems pretty costly, we iterate the list several times!
      val buffers = for (item <- value) yield {
        if (item == null) {
          throw new IllegalArgumentException("List elements cannot be null")
        }
        inner.encode(item, protocolVersion)
      }

      pack(buffers.toArray, value.size, protocolVersion)
    }

  override def decode(bytes: ByteBuffer, protocolVersion: ProtocolVersion): List[T] =
    if (bytes == null || bytes.remaining == 0) List.empty[T]
    else {
      val list  = List.newBuilder[T]
      val input = bytes.duplicate()
      val size  = readSize(input, protocolVersion)
      for (_ <- 0 until size) {
        list += inner.decode(
          readValue(input, protocolVersion),
          protocolVersion
        )
      }

      list.result()
    }

  override def format(value: List[T]): String =
    if (value == null) {
      "NULL"
    } else {
      value.mkString("[", ",", "]")
    }

  override def parse(value: String): List[T] =
    if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) {
      List.empty[T]
    } else {
      var idx = ParseUtils.skipSpaces(value, 0)
      if (value.charAt(idx) != '[') {
        throw new IllegalArgumentException(
          s"Cannot parse list value from '$value', at character $idx expecting '[' but got '${value.charAt(idx)}''"
        )
      }
      idx = ParseUtils.skipSpaces(value, idx + 1)
      if (value.charAt(idx) == ']') {
        List.empty[T]
      } else {
        val list = ListBuffer.empty[T]
        while (idx < value.length) {
          val n = ParseUtils.skipCQLValue(value, idx)
          list.append(
            inner.parse(value.substring(idx, n))
          )

          idx = ParseUtils.skipSpaces(value, n)
          if (idx >= value.length) {
            throw new IllegalArgumentException(
              s"Malformed list value '$value', missing closing ']'"
            )
          } else if (value.charAt(idx) == ']') {
            return list.result()
          } else if (value.charAt(idx) != ',') {
            throw new IllegalArgumentException(
              s"Cannot parse list value from '$value', at character $idx expecting ',' but got '${value
                .charAt(idx)}''"
            )
          }
          idx = ParseUtils.skipSpaces(value, idx + 1)
        }
        throw new IllegalArgumentException(s"Malformed list value '$value', missing closing ']'")
      }
    }
}

object ListCodec {
  def frozen[T](inner: TypeCodec[T]): ListCodec[T] = new ListCodec[T](inner, true)
}
