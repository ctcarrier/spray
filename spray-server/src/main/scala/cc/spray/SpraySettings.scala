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

import akka.config.Config._

object SpraySettings {
  lazy val RootActorId         = config.getString("spray.root-actor-id", "spray-root-service")
  lazy val TimeoutActorId      = config.getString("spray.timeout-actor-id", "spray-timeout-actor")
  lazy val AsyncTimeout        = config.getInt("spray.timeout", 1000)
  lazy val RootPath            = config.getString("spray.root-path")
  lazy val CompactJsonPrinting = config.getBool("spray.compact-json-printing", false)
}