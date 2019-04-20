/*
 *  Edge.scala
 *  (Topology)
 *
 *  Copyright (c) 2010-2019 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.topology

import scala.language.implicitConversions

object Edge {
  def apply[V](from: V, to: V): Edge[V] = Impl(from, to)

  implicit def fromTuple[V](tup: (V, V)): Edge[V] = apply(tup._1, tup._2)

  private final case class Impl[V](sourceVertex: V, targetVertex: V) extends Edge[V] {
    override def productPrefix = "Edge"
  }
}
trait Edge[+V] {
  def sourceVertex: V
  def targetVertex: V
}