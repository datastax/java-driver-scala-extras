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

import com.datastax.driver.core._

class SeqCodec[E](eltCodec: TypeCodec[E])
  extends AbstractSeqCodec[E, Seq[E]](
    DataType.list(eltCodec.getCqlType),
    TypeTokens.seqOf(eltCodec.getJavaType),
    eltCodec) {
}

object SeqCodec {

    def apply[E](eltCodec: TypeCodec[E]): SeqCodec[E] = new SeqCodec[E](eltCodec)

    import scala.reflect.runtime.universe._

    def apply[E](implicit eltTag: TypeTag[E]): SeqCodec[E] = {
        val eltCodec = TypeConversions.toCodec[E](eltTag.tpe)
        apply(eltCodec)
    }

}
