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

package com.datastax.oss.driver.api.core.`type`.codec.scala

import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.scala.CqlSessionAsyncExtension

import scala.collection.immutable.{SortedMap, SortedSet}

object Implicits {

  implicit val bigDecimalCodec: TypeCodec[BigDecimal] = TypeCodecs.bigDecimalCodec
  implicit val bigIntCodec: TypeCodec[BigInt]         = TypeCodecs.bigIntCodec
  implicit val booleanCodec: TypeCodec[Boolean]       = TypeCodecs.booleanCodec
  implicit val byteCodec: TypeCodec[Byte]             = TypeCodecs.byteCodec
  implicit val doubleCodec: TypeCodec[Double]         = TypeCodecs.doubleCodec
  implicit val floatCodec: TypeCodec[Float]           = TypeCodecs.floatCodec
  implicit val intCodec: TypeCodec[Int]               = TypeCodecs.intCodec
  implicit val longCodec: TypeCodec[Long]             = TypeCodecs.longCodec
  implicit val shortCodec: TypeCodec[Short]           = TypeCodecs.shortCodec
  implicit val stringCodec: TypeCodec[String]         = TypeCodecs.stringCodec

  implicit def optionCodec[T: TypeCodec]: TypeCodec[Option[T]] =
    TypeCodecs.optionOf(implicitly[TypeCodec[T]])

  implicit def frozenSeqCodec[T: TypeCodec]: TypeCodec[Seq[T]] =
    TypeCodecs.seqOf(implicitly[TypeCodec[T]])

  implicit def frozenListCodec[T: TypeCodec]: TypeCodec[List[T]] =
    TypeCodecs.listOf(implicitly[TypeCodec[T]])

  implicit def frozenVectorCodec[T: TypeCodec]: TypeCodec[Vector[T]] =
    TypeCodecs.vectorOf(implicitly[TypeCodec[T]])

  implicit def frozenSetCodec[T: TypeCodec]: TypeCodec[Set[T]] =
    TypeCodecs.setOf(implicitly[TypeCodec[T]])

  implicit def frozenSortedSetCodec[T: TypeCodec: Ordering]: TypeCodec[SortedSet[T]] =
    TypeCodecs.sortedSetOf(implicitly[TypeCodec[T]])

  implicit def frozenMapCodec[K: TypeCodec, V: TypeCodec]: TypeCodec[Map[K, V]] =
    TypeCodecs.mapOf(implicitly[TypeCodec[K]], implicitly[TypeCodec[V]])

  implicit def frozenSortedMapCodec[K: TypeCodec: Ordering, V: TypeCodec]
      : TypeCodec[SortedMap[K, V]] =
    TypeCodecs.sorterMapOf(implicitly[TypeCodec[K]], implicitly[TypeCodec[V]])

  implicit class CqlSessionOps(session: CqlSession) extends CqlSessionAsyncExtension(session)

  // **********************************************************************
  // To generate methods to Tuple2 and above, use this template method.
  // **********************************************************************
  //
  //  def template(t: Int): String = {
  //
  //    val ts             = (1 to t).map(i => s"t$i")
  //    val Ts             = ts.map(_.toUpperCase)
  //    val typeParameters = Ts.mkString(", ")
  //    val contextBounds  = Ts.map(t => s"$t: TypeCodec").mkString(", ")
  //    val implicitlies   = Ts.map(t => s"implicitly[TypeCodec[$t]]").mkString(", ")
  //
  //    s"""implicit def tuple${t}Codec[$contextBounds]: TypeCodec[($typeParameters)] =
  //       |    TypeCodecs.tuple${t}Of[$typeParameters]($implicitlies)
  //       |""".stripMargin
  //  }
  //
  //  (2 to 22).map(template).foreach(println(_))

  // format: off

  implicit def tuple1Codec[T: TypeCodec]: TypeCodec[Tuple1[T]] =
    TypeCodecs.tuple1Of(implicitly[TypeCodec[T]])

  implicit def tuple2Codec[T1: TypeCodec, T2: TypeCodec]: TypeCodec[(T1, T2)] =
    TypeCodecs.tuple2Of[T1, T2](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]])

  implicit def tuple3Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec]: TypeCodec[(T1, T2, T3)] =
    TypeCodecs.tuple3Of[T1, T2, T3](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]])

  implicit def tuple4Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec, T4: TypeCodec]: TypeCodec[(T1, T2, T3, T4)] =
    TypeCodecs.tuple4Of[T1, T2, T3, T4](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]], implicitly[TypeCodec[T4]])

  implicit def tuple5Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec, T4: TypeCodec, T5: TypeCodec]: TypeCodec[(T1, T2, T3, T4, T5)] =
    TypeCodecs.tuple5Of[T1, T2, T3, T4, T5](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]], implicitly[TypeCodec[T4]], implicitly[TypeCodec[T5]])

  implicit def tuple6Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec, T4: TypeCodec, T5: TypeCodec, T6: TypeCodec]: TypeCodec[(T1, T2, T3, T4, T5, T6)] =
    TypeCodecs.tuple6Of[T1, T2, T3, T4, T5, T6](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]], implicitly[TypeCodec[T4]], implicitly[TypeCodec[T5]], implicitly[TypeCodec[T6]])

  implicit def tuple7Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec, T4: TypeCodec, T5: TypeCodec, T6: TypeCodec, T7: TypeCodec]: TypeCodec[(T1, T2, T3, T4, T5, T6, T7)] =
    TypeCodecs.tuple7Of[T1, T2, T3, T4, T5, T6, T7](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]], implicitly[TypeCodec[T4]], implicitly[TypeCodec[T5]], implicitly[TypeCodec[T6]], implicitly[TypeCodec[T7]])

  implicit def tuple8Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec, T4: TypeCodec, T5: TypeCodec, T6: TypeCodec, T7: TypeCodec, T8: TypeCodec]: TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8)] =
    TypeCodecs.tuple8Of[T1, T2, T3, T4, T5, T6, T7, T8](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]], implicitly[TypeCodec[T4]], implicitly[TypeCodec[T5]], implicitly[TypeCodec[T6]], implicitly[TypeCodec[T7]], implicitly[TypeCodec[T8]])

  implicit def tuple9Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec, T4: TypeCodec, T5: TypeCodec, T6: TypeCodec, T7: TypeCodec, T8: TypeCodec, T9: TypeCodec]: TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9)] =
    TypeCodecs.tuple9Of[T1, T2, T3, T4, T5, T6, T7, T8, T9](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]], implicitly[TypeCodec[T4]], implicitly[TypeCodec[T5]], implicitly[TypeCodec[T6]], implicitly[TypeCodec[T7]], implicitly[TypeCodec[T8]], implicitly[TypeCodec[T9]])

  implicit def tuple10Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec, T4: TypeCodec, T5: TypeCodec, T6: TypeCodec, T7: TypeCodec, T8: TypeCodec, T9: TypeCodec, T10: TypeCodec]: TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)] =
    TypeCodecs.tuple10Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]], implicitly[TypeCodec[T4]], implicitly[TypeCodec[T5]], implicitly[TypeCodec[T6]], implicitly[TypeCodec[T7]], implicitly[TypeCodec[T8]], implicitly[TypeCodec[T9]], implicitly[TypeCodec[T10]])

  implicit def tuple11Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec, T4: TypeCodec, T5: TypeCodec, T6: TypeCodec, T7: TypeCodec, T8: TypeCodec, T9: TypeCodec, T10: TypeCodec, T11: TypeCodec]: TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)] =
    TypeCodecs.tuple11Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]], implicitly[TypeCodec[T4]], implicitly[TypeCodec[T5]], implicitly[TypeCodec[T6]], implicitly[TypeCodec[T7]], implicitly[TypeCodec[T8]], implicitly[TypeCodec[T9]], implicitly[TypeCodec[T10]], implicitly[TypeCodec[T11]])

  implicit def tuple12Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec, T4: TypeCodec, T5: TypeCodec, T6: TypeCodec, T7: TypeCodec, T8: TypeCodec, T9: TypeCodec, T10: TypeCodec, T11: TypeCodec, T12: TypeCodec]: TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12)] =
    TypeCodecs.tuple12Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]], implicitly[TypeCodec[T4]], implicitly[TypeCodec[T5]], implicitly[TypeCodec[T6]], implicitly[TypeCodec[T7]], implicitly[TypeCodec[T8]], implicitly[TypeCodec[T9]], implicitly[TypeCodec[T10]], implicitly[TypeCodec[T11]], implicitly[TypeCodec[T12]])

  implicit def tuple13Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec, T4: TypeCodec, T5: TypeCodec, T6: TypeCodec, T7: TypeCodec, T8: TypeCodec, T9: TypeCodec, T10: TypeCodec, T11: TypeCodec, T12: TypeCodec, T13: TypeCodec]: TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13)] =
    TypeCodecs.tuple13Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]], implicitly[TypeCodec[T4]], implicitly[TypeCodec[T5]], implicitly[TypeCodec[T6]], implicitly[TypeCodec[T7]], implicitly[TypeCodec[T8]], implicitly[TypeCodec[T9]], implicitly[TypeCodec[T10]], implicitly[TypeCodec[T11]], implicitly[TypeCodec[T12]], implicitly[TypeCodec[T13]])

  implicit def tuple14Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec, T4: TypeCodec, T5: TypeCodec, T6: TypeCodec, T7: TypeCodec, T8: TypeCodec, T9: TypeCodec, T10: TypeCodec, T11: TypeCodec, T12: TypeCodec, T13: TypeCodec, T14: TypeCodec]: TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14)] =
    TypeCodecs.tuple14Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]], implicitly[TypeCodec[T4]], implicitly[TypeCodec[T5]], implicitly[TypeCodec[T6]], implicitly[TypeCodec[T7]], implicitly[TypeCodec[T8]], implicitly[TypeCodec[T9]], implicitly[TypeCodec[T10]], implicitly[TypeCodec[T11]], implicitly[TypeCodec[T12]], implicitly[TypeCodec[T13]], implicitly[TypeCodec[T14]])

  implicit def tuple15Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec, T4: TypeCodec, T5: TypeCodec, T6: TypeCodec, T7: TypeCodec, T8: TypeCodec, T9: TypeCodec, T10: TypeCodec, T11: TypeCodec, T12: TypeCodec, T13: TypeCodec, T14: TypeCodec, T15: TypeCodec]: TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15)] =
    TypeCodecs.tuple15Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]], implicitly[TypeCodec[T4]], implicitly[TypeCodec[T5]], implicitly[TypeCodec[T6]], implicitly[TypeCodec[T7]], implicitly[TypeCodec[T8]], implicitly[TypeCodec[T9]], implicitly[TypeCodec[T10]], implicitly[TypeCodec[T11]], implicitly[TypeCodec[T12]], implicitly[TypeCodec[T13]], implicitly[TypeCodec[T14]], implicitly[TypeCodec[T15]])

  implicit def tuple16Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec, T4: TypeCodec, T5: TypeCodec, T6: TypeCodec, T7: TypeCodec, T8: TypeCodec, T9: TypeCodec, T10: TypeCodec, T11: TypeCodec, T12: TypeCodec, T13: TypeCodec, T14: TypeCodec, T15: TypeCodec, T16: TypeCodec]: TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16)] =
    TypeCodecs.tuple16Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]], implicitly[TypeCodec[T4]], implicitly[TypeCodec[T5]], implicitly[TypeCodec[T6]], implicitly[TypeCodec[T7]], implicitly[TypeCodec[T8]], implicitly[TypeCodec[T9]], implicitly[TypeCodec[T10]], implicitly[TypeCodec[T11]], implicitly[TypeCodec[T12]], implicitly[TypeCodec[T13]], implicitly[TypeCodec[T14]], implicitly[TypeCodec[T15]], implicitly[TypeCodec[T16]])

  implicit def tuple17Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec, T4: TypeCodec, T5: TypeCodec, T6: TypeCodec, T7: TypeCodec, T8: TypeCodec, T9: TypeCodec, T10: TypeCodec, T11: TypeCodec, T12: TypeCodec, T13: TypeCodec, T14: TypeCodec, T15: TypeCodec, T16: TypeCodec, T17: TypeCodec]: TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17)] =
    TypeCodecs.tuple17Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]], implicitly[TypeCodec[T4]], implicitly[TypeCodec[T5]], implicitly[TypeCodec[T6]], implicitly[TypeCodec[T7]], implicitly[TypeCodec[T8]], implicitly[TypeCodec[T9]], implicitly[TypeCodec[T10]], implicitly[TypeCodec[T11]], implicitly[TypeCodec[T12]], implicitly[TypeCodec[T13]], implicitly[TypeCodec[T14]], implicitly[TypeCodec[T15]], implicitly[TypeCodec[T16]], implicitly[TypeCodec[T17]])

  implicit def tuple18Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec, T4: TypeCodec, T5: TypeCodec, T6: TypeCodec, T7: TypeCodec, T8: TypeCodec, T9: TypeCodec, T10: TypeCodec, T11: TypeCodec, T12: TypeCodec, T13: TypeCodec, T14: TypeCodec, T15: TypeCodec, T16: TypeCodec, T17: TypeCodec, T18: TypeCodec]: TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18)] =
    TypeCodecs.tuple18Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]], implicitly[TypeCodec[T4]], implicitly[TypeCodec[T5]], implicitly[TypeCodec[T6]], implicitly[TypeCodec[T7]], implicitly[TypeCodec[T8]], implicitly[TypeCodec[T9]], implicitly[TypeCodec[T10]], implicitly[TypeCodec[T11]], implicitly[TypeCodec[T12]], implicitly[TypeCodec[T13]], implicitly[TypeCodec[T14]], implicitly[TypeCodec[T15]], implicitly[TypeCodec[T16]], implicitly[TypeCodec[T17]], implicitly[TypeCodec[T18]])

  implicit def tuple19Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec, T4: TypeCodec, T5: TypeCodec, T6: TypeCodec, T7: TypeCodec, T8: TypeCodec, T9: TypeCodec, T10: TypeCodec, T11: TypeCodec, T12: TypeCodec, T13: TypeCodec, T14: TypeCodec, T15: TypeCodec, T16: TypeCodec, T17: TypeCodec, T18: TypeCodec, T19: TypeCodec]: TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19)] =
    TypeCodecs.tuple19Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]], implicitly[TypeCodec[T4]], implicitly[TypeCodec[T5]], implicitly[TypeCodec[T6]], implicitly[TypeCodec[T7]], implicitly[TypeCodec[T8]], implicitly[TypeCodec[T9]], implicitly[TypeCodec[T10]], implicitly[TypeCodec[T11]], implicitly[TypeCodec[T12]], implicitly[TypeCodec[T13]], implicitly[TypeCodec[T14]], implicitly[TypeCodec[T15]], implicitly[TypeCodec[T16]], implicitly[TypeCodec[T17]], implicitly[TypeCodec[T18]], implicitly[TypeCodec[T19]])

  implicit def tuple20Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec, T4: TypeCodec, T5: TypeCodec, T6: TypeCodec, T7: TypeCodec, T8: TypeCodec, T9: TypeCodec, T10: TypeCodec, T11: TypeCodec, T12: TypeCodec, T13: TypeCodec, T14: TypeCodec, T15: TypeCodec, T16: TypeCodec, T17: TypeCodec, T18: TypeCodec, T19: TypeCodec, T20: TypeCodec]: TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20)] =
    TypeCodecs.tuple20Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]], implicitly[TypeCodec[T4]], implicitly[TypeCodec[T5]], implicitly[TypeCodec[T6]], implicitly[TypeCodec[T7]], implicitly[TypeCodec[T8]], implicitly[TypeCodec[T9]], implicitly[TypeCodec[T10]], implicitly[TypeCodec[T11]], implicitly[TypeCodec[T12]], implicitly[TypeCodec[T13]], implicitly[TypeCodec[T14]], implicitly[TypeCodec[T15]], implicitly[TypeCodec[T16]], implicitly[TypeCodec[T17]], implicitly[TypeCodec[T18]], implicitly[TypeCodec[T19]], implicitly[TypeCodec[T20]])

  implicit def tuple21Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec, T4: TypeCodec, T5: TypeCodec, T6: TypeCodec, T7: TypeCodec, T8: TypeCodec, T9: TypeCodec, T10: TypeCodec, T11: TypeCodec, T12: TypeCodec, T13: TypeCodec, T14: TypeCodec, T15: TypeCodec, T16: TypeCodec, T17: TypeCodec, T18: TypeCodec, T19: TypeCodec, T20: TypeCodec, T21: TypeCodec]: TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21)] =
    TypeCodecs.tuple21Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]], implicitly[TypeCodec[T4]], implicitly[TypeCodec[T5]], implicitly[TypeCodec[T6]], implicitly[TypeCodec[T7]], implicitly[TypeCodec[T8]], implicitly[TypeCodec[T9]], implicitly[TypeCodec[T10]], implicitly[TypeCodec[T11]], implicitly[TypeCodec[T12]], implicitly[TypeCodec[T13]], implicitly[TypeCodec[T14]], implicitly[TypeCodec[T15]], implicitly[TypeCodec[T16]], implicitly[TypeCodec[T17]], implicitly[TypeCodec[T18]], implicitly[TypeCodec[T19]], implicitly[TypeCodec[T20]], implicitly[TypeCodec[T21]])

  implicit def tuple22Codec[T1: TypeCodec, T2: TypeCodec, T3: TypeCodec, T4: TypeCodec, T5: TypeCodec, T6: TypeCodec, T7: TypeCodec, T8: TypeCodec, T9: TypeCodec, T10: TypeCodec, T11: TypeCodec, T12: TypeCodec, T13: TypeCodec, T14: TypeCodec, T15: TypeCodec, T16: TypeCodec, T17: TypeCodec, T18: TypeCodec, T19: TypeCodec, T20: TypeCodec, T21: TypeCodec, T22: TypeCodec]: TypeCodec[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22)] =
    TypeCodecs.tuple22Of[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22](implicitly[TypeCodec[T1]], implicitly[TypeCodec[T2]], implicitly[TypeCodec[T3]], implicitly[TypeCodec[T4]], implicitly[TypeCodec[T5]], implicitly[TypeCodec[T6]], implicitly[TypeCodec[T7]], implicitly[TypeCodec[T8]], implicitly[TypeCodec[T9]], implicitly[TypeCodec[T10]], implicitly[TypeCodec[T11]], implicitly[TypeCodec[T12]], implicitly[TypeCodec[T13]], implicitly[TypeCodec[T14]], implicitly[TypeCodec[T15]], implicitly[TypeCodec[T16]], implicitly[TypeCodec[T17]], implicitly[TypeCodec[T18]], implicitly[TypeCodec[T19]], implicitly[TypeCodec[T20]], implicitly[TypeCodec[T21]], implicitly[TypeCodec[T22]])

  // format: on
}
