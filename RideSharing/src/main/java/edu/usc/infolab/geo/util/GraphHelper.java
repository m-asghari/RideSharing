package edu.usc.infolab.geo.util;

import com.vividsolutions.jts.geom.LineString;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.graph.build.feature.FeatureGraphGenerator;
import org.geotools.graph.build.line.DirectedLineStringGraphGenerator;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.opengis.feature.simple.SimpleFeature;

public class GraphHelper {
  public static Graph buildGraph(SimpleFeatureCollection featureCollection) {
    DirectedLineStringGraphGenerator lineStringGen = new DirectedLineStringGraphGenerator();
    FeatureGraphGenerator featureGen = new FeatureGraphGenerator(lineStringGen);
    SimpleFeatureIterator iterator = featureCollection.features();
    while (iterator.hasNext()) {
      SimpleFeature feature = iterator.next();
      featureGen.add(feature);
    }
    return featureGen.getGraph();
  }

  public static LineString getLineString(Edge e) {
    SimpleFeature feature = (SimpleFeature) e.getObject();
    LineString line = (LineString) feature.getDefaultGeometry();
    return line;
  }
}
