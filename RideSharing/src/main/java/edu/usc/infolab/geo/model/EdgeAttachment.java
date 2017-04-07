package edu.usc.infolab.geo.model;

import com.vividsolutions.jts.geom.Coordinate;
import edu.usc.infolab.geo.util.EdgeHelper;
import org.geotools.graph.structure.Edge;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * An object that attaches to an edge, e.g., a sensor or an accident.
 */
public class EdgeAttachment {
    private static final long INVALID_EDGE_ID = -1;
    public static final int HIGHWAY_SENSOR = 1001;
    public static final int ACCIDENT = 1002;
    public static final int ARTERIAL_SENSOR = 1003;

    private String attachmentId;
    private int type;

    private Coordinate location;

    private Edge edge;

    private Map<String, Object> data;

    public EdgeAttachment(String attachmentId, Coordinate location) {
        this.attachmentId = attachmentId;
        this.location = location;
        this.edge = null;
        this.type = HIGHWAY_SENSOR;
        this.data = new HashMap<>();
    }

    public String getAttachmentId() {
        return attachmentId;
    }


    public Coordinate getLocation() {
        return location;
    }

    public long getEdgeId() {
        if (this.edge == null) {
            return INVALID_EDGE_ID;
        }
        return new EdgeHelper(this.edge).getInnerEdgeId();
    }

    public boolean isAttached() {
        return this.edge != null;
    }

    public Edge getEdge() {
        return this.edge;
    }

    public void setEdge(Edge e) {
        this.edge = e;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private void setAttribute(String name, Object value) {
        data.put(name, value);
    }

    public Object getAttribute(String name) {
        return data.get(name);
    }

    @Override
    public String toString() {
        String typeStr = "";
        if (type == HIGHWAY_SENSOR) {
            typeStr = "Highway";
        } else if (type == ARTERIAL_SENSOR) {
            typeStr = "Arterial";
        } else if (type == ACCIDENT) {
            typeStr = "Accident";
        }
        return String.format("%s %s (%.4f, %.4f) %s",
                typeStr, getAttachmentId(), getLocation().y, getLocation().x, getStreetName());
    }


    public String getStreetName() {
        return (String) getAttribute("streetName");
    }

    public void setStreetName(String streetName) {
        setAttribute("streetName", streetName);
    }

    public int getDirection() {
        return (Integer) getAttribute("direction");
    }

    public void setDirection(int direction) {
        setAttribute("direction", direction);
    }

    public Date getTime() {
        return (Date) getAttribute("time");
    }

    public void setTime(Date time) {
        setAttribute("time", time);
    }
}
