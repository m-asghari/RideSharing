package edu.usc.infolab.geo.semanticroute.util;

import edu.usc.infolab.geo.semanticroute.model.Route;
import edu.usc.infolab.geo.semanticroute.model.Route.RouteType;
import edu.usc.infolab.geo.semanticroute.model.RoutePartition;
import edu.usc.infolab.geo.semanticroute.model.RouteSegment;
import edu.usc.infolab.geo.util.EdgeHelper;
import org.geotools.graph.structure.Edge;

import java.util.List;

/**
 * Generates route description based on the route partition.
 *
 * @author Yaguang
 */
public class RouteDescriptionGenerator {
    RoutePartition partition = null;

    public RouteDescriptionGenerator(RoutePartition partition) {
        this.partition = partition;
    }

    public String getRouteDescription() {
        List<RouteSegment> segments = partition.getSegments();
        StringBuilder sb = new StringBuilder();
        for (RouteSegment segment : segments) {
            sb.append(describe(segment) + "\n");
        }
        return sb.toString();
    }

    private String describe(RouteSegment segment) {
        Route route = segment.getRoute();
        double length = segment.getLength();
        StringBuilder sb = new StringBuilder();
        if (route.getRouteType() == RouteType.NATURAL) {
            List<Edge> edges = route.getEdges();
            EdgeHelper eh = new EdgeHelper(edges.get(0));
            sb.append(String.format("Go along %s for %.0fm", eh.getStreetName(), length));
        } else if (route.getRouteType() == RouteType.KNOWN) {
            sb.append(
                    String.format("Go along knowledge route: %s for %.0fm", route.getRouteId(), length));
        }
        return sb.toString();
    }

    // Calculate type and modifier
    // Type: turn, depart, arrive, merge, on_ramp, off_ramp, continue
    // modifier: left, right, uturn, sharp_right, slight_right, sharp_left, slight_right

    enum ManeuverType {
        TURN,
        DEPART,
        ARRIVE,
        CONTINUE,
        ON_RAMP,
        OFF_RAMP,
        MERGE
    }

    enum Modifier {
        LEFT,
        RIGHT,
        UTURN,
        SHARP_RIGHT,
        SLIGHT_RIGHT,
        SHARP_LEFT,
        SLIGHT_LEFT
    }
}
