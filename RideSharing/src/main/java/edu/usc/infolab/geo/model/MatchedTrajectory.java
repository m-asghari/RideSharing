package edu.usc.infolab.geo.model;

import com.vividsolutions.jts.geom.Coordinate;
import edu.usc.infolab.geo.util.EdgeHelper;
import org.geotools.graph.structure.DirectedEdge;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by yaguang on 3/20/16.
 */
public class MatchedTrajectory {
    private Trajectory trajectory;
    private List<EdgeWithTime> edgeWithTimes;

    public Trajectory getTrajectory() {
        return trajectory;
    }

    public List<EdgeWithTime> getEdgeWithTimes() {
        return edgeWithTimes;
    }

    public MatchedTrajectory(Trajectory trajectory, List<EdgeWithTime> records) {
        this.trajectory = trajectory;
        this.edgeWithTimes = records;
    }

    public List<Coordinate> toCoordinateList() {
        List<Coordinate> coordinates = edgeWithTimes.stream()
                .map(edgeWithTime -> new EdgeHelper(edgeWithTime.edge).getStartCoordinate()).collect(Collectors.toList());
        coordinates.add(new EdgeHelper(edgeWithTimes.get(edgeWithTimes.size() - 1).edge).getEndCoordinate());
        return coordinates;
    }

    /**
     * Edge labeled with time. i.e., a moving object passed the start node at startTime, and the end node at endTime.
     */
    public static class EdgeWithTime {
        private DirectedEdge edge;
        private DateTime startTime;
        private DateTime endTime;
        private final String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

        public DateTime getStartTime() {
            return startTime;
        }

        public DateTime getEndTime() {
            return endTime;
        }

        public DirectedEdge getEdge() {
            return edge;
        }

        public EdgeWithTime(DirectedEdge edge, DateTime start, DateTime end) {
            this.edge = edge;
            if (start.compareTo(end) >= 0) {
                throw new IllegalArgumentException(String.format("%s is greater than %s!", start, end));
            } else {
                this.startTime = start;
                this.endTime = end;
            }
        }

        public EdgeWithTime(DirectedEdge edge, long start, long end) {
            this(edge, new DateTime(start), new DateTime(end));
        }

        @Override
        public String toString() {
            return String.format("%s,%s,%s", new EdgeHelper(edge).getInnerEdgeId(),
                    startTime.toString(pattern),
                    endTime.toString(pattern));
        }

        public String toString(DateTimeZone timezone) {
            EdgeHelper eh = new EdgeHelper(edge);
            return String.format("%s,%s,%s,%.1f m/s", eh.getInnerEdgeId(),
                    startTime.withZone(timezone).toString(pattern),
                    endTime.withZone(timezone).toString(pattern),
                    eh.getLength() / (endTime.getMillis() - startTime.getMillis()) * 1000
            );
        }

    }
}
