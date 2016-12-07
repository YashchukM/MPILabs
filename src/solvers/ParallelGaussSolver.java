package solvers;

import context.MPIContext;
import runners.gauss.GaussMPIRunner;
import runners.gauss.LeaderGaussMPIRunner;
import runners.gauss.SlaveGaussMPIRunner;

/**
 * Created by Михайло on 29.11.2016.
 */
public class ParallelGaussSolver {
    private GaussMPIRunner mpiRunner;

    private ParallelGaussSolver() {}

    public static void solve() {
        ParallelGaussSolver solver = new ParallelGaussSolver();
        solver.init();
        solver.mpiRunner.run();
    }

    private void init() {
        if (MPIContext.isLeader()) {
            this.mpiRunner = new LeaderGaussMPIRunner();
        } else {
            this.mpiRunner = new SlaveGaussMPIRunner();
        }
    }
}
