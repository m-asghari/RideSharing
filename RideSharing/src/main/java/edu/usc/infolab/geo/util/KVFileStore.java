package edu.usc.infolab.geo.util;

import com.google.gson.Gson;
import edu.usc.infolab.geo.model.MyList;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class KVFileStore {
  private static File baseDir = new File("data");
  private static Gson gson = new Gson();
  private static Logger logger = Logger.getLogger(KVFileStore.class.getName());

  private static File getDataFile(String key) {
    return new File(baseDir, key + ".json");
  }

  public static <T> T get(String key, Class<T> classOfT) {
    File file = getDataFile(key);
    String jsonString = "";
    try {
      jsonString = FileUtils.readFileToString(file);
    } catch (IOException e) {
      logger.severe(e.getMessage());
    }
    return gson.fromJson(jsonString, classOfT);
  }
  public static <T> List<T> getList(String key, Class<T> classOfT) {
    File file = getDataFile(key);
    String jsonString = "";
    try {
      jsonString = FileUtils.readFileToString(file);
    } catch (IOException e) {
      logger.severe(e.getMessage());
    }
    return gson.fromJson(jsonString, new MyList<T>(classOfT));
  }
  public static void set(String key, Object o) {
    File file = getDataFile(key);
    String jsonString = gson.toJson(o);
    try {
      FileUtils.writeStringToFile(file, jsonString);
    } catch (IOException e) {
      logger.severe(e.getMessage());
    }
  }
}
