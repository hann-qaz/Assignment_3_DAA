package benchmark;

import graph.Graph;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GraphGenerator {
    public enum Kind { UNDIRECTED, DAG, CYCLIC }

    public static Graph generate(int vertices, int edges, Kind kind, long seed) {
        long max = (long) vertices * (vertices - 1);
        if (kind != Kind.CYCLIC) max /= 2;
        int minimum = kind == Kind.DAG ? 0 : kind == Kind.CYCLIC ? 3 : vertices - 1;
        if (vertices < 2 || (kind == Kind.CYCLIC && vertices < 3) || edges < minimum || edges > max)
            throw new IllegalArgumentException("Impossible simple graph configuration");
        Random random = new Random(seed);
        Graph graph = new Graph(vertices, kind != Kind.UNDIRECTED);
        Set<Long> used = new HashSet<>();
        if (kind == Kind.UNDIRECTED) {
            // Every new vertex connects to an earlier one, guaranteeing connectivity.
            for (int v = 1; v < vertices; v++) add(graph, used, v, random.nextInt(v), random);
        } else if (kind == Kind.CYCLIC) {
            // A cycle in the first three vertices; the others may form separate SCCs.
            add(graph, used, 0, 1, random);
            add(graph, used, 1, 2, random);
            add(graph, used, 2, 0, random);
        }
        while (graph.edges().size() < edges) {
            int from = random.nextInt(vertices);
            int to = random.nextInt(vertices);
            if (from == to) continue;
            if (kind == Kind.DAG && from > to) { int temp = from; from = to; to = temp; }
            add(graph, used, from, to, random);
        }
        return graph;
    }

    private static void add(Graph graph, Set<Long> used, int from, int to, Random random) {
        int a = graph.isDirected() ? from : Math.min(from, to);
        int b = graph.isDirected() ? to : Math.max(from, to);
        long key = ((long) a << 32) | b;
        if (used.add(key)) graph.addEdge(from, to, 1 + random.nextInt(100));
    }
}
