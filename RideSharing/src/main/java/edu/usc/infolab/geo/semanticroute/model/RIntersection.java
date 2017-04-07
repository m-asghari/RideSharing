package edu.usc.infolab.geo.semanticroute.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * An intersection in the road network for routing purpose.
 * Created by yaguang on 6/21/16.
 */
public class RIntersection implements IJsonElement {
    private RLocation location;
    //    private List<RBearing> bearings;
    String nodeId;

    public RLocation getLocation() {
        return location;
    }

//    public void setBearings(List<RBearing> bearings) {
//        this.bearings = bearings;
//    }

    public String getNodeId() {
        return nodeId;
    }

    public RIntersection(RLocation location, String nodeId) {
        this.location = location;
        this.nodeId = nodeId;
    }

    public JsonObject toJsonElement() {
        JsonObject jsonObject = new JsonObject();
        Gson gson = new Gson();
        jsonObject.add("location", location.toJsonElement());
        jsonObject.addProperty("node_id", nodeId);
//        JsonArray jsonBearings = new JsonArray();
//        for (RBearing bearing : bearings) {
//            jsonBearings.add(bearing.toJsonElement());
//        }
//        jsonObject.add("bearings", jsonBearings);
        return jsonObject;
    }
}
