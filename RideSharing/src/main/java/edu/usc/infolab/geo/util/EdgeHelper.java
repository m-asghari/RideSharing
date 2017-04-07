package edu.usc.infolab.geo.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import edu.usc.infolab.geo.model.EdgeAttachment;
import org.geotools.graph.structure.DirectedEdge;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.opengis.feature.simple.SimpleFeature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class to get information from an edge associated with SimpleFeature.
 *
 * @author Yaguang
 */
public class EdgeHelper {
    @SuppressWarnings("unused")
    private final Edge e;
    private final SimpleFeature sf;

    public enum Direction {
        ORIGINAL, REVERSE
    }

    public EdgeHelper(Edge e) {
        this.e = e;
        this.sf = (SimpleFeature) e.getObject();
    }

    public long getWayId() {
        return (Long) sf.getAttribute("way_id");
    }

    public long getInnerEdgeId() {
        return (Long) sf.getAttribute("link_id");
    }

    /**
     * Gets the end coordinate in metric space (rather than WGS84).
     *
     * @return
     */
    public Coordinate getEndCoordinate() {
        return getGeometry().getEndPoint().getCoordinate();
    }

    /**
     * Gets the start coordinate in metric space (rather than WGS84).
     *
     * @return
     */
    public Coordinate getStartCoordinate() {
        return getGeometry().getStartPoint().getCoordinate();
    }

    public long getInnerFromNodeId() {
        return (Long) sf.getAttribute("from_node_id");
    }

    public long getInnerToNodeId() {
        return (Long) sf.getAttribute("to_node_id");
    }

    public Node getStartNode() {
        return ((DirectedEdge) e).getInNode();
    }

    public Node getEndNode() {
        return ((DirectedEdge) e).getOutNode();
    }

    public Direction getEdgeIdDirection() {
        return MapDataHelper.getEdgeIdDirection(getInnerEdgeId()) > 0 ? Direction.REVERSE
                : Direction.ORIGINAL;
    }

    public Short getDirection() {
        return (Short) sf.getAttribute("travel_direction");
    }

    public LineString getLineString() {
        return getGeometry();
    }

    public String getStreetName() {
        String name = (String) sf.getAttribute("street_name");
        return name;
    }

    public String getNormalizedStreetName() {
        String name = (String) sf.getAttribute("street_name");
        String[] fields = name.split(" ");
        int size = fields.length;
        switch (fields[0].toUpperCase()) {
            case "S":
                fields[0] = "South";
                break;
            case "N":
                fields[0] = "North";
                break;
            case "W":
                fields[0] = "West";
                break;
            case "E":
                fields[0] = "East";
                break;
        }
        switch (fields[size - 1].toUpperCase()) {
            case "AVE":
                fields[size - 1] = "Avenue";
                break;
            case "ST":
                fields[size - 1] = "Street";
                break;
            case "BLVD":
                fields[size - 1] = "Boulevard";
                break;
            case "DR":
                fields[size - 1] = "Drive";
                break;
            case "RD":
                fields[size - 1] = "Road";
                break;
        }
        return String.join(" ", fields);
    }

    public void setStreetName(String name) {
        sf.setAttribute("street_name", name);
    }

    public List<String> getAllNames() {
        String nameDefault = (String) sf.getAttribute("name_default");
        List<String> names = new ArrayList<String>(Arrays.asList(nameDefault.split(";")));
        names.add(getStreetName());
        return names;
    }

    public LineString getGeometry() {
        return (LineString) sf.getDefaultGeometry();
    }

    public double getLength() {
        return (Double) sf.getAttribute("length");
    }

    public List<Coordinate> getCoordinateList() {
        LineString line = getGeometry();
        List<Coordinate> coords = new ArrayList<Coordinate>();
        coords.addAll(Arrays.asList(line.getCoordinates()));
        return coords;
    }

    public Edge getEdge() {
        return this.e;
    }

    public String getFunctionClass() {
        return (String) sf.getAttribute("function_class");
    }

    public void setUserAttribute(String key, Object o) {
        this.sf.getUserData().put(key, o);
    }

    public Object getUserAttribute(String key) {
        return this.sf.getUserData().get(key);
    }

    private List<EdgeAttachment> getMutableAttachments() {
        List<EdgeAttachment> attachments = (List<EdgeAttachment>) getUserAttribute("objects");
        if (attachments == null) {
            attachments = new ArrayList<>();
            setUserAttribute("objects", attachments);
        }
        return attachments;
    }

    public void attachObject(EdgeAttachment o) {
        getMutableAttachments().add(o);
    }

    /**
     * Detaches an object from the edge.
     *
     * @param o
     * @return
     */
    public boolean detachObject(EdgeAttachment o) {
        List<EdgeAttachment> attachments = getMutableAttachments();
        o.setEdge(null);
        return attachments.remove(o);
    }

    /**
     * Gets all the objects attached to this edge.
     *
     * @return
     */
    public List<EdgeAttachment> getAttachments() {
        return new ArrayList<>(getMutableAttachments());
    }

    /**
     * Gets attachments of certain type.
     *
     * @param type type of attachment, e.g, HIGHWAY_SENSOR, ACCIDENT.
     * @return
     */
    public List<EdgeAttachment> getAttachments(int type) {
        List<EdgeAttachment> attachments = getMutableAttachments();
        List<EdgeAttachment> result = attachments.stream()
                .filter(attachment -> attachment.getType() == type).collect(Collectors.toList());
        return result;
    }

    /**
     * Gets attachments of certain type.
     *
     * @param types types of attachment, e.g, ARTERIAL_SENSORS, HIGHWAY_SENSOR, ACCIDENT.
     * @return
     */
    public List<EdgeAttachment> getAttachments(HashSet<Integer> types) {
        List<EdgeAttachment> attachments = getMutableAttachments();
        List<EdgeAttachment> result = attachments.stream()
                .filter(attachment -> types.contains(attachment.getType())).collect(Collectors.toList());
        return result;
    }

    public void setAttachments(List<EdgeAttachment> attachments) {
        List<EdgeAttachment> newAttachments = new ArrayList<>(attachments);
        setUserAttribute("objects", attachments);
    }

    public double getCosineAngle(EdgeHelper eh) {
        double dx1 = eh.getEndCoordinate().x - eh.getStartCoordinate().x,
                dy1 = eh.getEndCoordinate().y - eh.getStartCoordinate().y,
                dx2 = getEndCoordinate().x - getStartCoordinate().x,
                dy2 = getEndCoordinate().y - getStartCoordinate().y;
        double len1 = Math.sqrt(dx1 * dx1 + dy1 * dy1),
                len2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);
        double cosine = (dx1 * dx2 + dy1 * dy2) / (len1 * len2);
        return cosine;
    }
}
