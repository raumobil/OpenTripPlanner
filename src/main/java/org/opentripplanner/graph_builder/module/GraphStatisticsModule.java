package org.opentripplanner.graph_builder.module;

import org.locationtech.jts.geom.LineString;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.graph_builder.DataImportIssueStore;
import org.opentripplanner.graph_builder.services.GraphBuilderModule;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.util.stats.DiscreteDistribution;
import org.opentripplanner.util.stats.DiscreteDistribution.ConstantQuantifiable;
import org.opentripplanner.util.stats.DiscreteDistribution.LogQuantifiable;
import org.opentripplanner.util.stats.DiscreteDistribution.NumberQuantifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Print statistics on geometry and edge/vertices data for a graph (number of geometry, average
 * number of points, size distribution, edge names size, etc...)
 */
public class GraphStatisticsModule implements GraphBuilderModule {

    /**
     * An set of ids which identifies what stages this graph builder provides (i.e. streets,
     * elevation, transit)
     */
    public List<String> provides() {
        return Collections.emptyList();
    }

    /** A list of ids of stages which must be provided before this stage */
    public List<String> getPrerequisites() {
        return Arrays.asList("streets");
    }

    private static final Logger LOG = LoggerFactory
            .getLogger(GraphStatisticsModule.class);

    @Override
    public void buildGraph(
            Graph graph,
            HashMap<Class<?>, Object> extra,
            DataImportIssueStore issueStore
    ) {

        DiscreteDistribution<ConstantQuantifiable<String>> edgeTypeDistribution = new DiscreteDistribution<ConstantQuantifiable<String>>();
        DiscreteDistribution<NumberQuantifiable<Integer>> edgeNameDistribution = new DiscreteDistribution<NumberQuantifiable<Integer>>();
        DiscreteDistribution<NumberQuantifiable<Integer>> geomSizeDistribution = new DiscreteDistribution<NumberQuantifiable<Integer>>();
        DiscreteDistribution<LogQuantifiable<Double>> geomLenDistribution = new DiscreteDistribution<LogQuantifiable<Double>>();

        DiscreteDistribution<ConstantQuantifiable<String>> vertexTypeDistribution = new DiscreteDistribution<ConstantQuantifiable<String>>();
        DiscreteDistribution<NumberQuantifiable<Integer>> vertexNameDistribution = new DiscreteDistribution<NumberQuantifiable<Integer>>();
        DiscreteDistribution<NumberQuantifiable<Integer>> vertexLabelDistribution = new DiscreteDistribution<NumberQuantifiable<Integer>>();

        for (Edge e : graph.getEdges()) {
            edgeTypeDistribution.add(new ConstantQuantifiable<String>(e.getClass().toString()));
            edgeNameDistribution.add(new NumberQuantifiable<Integer>(e.getDefaultName() == null ? 0 : e
                    .getDefaultName().length()), e.getDefaultName());
            if (e.getGeometry() != null) {
                LineString geometry = e.getGeometry();
                geomSizeDistribution.add(new NumberQuantifiable<Integer>(geometry.getNumPoints()));
                double lenMeters = SphericalDistanceLibrary.fastLength(geometry);
                geomLenDistribution.add(new LogQuantifiable<Double>(lenMeters, 5.0));
            }
        }
        for (Vertex v : graph.getVertices()) {
            vertexTypeDistribution.add(new ConstantQuantifiable<String>(v.getClass().toString()));
            vertexNameDistribution.add(new NumberQuantifiable<Integer>(v.getDefaultName() == null ? 0 : v
                    .getDefaultName().length()), v.getDefaultName());
            vertexLabelDistribution.add(new NumberQuantifiable<Integer>(v.getLabel().length()),
                    v.getLabel());
        }

        LOG.info("Geometry size distribution (linear scale, # points):\n"
                + geomSizeDistribution.toString());
        LOG.info("Geometry length distribution (log scale, meters):\n"
                + geomLenDistribution.toString());
        LOG.info("Edge type distribution:\n" + edgeTypeDistribution.toString());
        LOG.info("Edge name distribution:\n" + edgeNameDistribution.toString());
        LOG.info("Vertex type distribution:\n" + vertexTypeDistribution.toString());
        LOG.info("Vertex name distribution:\n" + vertexNameDistribution.toString());
        LOG.info("Vertex label distribution:\n" + vertexLabelDistribution.toString());
    }

    @Override
    public void checkInputs() {
        // no inputs to check
    }
}
