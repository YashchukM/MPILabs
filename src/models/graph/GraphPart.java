package models.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import context.MPIContext;

/**
 * Created by Михайло on 07.12.2016.
 */
public class GraphPart {
    public static final int UNDEFINED_VERTEX = -1;

    private int verticesNumber;
    private Map<Integer, double[]> columns;
    private Map<Integer, Integer> paths;
    private Set<Integer> chosenVertices;

    public GraphPart() {
        this.columns = new HashMap<>();
        this.chosenVertices = new HashSet<>();
        this.paths = new HashMap<>();
    }

    public static boolean isUndefinedVertex(int vertex) {
        return vertex == UNDEFINED_VERTEX;
    }

    public void init(double[] slice, int verticesNumber, int columnsNumber, int firstVertex) {
        this.verticesNumber = verticesNumber;
        for (int offset = 0, v = firstVertex; offset < columnsNumber * verticesNumber; offset += verticesNumber, v++) {
            double[] column = new double[verticesNumber];
            System.arraycopy(slice, offset, column, 0, verticesNumber);
            columns.put(v, column);
        }
        addChosenVertex(0);
    }

    public void addChosenVertex(int vertex) {
        chosenVertices.add(vertex);
        recalculateDistances();
    }

    private void recalculateDistances() {
        paths.clear();
        for (Integer ownVertex : columns.keySet()) {
            if (chosenVertices.contains(ownVertex)) {
                double[] column = columns.get(ownVertex);
                int minVertex = UNDEFINED_VERTEX;
                double min = Double.MAX_VALUE;
                for (int vertex = 1; vertex < verticesNumber; vertex++) {
                    if (!chosenVertices.contains(vertex)) {
                        if (column[vertex] > 0d && column[vertex] < min) {
                            min = column[vertex];
                            minVertex = vertex;
                        }
                    }
                }
                if (minVertex != UNDEFINED_VERTEX) {
                    paths.put(ownVertex, minVertex);
                }
            }
        }
    }

    public int getShortestDistanceVertex() {
        double distance = Double.MAX_VALUE;
        int vertex = UNDEFINED_VERTEX;
        for (Integer vertexTo : paths.keySet()) {
            double localDistance = columns.get(vertexTo)[paths.get(vertexTo)];
            if (localDistance < distance) {
                distance = localDistance;
                vertex = vertexTo;
            }
        }
        return vertex;
    }

    public double getDistance(int vertexTo) {
        if (isUndefinedVertex(vertexTo)) {
            return 0d;
        } else {
            int vertexFrom = paths.get(vertexTo);
            return columns.get(vertexTo)[vertexFrom];
        }
    }

    public int getVerticesNumber() {
        return verticesNumber;
    }

    public Map<Integer, double[]> getColumns() {
        return columns;
    }

    public Map<Integer, Integer> getPaths() {
        return paths;
    }

    public Set<Integer> getChosenVertices() {
        return chosenVertices;
    }
}
