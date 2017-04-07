package edu.usc.infolab.shortestpath;

import com.vividsolutions.jts.geom.Coordinate;
import edu.usc.infolab.geo.model.IndexedDirectedGraph;
import edu.usc.infolab.geo.util.MapDataHelper;
import edu.usc.infolab.geo.util.route.PathWithStat;

public class ShortestPathUtil {

    public static void GetShortestPath() throws Exception {
        IndexedDirectedGraph graph = MapDataHelper.getNYCGraph();
        Coordinate source = new Coordinate(-74.004420, 40.741781),  // Google NYC
                destination = new Coordinate(-73.98228, 40.75742);
        PathWithStat pathWithStat = graph.queryShortestPath(source, destination);
        System.out.println(String.format("Distance: %.1fm", pathWithStat.getDistance()));
    }
}
