package edu.usc.infolab.ridesharing.datasets.real.nyctaxi;

import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by mohammad on 5/25/17.
 */
public class PreProcess {
    protected static final double maxLat = 41.0;
    protected static final double minLat = 40.0;
    protected static final double maxLng = -73.0;
    protected static final double minLng = -74.5;

    private static void FindMinMaxLatLng(File dir) {
        Double maxLat = -1. * Integer.MAX_VALUE, maxLng = -1. * Integer.MAX_VALUE, minLat = 1. * Integer.MAX_VALUE, minLng = 1. * Integer.MAX_VALUE;
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
                if (file.isDirectory()) continue;
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                br.readLine();
                String line = "";
                while ((line = br.readLine()) != null) {
                    String[] fields = line.split(",");
                    if (fields.length < 14) continue;
                    Double pLng = Double.parseDouble(fields[10]);
                    Double pLat = Double.parseDouble(fields[11]);
                    Double dLng = Double.parseDouble(fields[12]);
                    Double dLat = Double.parseDouble(fields[13]);
                    if (pLat != 0 && maxLat.compareTo(pLat) < 0) maxLat = pLat;
                    if (dLat != 0 && maxLat.compareTo(dLat) < 0) maxLat = dLat;
                    if (pLng != 0 && maxLng.compareTo(pLng) < 0) maxLng = pLng;
                    if (dLng != 0 && maxLng.compareTo(dLng) < 0) maxLng = dLng;
                    if (pLat != 0 && minLat.compareTo(pLat) > 0) minLat = pLat;
                    if (dLat != 0 && minLat.compareTo(dLat) > 0) minLat = dLat;
                    if (pLng != 0 && minLng.compareTo(pLng) > 0) minLng = pLng;
                    if (dLng != 0 && minLng.compareTo(dLng) > 0) minLng = dLng;
                }
                br.close();
                fr.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        System.out.println(String.format("maxLat: %f, maxLng: %f, minLat: %f, minLng: %f\n", maxLat, maxLng, minLat, minLng));
    }

    /*private static boolean IsValid(Request request) {
        if (!request.source.point.In(minLat, maxLat, minLng, maxLng))
            return false;
        if (!request.source.point.In(minLat, maxLat, minLng, maxLng))
            return false;
        return true;
    }*/

    @SuppressWarnings("unused")
    private static void PreProcessData(File dir) {
        try {
            HashMap<Integer, FileWriter> FWs = new HashMap<>();
            HashMap<Integer, BufferedWriter> BWs = new HashMap<>();
            for (int i = 0; i < 12; i++) {
                File oFile =
                        new File(
                                dir,
                                String.format(
                                        "/Filtered/trips_procesed_%d.csv",
                                        i));
                FWs.put(i, new FileWriter(oFile));
                BWs.put(i, new BufferedWriter(FWs.get(i)));
            }

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

                br.readLine(); // Pass the first line

                String line;
                while ((line = br.readLine()) != null) {
                    String[] fields = line.split(",");
                    if (fields.length < 14) continue;
                    Time request = new Time(Time.sdf.parse(fields[5]));
                    GPSPoint pickUp =
                            new GPSPoint(Double.parseDouble(fields[11]), Double.parseDouble(fields[10]));
                    GPSPoint dropOff =
                            new GPSPoint(Double.parseDouble(fields[13]), Double.parseDouble(fields[12]));


                    GPSPoint origin = new GPSPoint(minLat, minLng);
                    if (pickUp.In(minLat, maxLat, minLng, maxLng)
                            && dropOff.In(minLat, maxLat, minLng, maxLng)) {
                        int month = request.Get(Calendar.MONTH);
                        BWs.get(request.Get(Calendar.MONTH)).write(
                                String.format(
                                        "%s,%d,%d,%d,%d,%d,%d,%d,%s,%s,%s,%s,%s,%s,%.2f,%.2f,%s,%s,%.2f,%.2f\n",
                                        fields[5],// 0- Pick-up Time
                                        request.Get(Calendar.YEAR),// 1-
                                        request.Get(Calendar.MONTH),// 2-
                                        request.Get(Calendar.DAY_OF_MONTH),// 3-
                                        request.Get(Calendar.DAY_OF_WEEK),// 4-
                                        request.Get(Calendar.HOUR),// 5-
                                        request.Get(Calendar.MINUTE),// 6-
                                        request.Get(Calendar.SECOND),// 7-
                                        fields[6],// 8- Drop-off Time
                                        fields[7],// 9- Passenger Count
                                        fields[8],// 10- Trip Time in Sec
                                        fields[9],// 11- Trip Distance
                                        fields[10],// 12- Pick-up Lng
                                        fields[11],// 13- Pick-up Lat
                                        pickUp.EuclideanDistanceInMilesAndMillis(
                                                new GPSPoint(Double.parseDouble(fields[11]), minLng)).distance * 1600,// 14- Pick-up Lat Delta
                                        pickUp.EuclideanDistanceInMilesAndMillis(
                                                new GPSPoint(minLat, Double.parseDouble(fields[10]))).distance * 1600,// 15- Pick-up Lng Delta
                                        fields[12],// 16- Drop-off Lng
                                        fields[13],// 17- Drop-off Lat
                                        pickUp.EuclideanDistanceInMilesAndMillis(
                                                new GPSPoint(Double.parseDouble(fields[13]), minLng)).distance * 1600,// 18- Drop-off Lat Delta
                                        pickUp.EuclideanDistanceInMilesAndMillis(
                                                new GPSPoint(minLat, Double.parseDouble(fields[12]))).distance * 1600// 19- Drop-off Lng Delta
                                ));
                    }
                }
                br.close();
                fr.close();
            }

            for (int i = 0; i < 12; i++) {
                BWs.get(i).close();
                FWs.get(i).close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final Random rand = new Random();

    protected static GPSPoint NewRandomPoint() {
        double lat = minLat + (rand.nextDouble() * (maxLat - minLat));
        double lng = minLng + (rand.nextDouble() * (maxLng - minLng));
        return new GPSPoint(lat, lng);
    }

    private void PopulateTripsTable(File dir) throws SQLException {
        String dbDriver = "oracle.jdbc.driver.OracleDriver";
        String dbConnectionString = "jdbc:oracle:thin:@localhost:1521";
        String dbUser = "SYS";
        String dbPassword = "rth@usc";
        String dbDateFormat = "yyyy-mm-dd hh24:mi:ss";

        String insertSQLFormat = "INSERT INTO TRIPS VALUES" +
                "(to_date('%s','%s'), %s, %s, %s, %s, %s, %s, %s, %s," +
                "to_date('%s','%s'), %s, %s, %s," +
                " %s, %s, %s, %s, %s, %s, %s, %s)";

        Connection dbConnection = null;
        Statement statement = null;
        try {
            Class.forName(dbDriver);
            dbConnection = DriverManager.getConnection(dbConnectionString, dbUser, dbPassword);

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

                String line;
                while ((line = br.readLine()) != null) {
                    String[] fields = line.split(",");

                    statement = dbConnection.createStatement();

                    String now = Time.sdf.format(Calendar.getInstance().getTime());
                    statement.executeUpdate(
                            String.format(insertSQLFormat,
                                    fields[0], dbDateFormat,
                                    fields[1], fields[2], fields[3], fields[4], "1",
                                    fields[5], fields[6], fields[7],
                                    fields[8], dbDateFormat,
                                    fields[9], fields[10], fields[11],
                                    fields[12], fields[15], fields[13], fields[14],
                                    fields[16], fields[19], fields[17], fields[18])
                    );
                }
                br.close();
                fr.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
    }

    public static void main(String[] args) {
        String file = "../Data/NYCTaxiDataset/TripData";
        PreProcessData(new File(file));
        //FindMinMaxLatLng(new File(file));
    }

}
