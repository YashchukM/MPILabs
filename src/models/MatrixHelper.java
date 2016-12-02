package models;

/**
 * Created by Михайло on 29.11.2016.
 */
public class MatrixHelper {
    // Size of original matrix
    private int size;

    // Row indexes of original matrix in order of choosing them as pivot
    private int[] orderedRows;

    // Process id's in order of having pivot row
    private int[] orderedProcesses;

    // Solutions array
    private double[] solutions;

    // Service counter
    private int counter;

    public MatrixHelper() {}

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.counter = 0;
        this.size = size;
        this.orderedRows = new int[size];
        this.orderedProcesses = new int[size];
        this.solutions = new double[size];
    }

    public int getRowByOrder(int order) {
        return orderedRows[order];
    }

    public int getProcessByOrder(int order) {
        return orderedProcesses[order];
    }

    public void addOrderedRow(int rowIndex, int processId) {
        orderedProcesses[counter] = processId;
        orderedRows[counter++] = rowIndex;
    }

    public double[] getSolutions() {
        return solutions;
    }

    public void addSolution(double solution, int index) {
        solutions[index] = solution;
    }

    public int[] getOrderedRows() {
        return orderedRows;
    }

    public int[] getOrderedProcesses() {
        return orderedProcesses;
    }
}
