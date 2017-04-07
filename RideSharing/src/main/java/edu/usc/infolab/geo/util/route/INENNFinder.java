package edu.usc.infolab.geo.util.route;

import edu.usc.infolab.geo.model.EdgeAttachment;
import edu.usc.infolab.geo.model.IndexedDirectedGraph;
import edu.usc.infolab.geo.util.EdgeAttachmentHelper;
import edu.usc.infolab.geo.util.EdgeHelper;
import edu.usc.infolab.geo.util.WGS2MetricTransformer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.geotools.graph.structure.DirectedEdge;
import org.geotools.graph.structure.DirectedNode;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.opengis.referencing.operation.TransformException;

import java.util.*;
import java.util.Map.Entry;

/**
 * Created by yaguang on 1/27/16.
 */
public class INENNFinder {
    IndexedDirectedGraph graph = null;

    public INENNFinder(IndexedDirectedGraph graph) {
        this.graph = graph;
    }

    /**
     * Finds top-K nearest neighbors from center based on road network distance.
     *
     * @param center
     * @param K
     * @param maxDist
     * @return
     */
    public List<Entry<EdgeAttachment, Double>> queryNearestNeighbors(
            EdgeAttachment center, int K, double maxDist) throws TransformException {
        return queryNearestNeighbors(center, K, maxDist, false);
    }

    public List<Entry<EdgeAttachment, Double>> queryNearestNeighbors(
            EdgeAttachment center, int K, double maxDist, boolean reverse) throws TransformException {
        return queryNearestNeighbors(center, K, maxDist, reverse, center.getType());
    }


    /**
     * Finds top-K nearest neighbors from center based on road network distance.
     *
     * @param center  edge attachment, e.g., sensor, accident.
     * @param K       number of near neighbor to find.
     * @param maxDist maximum distance.
     * @param reverse reverse the edge direction.
     * @return
     */
    public List<Entry<EdgeAttachment, Double>> queryNearestNeighbors(
            EdgeAttachment center, int K, double maxDist, boolean reverse, int attachmentType) throws TransformException {
        HashSet<Integer> attachmentTypes = new HashSet<>();
        attachmentTypes.add(attachmentType);
        return queryNearestNeighbors(center, K, maxDist, reverse, attachmentTypes);
    }

    /**
     * Finds top-K nearest neighbors from center based on road network distance.
     *
     * @param center  edge attachment, e.g., sensor, accident.
     * @param K       number of near neighbor to find.
     * @param maxDist maximum distance.
     * @param reverse reverse the edge direction.
     * @return top-K nearest neighbors sorted by distance ascending.
     */
    public List<Entry<EdgeAttachment, Double>> queryNearestNeighbors(
            EdgeAttachment center, int K, double maxDist, boolean reverse, HashSet<Integer> attachmentTypes) throws TransformException {
        List<Entry<EdgeAttachment, Double>> result = new ArrayList<>();
        PriorityQueue<INENode> openQueue = new PriorityQueue<INENode>();
        HashMap<Node, INENode> nodeMap = new HashMap<Node, INENode>();
        WGS2MetricTransformer transformer = graph.getTransformer();
        // Order by value descending.
        PriorityQueue<Entry<EdgeAttachment, Double>> topK =
                new PriorityQueue<>((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        // Constructs initial INENode.
        EdgeAttachmentHelper edgeAttachmentHelper = new EdgeAttachmentHelper(graph);
        // Coordinate projection = transformer.fromWGS84(edgeAttachmentHelper.getProjectionToEdge(center));
        EdgeHelper eh = new EdgeHelper(center.getEdge());
        double initialCost = 0;
        if (reverse) {
            initialCost = edgeAttachmentHelper.getProjectionDistanceFromStart(center);
        } else {
            initialCost = edgeAttachmentHelper.getProjectionDistanceToEnd(center);
        }
        INENode sourceINENode = new INENode(null, eh.getEndNode(), initialCost);
        openQueue.add(sourceINENode);
        // Updates node _map.
        nodeMap.put(sourceINENode.getNode(), sourceINENode);
        while (!openQueue.isEmpty()) {
            INENode curINENode = openQueue.poll();
            curINENode.close();
            Node curNode = curINENode.getNode();
            if (curINENode.getCost() > maxDist) {
                break;
            }
            // The current cost is larger than the Kth nearest neighbor.
            if (topK.size() == K && curINENode.getCost() >= topK.peek().getValue()) {
                break;
            }
            List<DirectedEdge> edges = null;
            if (reverse) {
                edges = ((DirectedNode) curNode).getInEdges();
            } else {
                edges = ((DirectedNode) curNode).getOutEdges();
            }
            for (DirectedEdge edge : edges) {
                EdgeHelper tempEh = new EdgeHelper(edge);
                Node nextNode = edge.getOutNode();
                if (reverse) {
                    nextNode = edge.getInNode();
                }
                List<EdgeAttachment> attachments = tempEh.getAttachments(attachmentTypes);
                // Iterates through attachments and insert into the result.
                for (EdgeAttachment attachment : attachments) {
                    double cost = curINENode.getCost();
                    if (reverse) {
                        cost += edgeAttachmentHelper.getProjectionDistanceToEnd(attachment);
                    } else {
                        cost += edgeAttachmentHelper.getProjectionDistanceFromStart(attachment);
                    }
                    if (topK.size() < K) {
                        topK.add(new ImmutablePair<>(attachment, cost));
                    } else if (cost < topK.peek().getValue()) {
                        topK.poll();
                        topK.add(new ImmutablePair<>(attachment, cost));
                    }
                }
                if (nodeMap.containsKey(nextNode)) {
                    // This node has been visited before.
                    INENode nextINENode = nodeMap.get(nextNode);
                    if (!nextINENode.isClosed()) {
                        double newCost = tempEh.getLength() + curINENode.getCost();
                        if (newCost < nextINENode.getCost()) {
                            openQueue.remove(nextINENode);
                            nextINENode = new INENode(curINENode, nextNode, newCost);
                            openQueue.add(nextINENode);
                            nodeMap.put(nextNode, nextINENode);
                        }
                    }
                } else {
                    double cost = tempEh.getLength() + curINENode.getCost();
                    INENode nextINENode = new INENode(curINENode, nextNode, cost);
                    openQueue.add(nextINENode);
                    nodeMap.put(nextNode, nextINENode);
                }
            }
        }
        // Constructs result.
        result = new ArrayList<>(topK);
        Collections.sort(result, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));
        return result;
    }

    private class INENode implements Comparable<INENode> {
        private INENode parent;
        private Node node;
        double cost;
        boolean closed;

        public boolean isClosed() {
            return closed;
        }

        public void close() {
            this.closed = true;
        }

        public INENode getParent() {
            return this.parent;
        }

        public Node getNode() {
            return this.node;
        }

        public double getCost() {
            return this.cost;
        }

        public Edge getEdge() {
            return null;
        }


        public INENode(INENode parent, Node node, double cost) {
            this.parent = parent;
            this.node = node;
            this.cost = cost;
            this.closed = false;
        }

        public int compareTo(INENode o) {
            double diff = this.getCost() - o.getCost();
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
            return String.format("nid:%s, cost:%f, h:%f, %f, %s", this.node.toString(),
                    this.getCost(), this.closed);
        }
    }
}
