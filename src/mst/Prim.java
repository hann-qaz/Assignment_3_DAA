package mst;

import graph.*;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Prim {
    public static MstResult run(Graph graph, Stats stats) {
        if (graph.isDirected()) throw new IllegalArgumentException("MST needs undirected roads");
        boolean[] visited = new boolean[graph.size()];
        PriorityQueue<Edge> queue = new PriorityQueue<>((a, b) -> {
            stats.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });
        List<Edge> chosen = new ArrayList<>();
        long weight = 0;
        int components = 0;
        for (int start = 0; start < graph.size(); start++) {
            if (visited[start]) continue;
            components++;
            visit(graph, start, visited, queue, stats);
            while (!queue.isEmpty()) {
                Edge edge = queue.remove();
                stats.queuePops++;
                if (visited[edge.to()]) continue;
                chosen.add(edge);
                weight += edge.weight();
                visit(graph, edge.to(), visited, queue, stats);
            }
        }
        return new MstResult(chosen, weight, components);
    }

    private static void visit(Graph graph, int v, boolean[] visited,
                              PriorityQueue<Edge> queue, Stats stats) {
        visited[v] = true;
        for (Edge edge : graph.neighbors(v)) {
            stats.edgeChecks++;
            if (!visited[edge.to()]) { queue.add(edge); stats.queuePushes++; }
        }
    }
}
