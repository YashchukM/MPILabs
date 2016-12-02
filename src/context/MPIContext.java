package context;

import mpi.MPI;

/**
 * Created by Михайло on 29.11.2016.
 */
public class MPIContext {
    public static final int LEADER_ID = 0;
    public static final int CUSTOM_TAG = 19;

    private static int processesNumber;
    private static int processId;

    static {
        processesNumber = MPI.COMM_WORLD.Size();
        processId = MPI.COMM_WORLD.Rank();
    }

    private MPIContext() {}

    public static boolean isLeader() {
        return processId == LEADER_ID;
    }

    public static int getProcessesNumber() {
        return processesNumber;
    }

    public static int getProcessId() {
        return processId;
    }
}
