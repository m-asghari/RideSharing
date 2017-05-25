package edu.usc.infolab.ridesharing.datasets.real.nyctaxi;

import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;

import java.io.*;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by mohammad on 5/25/17.
 */
public class PreProcess {
    protected static final String filterStart = "2013-09-19 00:00:00";
    protected static final String filterEnd = "2013-09-19 23:59:59";

    protected static final double maxLat = 41.0;
    protected static final double minLat = 40.0;
    protected static final double maxLng = -73.0;
    protected static final double minLng = -74.5;

    /*private static void FindMinMaxLatLng(File inFile) {
        Double maxLat = -1. * Integer.MAX_VALUE, maxLng = -1. * Integer.MAX_VALUE, minLat = 1. * Integer.MAX_VALUE, minLng = 1. * Integer.MAX_VALUE;
        try {
            FileReader fr = new FileReader(inFile);
            BufferedReader br = new BufferedReader(fr);

            String line = "";
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                Double pLng = Double.parseDouble(fields[5]);
                Double pLat = Double.parseDouble(fields[6]);
                Double dLng = Double.parseDouble(fields[7]);
                Double dLat = Double.parseDouble(fields[8]);
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
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        System.out.println(String.format("maxLat: %f, maxLng: %f, minLat: %f, minLng: %f\n", maxLat, maxLng, minLat, minLng));
    }

    private static boolean IsValid(Request request) {
        if (!request.source.point.In(minLat, maxLat, minLng, maxLng))
            return false;
        if (!request.source.point.In(minLat, maxLat, minLng, maxLng))
            return false;
        return true;
    }*/

    @SuppressWarnings("unused")
    private static void PreProcessData(File dir) {
        try {
            Time start = new Time(Time.sdf.parse(filterStart));
            Time end = new Time(Time.sdf.parse(filterEnd));

            File oFile =
                    new File(
                            dir,
                            String.format(
                                    "/Filtered/trips_procesed_%s.csv",
                                    Utils.FILE_SYSTEM_SDF.format(start.GetTime()).substring(0, 10)));
            FileWriter fw = new FileWriter(oFile);
            BufferedWriter bw = new BufferedWriter(fw);

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
                        bw.write(
                                String.format(
                                        "%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                                        fields[5],// Request Time
                                        request.Get(Calendar.YEAR),
                                        request.Get(Calendar.MONTH),
                                        request.Get(Calendar.DAY_OF_MONTH),
                                        request.Get(Calendar.DAY_OF_WEEK),
                                        request.Get(Calendar.HOUR),
                                        request.Get(Calendar.MINUTE),
                                        request.Get(Calendar.SECOND),
                                        fields[6],
                                        fields[7],
                                        fields[8],
                                        fields[9],
                                        fields[10],// Pick-up Lng
                                        fields[11],// Pick-up Lat
                                        pickUp.EuclideanDistanceInMilesAndMillis(
                                                new GPSPoint(Double.parseDouble(fields[11]), minLng)).distance * 1600,
                                        pickUp.EuclideanDistanceInMilesAndMillis(
                                                new GPSPoint(minLat, Double.parseDouble(fields[10]))).distance * 1600,
                                        fields[12],// Drop-off Lng
                                        fields[13],// Drop-off Lat
                                        pickUp.EuclideanDistanceInMilesAndMillis(
                                                new GPSPoint(Double.parseDouble(fields[13]), minLng)).distance * 1600,
                                        pickUp.EuclideanDistanceInMilesAndMillis(
                                                new GPSPoint(minLat, Double.parseDouble(fields[12]))).distance * 1600
                                ));
                    }
                }
                br.close();
                fr.close();
            }

            bw.close();
            fw.close();
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

    public static void main(String[] args) {
        String file = "../Data/NYCTaxiDataset/Taxis";
        PreProcessData(new File(file));
    }
}
