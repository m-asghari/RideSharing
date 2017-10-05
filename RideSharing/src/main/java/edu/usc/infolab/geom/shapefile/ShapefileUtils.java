package edu.usc.infolab.geom.shapefile;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.OperatorOverlaps;
import com.esri.core.geometry.OperatorWithin;
import com.esri.core.geometry.Point;
import edu.usc.infolab.geom.GPSPoint;

/**
 * Created by Mohammad on 10/4/2017.
 */
public class ShapefileUtils {
    public static boolean isWithin(Geometry in, Geometry out) {
        return OperatorWithin.local().execute(in, out, null,null);
    }

    public static boolean overlaps(Geometry in, Geometry out) {
        return OperatorOverlaps.local().execute(in, out, null, null);
    }

    public static Point getShapefilePoint(GPSPoint gpsPoint) {
        return new Point(gpsPoint.getLng(), gpsPoint.getLat());
    }
}
