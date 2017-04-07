package edu.usc.infolab.geo.semanticroute.demo;

import com.vividsolutions.jts.io.ParseException;
import edu.usc.infolab.geo.model.IndexedDirectedGraph;
import edu.usc.infolab.geo.semanticroute.dao.RouteBean;
import edu.usc.infolab.geo.semanticroute.dao.RouteBeanHelper;
import edu.usc.infolab.geo.semanticroute.model.Route;
import edu.usc.infolab.geo.semanticroute.model.RoutePartition;
import edu.usc.infolab.geo.semanticroute.util.BasicRoutePartitioner;
import edu.usc.infolab.geo.semanticroute.util.BasicRouteRetriever;
import edu.usc.infolab.geo.semanticroute.util.NaturalRouteConstructor;
import edu.usc.infolab.geo.semanticroute.util.RouteHelper;
import edu.usc.infolab.geo.util.MapDataHelper;
import edu.usc.infolab.geo.util.Utility;
import org.apache.commons.io.FilenameUtils;
import org.geotools.graph.path.Walk;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by yaguang on 5/10/16.
 */
public class ExperimentsDemo {

    private IndexedDirectedGraph graph = null;
    private List<Route> knowledgeRoutes = null;
    private List<Route> naturalRoutes = null;
    private List<Route> queryRoutes = null;
    private double ratio = 0.2;  // percentage of data used for testing.
    private int maxTrainSize = 150;

    public ExperimentsDemo() {
        try {
            graph = MapDataHelper.getLAGraph();
            knowledgeRoutes = getKnowledgeRoutes();
            naturalRoutes = getNaturalRoutes();
            // Splits the data into training and testing.
            int testSize = (int) Math.round(ratio * knowledgeRoutes.size());
            long seed = 12345;
            Collections.shuffle(knowledgeRoutes, new Random(seed));
            queryRoutes = knowledgeRoutes.subList(0, testSize);
            knowledgeRoutes = knowledgeRoutes.subList(testSize, knowledgeRoutes.size());
        } catch (IOException | ParseException | SQLException | TransformException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws SQLException, FileNotFoundException {
        System.out.println("Experiments.");
//        knowledgeRouteSizeVsNumofRouteSegments();
        ExperimentsDemo demo = new ExperimentsDemo();
//        demo.routeSizeVsTime();
//        demo.routeSizeVsTimeSingleRoute();
        demo.knowledgeRouteNumVsTime();
//        demo.lambdaAndKnowledgeVsNumOfRouteSegments();
//        demo.maxerrordistAndKnowledgeNumberVsKnowledgeCoverage();
    }


    private void knowledgeRouteNumVsTime() throws FileNotFoundException {
        String pattern = "yyyyMMddhhmmss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        String output = formatter.format(new Date());
        String outputFilename = FilenameUtils.concat(Utility.getProperty("experiments_dir"),
                "knowledge_route_num_vs_time_" + output + ".txt");
        PrintWriter pw = new PrintWriter(outputFilename);
        for (int maxKnowledgeRoute = 250; maxKnowledgeRoute <= 280; maxKnowledgeRoute += 10) {
            BasicRouteRetriever retriever = new BasicRouteRetriever(graph);
            retriever.addRoutes(naturalRoutes);
            retriever.addRoutes(knowledgeRoutes.subList(0, maxKnowledgeRoute));
            BasicRoutePartitioner partitioner = new BasicRoutePartitioner(retriever);
            long start = System.currentTimeMillis();
            double segmentNum = doPartitionAndRecordStatistic(queryRoutes, null, partitioner);
            long end = System.currentTimeMillis();
            long elapsed = end - start;
            pw.println(String.format("%d\t%d\t%.1f", maxKnowledgeRoute, elapsed, segmentNum));
            pw.flush();
        }
        pw.close();
    }

    private void routeSizeVsTime() throws FileNotFoundException {
        String pattern = "yyyyMMddhhmmss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        String output = formatter.format(new Date());
        String outputFilename = FilenameUtils.concat(Utility.getProperty("experiments_dir"),
                "route_size_vs_time_" + output + ".txt");
        PrintWriter pw = new PrintWriter(outputFilename);
        BasicRouteRetriever retriever = new BasicRouteRetriever(graph);
        // Adds natural routes.
        retriever.addRoutes(naturalRoutes);

        // Adds knowledge routes.
        int maxKnowledgeRoute = 100;
        retriever.addRoutes(knowledgeRoutes.subList(0, maxKnowledgeRoute));
        BasicRoutePartitioner partitioner = new BasicRoutePartitioner(retriever);

        // Build query routes.
        List<Route> baseRoutes = new ArrayList<>();
        int baseRouteNum = 20, minRouteSize = 100;
        for (int i = 0; i < queryRoutes.size(); ++i) {
            if (queryRoutes.get(i).size() >= minRouteSize) {
                baseRoutes.add(queryRoutes.get(i));
                if (baseRoutes.size() >= baseRouteNum) {
                    break;
                }
            }
        }
        for (int i = 1; i < minRouteSize; i++) {
            long start = System.currentTimeMillis();
            int validCount = 0;
            int segmentSize = 0;
            for (Route baseRoute : baseRoutes) {
                try {
                    String routeId = baseRoute.getRouteId() + "_" + i;
                    Route newRoute = new Route(baseRoute.getNodes().subList(0, i),
                            routeId, baseRoute.getRouteType(), baseRoute.getScore());
                    RoutePartition parition = partitioner.partition(newRoute);
                    validCount += 1;
                    segmentSize += parition.size();
//            RouteDescriptionGenerator routeDescriptionGenerator = new RouteDescriptionGenerator()
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            long elapsed = System.currentTimeMillis() - start;
            pw.println(String.format("%s\t%.1f\t%.1f", i, elapsed * 1.0 / validCount, segmentSize * 1.0 / validCount));
        }
        pw.close();
    }

    private void routeSizeVsTimeSingleRoute() throws FileNotFoundException {
        String pattern = "yyyyMMddhhmmss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        String output = formatter.format(new Date());
        String outputFilename = FilenameUtils.concat(Utility.getProperty("experiments_dir"),
                "route_size_vs_time_" + output + ".txt");
        PrintWriter pw = new PrintWriter(outputFilename);
        BasicRouteRetriever retriever = new BasicRouteRetriever(graph);
        // Adds natural routes.
        retriever.addRoutes(naturalRoutes);

        // Adds knowledge routes.
        int maxKnowledgeRoute = 100;
        retriever.addRoutes(knowledgeRoutes.subList(0, maxKnowledgeRoute));
        BasicRoutePartitioner partitioner = new BasicRoutePartitioner(retriever);

        // Build query routes.
        Route baseRoute = queryRoutes.get(8);
        List<Route> routesVarSize = new ArrayList<>();
        int size = baseRoute.size();
        for (int i = 20; i < size; i++) {
            String routeId = baseRoute.getRouteId() + "_" + i;
            Route newRoute = new Route(baseRoute.getNodes().subList(0, i),
                    routeId, baseRoute.getRouteType(), baseRoute.getScore());
            routesVarSize.add(newRoute);
        }

        // Does partition and records statistics.
        doPartitionAndRecordStatistic(routesVarSize, pw, partitioner);
        pw.close();
    }

    private static void knowledgeRouteSizeVsNumofRouteSegments() throws SQLException, FileNotFoundException {
        ExperimentsDemo demo = new ExperimentsDemo();
//        for (int knowledgeSize : new int[]{250, 200, 150, 100, 50}) {
        for (int knowledgeSize : new int[]{250, 200, 150, 100, 50}) {
            demo.doExperiment(knowledgeSize);
        }
    }

    public void maxerrordistAndKnowledgeNumberVsKnowledgeCoverage() throws FileNotFoundException {
        String pattern = "yyyyMMddhhmmss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        String output = formatter.format(new Date());
        String outputFilename = FilenameUtils.concat(Utility.getProperty("experiments_dir"),
                "error_vs_knowledge_coverage_" + output + ".txt");
        PrintWriter pw = new PrintWriter(outputFilename);
        List<Double> errorDists = new ArrayList<>();
        List<Integer> knowledgeRouteSizes = new ArrayList<>();
        for (int knowledgeRouteSize = 10; knowledgeRouteSize <= 250; knowledgeRouteSize += 20) {
            knowledgeRouteSizes.add(knowledgeRouteSize);
        }
        for (double error = 0; error <= 0; error += 0.1) {
            errorDists.add(error);
        }
        for (double error : errorDists) {
            for (int knowledgeRouteSize : knowledgeRouteSizes) {
                BasicRouteRetriever retriever = new BasicRouteRetriever(graph);
                retriever.addRoutes(naturalRoutes);
                retriever.addRoutes(knowledgeRoutes.subList(0, knowledgeRouteSize));
                BasicRoutePartitioner partitioner = new BasicRoutePartitioner(retriever);
                partitioner.setLambda(error);
                double totalCoverage = 0;
                List<Route> routes = queryRoutes;
                int validSize = 0;
                for (Route queryRoute : routes) {
                    try {
                        long start = System.currentTimeMillis();
                        RoutePartition parition = partitioner.partition(queryRoute);
                        long elapsed = System.currentTimeMillis() - start;
                        int nodeNum = queryRoute.size();
                        totalCoverage += parition.getKnowledgeCoverage();
                        validSize += 1;
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }

                String line = String.format("%.2f\t%d\t%.2f", error, knowledgeRouteSize, totalCoverage / validSize);
                pw.println(line);
                pw.flush();
            }
        }
        pw.close();
    }

    public void lambdaAndKnowledgeVsNumOfRouteSegments() throws FileNotFoundException {
        String pattern = "yyyyMMddhhmmss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        String output = formatter.format(new Date());
        String outputFilename = FilenameUtils.concat(Utility.getProperty("experiments_dir"),
                "lambda_vs_num_of_route_segments_" + output + ".txt");
        PrintWriter pw = new PrintWriter(outputFilename);
        List<Double> lambdas = new ArrayList<>();
        List<Integer> knowledgeRouteSizes = new ArrayList<>();
        for (int knowledgeRouteSize = 10; knowledgeRouteSize <= 250; knowledgeRouteSize += 20) {
            knowledgeRouteSizes.add(knowledgeRouteSize);
        }
        for (double lambda = 0; lambda <= 0.1; lambda += 0.1) {
            lambdas.add(lambda);
        }
        for (double lambda : lambdas) {
            for (int knowledgeRouteSize : knowledgeRouteSizes) {
                BasicRouteRetriever retriever = new BasicRouteRetriever(graph);
                retriever.addRoutes(naturalRoutes);
                retriever.addRoutes(knowledgeRoutes.subList(0, knowledgeRouteSize));
                BasicRoutePartitioner partitioner = new BasicRoutePartitioner(retriever);
                partitioner.setLambda(lambda);
                double segmentNum = doPartitionAndRecordStatistic(queryRoutes.subList(0, 30), null, partitioner);
                String line = String.format("%.2f\t%d\t%.1f", lambda, knowledgeRouteSize, segmentNum);
                pw.println(line);
                pw.flush();
            }
        }
        pw.close();
    }

    public void doExperiment(int maxKnowledgeRoute) throws SQLException, FileNotFoundException {
        String pattern = "yyyyMMddhhmmss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        String output = formatter.format(new Date());
        String outputFilename = FilenameUtils.concat(Utility.getProperty("experiments_dir"),
                "infomration_compression_" + maxKnowledgeRoute + "_" + output + ".txt");
        PrintWriter pw = new PrintWriter(outputFilename);
        pw.println("#Knowledge\t" + knowledgeRoutes.size());
        pw.println("#Natural\t" + naturalRoutes.size());
        pw.println("#Query\t" + queryRoutes.size());
        pw.flush();
//        PlaceHelper placeHelper = new PlaceHelper();
//        for (Route route : queryRoutes) {
//            System.out.println(route.toString());
//            PlaceBean fromPlace = placeHelper.getPlace(route.getFromPlace()),
//                    toPlace = placeHelper.getPlace(route.getToPlace());
//            System.out.println(fromPlace.getSummary() + "->" + toPlace.getSummary());
//        }
        // Do route partition without knowledge routes.
        BasicRouteRetriever retriever = new BasicRouteRetriever(graph);
        retriever.addRoutes(naturalRoutes);
        BasicRoutePartitioner partitioner = new BasicRoutePartitioner(retriever);
        long start = System.currentTimeMillis();
        double segmentNum = 0;
        segmentNum = doPartitionAndRecordStatistic(queryRoutes, pw, partitioner);
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        pw.println("Total\t" + segmentNum);
        pw.println("Elapsed\t" + elapsed);

        retriever.addRoutes(knowledgeRoutes.subList(0, maxKnowledgeRoute));
        start = System.currentTimeMillis();
        segmentNum = doPartitionAndRecordStatistic(queryRoutes, pw, partitioner);
        end = System.currentTimeMillis();
        elapsed = end - start;
        pw.println("Total\t" + segmentNum);
        pw.println("Elapsed\t" + elapsed);
        pw.close();
    }

    private double doPartitionAndRecordStatistic(List<Route> routes, PrintWriter pw, BasicRoutePartitioner partitioner) {
        double segmentNum = 0;
        int validSize = 0;
        for (Route queryRoute : routes) {
            try {
                long start = System.currentTimeMillis();
                RoutePartition parition = partitioner.partition(queryRoute);
                long elapsed = System.currentTimeMillis() - start;
                int nodeNum = queryRoute.size();
//            RouteDescriptionGenerator routeDescriptionGenerator = new RouteDescriptionGenerator()
                if (pw != null) {
                    pw.println(String.format("%s\t%d\t%d\t%d", queryRoute.getRouteId(),
                            nodeNum, parition.size(), elapsed));
                    pw.flush();
                }
                segmentNum += parition.size();
                validSize += 1;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return segmentNum / validSize;
    }

    private List<Route> getNaturalRoutes() throws SQLException, TransformException, ParseException, IOException {
        NaturalRouteConstructor naturalRouteConstructor = new NaturalRouteConstructor(graph);
        return naturalRouteConstructor.constructRoutes();
    }

    private List<Route> getKnowledgeRoutes() throws SQLException, MismatchedDimensionException,
            IOException, ParseException, TransformException {
        List<Route> routes = new ArrayList<>();
        RouteBeanHelper rbh = new RouteBeanHelper();
        List<RouteBean> routeBeans = rbh.getAllRoutes();
        for (RouteBean routeBean : routeBeans) {
            Walk walk = RouteHelper.getWalk(routeBean.getNodeIdSequence(), graph);
            String summary = "";
            @SuppressWarnings("unchecked")
            Route route = new Route(walk, "K_" + routeBean.getRoute_id(), Route.RouteType.KNOWN, 1.0, summary,
                    routeBean.getFrom_place_id(), routeBean.getTo_place_id());
            routes.add(route);
        }
        return routes;
    }
}
