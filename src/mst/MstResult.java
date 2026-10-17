package mst;

import graph.Edge;
import java.util.List;

/** Disconnected input produces a minimum spanning forest. */
public record MstResult(List<Edge> edges, long weight, int components) {
    public MstResult { edges = List.copyOf(edges); }
    public boolean connected() { return components <= 1; }
}
