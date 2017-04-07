package edu.usc.infolab.geo.semanticroute.demo;

import edu.usc.infolab.geo.util.Constants;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PGFeatureCollectionDemo {
  public static void main(String[] args) {
    String tableName = "places";
    try {
      SimpleFeatureCollection collection = getFeatureCollectionFromDB(tableName);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  private static SimpleFeatureCollection getFeatureCollectionFromDB(String tableName)
      throws IOException {
    Map<String, String> params = getConnParams();
    DataStore dataStore = DataStoreFinder.getDataStore(params);
    SimpleFeatureSource source = dataStore.getFeatureSource(tableName);
    SimpleFeatureCollection features = source.getFeatures();
    return features;
  }

  private static Map<String, String> getConnParams() {
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("dbtype", "postgis");
    params.put("host", Constants.DB_HOST);
    params.put("port", Constants.DB_PORT);
    params.put("database", "LA_RN");
    params.put("user", Constants.DB_USER);
    params.put("passwd", Constants.DB_PASSWORD);
    return params;
  }
}
