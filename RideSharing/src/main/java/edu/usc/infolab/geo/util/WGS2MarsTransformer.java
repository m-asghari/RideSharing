package edu.usc.infolab.geo.util;

import com.vividsolutions.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * World Geodetic System ==> Mars Geodetic System
 * 
 * @author Yaguang
 *
 * @see {@url https://on4wp7.codeplex.com/SourceControl/changeset/view/21483#353936}
 */
public class WGS2MarsTransformer {

  static final double pi = Math.PI;

  //
  // Krasovsky 1940
  //
  // a = 6378245.0, 1/f = 298.3
  // b = a * (1 - f)
  // ee = (a^2 - b^2) / a^2;
  static final double a = 6378245.0;
  static final double ee = 0.00669342162296594323;

  public static Coordinate transform(Coordinate wgsCoord) {
    Coordinate marsCoord = new Coordinate();
    double wgLon = wgsCoord.x, wgLat = wgsCoord.y;
    double mgLat = wgLat, mgLon = wgLon;
    if (outOfChina(wgLat, wgLon)) {
      return wgsCoord;
    }
    //
    // World Geodetic System ==> Mars Geodetic System
    double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
    double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
    double radLat = wgLat / 180.0 * pi;
    double magic = Math.sin(radLat);
    magic = 1 - ee * magic * magic;
    double sqrtMagic = Math.sqrt(magic);
    dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
    dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
    mgLat = wgLat + dLat;
    mgLon = wgLon + dLon;
    marsCoord = new Coordinate(mgLon, mgLat);
    return marsCoord;
  }

  public static List<Coordinate> transform(List<Coordinate> wgsCoords) {
    List<Coordinate> marsCoords = new ArrayList<>();
    for (Coordinate wgsCoord : wgsCoords) {
      marsCoords.add(transform(wgsCoord));
    }
    return marsCoords;
  }

  static boolean outOfChina(double lat, double lon) {
    if (lon < 72.004 || lon > 137.8347)
      return true;
    if (lat < 0.8293 || lat > 55.8271)
      return true;
    return false;
  }

  static double transformLat(double x, double y) {
    double ret =
        -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
    ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
    ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
    ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
    return ret;
  }

  static double transformLon(double x, double y) {
    double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
    ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
    ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
    ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
    return ret;
  }
}
