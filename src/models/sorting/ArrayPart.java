package models.sorting;

import java.util.Arrays;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * Created by Михайло on 06.12.2016.
 */
public class ArrayPart {
    private double[] array;
    private double pivot;

    public double getAverage() {
        return Arrays.stream(array).average().orElse(0d);
    }

    public double[] getArray() {
        return array;
    }

    public void setArray(double[] array) {
        this.array = array;
    }

    public double getPivot() {
        return pivot;
    }

    public void setPivot(double pivot) {
        this.pivot = pivot;
    }

    public double[] loePivot() {
        return Arrays.stream(array).filter(value -> value <= pivot).toArray();
    }

    public double[] mtPivot() {
        return Arrays.stream(array).filter(value -> value > pivot).toArray();
    }

    public void updateArray(double[] receivedArray, boolean ownsLessPart) {
        double[] oldPart;
        if (ownsLessPart) {
            oldPart = loePivot();
        } else {
            oldPart = mtPivot();
        }
        array = new double[receivedArray.length + oldPart.length];
        System.arraycopy(oldPart, 0, array, 0, oldPart.length);
        System.arraycopy(receivedArray, 0, array, oldPart.length, receivedArray.length);
        Arrays.sort(array);
    }
}
