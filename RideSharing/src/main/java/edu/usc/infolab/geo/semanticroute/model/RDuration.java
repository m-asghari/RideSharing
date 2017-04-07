package edu.usc.infolab.geo.semanticroute.model;

import com.google.gson.JsonObject;

/**
 * Created by yaguang on 6/21/16.
 */
public class RDuration implements IJsonElement {
    private double value;  // Duration in seconds.


    public double getValue() {
        return value;
    }

    public RDuration(double value) {
        this.value = value;
    }

    @Override
    public JsonObject toJsonElement() {
        JsonObject jsonObject = new JsonObject();
        int hours = 0, minutes = 0;
        if (value > 3600) {
            hours = (int) value / 3600;
            value = (int) value % 3600;
        }
        minutes = (int) Math.floor(value / 60);
        String text = String.format("%d mins", minutes);
        if (hours > 0) {
            text = String.format("%d hours %d mins", hours, minutes);
        }
        jsonObject.addProperty("value", value);
        jsonObject.addProperty("text", text);
        return jsonObject;
    }
}
