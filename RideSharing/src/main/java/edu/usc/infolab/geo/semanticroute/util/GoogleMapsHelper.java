package edu.usc.infolab.geo.semanticroute.util;

import com.google.gson.Gson;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.*;
import com.vividsolutions.jts.geom.Coordinate;
import edu.usc.infolab.geo.model.Trajectory;
import edu.usc.infolab.geo.model.TrjRecord;
import edu.usc.infolab.geo.semanticroute.dao.PlaceBean;
import edu.usc.infolab.geo.semanticroute.dao.PlaceHelper;
import edu.usc.infolab.geo.semanticroute.dao.RouteBean;
import edu.usc.infolab.geo.util.Constants;
import edu.usc.infolab.geo.util.WGS2MetricTransformer;
import org.joda.time.DateTime;
import org.opengis.referencing.operation.TransformException;
import org.postgis.LineString;
import org.postgis.PGgeometry;
import org.postgis.Point;
import se.walkercrou.places.*;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GoogleMapsHelper {
    static Logger logger = Logger.getLogger(GoogleMapsHelper.class.getName());

    public static RouteBean getRoute(PlaceBean from, PlaceBean to) throws Exception {
        return getRoute(from, to, false);
    }

    private static GeoApiContext getGeoApiContext() {
        GeoApiContext context = new GeoApiContext().setApiKey(Constants.GOOGLE_API_KEY);
        return context;
    }

    public static RouteBean getRoute(PlaceBean from, PlaceBean to, boolean randomAlternative)
            throws Exception {
        RouteBean route = null;
        GeoApiContext context = new GeoApiContext().setApiKey(Constants.GOOGLE_API_KEY);
        int routeInd = 0;
        DirectionsRoute[] routes =
                DirectionsApi.newRequest(context).mode(TravelMode.DRIVING).alternatives(randomAlternative)
                        .origin(from.getLatLng()).destination(to.getLatLng()).await().routes;
        if (randomAlternative) {
            Random rand = new Random(System.currentTimeMillis());
            routeInd = rand.nextInt(routes.length);
        }
        route = directionsRouteToRouteBean(routes[routeInd]);
        route.setFrom_place_id(from.getPlace_id());
        route.setTo_place_id(to.getPlace_id());
        return route;
    }

    public static Trajectory getTrajectory(LatLng origin, LatLng destination, DateTime departureTime,
                                           boolean randomAlternative)
            throws Exception {

        GeoApiContext context = new GeoApiContext().setApiKey(Constants.GOOGLE_API_KEY);
        int routeInd = 0;
        DirectionsRoute[] routes =
                DirectionsApi.newRequest(context).mode(TravelMode.DRIVING).alternatives(true)
                        .origin(origin).destination(destination).departureTime(departureTime).await().routes;
        if (randomAlternative) {
            Random rand = new Random(System.currentTimeMillis());
            routeInd = rand.nextInt(routes.length);
        }
        return directionsRouteToTrajectory(routes[routeInd]);
    }

    /**
     * Converts directions route to a trajectory assuming constant speed (with Gaussian noise).
     *
     * @param route
     * @return
     */
    private static Trajectory directionsRouteToTrajectory(DirectionsRoute route) throws TransformException {
        List<TrjRecord> records = new ArrayList<>();
        DirectionsLeg leg = route.legs[0];
        DirectionsStep[] steps = leg.steps;
        // Fill the curve
        WGS2MetricTransformer transformer = new WGS2MetricTransformer(leg.startLocation.lng, leg.startLocation.lat);
        long currentTime = new Date().getTime();
        if (leg.departureTime != null) {
            currentTime = leg.departureTime.toDate().getTime();
        }
        for (DirectionsStep step : steps) {
            List<LatLng> decodePath = step.polyline.decodePath();
            List<Coordinate> coords = decodePath.stream()
                    .map(latLng -> new Coordinate(latLng.lng, latLng.lat)).collect(Collectors.toList());
            List<Coordinate> metricCoords = transformer.fromWGS84(coords);
            long duration = step.duration.inSeconds;
            double speed = (double) step.distance.inMeters / duration;
            records.add(new TrjRecord(currentTime, coords.get(0)));
            for (int i = 1; i < coords.size(); ++i) {
                double distance = metricCoords.get(i - 1).distance(metricCoords.get(i));
                currentTime += (long) ((distance / speed) * 1000);
                records.add(new TrjRecord(currentTime, coords.get(i)));
            }
        }
        return new Trajectory(records);
    }

    public static RouteBean getRoute(String fromPlaceId, String toPlaceId, boolean randomAlternative)
            throws Exception {
        PlaceHelper places = new PlaceHelper();
        PlaceBean from = places.getPlace(fromPlaceId), to = places.getPlace(toPlaceId);
        return getRoute(from, to, randomAlternative);
    }


    private static RouteBean directionsRouteToRouteBean(DirectionsRoute route) {
        RouteBean routeBean = new RouteBean();
        DirectionsLeg leg = route.legs[0];
        DirectionsStep[] steps = leg.steps;
        // Fill the curve
        List<Point> points = new ArrayList<Point>();
        for (DirectionsStep step : steps) {
            List<LatLng> decodePath = step.polyline.decodePath();
            for (LatLng latlng : decodePath) {
                Point point = new Point(latlng.lng, latlng.lat);
                point.setSrid(Constants.DEFAULT_SRID);
                points.add(point);
            }
        }
        Point[] pointsArray = new Point[points.size()];
        LineString path = new LineString(points.toArray(pointsArray));
        path.setSrid(Constants.DEFAULT_SRID);
        routeBean.setPath(new PGgeometry(path));
        routeBean.setJson_str(new Gson().toJson(route));
        routeBean.setNode_sequence("");
        return routeBean;
    }

    /**
     * Gets certain types of POIs within a radius.
     *
     * @param latlng the coordinate of the center.
     * @return
     */
    public static List<PlaceBean> getNearby(LatLng latlng) {
        GooglePlaces client = new GooglePlaces(Constants.GOOGLE_API_KEY);
        // double lat = 34.020207, lng = -118.2897;
        double lat = latlng.lat, lng = latlng.lng;
        double radius = 15000;
        // String[] types = new String[] {"school", "food", "church", "university", "museum", "cafe",
        // "bar", "hospital", "airport", "bank", "library", "movie_theater"};
        List<String> types = Arrays.asList(Types.TYPE_CHURCH, Types.TYPE_UNIVERSITY, Types.TYPE_MUSEUM,
                Types.TYPE_AIRPORT, Types.TYPE_MOVIE_THEATER, Types.TYPE_RESTAURANT);
        // List<Place> places = client.getNearbyPlaces(lat, lng, radius);
        // List<Place> places = client.getPlacesByRadar(lat, lng, radius,
        // Param.name("types").value("school"));
        Param typeParam = TypeParam.name("types").value(types);
        // List<Place> places = client.getPlacesByRadar(lat, lng, radius, typeParam);
        List<Place> places = client.getNearbyPlaces(lat, lng, radius, typeParam);
        List<PlaceBean> placeBeans = new ArrayList<PlaceBean>();
        for (Place place : places) {
            placeBeans.add(placeToPlaceBean(place));
        }
        return placeBeans;
    }

    public static List<String> getNearbyPlaceId(LatLng latlng, double radius) {
        GooglePlaces client = new GooglePlaces(Constants.GOOGLE_API_KEY);
        // double lat = 34.020207, lng = -118.2897;
        double lat = latlng.lat, lng = latlng.lng;
//        double radius = 15000;
        List<String> types = Arrays.asList(Types.TYPE_CHURCH, Types.TYPE_UNIVERSITY, Types.TYPE_MUSEUM,
                Types.TYPE_AIRPORT, Types.TYPE_MOVIE_THEATER, Types.TYPE_RESTAURANT);
        Param typeParam = TypeParam.name("types").value(types);
        List<Place> places = client.getPlacesByRadar(lat, lng, radius, typeParam);
        List<String> placeIds = new ArrayList<>();
        for (Place place : places) {
            placeIds.add(place.getPlaceId());
        }
        return placeIds;
    }

    public static List<String> getNearbyPlaceId(LatLng latlng) {
        return getNearbyPlaceId(latlng, 15000);
    }

    public static PlaceBean placeToPlaceBean(Place place) {
        PlaceBean bean = new PlaceBean();
        bean.setPlace_id(place.getPlaceId());
        bean.setLat(place.getLatitude());
        bean.setLng(place.getLongitude());
        bean.setName(place.getName());
        bean.setTypes(String.join("|", place.getTypes()));
        bean.setScore(place.getRating());
        bean.setAddress(place.getVicinity());
        return bean;
    }

    /**
     * Generates routes that start from center to surrounding POIs.
     *
     * @param centerPlaceId the place_id of the center.
     * @throws SQLException
     */
    public static List<RouteBean> generateRoutesFromCenter(String centerPlaceId) throws SQLException {
        PlaceHelper places = new PlaceHelper();
        PlaceBean center = places.getPlace(centerPlaceId);
        List<PlaceBean> nearbyPlaces = getNearby(center.getLatLng());
        List<RouteBean> routes = new ArrayList<RouteBean>();
        for (PlaceBean place : nearbyPlaces) {
            if (place.getPlace_id().equals(center.getPlace_id()))
                continue;
            try {
                RouteBean route = GoogleMapsHelper.getRoute(center, place, false);
                routes.add(route);
                route = GoogleMapsHelper.getRoute(place, center, false);
                routes.add(route);
            } catch (Exception e) {
                logger.severe(e.getMessage());
                logger.severe(String.format("Failed to get route from: %s to %s.", center.getPlace_id(),
                        place.getPlace_id()));
                continue;
            }
        }
        return routes;
    }

    /**
     * Increases the current date by n weeks so that it is greater than currentTime
     *
     * @param departureTime
     * @return
     */
    public static DateTime getFutureTimeWeekly(DateTime departureTime, DateTime currentTime) {
        long result = departureTime.getMillis();
        long delay = 10 * 1000;  // assuming 10 seconds delay.
        long period = 7 * 24 * 3600 * 1000;  //
        if (result <= currentTime.getMillis() + delay) {
            double diff = currentTime.getMillis() + delay - result;
            result += (long) Math.ceil(diff / period) * period;
        }
        return new DateTime(result);
    }

    public static String reverseGeocoding(LatLng latLng) {
        GeoApiContext context = getGeoApiContext();
        String address = "";
        try {
            GeocodingResult[] results = GeocodingApi.reverseGeocode(context, latLng).await();
            address = results[0].formattedAddress;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return address;
    }
}
