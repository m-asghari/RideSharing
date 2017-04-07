package edu.usc.infolab.geo.semanticroute.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Route object for Routing purpose.
 */
public class RRoute implements IJsonElement {
    private RDistance distance;
    private RDuration duration;
    private RPolyline geometry;
    private List<RSegment> segments;
    /**
     * Summary of the route, e.g., names of top two longest street.
     */
    private String summary;

    public RRoute(List<RSegment> segments) {
        init(segments);
        this.summary = generateRouteSummary();
    }

    public String getSummary() {
        return summary;
    }

    /**
     * Finds the top 1 or 2 road name in this route.
     * TODO(yaguang): change to more sophisticate method.
     *
     * @return
     */
    private String generateRouteSummary() {
        // Sorts segments by distance.
        String summary = "";
        List<Pair<Double, String>> pairs = new ArrayList<>();
        for (RSegment segment : segments) {
            for (RStep step : segment.getSteps()) {
                pairs.add(new ImmutablePair<>(step.getDistance().getValue(), step.getName()));
            }
        }
        Collections.sort(pairs);
        if (pairs.size() > 0) {
            summary = pairs.get(pairs.size() - 1).getRight();
        }
        return summary;
    }

    public RRoute(List<RSegment> segments, String summary) {
        init(segments);
        this.summary = summary;
    }

    private void init(List<RSegment> segments) {
        this.segments = new ArrayList<>(segments);
        double distanceValue = 0;
        double durationValue = 0;
        List<RLocation> locations = new ArrayList<>();
        for (RSegment segment : this.segments) {
            distanceValue += segment.getDistance().getValue();
            durationValue += segment.getDuration().getValue();
            locations.addAll(segment.getGeometry().toLocationList());
        }
        this.geometry = new RPolyline(locations);
        this.distance = new RDistance(distanceValue);
        this.duration = new RDuration(durationValue);
    }

    @Override
    public JsonElement toJsonElement() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("distance", distance.toJsonElement());
        jsonObject.add("duration", duration.toJsonElement());
        jsonObject.addProperty("summary", summary);
        jsonObject.add("geometry", geometry.toJsonElement());
        JsonArray segmentJsonArray = new JsonArray();
        for (RSegment segment : segments) {
            segmentJsonArray.add(segment.toJsonElement());
        }
        jsonObject.add("segments", segmentJsonArray);
        return jsonObject;
    }
}
