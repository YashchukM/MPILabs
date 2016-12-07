package runners.gauss;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import context.MPIContext;
import mpi.MPI;

/**
 * Created by Михайло on 29.11.2016.
 */
public class LeaderGaussMPIRunner extends GaussMPIRunner {
    private static final String DEFAULT_SIZE_FILE_LOCATION = "input.txt";
    private static final int DEFAULT_MATRIX_SIZE = 12;

    private double[][] bigMatrix;

    public LeaderGaussMPIRunner() {

    }

    @Override
    protected void loadData() {
        loadMatrix();
        divideAndDistribute();
    }

    @Override
    protected void inversedPassage() {
        super.inversedPassage();
        System.out.println("Solutions vector: " + Arrays.toString(matrixHelper.getSolutions()));

        int counter = 0;
        for (int i = 0; i < bigMatrix.length; i++) {
            double sum = bigMatrix[i][bigMatrix.length];
            for (int j = 0; j < bigMatrix[i].length - 1; j++) {
                sum -= bigMatrix[i][j] * matrixHelper.getSolutions()[j];
            }
            if (sum > 0.000001) {
                counter++;
            }
        }
        System.out.println("Total incorrect: " + counter + "/" + matrixHelper.getSize());
    }

    private void divideAndDistribute() {
        int processesNumber = MPIContext.getProcessesNumber();
        int matrixSize = matrixHelper.getSize();

        // Send sizes to slaves
        int[] sizes = new int[processesNumber + 1];
        for (int process = 1; process < processesNumber; process++) {
            sizes[process] = matrixSize / processesNumber + (process < matrixSize % processesNumber ? 1 : 0);
        }
        sizes[processesNumber] = matrixSize;
        MPI.COMM_WORLD.Bcast(sizes, 0, sizes.length, MPI.INT, MPIContext.LEADER_ID);

        // All without the leader
        for (int process = 1; process < processesNumber; process++) {
            double[] matrixPart = new double[sizes[process] * (matrixSize + 1)];
            for (int row = process, of = 0; row < matrixSize; row += processesNumber, of += matrixSize + 1) {
                System.arraycopy(bigMatrix[row], 0, matrixPart, of, matrixSize + 1);
            }
            // Send parts of matrix to their processes
            MPI.COMM_WORLD.Send(matrixPart, 0, matrixPart.length, MPI.DOUBLE, process, MPIContext.CUSTOM_TAG);
        }

        for (int row = 0; row < matrixSize; row += processesNumber) {
            matrixPart.addRow(row, bigMatrix[row]);
        }
    }

    private void loadMatrix() {
        int size = readSizeFromFile(DEFAULT_SIZE_FILE_LOCATION);
        matrixHelper.setSize(size);

        double[][] matrix = new double[size][size + 1];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size + 1; j++) {
                matrix[i][j] = random.nextDouble() * 1000 - 500;
//                System.out.print(matrix[i][j] + " ");
            }
//            System.out.println();
        }

        bigMatrix = matrix;
    }

    private int readSizeFromFile(String fileName) {
        try (Scanner scanner = new Scanner(new FileInputStream(fileName))) {
            return scanner.nextInt();
        } catch (FileNotFoundException e) {
            return DEFAULT_MATRIX_SIZE;
        }
    }
}
