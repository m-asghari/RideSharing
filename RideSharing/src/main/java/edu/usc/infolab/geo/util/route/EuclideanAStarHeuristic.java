
package edu.usc.infolab.geo.util.route;

import edu.usc.infolab.geo.util.NodeHelper;
import org.geotools.graph.structure.Node;

/**
 * Uses the Euclidean distance between two nodes as the lower bound.
 * 
 * @author Yaguang
 *
 */
public class EuclideanAStarHeuristic implements AStarHeuristic {

  public double calculate(Node source, Node destination) {
    return new NodeHelper(source).getCoordinate()
        .distance(new NodeHelper(destination).getCoordinate());
  }

}
