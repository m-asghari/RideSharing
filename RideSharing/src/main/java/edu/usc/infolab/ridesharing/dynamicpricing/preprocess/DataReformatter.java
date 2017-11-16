package edu.usc.infolab.ridesharing.dynamicpricing.preprocess;

import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.geom.shapefile.ShapeReader;
import edu.usc.infolab.geom.shapefile.ShapeWriter;
import edu.usc.infolab.geom.shapefile.ShapefileUtils;
import edu.usc.infolab.ridesharing.Pair;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.dynamicpricing.TransitionModel;
import sun.security.provider.SHA;

import java.io.*;
import java.text.ParseException;
import java.util.*;

/**
 * Created by Mohammad on 11/3/2017.
 */
public class DataReformatter {
    private static final double maxLat = 41.0;
    private static final double minLat = 40.0;
    private static final double maxLng = -73.0;
    private static final double minLng = -74.5;

    TransitionModel transition_model;
    HashMap<Integer, List<Integer>> supply_matrix;

    //private static HashMap<Integer, Polygon> locations = new HashMap<>();

    private static final String shpFileName = "data/NYCensusTracts/tl_2017_36_tract.shp"; // 2835+ Census Tracts for our taxi data
    private static final String locationType = "tract";
    //private static final String shpFileName = "data/NYCountySub/tl_2017_36_cousub.shp"; // 28 county subdivisions for our taxi data
    //private static final String shpFileName = "../Data/NYCTaxiDataset/TripData/ReformattedData/05-May/locations_5_12.shp";

    public static HashMap<Integer, Polygon> getLocations(String locationFile) throws IOException{
        HashMap<Integer, Polygon> locations = new HashMap<>();

        Polygon boundingBox = new Polygon();
        boundingBox.startPath(minLng, minLat);
        boundingBox.lineTo(maxLng, minLat);
        boundingBox.lineTo(maxLng, maxLat);
        boundingBox.lineTo(minLng, maxLat);
        boundingBox.closeAllPaths();

        InputStream is = new FileInputStream(locationFile);
        DataInputStream dis = new DataInputStream(is);
        ShapeReader shpReader = new ShapeReader(dis);
        while (shpReader.hasMore()) {
            Pair<Integer, Polygon> polygon = shpReader.readPolygon();
            boolean isWithin = ShapefileUtils.isWithin(polygon.Second, boundingBox);
            if (isWithin) {
                locations.put(polygon.First, polygon.Second);
            } else {
                boolean overlaps = ShapefileUtils.overlaps(polygon.Second, boundingBox);
                if (overlaps) {
                    locations.put(polygon.First, polygon.Second);
                }
            }
        }
        if (is != null)
            is.close();
        if (dis != null)
            dis.close();
        return locations;
    }

    public static int getLocationID(HashMap<Integer, Polygon> locations, GPSPoint point) {
        Point p = ShapefileUtils.getShapefilePoint(point);
        for (Map.Entry<Integer, Polygon> entry : locations.entrySet()) {
            if (ShapefileUtils.isWithin(p, entry.getValue())) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public static List<String> reformatData(File dir, HashMap<Integer, Polygon> locations) {
        List<String> reformattedData = new ArrayList<>();
        try {
            File[] inputFiles =
                    dir.listFiles(
                            new FilenameFilter() {
                                @Override
                                public boolean accept(File dir, String name) {
                                    if (name.endsWith(".csv")) return true;
                                    return false;
                                }
                            });

            for (File file : inputFiles) {
                System.out.println(String.format("Started File: %s", file.getName()));
                if (file.isDirectory()) continue;
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);

                List<FileWriter> fws = new ArrayList<>();
                List<BufferedWriter> bws = new ArrayList<>();
                for (int i = 0; i < 31; i++) {
                    String reformattedFile = file.getPath().replace(".csv", String.format("_%d.csv", i+1));
                    reformattedData.add(reformattedFile);
                    FileWriter fw = new FileWriter(reformattedFile);
                    BufferedWriter bw = new BufferedWriter(fw);
                    fws.add(fw);
                    bws.add(bw);
                }

                br.readLine(); // Pass the first line
                String line;
                while ((line = br.readLine()) != null) {
                    String[] fields = line.split(",");
                    if (fields.length < 14) continue;
                    Time request = new Time(Time.sdf.parse(fields[5]));
                    GPSPoint pickUp =
                            new GPSPoint(Double.parseDouble(fields[11]), Double.parseDouble(fields[10]));
                    int pickUpID = getLocationID(locations, pickUp);
                    GPSPoint dropOff =
                            new GPSPoint(Double.parseDouble(fields[13]), Double.parseDouble(fields[12]));
                    int dropOffID = getLocationID(locations, dropOff);


                    if (pickUpID != -1 && dropOffID != -1) {
                        int day = request.Get(Calendar.DAY_OF_MONTH);

                        bws.get(day - 1)
                                .write(String.format(
                                        "%s,%d,%d,%d,%d,%d,%d,%d,%s,%s,%s,%s,%s,%s,%d,%s,%s,%d\n",
                                        fields[5],// 0- Pick-up Time
                                        request.Get(Calendar.YEAR),// 1-
                                        request.Get(Calendar.MONTH),// 2-
                                        request.Get(Calendar.DAY_OF_MONTH),// 3-
                                        request.Get(Calendar.DAY_OF_WEEK),// 4-
                                        request.Get(Calendar.HOUR_OF_DAY),// 5-
                                        request.Get(Calendar.MINUTE),// 6-
                                        request.Get(Calendar.SECOND),// 7-
                                        fields[6],// 8- Drop-off Time
                                        fields[7],// 9- Passenger Count
                                        fields[8],// 10- Trip Time in Sec
                                        fields[9],// 11- Trip Distance
                                        fields[10],// 12- Pick-up Lng
                                        fields[11],// 13- Pick-up Lat
                                        pickUpID, //14-
                                        fields[12],// 15- Drop-off Lng
                                        fields[13],// 16- Drop-off Lat
                                        dropOffID//17-
                                ));
                    }
                }
                br.close();
                fr.close();
                for (int i = 0; i < 31; i++) {
                    bws.get(i).close();
                    fws.get(i).close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reformattedData;
    }

    private static String updateLocationID(File inputFile, HashMap<Integer, Polygon> locations, String locationType) throws IOException {
        FileReader fr = new FileReader(inputFile);
        BufferedReader br = new BufferedReader(fr);

        String updatedLocationFilePath = inputFile.getPath().replace(".csv", String.format("_%s.csv", locationType));
        FileWriter fw = new FileWriter(updatedLocationFilePath);
        BufferedWriter bw = new BufferedWriter(fw);

        String line;
        while ((line = br.readLine()) != null) {
            String[] fields = line.split(",");
            if (fields.length < 18) continue;
            GPSPoint pickUp =
                    new GPSPoint(Double.parseDouble(fields[13]), Double.parseDouble(fields[12]));
            int pickUpID = getLocationID(locations, pickUp);
            GPSPoint dropOff =
                    new GPSPoint(Double.parseDouble(fields[16]), Double.parseDouble(fields[15]));
            int dropOffID = getLocationID(locations, dropOff);

            if (pickUpID != -1 && dropOffID != -1) {
                bw.write(String.format(
                        "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,%s,%s,%d\n",
                        fields[0],// 0- Pick-up Time
                        fields[1],// 1- Pick-up Year
                        fields[2],// 2- Pick-up Month
                        fields[3],// 3- Pick-up Day of Month
                        fields[4],// 4- Pick-up Day of Week
                        fields[5],// 5- Pick-up Hour of Day
                        fields[6],// 6- Pick-up Minute
                        fields[7],// 7- Pick-up Second
                        fields[8],// 8- Drop-off Time
                        fields[9],// 9- Passenger Count
                        fields[10],// 10- Trip Time in Sec
                        fields[11],// 11- Trip Distance
                        fields[12],// 12- Pick-up Lng
                        fields[13],// 13- Pick-up Lat
                        pickUpID, //14-
                        fields[15],// 15- Drop-off Lng
                        fields[16],// 16- Drop-off Lat
                        dropOffID//17-
                        )
                );
            }
        }
        bw.close();
        fw.close();
        br.close();
        fr.close();
        return updatedLocationFilePath;
    }

    private static String getPopularLocations(File inputFile, HashMap<Integer, Polygon> locations, String locationFile, String locationType) throws IOException, ParseException{
        FileReader fr = new FileReader(inputFile);
        BufferedReader br = new BufferedReader(fr);

        HashMap<Integer, Long> pickUpCounts = new HashMap<>();
        HashMap<Integer, Long> dropOffCounts = new HashMap<>();
        long recordCount = 0;
        for (Map.Entry<Integer, Polygon> entry : locations.entrySet()) {
            pickUpCounts.put(entry.getKey(), 0L);
            dropOffCounts.put(entry.getKey(), 0L);
        }
        br.readLine(); // Pass the first line
        String line;
        while ((line = br.readLine()) != null) {
            String[] fields = line.split(",");
            if (fields.length < 18) continue;
            int pickUpID = Integer.parseInt(fields[14]);
            int dropOffID = Integer.parseInt(fields[17]);

            pickUpCounts.put(pickUpID, pickUpCounts.get(pickUpID) + 1);
            dropOffCounts.put(dropOffID, dropOffCounts.get(dropOffID) + 1);
            recordCount++;
        }
        br.close();
        fr.close();

        //int threshold = (int)(recordCount / (locations.size() * 100));
        int threshold = 1000;
        List<Integer> unpopularLocations = new ArrayList<>();
        for (Map.Entry<Integer, Polygon> entry : locations.entrySet()) {
            if (pickUpCounts.get(entry.getKey()) < threshold || dropOffCounts.get(entry.getKey()) < threshold) {
                unpopularLocations.add(entry.getKey());
            }
        }

        fr = new FileReader(inputFile);
        br = new BufferedReader(fr);

        pickUpCounts = new HashMap<>();
        dropOffCounts = new HashMap<>();
        recordCount = 0;
        List<Integer> popularLocations = new ArrayList<>();
        for (Map.Entry<Integer, Polygon> entry : locations.entrySet()) {
            if (!unpopularLocations.contains(entry.getKey())) {
                popularLocations.add(entry.getKey());
                pickUpCounts.put(entry.getKey(), 0L);
                dropOffCounts.put(entry.getKey(), 0L);
            }
        }
        br.readLine(); // Pass the first line
        int differentPickUpDropOff = 0;
        while ((line = br.readLine()) != null) {
            String[] fields = line.split(",");
            if (fields.length < 18) continue;
            int pickUpID = Integer.parseInt(fields[14]);
            int dropOffID = Integer.parseInt(fields[17]);
            if (unpopularLocations.contains(pickUpID) || unpopularLocations.contains(dropOffID)) continue;

            if (pickUpID != dropOffID)
                differentPickUpDropOff++;

            pickUpCounts.put(pickUpID, pickUpCounts.get(pickUpID) + 1);
            dropOffCounts.put(dropOffID, dropOffCounts.get(dropOffID) + 1);
            recordCount++;
        }
        Collections.sort(popularLocations);

        // Write popular locations to new shape file
        InputStream is = new FileInputStream(locationFile);
        DataInputStream dis = new DataInputStream(is);
        String outputShapeFile = inputFile.getPath().replace("trip_data", String.format("locations_%s",locationType)).replace(".csv", ".shp");
        ShapeWriter shpWriter = new ShapeWriter(dis, outputShapeFile);
        shpWriter.write(popularLocations);
        br.close();
        fr.close();
        return outputShapeFile;
    }

    private static int timeSlot = 1;

    public static void generateDemandData(File file, HashMap<Integer, Polygon> locations) throws  IOException{
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);

        int timeIntervalSize = 24/timeSlot;
        int[][][] transition = new int[timeIntervalSize][][];
        int[][] demand = new int[timeIntervalSize][];
        for (int t = 0; t < timeIntervalSize; t++) {
            transition[t] = new int[locations.size()][];
            demand[t] = new int[locations.size()];
            for (int i = 0; i < locations.size(); i++) {
                transition[t][i] = new int[locations.size()];
                demand[t][i] = 0;
                for (int j = 0; j < locations.size(); j++) {
                    transition[t][i][j] = 0;
                }
            }
        }

        String line = "";
        while ((line = br.readLine()) != null) {
            String[] fields = line.split(",");
            int timeInterval = Integer.parseInt(fields[5]) / timeSlot;
            int pickUpID = Integer.parseInt(fields[14]);
            int dropOffID = Integer.parseInt(fields[17]);
            transition[timeInterval][pickUpID-1][dropOffID-1]++;
            demand[timeInterval][pickUpID-1]++;
        }
        br.close();
        fr.close();
        FileWriter fw = new FileWriter(file.getPath().replace("trip_data", "transition").replace(".csv", ".csv"));
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(String.format("%d,%d\n", timeIntervalSize, locations.size()));
        for (int t = 0; t < timeIntervalSize; t++) {
            bw.write(Integer.toString(t));bw.write("\n");
            for (int i = 0; i < locations.size(); i++) {
                StringBuilder sb = new StringBuilder();
                sb.append((double)transition[t][i][0]/(double)demand[t][i]);
                for (int j = 1; j < locations.size(); j++) {
                    sb.append(",");
                    sb.append((double)transition[t][i][j]/(double)demand[t][i]);
                }
                sb.append("\n");
                bw.write(sb.toString());
            }
        }
        bw.close();
        fw.close();

        fw = new FileWriter(file.getPath().replace("trip_data", "demand").replace(".csv", ".csv"));
        bw = new BufferedWriter(fw);
        bw.write(String.format("%d,%d\n", timeIntervalSize, locations.size()));
        for (int t = 0; t < timeIntervalSize; t++) {
            StringBuilder sb = new StringBuilder();
            sb.append(t);
            for (int i = 0; i < locations.size(); i++) {
                sb.append(",");
                sb.append(demand[t][i]);
            }
            sb.append("\n");
            bw.write(sb.toString());
        }
        bw.close();
        fw.close();
    }

    public static void main(String[] args) throws IOException, ParseException {
        HashMap<Integer, Polygon> locations = getLocations(shpFileName);
        //List<String> reformattedData = reformatData(new File("../Data/NYCTaxiDataset/TripData"), locations);
        File dir = new File("../Data/NYCTaxiDataset/TripData");
        File[] inputFiles =
                dir.listFiles(
                        new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                if (name.endsWith(".csv") && name.startsWith("trip_data_5_")) return true;
                                return false;
                            }
                        });
        List<String> reformattedData = new ArrayList<>();
        for (File file : inputFiles) {
            reformattedData.add(file.getPath());
        }
        for (String filepath : reformattedData) {
            String popularLocationsFile = getPopularLocations(new File(filepath), locations, shpFileName, locationType);
            HashMap<Integer, Polygon> popularLocations = getLocations(popularLocationsFile);
            String updatedLocationFilePath = updateLocationID(new File(filepath), popularLocations, locationType);
            generateDemandData(new File(updatedLocationFilePath), popularLocations);
        }
        /*String filepath = reformattedData.get(0);
        String popularLocationsFile = getPopularLocations(new File(filepath), locations, shpFileName, locationType);
        HashMap<Integer, Polygon> popularLocations = getLocations(popularLocationsFile);
        String updatedLocationFilePath = updateLocationID(new File(filepath), popularLocations, locationType);
        generateDemandData(new File(updatedLocationFilePath), popularLocations);*/
    }
}
