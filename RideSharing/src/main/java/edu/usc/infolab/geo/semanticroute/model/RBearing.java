package edu.usc.infolab.geo.semanticroute.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Created by yaguang on 6/21/16.
 */
public class RBearing implements IJsonElement {
    private double value;

    public double getValue() {
        return value;
    }

    public RBearing(double value) {
        this.value = value;
    }

    public JsonElement toJsonElement() {
        return new JsonPrimitive(value);
    }


    public String toString() {
        return "" + getValue();
    }

    public static RBearing getBearing(double fromLat, double fromLon, double toLat, double toLon) {
        double fromLatRad = Math.toRadians(fromLat);
        double toLatRad = Math.toRadians(toLat);
        double lonRadDiff = Math.toRadians(toLon - fromLon);
        double y = Math.sin(lonRadDiff) * Math.cos(toLatRad);
        double x = Math.cos(fromLatRad) * Math.sin(toLatRad) - Math.sin(fromLatRad) * Math.cos(toLatRad) * Math.cos(lonRadDiff);
        double bearing = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
        return new RBearing(bearing);
    }

    public static RBearing getBearing(RLocation from, RLocation to) {
        return getBearing(from.getLat(), from.getLng(), to.getLat(), to.getLng());
    }

}
