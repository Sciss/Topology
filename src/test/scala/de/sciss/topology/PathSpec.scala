package de.sciss.topology

import de.sciss.topology.Graph.EdgeMap
import org.scalatest.FlatSpec

class PathSpec extends FlatSpec {
  type V = Int
  type E = Edge[V]

  // cf. https://en.wikipedia.org/wiki/Directed_graph#/media/File:Directed_acyclic_graph_2.svg
  val edges: Seq[E] = Seq(
     3 ->  8,
     3 -> 10,
     5 -> 11,
     7 -> 11,
     7 ->  8,
     8 ->  9,
    11 ->  2,
    11 ->  9,
    11 -> 10
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
      11 -> Set[E](11->2, 11->9, 11->10)
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
      11 -> Set[E](5->11, 7->11)
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
      11 -> Set[E](5->11, 7->11, 11->2, 11->9, 11->10)
    )
    assert(mapBi === expectedBi)
  }

  it should "produce correct paths with directed DFS" in {
    val path1 = Graph.findDirectedPath(3, 2, mapTgt)
    assert(path1 === Nil)
    val path1b = Graph.findDirectedPath(3, 2, mapBi)
    assert(path1b === Nil)

    val path2 = Graph.findDirectedPath(7, 2, mapTgt)
    assert(path2 === List(7, 11, 2))
    val path2b = Graph.findDirectedPath(7, 2, mapBi)
    assert(path2b === List(7, 11, 2))

    val path3 = Graph.findDirectedPath(5, 10, mapTgt)
    assert(path3 === List(5, 11, 10))
    val path3b = Graph.findDirectedPath(5, 10, mapBi)
    assert(path3b === List(5, 11, 10))

    val path4 = Graph.findDirectedPath(10, 5, mapTgt)
    assert(path4 === Nil)
    val path4b = Graph.findDirectedPath(10, 5, mapBi)
    assert(path4b === Nil)
  }

  it should "produce correct paths with undirected DFS" in {
    val path1 = Graph.findUndirectedPath(3, 2, mapBi)
//    println(s"path1: $path1")
    assert(path1 === List(3, 8, 7, 11, 2) || path1 === List(3, 8, 9, 11, 2))

    val path2 = Graph.findUndirectedPath(7, 2, mapBi)
//    println(s"path2: $path2")
    assert(path2 === List(7, 11, 2) || path2 === List(7, 8, 9, 11, 2))

    val path3 = Graph.findUndirectedPath(5, 10, mapBi)
//    println(s"path3: $path3")
    assert(path3 === List(5, 11, 10) || path3 === List(5, 11, 7, 8, 3, 10))

    val path4 = Graph.findUndirectedPath(10, 5, mapBi)
//    println(s"path4: $path4")
    assert(path4 === List(10, 11, 5) || path4 === List(10, 3, 8, 7, 11, 5))
  }
}