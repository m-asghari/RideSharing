package edu.usc.infolab.geo.semanticroute.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Route segment for Routing.
 * Created by yaguang on 6/21/16.
 */
public class RSegment implements IJsonElement {
    private RDistance distance;
    private RDuration duration;

    private List<RStep> steps;

    /**
     * Describe the current step, usually used for navigation.
     */
    private String summary;

    private String type;

    public static final String NATURAL_SEGMENT = "natural";
    public static final String KNOWN_SEGMENT = "known";

    RPolyline geometry;

    RPolyline sourceRoute;

    public String getType() {
        return this.type;
    }


    public boolean hasStreetName() {
        return this.getType().equals(KNOWN_SEGMENT) || (!this.getSteps().get(0).getName().isEmpty());
    }

    public RDistance getDistance() {
        return distance;
    }

    public RDuration getDuration() {
        return duration;
    }

    public String getSummary() {
        return summary;
    }

    public RPolyline getGeometry() {
        return geometry;
    }

    public List<RStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public RSegment(List<RStep> steps, RPolyline sourceRoute) {
        // TODO(yaguang) more sophisticated method for summary generation.
        this(steps, sourceRoute, steps.get(0).getSummary(), NATURAL_SEGMENT);
    }

    public RSegment(List<RStep> steps, RPolyline sourceRoute, String summary, String type) {
        this.steps = new ArrayList<>(steps);
        this.sourceRoute = sourceRoute;
        List<RLocation> locations = new ArrayList<>();
        double distanceValue = 0;
        double durationValue = 0;
        for (RStep step : steps) {
            distanceValue += step.getDistance().getValue();
            durationValue += step.getDuration().getValue();
            locations.addAll(step.getGeometry().toLocationList());
        }
        geometry = new RPolyline(locations);
        distance = new RDistance(distanceValue);
        duration = new RDuration(durationValue);
        this.summary = summary;
        this.type = type;
    }

    @Override
    public JsonElement toJsonElement() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("distance", distance.toJsonElement());
        jsonObject.add("duration", duration.toJsonElement());
        jsonObject.addProperty("summary", summary);
        jsonObject.add("geometry", geometry.toJsonElement());
        jsonObject.addProperty("type", type);
        JsonArray stepJsonArray = new JsonArray();
        for (RStep step : steps) {
            stepJsonArray.add(step.toJsonElement());
        }
        jsonObject.add("steps", stepJsonArray);
        return jsonObject;
    }
}
