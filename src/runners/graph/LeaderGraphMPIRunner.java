package runners.graph;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import context.MPIContext;
import mpi.MPI;

/**
 * Created by Михайло on 07.12.2016.
 */
public class LeaderGraphMPIRunner extends GraphMPIRunner {
    private static final String DEFAULT_SIZE_FILE_LOCATION = "input.txt";
    private static final String DEFAULT_GRAPH_FILE_LOCATION = "graph.txt";
    private static final int DEFAULT_MATRIX_SIZE = 12;
    private static final double DEFAULT_PERCENT = 0.3d;
    private static final int DEFAULT_BOUND = 50;

    private double[][] incidenceMatrix;
    private Map<Integer, Integer> finalPath;

    public LeaderGraphMPIRunner() {
        this.finalPath = new HashMap<>();
    }

    @Override
    protected void loadData() {
        loadMatrix();
        divideAndDistribute();
    }

    @Override
    protected void connect(int vertexFrom, int vertexTo, double distance) {
        double[] distances = new double[MPIContext.getProcessesNumber()];
        distances[0] = distance;

        distances = MPIContext.reduceDoubleArray(distances, MPI.MAX);
        int processWithMinimal = indexOfMinimal(distances);
        MPIContext.broadcastInt(processWithMinimal);

        if (processWithMinimal != MPIContext.LEADER_ID) {
            int[] path = MPIContext.receiveIntArray(2, processWithMinimal);
            vertexFrom = path[0];
            vertexTo = path[1];
        }
        MPIContext.broadcastInt(vertexFrom);

        finalPath.put(vertexFrom, vertexTo);
        graphPart.addChosenVertex(vertexFrom);
    }

    @Override
    protected void printResult() {
        for (Map.Entry<Integer, Integer> way : finalPath.entrySet()) {
            System.out.println(way.getKey() + "->" + way.getValue());
        }
    }

    private int indexOfMinimal(double[] distances) {
        int index = 0;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < MPIContext.getProcessesNumber(); i++) {
            if (distances[i] > 0d && distances[i] < min) {
                index = i;
                min = distances[i];
            }
        }
        return index;
    }

    // Divide and distribute incidence matrix parts between slave processes
    private void divideAndDistribute() {
        int processesNumber = MPIContext.getProcessesNumber();
        int size = incidenceMatrix.length;
        int baseWidth = size / processesNumber, zeroSize = baseWidth + (size % processesNumber != 0 ? 1 : 0);

        // Slave's parts
        for (int process = 1, offset = zeroSize; process < processesNumber; process++) {
            int sliceSize = baseWidth + (process < size % processesNumber ? 1 : 0);
            double[] slice = matrixSlice(offset, offset + sliceSize);

            MPIContext.sendIntArray(new int[]{size, sliceSize, offset}, process);
            MPIContext.sendDoubleArray(slice, process);

            offset += sliceSize;
        }

        // Leader part
        graphPart.init(matrixSlice(0, zeroSize), size, zeroSize, 0);
    }

    // Transform columns in range [beg, end) into one-dimentional array
    private double[] matrixSlice(int beg, int end) {
        int size = incidenceMatrix.length, index = 0;
        double[] slice = new double[(end - beg) * size];

        for (int j = beg; j < end; j++) {
            for (int i = 0; i < size; i++) {
                slice[index++] = incidenceMatrix[i][j];
            }
        }

        return slice;
    }

    // Generate and load into memory incidence matrix for graph
    private void loadMatrix() {
        int size = readSizeFromFile(DEFAULT_SIZE_FILE_LOCATION);
        double[][] matrix = new double[size][size];
        Random random = new Random();
        int iterationsNumber = (int) (size * size * DEFAULT_PERCENT);

        for (int iteration = 0; iteration < iterationsNumber; iteration++) {
            int i = random.nextInt(size);
            int j = random.nextInt(size);
            if (i != j) {
                matrix[i][j] = matrix[j][i] = random.nextInt(DEFAULT_BOUND);
            }
        }

        double[] zero = new double[size];
        for (int i = 0; i < size; i++) {
            if (Arrays.equals(matrix[i], zero)) {
                int j;
                do {
                    j = random.nextInt(size);
                } while (i == j);
                matrix[i][j] = matrix[j][i] = random.nextInt(DEFAULT_BOUND);
            }
        }
        print(matrix);
        incidenceMatrix = matrix;
    }

    private void print(double[][] m) {
        for (double[] row : m) {
            System.out.println(Arrays.toString(row));
        }
    }

    private int readSizeFromFile(String fileName) {
        try (Scanner scanner = new Scanner(new FileInputStream(fileName))) {
            return scanner.nextInt();
        } catch (FileNotFoundException e) {
            return DEFAULT_MATRIX_SIZE;
        }
    }
}
