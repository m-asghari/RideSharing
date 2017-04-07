/**
 *
 */
package edu.usc.infolab.geo.semanticroute.util;

import edu.usc.infolab.geo.model.IndexedDirectedGraph;
import edu.usc.infolab.geo.semanticroute.model.Route;
import org.geotools.graph.structure.Edge;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Retrieves routes that contains a node.
 *
 * @author Yaguang
 */
public class BasicRouteRetriever implements RouteRetriever {

    private IndexedDirectedGraph graph = null;
    private HashMap<Long, HashSet<Route>> edgeIndex = new HashMap<Long, HashSet<Route>>();

    public BasicRouteRetriever(IndexedDirectedGraph graph) {
        this.graph = graph;
    }

    public void addRoutes(Collection<Route> routes) {
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
     * Retrieves candidate routes.
     *
     * @see RouteRetriever#queryRoutes(Edge)
     */
    public HashSet<Route> queryRoutes(Edge e) {
        // Gets knowledge routes
        long edgeId = graph.getInnerEdgeId(e);
        HashSet<Route> result = edgeIndex.getOrDefault(edgeId, new HashSet<Route>());
        return result;
    }
}
