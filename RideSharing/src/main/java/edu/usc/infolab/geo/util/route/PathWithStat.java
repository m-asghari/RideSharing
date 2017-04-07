package edu.usc.infolab.geo.util.route;

import org.geotools.graph.path.Path;

/**
 * A path in the road network with attached statistic, e.g., distance.
 * Created by yaguang on 11/7/16.
 */
public class PathWithStat {
    private Path path;
    private double distance;

    public PathWithStat(Path path, double distance) {
        this.path = path;
        this.distance = distance;
    }

    public Path getPath() {
        return path;
    }

    public double getDistance() {
        return distance;
    }
}
