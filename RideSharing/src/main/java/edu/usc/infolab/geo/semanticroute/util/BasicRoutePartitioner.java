package edu.usc.infolab.geo.semanticroute.util;

import edu.usc.infolab.geo.semanticroute.model.Route;
import edu.usc.infolab.geo.semanticroute.model.RoutePartition;
import edu.usc.infolab.geo.semanticroute.model.RouteSegment;
import org.geotools.graph.structure.Edge;

import java.util.*;

public class BasicRoutePartitioner implements RoutePartitioner {
    /**
     * Structure used during the DP process. <br/>
     * Note: this class has a natural ordering that is inconsistent with equals
     *
     * @author Yaguang
     */
    private class DPNode implements Comparable<DPNode> {
        private final RouteSegment routeSegment;
        private final int n;
        private final double score;
        private final DPNode parent;

        public RouteSegment getRouteSegment() {
            return this.routeSegment;
        }

        public int getNumber() {
            return n;
        }

        public double getScore() {
            return score;
        }

        public DPNode getParent() {
            return parent;
        }

        public DPNode(DPNode parent, RouteSegment segment, int n, double score) {
            this.parent = parent;
            this.routeSegment = segment;
            this.n = n;
            this.score = score;
        }

        public int compareTo(DPNode o) {
            if (o == null || this.score > o.score) {
                return 1;
            } else if (this.score < o.score) {
                return -1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return String.format("RS:{%s}, N:{%d}, Score:{%.1f}", this.routeSegment, n, score);
        }

    }

    private Route route = null;
    private RouteRetriever routeRetriever = null;
    private double lambda = 0.1;

    private double getScore(RouteSegment rs) {
        return RouteSegmentEstimator.getScore(rs);
    }

    public void setLambda(double value) {
        this.lambda = value;
    }

    public BasicRoutePartitioner(RouteRetriever routeRetriever) {
        this.routeRetriever = routeRetriever;
    }

    private HashSet<Route> getCandidateRoutes(Edge e) {
        return routeRetriever.queryRoutes(e);
    }

    /**
     * Partitions a route into several route segments.
     *
     * @param route the route to be partitioned.
     */
    public RoutePartition partition(Route route) {
        this.route = route;
        return doPartition();
    }


    private RoutePartition doPartition() {
        final int N = route.size();
        List<Edge> edges = route.getEdges();
        @SuppressWarnings("unchecked")
        HashMap<Route, List<DPNode>>[] D = (HashMap<Route, List<DPNode>>[]) new HashMap[N];
        for (int i = 0; i < N; ++i) {
            D[i] = new HashMap<>();
        }
        for (int i = 1; i < N; ++i) {
            Edge currentEdge = edges.get(i - 1);
            HashSet<Route> candRoutes = getCandidateRoutes(currentEdge);
            for (Route candRoute : candRoutes) {
                List<DPNode> dpNodes = new ArrayList<DPNode>();
                if (D[i - 1].containsKey(candRoute)) {
                    // Extends the last route segment.
                    List<DPNode> prevNodes = D[i - 1].get(candRoute);
                    for (DPNode dpNode : prevNodes) {
                        RouteSegment prevRs = dpNode.getRouteSegment();
                        RouteSegment rs =
                                new RouteSegment(prevRs.getRoute(), prevRs.getStartInd(), prevRs.getEndInd() + 1);
                        double score = dpNode.getScore() - getScore(prevRs) + getScore(rs);
                        DPNode newDpNode = new DPNode(dpNode.getParent(), rs, dpNode.getNumber(), score);
                        dpNodes.add(newDpNode);
                    }
                }
                // Finds the route (other than the current route) that maximizes the score.
                DPNode maxNode = null;
                for (Map.Entry<Route, List<DPNode>> entry : D[i - 1].entrySet()) {
                    if (entry.getKey().equals(candRoute))
                        continue;
                    for (DPNode node : entry.getValue()) {
                        if (maxNode == null || node.compareTo(maxNode) > 0) {
                            maxNode = node;
                        }
                    }
                }
                if (maxNode != null || D[i - 1].isEmpty()) {
                    // The route in D[i-1] is equal to candRoute, then builds a new DPNode.
                    int endInd = candRoute.getNodes().indexOf(route.get(i));
                    RouteSegment rs = new RouteSegment(candRoute, endInd - 1, endInd);
                    double score = getScore(rs) - lambda;
                    int number = 1;
                    if (maxNode != null) {
                        score += maxNode.getScore();
                        number += maxNode.getNumber();
                    }
                    DPNode newNode = new DPNode(maxNode, rs, number, score);
                    dpNodes.add(newNode);
                }

                D[i].put(candRoute, dpNodes);
            }
        }
        // Finds the maximum one in D[N - 1].
        DPNode maxNode = null;
        for (Route key : D[N - 1].keySet()) {
            for (DPNode node : D[N - 1].get(key)) {
                if (node.compareTo(maxNode) > 0) {
                    maxNode = node;
                }
            }
        }
        // Backtracking to find the best partition.
        return backtracking(maxNode);
    }

    private RoutePartition backtracking(DPNode maxNode) {
        List<RouteSegment> segments = new ArrayList<RouteSegment>();
        while (maxNode != null) {
            segments.add(maxNode.getRouteSegment());
            maxNode = maxNode.getParent();
        }
        Collections.reverse(segments);
        return new RoutePartition(route, segments);
    }
}
