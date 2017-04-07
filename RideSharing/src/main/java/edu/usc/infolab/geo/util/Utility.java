package edu.usc.infolab.geo.util;

import com.vividsolutions.jts.geom.Coordinate;
import edu.usc.infolab.geo.model.LocationUpdate;
import edu.usc.infolab.geo.model.Trajectory;
import edu.usc.infolab.geo.model.TrjRecord;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTimeZone;
import org.opengis.referencing.operation.TransformException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class Utility {

    //    private static String dataBaseDir = getProperty("beijing_taxi_dir");
    private static final String CONFIG_FILE = "config.properties";

    static {
    }

    public static String getProperty(String key) {
        Properties properties = new Properties();
        String resourceName = CONFIG_FILE;
        String result = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream input = loader.getResourceAsStream(resourceName);
        try {
            properties.load(input);
            result = properties.getProperty(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static List<String> readLines(String fileName) throws ZipException, IOException {
        // String fileName = FilenameUtils.concat(dataBaseDir, vehicleId + ".zip");
        List<String> lines = null;
        if (fileName.endsWith("zip")) {
            ZipFile zipFile = new ZipFile(new File(fileName));
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            // Reads first entry.
            if (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                InputStream stream = zipFile.getInputStream(entry);
                lines = IOUtils.readLines(stream, StandardCharsets.UTF_8);
            }
            zipFile.close();
        } else {
            lines = IOUtils.readLines(new FileInputStream(fileName));
        }
        return lines;
    }

    public static List<Coordinate> readCoordinates(String fileName) throws ZipException, IOException {
        List<String> lines = readLines(fileName);
        SortedMap<String, Coordinate> map = new TreeMap<>();
        // Sorts by time.
        for (String line : lines) {
            String[] fields = line.split(",");
            if (fields.length == 9) {
                String key = fields[3];
                double lng = Double.valueOf(fields[4]);
                double lat = Double.valueOf(fields[5]);
                map.put(key, new Coordinate(lng, lat));
            }
        }
        // Generates coordinate list.
        List<Coordinate> coords = new ArrayList<>(map.values());
        return coords;
    }


    public static List<LocationUpdate> readLocationUpdates(String fileName)
            throws ZipException, IOException, ParseException {
        SortedMap<String, LocationUpdate> updates = new TreeMap<>();
        List<String> lines = readLines(fileName);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        // Sorts by time.
        for (String line : lines) {
            String[] fields = line.split(",");
            if (fields.length == 9) {
                String key = fields[3];
                Date date = sdf.parse(key);
                double lng = Double.valueOf(fields[4]);
                double lat = Double.valueOf(fields[5]);
                int event = Integer.valueOf(fields[1]);
                int status = Integer.valueOf(fields[2]);
                TrjRecord record = new TrjRecord(date.getTime(), new Coordinate(lng, lat));
                LocationUpdate update = new LocationUpdate(record, status, event, line);
                updates.put(key, update);
            }
        }
        List<LocationUpdate> result = new ArrayList<LocationUpdate>(updates.values());
        return result;
    }

    /**
     * Reads beijing trajectory from file.
     *
     * @param fileName    trajectory filename
     * @param transformer whether
     * @return
     * @throws ZipException
     * @throws IOException
     * @throws ParseException
     * @throws TransformException
     */
    public static Trajectory readBeijingTrajectory(String fileName, WGS2MetricTransformer transformer)
            throws ZipException, IOException, ParseException, TransformException {
        List<String> lines = readLines(fileName);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        sdf.setTimeZone(DateTimeZone.forID("Asia/Shanghai").toTimeZone());
        List<TrjRecord> records = new ArrayList<>();
        String moid = lines.get(0).split(",")[0];
        // Sorts by time.
        for (String line : lines) {
            String[] fields = line.split(",");
            if (fields.length == 9) {
                Date date = sdf.parse(fields[3]);
                double lng = Double.valueOf(fields[4]);
                double lat = Double.valueOf(fields[5]);
                Coordinate location = new Coordinate(lng, lat);
                location = WGS2MarsTransformer.transform(location);
                if (transformer != null) {
                    location = transformer.fromWGS84(location);
                }
                TrjRecord record = new TrjRecord(date.getTime(), location);
                records.add(record);
            }
        }
        return new Trajectory(records, moid);
    }
}
