package runners;

import context.MPIContext;
import mpi.MPI;

/**
 * Created by Михайло on 29.11.2016.
 */
public class SlaveGaussMPIRunner extends GaussMPIRunner {
    public SlaveGaussMPIRunner() {

    }

    @Override
    protected void loadData() {
        int processesNumber = MPIContext.getProcessesNumber();
        int[] sizeHolder = new int[processesNumber + 1];

        MPI.COMM_WORLD.Bcast(sizeHolder, 0, processesNumber + 1, MPI.INT, MPIContext.LEADER_ID);
        matrixHelper.setSize(sizeHolder[processesNumber]);
        int rowsNumber = sizeHolder[MPIContext.getProcessId()];

        double[] rows = new double[rowsNumber * (matrixHelper.getSize() + 1)];
        MPI.COMM_WORLD.Recv(rows, 0, rows.length, MPI.DOUBLE, MPIContext.LEADER_ID, MPIContext.CUSTOM_TAG);

        for (int rowInd = MPIContext.getProcessId(), offset = 0;
             rowInd < matrixHelper.getSize();
             rowInd += processesNumber, offset += matrixHelper.getSize() + 1) {
            double[] row = new double[matrixHelper.getSize() + 1];
            System.arraycopy(rows, offset, row, 0, matrixHelper.getSize() + 1);
            matrixPart.addRow(rowInd, row);
        }
    }
}
