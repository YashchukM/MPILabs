package runners.sorting;

import context.MPIContext;
import models.sorting.ArrayPart;
import runners.MPIRunner;

/**
 * Created by Михайло on 06.12.2016.
 */
public abstract class SortingMPIRunner implements MPIRunner {
    protected ArrayPart arrayPart;

    public SortingMPIRunner() {
        this.arrayPart = new ArrayPart();
    }

    @Override
    public void run() {
        loadData();
        sort();
        gatherParts();
    }

    private void sort() {
        int iterationsNumber = getIterationsNumber();
        for (int i = 0; i < iterationsNumber; i++) {
            distrubutePivot();
            exchangeParts(i, getPairedProcess(i));
        }
    }

    private void exchangeParts(int iteration, int pairedProcess) {
        double[] lessPart = arrayPart.loePivot();
        double[] morePart = arrayPart.mtPivot();
        double[] receivedPart;
        boolean ownsLessPart = ownsLessPart(iteration);

        if (ownsLessPart) {
            int receivedSize = MPIContext.receiveInt(pairedProcess);
            receivedPart = MPIContext.receiveDoubleArray(receivedSize, pairedProcess);

            MPIContext.sendInt(morePart.length, pairedProcess);
            MPIContext.sendDoubleArray(morePart, pairedProcess);
        } else {
            MPIContext.sendInt(lessPart.length, pairedProcess);
            MPIContext.sendDoubleArray(lessPart, pairedProcess);

            int receivedSize = MPIContext.receiveInt(pairedProcess);
            receivedPart = MPIContext.receiveDoubleArray(receivedSize, pairedProcess);
        }
        arrayPart.updateArray(receivedPart, ownsLessPart);
    }

    // Bit on iteration's position equals to zero
    private boolean ownsLessPart(int iteration) {
        return  ((MPIContext.getProcessId() >> iteration) & 1) == 0;
    }

    // Process with id that differs from this in iteration'th bit
    private int getPairedProcess(int iteration) {
        int id = MPIContext.getProcessId();
        int iterationThBit = (id >> iteration) & 1;
        int clearBitMask = ~(1 << iteration);
        int setBitMask = (1 - iterationThBit) << iteration;
        return (id & clearBitMask) | setBitMask;
    }

    // Number of iterations equals to log(base 2) of processes number
    private int getIterationsNumber() {
        int log2 = (int) (Math.log(MPIContext.getProcessesNumber()) / Math.log(2d));
        return log2;
    }

    protected abstract void loadData();

    protected abstract void distrubutePivot();

    protected abstract void gatherParts();
}
