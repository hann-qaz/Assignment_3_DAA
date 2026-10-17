package graph;

/** One directed adjacency entry, or one original edge in the edge list. */
public record Edge(int from, int to, int weight) { }
