package edu.usc.infolab.geo.util;


import au.com.bytecode.opencsv.CSVReader;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.linearref.LocationIndexedLine;
import edu.usc.infolab.geo.model.EdgeAttachment;
import edu.usc.infolab.geo.model.IndexedDirectedGraph;
import org.apache.commons.io.FileUtils;
import org.geotools.graph.structure.Edge;
import org.opengis.referencing.operation.TransformException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EdgeAttachmentHelper {
    private static final int INVALID_DIRECTION = 130;
    IndexedDirectedGraph graph = null;
    /**
     * The maximum distance from sensor to the link in meter
     */
    final double maxDistance = 200;
    private WGS2MetricTransformer transformer = null;
    HashSet<String> validClasses = new HashSet<>();

    public EdgeAttachmentHelper(IndexedDirectedGraph graph) {
        this.graph = graph;
        transformer = graph.getTransformer();
        validClasses.add("motorway");
        validClasses.add("motorway_link");
        validClasses.add("primary");
        validClasses.add("trunk");
        validClasses.add("track");
    }

    public double getRNDistance(EdgeAttachment s1, EdgeAttachment s2) throws TransformException {
        return getRNDistance(s1, s2, Double.POSITIVE_INFINITY);
    }

    public double getRNDistance(EdgeAttachment s1, EdgeAttachment s2, double maxDist) throws TransformException {
        attach(s1);
        attach(s2);
        double distance = Double.POSITIVE_INFINITY;
        if (s1.isAttached() && s2.isAttached()) {
            Edge edge1 = graph.getEdge(s1.getEdgeId()), edge2 = graph.getEdge(s2.getEdgeId());
            distance = graph.getRNDist(edge1, transformer.fromWGS84(s1.getLocation()), edge2,
                    transformer.fromWGS84(s2.getLocation()), maxDist);
        }
        return distance;
    }

    /**
     * Maps a sensor to the road network.
     * <p>
     * TODO Use the angle.
     * </p>
     *
     * @param s
     * @throws TransformException
     */
    public boolean attach(EdgeAttachment s) throws TransformException {
        if (s.isAttached())
            return true;
        boolean attached = false;
        // Gets the nearest link.
        Coordinate location = transformer.fromWGS84(s.getLocation());
        Envelope env = new Envelope(location);
        env.expandBy(maxDistance);
        HashSet<Edge> edges = graph.queryEdges(env);
        Edge nnEdge = null;
        double maxScore = Double.NEGATIVE_INFINITY;
        for (Edge edge : edges) {
            EdgeHelper eh = new EdgeHelper(edge);
            // System.out.println(String.join(";", eh.getAllNames()) + "," + eh.getInnerEdgeId());
            if (checkStreetName(eh, s) && checkFunctionClass(eh)) {
                LocationIndexedLine indexLine = new LocationIndexedLine(eh.getLineString());
                Coordinate projection = indexLine.extractPoint(indexLine.project(location));
                double distance = projection.distance(location);
                double cosAngle = 0;
                if (s.getDirection() != INVALID_DIRECTION) {
                    cosAngle = getCosAngle(eh, s.getDirection());
                }
                // Uses weighted exponential sum of distance and angle as the final score.
                double score = (10 - 0.17 * Math.pow(distance, 1.4)) + 10 * Math.pow(cosAngle, 4);
                if (score > maxScore) {
                    maxScore = score;
                    nnEdge = eh.getEdge();
                }
            }
        }
        if (nnEdge != null) {
            attached = true;
            s.setEdge(nnEdge);
            new EdgeHelper(nnEdge).attachObject(s);
        }
        return attached;
    }

    /**
     * The angles between sensor direction and edge should be lower than 120 degrees.
     *
     * @param eh
     * @param direction
     * @return
     */
    private double getCosAngle(EdgeHelper eh, int direction) {
        Coordinate[] coords = eh.getLineString().getCoordinates();
        Coordinate start = coords[0], end = coords[coords.length - 1];
        double dx = end.x - start.x, dy = end.y - start.y;
        double[][] directions = new double[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        double innerProduct = directions[direction][0] * dx + directions[direction][1] * dy;
        return innerProduct / Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Check the angle and function_class to decide
     *
     * @param eh
     * @return
     */
    private boolean checkFunctionClass(EdgeHelper eh) {
        return validClasses.contains(eh.getFunctionClass().toLowerCase());
    }


    private String stemStreetName(String streetName) {
        Pattern p = Pattern.compile("[0-9]+");
        Matcher m = p.matcher(streetName);
        String result = "";
        if (m.find()) {
            result = m.group();
        }
        return result;
    }

    /**
     * Cleans the street name in order to be matched, e.g.,
     * <ul>
     * <li>SR-101, SR101, US101, 101</li>
     * <li>I-10, I10, 10</li>
     * </ul>
     * Edge name may contain multiple fields split by ";".
     *
     * @param eh         Edge helper
     * @param attachment
     * @return
     */
    private boolean checkStreetName(EdgeHelper eh, EdgeAttachment attachment) {
        List<String> edgeNames = eh.getAllNames();
        String attachedName = attachment.getStreetName();
        for (String edgeName : edgeNames) {
            if (stemStreetName(edgeName).equals(stemStreetName(attachedName))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the distance from the sensor to the mapped edge.
     *
     * @param s
     * @return
     * @throws TransformException
     */

    public double getDistanceFromEdge(EdgeAttachment s) throws TransformException {
        double distance = Double.POSITIVE_INFINITY;
        if (s.isAttached()) {
            Coordinate location = transformer.fromWGS84(s.getLocation());
            Coordinate projection = getProjectionToEdgeMetric(s);
            distance = projection.distance(location);
        }
        return distance;
    }

    private Coordinate getProjectionToEdgeMetric(EdgeAttachment s) throws TransformException {
        Coordinate projection = new Coordinate();
        if (s.isAttached()) {
            Coordinate location = transformer.fromWGS84(s.getLocation());
            EdgeHelper eh = new EdgeHelper(graph.getEdge(s.getEdgeId()));
            LocationIndexedLine indexedLine = new LocationIndexedLine(eh.getLineString());
            projection = indexedLine.extractPoint(indexedLine.project(location));
        }
        return projection;
    }

    /**
     * Projects the attachment to the edge.
     *
     * @param s
     * @return the coordinate of the projection.
     * @throws TransformException
     */
    public Coordinate getProjectionToEdge(EdgeAttachment s) throws TransformException {
        if (!s.isAttached()) {
            return new Coordinate();
        }
        return transformer.toWGS84(getProjectionToEdgeMetric(s));
    }

    public double getProjectionDistanceToEnd(EdgeAttachment s) throws TransformException {
        double distance = Double.POSITIVE_INFINITY;
        if (s.isAttached()) {
            Coordinate projection = getProjectionToEdgeMetric(s);
            EdgeHelper eh = new EdgeHelper(s.getEdge());
            distance = projection.distance(eh.getEndCoordinate());
        }
        return distance;
    }


    public double getProjectionDistanceFromStart(EdgeAttachment s) throws TransformException {
        double distance = Double.POSITIVE_INFINITY;
        if (s.isAttached()) {
            Coordinate projection = getProjectionToEdgeMetric(s);
            EdgeHelper eh = new EdgeHelper(s.getEdge());
            distance = projection.distance(eh.getStartCoordinate());
        }
        return distance;
    }

    public List<EdgeAttachment> readSensors(String filepath) throws IOException {
        List<String> lines = FileUtils.readLines(new File(filepath), "utf-8");
        List<EdgeAttachment> sensors = new ArrayList<>();
        Pattern p = Pattern.compile("SDO_POINT_TYPE\\((.*),(.*),NULL\\),NULL,NULL\\)");
        for (String line : lines.subList(1, lines.size())) {
            String[] fields = line.split("\\|");
            Matcher m = p.matcher(fields[8]);
            if (!m.find()) {
                continue;
            }
            String sensorId = fields[3];
            String streetName = fields[5];
            int direction = Integer.parseInt(fields[9]);
            double lng = Double.valueOf(m.group(1)), lat = Double.valueOf(m.group(2));
            Coordinate location = new Coordinate(lng, lat);
            EdgeAttachment sensor = new EdgeAttachment(sensorId, location);
            sensor.setDirection(direction);
            sensor.setStreetName(streetName);
            sensors.add(sensor);
        }
        return sensors;
    }

    public List<EdgeAttachment> readSensors_2012(String filepath) throws IOException {
        List<String> lines = FileUtils.readLines(new File(filepath), "utf-8");
        List<EdgeAttachment> sensors = new ArrayList<>();
        for (String line : lines.subList(1, lines.size())) {
            String[] fields = line.split("\t");
            String sensorId = fields[2];
            String streetName = fields[6];
            int direction = Integer.parseInt(fields[5]);
            double lng = Double.valueOf(fields[3]), lat = Double.valueOf(fields[4]);
            Coordinate location = new Coordinate(lng, lat);
            EdgeAttachment sensor = new EdgeAttachment(sensorId, location);
            sensor.setDirection(direction);
            sensor.setStreetName(streetName);
            sensors.add(sensor);
        }
        return sensors;
    }

    public List<EdgeAttachment> readSensors_2015(String filepath) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(filepath));
        List<String[]> records = csvReader.readAll();
        List<EdgeAttachment> sensors = new ArrayList<>();
        for (String[] fields : records.subList(1, records.size())) {
            String sensorId = fields[2];
            String sensorType = fields[3];
            String streetName = fields[4];
            int direction = Integer.parseInt(fields[8]);
            double lng = Double.valueOf(fields[6]), lat = Double.valueOf(fields[7]);
            Coordinate location = new Coordinate(lng, lat);
            EdgeAttachment sensor = new EdgeAttachment(sensorId, location);
            sensor.setDirection(direction);
            sensor.setStreetName(streetName);
            if (sensorType.toLowerCase().equals("highway")) {
                sensor.setType(EdgeAttachment.HIGHWAY_SENSOR);
            } else if (sensorType.toLowerCase().equals("arterial")) {
                sensor.setType(EdgeAttachment.ARTERIAL_SENSOR);
            }
            sensors.add(sensor);
        }
        return sensors;
    }

    public List<EdgeAttachment> readAccidents_2012(String filepath) throws IOException {
        List<String> lines = FileUtils.readLines(new File(filepath), "utf-8");
        List<EdgeAttachment> accidents = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd HH:mm");
        for (String line : lines.subList(1, lines.size())) {
            try {
                String[] fields = line.split("\t");
                String eventId = fields[0];
                String streetName = fields[5];
                int direction = Integer.parseInt(fields[6]);
                Date time = format.parse(fields[4]);
                double lng = Double.valueOf(fields[3]), lat = Double.valueOf(fields[2]);
                Coordinate location = new Coordinate(lng, lat);
                EdgeAttachment accident = new EdgeAttachment(eventId, location);
                accident.setDirection(direction);
                accident.setStreetName(streetName);
                accident.setType(EdgeAttachment.ACCIDENT);
                accident.setTime(time);
                accidents.add(accident);
            } catch (Exception e) {
                System.err.println(line);
                e.printStackTrace();
            }
        }
        return accidents;
    }

    public List<EdgeAttachment> readAccidents_2015(String csvFilepath) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(csvFilepath));
        List<EdgeAttachment> accidents = new ArrayList<>();
        List<String[]> entries = csvReader.readAll();
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy H:mm");
        for (String[] fields : entries.subList(1, entries.size())) {
            try {
                String eventId = fields[0];
                String streetName = fields[5];
                int direction = Integer.parseInt(fields[6]);
                Date time = format.parse(fields[4]);
                double lat = Double.valueOf(fields[3]), lng = Double.valueOf(fields[2]);
                Coordinate location = new Coordinate(lng, lat);
                EdgeAttachment accident = new EdgeAttachment(eventId, location);
                accident.setDirection(direction);
                accident.setStreetName(streetName);
                accident.setType(EdgeAttachment.ACCIDENT);
                accident.setTime(time);
                accidents.add(accident);
            } catch (Exception e) {
                System.err.println(String.join(",", (CharSequence[]) fields));
                e.printStackTrace();
            }
        }
        return accidents;
    }
}
