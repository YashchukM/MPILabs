package runners.graph;

import java.util.Arrays;

import context.MPIContext;
import mpi.MPI;

/**
 * Created by Михайло on 07.12.2016.
 */
 public class SlaveGraphMPIRunner extends GraphMPIRunner {
    @Override
    protected void loadData() {
        int[] metadata = MPIContext.receiveIntArray(3, MPIContext.LEADER_ID);
        double[] slice = MPIContext.receiveDoubleArray(metadata[0] * metadata[1], MPIContext.LEADER_ID);

        graphPart.init(slice, metadata[0], metadata[1], metadata[2]);
    }

    @Override
    protected void connect(int vertexFrom, int vertexTo, double distance) {
        int processesNumber = MPIContext.getProcessesNumber();
        double[] distances = new double[processesNumber];
        distances[MPIContext.getProcessId()] = distance;

        MPIContext.reduceDoubleArray(distances, MPI.MAX);
        int processWithMin = MPIContext.broadcastedInt();
        if (processWithMin == MPIContext.getProcessId()) {
            MPIContext.sendIntArray(new int[] { vertexFrom, vertexTo }, MPIContext.LEADER_ID);
        }

        int chosenVertex = MPIContext.broadcastedInt();
        graphPart.addChosenVertex(chosenVertex);
    }
}
