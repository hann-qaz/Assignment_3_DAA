# Grading evidence map

This checklist maps the supplied rubric to deliverables; it is not a promised grade.

| Criterion | Points | Evidence |
|---|---:|---|
| DSU implementation | 10 | `src/dsu/DisjointSetUnion.java`, size heuristic, full compression, DSU tests |
| Kruskal and Prim | 20 | `src/mst/`, total weights, forests, connectivity, cross-checks and exhaustive oracle |
| SCC and topological ordering | 15 | `src/connectivity/`, cycle rejection, mutual reachability tests |
| Dijkstra | 15 | `src/shortestpath/`, paths, unreachable vertices, negative-weight rejection |
| Complexity analysis | 10 | README Complexity Analysis, implementation-specific heap and space bounds |
| Experimental design and benchmarking | 10 | `src/benchmark/`, exact sizes, seed 42, 5 measured repetitions, nanoTime, counters |
| Results, plots, discussion | 10 | README Results and Discussion, 105 raw runs, 21 summaries, two PNG plots, DSU comparison |
| Testing and correctness | 5 | `tests/AlgorithmTests.java`, independent oracles and required edge cases |
| README and GitHub quality | 5 | Complete report, task checklist, meaningful commits, CI workflow |
| Total | 100 | All categories represented |
