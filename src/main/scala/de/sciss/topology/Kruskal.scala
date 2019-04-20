/*
 *  Kruskal.scala
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

import scala.collection.generic.CanBuildFrom

import scala.collection.{Seq => SSeq}

object Kruskal {
  /** Builds a minimum-spanning-tree using Kruskal's method.
    *
    * @param sorted   edges which '''must be sorted''' by ascending weight
    * @param ord      vertex ordering
    * @param edgeView edge view type class
    * @return         the subset of edges forming the MST
    */
  def apply[V, E, From <: SSeq[E], To](sorted: From)(implicit ord: Ordering[V], edgeView: EdgeView[V, E],
                                                    cbf: CanBuildFrom[From, E, To]): To = {
    val b   = cbf(sorted)
    var uf  = UnionFind[V, E](sorted)
    sorted.foreach { e =>
      import edgeView._
      val start = sourceVertex(e)
      val end   = targetVertex(e)
      if (!uf.isConnected(start, end)) {
        uf = uf.union(start, end)
        b += e
      }
    }
    b.result()
  }
}