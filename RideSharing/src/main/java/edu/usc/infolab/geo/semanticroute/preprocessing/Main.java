package edu.usc.infolab.geo.semanticroute.preprocessing;


import com.google.maps.model.LatLng;
import com.vividsolutions.jts.geom.Coordinate;
import edu.usc.infolab.geo.model.IndexedDirectedGraph;
import edu.usc.infolab.geo.semanticroute.dao.PlaceBean;
import edu.usc.infolab.geo.semanticroute.dao.PlaceHelper;
import edu.usc.infolab.geo.semanticroute.dao.RouteBean;
import edu.usc.infolab.geo.semanticroute.dao.RouteBeanHelper;
import edu.usc.infolab.geo.semanticroute.util.GoogleMapsHelper;
import edu.usc.infolab.geo.util.Constants;
import edu.usc.infolab.geo.util.MapDataHelper;
import edu.usc.infolab.geo.util.WGS2MetricTransformer;
import edu.usc.infolab.geo.util.mapmatching.HMMRouteCalibrator;
import edu.usc.infolab.geo.util.mapmatching.RouteCalibrator;
import org.geotools.graph.path.Walk;
import org.geotools.graph.structure.Node;
import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Place;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

public class Main {

    static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {
        // Location centers
        String homePlaceId = "ChIJ7aVxnOTHwoARxKIntFtakKo"; // USC
        //        getNearbyPlaces();
//        getUserRoutes();
//        mapRoutes();
        // getRoutesFromCenterAndWriteDB(homePlaceId);
        // String workPlaceId = "ChIJZQ9c8IW8woARN0gTXFiTqSU"; // UCLA
        // LatLng homeLocation = new LatLng(34.068921, -118.4473698);
        // // LatLng workLocation = new LatLng(34.0223519, -118.2873057);
        // // Get home surrounds
        // String workKey = "work_nearby", homeKey = "home_nearby";
        // // List<PlaceBean> workNearby = getNearby(workLocation);
        // // List<PlaceBean> homeNearby = getNearby(homeLocation);
        // // KVFileStore.set(workKey, workNearby);
        // // KVFileStore.set(homeKey, homeNearby);
        //
        // List<PlaceBean> homeNearby = KVFileStore.getList(homeKey, PlaceBean.class);
        // Places places = new Places();
        // try {
        // int affectedRows = places.addPlaces(homeNearby);
        // System.out.println(affectedRows);
        // } catch (SQLException e) {
        // e.printStackTrace();
        // }
        // for (PlaceBean place : homeNearby) {
        // // System.out.println(place.name);
        // }

    }

    public static void getNearbyPlaces(double radius) throws SQLException {
        String homePlaceId = "ChIJ7aVxnOTHwoARxKIntFtakKo"; // USC
//         getRoutesFromCenterAndWriteDB(homePlaceId);
        String workPlaceId = "ChIJZQ9c8IW8woARN0gTXFiTqSU"; // UCLA
        LatLng homeLocation = new LatLng(34.068921, -118.4473698);
        LatLng workLocation = new LatLng(34.0223519, -118.2873057);
        // Get home surrounds
        String workKey = "work_nearby", homeKey = "home_nearby";
//        List<PlaceBean> workNearby = getNearby(workLocation);
        PlaceHelper placeHelper = new PlaceHelper();
//        double radius = 15000;
        List<String> placeIds = GoogleMapsHelper.getNearbyPlaceId(homeLocation, radius);
        placeIds.addAll(GoogleMapsHelper.getNearbyPlaceId(workLocation, radius));
        HashSet<String> uniquePlaceIds = new HashSet<>(placeIds);
        List<PlaceBean> placeBeanList = new ArrayList<>();
        GooglePlaces client = new GooglePlaces(Constants.GOOGLE_API_KEY);
        for (String placeId : uniquePlaceIds) {
            Place place = client.getPlaceById(placeId);
            placeBeanList.add(GoogleMapsHelper.placeToPlaceBean(place));
        }
        try {
            int affectedRows = placeHelper.addPlaces(placeBeanList);
            System.out.println(affectedRows);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (PlaceBean place : placeBeanList) {
            System.out.println(place.getName());
        }
    }

    public static void mapRoutes() {
        RouteBeanHelper rh = new RouteBeanHelper();
        WGS2MetricTransformer transformer = WGS2MetricTransformer.LATransformer;
        try {
            IndexedDirectedGraph graph = MapDataHelper.getLAGraph();
            RouteCalibrator calibrator = new HMMRouteCalibrator(graph);
            List<RouteBean> routes = rh.getAllRoutes();
            for (RouteBean routeBean : routes) {
                List<Coordinate> coords = rh.getCoordinates(routeBean);
                coords = transformer.fromWGS84(coords);
                Walk walk = calibrator.calibrate(coords);
                String[] nodeIds = new String[walk.size()];
                for (int i = 0; i < walk.size(); ++i) {
                    nodeIds[i] = String.valueOf(graph.getInnerNodeId((Node) walk.get(i)));
                }
                String nodeSequence = String.join(",", (CharSequence[]) nodeIds);
                int row = rh.setNodeSequence(routeBean.getRoute_id(), nodeSequence);
                System.out.println(nodeSequence + ", " + row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param placeId
     * @throws SQLException
     */
    public static void getRoutesFromCenterAndWriteDB(String placeId) throws SQLException {
        List<RouteBean> routes = GoogleMapsHelper.generateRoutesFromCenter(placeId);
        RouteBeanHelper routeHelper = new RouteBeanHelper();
        routeHelper.addRoutes(routes);
    }

    public static void getRoutes() throws Exception {
        List<RouteBean> routes = new ArrayList<RouteBean>();
        String homePlaceId = "ChIJ7aVxnOTHwoARxKIntFtakKo"; // USC
//         getRoutesFromCenterAndWriteDB(homePlaceId);
        String workPlaceId = "ChIJZQ9c8IW8woARN0gTXFiTqSU"; // UCLA

        RouteBeanHelper routeBeanHelper = new RouteBeanHelper();
//        IndexedDirectedGraph graph = MapDataHelper.getLAGraph();
//        RouteCalibrator calibrator = new HMMRouteCalibrator(graph);
        PlaceHelper placeHelper = new PlaceHelper();
        List<String> placeIds = placeHelper.getPlaceIds();
        String[] centerIds = new String[]{homePlaceId, workPlaceId};
        for (int i = 0; i < centerIds.length; ++i) {
            for (int j = 0; j < placeIds.size(); ++j) {
//            for (int j = 0; j < 2; ++j) {
                System.out.println(i * placeIds.size() + j);
                String fromPlaceId = centerIds[i], toPlaceId = placeIds.get(j);
                RouteBean route = GoogleMapsHelper.getRoute(fromPlaceId, toPlaceId, false);
                routes.add(route);
                route = GoogleMapsHelper.getRoute(toPlaceId, fromPlaceId, false);
                routes.add(route);
            }
        }
        for (RouteBean route : routes) {
            try {
                routeBeanHelper.addRoute(route);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
