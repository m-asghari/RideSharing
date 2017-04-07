package edu.usc.infolab.geo.semanticroute.servlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vividsolutions.jts.io.ParseException;
import edu.usc.infolab.geo.model.IndexedDirectedGraph;
import edu.usc.infolab.geo.semanticroute.model.Route;
import edu.usc.infolab.geo.semanticroute.util.RouteHelper;
import edu.usc.infolab.geo.semanticroute.util.RoutingHelper;
import edu.usc.infolab.geo.util.MapDataHelper;
import org.opengis.referencing.operation.TransformException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by yaguang on 6/27/16.
 */
public class GetUserRoutes extends HttpServlet {

    private IndexedDirectedGraph getGraph() {
        IndexedDirectedGraph graph = null;
        try {
            graph = MapDataHelper.getLAGraph();
        } catch (IOException | TransformException | ParseException e) {
            e.printStackTrace();
        }
        return graph;
    }


    /**
     * Returns a map of user' know route. key: routeId, value: route in json format.
     *
     * @param request
     * @param response
     * @throws IOException
     */
    private void doRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject jsonRequest = new JsonParser().parse(request.getParameter("request")).getAsJsonObject();
        JsonObject jsonResponse = new JsonObject();
        int userId = jsonRequest.get("userId").getAsInt();
        RoutingHelper routingHelper = new RoutingHelper(getGraph());
        try {
            List<Route> routes = RouteHelper.getUserRoutes(userId, getGraph());
            JsonArray routeJsonArray = new JsonArray();
            for (Route route : routes) {
                JsonObject jsonRoute = routingHelper.routeToJsonObject(route);
                routeJsonArray.add(jsonRoute);
            }
            jsonResponse.add("routes", routeJsonArray);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        jsonResponse.add("request", jsonRequest);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
        response.flushBuffer();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doRequest(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doRequest(request, response);
    }
}
