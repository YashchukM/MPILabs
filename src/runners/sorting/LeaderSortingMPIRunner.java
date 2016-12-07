package runners.sorting;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import context.MPIContext;
import mpi.MPI;

/**
 * Created by Михайло on 06.12.2016.
 */
public class LeaderSortingMPIRunner extends SortingMPIRunner {
    private static final String DEFAULT_SIZE_FILE_LOCATION = "input.txt";
    private static final int DEFAULT_ARRAY_SIZE = 42;

    private double[] bigArray;

    @Override
    protected void loadData() {
        loadArray();
        divideAndDistribute();
    }

    @Override
    protected void distrubutePivot() {
        double pivot = arrayPart.getAverage();
        arrayPart.setPivot(pivot);
        MPIContext.broadcastDouble(pivot);
    }

    @Override
    protected void gatherParts() {
        int[] sizes = new int[MPIContext.getProcessesNumber()];
        sizes[0] = arrayPart.getArray().length;
        sizes = MPIContext.reduceIntArray(sizes, MPI.MAX);

        bigArray = MPIContext.gathervDoubleArray(arrayPart.getArray(), sizes);
        System.out.println(Arrays.toString(bigArray));
    }

    private void loadArray() {
        int size = readSizeFromFile(DEFAULT_SIZE_FILE_LOCATION);

        double[] array = new double[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            array[i] = random.nextDouble() * 1000 - 500;
        }

        bigArray = array;
        System.out.println(Arrays.toString(bigArray));
    }

    private void divideAndDistribute() {
        int processesNumber = MPIContext.getProcessesNumber();
        int arraySize = bigArray.length;

        // Send sizes to slaves
        int[] sizes = new int[processesNumber + 1];
        for (int process = 1; process < processesNumber; process++) {
            sizes[process] = arraySize / processesNumber + (process < arraySize % processesNumber ? 1 : 0);
        }
        sizes[processesNumber] = arraySize;
        MPI.COMM_WORLD.Bcast(sizes, 0, sizes.length, MPI.INT, MPIContext.LEADER_ID);

        // All without the leader
        for (int process = 1, of = sizes[1]; process < processesNumber; process++, of += sizes[process]) {
            double[] arrayPart = new double[sizes[process]];
            System.arraycopy(bigArray, of, arrayPart, 0, sizes[process]);
            
            // Send parts of matrix to their processes
            MPI.COMM_WORLD.Send(arrayPart, 0, arrayPart.length, MPI.DOUBLE, process, MPIContext.CUSTOM_TAG);
        }

        // Set leader part
        double[] leaderArrayPart = new double[arraySize / processesNumber + (arraySize % processesNumber != 0 ? 1 : 0)];
        System.arraycopy(bigArray, 0, leaderArrayPart, 0, leaderArrayPart.length);
        Arrays.sort(leaderArrayPart);
        arrayPart.setArray(leaderArrayPart);

        // Just to not overload memory
        bigArray = null;
    }

    private int readSizeFromFile(String fileName) {
        try (Scanner scanner = new Scanner(new FileInputStream(fileName))) {
            return scanner.nextInt();
        } catch (FileNotFoundException e) {
            return DEFAULT_ARRAY_SIZE;
        }
    }
}
