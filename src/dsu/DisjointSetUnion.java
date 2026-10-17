package dsu;

import graph.Stats;

public class DisjointSetUnion {
    private final int[] parent;
    private final int[] size;
    private final Stats stats;
    private int components;

    public DisjointSetUnion(int n, Stats stats) {
        if (n < 0) throw new IllegalArgumentException("Negative set count");
        this.stats = stats;
        parent = new int[n];
        size = new int[n];
        components = n;
        for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
    }

    public int find(int x) {
        if (x < 0 || x >= parent.length) throw new IllegalArgumentException("Invalid element");
        stats.findCalls++;
        int root = x;
        while (root != parent[root]) { root = parent[root]; stats.parentSteps++; }
        // A second walk makes every visited node point directly to the root.
        while (x != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    public boolean union(int a, int b) {
        stats.unionCalls++;
        int rootA = find(a);
        int rootB = find(b);
        if (rootA == rootB) return false;
        if (size[rootA] < size[rootB]) {
            int temp = rootA; rootA = rootB; rootB = temp;
        }
        parent[rootB] = rootA;
        size[rootA] += size[rootB];
        components--;
        return true;
    }

    public int components() { return components; }
}
