package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Graph {
    private final boolean directed;
    private final List<List<Edge>> adjacency = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    public Graph(int vertices, boolean directed) {
        if (vertices < 0) throw new IllegalArgumentException("Negative vertex count");
        this.directed = directed;
        for (int i = 0; i < vertices; i++) adjacency.add(new ArrayList<>());
    }

    public int size() { return adjacency.size(); }
    public boolean isDirected() { return directed; }
    public List<Edge> edges() { return Collections.unmodifiableList(edges); }
    public List<Edge> neighbors(int v) {
        checkVertex(v);
        return Collections.unmodifiableList(adjacency.get(v));
    }

    public void checkVertex(int v) {
        if (v < 0 || v >= size()) throw new IllegalArgumentException("Invalid vertex: " + v);
    }

    public void addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);
        Edge edge = new Edge(from, to, weight);
        edges.add(edge);
        adjacency.get(from).add(edge);
        if (!directed) adjacency.get(to).add(new Edge(to, from, weight));
    }

    public Graph reversed() {
        if (!directed) throw new IllegalArgumentException("Expected directed graph");
        Graph reverse = new Graph(size(), true);
        for (Edge edge : edges) reverse.addEdge(edge.to(), edge.from(), edge.weight());
        return reverse;
    }
}
