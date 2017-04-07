package edu.usc.infolab.geo.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.io.ParseException;
import edu.usc.infolab.geo.model.IndexedDirectedGraph;
import edu.usc.infolab.geo.semanticroute.model.Route;
import edu.usc.infolab.geo.ui.MapViewer;
import org.apache.commons.io.FilenameUtils;
import org.geotools.graph.structure.DirectedEdge;
import org.geotools.graph.structure.DirectedNode;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.geotools.swing.JMapFrame;
import org.opengis.referencing.operation.TransformException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by yaguang on 5/1/16.
 */
public class TestCaseBuilder {

    private IndexedDirectedGraph graph = null;
    private List<Route> routes = new ArrayList<>();
    private List<Route> naturalRoutes = null;
    private List<Route> knowledgeRoutes = null;
    private List<Route> queryRoutes = null;
    String dir = null;
    String shapeFilename = null;
    String csvFilename = null;

    public TestCaseBuilder(String filename) throws TransformException, ParseException, IOException {
        dir = FilenameUtils.concat(Utility.getProperty("test_data_dir"), "semanticroute");
        shapeFilename = FilenameUtils.getBaseName(filename);
        csvFilename = FilenameUtils.concat(dir, "links_" + shapeFilename + ".csv");
        init(filename);
    }

    public IndexedDirectedGraph getGraph() {
        return graph;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public List<Route> getQueryRoutes() {
        return queryRoutes;
    }

    /**
     * Returns a test graph, and a set of routes.
     *
     * @param filename
     */
    private void init(String filename) throws IOException, TransformException, ParseException {

        List<List<String>> blocks = readBlocks(filename);
        // Reads nodes.
        List<String> nodeBlock = null, edgeBlock = null;
        for (List<String> block : blocks) {
            String type = block.get(0);
            switch (type) {
                case "NODE":
                    nodeBlock = block;
                    break;
                case "EDGE":
                    edgeBlock = block;
                    generateLinkCSV(nodeBlock, edgeBlock, csvFilename);
                    this.graph = buildGraph(csvFilename);
                    break;
                case "NROUTE":
                    setNaturalRoutes(parseRoutes(block.subList(1, block.size()), graph));
                    updateEdgeName(graph, getNaturalRoutes());
                    this.routes.addAll(getNaturalRoutes());
                    break;
                case "KROUTE":
                    setKnowledgeRoutes(parseRoutes(block.subList(1, block.size()), graph));
                    this.routes.addAll(getKnowledgeRoutes());
                    break;
                case "ROUTE":
                    queryRoutes = parseRoutes(block.subList(1, block.size()), graph);
                    break;
            }
        }
    }

    private static void updateEdgeName(IndexedDirectedGraph graph, List<Route> naturalRoutes) {
        for (Route route : naturalRoutes) {
            String name = route.getSummary();
            for (Edge edge : route.getEdges()) {
                EdgeHelper eh = new EdgeHelper(edge);
                eh.setStreetName(name);
            }
        }
    }

    private static void printRoute(List<Route> routes) {
        if (routes != null) {
            for (Route route : routes) {
                System.out.println(route.toString());
            }
        }
    }

    private static void printGraph(IndexedDirectedGraph graph) {
        HashSet<DirectedEdge> edges = (HashSet<DirectedEdge>) graph.getEdges();
        HashSet<DirectedNode> nodes = (HashSet<DirectedNode>) graph.getNodes();
        for (DirectedNode node : nodes) {
            NodeHelper nh = new NodeHelper(node);
            System.out.println(nh.getInnerNodeId() + "," + nh.getCoordinate().toString());
        }
        for (DirectedEdge edge : edges) {
            EdgeHelper eh = new EdgeHelper(edge);
            System.out.println(eh.getInnerEdgeId() + "," + eh.getStreetName() + "," + eh.getInnerFromNodeId() + "->" + eh.getInnerToNodeId());
        }
    }


    private static IndexedDirectedGraph buildGraph(String csvFilename) throws ParseException, TransformException, IOException {
        IndexedDirectedGraph graph = MapDataHelper.getGraph(csvFilename, null);
        return graph;
    }

    private static void generateLinkCSV(List<String> nodeBlock, List<String> edgeBlock, String csvFilename) throws FileNotFoundException {
        HashMap<String, Coordinate> nodes = parseNodes(nodeBlock.subList(1, nodeBlock.size()));
        PrintWriter pw = new PrintWriter(csvFilename);
        String header = "link_id|street_name|name_default|from_node_id|to_node_id|way_id|travel_direction|function_class|length|geom";
        pw.println(header);
        for (String edgeString : edgeBlock.subList(1, edgeBlock.size())) {
            List<String> edgeFields = new ArrayList<>();
            String[] fields = edgeString.split(",");
            String edgeId = fields[0];
            String fromId = fields[1], toId = fields[2];
            String travelDir = "3";
            if (fields.length >= 4) {
                travelDir = fields[3];
            }
            Coordinate fromNode = nodes.get(fromId), toNode = nodes.get(toId);
            String streetName = "Edge_" + edgeId;
            String nameDefault = streetName;
            String wayId = edgeId;
            String functionClass = "residential";
            double length = fromNode.distance(toNode);
            String gemo = String.format("LINESTRING(%.6f %.6f,%.6f %.6f)", fromNode.x, fromNode.y, toNode.x, toNode.y);
            edgeFields.add(edgeId);
            edgeFields.add(streetName);
            edgeFields.add(nameDefault);
            edgeFields.add(fromId);
            edgeFields.add(toId);
            edgeFields.add(wayId);
            edgeFields.add(travelDir);
            edgeFields.add(functionClass);
            edgeFields.add(String.format("%.2f", length));
            edgeFields.add(gemo);
            pw.println(String.join("|", edgeFields));
        }
        pw.close();
    }

    private static List<List<String>> readBlocks(String filename) throws IOException {
        List<List<String>> blocks = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filename));
        int lineInd = 0, count = 0;
        while (lineInd + count < lines.size()) {
            while (lineInd + count < lines.size() && !lines.get(lineInd + count).trim().isEmpty()) {
                ++count;
            }
            blocks.add(lines.subList(lineInd, lineInd + count));
            lineInd += count + 1;
            count = 0;
        }
        return blocks;
    }

    private static HashMap<String, Coordinate> parseNodes(List<String> lines) {
        HashMap<String, Coordinate> nodes = new HashMap<>();
        for (String line : lines) {
            String[] fields = line.split(",");
            String key = fields[0];
            Coordinate coord = new Coordinate(Double.valueOf(fields[1]),
                    Double.valueOf(fields[2]));
            nodes.put(key, coord);
        }
        return nodes;
    }

    private static List<Route> parseRoutes(List<String> lines, IndexedDirectedGraph graph) {
        List<Route> routes = new ArrayList<>();
        for (String line : lines) {
            String[] fields = line.split(",");
            String routeId = fields[0];
            Route.RouteType type = Route.RouteType.NATURAL;
            switch (fields[1]) {
                case "N":
                    type = Route.RouteType.NATURAL;
                    break;
                case "K":
                    type = Route.RouteType.KNOWN;
                    break;
                case "Q":
                    type = Route.RouteType.QUERY;
                    break;
            }
            int direction = Integer.valueOf(fields[2]);
            String routeSummary = fields[3];
            double score = Double.valueOf(fields[4]);
            List<Node> nodes = new ArrayList<>();
            for (int i = 5; i < fields.length; ++i) {
                long nodeId = Long.valueOf(fields[i]);
                nodes.add(graph.getNode(nodeId));
            }
            if (direction == 1) {
                Route route = new Route(nodes, routeId, type, score, routeSummary);
                routes.add(route);
            } else if (direction == 3) {
                Route route = new Route(nodes, routeId, type, score, routeSummary);
                routes.add(route);
                // Add reverse route
                Collections.reverse(nodes);
                routeId += "_R";
                route = new Route(nodes, routeId, type, score, routeSummary);
                routes.add(route);
            }

        }
        return routes;
    }

    public List<Route> getNaturalRoutes() {
        return naturalRoutes;
    }

    public void setNaturalRoutes(List<Route> naturalRoutes) {
        this.naturalRoutes = naturalRoutes;
    }

    public List<Route> getKnowledgeRoutes() {
        return knowledgeRoutes;
    }

    public void setKnowledgeRoutes(List<Route> knowledgeRoutes) {
        this.knowledgeRoutes = knowledgeRoutes;
    }

    public void visualize() throws TransformException, ParseException, IOException {
        MapDataHelper.generateShape(csvFilename, dir, shapeFilename);
        printGraph(graph);
        printRoute(getNaturalRoutes());
        printRoute(getKnowledgeRoutes());
        printRoute(queryRoutes);
        MapViewer viewer = new MapViewer(FilenameUtils.concat(dir, shapeFilename + ".shp"));
        JMapFrame.showMap(viewer.getMapContent());
    }
}
