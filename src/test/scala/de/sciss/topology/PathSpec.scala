package de.sciss.topology

import de.sciss.topology.Graph.EdgeMap
import org.scalatest.FlatSpec

class PathSpec extends FlatSpec {
  type V = Int
  type E = Edge[V]

  val edges: Seq[E] = Seq(
     3 ->  8,
     3 -> 10,
     5 -> 11,
     7 -> 11,
     7 ->  8,
     8 ->  9,
    11 ->  2,
    11 ->  9,
    11 -> 10,
  )

  val mapTgt: EdgeMap[V, E] = Graph.mkTargetEdgeMap[V, E](edges)
  val mapSrc: EdgeMap[V, E] = Graph.mkSourceEdgeMap[V, E](edges)
  val mapBi : EdgeMap[V, E] = Graph.mkBiEdgeMap    [V, E](edges)

  "A DAG" should "produce the correct edge-maps" in {
    val expectedTgt: EdgeMap[V, E] = Map(
       2 -> Set[E](),
       3 -> Set[E](3->8, 3->10),
       5 -> Set[E](5->11),
       7 -> Set[E](7->8, 7->11),
       8 -> Set[E](8->9),
       9 -> Set[E](),
      10 -> Set[E](),
      11 -> Set[E](11->2, 11->9, 11->10),
    )
    assert(mapTgt === expectedTgt)

    val expectedSrc: EdgeMap[V, E] = Map(
       2 -> Set[E](11->2),
       3 -> Set[E](),
       5 -> Set[E](),
       7 -> Set[E](),
       8 -> Set[E](3->8, 7->8),
       9 -> Set[E](8->9, 11->9),
      10 -> Set[E](3->10, 11->10),
      11 -> Set[E](5->11, 7->11),
    )
    assert(mapSrc === expectedSrc)

    val expectedBi: EdgeMap[V, E] = Map(
       2 -> Set[E](11->2),
       3 -> Set[E](3->8, 3->10),
       5 -> Set[E](5->11),
       7 -> Set[E](7->8, 7->11),
       8 -> Set[E](3->8, 7->8, 8->9),
       9 -> Set[E](8->9, 11->9),
      10 -> Set[E](3->10, 11->10),
      11 -> Set[E](5->11, 7->11, 11->2, 11->9, 11->10),
    )
    assert(mapBi === expectedBi)
  }

  it should "produce correct paths with DFS" in {
    val path1 = Graph.findPath(3, 2, mapTgt)
    assert(path1 === Nil)
    val path1b = Graph.findPath(3, 2, mapBi)
    assert(path1b === Nil)

    val path2 = Graph.findPath(7, 2, mapTgt)
    assert(path2 === List(7, 11, 2))
    val path2b = Graph.findPath(7, 2, mapBi)
    assert(path2b === List(7, 11, 2))

    val path3 = Graph.findPath(5, 10, mapTgt)
    assert(path3 === List(5, 11, 10))
    val path3b = Graph.findPath(5, 10, mapBi)
    assert(path3b === List(5, 11, 10))

    val path4 = Graph.findPath(10, 5, mapTgt)
    assert(path4 === Nil)
    val path4b = Graph.findPath(10, 5, mapBi)
    assert(path4b === Nil)
  }
}