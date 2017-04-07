package edu.usc.infolab.geo.util.route;

import org.geotools.graph.structure.Node;

public interface AStarHeuristic {
  public double calculate(Node source, Node destination);
}
