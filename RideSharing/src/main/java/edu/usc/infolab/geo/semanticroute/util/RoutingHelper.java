package edu.usc.infolab.geo.semanticroute.util;

import com.google.gson.JsonObject;
import com.vividsolutions.jts.geom.Coordinate;
import edu.usc.infolab.geo.model.IndexedDirectedGraph;
import edu.usc.infolab.geo.semanticroute.model.*;
import edu.usc.infolab.geo.util.EdgeHelper;
import edu.usc.infolab.geo.util.NodeHelper;
import edu.usc.infolab.geo.util.WGS2MetricTransformer;
import org.geotools.graph.path.Walk;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.opengis.referencing.operation.TransformException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class RoutingHelper {
    IndexedDirectedGraph graph;
    WGS2MetricTransformer transformer;

    public RoutingHelper(IndexedDirectedGraph graph) {
        this.graph = graph;
        this.transformer = graph.getTransformer();
    }

    private RLocation toWGS84RLocation(Coordinate coordinate) {
        RLocation location = null;
        try {
            location = new RLocation(transformer.toWGS84(coordinate));
        } catch (TransformException e) {
            e.printStackTrace();
        }
        return location;
    }

    private RLocation toWGS84RLocation(Node node) {
        return toWGS84RLocation(new NodeHelper(node).getCoordinate());
    }

    /**
     * TODO(test polyline).
     *
     * @param route
     * @return
     */
    public JsonObject routeToJsonObject(Route route) {
        JsonObject jsonObject = new JsonObject();
        List<RLocation> points = route.getCoordinateList().stream().
                map(this::toWGS84RLocation).collect(Collectors.toList());
        RPolyline polyline = new RPolyline(points);
        jsonObject.add("geometry", polyline.toJsonElement());
        jsonObject.addProperty("fromPlace", route.getFromPlace());
        jsonObject.addProperty("toPlace", route.getToPlace());
        jsonObject.addProperty("routeId", route.getRouteId());
        jsonObject.addProperty("score", route.getScore());
        jsonObject.addProperty("summary", route.getSummary());
        jsonObject.addProperty("routeType", route.getRouteTypeString());
        return jsonObject;
    }

    RPolyline getPolyline(Route route) {
        List<RLocation> locations = route.getNodes().stream().map(this::toWGS84RLocation).collect(Collectors.toList());
        return new RPolyline(locations);
    }


    private List<RStep> generateRStepsFromKRSegment(RouteSegment prevRouteSegment, RouteSegment currentRouteSegment) {
        Walk currentWalk = currentRouteSegment.getWalk();

        Walk previousWalk = null;
        if (prevRouteSegment != null) {
            previousWalk = prevRouteSegment.getWalk();
        }
        List<Walk> subWalks = new ArrayList<>();

        // Add the previous walk.
        subWalks.add(previousWalk);
        // Splits current walk into several parts according to route name.
        List<Edge> currentEdges = currentWalk.getEdges();
        Walk subWalk = new Walk();
        Edge prevEdge = null;
        for (Edge currentEdge : currentEdges) {
            String currentName = new EdgeHelper(currentEdge).getStreetName();
            if (prevEdge != null && !currentName.equals(new EdgeHelper(prevEdge).getStreetName())) {
                subWalks.add(subWalk);
                subWalk = new Walk();
            }
            subWalk.addEdge(currentEdge);
            prevEdge = currentEdge;
        }
        // Adds the last sub-walk.
        if (!subWalk.isEmpty()) {
            subWalks.add(subWalk);
        }
        List<RStep> steps = new ArrayList<>();
        for (int i = 1; i < subWalks.size(); ++i) {
            RStepManeuver rStepManeuver = getStepManeuver(subWalks.get(i - 1), subWalks.get(i));
            List<RIntersection> intersections = getrIntersections(subWalks.get(i));
            String name = new EdgeHelper((Edge) subWalks.get(i).getEdges().get(0)).getStreetName();
            RStep step = new RStep(name, intersections, rStepManeuver);
            steps.add(step);
        }

        return steps;
    }

    /**
     * Calculates maneuver based the relationship the current walk with the previous one.
     *
     * @param previousWalk
     * @param currentWalk
     * @return
     */
    private RStepManeuver getStepManeuver(Walk previousWalk, Walk currentWalk) {
        String maneuverType;
        RLocation maneuverLocation = toWGS84RLocation(currentWalk.getFirst());
        RLocation afterManeuverLocation = toWGS84RLocation((Node) currentWalk.get(1));
        RBearing bearingBefore = new RBearing(0);
        RBearing bearingAfter = RBearing.getBearing(maneuverLocation, afterManeuverLocation);
        if (previousWalk == null) {
            maneuverType = RStepManeuver.DEPART;
        } else {
            maneuverType = RStepManeuver.TURN;
            // TODO(yaguang) Remove hard-coded number.
            RLocation beforeManeuverLocation = toWGS84RLocation(new NodeHelper(
                    (Node) previousWalk.get(previousWalk.size() - 2)).getCoordinate());
            bearingBefore = RBearing.getBearing(beforeManeuverLocation, maneuverLocation);
        }
        RStepManeuver maneuver = new RStepManeuver(maneuverLocation, bearingBefore, bearingAfter, maneuverType);
        return maneuver;
    }


    private List<RStep> generateRStepsFromNRSegment(RouteSegment prevRouteSegment, RouteSegment currentRouteSegment) {
        String name = currentRouteSegment.getRoute().getSummary();
        Walk walk = currentRouteSegment.getWalk();
        List<RIntersection> intersections = getrIntersections(walk);
        Walk previousWalk = null, currentWalk = currentRouteSegment.getWalk();
        if (prevRouteSegment != null)
            previousWalk = prevRouteSegment.getWalk();
        RStepManeuver maneuver = getStepManeuver(previousWalk, currentWalk);
        List<RStep> rSteps = new ArrayList<>();
        rSteps.add(new RStep(name, intersections, maneuver));
        return rSteps;
    }

    /**
     * Gets intersection from walk.
     *
     * @param walk
     * @return
     */
    private List<RIntersection> getrIntersections(List<Node> walk) {
        List<RIntersection> intersections = new ArrayList<>();
        for (Node node : walk) {
            NodeHelper nodeHelper = new NodeHelper(node);
            String nodeId = String.valueOf(nodeHelper.getInnerNodeId());
            RLocation location = toWGS84RLocation(nodeHelper.getCoordinate());
            RIntersection intersection = new RIntersection(location, nodeId);
            intersections.add(intersection);
        }
        return intersections;
    }

    private RSegment generateRSegment(RouteSegment prevRouteSegment, RouteSegment currentRouteSegment) {
        Route sourceRoute = currentRouteSegment.getRoute();
        RPolyline sourceRoutePolyline = getPolyline(sourceRoute);
        List<RStep> rSteps = null;
        RSegment rSegment = null;
        if (currentRouteSegment.getRouteType() == Route.RouteType.NATURAL) {
            // Contains only one steps.
            rSteps = generateRStepsFromNRSegment(prevRouteSegment, currentRouteSegment);
            rSegment = new RSegment(rSteps, sourceRoutePolyline);
        } else if (currentRouteSegment.getRouteType() == Route.RouteType.KNOWN) {
            // Contains multiple steps.
            rSteps = generateRStepsFromKRSegment(prevRouteSegment, currentRouteSegment);
            String summary = String.format("Drive towards %s via %s", sourceRoute.getToPlace(), sourceRoute.getSummary());
            rSegment = new RSegment(rSteps, sourceRoutePolyline, summary, RSegment.KNOWN_SEGMENT);
        }
        return rSegment;
    }

    private RSegment generateArrivalRSegment(RouteSegment routeSegment) {
        List<RStep> rSteps = new ArrayList<>();
        RLocation location = toWGS84RLocation(new NodeHelper(routeSegment.getWalk().getLast()).getCoordinate());
        RStepManeuver rStepManeuver = new RStepManeuver(location, null, null, RStepManeuver.ARRIVE);
        RStep rStep = new RStep("", null, rStepManeuver);
        rSteps.add(rStep);
        return new RSegment(rSteps, null);
    }


    /**
     * Converts a route partition into a format that is suitable for routing.
     *
     * @param routePartition
     * @return
     */
    public RRoute generateRoute(RoutePartition routePartition) {
        List<RouteSegment> routeSegments = routePartition.getSegments();
        List<RSegment> rSegments = new ArrayList<>();
        int size = routeSegments.size();
        for (int i = 0; i < size; ++i) {
            RouteSegment prevRouteSegment = null;
            RouteSegment currentRouteSegment = routeSegments.get(i);
            if (i > 0) {
                prevRouteSegment = routeSegments.get(i - 1);
            }
            RSegment rSegment = generateRSegment(prevRouteSegment, currentRouteSegment);
            rSegments.add(rSegment);
        }
        rSegments = mergeShortSegments(rSegments);
        return new RRoute(rSegments);
    }


    private RSegment mergeSegments(RSegment segment1, RSegment segment2) {
        List<RStep> steps = new ArrayList<>();
        steps.addAll(segment1.getSteps());
        steps.addAll(segment2.getSteps());
        List<RLocation> locations = new ArrayList<>();
        locations.addAll(segment1.getGeometry().toLocationList());
        locations.addAll(segment2.getGeometry().toLocationList());
        String segType = segment1.getType();
        String summary = segment1.getSummary();
        // Determine segType
        RSegment newSegment = new RSegment(steps, new RPolyline(locations), summary, segType);
        return newSegment;
    }

    /**
     * Merges RSegment with no name or too short name.
     *
     * @param rSegments
     * @return
     */
    private List<RSegment> mergeShortSegments(List<RSegment> rSegments) {
        List<RSegment> mergedSegments = new ArrayList<>();
        // Merges short natural segments with no name.
        double lengthThreshold = 300;  // 300 m
        int currentIndex = 0;
        while (currentIndex < rSegments.size()) {
            RSegment currentSegment = rSegments.get(currentIndex);
            RSegment prevSegment =
                    mergedSegments.isEmpty() ? null : mergedSegments.get(mergedSegments.size() - 1);
            if (!currentSegment.hasStreetName() && prevSegment != null) {
                RSegment mergedSegment = mergeSegments(prevSegment, currentSegment);
                mergedSegments.set(mergedSegments.size() - 1, mergedSegment);
            } else {
                mergedSegments.add(currentSegment);
            }
            currentIndex++;
        }
        // Merge small segments into
        return mergedSegments;
    }


}
