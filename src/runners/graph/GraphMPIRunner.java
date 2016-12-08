package runners.graph;

import java.util.Map;

import context.MPIContext;
import models.graph.GraphPart;
import runners.MPIRunner;

/**
 * Created by Михайло on 07.12.2016.
 */
public abstract class GraphMPIRunner implements MPIRunner {
    protected GraphPart graphPart;

    public GraphMPIRunner() {
        this.graphPart = new GraphPart();
    }

    @Override
    public void run() {
        loadData();
        for (int i = 1; i < graphPart.getVerticesNumber(); i++) {
            int vertexTo = graphPart.getShortestDistanceVertex();
            int vertexFrom = graphPart.getPaths().getOrDefault(vertexTo, -1);
            double distance = graphPart.getDistance(vertexTo);

            connect(vertexFrom, vertexTo, distance);
        }
        printResult();
    }

    protected void printResult() {
        //
    }

    protected abstract void loadData();

    protected abstract void connect(int vertexFrom, int vertexTo, double distance);
}
