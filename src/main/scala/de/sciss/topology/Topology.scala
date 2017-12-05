/*
 *  Topology.scala
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

import scala.Vector.{empty => emptySeq}
import scala.collection.immutable.{IndexedSeq => Vec}
import scala.collection.mutable.{Set => MSet, Stack => MStack}
import scala.util.{Failure, Success, Try}

object Topology {
  /** Creates an empty topology with no vertices or edges.
    *
    * @tparam V   vertex type
    * @tparam E   edge type
    */
  def empty[V, E](implicit edgeView: EdgeView[V, E]): Topology[V, E] = apply[V, E](emptySeq, Set.empty)(0, Map.empty)

  sealed trait Move[V] {
    def reference: V
    def affected : Vec[V]
    def isAfter  : Boolean
    def isBefore : Boolean
  }
  final case class MoveAfter [V](reference: V, affected: Vec[V]) extends Move[V] {
    def isAfter = true ; def isBefore = false
  }
  final case class MoveBefore[V](reference: V, affected: Vec[V]) extends Move[V] {
    def isAfter = false; def isBefore = true
  }

  final case class CycleDetected() extends RuntimeException
}

/** An online topological order maintenance structure. This is an immutable data structure with
  * amortized costs. The edge adding operation returns a new copy of the modified structure along
  * with a list of vertices which have been moved due to the insertion. The caller can then use
  * that list to adjust any views (e.g. DSP processes).
  *
  * @param  vertices      the vertices in the structure
  * @param  edges         a set of edges between the vertices
  * @param  unconnected   the number of unconnected vertices (the leading elements in `vertices`)
  * @param  edgeMap       allows lookup of edges via source vertex keys
  *
  * @tparam V             vertex type
  * @tparam E             edge type
  */
final case class Topology[V, E] private (vertices: Vec[V], edges: Set[E])
                                        (val unconnected: Int, val edgeMap: Map[V, Set[E]])
                                        (implicit edgeView: EdgeView[V, E])
  extends Ordering[V] {

  import de.sciss.topology.Topology.{CycleDetected, Move, MoveAfter, MoveBefore}
  import edgeView._

  private type T = Topology[V, E]

  override def toString = s"Topology($vertices, $edges)($unconnected, $edgeMap)"

  /** For two connected vertices `a` and `b`, returns `-1` if `a` is before `b`, or `1` if `a` follows `b`,
    *  or `0` if both are equal. Throws an exception if `a` or `b` is unconnected.
    */
  def compare(a: V, b: V): Int = {
    val ai = vertices.indexOf(a)
    val bi = vertices.indexOf(b)
    require(ai >= unconnected && bi >= unconnected)
    if (ai < bi) -1 else if (ai > bi) 1 else 0
  }

  /** Tries to insert an edge into the topological order.
    * Throws an exception if the source or target vertex of the edge is not contained in the vertex list of this
    * structure.
    *
    * @param e  the edge to insert
    * @return   `Failure` if the edge would violate acyclicity, otherwise `Success` of a tuple
    *           that contains the new topology and possibly affected vertices which need to
    *           be moved with respect to the reference to reflect the new ordering. In case
    *           that the reference is the source vertex of the added edge, the affected vertices
    *           should be moved _after_ the reference and keep their internal grouping order.
    *           In case the reference is the target vertex, the affected vertices should be
    *           moved _before_ the reference
    */
  def addEdge(e: E): Try[(T, Option[Move[V]])] = {
    val source	   = sourceVertex(e)
    val target	   = targetVertex(e)
    val upBound	   = vertices.indexOf(source)
    if (upBound < 0) return Failure(new IllegalArgumentException(s"Source vertex $source not found"))
    val loBound	   = vertices.indexOf(target)
    if (loBound < 0) return Failure(new IllegalArgumentException(s"Target vertex $target not found"))
    val newEdgeMap: Map[V, Set[E]] = edgeMap + (source -> (edgeMap.getOrElse(source, Set.empty) + e))
    val newEdgeSet = edges + e

    if (loBound == upBound) Failure(new CycleDetected)
    // dealing with unconnected elements
    else if (upBound < unconnected) { // first edge for source
      if (loBound < unconnected) { // first edge for target
        val min         = math.min(upBound, loBound)
        val max         = math.max(upBound, loBound)
        val newUnCon    = unconnected - 2
        val newVertices = vertices
          .patch(min     , emptySeq, 1)
          .patch(max - 1 , emptySeq, 1)
          .patch(newUnCon, Vector(source, target), 0)
        Success((copy(newVertices, newEdgeSet)(newUnCon, newEdgeMap), Some(MoveAfter(source, Vector(target)))))
      } else {
        val newUnCon    = unconnected - 1
        val sourceSeq   = Vector(source)
        val newVertices = vertices
          .patch(upBound    , emptySeq , 1)
          .patch(loBound - 1, sourceSeq, 0)
        Success((copy(newVertices, newEdgeSet)(newUnCon, newEdgeMap), Some(MoveBefore(target, sourceSeq))))
      }

      // regular algorithm
    } else if (loBound > upBound) {
      Success((copy(vertices, newEdgeSet)(unconnected, newEdgeMap), None))
    } else /* if (loBound < upBound) */ {
      val visited = MSet.empty[V]
      if (!discovery(visited, newEdgeMap, target, upBound)) {
        Failure(new CycleDetected)  // Cycle --> Abort
      } else {
        val (newVertices, affected) = shift(visited, loBound, upBound)
        val newUnCon                = if (loBound < unconnected) unconnected - 1 else unconnected
        Success((copy(newVertices, newEdgeSet)(newUnCon, newEdgeMap), Some(MoveAfter(source, affected))))
      }
    }
  }

  /** Tests if an edge can be added without producing a cycle.
    *
    * @param e  the edge to test
    * @return   `true` if the insertion is possible. Then calling `addEdge` is guaranteed to be a `Success`.
    *           `false` if the insertion would introduce a cycle. Then calling `addEdge` is guaranteed to be a
    *           `Failure`
    */
  def canAddEdge(e: E): Boolean = {
    val source  = sourceVertex(e)
    val target  = targetVertex(e)
    val upBound = vertices.indexOf(source)
    val loBound = vertices.indexOf(target)

    (upBound >= 0 && loBound >= 0) && (upBound != loBound) && (
      (upBound < unconnected) || (loBound > upBound) || {
        val visited = MSet.empty[V]
        val newEdgeMap: Map[V, Set[E]] = edgeMap + (source -> (edgeMap.getOrElse(source, Set.empty) + e))
        discovery(visited, newEdgeMap, target, upBound)
      }
    )
  }

  /** Removes the edge from the topology. If the edge is not contained in the
    * structure, returns the topology unmodified.
    */
  def removeEdge(e: E): Topology[V, E] = {
    if (edges.contains(e)) {
      val source  = sourceVertex(e)
      val newEMV  = edgeMap(source) - e
      val newEM   = if (newEMV.isEmpty) edgeMap - source else edgeMap + (source -> newEMV)
      copy(edges = edges - e)(unconnected, newEM)
    } else this
  }

  /** Adds a new vertex to the set of unconnected vertices. Throws an exception
    * if the vertex had been added before.
    */
  def addVertex(v: V): Topology[V, E] = {
    if (vertices.contains(v)) throw new IllegalArgumentException(s"Vertex $v was already added")
    copy(v +: vertices)(unconnected + 1, edgeMap)
  }

  /** Removes a vertex and all associated '''outgoing''' edges. If the vertex is not
    * contained in the structure, returns the unmodified topology.
    *
    * '''Note:''' incoming edges pointing to the removed vertex are not detected and removed.
    * this is the responsibility of the caller.
    */
  def removeVertex(v: V): Topology[V, E] = {
    val idx = vertices.indexOf(v)
    if (idx >= 0) {
      val newV = vertices.patch(idx, emptySeq, 1)
      if (idx < unconnected) {
        val newUnCon = unconnected - 1
        copy(newV)(newUnCon, edgeMap)
      } else {
        if (edgeMap.contains(v)) {
          val e     = edgeMap(v)
          val newEM = edgeMap - v
          val newE  = edges -- e
          copy(newV, newE)(unconnected, newEM)
        } else {
          copy(newV)(unconnected, edgeMap)
        }
      }
    } else this
  }

  // note: assumes audio rate
  private def discovery(visited: MSet[V], newEdgeMap: Map[V, Set[E]], v: V, upBound: Int): Boolean = {
    val targets = MStack(v)
    while (targets.nonEmpty) {
      val v           = targets.pop()
      visited        += v
      val moreTargets = newEdgeMap.getOrElse(v, Set.empty).map(targetVertex)
      val grouped     = moreTargets.groupBy { t =>
        val vIdx = vertices.indexOf(t)
        if (vIdx < upBound) -1 else if (vIdx > upBound) 1 else 0
      }
      if (grouped.contains(0)) return false // cycle detected
      // visit s if it was not not already visited
      // and if it is in affected region
      //         grouped.get( -1 ).foreach( targets.pushAll( _.diff( visited )))
      targets.pushAll(grouped.getOrElse(-1, Set.empty).filter(!visited.contains(_)))
    }
    true
  }

  // initial cond: loBound (target) < upBound (source)
  private def shift(visited: collection.Set[V], loBound: Int, upBound: Int): (Vec[V], Vec[V]) = {
    // shift vertices in affected region down ord
    val (a, b)                  = vertices.splitAt(upBound)
    val (begin, target)         = a.splitAt(loBound)
    val source                  = b.head
    val end                     = b.tail
    val (affected, unaffected)  = target.partition(visited.contains)

    val shifted = begin ++ unaffected ++ (source +: affected) ++ end

    (shifted, affected)
  }
}