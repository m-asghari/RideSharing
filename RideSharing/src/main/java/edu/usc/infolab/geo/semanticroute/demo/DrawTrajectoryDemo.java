package edu.usc.infolab.geo.semanticroute.demo;


import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import edu.usc.infolab.geo.semanticroute.dao.RouteBean;
import edu.usc.infolab.geo.semanticroute.dao.RouteBeanHelper;
import edu.usc.infolab.geo.util.Constants;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.WKTReader2;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.*;
import org.geotools.styling.Stroke;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.styling.JSimpleStyleDialog;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DrawTrajectoryDemo {

  static StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
  static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);
  DefaultFeatureCollection trajectoryCollection = null;

  public static void main(String[] args) throws Exception {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }
    DrawTrajectoryDemo me = new DrawTrajectoryDemo();
    me.displayShapefile();
  }

  public void foo() {
    // List<Edge> edges = path.getEdges();
    // for (Edge edge : edges) {
    // SimpleFeature feature = (SimpleFeature) edge.getObject();
    // System.out.println("" + feature.getAttribute("way_id") + ":"
    // + feature.getAttribute("street_name") + ", " + feature.getAttribute("length"));
    // }
  }
  // docs end main

  // docs start display
  /**
   * Prompts the user for a shapefile (unless a filename is provided on the command line; then
   * creates a simple Style and displays the shapefile on screen
   */
  private void displayShapefile() throws Exception {
    // File file = JFileDataStoreChooser.showOpenFile("shp", null);
    // if (file == null) {
    // return;
    // }
    File file = new File(Constants.LINK_SHP_FILE);

    FileDataStore store = FileDataStoreFinder.getDataStore(file);
    SimpleFeatureSource featureSource = store.getFeatureSource();

    // Shows the content of the featuresource.
    // showContent(featureSource);
    // Create a _map context and add our shapefile to it
    MapContent map = new MapContent();
    map.setTitle("Google Trajectory");

    // Create a basic Style to render the features
    Style style = createStyle(file, featureSource);

    // Add the features and the associated Style object to
    // the MapContext as a new MapLayer
    // _map.addLayer(featureSource, style);
    // Style style = SLD.createSimpleStyle(featureSource.getSchema());
    Layer layer = new FeatureLayer(featureSource, style);
    Layer trajectoryLayer = getTrajectoryLayer();
    map.addLayer(layer);
    map.addLayer(trajectoryLayer);
    showTrajectories();
    // showToLinks(featureSource);
    // Now display the _map
    JMapFrame.showMap(map);
  }

  /**
   * Shows the links with travel_direction='T'.
   * 
   * @param featureSource
   * @throws ParseException
   * @throws IOException
   */
  private void showToLinks(SimpleFeatureSource featureSource) throws ParseException, IOException {
    final SimpleFeatureType TYPE = trajectoryCollection.getSchema();
    for (SimpleFeature feature : getToLinks(featureSource.getFeatures())) {
      LineString line =
          (LineString) ((MultiLineString) feature.getDefaultGeometry()).getGeometryN(0);
      System.out.println(line.toText());
      trajectoryCollection.add(SimpleFeatureBuilder.build(TYPE,
          new Object[] {line, feature.getAttribute("link_id")}, null));
    }
  }

  private void showTrajectories() throws ParseException, SQLException {
    final SimpleFeatureType TYPE = trajectoryCollection.getSchema();
    WKTReader2 reader = new WKTReader2();
    for (RouteBean route : getRoutes()) {
      StringBuffer sb = new StringBuffer();
      route.getPath().getGeometry().outerWKT(sb);
      LineString line = (LineString) reader.read(sb.toString());
      trajectoryCollection.add(
          SimpleFeatureBuilder.build(TYPE, new Object[] {line, "" + route.getRoute_id()}, null));
    }
  }

  // docs end display
  private Layer getTrajectoryLayer() throws ParseException {
    SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();

    b.setName("trajectory");
    b.setCRS(DefaultGeographicCRS.WGS84);
    // picture location
    b.add("geom", LineString.class);
    // picture url
    b.add("text", String.class);

    final SimpleFeatureType TYPE = b.buildFeatureType();

    // SimpleFeatureCollection collection = FeatureCollections.newCollection("internal");
    trajectoryCollection = new DefaultFeatureCollection("trajectory", TYPE);


    // Style style = SLD.createPointStyle("Star", Color.BLUE, Color.BLUE, 0.3f, 15);
    Style style = SLD.createLineStyle(Color.RED, 3f);

    Layer trajectoryLayer = new FeatureLayer(trajectoryCollection, style);

    trajectoryLayer.setTitle("trajectory layer");

    return trajectoryLayer;
  }

  private List<LineString> getLineStrings() throws ParseException {
    String[] wkts = new String[] {"LINESTRING(-117.5455365 33.8420948,-117.5457881 33.8417022)",
        "LINESTRING(-117.393508 33.9089091,-117.3936914 33.9089353,-117.3938149 33.9090216,-117.3948738 33.9103092,-117.3957578 33.9113842,-117.3962322 33.911964,-117.397462 33.9134672,-117.3975743 33.9135767)"};
    List<LineString> lines = new ArrayList<LineString>();
    WKTReader2 wktReader = new WKTReader2();
    for (String wkt : wkts) {
      LineString line = (LineString) wktReader.read(wkt);
      line.setSRID(4326);
      lines.add(line);
    }
    return lines;
  }

  private List<SimpleFeature> getToLinks(SimpleFeatureCollection collection)
      throws ParseException {
    List<SimpleFeature> lines = new ArrayList<SimpleFeature>();
    SimpleFeatureIterator iterator = collection.features();
    while (iterator.hasNext()) {
      SimpleFeature feature = iterator.next();
      if (feature.getAttribute("travel_dir").equals(2)) {
        lines.add(feature);
      }
    }
    return lines;
  }

  private List<RouteBean> getRoutes() throws SQLException {
    RouteBeanHelper routeHelper = new RouteBeanHelper();
    // int[] routeIds = new int[] {86};
    // int[] routeIds = new int[] {86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101,
    // 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119,
    // 120, 121, 122};
    int[] routeIds = new int[] {86};
    List<RouteBean> routes = new ArrayList<RouteBean>();
    for (int routeId : routeIds) {
      RouteBean route = routeHelper.getRoute(routeId);
      routes.add(route);
    }
    return routes;
  }

  private void showContent(FeatureSource<SimpleFeatureType, SimpleFeature> featureSource)
      throws IOException {
    // TODO Auto-generated method stub
    FeatureCollection<SimpleFeatureType, SimpleFeature> collection = featureSource.getFeatures();
    FeatureIterator<SimpleFeature> iterator = collection.features();
    while (iterator.hasNext()) {
      SimpleFeature feature = iterator.next();
      System.out.println(feature.getID() + ": ");
      for (Property attribute : feature.getProperties()) {
        System.out.println("\t" + attribute.getName() + ":" + attribute.getValue());
      }
    }
  }

  // docs start create style
  /**
   * Create a Style to display the features. If an SLD file is in the same directory as the
   * shapefile then we will create the Style by processing this. Otherwise we display a
   * JSimpleStyleDialog to prompt the user for preferences.
   */
  private Style createStyle(File file, FeatureSource featureSource) {
    File sld = toSLDFile(file);
    if (sld != null) {
      return createFromSLD(sld);
    }

    SimpleFeatureType schema = (SimpleFeatureType) featureSource.getSchema();
    return JSimpleStyleDialog.showDialog(null, schema);
  }

  // docs end create style

  // docs start sld
  /**
   * Figure out if a valid SLD file is available.
   */
  public File toSLDFile(File file) {
    String path = file.getAbsolutePath();
    String base = path.substring(0, path.length() - 4);
    String newPath = base + ".sld";
    File sld = new File(newPath);
    if (sld.exists()) {
      return sld;
    }
    newPath = base + ".SLD";
    sld = new File(newPath);
    if (sld.exists()) {
      return sld;
    }
    return null;
  }

  /**
   * Create a Style object from a definition in a SLD document
   */
  private Style createFromSLD(File sld) {
    try {
      SLDParser stylereader = new SLDParser(styleFactory, sld.toURI().toURL());
      Style[] style = stylereader.readXML();
      return style[0];

    } catch (Exception e) {
    }
    return null;
  }

  // docs end sld


  // docs start alternative
  /**
   * Here is a programmatic alternative to using JSimpleStyleDialog to get a Style. This methods
   * works out what sort of feature geometry we have in the shapefile and then delegates to an
   * appropriate style creating method.
   */
  private Style createStyle2(FeatureSource featureSource) {
    SimpleFeatureType schema = (SimpleFeatureType) featureSource.getSchema();
    Class geomType = schema.getGeometryDescriptor().getType().getBinding();

    if (Polygon.class.isAssignableFrom(geomType) || MultiPolygon.class.isAssignableFrom(geomType)) {
      return createPolygonStyle();

    } else if (LineString.class.isAssignableFrom(geomType)
        || MultiLineString.class.isAssignableFrom(geomType)) {
      return createLineStyle();

    } else {
      return createPointStyle();
    }
  }

  /**
   * Create a Style to draw polygon features with a thin blue outline and a cyan fill
   */
  private Style createPolygonStyle() {

    // create a partially opaque outline stroke
    Stroke stroke = styleFactory.createStroke(filterFactory.literal(Color.BLUE),
        filterFactory.literal(1), filterFactory.literal(0.5));

    // create a partial opaque fill
    Fill fill =
        styleFactory.createFill(filterFactory.literal(Color.CYAN), filterFactory.literal(0.5));

    /*
     * Setting the geometryPropertyName arg to null signals that we want to draw the default
     * geomettry of features
     */
    PolygonSymbolizer sym = styleFactory.createPolygonSymbolizer(stroke, fill, null);

    Rule rule = styleFactory.createRule();
    rule.symbolizers().add(sym);
    FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[] {rule});
    Style style = styleFactory.createStyle();
    style.featureTypeStyles().add(fts);

    return style;
  }

  /**
   * Create a Style to draw line features as thin blue _lines
   */
  private Style createLineStyle() {
    Stroke stroke =
        styleFactory.createStroke(filterFactory.literal(Color.BLUE), filterFactory.literal(1));

    /*
     * Setting the geometryPropertyName arg to null signals that we want to draw the default
     * geomettry of features
     */
    LineSymbolizer sym = styleFactory.createLineSymbolizer(stroke, null);

    Rule rule = styleFactory.createRule();
    rule.symbolizers().add(sym);
    FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[] {rule});
    Style style = styleFactory.createStyle();
    style.featureTypeStyles().add(fts);

    return style;
  }

  /**
   * Create a Style to draw point features as circles with blue outlines and cyan fill
   */
  private Style createPointStyle() {
    Graphic gr = styleFactory.createDefaultGraphic();

    Mark mark = styleFactory.getCircleMark();

    mark.setStroke(
        styleFactory.createStroke(filterFactory.literal(Color.BLUE), filterFactory.literal(1)));

    mark.setFill(styleFactory.createFill(filterFactory.literal(Color.CYAN)));

    gr.graphicalSymbols().clear();
    gr.graphicalSymbols().add(mark);
    gr.setSize(filterFactory.literal(5));
    
    /*
     * Setting the geometryPropertyName arg to null signals that we want to draw the default
     * geomettry of features
     */
    PointSymbolizer sym = styleFactory.createPointSymbolizer(gr, null);

    Rule rule = styleFactory.createRule();
    rule.symbolizers().add(sym);
    FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[] {rule});
    Style style = styleFactory.createStyle();
    style.featureTypeStyles().add(fts);

    return style;
  }

}

// docs end source
