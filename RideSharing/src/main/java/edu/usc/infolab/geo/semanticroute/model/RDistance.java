package edu.usc.infolab.geo.semanticroute.model;

import com.google.gson.JsonObject;

/**
 * Created by yaguang on 6/21/16.
 */
public class RDistance implements IJsonElement {
    private double value;

    public double getValue() {
        return value;
    }

    public RDistance(double value) {
        this.value = value;
    }

    @Override
    public JsonObject toJsonElement() {
        JsonObject jsonObject = new JsonObject();
        String text = String.format("%.1f mi", value / 1609.34);
        jsonObject.addProperty("value", value);
        jsonObject.addProperty("text", text);
        return jsonObject;
    }
}
