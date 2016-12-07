package runners.gauss;

import java.util.Arrays;

import context.MPIContext;
import models.gauss.MatrixHelper;
import models.gauss.MatrixPart;
import mpi.MPI;
import runners.MPIRunner;

/**
 * Created by Михайло on 29.11.2016.
 */
public abstract class GaussMPIRunner implements MPIRunner {
    protected MatrixPart matrixPart;
    protected MatrixHelper matrixHelper;

    public GaussMPIRunner() {
        this.matrixPart = new MatrixPart();
        this.matrixHelper = new MatrixHelper();
    }

    @Override
    public void run() {
        loadData();
        straightPassage();
        inversedPassage();
    }

    protected abstract void loadData();

    protected void straightPassage() {
        for (int i = 0; i < matrixHelper.getSize(); i++) {
            double localMax = matrixPart.getLocalMax(i);
            double globalMax = getGlobalMax(localMax);
            double[] rowWithMax = getRowWithMax(localMax == globalMax);

            transformRows(i, rowWithMax);
        }
    }

    private double[] getRowWithMax(boolean currentProcessOwnsMax) {
        double[] rowWithMax = new double[matrixHelper.getSize() + 1];
        int processId = 0, index = 0;

        if (currentProcessOwnsMax) {
            rowWithMax = matrixPart.getRowWithMax();
            processId = MPIContext.getProcessId();
            index = matrixPart.getIndexOfRowWithMax();
        }

        // Remember order of choosing pivot row for use in inversed passage
        int maxRowOwner = getMaxRowOwner(processId);
        matrixHelper.addOrderedRow(getIndexOfRowWithMax(index), maxRowOwner);

        // Send/recieve row which contains maximum abs value at current position
        MPI.COMM_WORLD.Bcast(rowWithMax, 0, rowWithMax.length, MPI.DOUBLE, maxRowOwner);
        return rowWithMax;
    }

    private double getGlobalMax(double localMax) {
        double[] globalMaxHolder = new double[1];
        MPI.COMM_WORLD.Allreduce(new double[]{localMax}, 0, globalMaxHolder, 0, 1, MPI.DOUBLE, MPI.MAX);
        return globalMaxHolder[0];
    }

    private int getMaxRowOwner(int processId) {
        int[] sendingProcessHolder = { processId };
        MPI.COMM_WORLD.Allreduce(sendingProcessHolder, 0, sendingProcessHolder, 0, 1, MPI.INT, MPI.MAX);
        return sendingProcessHolder[0];
    }

    private int getIndexOfRowWithMax(int index) {
        int[] indexHolder = { index };
        MPI.COMM_WORLD.Allreduce(indexHolder, 0, indexHolder, 0, 1, MPI.INT, MPI.MAX);
        return indexHolder[0];
    }

    private double getSolution(double solutionCandidate, int sender) {
        double[] solutionHolder = { solutionCandidate };
        MPI.COMM_WORLD.Bcast(solutionHolder, 0, 1, MPI.DOUBLE, sender);
        return solutionHolder[0];
    }

    private void transformRows(int curCol, double[] rowWithMax) {
        double globalMax = rowWithMax[curCol];
        for (int rowInd = 0; rowInd < matrixPart.getRowsNumber(); rowInd++) {
            if (!matrixPart.getDoneRows().contains(rowInd)) {
                if (Arrays.equals(rowWithMax, matrixPart.getRow(rowInd))) {
                    matrixPart.addDoneRow(rowInd);
                } else {
                    double[] row = matrixPart.getRow(rowInd);
                    double divider = row[curCol];
                    for (int colInd = curCol; colInd < matrixHelper.getSize() + 1; colInd++) {
                        row[colInd] = rowWithMax[colInd] - row[colInd] / divider * globalMax;
                    }
                }
            }
        }
    }

    protected void inversedPassage() {
        int matrixSize = matrixHelper.getSize();
        for (int order = matrixSize - 1; order >= 0 ; order--) {
            int rowIndex = matrixHelper.getRowByOrder(order);
            double curSolutionCandidate = 0d;
            if (matrixPart.getIndexes().contains(rowIndex)) {
                double[] row = matrixPart.getRow(matrixPart.getIndexes().indexOf(rowIndex));
                curSolutionCandidate = row[matrixSize] / row[order];
            }

            double curSolution = getSolution(curSolutionCandidate, matrixHelper.getProcessByOrder(order));
            matrixHelper.addSolution(curSolution, order);

            updateMatrixPart(curSolution, order);
        }
    }

    private void updateMatrixPart(double curSolution, int order) {
        for (int rowInd = 0; rowInd < matrixPart.getRowsNumber(); rowInd++) {
            double[] row = matrixPart.getRow(rowInd);
            row[matrixHelper.getSize()] -= row[order] * curSolution;
            row[order] = 0d;
        }
    }
}