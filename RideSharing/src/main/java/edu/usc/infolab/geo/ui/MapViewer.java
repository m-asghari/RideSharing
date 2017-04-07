package edu.usc.infolab.geo.ui;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.*;
import org.geotools.styling.Font;
import org.geotools.styling.Stroke;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;

public class MapViewer extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = -1836257305361488531L;
    protected StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
    protected FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
    public static final String TRAJECTORY_LAYER_NAME = "trajectory";
    public static final String POINT_LAYER_NAME = "point";
    public static final String PATH_LAYER_NAME = "path";
    protected MapContent mapContent = null;
    protected HashMap<String, DefaultFeatureCollection> featureCollections =
            new HashMap<String, DefaultFeatureCollection>();

    public MapViewer(String shapeFileName) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            init(shapeFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MapContent getMapContent() {
        return mapContent;
    }

    /**
     *
     */
    protected void init(String shapeFileName) throws Exception {
        File file = new File(shapeFileName);

        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = store.getFeatureSource();

        mapContent = new MapContent();
        mapContent.setTitle("MapViewer");

        // Create a Style to render the features
        Layer mapLayer = createMapLayer(featureSource);
        Layer trajectoryLayer = createDefaultLineLayer(TRAJECTORY_LAYER_NAME, Color.RED);
        Layer pathLayer = createDefaultLineLayer(PATH_LAYER_NAME, Color.BLUE);
        Layer pointLayer = createDefaultPointLayer(POINT_LAYER_NAME, Color.GREEN);
        mapContent.addLayer(mapLayer);
        mapContent.addLayer(pointLayer);
        mapContent.addLayer(trajectoryLayer);
        mapContent.addLayer(pathLayer);
    }

    public void setTitle(String title) {
        mapContent.setTitle(title);
    }


    public Layer createMapLayer(FeatureSource featureSource) {
        Style style = createStyle(featureSource);
        Layer mapLayer = new FeatureLayer(featureSource, style);
        return mapLayer;
    }

    public Layer createDefaultLineLayer(String layerName, Color color) {
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("LineFeature");
        b.setCRS(DefaultGeographicCRS.WGS84);
        b.add("geom", LineString.class);
        b.add("text", String.class);
        final SimpleFeatureType TYPE = b.buildFeatureType();

        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection(layerName, TYPE);
        Style style = SLD.createLineStyle(color, 3f);
        Layer layer = new FeatureLayer(featureCollection, style);
        layer.setTitle(layerName);
        featureCollections.put(layerName, featureCollection);
        return layer;
    }

    public Layer createDefaultPointLayer(String layerName, Color color) {
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("PointFeature");
        b.setCRS(DefaultGeographicCRS.WGS84);
        b.add("geom", Point.class);
        b.add("text", String.class);
        final SimpleFeatureType TYPE = b.buildFeatureType();

        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection(layerName, TYPE);
        StyleBuilder sb = new StyleBuilder(sf);
        Font font = sb.createFont("Arial", 15);
        Style style = SLD.createPointStyle("Circle", color, color, 1, 10, "text", font);
        Layer layer = new FeatureLayer(featureCollection, style);
        layer.setTitle(layerName);
        featureCollections.put(layerName, featureCollection);
        return layer;
    }

    public void drawLine(LineString line, String lineName, String layerName) {
        DefaultFeatureCollection featureCollection = featureCollections.get(layerName);
        SimpleFeatureType TYPE = featureCollection.getSchema();
        featureCollection.add(SimpleFeatureBuilder.build(TYPE, new Object[]{line, lineName}, null));
    }

    public void drawLine(List<Coordinate> line, String lineName, String layerName) {
        GeometryFactory factory = JTSFactoryFinder.getGeometryFactory();
        LineString lineString = factory.createLineString(line.toArray(new Coordinate[line.size()]));
        drawLine(lineString, lineName, layerName);
    }

    public void drawPoint(List<Coordinate> coords, String name, String layerName) {
        DefaultFeatureCollection featureCollection = featureCollections.get(layerName);
        SimpleFeatureType TYPE = featureCollection.getSchema();
        GeometryFactory factory = JTSFactoryFinder.getGeometryFactory();
        for (int i = 0; i < coords.size(); ++i) {
            Point point = factory.createPoint(coords.get(i));
            featureCollection
                    .add(SimpleFeatureBuilder.build(TYPE, new Object[]{point, name + "_" + i}, null));
        }
    }

    public void drawPoint(List<Coordinate> coords, String[] names, String layerName) {
        DefaultFeatureCollection featureCollection = featureCollections.get(layerName);
        SimpleFeatureType TYPE = featureCollection.getSchema();
        GeometryFactory factory = JTSFactoryFinder.getGeometryFactory();
        for (int i = 0; i < coords.size(); ++i) {
            Point point = factory.createPoint(coords.get(i));
            featureCollection
                    .add(SimpleFeatureBuilder.build(TYPE, names, null));
        }
    }

    /**
     * Create a Style to display the features. If an SLD file is in the same directory as the
     * shapefile then we will create the Style by processing this. Otherwise we display a
     * JSimpleStyleDialog to prompt the user for preferences.
     */
    @SuppressWarnings("rawtypes")
    protected Style createStyle(FeatureSource featureSource) {
        // SimpleFeatureType schema = (SimpleFeatureType) featureSource.getSchema();
        // Style style = SLD.createSimpleStyle(schema);

        // Line Layer.
        Stroke stroke = sf.createStroke(ff.literal(Color.BLACK), ff.literal(1));
        LineSymbolizer lineSym = sf.createLineSymbolizer(stroke, null);

        Rule rule1 = sf.createRule();
        rule1.symbolizers().add(lineSym);

        // Label layer
        Font font = sf.getDefaultFont();
        font.setSize(ff.literal(20));
        Fill labelFill = sf.createFill(ff.literal(Color.BLACK));
        String labelField = "name";
        TextSymbolizer textSym = sf.createTextSymbolizer(labelFill, new Font[]{font}, null,
                ff.property(labelField), null, null);
        Rule rule2 = sf.createRule();
        rule2.symbolizers().add(textSym);
        rule2.setMaxScaleDenominator(3000);

        FeatureTypeStyle fts = sf.createFeatureTypeStyle(new Rule[]{rule1, rule2});
        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);
        return style;
    }
}
