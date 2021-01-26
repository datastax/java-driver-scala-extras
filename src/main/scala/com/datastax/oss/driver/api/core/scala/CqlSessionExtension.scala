/*
 * Copyright 2017 DataStax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datastax.oss.driver.api.core.scala

import java.nio.ByteBuffer
import java.util

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.codec.registry.MutableCodecRegistry
import com.datastax.oss.driver.api.core.cql._

import _root_.scala.concurrent.{ExecutionContext, Future}

abstract class CqlSessionExtension(session: CqlSession) {

  def registerCodecs(codecs: TypeCodec[_]*): Unit =
    session.getContext.getCodecRegistry match {
      case mutableRegistry: MutableCodecRegistry =>
        mutableRegistry.register(codecs: _*)

      case _ =>
        throw new IllegalStateException("CodecRegistry isn't mutable")
    }

  protected class ScalaPreparedStatement[U] private[core] (
      fn: U => BoundStatement,
      pstmt: PreparedStatement
  ) extends PreparedStatement {

    def apply(u: U): BoundStatement = fn(u)

    override def getId: ByteBuffer                          = pstmt.getId
    override def getQuery: String                           = pstmt.getQuery
    override def getVariableDefinitions: ColumnDefinitions  = pstmt.getVariableDefinitions
    override def getPartitionKeyIndices: util.List[Integer] = pstmt.getPartitionKeyIndices
    override def getResultMetadataId: ByteBuffer            = pstmt.getResultMetadataId
    override def getResultSetDefinitions: ColumnDefinitions = pstmt.getResultSetDefinitions
    override def setResultMetadata(
        newResultMetadataId: ByteBuffer,
        newResultSetDefinitions: ColumnDefinitions
    ): Unit =
      pstmt.setResultMetadata(newResultMetadataId, newResultSetDefinitions)
    override def bind(values: AnyRef*): BoundStatement =
      pstmt.bind(values: _*)
    override def boundStatementBuilder(values: AnyRef*): BoundStatementBuilder =
      pstmt.boundStatementBuilder(values: _*)
  }

  // **********************************************************************
  // To generate methods to Tuple2 and above, use this template method.
  // **********************************************************************
  //
  //  def template(t: Int): String = {
  //    val ts             = (1 to t).map(i => s"t$i")
  //    val Ts             = ts.map(_.toUpperCase)
  //    val typeParameters = Ts.mkString(", ")
  //    val codecs = ts
  //      .zip(Ts)
  //      .map { case (param, typ) =>
  //        s"${param}: TypeCodec[$typ]"
  //      }
  //      .mkString(", ")
  //    val setters = (1 to t).map(i => s".set(${i - 1}, t._$i, t$i)").mkString("")
  //
  //    s"""def iPrepare[$typeParameters](query: String)(implicit $codecs): ScalaPreparedStatement[($typeParameters)] = {
  //       |    val pstmt = session.prepare(query)
  //       |
  //       |    new ScalaPreparedStatement[($typeParameters)](
  //       |      (t: ($typeParameters)) => pstmt.bind()${setters},
  //       |      pstmt
  //       |    )
  //       |  }
  //       |""".stripMargin
  //  }
  //
  // (2 to 22).map(template).foreach(println(_))

  // format: off

  def iPrepare[T1](query: String)(implicit t1: TypeCodec[T1]): ScalaPreparedStatement[T1] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[T1](
      (t: T1) => pstmt.bind().set(0, t, t1),
      pstmt
    )
  }

  def iPrepare[T1, T2](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2]): ScalaPreparedStatement[(T1, T2)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2)](
      (t: (T1, T2)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3]): ScalaPreparedStatement[(T1, T2, T3)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3)](
      (t: (T1, T2, T3)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3, T4](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4]): ScalaPreparedStatement[(T1, T2, T3, T4)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3, T4)](
      (t: (T1, T2, T3, T4)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3, T4, T5](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5]): ScalaPreparedStatement[(T1, T2, T3, T4, T5)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3, T4, T5)](
      (t: (T1, T2, T3, T4, T5)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3, T4, T5, T6](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6]): ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6)](
      (t: (T1, T2, T3, T4, T5, T6)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3, T4, T5, T6, T7](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7]): ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7)](
      (t: (T1, T2, T3, T4, T5, T6, T7)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8]): ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8)](
      (t: (T1, T2, T3, T4, T5, T6, T7, T8)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9]): ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9)](
      (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10]): ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)](
      (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11]): ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)](
      (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12]): ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12)](
      (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13]): ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13)](
      (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14]): ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14)](
      (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13).set(13, t._14, t14),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15]): ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15)](
      (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13).set(13, t._14, t14).set(14, t._15, t15),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16]): ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16)](
      (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13).set(13, t._14, t14).set(14, t._15, t15).set(15, t._16, t16),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17]): ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17)](
      (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13).set(13, t._14, t14).set(14, t._15, t15).set(15, t._16, t16).set(16, t._17, t17),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18]): ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18)](
      (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13).set(13, t._14, t14).set(14, t._15, t15).set(15, t._16, t16).set(16, t._17, t17).set(17, t._18, t18),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18], t19: TypeCodec[T19]): ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19)](
      (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13).set(13, t._14, t14).set(14, t._15, t15).set(15, t._16, t16).set(16, t._17, t17).set(17, t._18, t18).set(18, t._19, t19),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18], t19: TypeCodec[T19], t20: TypeCodec[T20]): ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20)](
      (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13).set(13, t._14, t14).set(14, t._15, t15).set(15, t._16, t16).set(16, t._17, t17).set(17, t._18, t18).set(18, t._19, t19).set(19, t._20, t20),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18], t19: TypeCodec[T19], t20: TypeCodec[T20], t21: TypeCodec[T21]): ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21)](
      (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13).set(13, t._14, t14).set(14, t._15, t15).set(15, t._16, t16).set(16, t._17, t17).set(17, t._18, t18).set(18, t._19, t19).set(19, t._20, t20).set(20, t._21, t21),
      pstmt
    )
  }
  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22](query: String)(implicit t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18], t19: TypeCodec[T19], t20: TypeCodec[T20], t21: TypeCodec[T21], t22: TypeCodec[T22]): ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22)] = {
    val pstmt = session.prepare(query)

    new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22)](
      (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13).set(13, t._14, t14).set(14, t._15, t15).set(15, t._16, t16).set(16, t._17, t17).set(17, t._18, t18).set(18, t._19, t19).set(19, t._20, t20).set(20, t._21, t21).set(21, t._22, t22),
      pstmt
    )
  }
  // format: on
}

abstract class CqlSessionAsyncExtension(session: CqlSession) extends CqlSessionExtension(session) {

  import com.datastax.oss.driver.api.core.scala.FutureConverters._

  // format: off
  // **********************************************************************
  // To generate methods to Tuple2 and above, use this template method.
  // **********************************************************************
  //
  //  def template(t: Int): String = {
  //    val ts             = (1 to t).map(i => s"t$i")
  //    val Ts             = ts.map(_.toUpperCase)
  //    val typeParameters = Ts.mkString(", ")
  //    val codecs = ts
  //      .zip(Ts)
  //      .map { case (param, typ) =>
  //        s"${param}: TypeCodec[$typ]"
  //      }
  //      .mkString(", ")
  //    val setters = (1 to t).map(i => s".set(${i - 1}, t._$i, t$i)").mkString("")
  //
  //    s"""def iPrepare[$typeParameters](query: String)(implicit ec: ExecutionContext, $codecs): Future[ScalaPreparedStatement[($typeParameters)]] =
  //       |  session.prepareAsync(query).asScala.map { pstmt =>
  //       |    new ScalaPreparedStatement[($typeParameters)](
  //       |      (t: ($typeParameters)) => pstmt.bind()${setters},
  //       |      pstmt
  //       |    )
  //       |  }
  //       |""".stripMargin
  //  }
  //
  // (2 to 22).map(template).foreach(println(_))

  def iPrepareAsync[T1](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1]): Future[ScalaPreparedStatement[T1]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[T1](
        (t: T1) => pstmt.bind().set(0, t, t1),
        pstmt
      )
    }

  def iPrepare[T1, T2](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2]): Future[ScalaPreparedStatement[(T1, T2)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2)](
        (t: (T1, T2)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3]): Future[ScalaPreparedStatement[(T1, T2, T3)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3)](
        (t: (T1, T2, T3)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3, T4](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4]): Future[ScalaPreparedStatement[(T1, T2, T3, T4)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3, T4)](
        (t: (T1, T2, T3, T4)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3, T4, T5](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5]): Future[ScalaPreparedStatement[(T1, T2, T3, T4, T5)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3, T4, T5)](
        (t: (T1, T2, T3, T4, T5)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3, T4, T5, T6](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6]): Future[ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6)](
        (t: (T1, T2, T3, T4, T5, T6)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3, T4, T5, T6, T7](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7]): Future[ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7)](
        (t: (T1, T2, T3, T4, T5, T6, T7)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8]): Future[ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8)](
        (t: (T1, T2, T3, T4, T5, T6, T7, T8)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9]): Future[ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9)](
        (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10]): Future[ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)](
        (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11]): Future[ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)](
        (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12]): Future[ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12)](
        (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13]): Future[ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13)](
        (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14]): Future[ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14)](
        (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13).set(13, t._14, t14),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15]): Future[ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15)](
        (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13).set(13, t._14, t14).set(14, t._15, t15),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16]): Future[ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16)](
        (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13).set(13, t._14, t14).set(14, t._15, t15).set(15, t._16, t16),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17]): Future[ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17)](
        (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13).set(13, t._14, t14).set(14, t._15, t15).set(15, t._16, t16).set(16, t._17, t17),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18]): Future[ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18)](
        (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13).set(13, t._14, t14).set(14, t._15, t15).set(15, t._16, t16).set(16, t._17, t17).set(17, t._18, t18),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18], t19: TypeCodec[T19]): Future[ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19)](
        (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13).set(13, t._14, t14).set(14, t._15, t15).set(15, t._16, t16).set(16, t._17, t17).set(17, t._18, t18).set(18, t._19, t19),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18], t19: TypeCodec[T19], t20: TypeCodec[T20]): Future[ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20)](
        (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13).set(13, t._14, t14).set(14, t._15, t15).set(15, t._16, t16).set(16, t._17, t17).set(17, t._18, t18).set(18, t._19, t19).set(19, t._20, t20),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18], t19: TypeCodec[T19], t20: TypeCodec[T20], t21: TypeCodec[T21]): Future[ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21)](
        (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13).set(13, t._14, t14).set(14, t._15, t15).set(15, t._16, t16).set(16, t._17, t17).set(17, t._18, t18).set(18, t._19, t19).set(19, t._20, t20).set(20, t._21, t21),
        pstmt
      )
    }

  def iPrepare[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22](query: String)(implicit ec: ExecutionContext, t1: TypeCodec[T1], t2: TypeCodec[T2], t3: TypeCodec[T3], t4: TypeCodec[T4], t5: TypeCodec[T5], t6: TypeCodec[T6], t7: TypeCodec[T7], t8: TypeCodec[T8], t9: TypeCodec[T9], t10: TypeCodec[T10], t11: TypeCodec[T11], t12: TypeCodec[T12], t13: TypeCodec[T13], t14: TypeCodec[T14], t15: TypeCodec[T15], t16: TypeCodec[T16], t17: TypeCodec[T17], t18: TypeCodec[T18], t19: TypeCodec[T19], t20: TypeCodec[T20], t21: TypeCodec[T21], t22: TypeCodec[T22]): Future[ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22)]] =
    session.prepareAsync(query).asScala.map { pstmt =>
      new ScalaPreparedStatement[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22)](
        (t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22)) => pstmt.bind().set(0, t._1, t1).set(1, t._2, t2).set(2, t._3, t3).set(3, t._4, t4).set(4, t._5, t5).set(5, t._6, t6).set(6, t._7, t7).set(7, t._8, t8).set(8, t._9, t9).set(9, t._10, t10).set(10, t._11, t11).set(11, t._12, t12).set(12, t._13, t13).set(13, t._14, t14).set(14, t._15, t15).set(15, t._16, t16).set(16, t._17, t17).set(17, t._18, t18).set(18, t._19, t19).set(19, t._20, t20).set(20, t._21, t21).set(21, t._22, t22),
        pstmt
      )
    }

  // format: on
}
