package edu.usc.infolab.geo.semanticroute.model;

import org.geotools.graph.structure.Node;

import java.util.ArrayList;
import java.util.List;

public class RoutePartition {
    final Route route;
    final List<RouteSegment> segments;

    public RoutePartition(Route r) {
        this.route = r;
        segments = new ArrayList<>();
    }

    public RoutePartition(Route r, List<RouteSegment> segments) {
        this.route = r;
        this.segments = segments;
    }

    /**
     * Appends a sourceRoute segment to this partition
     *
     * @param segment
     */
    public void add(RouteSegment segment) {
        this.segments.add(segment);
    }

    /**
     * Gets a copy of the inner sourceRoute segments.
     *
     * @return
     */
    public List<RouteSegment> getSegments() {
        return new ArrayList<RouteSegment>(segments);
    }

    public int size() {
        return segments.size();
    }

    /**
     * Checks if the union of segments equals to related sourceRoute.
     *
     * @return
     */
    public boolean isValid() {
        boolean valid = true;
        List<Node> nodes = new ArrayList<Node>();
        for (RouteSegment segment : segments) {
            for (int i = segment.getStartInd(); i < segment.getEndInd(); ++i) {
                nodes.add(segment.getRoute().get(i));
            }
        }
        RouteSegment lastSegment = segments.get(segments.size() - 1);
        nodes.add(lastSegment.getRoute().get(lastSegment.getEndInd()));
        if (nodes.size() != route.size()) {
            valid = false;
        } else {
            for (int i = 0; i < nodes.size(); ++i) {
                if (!nodes.get(i).equals(route.get(i))) {
                    valid = false;
                    break;
                }
            }
        }
        return valid;
    }

    /**
     * Calculates the percentage of sourceRoute that is represented using knowledge routes.
     *
     * @return
     */
    public double getKnowledgeCoverage() {
        double totalLength = 0;
        double knowledgeLength = 0;
        for (RouteSegment routeSegment : segments) {
            totalLength += routeSegment.getLength();
            if (routeSegment.getRouteType() == Route.RouteType.KNOWN) {
                knowledgeLength += routeSegment.getLength();
            }
        }
        return knowledgeLength / totalLength;
    }

}
