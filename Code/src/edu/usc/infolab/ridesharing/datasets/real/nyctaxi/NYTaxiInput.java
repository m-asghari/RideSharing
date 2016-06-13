package edu.usc.infolab.ridesharing.datasets.real.nyctaxi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.Random;

import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.datasets.Input;


public class NYTaxiInput<R extends Request, D extends Driver<R>> extends Input<R, D> {
	protected static final String filterStart = "2013-09-19 00:00:00";
	protected static final String filterEnd = "2013-09-19 23:59:59";
	protected static final int maxWaitTime = 60;
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
	
	private static void FilterData(File dir) {
		try {
			Time start = new Time(Time.sdf.parse(filterStart));
			Time end = new Time(Time.sdf.parse(filterEnd));
						
			File oFile = new File(dir, 
					String.format("/Filtered/trips_%s.csv",Time.sdf.format(start.GetTime()).substring(0, 10).replace("-", "_")));
			FileWriter fw = new FileWriter(oFile);
			BufferedWriter bw = new BufferedWriter(fw);
			
			File[] inputFiles = dir.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".csv"))
						return true;
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
					GPSPoint pickUp = new GPSPoint(Double.parseDouble(fields[11]), Double.parseDouble(fields[10]));
					GPSPoint dropOff = new GPSPoint(Double.parseDouble(fields[13]), Double.parseDouble(fields[12]));
					
					if (request.compareTo(start) > 0 && request.compareTo(end) < 0 &&
							pickUp.In(minLat, maxLat, minLng, maxLng) &&
							dropOff.In(minLat, maxLat, minLng, maxLng)) {
						bw.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
								fields[5], fields[6], fields[7], fields[8], fields[9], fields[10], fields[11], fields[12], fields[13]));
					}
				}
				br.close();
				fr.close();				
			}			
			
			bw.close();
			fw.close();
		}
		catch (Exception e) {
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
		//String file = "../Data/NYCTaxiDataset/Trips/Filtered/trips_2013_05_12.csv";
		String file = "../Data/NYCTaxiDataset/Trips";
		FilterData(new File(file));
	}
}
