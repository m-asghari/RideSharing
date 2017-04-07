package edu.usc.infolab.geo.util.route;

import edu.usc.infolab.geo.util.EdgeHelper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.DirectedEdge;
import org.geotools.graph.structure.DirectedNode;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;

import java.util.*;

public class BoundedAStarSPFinder {
    @SuppressWarnings("unused")
    private final Graph graph;
    private final AStarHeuristic heuristic;

    public BoundedAStarSPFinder(Graph graph) {
        this.graph = graph;
        this.heuristic = new EuclideanAStarHeuristic();
    }

    public BoundedAStarSPFinder(Graph graph, AStarHeuristic heuristic) {
        this.graph = graph;
        this.heuristic = heuristic;
    }


    public List<Pair<Node, Double>> rangeQuery(Node source, double bound) {
        List<Pair<Node, Double>> result = new ArrayList<>();
        PriorityQueue<AStarNode> openQueue = new PriorityQueue<>();
        HashMap<Node, AStarNode> nodeMap = new HashMap<>();
        // Puts the first node into the priority queue.
        AStarNode sourceASNode =
                new AStarNode(null, source, 0, 0);
        openQueue.add(sourceASNode);
        nodeMap.put(source, sourceASNode);
        while (!openQueue.isEmpty()) {
            AStarNode curASNode = openQueue.poll();
            Node curNode = curASNode.getNode();
            curASNode.close();
            result.add(new ImmutablePair<>(curASNode.getNode(), curASNode.getG()));
            if (curASNode.getEstimatedTotalCost() > bound) {
                break;
            }
            // Gets out nodes.
            List<DirectedEdge> edges = ((DirectedNode) curNode).getOutEdges();
            for (DirectedEdge edge : edges) {
                Node nextNode = edge.getOutNode();
                EdgeHelper el = new EdgeHelper(edge);
                if (nodeMap.containsKey(nextNode)) {
                    AStarNode nextASNode = nodeMap.get(nextNode);
                    if (!nextASNode.isClosed()) {
                        // This node has been visited before but not closed yet.
                        double newG = el.getLength() + curASNode.getG();
                        if (el.getLength() + curASNode.getG() < nextASNode.getG()) {
                            openQueue.remove(nextASNode);
                            nextASNode = new AStarNode(curASNode, nextNode, newG, nextASNode.getH());
                            openQueue.add(nextASNode);
                            nodeMap.put(nextNode, nextASNode);
                        }
                    }
                } else {
                    double newG = el.getLength() + curASNode.getG();
                    double h = 0;
                    AStarNode nextASNode = new AStarNode(curASNode, nextNode, newG, h);
                    openQueue.add(nextASNode);
                    nodeMap.put(nextNode, nextASNode);
                }
            }
        }
        return result;
    }


    /**
     * Gets the shortest path between two nodes with bound. The searching process will stop once the
     * estimate total cost reaches the bound.
     *
     * @param source
     * @param destination
     * @param bound
     * @return a path or null if the path does not exist.
     */
    @SuppressWarnings("unchecked")
    public Path getPath(Node source, Node destination, double bound) {
        PathWithStat pathWithStat = getPathWithStat(source, destination, bound);
        return pathWithStat.getPath();
    }

    /**
     * Gets the shortest path between two nodes with bound. The searching process will stop once the
     * estimate total cost reaches the bound.
     *
     * @param source
     * @param destination
     * @param bound
     * @return a path or null if the path does not exist.
     */
    @SuppressWarnings("unchecked")
    public PathWithStat getPathWithStat(Node source, Node destination, double bound) {
        PriorityQueue<AStarNode> openQueue = new PriorityQueue<AStarNode>();
        HashMap<Node, AStarNode> nodeMap = new HashMap<Node, AStarNode>();
        // Puts the first node into the priority queue.
        AStarNode sourceASNode =
                new AStarNode(null, source, 0, heuristic.calculate(source, destination));
        openQueue.add(sourceASNode);
        nodeMap.put(source, sourceASNode);
        AStarNode solutionNode = null;
        while (!openQueue.isEmpty()) {
            AStarNode curASNode = openQueue.poll();
            Node curNode = curASNode.getNode();
            curASNode.close();
            if (curNode.equals(destination)) {
                solutionNode = curASNode;
                break;
            }
            if (curASNode.getEstimatedTotalCost() > bound) {
                break;
            }
            // Gets out nodes.
            List<DirectedEdge> edges = ((DirectedNode) curNode).getOutEdges();
            for (DirectedEdge edge : edges) {
                Node nextNode = edge.getOutNode();
                EdgeHelper el = new EdgeHelper(edge);
                if (nodeMap.containsKey(nextNode)) {
                    AStarNode nextASNode = nodeMap.get(nextNode);
                    if (!nextASNode.isClosed()) {
                        // This node has been visited before but not closed yet.
                        double newG = el.getLength() + curASNode.getG();
                        if (el.getLength() + curASNode.getG() < nextASNode.getG()) {
                            openQueue.remove(nextASNode);
                            nextASNode = new AStarNode(curASNode, nextNode, newG, nextASNode.getH());
                            openQueue.add(nextASNode);
                            nodeMap.put(nextNode, nextASNode);
                        }
                    }
                } else {
                    double newG = el.getLength() + curASNode.getG();
                    double h = heuristic.calculate(nextNode, destination);
                    AStarNode nextASNode = new AStarNode(curASNode, nextNode, newG, h);
                    openQueue.add(nextASNode);
                    nodeMap.put(nextNode, nextASNode);
                }
            }
        }
        double distance = Double.MAX_VALUE;
        Path path = null;
        if (solutionNode != null) {
            path = backtracking(solutionNode);
            distance = solutionNode.getEstimatedTotalCost();
        }
        return new PathWithStat(path, distance);
    }

    /**
     * Calculates the shortest path between two nodes.
     *
     * @param source
     * @param destination
     * @return
     */
    public Path getPath(Node source, Node destination) {
        return getPath(source, destination, Double.POSITIVE_INFINITY);
    }

    private Path backtracking(AStarNode asNode) {
        Path path = new Path();
        while (asNode != null) {
            path.add(asNode.getNode());
            asNode = asNode.getParent();
        }
        Collections.reverse(path);
        return path;
    }

    private class AStarNode implements Comparable<AStarNode> {
        private AStarNode parent;
        private Node node;
        double g;
        double h;
        boolean closed;

        public boolean isClosed() {
            return closed;
        }

        public void close() {
            this.closed = true;
        }

        public AStarNode getParent() {
            return this.parent;
        }

        public Node getNode() {
            return this.node;
        }

        public double getG() {
            return this.g;
        }

        public double getH() {
            return this.h;
        }

        /**
         * Gets the estimated total cost from source to destination through this path.
         *
         * @return
         */
        public double getEstimatedTotalCost() {
            return this.g + this.h;
        }


        public AStarNode(AStarNode parent, Node node, double g, double h) {
            this.parent = parent;
            this.node = node;
            this.g = g;
            this.h = h;
            this.closed = false;
        }

        public int compareTo(AStarNode o) {
            double diff = this.getEstimatedTotalCost() - o.getEstimatedTotalCost();
            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return String.format("nid:%s, cost:%f, h:%f, %f, %s", this.node.toString(), this.g, this.h,
                    this.getEstimatedTotalCost(), this.closed);
        }
    }
}
