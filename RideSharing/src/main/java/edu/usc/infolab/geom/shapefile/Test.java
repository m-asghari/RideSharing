package edu.usc.infolab.geom.shapefile;

import com.esri.core.geometry.OperatorWithin;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.SpatialReference;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Mohammad on 10/4/2017.
 */
public class Test {

    public static void main(String[] args) throws Exception {
        Polygon p = new Polygon();
        p.startPath(0,3);
        p.lineTo(1,3);
        p.lineTo(1,5);
        p.lineTo(0,5);
        p.closeAllPaths();
        Point point = new Point();
        point.setX(0.5);
        point.setY(4);
        boolean within = pointIsWithin(point, p);

        InputStream is = null;
        DataInputStream dis = null;
        try {
            is = new FileInputStream("data/CACensusTracts/tl_2017_06_tract.shp");
            dis = new DataInputStream(is);
            ShapeReader shpReader = new ShapeReader(dis);
            while (shpReader.hasMore()) {
                Polygon polygon = shpReader.readPolygon();
            }

        } catch (IOException ioe){
            ioe.printStackTrace();
        } finally {
            if (is != null)
                is.close();
            if (dis != null)
                dis.close();
        }
    }

    static boolean pointIsWithin(Point point, Polygon polygon) {
        boolean isWithin = OperatorWithin.local().execute(point, polygon, null, null);
        return isWithin;
    }
}
