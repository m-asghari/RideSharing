package edu.usc.infolab.geo.semanticroute.util;

import edu.usc.infolab.geo.semanticroute.model.RouteSegment;

public class RouteSegmentEstimator {
    public static double getScore(RouteSegment routeSegment) {
        double rsLength = routeSegment.getLength();
        return routeSegment.getRoute().getScore() * Math.pow(rsLength / routeSegment.getRoute().getLength(), 2);
    }
}
