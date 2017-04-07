/**
 * 
 */
package edu.usc.infolab.geo.semanticroute.util;

import edu.usc.infolab.geo.semanticroute.model.Route;
import org.geotools.graph.structure.Edge;

import java.util.HashSet;

/**
 * Retrieves candidate routes for a given edge.
 * 
 * @author Yaguang
 *
 */
public interface RouteRetriever {
  public HashSet<Route> queryRoutes(Edge edge);
}
