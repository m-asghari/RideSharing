package edu.usc.infolab.geo.semanticroute.util;

import edu.usc.infolab.geo.semanticroute.model.Route;
import edu.usc.infolab.geo.semanticroute.model.RoutePartition;

/**
 * 
 * @author Yaguang
 *
 */
public interface RoutePartitioner {
  RoutePartition partition(Route route);
}
