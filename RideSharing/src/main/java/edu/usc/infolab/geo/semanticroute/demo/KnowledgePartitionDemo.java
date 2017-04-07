package edu.usc.infolab.geo.semanticroute.demo;

import com.vividsolutions.jts.io.ParseException;
import edu.usc.infolab.geo.model.IndexedDirectedGraph;
import edu.usc.infolab.geo.semanticroute.dao.RouteBean;
import edu.usc.infolab.geo.semanticroute.dao.RouteBeanHelper;
import edu.usc.infolab.geo.semanticroute.model.Route;
import edu.usc.infolab.geo.semanticroute.model.Route.RouteType;
import edu.usc.infolab.geo.semanticroute.model.RoutePartition;
import edu.usc.infolab.geo.semanticroute.util.BasicRoutePartitioner;
import edu.usc.infolab.geo.semanticroute.util.ExactRouteRetriever;
import edu.usc.infolab.geo.semanticroute.util.RouteDescriptionGenerator;
import edu.usc.infolab.geo.semanticroute.util.RouteHelper;
import edu.usc.infolab.geo.util.MapDataHelper;
import org.geotools.graph.path.Walk;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KnowledgePartitionDemo {
    public static void main(String[] args) throws Exception {
        new KnowledgePartitionDemo().doDemo();
    }

    private IndexedDirectedGraph mGraph = null;

    private void doDemo() throws MismatchedDimensionException, SQLException, IOException,
            ParseException, TransformException {
        List<Route> routes = getKnowledgeRoutes();
        IndexedDirectedGraph graph = getGraph();
        for (Route route : routes) {
            List<Route> newRoutes = new ArrayList<Route>(routes);
            newRoutes.remove(route);
            ExactRouteRetriever routeRetriever = new ExactRouteRetriever(graph);
            routeRetriever.addRoute(newRoutes);
            BasicRoutePartitioner partitioner = new BasicRoutePartitioner(routeRetriever);
            RoutePartition partition = partitioner.partition(route);
            RouteDescriptionGenerator rdg = new RouteDescriptionGenerator(partition);
            String description = rdg.getRouteDescription();
            System.out.println("============================");
            System.out.println(route.getRouteId());
            System.out.println(description);
            System.out.println("============================");
        }
    }

    private IndexedDirectedGraph getGraph()
            throws MismatchedDimensionException, IOException, ParseException, TransformException {
        if (mGraph == null) {
            mGraph = MapDataHelper.getLAGraphSmall();
        }
        return mGraph;
    }

    private List<Route> getKnowledgeRoutes() throws SQLException, MismatchedDimensionException,
            IOException, ParseException, TransformException {
        List<Route> routes = new ArrayList<Route>();
        RouteBeanHelper rbh = new RouteBeanHelper();
        List<RouteBean> routeBeans = rbh.getAllRoutes();
        IndexedDirectedGraph graph = getGraph();
        for (RouteBean routeBean : routeBeans) {
            Walk walk = RouteHelper.getWalk(routeBean.getNodeIdSequence(), graph);
            @SuppressWarnings("unchecked")
            Route route = new Route(walk, "K_" + routeBean.getRoute_id(), RouteType.KNOWN, 1.0);
            routes.add(route);
        }
        return routes;
    }
}
