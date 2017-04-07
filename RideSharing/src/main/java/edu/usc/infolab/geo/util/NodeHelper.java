package edu.usc.infolab.geo.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import org.geotools.graph.structure.Node;
import org.opengis.feature.simple.SimpleFeature;

public class NodeHelper {
  @SuppressWarnings("unused")
  private final Node node;
  private SimpleFeature feature;

  public NodeHelper(Node node) {
    this.node = node;
    this.feature = (SimpleFeature) node.getObject();
  }

  public Coordinate getCoordinate() {
    Point p = (Point) feature.getAttribute("geom");
    return p.getCoordinate();
  }
  public long getInnerNodeId() {
    long nodeId = (Long) feature.getAttribute("node_id");
    return nodeId;
  }
}
