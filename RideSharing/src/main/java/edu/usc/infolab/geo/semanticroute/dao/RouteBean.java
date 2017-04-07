package edu.usc.infolab.geo.semanticroute.dao;

import com.google.gson.Gson;
import com.vividsolutions.jts.geom.Coordinate;
import org.postgis.LineString;
import org.postgis.PGgeometry;
import org.postgis.Point;

import java.util.ArrayList;
import java.util.List;

public class RouteBean {
    private int route_id;
    private String node_sequence;
    private String from_place_id;
    private String from_place;
    private String to_place_id;
    private String to_place;
    private PGgeometry path;
    private String json_str;
    private String main_road;
    private double score;

    public String getJson_str() {
        return json_str;
    }

    public void setJson_str(String json_str) {
        this.json_str = json_str;
    }

    public int getRoute_id() {
        return route_id;
    }

    public void setRoute_id(int route_id) {
        this.route_id = route_id;
    }

    public String getNode_sequence() {
        return node_sequence;
    }

    public void setNode_sequence(String node_sequence) {
        this.node_sequence = node_sequence;
    }

    public String getFrom_place_id() {
        return from_place_id;
    }

    public void setFrom_place_id(String from_place_id) {
        this.from_place_id = from_place_id;
    }

    public String getTo_place_id() {
        return to_place_id;
    }

    public void setTo_place_id(String to_place_id) {
        this.to_place_id = to_place_id;
    }

    public PGgeometry getPath() {
        return path;
    }

    public void setPath(PGgeometry path) {
        this.path = path;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public List<Long> getNodeIdSequence() {
        List<Long> nodeIds = new ArrayList<Long>();
        if (getNode_sequence() != null) {
            String[] nodeIdStrings = getNode_sequence().split(",");
            for (String nodeIdString : nodeIdStrings) {
                nodeIds.add(Long.valueOf(nodeIdString));
            }
        }
        return nodeIds;
    }

    public List<Coordinate> getCoordinates() {
        List<Coordinate> coords = new ArrayList<Coordinate>();
        LineString line = (LineString) path.getGeometry();
        for (Point p : line.getPoints()) {
            coords.add(new Coordinate(p.x, p.y));
        }
        return coords;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public String getMain_road() {
        return main_road;
    }

    public void setMain_road(String main_road) {
        this.main_road = main_road;
    }


    public String getTo_place() {
        return to_place;
    }

    public void setTo_place(String to_place) {
        this.to_place = to_place;
    }

    public String getFrom_place() {
        return from_place;
    }

    public void setFrom_place(String from_place) {
        this.from_place = from_place;
    }
}
