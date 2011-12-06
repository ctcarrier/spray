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

package cc

import spray.http._

package object spray {

  type Route = RequestContext => Unit
  type ContentTypeResolver = (String, Option[HttpCharset]) => ContentType
  type RouteFilter[T <: Product] = RequestContext => FilterResult[T]
  type GeneralAuthenticator[U] = RequestContext => Either[Rejection, U]
  type UserPassAuthenticator[U] = Option[(String, String)] => Option[U]
  type CacheKeyer = RequestContext => Option[Any]
  type RequiredParameterMatcher = Map[String, String] => Boolean
  type RejectionHandler = PartialFunction[List[Rejection], HttpResponse]

}