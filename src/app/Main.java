package app;

import graph.*;
import mst.*;
import connectivity.*;
import shortestpath.Dijkstra;

public class Main {
    public static void main(String[] args) {
        Graph city = new Graph(5, false);
        city.addEdge(0, 1, 4); city.addEdge(0, 2, 2);
        city.addEdge(1, 2, 1); city.addEdge(1, 3, 5);
        city.addEdge(2, 3, 8); city.addEdge(3, 4, 3);
        MstResult kruskal = Kruskal.run(city, new Stats());
        MstResult prim = Prim.run(city, new Stats());
        System.out.println("City districts are numbered 0..4");
        System.out.println("Kruskal: " + kruskal);
        System.out.println("Prim: " + prim);
        System.out.println("Connected: " + kruskal.connected());
        System.out.println("MST weights agree: " + (kruskal.weight() == prim.weight()));

        Graph roads = new Graph(6, true); // District 5 is unreachable from 0.
        roads.addEdge(0, 1, 4); roads.addEdge(0, 2, 2);
        roads.addEdge(2, 1, 1); roads.addEdge(1, 3, 5);
        roads.addEdge(3, 4, 3);
        System.out.println("DAG ordering: " + TopologicalSort.run(roads, new Stats()));
        Dijkstra.Result routes = Dijkstra.run(roads, 0, new Stats());
        for (int v = 0; v < roads.size(); v++) {
            System.out.println("0 -> " + v + ": distance="
                    + (routes.distanceTo(v) == Dijkstra.INF ? "unreachable" : routes.distanceTo(v))
                    + ", path=" + routes.pathTo(v));
        }
        roads.addEdge(4, 1, 1);
        System.out.println("SCCs after adding 4 -> 1: " + Kosaraju.run(roads, new Stats()));
        System.out.println("Cycle detection: " + TopologicalSort.run(roads, new Stats()));
    }
}
