/*
 *  UnionFind.scala
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

import de.sciss.topology.UnionFind.Node

import scala.annotation.tailrec

// this is based on code by Rafael O. Torres, which
// however was wrong.
//
// Original code, licensed under MIT:
// https://github.com/rators/PriorityMap/blob/master/src/main/scala/utils/algorithm/kruskal/UnionFind.scala

object UnionFind {
  final case class Node[V](parent: Option[V], treeSize: Int)

  def apply[V, E](edges: Iterable[E])(implicit ord: Ordering[V], edgeView: EdgeView[V, E]): UnionFind[V] = {
    val indexMap  = vertexIndexMap[V, E](edges)
    val size      = indexMap.size
    val nodes     = Vector.fill(size)(Node[V](None, 1))

    new UnionFind[V](nodes, indexMap)
  }

  def vertexIndexMap[V, E](edges: Iterable[E])(implicit ord: Ordering[V], edgeView: EdgeView[V, E]): Map[V, Int] = {
    val distinct: List[V] = Graph.mkVertexSeq(edges)
    val sorted  : List[V] = distinct.sorted
    sorted.iterator.zipWithIndex.toMap
  }
}

/**
  * Purely functional union find implementation.
  */
final class UnionFind[V](nodes: Vector[Node[V]], indexMap: Map[V, Int]) {

  def union(from: V, to: V): UnionFind[V] = {
    // if `from` == `to` then `from` and `to` are in the same set then
    // do nothing an return this set. Connected returns true
    // before utilizing the data structure to check connectivity.
    if (from == to) return this

    val rootA = root(from)
    val rootB = root(to)

    // If `from` and `to` are already in the same set do nothing and return
    // this instance of union-find.
    if (rootA == rootB) return this

    val idxA  = indexMap(rootA)
    val idxB  = indexMap(rootB)
    val nodeA = nodes(idxA)
    val nodeB = nodes(idxB)

    val newTreeSize = nodeA.treeSize + nodeB.treeSize

    // Append the smaller set (sS) to the larger set (lS) by making sS representative
    // the representative of the lS. Update the tree size of the sets.
    val (newNodeA, newNodeB) = if (nodeA.treeSize < nodeB.treeSize) {
      val newNodeA = Node[V](Some(rootB) , newTreeSize)
      val newNodeB = nodeB.copy(treeSize = newTreeSize)
      (newNodeA, newNodeB)
    } else {
      val newNodeA = nodeA.copy(treeSize = newTreeSize)
      val newNodeB = Node[V](Some(rootA) , newTreeSize)
      (newNodeA, newNodeB)
    }

    val newNodes = nodes
      .updated(idxA, newNodeA)
      .updated(idxB, newNodeB)

    new UnionFind[V](newNodes, indexMap)
  }

  def isConnected(from: V, to: V): Boolean =
    from == to || root(from) == root(to)

  @tailrec
  private def root(vertex: V): V = {
    val idx = indexMap(vertex)
    nodes(idx).parent match {
      case None         => vertex
      case Some(parent) => root(parent)
    }
  }

  /** For hypothesis testing */
  def pathToRoot(vertex: V): List[V] = {
    @tailrec
    def loop(child: V, res: List[V]): List[V] = {
      val res1  = child :: res
      val idx   = indexMap(child)
      nodes(idx).parent match {
        case None         => res1
        case Some(parent) => loop(parent, res1)
      }
    }

    loop(vertex, Nil)
  }
}