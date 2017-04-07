package edu.usc.infolab.geo.semanticroute.preprocessing;


import com.vividsolutions.jts.geom.Coordinate;
import edu.usc.infolab.geo.util.Constants;
import se.walkercrou.places.*;

import java.util.Arrays;
import java.util.List;

public class PlacesRetrieval {

    public static void main(String[] args) {
        double lat = 34.020207, lng = -118.2897;
        double radius = 1000;
        Coordinate center = new Coordinate(lng, lat);
        getNearbyPOIs(center, radius);
    }


    public static void getNearbyPOIs(Coordinate center, double radius) {
        GooglePlaces client = new GooglePlaces(Constants.GOOGLE_API_KEY);
        double lat = center.y, lng = center.x;
        // double radius = 10000;
        // String[] types = new String[] {"school", "food", "church", "university", "museum", "cafe",
        // "bar", "hospital", "airport", "bank", "library", "movie_theater"};
        List<String> types = Arrays.asList(Types.TYPE_SCHOOL, Types.TYPE_CHURCH, Types.TYPE_UNIVERSITY,
                Types.TYPE_MUSEUM, Types.TYPE_AIRPORT, Types.TYPE_MOVIE_THEATER,
                Types.TYPE_RESTAURANT, Types.TYPE_GROCERY_OR_SUPERMARKET);
        // List<Place> places = client.getNearbyPlaces(lat, lng, radius);
        // List<Place> places = client.getPlacesByRadar(lat, lng, radius,
        // Param.name("types").value("school"));
        Param typeParam = TypeParam.name("types").value(types);
        List<Place> places = client.getPlacesByRadar(lat, lng, radius, typeParam);
        for (Place place : places) {
            System.out.println(place.toString());
        }
        for (int i = 0; i < places.size(); ++i) {
            String placeId = places.get(i).getPlaceId();
            Place place = client.getPlaceById(placeId);
            places.set(i, place);
            System.out.println(
                    place.getName() + "\n" + String.join("|", place.getTypes()) + "\n" + place.getAddress());
        }
    }

}
