package edu.usc.infolab.ridesharing.dynamicpricing;

import com.esri.core.geometry.Polygon;
import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.geom.shapefile.ShapeReader;
import edu.usc.infolab.geom.shapefile.ShapefileUtils;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Mohammad on 10/2/2017.
 */
public class DynamicPricingModel {
    private static final double maxLat = 41.0;
    private static final double minLat = 40.0;
    private static final double maxLng = -73.0;
    private static final double minLng = -74.5;

    TransitionModel transition_model;
    HashMap<Integer, List<Integer>> supply_matrix;

    private static List<Polygon> locations = new ArrayList<>();

    //private static final String shpFileName = "data/NYCensusTracts/tl_2017_36_tract.shp"; // 2835+ Census Tracts for our taxi data
    private static final String shpFileName = "data/NYCountySub/tl_2017_36_cousub.shp"; // 28 county subdivisions for our taxi data

    public DynamicPricingModel() throws IOException{
        Polygon boundingBox = new Polygon();
        boundingBox.startPath(minLng, minLat);
        boundingBox.lineTo(maxLng, minLat);
        boundingBox.lineTo(maxLng, maxLat);
        boundingBox.lineTo(minLng, maxLat);
        boundingBox.closeAllPaths();

        InputStream is = new FileInputStream(shpFileName);
        DataInputStream dis = new DataInputStream(is);
        ShapeReader shpReader = new ShapeReader(dis);
        while (shpReader.hasMore()) {
            Polygon polygon = shpReader.readPolygon();
            boolean isWithin = ShapefileUtils.isWithin(polygon, boundingBox);
            if (isWithin) {
                locations.add(polygon);
            } else {
                boolean overlaps = ShapefileUtils.overlaps(polygon, boundingBox);
                if (overlaps) {
                    locations.add(polygon);
                }
            }
        }
        if (is != null)
            is.close();
        if (dis != null)
            dis.close();
    }

    private static int getLocationID(GPSPoint gpsPoint) {
        return -1;
    }

    public double getPrice(AuctionRequest r, Time now) {
        return -1;
    }
}
