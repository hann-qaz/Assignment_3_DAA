package connectivity;

import graph.*;
import java.util.*;

public class TopologicalSort {
    public record Result(List<Integer> order, boolean hasCycle) {
        public Result { order = List.copyOf(order); }
    }

    public static Result run(Graph graph, Stats stats) {
        if (!graph.isDirected()) throw new IllegalArgumentException("Expected directed graph");
        int[] indegree = new int[graph.size()];
        for (Edge edge : graph.edges()) { indegree[edge.to()]++; stats.edgeChecks++; }
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < graph.size(); v++) {
            if (indegree[v] == 0) { queue.add(v); stats.queuePushes++; }
        }
        List<Integer> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            int v = queue.remove();
            stats.queuePops++;
            order.add(v);
            for (Edge edge : graph.neighbors(v)) {
                stats.edgeChecks++;
                if (--indegree[edge.to()] == 0) { queue.add(edge.to()); stats.queuePushes++; }
            }
        }
        boolean cycle = order.size() != graph.size();
        return new Result(cycle ? List.of() : order, cycle);
    }
}
