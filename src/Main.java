import mpi.MPI;
import solvers.ParallelGaussSolver;
import solvers.ParallelGraphSolver;
import solvers.ParallelSortingSolver;

/**
 * Created by Михайло on 27.11.2016.
 */
public class Main {
    public static void main(String[] args) {
        MPI.Init(args);

//        ParallelGaussSolver.solve();
//        ParallelSortingSolver.solve();
        ParallelGraphSolver.solve();

        MPI.Finalize();
    }
}
