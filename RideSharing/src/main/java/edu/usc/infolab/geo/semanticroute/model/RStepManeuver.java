package edu.usc.infolab.geo.semanticroute.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.text.WordUtils;

/**
 * Created by yaguang on 6/21/16.
 */
public class RStepManeuver implements IJsonElement {
    private RLocation location;
    private RBearing bearingBefore;
    private RBearing bearingAfter;
    private String type;
    private String modifier;

    public static final String DEPART = "depart";
    public static final String ARRIVE = "arrive";
    public static final String TURN = "turn";
    public static final String STRAIGHT = "straight";
    public static final String SLIGHT_RIGHT = "slight right";
    public static final String RIGHT = "right";
    public static final String SHARP_RIGHT = "sharp right";
    public static final String U_TURN = "u-turn";
    public static final String SHARP_LEFT = "sharp left";
    public static final String LEFT = "left";
    public static final String SLIGHT_LEFT = "slight left";

    public RStepManeuver(RLocation location, RBearing bearingBefore, RBearing bearingAfter, String type) {
        this.location = location;
        this.bearingBefore = bearingBefore;
        this.bearingAfter = bearingAfter;
        this.type = type;
        if (this.type.equals(TURN)) {
            this.modifier = getTurnModifier(bearingBefore, bearingAfter);
        } else {
            this.modifier = "";
        }
    }

    public String getType() {
        return this.type;
    }

    /**
     * Describes the maneuver, currently just return the modifier.
     *
     * @return
     */
    public String getDescription() {
        return WordUtils.capitalize(this.modifier, '\n');
    }

    @Override
    public JsonElement toJsonElement() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("location", location.toJsonElement());
        jsonObject.add("bearing_before", bearingBefore.toJsonElement());
        jsonObject.add("bearing_after", bearingAfter.toJsonElement());
        jsonObject.addProperty("type", type);
        jsonObject.addProperty("modifier", modifier);
        return jsonObject;
    }

    public static String getTurnModifier(RBearing bearingBefore, RBearing bearingAfter) {
        String modifier = "";
        double diff = ((bearingAfter.getValue() - bearingBefore.getValue() + 360) % 360);
        if (diff <= 10) {
            modifier = STRAIGHT;
        } else if (diff <= 45) {
            modifier = SLIGHT_RIGHT;
        } else if (diff <= 135) {
            modifier = RIGHT;
        } else if (diff <= 170) {
            modifier = SHARP_RIGHT;
        } else if (diff <= 190) {
            modifier = U_TURN;
        } else if (diff <= 225) {
            modifier = SHARP_LEFT;
        } else if (diff <= 315) {
            modifier = LEFT;
        } else if (diff <= 350) {
            modifier = SLIGHT_LEFT;
        } else {
            modifier = STRAIGHT;
        }
        return modifier;
    }
}
