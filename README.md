# Topology

## statement

Topology is a library for the Scala programming language that provides an online data structure for a directed acyclic graph. It is (C)opyright 2010&ndash;2014 by Hanns Holger Rutz. All rights reserved. This project is released under the [GNU Lesser General Public License](http://github.com/Sciss/Topology/blob/master/LICENSE) v2.1+ and comes with absolutely no warranties. To contact the author, send an email to `contact at sciss.de`

## building

This project builds with sbt 0.13 against Scala 2.11, 2.10.

## linking

To use this project as a library, use the following artifact:

    libraryDependencies += "de.sciss" %% "topology" % v

The current version `v` is `1.0.0-SNAPSHOT`

## getting started

The graph is embodied by the `Topology[V, E]` type where `V` is the opaque vertex type, and `E` is the directed edge type that implements the `Topology.Edge` trait, pointing to the source and target vertex. For example:

```scala
    import de.sciss.topology._

    case class Vertex(label: String)
    case class Edge(sourceVertex: Vertex, targetVertex: Vertex) extends Topology.Edge[Vertex]
```

Then a new empty graph is constructed:

```scala
    val t0 = Topology.empty[Vertex, Edge]
```

The graph has methods `addVertex`, `removeVertex`, `addEdge` and `removeEdge` to evolve the topology. The structure is immutable, so calling any of these methods will return a new updated structure. The `addEdge` method is special in that it returns a `Try`. This is a `Failure` if the edge insertion could not be performed because it would produce a cyclic graph. The successful result is a tuple consisting of the new topology and an optional instruction to move affected vertices. This can be used, for example, by the call site to synchronize a linearized order to the vertices.

```scala
    import scala.util.Success

    val t1 = t0.addVertex(Vertex("foo"))
    val t2 = t1.addVertex(Vertex("bar"))

    val Success((t3, m0)) = t2.addEdge(Edge(Vertex("foo"), Vertex("bar")))  // m0 contains the movement
    t3.addEdge(Edge(Vertex("bar"), Vertex("foo")))  // this is a failure
    val t4 = t3.removeEdge(Edge(Vertex("foo"), Vertex("bar")))
    val Success((t5, m1)) = t4.addEdge(Edge(Vertex("bar"), Vertex("foo")))  // now it's possible; m1 contains movement
    val t6 = t5.removeVertex(Vertex("bar"))
```