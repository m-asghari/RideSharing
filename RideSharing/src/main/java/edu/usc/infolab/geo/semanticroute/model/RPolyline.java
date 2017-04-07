package edu.usc.infolab.geo.semanticroute.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by yaguang on 6/23/16.
 */
public class RPolyline implements IJsonElement {

    List<RLocation> locations;

    public RPolyline(List<RLocation> locations) {
        this.locations = locations;
    }

    public RLocation get(int index) {
        return locations.get(index);
    }

    public int size() {
        return locations.size();
    }

    public List<RLocation> toLocationList() {
        return new ArrayList<>(locations);
    }

    public RDistance getDistance() {
        double length = 0;
        double size = size();
        for (int i = 1; i < size; ++i) {
            length += RLocation.getDistance(get(i - 1), get(i)).getValue();
        }
        return new RDistance(length);
    }

    @Override
    public JsonElement toJsonElement() {
        List<LatLng> latLngs = locations.stream().map(RLocation::toLatLng).collect(Collectors.toList());
        EncodedPolyline polyline = new EncodedPolyline(latLngs);
        return new JsonPrimitive(polyline.getEncodedPath());
    }
}
