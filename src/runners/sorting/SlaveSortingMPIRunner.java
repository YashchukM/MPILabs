package runners.sorting;

import java.util.Arrays;

import context.MPIContext;
import mpi.MPI;

/**
 * Created by Михайло on 06.12.2016.
 */
public class SlaveSortingMPIRunner extends SortingMPIRunner {
    @Override
    protected void loadData() {
        int processesNumber = MPIContext.getProcessesNumber();

        int[] sizeHolder = new int[processesNumber + 1];
        MPI.COMM_WORLD.Bcast(sizeHolder, 0, processesNumber + 1, MPI.INT, MPIContext.LEADER_ID);
        int size = sizeHolder[MPIContext.getProcessId()];

        double[] received = new double[size];
        MPI.COMM_WORLD.Recv(received, 0, received.length, MPI.DOUBLE, MPIContext.LEADER_ID, MPIContext.CUSTOM_TAG);
        Arrays.sort(received);
        arrayPart.setArray(received);
    }

    @Override
    protected void distrubutePivot() {
        double pivot = MPIContext.broadcastedDouble();
        arrayPart.setPivot(pivot);
    }

    @Override
    protected void gatherParts() {
        int[] sizes = new int[MPIContext.getProcessesNumber()];
        sizes[MPIContext.getProcessId()] = arrayPart.getArray().length;
        MPIContext.reduceIntArray(sizes, MPI.MAX);

        MPIContext.gathervDoubleArray(arrayPart.getArray());
    }
}
