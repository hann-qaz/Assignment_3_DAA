package graph;

/** Fresh counters for one algorithm run. No global mutable counters. */
public class Stats {
    public long edgeChecks;
    public long comparisons;
    public long findCalls;
    public long parentSteps;
    public long unionCalls;
    public long queuePushes;
    public long queuePops;
    public long relaxations;

    public String csv() {
        return edgeChecks + "," + comparisons + "," + findCalls + "," + parentSteps
                + "," + unionCalls + "," + queuePushes + "," + queuePops + "," + relaxations;
    }
}
