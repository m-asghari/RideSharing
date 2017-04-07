package edu.usc.infolab.geo.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import edu.usc.infolab.geo.model.IndexedDirectedGraph;
import org.geotools.data.*;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for read/write _map data file and related CRS transform.
 *
 * @author Yaguang
 */
public class MapDataHelper {

    private final static short TRAVEL_DIRECTION_FROM = 1;
    private final static short TRAVEL_DIRECTION_TO = 2;
    private final static short TRAVEL_DIRECTION_BOTH = 3;
    /**
     * When converting bidirectional edges to two directed edges, we add BASE_LINK_ID to the
     * original link_id to get the new link_id.
     */
    private final static long BASE_LINK_ID = 10000000000000L;

    private static Map<String, IndexedDirectedGraph> _graphs = new HashMap<String, IndexedDirectedGraph>();


    private static WGS2MetricTransformer getLATransformer() {
        return WGS2MetricTransformer.LATransformer;
    }

    private static WGS2MetricTransformer getBeijingTransformer() {
        return WGS2MetricTransformer.BeijingTransformer;
    }

    private static WGS2MetricTransformer getNYCTransformer() {
        return WGS2MetricTransformer.NYCTransformer;
    }

    public static IndexedDirectedGraph getLAGraph()
            throws MismatchedDimensionException, IOException, ParseException, TransformException {
        String key = "LA";
        IndexedDirectedGraph graph = _graphs.getOrDefault(key, null);
        if (graph == null) {
            graph = getGraph(Utility.getProperty("la_links_csv_file"), getLATransformer());
            _graphs.put(key, graph);
        }
        return graph;
    }

    public static IndexedDirectedGraph getLAGraphSmall()
            throws MismatchedDimensionException, IOException, ParseException, TransformException {
        return getGraph(Utility.getProperty("la_links_small_csv_file"), getLATransformer());
    }

    public static IndexedDirectedGraph getLAGraphTiny()
            throws MismatchedDimensionException, IOException, ParseException, TransformException {
        return getGraph(Utility.getProperty("la_links_tiny_csv_file"), getLATransformer());
    }

    public static IndexedDirectedGraph getBeijingGraph()
            throws MismatchedDimensionException, IOException, ParseException, TransformException {
        String key = "Beijing";
        IndexedDirectedGraph graph = _graphs.getOrDefault(key, null);
        if (graph == null) {
            graph = getGraph(Utility.getProperty("beijing_links_csv_file"), getBeijingTransformer());
            _graphs.put(key, graph);
        }
        return graph;
    }

    public static IndexedDirectedGraph getBeijingGraphForTest()
            throws MismatchedDimensionException, IOException, ParseException, TransformException {
        return getGraph(Utility.getProperty("beijing_links_test_csv_file"), getBeijingTransformer());
    }

    public static IndexedDirectedGraph getNYCGraph() throws ParseException, TransformException, IOException {
        String key = "NYC";
        IndexedDirectedGraph graph = _graphs.getOrDefault(key, null);
        if (graph == null) {
//            graph = getGraph(Utility.getProperty("nyc_links_connected_large_csv_file"), getNYCTransformer());
            graph = getGraph(Utility.getProperty("nyc_links_csv_file"), getNYCTransformer());
            _graphs.put(key, graph);
        }
        return graph;
    }

    public static IndexedDirectedGraph getGraph(String csvFilename, WGS2MetricTransformer transformer)
            throws MismatchedDimensionException, IOException, ParseException, TransformException {
        String delim = Utility.getProperty("default_delimiter");
        SimpleFeatureCollection collection =
                getFeatureCollectionFromCSV(csvFilename, delim, true, transformer);
        IndexedDirectedGraph graph =
                new IndexedDirectedGraph(collection, transformer);
        return graph;
    }

    @Deprecated
    static SimpleFeatureCollection getLALinkFeatureCollectionForTest()
            throws IOException, ParseException, MismatchedDimensionException, TransformException {
        String linksCSVFileName = Utility.getProperty("la_links_test_csv_file");
        String delim = Utility.getProperty("default_delimiter");
        SimpleFeatureCollection collection =
                getFeatureCollectionFromCSV(linksCSVFileName, delim, true, getLATransformer());
        return collection;
    }

    public static IndexedDirectedGraph getLAGraphForTest()
            throws MismatchedDimensionException, IOException, ParseException, TransformException {
        String linksCSVFileName = Utility.getProperty("la_links_test_csv_file");
        String delim = Utility.getProperty("default_delimiter");
        WGS2MetricTransformer transformer = getLATransformer();
        SimpleFeatureCollection collection =
                getFeatureCollectionFromCSV(linksCSVFileName, delim, true, transformer);
        IndexedDirectedGraph graph =
                new IndexedDirectedGraph(collection, transformer);
        return graph;
    }


    @Deprecated
    public static SimpleFeatureCollection getLALinkFeatureCollectionSmall()
            throws IOException, ParseException, MismatchedDimensionException, TransformException {
        String linksCSVFileName = Utility.getProperty("la_links_small_csv_file");
        String delim = Utility.getProperty("default_delimiter");
        WGS2MetricTransformer transformer = getLATransformer();
        SimpleFeatureCollection collection =
                getFeatureCollectionFromCSV(linksCSVFileName, delim, true, transformer);
        return collection;
    }

    /**
     * Gets the link with travel_direction reversed.
     *
     * @param link
     * @return
     */
    private static SimpleFeature getReverseLink(SimpleFeature link) {
        long link_id = (Long) link.getAttribute("link_id") + BASE_LINK_ID;
        long from_node_id = (Long) link.getAttribute("to_node_id");
        long to_node_id = (Long) link.getAttribute("from_node_id");
        LineString geom = (LineString) ((LineString) link.getDefaultGeometry()).reverse();
        SimpleFeature newLink = SimpleFeatureBuilder.copy(link);
        newLink.setAttribute("link_id", link_id);
        newLink.setAttribute("from_node_id", from_node_id);
        newLink.setAttribute("to_node_id", to_node_id);
        newLink.setAttribute("geom", geom);
        return SimpleFeatureBuilder.build(link.getFeatureType(), newLink.getAttributes(), "" + link_id);
    }

    /**
     * Gets {@link SimpleFeatureType} for generating shape file.
     *
     * @return
     */
    private static SimpleFeatureType getLinkFeatureTypeForShp() {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("links_shp");
        builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference system
        // Adds attributes in order.
        builder.add("link_id", Long.class);
        builder.length(15).add("name", String.class);
        builder.add("from_id", Long.class);
        builder.add("to_id", Long.class);
        builder.length(15).add("travel_dir", String.class);
        builder.add("the_geom", LineString.class);
        // build the type
        return builder.buildFeatureType();
    }

    private static SimpleFeatureType getLinkFeatureType() {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("links");
        builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference system
        // Adds attributes in order.
        builder.add("link_id", Long.class);
        builder.add("street_name", String.class);
        builder.add("name_default", String.class);
        builder.add("from_node_id", Long.class);
        builder.add("to_node_id", Long.class);
        builder.add("way_id", Long.class);
        builder.add("travel_direction", Short.class);
        builder.add("function_class", String.class);
        builder.add("length", Double.class);
        builder.add("geom", LineString.class);
        // build the type
        return builder.buildFeatureType();
    }

    private static String getDirectionDescription(int direction) {
        String description = "Both";
        if (direction == TRAVEL_DIRECTION_FROM) {
            description = "Forward";
        } else if (direction == TRAVEL_DIRECTION_TO) {
            description = "Backward";
        } else if (direction == TRAVEL_DIRECTION_BOTH) {
            description = "Both";
        }
        return description;
    }

    /**
     * Gets default LA feature collection to generateNodeId the shape file.
     *
     * @return
     * @throws IOException
     * @throws ParseException
     * @throws TransformException
     * @throws MismatchedDimensionException
     */
    private static SimpleFeatureCollection getFeatureCollectionForShp(String linksCSVFileName)
            throws IOException, ParseException, MismatchedDimensionException, TransformException {
        String delim = Utility.getProperty("default_delimiter");
        SimpleFeatureCollection collection =
                getFeatureCollectionFromCSV(linksCSVFileName, delim, false, null);
        SimpleFeatureType shpFeatureType = getLinkFeatureTypeForShp();
        DefaultFeatureCollection shpCollection =
                new DefaultFeatureCollection("links_shp", shpFeatureType);
        SimpleFeatureIterator iterator = collection.features();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(shpFeatureType);
        while (iterator.hasNext()) {
            SimpleFeature feature = iterator.next();
            int shpFeatureCount = shpFeatureType.getAttributeCount();
            Object[] values = new Object[shpFeatureCount];
            values[0] = feature.getAttribute("link_id");
            values[1] = feature.getAttribute("street_name");
            values[2] = feature.getAttribute("from_node_id");
            values[3] = feature.getAttribute("to_node_id");
            values[4] = getDirectionDescription((Short) feature.getAttribute("travel_direction"));
            values[5] = feature.getAttribute("geom");
            SimpleFeature shpFeature = builder.buildFeature(feature.getID(), values);
            shpCollection.add(shpFeature);
        }
        return shpCollection;
    }

    /**
     * Schema of Links: link_id: Long street_name: String from_node_id: Long to_node_id: Long
     * num_of_lanes: Integer way_id: Long travel_direction: Short function_class: String length:
     * double gemo: text
     *
     * @param line
     * @param delim
     * @param reader
     * @return
     * @throws ParseException
     * @throws TransformException
     * @throws MismatchedDimensionException
     */
    private static Object[] parseLinkLine(String line, String delim, WKTReader reader,
                                          WGS2MetricTransformer transformer)
            throws ParseException, MismatchedDimensionException, TransformException {

        String[] fields = line.split(delim);
        if (fields.length < 10) {
            throw new ParseException("Field length should be 10, current:" + fields.length + ", " + line);
        }

        Object[] values = new Object[10];

        // 0: link_id
        values[0] = Long.parseLong(fields[0]);
        // 1: street_name
        values[1] = fields[1];
        // 2. name_default
        values[2] = fields[2];
        // 3: from_node_id
        values[3] = Long.parseLong(fields[3]);
        // 4: to_node_id
        values[4] = Long.parseLong(fields[4]);
        // 5. way_id
        try {
            values[5] = Long.parseLong(fields[5]);
        } catch (NumberFormatException e) {
            values[5] = -1;
        }
        // 6. travel_direction
        values[6] = Short.parseShort(fields[6]);
        // 7. function_class
        values[7] = fields[7];
        // 8. length
        values[8] = Double.parseDouble(fields[8]);
        // 9. geom
        LineString lineString = (LineString) reader.read(fields[9]);
        if (transformer != null) {
            lineString = (LineString) transformer.fromWGS84(lineString);
        }
        values[9] = lineString;
        return values;
    }

    /**
     * Builds feature collection from CSV file.
     *
     * @param fileName
     * @param delim
     * @param handleTravelDirection whether to change a bidirectional edge to two unidirectional
     *                              edges.
     * @param transformer
     * @return
     * @throws IOException
     * @throws ParseException
     * @throws TransformException
     * @throws MismatchedDimensionException
     */
    private static SimpleFeatureCollection getFeatureCollectionFromCSV(String fileName, String delim,
                                                                       boolean handleTravelDirection,
                                                                       WGS2MetricTransformer transformer)
            throws IOException, ParseException, MismatchedDimensionException, TransformException {
        List<String> lines = Files.readAllLines(Paths.get(fileName), Charset.defaultCharset());
        return getFeatureCollectionFromCSVParallel(lines, delim, handleTravelDirection, transformer);
//        return getFeatureCollectionFromCSV(lines, delim, handleTravelDirection, transformer);
    }

    private static SimpleFeatureCollection getFeatureCollectionFromCSV(List<String> lines,
                                                                       String delim, boolean handleTravelDirection,
                                                                       WGS2MetricTransformer transformer)
            throws IOException, ParseException, MismatchedDimensionException, TransformException {
        SimpleFeatureType featureType = getLinkFeatureType();
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        WKTReader reader = new WKTReader(geometryFactory);
        DefaultFeatureCollection collection =
                new DefaultFeatureCollection("links_collection", featureType);
        int MAX_COUNT = lines.size();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
        for (int i = 1; i < MAX_COUNT; ++i) {
            String line = lines.get(i);
            if (i % 10000 == 0) {
                System.out.println(i);
            }
            Object[] values = parseLinkLine(line, delim, reader, transformer);
            SimpleFeature feature = builder.buildFeature(values[0].toString(), values);
            if (handleTravelDirection) {
                short travel_direction = (Short) feature.getAttribute("travel_direction");
                if (travel_direction == TRAVEL_DIRECTION_FROM) {
                    // DO nothing.
                    collection.add(feature);
                } else if (travel_direction == TRAVEL_DIRECTION_TO) {
                    // reverse it.
                    collection.add(getReverseLink(feature));
                } else if (travel_direction == TRAVEL_DIRECTION_BOTH) {
                    // Add one more reverse link.
                    collection.add(feature);
                    collection.add(getReverseLink(feature));
                }
            } else {
                collection.add(feature);
            }
        }
        return collection;
    }


    public static DataStore generateShape(String csvFilename,
                                          String directory, String name) throws IOException, TransformException, ParseException {
        SimpleFeatureCollection featureCollection = getFeatureCollectionForShp(csvFilename);
        SimpleFeatureType featureType = featureCollection.getSchema();

        File file = new File(directory, name + ".shp");

        FileDataStoreFactorySpi factory = FileDataStoreFinder.getDataStoreFactory("shp");
        Map<String, java.io.Serializable> creationParams = new HashMap<String, java.io.Serializable>();
        creationParams.put("url", DataUtilities.fileToURL(file));

        DataStore dataStore = factory.createNewDataStore(creationParams);
        dataStore.createSchema(featureType);

        SimpleFeatureStore featureStore =
                (SimpleFeatureStore) dataStore.getFeatureSource(name);

        Transaction t = new DefaultTransaction();
        try {
            SimpleFeatureCollection collection = featureCollection; // grab all features
            featureStore.addFeatures(collection);
            t.commit(); // write it out
        } catch (IOException e) {
            e.printStackTrace();
            try {
                t.rollback();
            } catch (IOException ee) {
                throw ee;
            }
        } finally {
            t.close();
        }
        return dataStore;
    }

    public static List<Coordinate> getTestTrajectory(int pointNum) {
        String fileName = "data/test/route1.trj";
        List<Coordinate> coords = new ArrayList<Coordinate>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(fileName), Charset.defaultCharset());
            for (String line : lines) {
                String[] fields = line.split(",");
                if (fields.length == 2) {
                    Coordinate coord =
                            new Coordinate(Double.parseDouble(fields[0]), Double.parseDouble(fields[1]));
                    coords.add(coord);
                    if (coords.size() >= pointNum)
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return coords;
    }

    /**
     * Checks the direction of the edge id.
     *
     * @param edgeId
     * @return
     */
    public static long getEdgeIdDirection(long edgeId) {
        return edgeId / BASE_LINK_ID;
    }


    private static SimpleFeatureCollection getFeatureCollectionFromCSVParallel(List<String> lines,
                                                                               String delim, boolean handleTravelDirection,
                                                                               WGS2MetricTransformer transformer)
            throws IOException, ParseException, MismatchedDimensionException, TransformException {
        int cores = Runtime.getRuntime().availableProcessors();
        SimpleFeatureType featureType = getLinkFeatureType();
        DefaultFeatureCollection collection =
                new DefaultFeatureCollection("links_collection", featureType);
        int size = lines.size();
        System.out.println("Loading with " + cores + " threads.");
        int trunkSize = size / cores + 1;
        List<FeatureLoader> loaders = new ArrayList<>();
        for (int i = 1; i < size; i += trunkSize) {
            FeatureLoader loader = new FeatureLoader(lines.subList(i,
                    Math.min(size, i + trunkSize)),
                    delim, handleTravelDirection, transformer);
            loaders.add(loader);
            loader.start();
        }
        loaders.forEach(loader -> {
            try {
                loader.join();
                collection.addAll(loader.getResult());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        return collection;
    }

    private static class FeatureLoader extends Thread {
        List<String> lines = null;
        List<SimpleFeature> features = null;
        String delim;
        boolean handleTravelDirection;
        WGS2MetricTransformer transformer;
        static int workId = 0;

        public FeatureLoader(List<String> lines, String delim, boolean handleTravelDirection,
                             WGS2MetricTransformer transformer) {
            super("" + workId);
            workId += 1;
            this.lines = lines;
            this.features = new ArrayList<>(lines.size());
            this.delim = delim;
            this.handleTravelDirection = handleTravelDirection;
            this.transformer = transformer;
        }

        private void doWork() {
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            WKTReader reader = new WKTReader(geometryFactory);
            SimpleFeatureType featureType = getLinkFeatureType();
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
            int size = lines.size();
            for (int i = 0; i < size; ++i) {
                try {
                    String line = lines.get(i);
                    Object[] values = parseLinkLine(line, delim, reader, transformer);
                    SimpleFeature feature = builder.buildFeature(values[0].toString(), values);
                    if (handleTravelDirection) {
                        short travel_direction = (Short) feature.getAttribute("travel_direction");
                        if (travel_direction == TRAVEL_DIRECTION_FROM) {
                            // DO nothing.
                            features.add(feature);
                        } else if (travel_direction == TRAVEL_DIRECTION_TO) {
                            // reverse it.
                            features.add(getReverseLink(feature));
                        } else if (travel_direction == TRAVEL_DIRECTION_BOTH) {
                            // Add one more reverse link.
                            features.add(feature);
                            features.add(getReverseLink(feature));
                        }
                    } else {
                        features.add(feature);
                    }
                    if (this.getName().equals("0") && i % 10000 == 0) {
                        System.out.println(String.format("%.1f%%,", i * 100.0 / size));
                    }
                } catch (ParseException | TransformException e) {
                    e.printStackTrace();
                }
            }
        }

        public List<SimpleFeature> getResult() {
            return features;
        }

        @Override
        public void run() {
            super.run();
            doWork();
        }
    }
}
