package edu.usc.infolab.geo.semanticroute.model;

import com.vividsolutions.jts.geom.Coordinate;
import edu.usc.infolab.geo.util.EdgeHelper;
import edu.usc.infolab.geo.util.NodeHelper;
import org.geotools.graph.path.Walk;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A sourceRoute represented by a Walk in the road network.
 *
 * @author Yaguang
 */
public class Route {


    public enum RouteType {
        NATURAL, KNOWN, QUERY
    }

    private double length = -1;
    private final double score;
    private final String routeId;
    private final RouteType routeType;
    private final Walk walk;
    /**
     * Summary of the route, e.g., the street name or a description of the route.
     */
    private String summary = "";

    // Optional
    private String fromPlace = "";
    private String toPlace = "";

    /**
     * Version.
     */
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 6675769949510439847L;

    public Route(Collection<Node> nodes, String routeId) {
        this(nodes, routeId, RouteType.NATURAL, 0.0, "", "", "");
    }

    public Route(Collection<Node> nodes, String routeId, RouteType routeType) {
        this(nodes, routeId, routeType, 0.0, "", "", "");
    }

    public Route(Collection<Node> nodes, String routeId, RouteType routeType, double score) {
        this(nodes, routeId, routeType, score, "", "", "");
    }

    public Route(Collection<Node> nodes, String routeId, RouteType routeType, double score, String summary) {
        this(nodes, routeId, routeType, score, summary, "", "");
    }


    public Route(Collection<Node> nodes, String routeId, RouteType routeType, double score, String summary,
                 String fromPlace, String toPlace) {
        this.walk = new Walk(nodes);
        this.routeId = routeId;
        this.routeType = routeType;
        this.score = score;
        this.summary = summary;
        this.fromPlace = fromPlace;
        this.toPlace = toPlace;
    }

    /**
     * Gets score of this sourceRoute.
     *
     * @return
     */
    public double getScore() {
        return this.score;
    }

    @SuppressWarnings("unchecked")
    public double getLength() {
        if (length == -1) {
            double tempLen = 0;
            List<Edge> edges = walk.getEdges();
            for (Edge e : edges) {
                EdgeHelper eh = new EdgeHelper(e);
                tempLen += eh.getLength();
            }
            length = tempLen;
        }
        return length;
    }

    public double getLength(int startInd, int endInd) {
        double tempLen = 0;
        List<Edge> edges = getEdges();
        if (startInd <= endInd && startInd >= 0 && endInd < size()) {
            for (int i = startInd + 1; i < endInd; ++i) {
                tempLen += new EdgeHelper(edges.get(i)).getLength();
            }
        }
        return tempLen;
    }

    public RouteType getRouteType() {
        return this.routeType;
    }

    public String getRouteTypeString() {
        String routeType = "";
        switch (this.routeType) {
            case KNOWN:
                routeType = "known";
                break;
            case NATURAL:
                routeType = "natural";
                break;
            case QUERY:
                routeType = "query";
                break;
        }
        return routeType;
    }

    /**
     * Gets the number of nodes in this sourceRoute.
     *
     * @return
     */
    public int size() {
        return this.walk.size();
    }

    /**
     * Gets the node with certain index.
     *
     * @param index
     * @return
     */
    public Node get(int index) {
        return (Node) this.walk.get(index);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Route) {
            Route route = (Route) other;
            return route.routeType == this.routeType && route.routeId.equals(this.routeId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return routeId.hashCode();
    }

    @SuppressWarnings("unchecked")
    public List<Edge> getEdges() {
        return this.walk.getEdges();
    }

    @SuppressWarnings("unchecked")
    public List<Node> getNodes() {
        return this.walk;
    }

    public String getRouteId() {
        return this.routeId;
    }

    public boolean isValid() {
        return this.walk.isValid();
    }

    @Override
    public String toString() {
        String routeTypeString = "";
        switch (routeType) {
            case KNOWN:
                routeTypeString = "K";
                break;
            case NATURAL:
                routeTypeString = "N";
                break;
            case QUERY:
                routeTypeString = "Q";
                break;
        }
        return String.format("%s,%s,%s,%s->%s,%.1f", routeId, routeTypeString,
                summary, fromPlace, toPlace, score);
    }

    public String getFromPlace() {
        return fromPlace;
    }

    public String getToPlace() {
        return toPlace;
    }


    public String getSummary() {
        return summary;
    }


    public Coordinate getStartCoordinate() {
        return new NodeHelper((Node) this.walk.get(0)).getCoordinate();
    }

    public Coordinate getEndCoordinate() {
        return new NodeHelper((Node) this.walk.get(this.walk.size() - 1)).getCoordinate();
    }

    public List<Coordinate> getCoordinateList() {
        List<Coordinate> coordinates = new ArrayList<>();
        for (Node node : (List<Node>) walk) {
            coordinates.add(new NodeHelper(node).getCoordinate());
        }
        return coordinates;
    }

    public String getNodeListString() {
        List<String> nodeList = new ArrayList<>();
        for (Node node : this.getNodes()) {
            nodeList.add("" + new NodeHelper(node).getInnerNodeId());
        }
        return String.join(",", nodeList);
    }
}
