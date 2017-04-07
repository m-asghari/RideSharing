/**
 *
 */
package edu.usc.infolab.geo.semanticroute.model;

import org.geotools.graph.path.Walk;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.opengis.feature.simple.SimpleFeature;

import java.util.List;

/**
 * @author Yaguang
 */
public class RouteSegment {
    private final Route route;

    private final int startInd, endInd;

    private double length = -1;

    /**
     * A segment of a sourceRoute.
     *
     * @param route    the associated sourceRoute.
     * @param startInd start index of the nodes.
     * @param endInd   end index of the nodes.
     */
    public RouteSegment(Route route, int startInd, int endInd) {
        this.route = route;
        if (startInd < 0 || startInd > endInd || endInd > route.size()) {
            throw new IllegalArgumentException(String.format("illegal index: %s,%s", startInd, endInd));
        }
        this.startInd = Math.max(0, startInd);
        this.endInd = Math.min(endInd, route.size() - 1);
    }


    /**
     * Gets the walk of the route segment, i.e., a list of nodes / edges.
     *
     * @return
     */
    public Walk getWalk() {
        List<Node> nodes = route.getNodes().subList(startInd, endInd + 1);
        return new Walk(nodes);
    }

    public Route getRoute() {
        return this.route;
    }

    public Route.RouteType getRouteType() {
        return getRoute().getRouteType();
    }

    /**
     * Checks if two sourceRoute segments belong to the same sourceRoute.
     *
     * @param rs
     * @return
     */
    public boolean shareRoute(RouteSegment rs) {
        boolean result = false;
        if (rs != null && rs.getRoute() != null) {
            result = rs.getRoute().equals(this.route);
        }
        return result;
    }

    public double getLength() {
        if (length == -1) {
            int tempLen = 0;
            List<Edge> edges = route.getEdges();
            for (int i = startInd; i < endInd; ++i) {
                SimpleFeature feature = (SimpleFeature) edges.get(i).getObject();
                tempLen += (Double) feature.getAttribute("length");
            }
            length = tempLen;
        }
        return length;
    }

    public int getStartInd() {
        return this.startInd;
    }

    public int getEndInd() {
        return this.endInd;
    }

    @Override
    public String toString() {
        return String.format("%s,%d,%d", this.route.getRouteId(), startInd, endInd);
    }
}
