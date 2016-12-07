package models.gauss;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Михайло on 29.11.2016.
 */
public class MatrixPart {
    private List<Integer> indexes;
    private List<double[]> rows;
    private Set<Integer> doneRows;

    private int lastRowWithMax;

    public MatrixPart() {
        this.indexes = new ArrayList<>();
        this.rows = new ArrayList<>();
        this.doneRows = new HashSet<>();
    }

    public double getLocalMax(int elementIndex) {
        double max = 0D;
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            if (!doneRows.contains(rowIndex)) {
                if (Math.abs(rows.get(rowIndex)[elementIndex]) > max) {
                    max = Math.abs(rows.get(rowIndex)[elementIndex]);
                    lastRowWithMax = rowIndex;
                }
            }
        }
        return max;
    }

    public List<Integer> getIndexes() {
        return indexes;
    }

    public void addRow(int index, double[] row) {
        indexes.add(index);
        rows.add(row);
    }

    public int getRowsNumber() {
        return rows.size();
    }

    public int getLastRowWithMax() {
        return lastRowWithMax;
    }

    public double[] getRowWithMax() {
        return rows.get(lastRowWithMax);
    }

    public double[] getRow(int index) {
        return rows.get(index);
    }

    public int getIndexOfRowWithMax() {
        return indexes.get(lastRowWithMax);
    }

    public Set<Integer> getDoneRows() {
        return doneRows;
    }

    public void addDoneRow(int doneRow) {
        doneRows.add(doneRow);
    }

    public List<double[]> getRows() {
        return rows;
    }
}
