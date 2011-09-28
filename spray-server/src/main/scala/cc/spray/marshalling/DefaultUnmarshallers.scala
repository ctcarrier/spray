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
package marshalling

import http._
import MediaTypes._
import MediaRanges._
import HttpCharsets._
import xml.{XML, NodeSeq}
import java.nio.ByteBuffer
import java.net.URLDecoder
import utils.FormContent
import java.io.ByteArrayInputStream

trait DefaultUnmarshallers {
  
  implicit object StringUnmarshaller extends UnmarshallerBase[String] {
    val canUnmarshalFrom = ContentTypeRange(`*/*`) :: Nil // we can convert anything to a string

    def unmarshal(content: HttpContent) = {
      val charset = content.contentType.charset.getOrElse(`ISO-8859-1`)
      Right(new String(content.buffer, charset.nioCharset))
    }
  }

  implicit object CharArrayUnmarshaller extends UnmarshallerBase[Array[Char]] {
    val canUnmarshalFrom = ContentTypeRange(`*/*`) :: Nil // we can convert anything to a char array

    def unmarshal(content: HttpContent) = {
      val nioCharset = content.contentType.charset.getOrElse(`ISO-8859-1`).nioCharset
      val byteBuffer = ByteBuffer.wrap(content.buffer)
      val charBuffer = nioCharset.decode(byteBuffer)
      Right(charBuffer.array())
    }
  }
  
  implicit object NodeSeqUnmarshaller extends UnmarshallerBase[NodeSeq] {
    val canUnmarshalFrom = ContentTypeRange(`text/xml`) ::
                           ContentTypeRange(`text/html`) ::
                           ContentTypeRange(`application/xhtml+xml`) :: Nil

    def unmarshal(content: HttpContent) = protect {
      if (content.contentType.charset.isDefined) {
        XML.loadString(StringUnmarshaller.unmarshal(content).right.get)
      } else {
        XML.load(new ByteArrayInputStream(content.buffer))
      }
    }
  }

  implicit object FormContentUnmarshaller extends UnmarshallerBase[FormContent] {
    val canUnmarshalFrom = ContentTypeRange(`application/x-www-form-urlencoded`) :: Nil
  
    def unmarshal(content: HttpContent) = protect {
      FormContent {
        val data = DefaultUnmarshallers.StringUnmarshaller.unmarshal(content).right.get
        val charset = content.contentType.charset.getOrElse(`ISO-8859-1`).aliases.head
        URLDecoder.decode(data, charset).fastSplit('&').map {
          _.fastSplit('=') match {
            case key :: value :: Nil => (key, value)
            case _ => throw new IllegalArgumentException("'" + data + "' is not a valid form content")
          }
        } (collection.breakOut)
      }
    }
  }
  
  implicit def pimpHttpContentWithAs1(c: HttpContent): HttpContentExtractor = new HttpContentExtractor(Some(c)) 
  implicit def pimpHttpContentWithAs2(c: Option[HttpContent]): HttpContentExtractor = new HttpContentExtractor(c)
  
  class HttpContentExtractor(content: Option[HttpContent]) {
    def as[A](implicit unmarshaller: Unmarshaller[A]): Either[Rejection, A] = content match {
      case Some(httpContent) => unmarshaller(httpContent.contentType) match {
        case UnmarshalWith(converter) => converter(httpContent)
        case CantUnmarshal(onlyFrom) => Left(UnsupportedRequestContentTypeRejection(onlyFrom))
      }
      case None => Left(RequestEntityExpectedRejection)
    }
  }
  
}

object DefaultUnmarshallers extends DefaultUnmarshallers