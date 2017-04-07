package edu.usc.infolab.geo.semanticroute.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A step consists of a maneuver such as a turn or merge, followed by a distance of travel along a single way to the
 * subsequent step. This is corresponding to a natural route.
 * <p>
 * Created by yaguang on 6/21/16.
 */
public class RStep implements IJsonElement {
    private RDistance distance;
    private RDuration duration;
    private RPolyline geometry;
    private String name;
    /**
     * Describe the current step, usually for navigation purpose.
     */
    private String summary;

    private RStepManeuver maneuver;

    private List<RIntersection> intersections;

    private double speed = 12;

    public RDistance getDistance() {
        return distance;
    }

    public RDuration getDuration() {
        return duration;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public RPolyline getGeometry() {
        return geometry;
    }

    public RStep(String name, List<RIntersection> intersections, RStepManeuver stepManeuver) {
        this.name = name;
        this.intersections = new ArrayList<>(intersections);
        this.maneuver = stepManeuver;
        init();
    }

    private void init() {
        List<RLocation> locations = intersections.stream().map(RIntersection::getLocation).collect(Collectors.toList());
        this.geometry = new RPolyline(locations);
        this.distance = geometry.getDistance();
        this.duration = new RDuration(this.distance.getValue() / speed);
        switch (this.maneuver.getType()) {
            case RStepManeuver.DEPART:
                summary = String.format("Head onto %s", getName());
                break;
            case RStepManeuver.TURN:
                if (getName().trim().isEmpty()) {
                    summary = this.maneuver.getDescription();
                } else {
                    summary = String.format("%s onto %s", this.maneuver.getDescription(), getName());
                }
                break;
            case RStepManeuver.ARRIVE:
                summary = "Arrive";
                break;
        }
    }

    @Override
    public JsonObject toJsonElement() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("duration", duration.toJsonElement());
        jsonObject.add("distance", distance.toJsonElement());
        jsonObject.add("geometry", geometry.toJsonElement());
        jsonObject.addProperty("name", name);
        jsonObject.add("maneuver", maneuver.toJsonElement());
        JsonArray intersectionJsonArray = new JsonArray();
        for (RIntersection intersection : intersections) {
            intersectionJsonArray.add(intersection.toJsonElement());
        }
        jsonObject.add("intersections", intersectionJsonArray);
        jsonObject.addProperty("summary", summary);
        return jsonObject;
    }
}
