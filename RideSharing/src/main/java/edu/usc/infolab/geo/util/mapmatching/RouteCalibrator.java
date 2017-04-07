package edu.usc.infolab.geo.util.mapmatching;

import com.vividsolutions.jts.geom.Coordinate;
import edu.usc.infolab.geo.model.MatchedTrajectory;
import edu.usc.infolab.geo.model.Trajectory;
import org.geotools.graph.path.Walk;

import java.util.List;

public interface RouteCalibrator {
    public Walk calibrate(List<Coordinate> coords);

    public MatchedTrajectory calibrate(Trajectory trajectory);
}
