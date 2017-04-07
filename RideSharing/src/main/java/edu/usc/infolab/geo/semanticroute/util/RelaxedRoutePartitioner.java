package edu.usc.infolab.geo.semanticroute.util;

import edu.usc.infolab.geo.semanticroute.model.Route;
import edu.usc.infolab.geo.semanticroute.model.RoutePartition;
import edu.usc.infolab.geo.semanticroute.model.RouteSegment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Relaxed Optimal Partitioner.
 *
 * @author Yaguang
 */
public class RelaxedRoutePartitioner implements RoutePartitioner {
    private RouteRetriever routeRetriever = null;
    private double maxErrorDist = 1000;  // 1km.

    private Route route = null;

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


        @Override
        public String toString() {
            return String.format("RS:{%s}, N:{%d}, Score:{%.1f}", this.routeSegment, n, score);
        }

        @Override
        public int compareTo(DPNode o) {
            if (o == null || this.score > o.score) {
                return 1;
            } else if (this.score < o.score) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public RelaxedRoutePartitioner(RouteRetriever routeRetriever, double maxErrorDist) {
        this.routeRetriever = routeRetriever;
        this.maxErrorDist = maxErrorDist;
    }

    public RoutePartition partition(Route route) {
        this.route = route;
        return doPartition();
    }

    private RoutePartition doPartition() {
        DPNode maxNode = null;
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
