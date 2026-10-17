package mst;

import dsu.DisjointSetUnion;
import graph.*;
import java.util.ArrayList;
import java.util.List;

public class Kruskal {
    public static MstResult run(Graph graph, Stats stats) {
        if (graph.isDirected()) throw new IllegalArgumentException("MST needs undirected roads");
        List<Edge> sorted = new ArrayList<>(graph.edges());
        sorted.sort((a, b) -> { stats.comparisons++; return Integer.compare(a.weight(), b.weight()); });
        DisjointSetUnion dsu = new DisjointSetUnion(graph.size(), stats);
        List<Edge> chosen = new ArrayList<>();
        long weight = 0;
        for (Edge edge : sorted) {
            stats.edgeChecks++;
            if (dsu.union(edge.from(), edge.to())) {
                chosen.add(edge);
                weight += edge.weight();
                if (chosen.size() == graph.size() - 1) break;
            }
        }
        return new MstResult(chosen, weight, dsu.components());
    }
}
