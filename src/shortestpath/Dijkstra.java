package shortestpath;

import graph.*;
import java.util.*;

public class Dijkstra {
    public static final long INF = Long.MAX_VALUE;
    private record Entry(int vertex, long distance) { }

    public static class Result {
        private final long[] distances;
        private final int[] previous;
        private Result(long[] distances, int[] previous) {
            this.distances = distances;
            this.previous = previous;
        }
        public long distanceTo(int v) { return distances[v]; }
        public List<Integer> pathTo(int v) {
            if (distances[v] == INF) return List.of();
            List<Integer> path = new ArrayList<>();
            for (int current = v; current != -1; current = previous[current]) path.add(current);
            Collections.reverse(path);
            return path;
        }
    }

    public static Result run(Graph graph, int source, Stats stats) {
        graph.checkVertex(source);
        for (Edge edge : graph.edges()) {
            stats.edgeChecks++;
            if (edge.weight() < 0) throw new IllegalArgumentException("Dijkstra needs non-negative weights");
        }
        long[] distance = new long[graph.size()];
        int[] previous = new int[graph.size()];
        Arrays.fill(distance, INF);
        Arrays.fill(previous, -1);
        distance[source] = 0;
        PriorityQueue<Entry> queue = new PriorityQueue<>((a, b) -> {
            stats.comparisons++;
            return Long.compare(a.distance(), b.distance());
        });
        queue.add(new Entry(source, 0));
        stats.queuePushes++;
        while (!queue.isEmpty()) {
            Entry current = queue.remove();
            stats.queuePops++;
            int v = current.vertex();
            if (current.distance() != distance[v]) continue; // Ignore outdated queue entries.
            for (Edge edge : graph.neighbors(v)) {
                stats.edgeChecks++;
                long candidate = distance[v] + edge.weight();
                if (candidate < distance[edge.to()]) {
                    distance[edge.to()] = candidate;
                    previous[edge.to()] = v;
                    queue.add(new Entry(edge.to(), candidate));
                    stats.queuePushes++;
                    stats.relaxations++;
                }
            }
        }
        return new Result(distance, previous);
    }
}
