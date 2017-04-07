package edu.usc.infolab.geo.semanticroute.util;

import com.vividsolutions.jts.geom.Coordinate;
import edu.usc.infolab.geo.model.IndexedDirectedGraph;
import edu.usc.infolab.geo.semanticroute.dao.RouteBean;
import edu.usc.infolab.geo.semanticroute.dao.RouteBeanHelper;
import edu.usc.infolab.geo.semanticroute.model.Route;
import edu.usc.infolab.geo.util.EdgeHelper;
import org.geotools.graph.path.Walk;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RouteHelper {
    private RouteHelper() {
    }

    public static Walk getWalk(List<Long> nodeIds, IndexedDirectedGraph graph) {
        Walk walk = new Walk();
        for (Long nodeId : nodeIds) {
            Node node = graph.getNode(nodeId);
            walk.add(node);
        }
        return walk;
    }

    @SuppressWarnings("unchecked")
    public static List<Coordinate> getCoordinateList(Walk walk) {
        List<Coordinate> coords = new ArrayList<Coordinate>();
        for (Edge e : (List<Edge>) walk.getEdges()) {
            EdgeHelper eh = new EdgeHelper(e);
            coords.addAll(eh.getCoordinateList());
        }
        return coords;
    }

    public static List<Coordinate> getCoordinateList(Route route) {
        List<Coordinate> coords = new ArrayList<Coordinate>();
        for (Edge e : (List<Edge>) route.getEdges()) {
            EdgeHelper eh = new EdgeHelper(e);
            coords.addAll(eh.getCoordinateList());
        }
        return coords;
    }

    public static List<Route> getUserRoutes(int userId, IndexedDirectedGraph graph) throws SQLException {
        List<Route> routes = new ArrayList<>();
        RouteBeanHelper rbh = new RouteBeanHelper();
        List<RouteBean> routeBeans = rbh.getUserRoutes(userId);
        for (RouteBean routeBean : routeBeans) {
            Walk walk = RouteHelper.getWalk(routeBean.getNodeIdSequence(), graph);
            double score = routeBean.getScore();
            String summary = routeBean.getMain_road();
            @SuppressWarnings("unchecked")
            Route route = new Route(walk, "K_" + routeBean.getRoute_id(), Route.RouteType.KNOWN, score, summary,
                    routeBean.getFrom_place(), routeBean.getTo_place());
            routes.add(route);
        }
        return routes;
    }
}
