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

package cc.spray.utils

import akka.actor.{Actor, ActorRef}

object ActorHelpers {

  /**
   * Returns the actor whose id matches the given symbol.
   */
  def actor(id: Symbol): ActorRef = actor(id.name)

  /**
   * Returns the actor with the given id.
   */
  def actor(id: String): ActorRef = {
    val actors = Actor.registry.actorsFor(id)
    assert(actors.length == 1, actors.length + " actors for id '" + id + "' found, expected exactly one")
    actors.head
  }

  /**
   * Returns the actor of the given type. If there are no actors with the given type or more than one an
   * AssertionError will be thrown.
   * Note that this methods has to traverse all live actors in order to find the matching one.
   * If you need to locate an actor instance in performance criticial code resort to finding by actor id instead!
   */
  def actor[A <: Actor :Manifest]: ActorRef = {
    val actors = Actor.registry.actorsFor[A]
    assert(actors.length == 1, actors.length + " actors of type '" + manifest.erasure + "' found, expected exactly one")
    actors.head
  }
  
}