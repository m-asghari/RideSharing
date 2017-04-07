package edu.usc.infolab.geo.model;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;
import edu.usc.infolab.geo.util.EdgeHelper;
import edu.usc.infolab.geo.util.GraphHelper;
import edu.usc.infolab.geo.util.NodeHelper;
import edu.usc.infolab.geo.util.WGS2MetricTransformer;
import edu.usc.infolab.geo.util.route.BoundedAStarSPFinder;
import edu.usc.infolab.geo.util.route.PathWithStat;
import org.apache.commons.lang3.tuple.Pair;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.graph.path.AStarShortestPathFinder;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.*;
import org.geotools.graph.structure.basic.BasicDirectedGraph;
import org.geotools.graph.traverse.standard.AStarIterator.AStarFunctions;
import org.geotools.graph.traverse.standard.AStarIterator.AStarNode;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.operation.TransformException;

import java.util.*;

public class IndexedDirectedGraph extends BasicDirectedGraph {

    /**
     *
     */
    private static final long serialVersionUID = 5915594659305382835L;

    private STRtree edgeIndex = new STRtree();
    private STRtree nodeIndex = new STRtree();
    private HashMap<Long, Node> nodeMap = new HashMap<Long, Node>();
    private HashMap<Long, Edge> edgeMap = new HashMap<Long, Edge>();
    private HashMap<Long, Double> pathDistanceMap = new HashMap<>();
    private WGS2MetricTransformer transformer = null;

    public IndexedDirectedGraph(@SuppressWarnings("rawtypes") Collection nodes,
                                @SuppressWarnings("rawtypes") Collection edges) {
        super(nodes, edges);
        init();
    }

    public IndexedDirectedGraph(Graph graph) {
        this(graph.getNodes(), graph.getEdges());
    }

    public IndexedDirectedGraph(SimpleFeatureCollection collection) {
        this(GraphHelper.buildGraph(collection));
    }

    public IndexedDirectedGraph(SimpleFeatureCollection collection,
                                WGS2MetricTransformer transformer) {
        this(GraphHelper.buildGraph(collection));
        this.transformer = transformer;
    }

    /**
     * Get the transformer between WGS and metric used in this graph.
     */
    public WGS2MetricTransformer getTransformer() {
        return transformer;
    }

    private void init() {
        SimpleFeatureType nodeType = getNodeFeatureType();
        SimpleFeatureBuilder nodeFeatureBuilder = new SimpleFeatureBuilder(nodeType);
        // Builds Edge Index.
        for (Object obj : getEdges()) {
            DirectedEdge edge = (DirectedEdge) obj;
            SimpleFeature feature = (SimpleFeature) edge.getObject();
            long innerEdgeId = (Long) feature.getAttribute("link_id");
            LineString geom = (LineString) feature.getDefaultGeometry();
            // Index the edge
            edgeIndex.insert(geom.getEnvelopeInternal(), edge);
            // Updates the edge _map.
            edgeMap.put(innerEdgeId, edge);
            // Updates the node _map.
            Node inNode = edge.getInNode(), outNode = edge.getOutNode();
            Long inNodeId = (Long) feature.getAttribute("from_node_id"),
                    outNodeId = (Long) feature.getAttribute("to_node_id");
            if (nodeMap.put(inNodeId, inNode) == null) {
                nodeIndex.insert(geom.getStartPoint().getEnvelopeInternal(), inNode);
                SimpleFeature inNodeFeature = nodeFeatureBuilder.buildFeature(inNodeId.toString(),
                        new Object[]{inNodeId, geom.getStartPoint()});
                inNode.setObject(inNodeFeature);
            }
            if (nodeMap.put(outNodeId, outNode) == null) {
                nodeIndex.insert(geom.getEndPoint().getEnvelopeInternal(), outNode);
                SimpleFeature outNodeFeature = nodeFeatureBuilder.buildFeature(outNodeId.toString(),
                        new Object[]{outNodeId, geom.getEndPoint()});
                outNode.setObject(outNodeFeature);
            }
        }
        edgeIndex.build();
    }

    private SimpleFeatureType getNodeFeatureType() {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("node");
        builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference system
        builder.add("node_id", Long.class);
        builder.add("geom", Point.class);
        return builder.buildFeatureType();
    }

    /**
     * Gets a node with certain id.
     *
     * @param innerNodeId node id, different from the Node.getID();
     * @return
     */
    public Node getNode(long innerNodeId) {
        return nodeMap.get(innerNodeId);
    }

    public Edge getEdge(long innerEdgeId) {
        return edgeMap.get(innerEdgeId);
    }

    /**
     * Gets inner identifier for a node.
     *
     * @param node
     * @return
     */
    public long getInnerNodeId(Node node) {
        SimpleFeature feature = (SimpleFeature) node.getObject();
        return (Long) feature.getAttribute("node_id");
    }

    /*
     * Gets inner identifier for an edge.
     *
     * @param e
     * @return
     */
    public long getInnerEdgeId(Edge e) {
        SimpleFeature feature = (SimpleFeature) e.getObject();
        return (Long) feature.getAttribute("link_id");
    }

    /**
     * Gets edges within in a range.
     *
     * @param envelope
     * @return
     */
    @SuppressWarnings("unchecked")
    public HashSet<Edge> queryEdges(Envelope envelope) {
        List<Edge> edges = edgeIndex.query(envelope);
        return new HashSet<Edge>(edges);
    }

    /**
     * Gets nodes with in a range.
     *
     * @param envelope
     * @return
     */
    @SuppressWarnings("unchecked")
    public HashSet<Node> queryNodes(Envelope envelope) {
        List<Node> nodes = nodeIndex.query(envelope);
        return new HashSet<Node>(nodes);
    }


    /**
     * Finds the nearest neighbor of a given location.
     *
     * @param location
     * @return
     */
    public Node queryNearestNode(Coordinate location) {
        double curRadius = 25;  // Sets the initial query radius to 25m.
        HashSet<Node> cands = new HashSet<>();
        Node nn = null;
        while (cands.isEmpty()) {
            Envelope region = new Envelope(location);
            region.expandBy(curRadius);
            cands = queryNodes(region);
            curRadius *= 2;
        }
        // Finds the nearest node through refinement.
        double minDist = Double.MAX_VALUE;
        for (Node node : cands) {
            NodeHelper nodeHelper = new NodeHelper(node);
            double curDist = nodeHelper.getCoordinate().distance(location);
            if (curDist < minDist) {
                minDist = curDist;
                nn = node;
            }
        }
        return nn;
    }


    /**
     * Calculates the shortest path between two nodes using A-star algorithm.
     *
     * @param from
     * @param to
     * @return
     */
    public Path queryShortestPath(Node from, Node to) {
        BoundedAStarSPFinder finder = new BoundedAStarSPFinder(this);
        Path path = finder.getPath(from, to);
        return path;
    }

    /**
     * Calculates the shortest path between two nodes using bounded A-star algorithm.
     *
     * @param from
     * @param to
     * @return a path or null if the path does not exist.
     */
    public Path queryShortestPath(Node from, Node to, double bound) {
        BoundedAStarSPFinder finder = new BoundedAStarSPFinder(this);
        Path path = finder.getPath(from, to, bound);
        return path;
    }


    /**
     * Finds the shortest path from source (lng, lat) to destination (lng, lat).
     *
     * @param source      source location in (lng, lat).
     * @param destination destination location in (lng, lat).
     * @return A path and its statistic, e.g., distance.
     */
    public PathWithStat queryShortestPath(Coordinate source, Coordinate destination)
            throws TransformException {
        return queryShortestPath(source, destination, Double.MAX_VALUE);
    }

    /**
     * Finds the shortest path from source (lng, lat) to destination (lng, lat).
     *
     * @param source      source location in (lng, lat).
     * @param destination destination location in (lng, lat).
     * @param bound       maximum allowed distance, which is used for efficient bounded search. If the actual distance
     *                    from source to destination is greater than the bound, a null list will be returned.
     * @return A path and its statistic, e.g., distance.
     */
    public PathWithStat queryShortestPath(Coordinate source, Coordinate destination, double bound)
            throws TransformException {
        List<Coordinate> coordinates = null;
        Node sourceNode = queryNearestNode(transformer.fromWGS84(source)),
                destinationNode = queryNearestNode(transformer.fromWGS84(destination));
        BoundedAStarSPFinder finder = new BoundedAStarSPFinder(this);
        PathWithStat pathWithStat = finder.getPathWithStat(sourceNode, destinationNode, bound);
        return pathWithStat;
    }

    private AStarFunctions getAStartFunc(Node to) {
        AStarFunctions astarfunc = new AStarFunctions(to) {
            @Override
            public double h(Node n) {
                Point p1 = ((Point) ((SimpleFeature) n.getObject()).getAttribute("geom"));
                Point p2 = ((Point) ((SimpleFeature) getDest().getObject()).getAttribute("geom"));
                double distance = p1.distance(p2);
                return distance;
            }

            @Override
            public double cost(AStarNode n1, AStarNode n2) {
                DirectedEdge edge = (DirectedEdge) n1.getNode().getEdge(n2.getNode());
                double edgeCost = (Double) (((SimpleFeature) edge.getObject()).getAttribute("length"));
                return edgeCost;
            }
        };
        return astarfunc;
    }

    /**
     * Calculates the shortest path between two nodes using
     * {@link AStarShortestPathFinder}.
     *
     * @param from
     * @param to
     * @return
     */
    public Path queryShortestPath2(Node from, Node to) {
        AStarFunctions fn = getAStartFunc(to);
        AStarShortestPathFinder pf = new AStarShortestPathFinder(this, from, to, fn);
        pf.calculate();
        Path path = null;
        try {
            path = pf.getPath();
            path.reverse();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return path;
    }

    /**
     * Gets the road network distance between two coordinates that are associated with edges.
     *
     * @param e1
     * @param p1
     * @param e2
     * @param p2
     * @return
     */
    public double getRNDist(Edge e1, Coordinate p1, Edge e2, Coordinate p2) {
        return getRNDist(e1, p1, e2, p2, Double.POSITIVE_INFINITY);
    }

    /**
     * Gets the road network distance between two coordinates that are associated with edges.
     *
     * @param e1
     * @param p1
     * @param e2
     * @param p2
     * @param maxDist the maximum distance allowed for the shortest path searching, i.e., the
     *                searching process will stop once the distance threshold is meet.
     * @return distance between two coordinates, or Double.POSITIVE_INFINITY if the maxDist is exceeded.
     */
    @SuppressWarnings({"unchecked"})
    public double getRNDist(Edge e1, Coordinate p1, Edge e2, Coordinate p2, double maxDist) {
        double distance;
        try {
            EdgeHelper eh1 = new EdgeHelper(e1), eh2 = new EdgeHelper(e2);
            LineString line1 = eh1.getLineString(), line2 = eh2.getLineString();
            DirectedEdge de1 = (DirectedEdge) e1, de2 = (DirectedEdge) e2;
            LocationIndexedLine indexedLine1 = new LocationIndexedLine(line1);
            LocationIndexedLine indexedLine2 = new LocationIndexedLine(line2);
            LinearLocation ind1 = indexedLine1.project(p1), ind2 = indexedLine2.project(p2);
            if (e1.equals(e2)) {
                distance = indexedLine1.extractLine(ind1, ind2).getLength();
            } else {
                LinearLocation lineEnd1 = new LinearLocation(), lineStart2 = new LinearLocation();
                lineEnd1.setToEnd(line1);
                double distance1 = indexedLine1.extractLine(ind1, lineEnd1).getLength();
                double distance2 = indexedLine2.extractLine(lineStart2, ind2).getLength();
                double pathDistance = getPathDistance(maxDist, de1.getOutNode(), de2.getInNode());
                distance = distance1 + pathDistance + distance2;
            }
        } catch (Exception e) {
            distance = Double.POSITIVE_INFINITY;
            e.printStackTrace();
        }
        return distance;
    }


    /**
     * Gets the distance between from and to, possibly returns Double.POSITIVE_INFINITY if the maxDist is exceeded.
     *
     * @param maxDist
     * @param from
     * @param to
     * @return
     */
    private double getPathDistance(double maxDist, DirectedNode from, DirectedNode to) {
        if (new NodeHelper(from).getInnerNodeId() == new NodeHelper(to).getInnerNodeId())
            return 0;
        double defaultValue = -1;
        long key = getHashCode(from, to);
//        System.out.println("" + from + "," + to + "," + key);
        double pathDistance = pathDistanceMap.getOrDefault(key, defaultValue);
//        double pathDistance = defaultValue;
//        double pathDistance = defaultValue;
        if (pathDistance == defaultValue) {
            pathDistance = 0;
            Path path = queryShortestPath(from, to, maxDist);
            if (path != null && path.isValid()) {
                for (Object e : path.getEdges()) {
                    pathDistance += new EdgeHelper((Edge) e).getLength();
                }
                pathDistanceMap.put(key, pathDistance);
            } else {
                pathDistance = Double.POSITIVE_INFINITY;
            }
        }
        return pathDistance;
    }

    /**
     * Gets the hashcode give source and destination node, the key is intended to used as the key of the hashmap.
     *
     * @param from
     * @param to
     * @return
     */
    private static long getHashCode(Node from, Node to) {
//        long base = 2147483647L;
        long fromId = new NodeHelper(from).getInnerNodeId(), toId = new NodeHelper(to).getInnerNodeId();
//        return Long.hashCode(fromId) ^ Long.hashCode(toId);
        return ((long) fromId << 32) + toId;
//        return new Random().nextLong();
    }


    /**
     * Caches the shortest distance between adjacent nodes with distance lower than radius.
     * This is done by using dijkstra in each node.
     *
     * @param radius
     */
    public void cacheShortestDistances(double radius) {
        BoundedAStarSPFinder finder = new BoundedAStarSPFinder(this);
        int count = 0, totalCount = nodeMap.values().size();
        //  double range = 100; // 500 m
        for (Node from : nodeMap.values()) {
            count += 1;
            List<Pair<Node, Double>> result = finder.rangeQuery(from, radius);
            for (Pair<Node, Double> item : result) {
                long key = getHashCode(from, item.getLeft());
                pathDistanceMap.put(key, item.getRight());
            }
            if (count % 10000 == 0) {
                System.out.println(String.format("%2f %%", count * 100.0 / totalCount));
            }
        }
    }

    /**
     * Retrieves all the nodes connected to source using BSF search.
     *
     * @param source
     * @return
     */
    public HashSet<Node> getConnectedNodes(Node source) {
        HashSet<Node> visitedNodes = new HashSet<>();
        Queue<Node> candidates = new ArrayDeque<>();
        candidates.offer(source);
        visitedNodes.add(source);
        while (!candidates.isEmpty()) {
            DirectedNode currentNode = (DirectedNode) candidates.poll();
            for (DirectedEdge edge : (List<DirectedEdge>) currentNode.getOutEdges()) {
                DirectedNode outNode = edge.getOutNode();
                if (!visitedNodes.contains(outNode)) {
                    candidates.offer(outNode);
                    visitedNodes.add(outNode);
                }
            }
            for (DirectedEdge edge : (List<DirectedEdge>) currentNode.getInEdges()) {
                DirectedNode inNode = edge.getInNode();
                if (!visitedNodes.contains(inNode)) {
                    candidates.offer(inNode);
                    visitedNodes.add(inNode);
                }
            }
        }
        return visitedNodes;
    }

}
