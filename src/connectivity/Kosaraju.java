package connectivity;

import graph.*;
import java.util.*;

public class Kosaraju {
    public static List<List<Integer>> run(Graph graph, Stats stats) {
        if (!graph.isDirected()) throw new IllegalArgumentException("Expected directed graph");
        boolean[] visited = new boolean[graph.size()];
        int[] nextEdge = new int[graph.size()];
        List<Integer> finish = new ArrayList<>();
        ArrayDeque<Integer> stack = new ArrayDeque<>();
        // Iterative DFS keeps the next neighbor index, just like a recursive call.
        for (int start = 0; start < graph.size(); start++) {
            if (visited[start]) continue;
            visited[start] = true;
            stack.push(start);
            while (!stack.isEmpty()) {
                int v = stack.peek();
                if (nextEdge[v] == graph.neighbors(v).size()) {
                    finish.add(stack.pop());
                } else {
                    int to = graph.neighbors(v).get(nextEdge[v]++).to();
                    stats.edgeChecks++;
                    if (!visited[to]) { visited[to] = true; stack.push(to); }
                }
            }
        }
        Graph reverse = graph.reversed();
        stats.edgeChecks += graph.edges().size(); // Edges copied for the transpose.
        Arrays.fill(visited, false);
        List<List<Integer>> components = new ArrayList<>();
        for (int i = finish.size() - 1; i >= 0; i--) {
            int start = finish.get(i);
            if (visited[start]) continue;
            List<Integer> component = new ArrayList<>();
            visited[start] = true;
            stack.push(start);
            while (!stack.isEmpty()) {
                int v = stack.pop();
                component.add(v);
                for (Edge edge : reverse.neighbors(v)) {
                    stats.edgeChecks++;
                    if (!visited[edge.to()]) { visited[edge.to()] = true; stack.push(edge.to()); }
                }
            }
            components.add(component);
        }
        return components;
    }
}
