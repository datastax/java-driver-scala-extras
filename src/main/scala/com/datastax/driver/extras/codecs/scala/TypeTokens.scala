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

package com.datastax.driver.extras.codecs.scala

import com.google.common.reflect.{TypeParameter, TypeToken}

import scala.collection.immutable

/**
  * Utility methods to create and manipulate TypeToken instances.
  */
object TypeTokens {

  // TypeTokens for AnyVal types (primitive types) need to be wrapped

  val boolean: TypeToken[Boolean] = TypeToken.of(classOf[Boolean]).wrap()
  val byte: TypeToken[Byte] = TypeToken.of(classOf[Byte]).wrap()
  val short: TypeToken[Short] = TypeToken.of(classOf[Short]).wrap()
  val int: TypeToken[Int] = TypeToken.of(classOf[Int]).wrap()
  val long: TypeToken[Long] = TypeToken.of(classOf[Long]).wrap()
  val float: TypeToken[Float] = TypeToken.of(classOf[Float]).wrap()
  val double: TypeToken[Double] = TypeToken.of(classOf[Double]).wrap()

  def seqOf[T](eltType: Class[T]): TypeToken[immutable.Seq[T]] = {
    new TypeToken[immutable.Seq[T]]() {}.where(new TypeParameter[T]() {}, eltType)
  }

  def seqOf[T](eltType: TypeToken[T]): TypeToken[immutable.Seq[T]] = {
    new TypeToken[immutable.Seq[T]]() {}.where(new TypeParameter[T]() {}, eltType)
  }

  def listOf[T](eltType: Class[T]): TypeToken[immutable.List[T]] = {
    new TypeToken[immutable.List[T]]() {}.where(new TypeParameter[T]() {}, eltType)
  }

  def listOf[T](eltType: TypeToken[T]): TypeToken[immutable.List[T]] = {
    new TypeToken[immutable.List[T]]() {}.where(new TypeParameter[T]() {}, eltType)
  }

  def vectorOf[T](eltType: Class[T]): TypeToken[immutable.Vector[T]] = {
    new TypeToken[immutable.Vector[T]]() {}.where(new TypeParameter[T]() {}, eltType)
  }

  def vectorOf[T](eltType: TypeToken[T]): TypeToken[immutable.Vector[T]] = {
    new TypeToken[immutable.Vector[T]]() {}.where(new TypeParameter[T]() {}, eltType)
  }

  def setOf[T](eltType: Class[T]): TypeToken[immutable.Set[T]] = {
    new TypeToken[immutable.Set[T]]() {}.where(new TypeParameter[T]() {}, eltType)
  }

  def setOf[T](eltType: TypeToken[T]): TypeToken[immutable.Set[T]] = {
    new TypeToken[immutable.Set[T]]() {}.where(new TypeParameter[T]() {}, eltType)
  }

  def sortedSetOf[T](eltType: Class[T]): TypeToken[immutable.SortedSet[T]] = {
    new TypeToken[immutable.SortedSet[T]]() {}.where(new TypeParameter[T]() {}, eltType)
  }

  def sortedSetOf[T](eltType: TypeToken[T]): TypeToken[immutable.SortedSet[T]] = {
    new TypeToken[immutable.SortedSet[T]]() {}.where(new TypeParameter[T]() {}, eltType)
  }

  def treeSetOf[T](eltType: Class[T]): TypeToken[immutable.TreeSet[T]] = {
    new TypeToken[immutable.TreeSet[T]]() {}.where(new TypeParameter[T]() {}, eltType)
  }

  def treeSetOf[T](eltType: TypeToken[T]): TypeToken[immutable.TreeSet[T]] = {
    new TypeToken[immutable.TreeSet[T]]() {}.where(new TypeParameter[T]() {}, eltType)
  }

  def mapOf[K, V](keyType: Class[K], valueType: Class[V]): TypeToken[immutable.Map[K, V]] = {
    new TypeToken[immutable.Map[K, V]]() {}
      .where(new TypeParameter[K]() {}, keyType)
      .where(new TypeParameter[V]() {}, valueType)
  }

  def mapOf[K, V](keyType: TypeToken[K], valueType: TypeToken[V]): TypeToken[immutable.Map[K, V]] = {
    new TypeToken[immutable.Map[K, V]]() {}
      .where(new TypeParameter[K]() {}, keyType)
      .where(new TypeParameter[V]() {}, valueType)
  }

  def sortedMapOf[K, V](keyType: Class[K], valueType: Class[V]): TypeToken[immutable.SortedMap[K, V]] = {
    new TypeToken[immutable.SortedMap[K, V]]() {}
      .where(new TypeParameter[K]() {}, keyType)
      .where(new TypeParameter[V]() {}, valueType)
  }

  def sortedMapOf[K, V](keyType: TypeToken[K], valueType: TypeToken[V]): TypeToken[immutable.SortedMap[K, V]] = {
    new TypeToken[immutable.SortedMap[K, V]]() {}
      .where(new TypeParameter[K]() {}, keyType)
      .where(new TypeParameter[V]() {}, valueType)
  }

  def treeMapOf[K, V](keyType: Class[K], valueType: Class[V]): TypeToken[immutable.TreeMap[K, V]] = {
    new TypeToken[immutable.TreeMap[K, V]]() {}
      .where(new TypeParameter[K]() {}, keyType)
      .where(new TypeParameter[V]() {}, valueType)
  }

  def treeMapOf[K, V](keyType: TypeToken[K], valueType: TypeToken[V]): TypeToken[immutable.TreeMap[K, V]] = {
    new TypeToken[immutable.TreeMap[K, V]]() {}
      .where(new TypeParameter[K]() {}, keyType)
      .where(new TypeParameter[V]() {}, valueType)
  }

  def optionOf[T](eltType: Class[T]): TypeToken[Option[T]] = {
    new TypeToken[Option[T]]() {}.where(new TypeParameter[T]() {}, eltType)
  }

  def optionOf[T](eltType: TypeToken[T]): TypeToken[Option[T]] = {
    new TypeToken[Option[T]]() {}.where(new TypeParameter[T]() {}, eltType)
  }

  def tuple2Of[T1, T2](type1: Class[T1], type2: Class[T2]): TypeToken[(T1, T2)] = {
    new TypeToken[(T1, T2)]() {}
      .where(new TypeParameter[T1]() {}, type1)
      .where(new TypeParameter[T2]() {}, type2)
  }

  def tuple2Of[T1, T2](type1: TypeToken[T1], type2: TypeToken[T2]): TypeToken[(T1, T2)] = {
    new TypeToken[(T1, T2)]() {}
      .where(new TypeParameter[T1]() {}, type1)
      .where(new TypeParameter[T2]() {}, type2)
  }

  def tuple3Of[T1, T2, T3](type1: Class[T1], type2: Class[T2], type3: Class[T3]): TypeToken[(T1, T2, T3)] = {
    new TypeToken[(T1, T2, T3)]() {}
      .where(new TypeParameter[T1]() {}, type1)
      .where(new TypeParameter[T2]() {}, type2)
      .where(new TypeParameter[T3]() {}, type3)
  }

  def tuple3Of[T1, T2, T3](type1: TypeToken[T1], type2: TypeToken[T2], type3: TypeToken[T3]): TypeToken[(T1, T2, T3)] = {
    new TypeToken[(T1, T2, T3)]() {}
      .where(new TypeParameter[T1]() {}, type1)
      .where(new TypeParameter[T2]() {}, type2)
      .where(new TypeParameter[T3]() {}, type3)
  }

}
