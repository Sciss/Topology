/*
 *  Graph.scala
 *  (Topology)
 *
 *  Copyright (c) 2010-2020 Hanns Holger Rutz. All rights reserved.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.topology

import scala.annotation.tailrec
import scala.collection.SeqLike
import scala.collection.generic.CanBuildFrom

/** This object contains utility methods for constructing and querying graphs. */
object Graph {
  type EdgeMap[V, E] = Map[V, Set[E]]

  /** Collects as a `Set` all vertices references through a given collection of edges. */
  def mkVertexSet[V, E](edges: Iterable[E])(implicit edgeView: EdgeView[V, E]): Set[V] = {
    import edgeView._
    edges.iterator.flatMap(e => sourceVertex(e) :: targetVertex(e) :: Nil).toSet
  }

  /** Collects as a all vertices references through a given collection of edges.
    * The result is distinct, i.e. each vertex appears only once.
    */
  def mkVertexSeq[V, E, To <: SeqLike[V, To]](edges: Iterable[E])(implicit edgeView: EdgeView[V, E],
                                                                  cbf: CanBuildFrom[Nothing, V, To]): To = {
    import edgeView._
    val flat = edges.iterator.flatMap(e => sourceVertex(e) :: targetVertex(e) :: Nil)
    val b = cbf()
    flat.foreach(b += _)
    b.result().distinct
  }

  /** Creates a map from vertices to outgoing (target) edges,
    * including only edges for which a vertex is source.
    *
    * Note that the result will contain all vertices
    * as keys; if a vertex is a sink only, its value
    * will be the empty set.
    */
  def mkTargetEdgeMap[V, E](edges: Iterable[E])(implicit edgeView: EdgeView[V, E]): EdgeMap[V, E] =
    mkEdgeMap[V, E](edges, sources = false, targets = true)

  /** Creates a map from vertices to incoming (source) edges,
    * including only edges for which a vertex is sink.
    *
    * Note that the result will contain all vertices
    * as keys; if a vertex is a source only, its value
    * will be the empty set.
    */
  def mkSourceEdgeMap[V, E](edges: Iterable[E])(implicit edgeView: EdgeView[V, E]): EdgeMap[V, E] =
    mkEdgeMap[V, E](edges, sources = true, targets = false)

  /** Creates a map from vertices to adjacent edges,
    * including both edges for which a vertex is source
    * and for which a vertex is sink.
    */
  def mkBiEdgeMap[V, E](edges: Iterable[E])(implicit edgeView: EdgeView[V, E]): EdgeMap[V, E] =
    mkEdgeMap[V, E](edges, sources = true, targets = true)

  private def mkEdgeMap[V, E](edges: Iterable[E], sources: Boolean, targets: Boolean)
                             (implicit edgeView: EdgeView[V, E]): EdgeMap[V, E] = {
    import edgeView._

    var res = Map.empty[V, Set[E]]
    edges.foreach { e =>
      val start = sourceVertex(e)
      val end   = targetVertex(e)
      val oldS  = res.getOrElse(start, Set.empty)
      val oldE  = res.getOrElse(end  , Set.empty)
      val newS  = if (targets) oldS + e else oldS
      val newE  = if (sources) oldE + e else oldE
      res = res + (start -> newS) + (end -> newE)
    }
    res
  }

  /** Calculates minimum-spanning-tree, by simply calling `Kruskal.apply`. */
  def mst[V, E, From <: Seq[E], To](sorted: From)(implicit ord: Ordering[V], edgeView: EdgeView[V, E],
                                                  cbf: CanBuildFrom[From, E, To]): To =
    Kruskal[V, E, From, To](sorted)

  /** Uses depth-first-search in a directed graph to construct a path from `source` to `sink`.
    * Requires that the graph has no cycles.
    * Returns and empty sequence if no path is found.
    *
    * @param  source  the vertex to start with; if a path is found, this will be its first element
    * @param  sink    the vertex to end with; if a path is found, this will be its last element
    * @param  map     an map from vertices to the set of their outgoing (target) edges. It is possible to pass in
    *                 a "full" bi-directional edge-map, including in the set also incoming (source) edges. These
    *                 will be automatically ignored.
    */
  def findDirectedPath[V, E, To](source: V, sink: V, map: Map[V, Set[E]])(implicit edgeView: EdgeView[V, E],
                                                                          cbf: CanBuildFrom[Nothing, V, To]): To =
    findPath[V, E, To](source, sink, map, directed = true)

  def findUndirectedPath[V, E, To](source: V, sink: V, map: Map[V, Set[E]])(implicit edgeView: EdgeView[V, E],
                                                                            cbf: CanBuildFrom[Nothing, V, To]): To =
    findPath[V, E, To](source, sink, map, directed = false)

  /** Uses depth-first-search to construct a path from `source` to `sink`.
    * Returns and empty sequence if no path is found.
    *
    * @param  source    the vertex to start with; if a path is found, this will be its first element
    * @param  sink      the vertex to end with; if a path is found, this will be its last element
    * @param  map       an map from vertices to the set of their outgoing (target) and incoming (source) edges.
    *                   If `directed` is `true`, it suffices to provide only the set of target edges. If
    *                   `directed` is `true` and a "full" bi-directional edge-map is used, this is automatically
    *                   detected.
    * @param  directed  if `true`, consider the graph directed, and only follow the edges from sources to
    *                   targets. If `false`, allow movement in both directions.
    *                   Note that if the graph is not directed, the returned path is not guaranteed to be unique or
    *                   the shortest possible path.
    */
  def findPath[V, E, To](source: V, sink: V, map: Map[V, Set[E]], directed: Boolean)
                        (implicit edgeView: EdgeView[V, E], cbf: CanBuildFrom[Nothing, V, To]): To = {
    import edgeView._
    val b = cbf()

    @tailrec
    def loop(back: List[(V, Set[E])], seen: Set[V], rem: Map[V, Set[E]]): Unit =
      back match {
        case (v, edges) :: tail =>
          if (v == sink) {
            val it = back.reverseIterator.map(_._1)
            it.foreach(b += _)

          } else {
            var backNew = tail
            var remNew  = rem
            var seenNew = seen - v
            var vTail   = edges
            var done    = false
            while (vTail.nonEmpty && !done) {
              val e     = vTail.head
              vTail    -= e
              val start = sourceVertex(e)
              val end   = targetVertex(e)
              val isStart = v == start
              if (isStart || !directed) {
                val target = if (isStart) end else start
                if (!seen.contains(target)) {
                  val tTail = rem(target) - e
                  remNew    = if (tTail.isEmpty) rem - target else rem + (target -> tTail)
                  backNew   = (target -> tTail) :: (v -> vTail) :: tail
                  seenNew   = seen + target   // i.e. seenNew + v + target
                  done      = true
                }
              }
            }
            loop(backNew, seenNew, remNew)
          }

        case _ =>
      }

    map.get(source) match {
      case Some(sEdges) => loop((source -> sEdges) :: Nil, Set.empty + source, map)
      case None         =>
    }
    b.result()
  }
}
