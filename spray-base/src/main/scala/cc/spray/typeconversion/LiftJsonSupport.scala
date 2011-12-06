package cc.spray.typeconversion

import cc.spray._
import http._
import MediaTypes._

import net.liftweb.json.Serialization._
import net.liftweb.json._

/**
 * @author chris_carrier
 * @version 10/18/11
 */


trait LiftJsonSupport {
   implicit val formats: Formats

   implicit def liftJsonUnmarshaller[A <: Product :Manifest] = new UnmarshallerBase[A] {
     val canUnmarshalFrom = ContentTypeRange(`application/json`) :: Nil
     def unmarshal(content: HttpContent) = protect {
       try {
             val jsonSource = DefaultUnmarshallers.StringUnmarshaller(content).right.get
             parse(jsonSource).extract[A]
       } catch {
         case e => {
           throw e
         }
       }
     }
   }

   implicit def liftJsonMarshaller[A <: AnyRef] = new MarshallerBase[A] {
     val canMarshalTo = ContentType(`application/json`) :: Nil
     def marshal(value: A, contentType: ContentType) = {
             val jsonSource = write(value)
             DefaultMarshallers.StringMarshaller.marshal(jsonSource, contentType)
     }
   }
}