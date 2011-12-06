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
package caching

import utils._
import akka.util.duration._
import akka.util.Duration
import collection.mutable.LinkedHashMap
import akka.dispatch.{AlreadyCompletedFuture, Future}

object LruCache {
  /**
   * Creates a new LruCache instance
   */
  def apply[V](maxEntries: Int = 500, dropFraction: Double = 0.20, ttl: Duration = 5.minutes) = {
    new LruCache[V](maxEntries, dropFraction, ttl)
  }
}

/**
 * A last-recently-used cache with a defined capacity and time-to-live.
 * The {{dropFraction}} parameter is used for evicting items from the cache when the maximum capacity is reached.
 * E.g. with a {{maxEntries}} value of 100 and a {{dropFraction}} of 0.20 the cache will evict the oldest 20 cache
 * entries when the 101st entry is about to be stored.
 */
class LruCache[V](val maxEntries: Int, val dropFraction: Double, val ttl: Duration) extends Cache[V] { cache =>
  require(0.0 < dropFraction && dropFraction < 1.0, "dropFraction must be > 0 and < 1")

  class Entry(val future: Future[V]) {
    private var lastUsed = System.currentTimeMillis
    def refresh() { lastUsed = System.currentTimeMillis }
    def isAlive = (System.currentTimeMillis - lastUsed).millis < ttl // note that infinite Durations do not support .toMillis
    override def toString = future.value match {
      case Some(Right(value)) => value.toString
      case Some(Left(exception)) => exception.toString
      case None => "pending"
    }
  }

  protected[caching] val store = new Store

  def get(key: Any) = synchronized {
    store.getEntry(key).map(_.future)
  }

  def fromFuture(key: Any)(future: => Future[V]): Future[V] = synchronized {
    store.getEntry(key) match {
      case Some(entry) => entry.future
      case None => make(future) { future =>
        store.setEntry(key, new Entry(future))
        if (!future.isInstanceOf[AlreadyCompletedFuture[_]]) {
          future.onComplete { fut =>
            cache.synchronized {
              fut.value.get match {
                case Right(value) => store.setEntry(key, new Entry(new AlreadyCompletedFuture(Right(value))))
                case _ => store.remove(key) // in case of exceptions we remove the cache entry (i.e. try again later)
              }
            }
          }
        }
      }
    }
  }

  protected class Store extends LinkedHashMap[Any, Entry] {
    def getEntry(key: Any): Option[cache.Entry] = get(key).flatMap { entry =>
      if (entry.isAlive) {
        entry.refresh()
        remove(key)       // TODO: replace with optimized "refresh" implementation
        put(key, entry)
        Some(entry)
      } else {
        // entry expired, so remove this one and all earlier ones (they have expired as well)
        while (firstEntry.key != key) remove(firstEntry.key)
        remove(key)
        None
      }
    }

    def setEntry(key: Any, entry: cache.Entry) {
      put(key, entry)
      if (size > maxEntries) {
        // remove the earliest entries
        val newSize = maxEntries - (maxEntries * dropFraction).toInt
        while (size > newSize) remove(firstEntry.key)
      }
    }
  }
}