package context;

import java.util.Arrays;

import mpi.MPI;
import mpi.Op;

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

    public static void broadcastInt(int value) {
        int[] valueHolder = { value };
        MPI.COMM_WORLD.Bcast(valueHolder, 0, 1, MPI.INT, LEADER_ID);
    }

    public static void broadcastDouble(double value) {
        double[] valueHolder = { value };
        MPI.COMM_WORLD.Bcast(valueHolder, 0, 1, MPI.DOUBLE, LEADER_ID);
    }

    public static void broadcastDoubleArray(double[] array) {
        MPI.COMM_WORLD.Bcast(array, 0, array.length, MPI.DOUBLE, LEADER_ID);
    }

    public static int broadcastedInt() {
        int[] valueHolder = new int[1];
        MPI.COMM_WORLD.Bcast(valueHolder, 0, 1, MPI.INT, LEADER_ID);
        return valueHolder[0];
    }

    public static double broadcastedDouble() {
        double[] valueHolder = new double[1];
        MPI.COMM_WORLD.Bcast(valueHolder, 0, 1, MPI.DOUBLE, LEADER_ID);
        return valueHolder[0];
    }

    public static double[] broadcastedDoubleArray(int size) {
        double[] valueHolder = new double[size];
        MPI.COMM_WORLD.Bcast(valueHolder, 0, size, MPI.DOUBLE, LEADER_ID);
        return valueHolder;
    }

    public static void gathervDoubleArray(double[] array) {
        double[] receiveHolder = new double[0];
        int[] sizesHolder = new int[0];
        MPI.COMM_WORLD.Gatherv(array, 0, array.length, MPI.DOUBLE, receiveHolder, 0,
                sizesHolder, sizesHolder, MPI.DOUBLE, LEADER_ID);
    }

    public static double[] gathervDoubleArray(double[] array, int[] sizesHolder) {
        int[] offsetsHolder = new int[sizesHolder.length];
        int size = sizesHolder[0];
        for (int i = 1; i < sizesHolder.length; i++) {
            offsetsHolder[i] = offsetsHolder[i - 1] + sizesHolder[i - 1];
            size += sizesHolder[i];
        }
        double[] receiveHolder = new double[size];
        MPI.COMM_WORLD.Gatherv(array, 0, array.length, MPI.DOUBLE, receiveHolder, 0,
                sizesHolder, offsetsHolder, MPI.DOUBLE, LEADER_ID);
        return receiveHolder;
    }

    public static int receiveInt(int sender) {
        int[] valueHolder = new int[1];
        MPI.COMM_WORLD.Recv(valueHolder, 0, 1, MPI.INT, sender, MPIContext.CUSTOM_TAG);
        return valueHolder[0];
    }

    public static double[] receiveDoubleArray(int size, int sender) {
        double[] valueHolder = new double[size];
        MPI.COMM_WORLD.Recv(valueHolder, 0, size, MPI.DOUBLE, sender, MPIContext.CUSTOM_TAG);
        return valueHolder;
    }

    public static int[] reduceIntArray(int[] array, Op operation) {
        int[] result = new int[array.length];
        MPI.COMM_WORLD.Reduce(array, 0, result, 0, array.length, MPI.INT, operation, LEADER_ID);
        return result;
    }

    public static void sendInt(int value, int receiver) {
        int[] valueHolder = { value };
        MPI.COMM_WORLD.Send(valueHolder, 0, 1, MPI.INT, receiver, MPIContext.CUSTOM_TAG);
    }

    public static void sendDoubleArray(double[] array, int receiver) {
        MPI.COMM_WORLD.Send(array, 0, array.length, MPI.DOUBLE, receiver, MPIContext.CUSTOM_TAG);
    }
}
