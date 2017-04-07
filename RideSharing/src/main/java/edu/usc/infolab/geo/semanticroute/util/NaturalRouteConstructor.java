package edu.usc.infolab.geo.semanticroute.util;

import edu.usc.infolab.geo.model.IndexedDirectedGraph;
import edu.usc.infolab.geo.semanticroute.model.Route;
import edu.usc.infolab.geo.util.EdgeHelper;
import org.geotools.graph.structure.DirectedEdge;
import org.geotools.graph.structure.DirectedNode;
import org.geotools.graph.structure.Node;

import java.util.*;

/**
 * Constructs natural routes from road networks by merging adjacent edges with same name and direction.
 * Created by yaguang on 5/7/16.
 */
public class NaturalRouteConstructor {

    IndexedDirectedGraph graph = null;

    public NaturalRouteConstructor(IndexedDirectedGraph graph) {
        this.graph = graph;
    }

    class EdgeComparer implements Comparator<DirectedEdge> {

        @Override
        public int compare(DirectedEdge o1, DirectedEdge o2) {
            EdgeHelper eh1 = new EdgeHelper((DirectedEdge) o1),
                    eh2 = new EdgeHelper((DirectedEdge) o2);
            int result = eh1.getStreetName().compareTo(eh2.getStreetName());
            if (result == 0) {
                result = eh1.getStartCoordinate().compareTo(eh2.getStartCoordinate());
            }
            return result;
        }

    }

    /**
     * Constructs natural routes.
     *
     * @return
     */
    public List<Route> constructRoutes() {
        List<Route> routes = new ArrayList<>();
        PriorityQueue<DirectedEdge> edgeQueue = new PriorityQueue<>(new EdgeComparer());
        edgeQueue.addAll(graph.getEdges());
        HashSet<DirectedEdge> visitedEdges = new HashSet<>();
        int routeNum = 0;
        while (!edgeQueue.isEmpty()) {
            DirectedEdge originEdge = edgeQueue.poll();
            if (visitedEdges.contains(originEdge))
                continue;
            visitedEdges.add(originEdge);
            List<Node> nodes = new ArrayList<Node>();
            List<DirectedEdge> edges = new ArrayList<DirectedEdge>();
            // Gets previous nodes
            DirectedEdge currentEdge = originEdge;
            while (currentEdge != null) {
                DirectedNode inNode = currentEdge.getInNode();
                List<DirectedEdge> inEdges = inNode.getInEdges();
                DirectedEdge nextEdge = null;
                for (DirectedEdge e : inEdges) {
                    if (visitedEdges.contains(e))
                        continue;
                    if (isSameTBTRoute(e, currentEdge)) {
                        nextEdge = e;
                        break;
                    }
                }
                if (nextEdge != null) {
                    edges.add(nextEdge);
                    visitedEdges.add(nextEdge);
                }
                currentEdge = nextEdge;
            }
            Collections.reverse(edges);
            edges.add((DirectedEdge) originEdge);
            currentEdge = originEdge;
            while (currentEdge != null) {
                DirectedEdge nextEdge = null;
                DirectedNode outNode = currentEdge.getOutNode();
                List<DirectedEdge> outEdges = outNode.getOutEdges();
                for (DirectedEdge e : outEdges) {
                    if (visitedEdges.contains(e))
                        continue;
                    if (isSameTBTRoute(e, currentEdge)) {
                        nextEdge = e;
                        break;
                    }
                }
                if (nextEdge != null) {
                    edges.add(nextEdge);
                    visitedEdges.add(nextEdge);
                }
                currentEdge = nextEdge;
            }
            // Builds the routes
            for (int i = 0; i < edges.size(); ++i) {
                nodes.add(edges.get(i).getInNode());
            }
            nodes.add(edges.get(edges.size() - 1).getOutNode());
            String routeId = "NR_" + routeNum;
            String summary = new EdgeHelper(edges.get(0)).getStreetName();
            Route route = new Route(nodes, routeId, Route.RouteType.NATURAL, 0.0, summary);
            routes.add(route);
            ++routeNum;
        }
        return routes;
    }

    private boolean isSameTBTRoute(DirectedEdge e, DirectedEdge originEdge) {
        EdgeHelper eh1 = new EdgeHelper(e), eh2 = new EdgeHelper(originEdge);
        boolean result = false;
        double cosineThreshold = 0.2;
        if (eh1.getStreetName().trim().equals(eh2.getStreetName().trim())) {
            double cosine = eh1.getCosineAngle(eh2);
            result = cosine >= cosineThreshold;
        }
        return result;
    }
}
