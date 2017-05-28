package edu.usc.infolab.ridesharing.datasets.real.nyctaxi;

import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;
import edu.usc.infolab.ridesharing.datasets.Input;

import java.io.*;
import java.util.Random;

/**
 * Created by mohammad on 5/25/17.
 * <p>
 * Data Input Fields:
 * medallion, hack_license, vendor_id, rate_code, store_and_fwd_flag,
 * pickup_datetime, dropoff_datetime, passenger_count,
 * trip_time_in_secs, trip_distance,
 * pickup_longitude,pickup_latitude,dropoff_longitude,dropoff_latitude
 */
public class NYTaxiInput<R extends Request, D extends Driver<R>> extends Input<R, D> {
    protected static final String filterStart = "2013-09-19 00:00:00";
    protected static final String filterEnd = "2013-09-19 23:59:59";
    protected static int maxWaitTime;
    protected static Double minTripLength = 10.0;

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
    private static void FilterData(File dir) {
        try {
            Time start = new Time(Time.sdf.parse(filterStart));
            Time end = new Time(Time.sdf.parse(filterEnd));

            File oFile =
                    new File(
                            dir,
                            String.format(
                                    "/Filtered/trips_%s.csv",
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

                String line = br.readLine(); // Pass the first line

                while ((line = br.readLine()) != null) {
                    String[] fields = line.split(",");
                    if (fields.length < 14) continue;
                    Time request = new Time(Time.sdf.parse(fields[5]));
                    GPSPoint pickUp =
                            new GPSPoint(Double.parseDouble(fields[11]), Double.parseDouble(fields[10]));
                    GPSPoint dropOff =
                            new GPSPoint(Double.parseDouble(fields[13]), Double.parseDouble(fields[12]));

                    if (request.compareTo(start) > 0
                            && request.compareTo(end) < 0
                            && pickUp.In(minLat, maxLat, minLng, maxLng)
                            && dropOff.In(minLat, maxLat, minLng, maxLng)) {
                        bw.write(
                                String.format(
                                        "%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                                        fields[5],
                                        fields[6],
                                        fields[7],
                                        fields[8],
                                        fields[9],
                                        fields[10],
                                        fields[11],
                                        fields[12],
                                        fields[13]));
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

    @SuppressWarnings("unused")
    private static void GenerateInputDriversFromRequests(File reqFile, File dir, int size) {
        try {
            FileReader fr = new FileReader(reqFile);
            BufferedReader br = new BufferedReader(fr);

            File oFile =
                    new File(
                            dir, String.format("drivers_from_reqs_%s.csv", filterStart.substring(0, 10).replace("-", "")));
            if (!oFile.getParentFile().exists()) {
                oFile.getParentFile().mkdir();
            }
            FileWriter fw = new FileWriter(oFile);
            BufferedWriter bw = new BufferedWriter(fw);

            String line = "";
            int reqCounter = 0;
            int driverCoutner = 0;
            while ((line = br.readLine()) != null && driverCoutner < size) {
                reqCounter++;
                if (reqCounter % 10 == 0) {
                    driverCoutner++;
                    String[] fields = line.replace(", ", ",").split(",");
                    GPSPoint initialLoc =
                            new GPSPoint(Double.parseDouble(fields[6]), Double.parseDouble(fields[5]));
                    bw.write(String.format("%s,%s,%s\n", initialLoc.toString(), filterStart, filterEnd));
                }
            }

            br.close();
            fr.close();
            bw.close();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private static void GenerateInputDrivers(File dir, int size) {
        try {
            File oFile =
                    new File(
                            dir, String.format("drivers_%s.csv", filterStart.substring(0, 10).replace("-", "")));
            if (!oFile.getParentFile().exists()) {
                oFile.getParentFile().mkdir();
            }
            FileWriter fw = new FileWriter(oFile);
            BufferedWriter bw = new BufferedWriter(fw);

            for (int d = 0; d < size; d++) {
                GPSPoint initialLoc = NewRandomPoint();
                bw.write(String.format("%s,%s,%s\n", initialLoc.toString(), filterStart, filterEnd));
            }
            bw.close();
            fw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String file = "../Data/NYCTaxiDataset/TripData";
        FilterData(new File(file));
        //GenerateInputDrivers(new File(file), 1500);

        //String reqFile = "../Data/trips_2013_05_12.csv";
        //GenerateInputDriversFromRequests(new File(reqFile), new File(file), 10000);
        System.out.print("LNG Diff:");
        System.out.println(new GPSPoint(minLat, minLng).EuclideanDistanceInMilesAndMillis(new GPSPoint(minLat, maxLng)).distance * 1600);
        System.out.print("LAT Diff:");
        System.out.println(new GPSPoint(minLat, minLng).EuclideanDistanceInMilesAndMillis(new GPSPoint(maxLat, minLng)).distance * 1600);
    }
}
