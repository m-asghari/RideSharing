/**
 *
 */
package edu.usc.infolab.geo.semanticroute.util;

import edu.usc.infolab.geo.model.IndexedDirectedGraph;
import edu.usc.infolab.geo.semanticroute.model.Route;
import edu.usc.infolab.geo.semanticroute.model.Route.RouteType;
import edu.usc.infolab.geo.util.EdgeHelper;
import org.geotools.graph.structure.DirectedEdge;
import org.geotools.graph.structure.DirectedNode;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;

import java.util.*;

/**
 * Retrieves routes that contains a node.
 *
 * @author Yaguang
 */
public class ExactRouteRetriever implements RouteRetriever {

    private IndexedDirectedGraph graph = null;
    private HashMap<Long, HashSet<Route>> edgeIndex = new HashMap<Long, HashSet<Route>>();

    public ExactRouteRetriever(IndexedDirectedGraph graph) {
        this.graph = graph;
    }

    public void addRoute(List<Route> routes) {
        for (Route route : routes) {
            for (Edge edge : route.getEdges()) {
                long edgeId = graph.getInnerEdgeId(edge);
                HashSet<Route> candidates = null;
                if (!edgeIndex.containsKey(edgeId)) {
                    candidates = new HashSet<Route>();
                    edgeIndex.put(edgeId, candidates);
                } else {
                    candidates = edgeIndex.get(edgeId);
                }
                candidates.add(route);
            }
        }
    }

    /**
     * Gets turn-by-turn routes that contains the edge. <br/>
     * Turn-by-turn route: adjacent routes with same way_id.
     *
     * @param originEdge
     * @return
     */
    @SuppressWarnings("unchecked")
    Route getNaturalRoute(Edge originEdge) {
        EdgeHelper eh = new EdgeHelper(originEdge);
        String routeId = "" + eh.getWayId();
        String routeName = eh.getStreetName();
        List<Node> nodes = new ArrayList<Node>();
        List<DirectedEdge> edges = new ArrayList<DirectedEdge>();
        HashSet<DirectedEdge> visitedEdges = new HashSet<DirectedEdge>();
        // Gets previous nodes
        DirectedEdge inEdge = (DirectedEdge) originEdge, outEdge = (DirectedEdge) originEdge;
        while (inEdge != null) {
            DirectedNode inNode = inEdge.getInNode();
            List<DirectedEdge> inEdges = inNode.getInEdges();
            inEdge = null;
            for (DirectedEdge e : inEdges) {
                if (visitedEdges.contains(e))
                    break;
                if (isSameTBTRoute(e, originEdge)) {
                    inEdge = e;
                    edges.add(inEdge);
                    visitedEdges.add(inEdge);
                    break;
                }
            }
        }
        Collections.reverse(edges);
        edges.add((DirectedEdge) originEdge);
        while (outEdge != null) {
            DirectedNode outNode = outEdge.getOutNode();
            List<DirectedEdge> outEdges = outNode.getOutEdges();
            outEdge = null;
            for (DirectedEdge e : outEdges) {
                if (visitedEdges.contains(e))
                    break;
                if (isSameTBTRoute(e, originEdge)) {
                    outEdge = e;
                    edges.add(outEdge);
                    visitedEdges.add(outEdge);
                    break;
                }
            }
        }
        // Builds the routes
        for (int i = 0; i < edges.size(); ++i) {
            nodes.add(edges.get(i).getInNode());
        }
        nodes.add(edges.get(edges.size() - 1).getOutNode());
        String routeSummary = eh.getStreetName();
        Route route = new Route(nodes, routeId, RouteType.NATURAL, 0.0, routeSummary);
        return route;
    }

    boolean isSameTBTRoute(Edge e1, Edge e2) {
        EdgeHelper eh1 = new EdgeHelper(e1), eh2 = new EdgeHelper(e2);
        if (eh1.getEdgeIdDirection() != eh2.getEdgeIdDirection()) {
            return false;
        }
        return eh1.getWayId() == eh2.getWayId();
    }

    /**
     * Retrieves candidate routes.
     *
     * @see edu.usc.infolab.geo.semanticroute.util.RouteRetriever#queryRoutes(Edge)
     */
    public HashSet<Route> queryRoutes(Edge e) {
        // Gets knowledge routes
        long edgeId = graph.getInnerEdgeId(e);
        HashSet<Route> result = edgeIndex.getOrDefault(edgeId, new HashSet<Route>());
        result.add(getNaturalRoute(e));
        return result;
    }
}
