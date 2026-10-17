package benchmark;

import connectivity.*;
import graph.*;
import mst.*;
import shortestpath.Dijkstra;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;

public class Benchmark {
    private static final int REPEATS = 5;
    private static final int WARMUPS = 30;
    private static final long SEED = 42;
    private static volatile Object sink;

    private record Job(String name, String kind, Graph graph, Function<Stats, Object> algorithm) { }
    public record Row(String algorithm, String kind, int vertices, int edges, double averageMs) { }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);
        Path tables = Path.of("results/tables");
        Files.createDirectories(tables);
        StringBuilder raw = new StringBuilder("algorithm,kind,V,E,seed,repeat,time_ns,edge_checks,comparisons,find_calls,parent_steps,union_calls,queue_pushes,queue_pops,relaxations\n");
        StringBuilder summary = new StringBuilder("algorithm,kind,V,E,average_ms,min_ms,max_ms,stddev_ms,edge_checks,comparisons,find_calls,parent_steps,union_calls,queue_pushes,queue_pops,relaxations\n");
        StringBuilder markdown = new StringBuilder("| Algorithm | Input | V | E | Mean ms | Min ms | Max ms | SD ms | Edge checks | Comparisons |\n|---|---|---:|---:|---:|---:|---:|---:|---:|---:|\n");
        List<Row> rows = new ArrayList<>();
        int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}};
        for (int[] size : sizes) {
            Graph u = GraphGenerator.generate(size[0], size[1], GraphGenerator.Kind.UNDIRECTED, SEED);
            Graph dag = GraphGenerator.generate(size[0], size[1], GraphGenerator.Kind.DAG, SEED);
            Graph cyclic = GraphGenerator.generate(size[0], size[1], GraphGenerator.Kind.CYCLIC, SEED);
            if (!Kruskal.run(u, new Stats()).connected() || Kruskal.run(u, new Stats()).weight() != Prim.run(u, new Stats()).weight())
                throw new AssertionError("Invalid MST benchmark input");
            if (TopologicalSort.run(dag, new Stats()).hasCycle() || !TopologicalSort.run(cyclic, new Stats()).hasCycle())
                throw new AssertionError("Invalid directed benchmark input");
            List<Job> jobs = List.of(
                    new Job("Kruskal", "undirected", u, s -> Kruskal.run(u, s)),
                    new Job("Prim", "undirected", u, s -> Prim.run(u, s)),
                    new Job("Kosaraju", "cyclic", cyclic, s -> Kosaraju.run(cyclic, s)),
                    new Job("Kosaraju", "DAG", dag, s -> Kosaraju.run(dag, s)),
                    new Job("Topological", "DAG", dag, s -> TopologicalSort.run(dag, s)),
                    new Job("Topological", "cyclic", cyclic, s -> TopologicalSort.run(cyclic, s)),
                    new Job("Dijkstra", "cyclic", cyclic, s -> Dijkstra.run(cyclic, 0, s)));
            for (int warmup = 0; warmup < WARMUPS; warmup++)
                for (Job job : jobs) sink = job.algorithm.apply(new Stats());
            long[][] times = new long[jobs.size()][REPEATS];
            Stats[] counters = new Stats[jobs.size()];
            // Rotate the starting algorithm to reduce a fixed execution-order bias.
            for (int repeat = 0; repeat < REPEATS; repeat++) {
                for (int offset = 0; offset < jobs.size(); offset++) {
                    int index = (repeat + offset) % jobs.size();
                    Job job = jobs.get(index);
                    Stats stats = new Stats();
                    long start = System.nanoTime();
                    Object result = job.algorithm.apply(stats);
                    long elapsed = System.nanoTime() - start;
                    sink = result;
                    times[index][repeat] = elapsed;
                    if (counters[index] != null && !counters[index].csv().equals(stats.csv()))
                        throw new AssertionError("Operation counts changed on identical input");
                    counters[index] = stats;
                    raw.append(String.format("%s,%s,%d,%d,%d,%d,%d,%s%n", job.name, job.kind,
                            size[0], size[1], SEED, repeat + 1, elapsed, stats.csv()));
                }
            }
            for (int index = 0; index < jobs.size(); index++) {
                Job job = jobs.get(index);
                long[] values = times[index];
                double mean = Arrays.stream(values).average().orElseThrow();
                double variance = 0;
                for (long value : values) variance += (value - mean) * (value - mean);
                double sd = Math.sqrt(variance / (REPEATS - 1)) / 1e6; // Sample standard deviation.
                double min = Arrays.stream(values).min().orElseThrow() / 1e6;
                double max = Arrays.stream(values).max().orElseThrow() / 1e6;
                Stats stats = counters[index];
                rows.add(new Row(job.name, job.kind, size[0], size[1], mean / 1e6));
                summary.append(String.format("%s,%s,%d,%d,%.6f,%.6f,%.6f,%.6f,%s%n",
                        job.name, job.kind, size[0], size[1], mean / 1e6, min, max, sd, stats.csv()));
                markdown.append(String.format("| %s | %s | %d | %d | %.6f | %.6f | %.6f | %.6f | %d | %d |%n",
                        job.name, job.kind, size[0], size[1], mean / 1e6, min, max, sd, stats.edgeChecks, stats.comparisons));
            }
        }
        Files.writeString(tables.resolve("raw.csv"), raw);
        Files.writeString(tables.resolve("summary.csv"), summary);
        Files.writeString(tables.resolve("summary.md"), markdown);
        Files.writeString(Path.of("results/environment.txt"),
                "Measurement timestamp (UTC): " + java.time.Instant.now() + "\n"
                + "Java: " + System.getProperty("java.version") + "\nVM: " + System.getProperty("java.vm.name")
                + "\nOS: " + System.getProperty("os.name") + " " + System.getProperty("os.version")
                + "\nArchitecture: " + System.getProperty("os.arch")
                + "\nAvailable processors: " + Runtime.getRuntime().availableProcessors()
                + "\nSeed: " + SEED + "\nWarmups per job and size: " + WARMUPS
                + "\nMeasured repetitions: " + REPEATS + "\nTimer: System.nanoTime()\n");
        PlotWriter.write(rows, true);
        PlotWriter.write(rows, false);
        System.out.println(markdown);
        System.out.println("Saved 105 raw measurements, 21 summaries and two PNG plots in results/.");
    }
}
