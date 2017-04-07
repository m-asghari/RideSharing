package edu.usc.infolab.geo.semanticroute.demo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.io.ParseException;
import edu.usc.infolab.geo.model.IndexedDirectedGraph;
import edu.usc.infolab.geo.semanticroute.model.Route;
import edu.usc.infolab.geo.semanticroute.util.NaturalRouteConstructor;
import edu.usc.infolab.geo.ui.MapViewer;
import edu.usc.infolab.geo.util.MapDataHelper;
import edu.usc.infolab.geo.util.TestCaseBuilder;
import edu.usc.infolab.geo.util.Utility;
import edu.usc.infolab.geo.util.WGS2MetricTransformer;
import org.apache.commons.io.FilenameUtils;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapViewport;
import org.geotools.swing.JMapFrame;
import org.opengis.referencing.operation.TransformException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Generate graph for testing.
 * Created by yaguang on 3/7/16.
 */
public class TestCaseBuilderDemo {


    public static void main(String[] args) throws TransformException, ParseException, IOException {
//        testDrawNaturalRoute();
        testBuildGraph();
    }

    public static void testDrawNaturalRoute() throws ParseException, TransformException, IOException {
        IndexedDirectedGraph graph = MapDataHelper.getLAGraph();
        WGS2MetricTransformer transformer = WGS2MetricTransformer.LATransformer;
        System.out.println("Edge:" + graph.getEdges().size() + " Node:" + graph.getNodes().size());
        NaturalRouteConstructor constructor = new NaturalRouteConstructor(graph);
        List<Route> naturalRoutes = constructor.constructRoutes();
        System.out.println("Route #: " + naturalRoutes.size());
        String caseFilename = FilenameUtils.concat(Utility.getProperty("test_data_dir"),
                "semanticroute/natural_routes.txt");
        PrintWriter pw = new PrintWriter(caseFilename);
        String linkFilename = Utility.getProperty("la_link_shp_file");
        MapViewer viewer = new MapViewer(linkFilename);
        int nameRouteNum = 0;
        int nameRouteEdgeNum = 0;
        for (int i = 0; i < naturalRoutes.size(); ++i) {
            Route route = naturalRoutes.get(i);
            if (!route.getSummary().isEmpty()) {
                nameRouteNum += 1;
                nameRouteEdgeNum += route.getEdges().size();
            }
            Coordinate start = transformer.toWGS84(route.getStartCoordinate()),
                    end = transformer.toWGS84(route.getEndCoordinate());
            pw.println(route.toString());
            pw.println(String.format("(%.5f,%.5f)->(%.5f,%.5f)", start.x, start.y, end.x, end.y));
            pw.println(route.getNodeListString());
//            viewer.drawLine(transformer.toWGS84(RouteHelper.getCoordinateList(route)), "NR_" + i, MapViewer.PATH_LAYER_NAME);
//            List<Coordinate> points = Arrays.asList(start, end);
//            viewer.drawPoint(points, "NR_P_" + i, MapViewer.POINT_LAYER_NAME);
        }
        pw.close();
        System.out.println("Name Route #:" + nameRouteNum);
        System.out.println("Average Node #:" + nameRouteEdgeNum * 1.0 / nameRouteNum);
        JMapFrame.showMap(viewer.getMapContent());
        MapViewport viewport = viewer.getMapContent().getViewport();
        ReferencedEnvelope maxBounds = new ReferencedEnvelope();
        Coordinate bottomLeft = new Coordinate(-118.288, 34.028),
                topRight = new Coordinate(-118.282, 34.032);
        maxBounds.init(bottomLeft, topRight);
        viewport.setBounds(maxBounds);
    }

    public static void testBuildGraph() throws TransformException, ParseException, IOException {
        String filename = FilenameUtils.concat(Utility.getProperty("test_data_dir"),
                "semanticroute/route_partition_case.txt");
        TestCaseBuilder builder = new TestCaseBuilder(filename);
        builder.visualize();
    }
}
