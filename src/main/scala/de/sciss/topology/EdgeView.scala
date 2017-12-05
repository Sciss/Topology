/*
 *  EdgeView.scala
 *  (Topology)
 *
 *  Copyright (c) 2010-2017 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.topology

object EdgeView {
  /** Any type that extends `Edge` can be viewed as edge. */
  implicit def direct[V, E <: Edge[V]]: EdgeView[V, E] = DirectImpl.asInstanceOf[EdgeView[V, Edge[V]]]

  private object DirectImpl extends EdgeView[Any, Edge[Any]] {
    def sourceVertex(e: Edge[Any]): Any = e.sourceVertex
    def targetVertex(e: Edge[Any]): Any = e.targetVertex
  }
}
/** A type class or view to obtain vertices from (directed) edges. */
trait EdgeView[V, -E] {
  def sourceVertex(e: E): V
  def targetVertex(e: E): V
}