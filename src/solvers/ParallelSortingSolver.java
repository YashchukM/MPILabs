package solvers;

import context.MPIContext;
import runners.sorting.LeaderSortingMPIRunner;
import runners.sorting.SlaveSortingMPIRunner;
import runners.sorting.SortingMPIRunner;

/**
 * Created by Михайло on 06.12.2016.
 */
public class ParallelSortingSolver {
    private SortingMPIRunner mpiRunner;

    private ParallelSortingSolver() {}

    public static void solve() {
        ParallelSortingSolver solver = new ParallelSortingSolver();
        solver.init();
        solver.mpiRunner.run();
    }

    private void init() {
        if (MPIContext.isLeader()) {
            this.mpiRunner = new LeaderSortingMPIRunner();
        } else {
            this.mpiRunner = new SlaveSortingMPIRunner();
        }
    }
}
