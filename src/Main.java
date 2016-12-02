import mpi.MPI;
import solvers.ParallelGaussSolver;

/**
 * Created by Михайло on 27.11.2016.
 */
public class Main {
    public static void main(String[] args) {
        MPI.Init(args);

        ParallelGaussSolver.solve();

        MPI.Finalize();
    }
}
