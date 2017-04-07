/**
 *
 */
package edu.usc.infolab.geo.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.util.ArrayList;
import java.util.List;

/**
 * Transforms WGS to MIT Metric for easier distance measurement.
 *
 * @author Yaguang
 */
public class WGS2MetricTransformer {
    private CoordinateReferenceSystem DEFAULT_METRIC_CRS = null;
    private MathTransform toWGS84Transform = null;
    private MathTransform fromWGS84Transform = null;

    public static final WGS2MetricTransformer LATransformer = new WGS2MetricTransformer(-118, 34);
    public static final WGS2MetricTransformer BeijingTransformer = new WGS2MetricTransformer(116, 40);
    public static final WGS2MetricTransformer NYCTransformer = new WGS2MetricTransformer(-74, 41);

    public WGS2MetricTransformer(double lng, double lat) {
        try {
            // Load default coordinate system.
            DEFAULT_METRIC_CRS = CRS.decode(String.format("AUTO:42001,%.6f,%.6f", lng, lat));
            toWGS84Transform = CRS.findMathTransform(DEFAULT_METRIC_CRS, DefaultGeographicCRS.WGS84);
            fromWGS84Transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, DEFAULT_METRIC_CRS);
        } catch (FactoryException e) {
            e.printStackTrace();
        }
    }

    /**
     * Transforms a geometry from Massachusetts Metric to WGS84, i.e., lat/lng.
     *
     * @param geom
     * @return
     * @throws TransformException
     * @throws MismatchedDimensionException
     */
    public Geometry toWGS84(Geometry geom) throws MismatchedDimensionException, TransformException {
        Geometry result = null;
        result = JTS.transform(geom, toWGS84Transform);
        return result;
    }

    /**
     * Transforms a coordinate from Massachusetts Metric to WGS84, i.e., lat/lng.
     *
     * @param coord
     * @return
     * @throws TransformException
     */
    public Coordinate toWGS84(Coordinate coord) throws TransformException {
        Coordinate result = new Coordinate();
        result = JTS.transform(coord, result, toWGS84Transform);
        return result;
    }

    /**
     * Transforms a geometry from WGS84, i.e., lat/lng, to Massachusetts Metric CRS.
     *
     * @param geom
     * @return
     * @throws TransformException
     * @throws MismatchedDimensionException
     */
    public Geometry fromWGS84(Geometry geom) throws MismatchedDimensionException, TransformException {
        Geometry result = null;
        result = JTS.transform(geom, fromWGS84Transform);
        return result;
    }

    /**
     * Transforms a coordinate from WGS84, i.e., lat/lng, to Massachusetts Metric CRS.
     *
     * @param coord
     * @return
     * @throws TransformException
     */
    public Coordinate fromWGS84(Coordinate coord) throws TransformException {
        Coordinate result = new Coordinate();
        result = JTS.transform(coord, result, fromWGS84Transform);
        return result;
    }

    public List<Coordinate> fromWGS84(List<Coordinate> coords) throws TransformException {
        List<Coordinate> result = new ArrayList<Coordinate>();
        for (Coordinate coord : coords) {
            result.add(fromWGS84(coord));
        }
        return result;
    }

    public List<Coordinate> toWGS84(List<Coordinate> coords) throws TransformException {
        List<Coordinate> result = new ArrayList<Coordinate>();
        for (Coordinate coord : coords) {
            result.add(toWGS84(coord));
        }
        return result;
    }

    public Envelope toWGS84(Envelope envelope) throws TransformException {
        return JTS.transform(envelope, toWGS84Transform);
    }

    public Envelope fromWGS84(Envelope envelope) throws TransformException {
        return JTS.transform(envelope, fromWGS84Transform);
    }
}
