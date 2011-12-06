/*
 * Copyright (C) 2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.spray
package typeconversion

trait Deserializer[A, B] extends (A => Either[DeserializationError, B])

object Deserializer extends DeserializerLowerPriorityImplicits with CaseClassDeserializers {
  implicit def fromFunction2Converter[A, B](implicit f: A => B) = {
    new Deserializer[A, B] {
      def apply(a: A) = {
        try {
          Right(f(a))
        } catch {
          case ex => Left(MalformedContent(ex.toString))
        }
      }
    }
  }

  implicit def liftToTargetOption[A, B](implicit converter: Deserializer[A, B]) = {
    new Deserializer[A, Option[B]] {
      def apply(value: A) = converter(value) match {
        case Right(a) => Right(Some(a))
        case Left(ContentExpected) => Right(None)
        case Left(error) => Left(error)
      }
    }
  }
}

private[typeconversion] abstract class DeserializerLowerPriorityImplicits {
  implicit def lift2SourceOption[A, B](converter: Deserializer[A, B]) = liftToSourceOption(converter)
  implicit def liftToSourceOption[A, B](implicit converter: Deserializer[A, B]) = {
    new Deserializer[Option[A], B] {
      def apply(value: Option[A]) = value match {
        case Some(a) => converter(a)
        case None => Left(ContentExpected)
      }
    }
  }
}

sealed trait DeserializationError
case object ContentExpected extends DeserializationError
case class MalformedContent(errorMessage: String) extends DeserializationError
case class UnsupportedContentType(errorMessage: String) extends DeserializationError