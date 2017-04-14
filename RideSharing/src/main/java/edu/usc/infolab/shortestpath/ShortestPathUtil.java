package edu.usc.infolab.shortestpath;

import com.vividsolutions.jts.geom.Coordinate;
import edu.usc.infolab.geo.model.IndexedDirectedGraph;
import edu.usc.infolab.geo.util.MapDataHelper;
import edu.usc.infolab.geo.util.route.PathWithStat;
import edu.usc.infolab.geom.GPSPoint;
import org.geotools.graph.path.Path;

public class ShortestPathUtil {
    private static IndexedDirectedGraph graph;

    public static void InitializeNYCGraph() throws Exception{
        graph = MapDataHelper.getNYCGraph();
    }


    public static double GetShortestPath(double srcLat, double srcLng, double destLat, double destLng) throws Exception {
        Coordinate source = new Coordinate(srcLng, srcLat),
                destination = new Coordinate(destLng, destLat);
        PathWithStat pathWithStat = graph.queryShortestPath(source, destination);
        return pathWithStat.getDistance();
    }
}
