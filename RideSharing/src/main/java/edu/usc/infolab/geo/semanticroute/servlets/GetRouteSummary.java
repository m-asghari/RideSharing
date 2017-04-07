package edu.usc.infolab.geo.semanticroute.servlets;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.io.ParseException;
import edu.usc.infolab.geo.model.IndexedDirectedGraph;
import edu.usc.infolab.geo.semanticroute.model.RLocation;
import edu.usc.infolab.geo.semanticroute.model.RRoute;
import edu.usc.infolab.geo.semanticroute.model.Route;
import edu.usc.infolab.geo.semanticroute.model.RoutePartition;
import edu.usc.infolab.geo.semanticroute.util.*;
import edu.usc.infolab.geo.util.MapDataHelper;
import edu.usc.infolab.geo.util.Utility;
import edu.usc.infolab.geo.util.mapmatching.HMMRouteCalibrator;
import edu.usc.infolab.geo.util.mapmatching.RouteCalibrator;
import org.apache.commons.io.FilenameUtils;
import org.geotools.graph.path.Walk;
import org.geotools.graph.structure.DirectedEdge;
import org.geotools.graph.structure.Node;
import org.opengis.referencing.operation.TransformException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Servlet to get route summary of a route.
 * Request format:
 * <pre>
 * request: {
 *     routes: {
 *         points: [latlng1, latlng2, ...]
 *     }
 * }
 * </pre>
 * <p>
 * Response format:
 * <p>
 * <pre>
 * {
 *     request: request,
 *     routes: [
 *     {
 *      segments: [
 *      {
 *          steps: [{
 *              ...
 *          }]
 *
 *      }]
 *
 *     }],
 *
 * }
 * </pre>
 * Created by yaguang on 3/27/16.
 */
public class GetRouteSummary extends HttpServlet {

    private static IndexedDirectedGraph _graph = null;
    private static Collection<Route> _naturalRoutes = null;
    private static HashMap<Integer, Collection<Route>> _knownRoutes = new HashMap<>();
    private static HashMap<Integer, RoutePartitioner> _routePartitioners = new HashMap<>();

    Logger logger = Logger.getLogger(GetRouteSummary.class.getName());

    private IndexedDirectedGraph getGraph() {
        if (_graph == null) {
            try {
                logger.info("Creating Graph...");
                _graph = MapDataHelper.getLAGraph();
                logger.info("Caching shortest path...");
                _graph.cacheShortestDistances(200);
            } catch (IOException | ParseException | TransformException e) {
                e.printStackTrace();
            }
        }
        return _graph;
    }

    private Collection<Route> getNaturalRoutes() {
        if (_naturalRoutes == null) {
            NaturalRouteConstructor naturalRouteConstructor = new NaturalRouteConstructor(getGraph());
            _naturalRoutes = naturalRouteConstructor.constructRoutes();
        }
        return _naturalRoutes;
    }

    private Collection<Route> getKnownRoutes(int userId) {
        Collection<Route> routes = _knownRoutes.getOrDefault(userId, null);
        if (routes == null) {
            // Gets routes.
            try {
                routes = RouteHelper.getUserRoutes(userId, getGraph());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            _knownRoutes.put(userId, routes);
        }
        return routes;
    }

    private RoutePartitioner getRoutePartitioner(int userId) {
        RoutePartitioner routePartitioner = _routePartitioners.getOrDefault(userId, null);
        if (routePartitioner == null) {
            BasicRouteRetriever routeRetriever = new BasicRouteRetriever(getGraph());
            routeRetriever.addRoutes(getNaturalRoutes());
            routeRetriever.addRoutes(getKnownRoutes(userId));
            routePartitioner = new BasicRoutePartitioner(routeRetriever);
            _routePartitioners.put(userId, routePartitioner);
        }
        return routePartitioner;
    }


    private Route buildRoute(long[] edgeIds, String routeId, Route.RouteType routeType, double score) {
        List<Node> nodes = new ArrayList<Node>();
        String name = "";
        for (long edgeId : edgeIds) {
            DirectedEdge edge = (DirectedEdge) getGraph().getEdge(edgeId);
            nodes.add(edge.getInNode());
        }
        nodes.add(((DirectedEdge) getGraph().getEdge(edgeIds[edgeIds.length - 1])).getOutNode());
        return new Route(nodes, routeId, routeType, score);
    }

    private JsonElement getExampleResponse() {
        JsonObject response = new JsonObject();
        long[] edgeIds = new long[]{1617160710003L, 1617160710004L, 1617160710005L, 1236742310001L,
                1728700820001L, 1219405950001L, 1219405950002L, 1219405950003L, 1219405950004L,
                1219405950005L, 1219405950006L, 1219405950007L, 1219405940001L, 1219405940002L,
                1219405940003L, 376846720001L, 376846930001L, 376846930002L, 376846930003L};
        Route route = buildRoute(edgeIds, "1", Route.RouteType.KNOWN, 1.0);
        RouteRetriever routeRetriever = new ExactRouteRetriever(getGraph());
        BasicRoutePartitioner partitioner = new BasicRoutePartitioner(routeRetriever);
        RoutePartition rp = partitioner.partition(route);
        RoutingHelper routingHelper = new RoutingHelper(getGraph());
        RRoute rRoute = routingHelper.generateRoute(rp);
        JsonArray routes = new JsonArray();
        routes.add(rRoute.toJsonElement());
        response.add("routes", routes);
        return response;
    }

    private JsonObject getDebugResponse() {
        String filename = FilenameUtils.concat(Utility.getProperty("demo_data_dir"), "sample_response.json");
        JsonObject debugResponse = new JsonObject();
        try {
            debugResponse = new JsonParser().parse(new FileReader(filename)).getAsJsonObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return debugResponse;
    }

    private JsonObject handleQuery(JsonObject request) throws TransformException {
        if (request.get("debug").getAsBoolean()) {
            return getDebugResponse();
        }
        long startTime = System.currentTimeMillis();
        JsonObject response = new JsonObject();
        Gson gson = new Gson();
        // Gets List<Coordinate> from query.
        int userId = request.get("userId").getAsInt();
        List<RLocation> locations = gson.fromJson(request.getAsJsonObject("route").getAsJsonArray("points"),
                new TypeToken<List<RLocation>>() {
                }.getType());
        List<Coordinate> coordinates = locations.stream().map(RLocation::toCoordinate).collect(Collectors.toList());
        coordinates = getGraph().getTransformer().fromWGS84(coordinates);
        logger.info("Parsing: " + (System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();

        // Matches coordinates to the road network as a walk.
        RouteCalibrator routeCalibrator = new HMMRouteCalibrator(getGraph());
        Walk walk = routeCalibrator.calibrate(coordinates);
        logger.info("Mapmatching: " + (System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();

        // Builds a route query based on walk.
        String routeId = "queryRoute";
        Route queryRoute = new Route(walk, routeId, Route.RouteType.QUERY);

        // Partitions the route.
        RoutePartition routePartition = getRoutePartitioner(userId).partition(queryRoute);
        logger.info("Route Parititon: " + (System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();

        // Converts the route into RRoute.
        RoutingHelper routingHelper = new RoutingHelper(getGraph());
        RRoute rRoute = routingHelper.generateRoute(routePartition);
        logger.info("Generating RRoute: " + (System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();

        // Sends the response.
        JsonArray routes = new JsonArray();
        routes.add(rRoute.toJsonElement());
        response.add("routes", routes);
        response.add("request", request);
        String source = GoogleMapsHelper.reverseGeocoding(locations.get(0).toLatLng());
        String destination = GoogleMapsHelper.reverseGeocoding(locations.get(locations.size() - 1).toLatLng());
        response.addProperty("source", source);
        response.addProperty("destination", destination);
        return response;
    }

    private void doRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject jsonRequest = new JsonParser().parse(request.getParameter("request")).getAsJsonObject();
        JsonObject jsonResponse = new JsonObject();
        try {
            jsonResponse = handleQuery(jsonRequest);
        } catch (TransformException e) {
            e.printStackTrace();
        }
        String jsonString = jsonResponse.toString();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonString);
        response.flushBuffer();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doRequest(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doRequest(request, response);
    }


}
