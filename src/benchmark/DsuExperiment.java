package benchmark;

import dsu.DisjointSetUnion;
import graph.Stats;
import java.nio.file.*;

/** Operation-count experiment on an intentionally unfavorable union sequence. */
public class DsuExperiment {
    private static class Baseline {
        final int[] parent;
        final boolean compress;
        long steps;
        Baseline(int n, boolean compress) {
            this.compress = compress;
            parent = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;
        }
        int find(int x) {
            int root = x;
            while (parent[root] != root) { root = parent[root]; steps++; }
            if (compress) while (x != root) { int next = parent[x]; parent[x] = root; x = next; }
            return root;
        }
        void union(int a, int b) { parent[find(b)] = find(a); }
    }

    public static void write() throws Exception {
        int n = 1000;
        Baseline naive = new Baseline(n, false), compression = new Baseline(n, true);
        Stats stats = new Stats();
        DisjointSetUnion optimized = new DisjointSetUnion(n, stats);
        for (int i = 0; i < n - 1; i++) {
            naive.union(i + 1, i); compression.union(i + 1, i); optimized.union(i + 1, i);
        }
        StringBuilder table = new StringBuilder("round,naive_parent_steps,compression_only_parent_steps,size_and_compression_parent_steps\n");
        for (int round = 1; round <= 5; round++) {
            long a = naive.steps, b = compression.steps, c = stats.parentSteps;
            for (int v = 0; v < n; v++) {
                naive.find(v); compression.find(v); optimized.find(v);
            }
            table.append(round).append(',').append(naive.steps - a).append(',')
                    .append(compression.steps - b).append(',').append(stats.parentSteps - c).append('\n');
        }
        Files.writeString(Path.of("results/tables/dsu.csv"), table);
    }
}
