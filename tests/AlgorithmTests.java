package tests;

import benchmark.GraphGenerator;
import connectivity.*;
import dsu.DisjointSetUnion;
import graph.*;
import mst.*;
import shortestpath.Dijkstra;
import java.util.*;

/** Dependency-free tests. Checks always execute, even without java -ea. */
public class AlgorithmTests {
    private static int checks;

    public static void main(String[] args) {
        testDsu();
        testKnownGraphs();
        testSpecialCases();
        testRandomGraphs();
        testGenerators();
        System.out.println("PASS: " + checks + " correctness checks");
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static void rejects(Runnable action) {
        boolean rejected = false;
        try { action.run(); } catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected, "Invalid input must be rejected");
    }

    private static void testDsu() {
        Stats stats = new Stats();
        DisjointSetUnion dsu = new DisjointSetUnion(8, stats);
        check(dsu.components() == 8, "Initial singleton sets");
        for (int step = 1; step < 8; step *= 2)
            for (int i = 0; i < 8; i += step * 2) check(dsu.union(i, i + step), "Merge balanced sets");
        check(dsu.components() == 1, "One DSU component");
        check(!dsu.union(0, 7), "Repeated union returns false");
        long before = stats.parentSteps;
        dsu.find(7);
        long first = stats.parentSteps - before;
        before = stats.parentSteps;
        dsu.find(7);
        check(stats.parentSteps - before <= first, "Compression cannot increase path length");
        check(stats.parentSteps - before == 1, "Compressed path is one step");
        for (int i = 0; i < 8; i++) check(dsu.find(i) == dsu.find(0), "All elements connected");
        rejects(() -> dsu.find(-1));
        rejects(() -> new DisjointSetUnion(-1, new Stats()));
    }

    private static void testKnownGraphs() {
        Graph city = new Graph(4, false);
        city.addEdge(0, 1, 1); city.addEdge(1, 2, 2);
        city.addEdge(2, 3, 3); city.addEdge(0, 3, 10); city.addEdge(0, 2, 5);
        check(Kruskal.run(city, new Stats()).weight() == 6, "Known Kruskal weight");
        check(Prim.run(city, new Stats()).weight() == 6, "Known Prim weight");
        verifyMst(city);
        Graph roads = new Graph(6, true);
        roads.addEdge(0, 1, 7); roads.addEdge(0, 2, 2); roads.addEdge(2, 1, 1);
        roads.addEdge(1, 3, 3); roads.addEdge(2, 3, 9); roads.addEdge(3, 4, 0);
        Dijkstra.Result paths = Dijkstra.run(roads, 0, new Stats());
        check(paths.distanceTo(4) == 6, "Known shortest distance");
        check(paths.pathTo(4).equals(List.of(0, 2, 1, 3, 4)), "Known shortest path");
        check(paths.pathTo(5).isEmpty() && paths.distanceTo(5) == Dijkstra.INF, "Unreachable vertex");
        verifyDirected(roads);
        verifyOrder(roads);
        roads.addEdge(4, 1, 1);
        check(TopologicalSort.run(roads, new Stats()).hasCycle(), "Cycle is detected");
        verifyDirected(roads);
        check(Kosaraju.run(roads, new Stats()).stream().anyMatch(c -> new HashSet<>(c).equals(Set.of(1, 3, 4))), "Known SCC");
    }

    private static void testSpecialCases() {
        for (int n : new int[]{0, 1, 4}) {
            Graph graph = new Graph(n, false);
            MstResult k = Kruskal.run(graph, new Stats());
            MstResult p = Prim.run(graph, new Stats());
            check(k.weight() == 0 && p.weight() == 0, "Isolated vertices have zero forest weight");
            check(k.components() == n && p.components() == n, "Isolated component count");
            check(k.connected() == (n <= 1), "Connectivity convention");
            Graph directed = new Graph(n, true);
            verifyDirected(directed);
            verifyOrder(directed);
        }
        Graph disconnected = new Graph(5, false);
        disconnected.addEdge(0, 1, 2); disconnected.addEdge(2, 3, 3);
        check(Kruskal.run(disconnected, new Stats()).components() == 3, "Disconnected Kruskal");
        check(Prim.run(disconnected, new Stats()).components() == 3, "Disconnected Prim");
        check(Prim.run(disconnected, new Stats()).weight() == 5, "Forest weight");
        Graph ties = new Graph(4, false);
        for (int i = 0; i < 4; i++) ties.addEdge(i, (i + 1) % 4, 1);
        ties.addEdge(0, 1, 1); ties.addEdge(2, 2, -5);
        verifyMst(ties); // Multiple MSTs, parallel edges, duplicate weights, self-loop.
        check(Kruskal.run(ties, new Stats()).weight() == 3, "Ties and self-loop");
        Graph negative = new Graph(3, true);
        negative.addEdge(1, 2, -1); // Negative edge is unreachable from source, still rejected.
        rejects(() -> Dijkstra.run(negative, 0, new Stats()));
        Graph largeWeights = new Graph(3, true);
        largeWeights.addEdge(0, 1, Integer.MAX_VALUE); largeWeights.addEdge(1, 2, Integer.MAX_VALUE);
        check(Dijkstra.run(largeWeights, 0, new Stats()).distanceTo(2) == 4294967294L, "long distances avoid int overflow");
        Graph loop = new Graph(1, true); loop.addEdge(0, 0, 0);
        check(TopologicalSort.run(loop, new Stats()).hasCycle(), "Self-loop is a directed cycle");
        verifyDirected(loop);
        rejects(() -> new Graph(-1, false));
        rejects(() -> ties.addEdge(0, 4, 1));
        rejects(() -> Kruskal.run(loop, new Stats()));
        rejects(() -> Prim.run(loop, new Stats()));
        rejects(() -> Kosaraju.run(ties, new Stats()));
        rejects(() -> TopologicalSort.run(ties, new Stats()));
        rejects(() -> Dijkstra.run(loop, 1, new Stats()));
        rejects(() -> GraphGenerator.generate(3, 4, GraphGenerator.Kind.DAG, 1));
    }

    private static void verifyMst(Graph graph) {
        MstResult k = Kruskal.run(graph, new Stats());
        MstResult p = Prim.run(graph, new Stats());
        long best = Long.MAX_VALUE;
        int m = graph.edges().size();
        // Independent exhaustive oracle: enumerate every V-1 edge subset.
        for (int mask = 0; mask < (1 << m); mask++) {
            if (Integer.bitCount(mask) != graph.size() - 1) continue;
            boolean[][] reach = new boolean[graph.size()][graph.size()];
            for (int i = 0; i < graph.size(); i++) reach[i][i] = true;
            long weight = 0;
            for (int e = 0; e < m; e++) if ((mask & (1 << e)) != 0) {
                Edge edge = graph.edges().get(e);
                reach[edge.from()][edge.to()] = reach[edge.to()][edge.from()] = true;
                weight += edge.weight();
            }
            closure(reach);
            boolean connected = true;
            for (int i = 0; i < graph.size(); i++) connected &= reach[0][i];
            if (connected) best = Math.min(best, weight);
        }
        check(best != Long.MAX_VALUE, "Brute force found spanning tree");
        for (MstResult result : List.of(k, p)) {
            check(result.weight() == best, "MST matches exhaustive oracle");
            check(result.connected() && result.edges().size() == graph.size() - 1, "MST spans vertices");
            DisjointSetUnion dsu = new DisjointSetUnion(graph.size(), new Stats());
            long sum = 0;
            for (Edge edge : result.edges()) {
                check(dsu.union(edge.from(), edge.to()), "MST is acyclic");
                check(graph.neighbors(edge.from()).contains(edge), "MST edge belongs to input");
                sum += edge.weight();
            }
            check(sum == result.weight(), "MST edge sum");
        }
    }

    private static void closure(boolean[][] reach) {
        for (int k = 0; k < reach.length; k++)
            for (int i = 0; i < reach.length; i++)
                for (int j = 0; j < reach.length; j++) reach[i][j] |= reach[i][k] && reach[k][j];
    }

    private static void verifyDirected(Graph graph) {
        int n = graph.size();
        boolean[][] reach = new boolean[n][n];
        long[][] distance = new long[n][n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(distance[i], Dijkstra.INF);
            distance[i][i] = 0; reach[i][i] = true;
        }
        for (Edge edge : graph.edges()) {
            reach[edge.from()][edge.to()] = true;
            distance[edge.from()][edge.to()] = Math.min(distance[edge.from()][edge.to()], edge.weight());
        }
        closure(reach);
        // Floyd-Warshall is independent of Dijkstra's greedy heap implementation.
        for (int k = 0; k < n; k++) for (int i = 0; i < n; i++) for (int j = 0; j < n; j++)
            if (distance[i][k] != Dijkstra.INF && distance[k][j] != Dijkstra.INF)
                distance[i][j] = Math.min(distance[i][j], distance[i][k] + distance[k][j]);
        int[] component = new int[n]; Arrays.fill(component, -1);
        List<List<Integer>> sccs = Kosaraju.run(graph, new Stats());
        for (int c = 0; c < sccs.size(); c++) for (int v : sccs.get(c)) {
            check(component[v] == -1, "SCCs do not overlap"); component[v] = c;
        }
        for (int i = 0; i < n; i++) {
            check(component[i] >= 0, "SCC covers every vertex");
            Dijkstra.Result result = Dijkstra.run(graph, i, new Stats());
            for (int j = 0; j < n; j++) {
                check((component[i] == component[j]) == (reach[i][j] && reach[j][i]), "SCC equals mutual reachability");
                check(result.distanceTo(j) == distance[i][j], "Dijkstra equals Floyd-Warshall");
                List<Integer> path = result.pathTo(j);
                if (distance[i][j] == Dijkstra.INF) { check(path.isEmpty(), "Unreachable path empty"); continue; }
                check(path.get(0) == i && path.get(path.size() - 1) == j && path.size() <= n, "Path endpoints and no cycle");
                long sum = 0;
                for (int t = 1; t < path.size(); t++) {
                    int from = path.get(t - 1), to = path.get(t);
                    int min = graph.neighbors(from).stream().filter(e -> e.to() == to).mapToInt(Edge::weight).min().orElseThrow();
                    sum += min;
                }
                check(sum == distance[i][j], "Reconstructed path has shortest cost");
            }
        }
    }

    private static void verifyOrder(Graph graph) {
        TopologicalSort.Result result = TopologicalSort.run(graph, new Stats());
        check(!result.hasCycle(), "DAG accepted");
        check(result.order().size() == graph.size() && new HashSet<>(result.order()).size() == graph.size(), "Ordering is permutation");
        int[] position = new int[graph.size()];
        for (int i = 0; i < graph.size(); i++) position[result.order().get(i)] = i;
        for (Edge edge : graph.edges()) check(position[edge.from()] < position[edge.to()], "Every DAG edge points forward");
    }

    private static void testRandomGraphs() {
        for (int seed = 0; seed < 40; seed++) {
            Graph undirected = GraphGenerator.generate(5, 8, GraphGenerator.Kind.UNDIRECTED, seed);
            verifyMst(undirected);
            Graph dag = GraphGenerator.generate(7, 12, GraphGenerator.Kind.DAG, seed);
            verifyOrder(dag); verifyDirected(dag);
            Graph cyclic = GraphGenerator.generate(7, 15, GraphGenerator.Kind.CYCLIC, seed);
            check(TopologicalSort.run(cyclic, new Stats()).hasCycle(), "Random cycle detected");
            verifyDirected(cyclic);
        }
    }

    private static void testGenerators() {
        int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}};
        for (int[] size : sizes) for (GraphGenerator.Kind kind : GraphGenerator.Kind.values()) {
            Graph a = GraphGenerator.generate(size[0], size[1], kind, 42);
            Graph b = GraphGenerator.generate(size[0], size[1], kind, 42);
            check(a.edges().equals(b.edges()), "Fixed seed is reproducible");
            check(a.size() == size[0] && a.edges().size() == size[1], "Exact graph size");
            Set<String> pairs = new HashSet<>();
            for (Edge edge : a.edges()) {
                int from = a.isDirected() ? edge.from() : Math.min(edge.from(), edge.to());
                int to = a.isDirected() ? edge.to() : Math.max(edge.from(), edge.to());
                check(edge.from() != edge.to() && edge.weight() >= 1 && edge.weight() <= 100, "Simple positive generator");
                check(pairs.add(from + ":" + to), "No duplicate generated edges");
            }
            if (kind == GraphGenerator.Kind.UNDIRECTED) {
                MstResult k = Kruskal.run(a, new Stats()), p = Prim.run(a, new Stats());
                check(k.connected() && p.connected() && k.weight() == p.weight(), "Generated MST comparison");
            } else if (kind == GraphGenerator.Kind.DAG) verifyOrder(a);
            else check(TopologicalSort.run(a, new Stats()).hasCycle(), "Generated cycle");
        }
    }
}
