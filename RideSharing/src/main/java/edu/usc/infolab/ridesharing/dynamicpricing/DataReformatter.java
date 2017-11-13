package edu.usc.infolab.ridesharing.dynamicpricing;

import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.geom.shapefile.ShapeReader;
import edu.usc.infolab.geom.shapefile.ShapefileUtils;
import edu.usc.infolab.ridesharing.Time;

import java.io.*;
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

    private static List<Polygon> locations = new ArrayList<>();

    //private static final String shpFileName = "data/NYCensusTracts/tl_2017_36_tract.shp"; // 2835+ Census Tracts for our taxi data
    private static final String shpFileName = "data/NYCountySub/tl_2017_36_cousub.shp"; // 28 county subdivisions for our taxi data

    public static void initializeLocations() throws IOException{
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

    public static int getLocationID(GPSPoint point) {
        Point p = ShapefileUtils.getShapefilePoint(point);
        for (int l = 0; l < locations.size(); l++) {
            if (ShapefileUtils.isWithin(p, locations.get(l))) {
                return l;
            }
        }
        return -1;
    }

    public static void reformatData(File dir) {
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

                /*List<FileWriter> fws = new ArrayList<>();
                List<BufferedWriter> bws = new ArrayList<>();
                for (int i = 0; i < 31; i++) {
                    FileWriter fw = new FileWriter(file.getPath().replace(".csv", String.format("_%d_reformat.csv", i+1)));
                    BufferedWriter bw = new BufferedWriter(fw);
                    fws.add(fw);
                    bws.add(bw);
                }*/

                long[] pickUpCounts = new long[locations.size()];
                long[] dropOffCounts = new long[locations.size()];
                for (int i = 0; i < locations.size(); i++) {
                    pickUpCounts[i] = 0;
                    dropOffCounts[i] = 0;
                }
                br.readLine(); // Pass the first line
                String line;
                while ((line = br.readLine()) != null) {
                    String[] fields = line.split(",");
                    if (fields.length < 14) continue;
                    Time request = new Time(Time.sdf.parse(fields[5]));
                    GPSPoint pickUp =
                            new GPSPoint(Double.parseDouble(fields[11]), Double.parseDouble(fields[10]));
                    int pickUpID = getLocationID(pickUp);
                    GPSPoint dropOff =
                            new GPSPoint(Double.parseDouble(fields[13]), Double.parseDouble(fields[12]));
                    int dropOffID = getLocationID(dropOff);


                    if (pickUpID != -1 && dropOffID != -1) {
                        pickUpCounts[pickUpID]++;
                        dropOffCounts[dropOffID]++;
                        /*int day = request.Get(Calendar.DAY_OF_MONTH);

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
                                ));*/
                    }
                }
                br.close();
                fr.close();
                for (int i = 0; i < locations.size(); i++) {
                    System.out.format("Location %d: PickUp Count = %d, DropOff Count = %d\n", i, pickUpCounts[i], dropOffCounts[i]);
                }
                /*for (int i = 0; i < 31; i++) {
                    bws.get(i).close();
                    fws.get(i).close();
                }*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int timeSlot = 1;
    private static int locationsSize = 28;

    public static void generateDemandData(File file) throws  IOException{
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);

        int timeIntervalSize = 24/timeSlot;
        int[][][] demands = new int[timeIntervalSize][][];
        for (int t = 0; t < timeIntervalSize; t++) {
            demands[t] = new int[locationsSize][];
            for (int i = 0; i < locationsSize; i++) {
                demands[t][i] = new int[locationsSize];
                for (int j = 0; j < locationsSize; j++) {
                    demands[t][i][j] = 0;
                }
            }
        }

        String line = "";
        while ((line = br.readLine()) != null) {
            String[] fields = line.split(",");
            int timeInterval = Integer.parseInt(fields[5]) / timeSlot;
            int pickUpID = Integer.parseInt(fields[14]);
            int dropOffID = Integer.parseInt(fields[17]);
            demands[timeInterval][pickUpID][dropOffID]++;
        }
        br.close();
        fr.close();
        FileWriter fw = new FileWriter(file.getPath().replace("trip", "demand").replace("_reformat.csv", ".csv"));
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(String.format("%d,%d\n", timeIntervalSize, locationsSize));
        for (int t = 0; t < timeIntervalSize; t++) {
            for (int i = 0; i < locationsSize; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append(demands[t][i][0]);
                for (int j = 1; j < locationsSize; j++) {
                    sb.append(",");
                    sb.append(demands[t][i][j]);
                }
                sb.append("\n");
                bw.write(sb.toString());
            }
        }
        bw.close();
        fw.close();
    }

    public static void main(String[] args) throws IOException {
        initializeLocations();
        reformatData(new File("../Data/NYCTaxiDataset/TripData"));
        //generateDemandData(new File ("../Data/NYCTaxiDataset/TripData/ReformattedData/01-January/trip_data_1_1_reformat.csv"));
    }
}
