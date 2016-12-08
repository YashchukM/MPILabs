package solvers;

import context.MPIContext;
import runners.graph.GraphMPIRunner;
import runners.graph.LeaderGraphMPIRunner;
import runners.graph.SlaveGraphMPIRunner;

/**
 * Created by Михайло on 06.12.2016.
 */
public class ParallelGraphSolver {
    private GraphMPIRunner mpiRunner;

    private ParallelGraphSolver() {}

    public static void solve() {
        ParallelGraphSolver solver = new ParallelGraphSolver();
        solver.init();
        solver.mpiRunner.run();
    }

    private void init() {
        if (MPIContext.isLeader()) {
            this.mpiRunner = new LeaderGraphMPIRunner();
        } else {
            this.mpiRunner = new SlaveGraphMPIRunner();
        }
    }
}
