package cc.spray.typeconversion

import org.specs2.Specification
import org.specs2.specification._
import org.specs2.matcher.ThrownExpectations
import cc.spray.http._
import MediaTypes._
import net.liftweb.json.DefaultFormats

/**
 * @author chris_carrier
 * @version 9/26/11
 */

class LiftJsonSupportSpec extends Specification with LiftJsonSupport {

  implicit val formats = DefaultFormats

  case class Employee(fname: String, name: String, age: Int, id: Long, boardMember: Boolean)

  val employeeA = Employee("Frank", "Smith", 42, 12345, false)
  val employeeAJson = """{
    "fname":"Frank",
    "name":"Smith",
    "age":42,
    "id":12345,
    "boardMember": false
  }"""

  def is = args() ^
    "The LiftJsonSupport should" ^
    "provide unmarshalling support" ! unmarshallTest() ^
    "and return 404 for a non-existent resource" ! marshallTest() ^
    end

    def unmarshallTest() = {
      HttpContent(`application/json`, employeeAJson).as[Employee] must be equalTo
              Right(Employee("Frank", "Smith", 42, 12345, false))
    }

  def marshallTest() = {
      employeeA.toHttpContent mustEqual HttpContent(ContentType(`application/json`), employeeAJson)
    }

}