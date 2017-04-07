package edu.usc.infolab.geo.semanticroute.model;

import com.google.gson.JsonObject;
import com.google.maps.model.LatLng;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Created by yaguang on 6/21/16.
 */
public class RLocation {
    private double lat;
    private double lng;

    public RLocation(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public RLocation(LatLng latLng) {
        this.lat = latLng.lat;
        this.lng = latLng.lng;
    }

    public RLocation(Coordinate coord) {
        this.lat = coord.y;
        this.lng = coord.x;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public LatLng toLatLng() {
        return new LatLng(this.lat, this.lng);
    }

    public Coordinate toCoordinate() {
        return new Coordinate(this.lng, this.lat);
    }

    static final double EARTH_RADIUS_IN_METERS = 6372797.560856;

    /**
     * Gets the distance between two points in meter.
     *
     * @param loc1
     * @param loc2
     * @return
     */
    static RDistance getDistance(RLocation loc1, RLocation loc2) {
        double Radius = EARTH_RADIUS_IN_METERS;
        double lat1 = loc1.getLat();
        double lat2 = loc2.getLat();
        double lon1 = loc1.getLng();
        double lon2 = loc2.getLng();
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return new RDistance(Radius * c);
    }

    JsonObject toJsonElement() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("lat", lat);
        jsonObject.addProperty("lng", lng);
        return jsonObject;
    }
}
