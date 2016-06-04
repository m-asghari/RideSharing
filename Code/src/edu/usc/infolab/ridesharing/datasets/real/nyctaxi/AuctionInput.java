package edu.usc.infolab.ridesharing.datasets.real.nyctaxi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.auction.AuctionDriver;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;
import edu.usc.infolab.ridesharing.datasets.Input;

public class AuctionInput extends Input<AuctionRequest, AuctionDriver> {
	private static final String filterStart = "2013-05-12 00:00:00";
	private static final String filterEnd = "2013-05-12 23:59:59";
	private static final int maxWaitTime = 15;
	
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
	
	/*private static boolean IsValid(AuctionRequest request) {
		if (!request.source.point.In(minLat, maxLat, minLng, maxLng))
			return false;
		if (!request.source.point.In(minLat, maxLat, minLng, maxLng))
			return false;
		return true;
	}*/
	
	/*
	 * Format of files should be:
	 * pickUp_DateTime, dropOff_DataTime, passenger_count, tripTime_Seconds, tripDistance, pickUp_lng, pickUp_lat, dropOff_lng, dropOff_lat
	 */
	public static ArrayList<AuctionRequest> GenerateRequests(File inFile) {
		ArrayList<AuctionRequest> requests = new ArrayList<AuctionRequest>();
		try {
			FileReader fr = new FileReader(inFile);
			BufferedReader br = new BufferedReader(fr);
			
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] fields = line.replace(", ", ",").split(",");
				GPSPoint source = new GPSPoint(Double.parseDouble(fields[6]), Double.parseDouble(fields[5]));
				GPSPoint dest = new GPSPoint(Double.parseDouble(fields[8]), Double.parseDouble(fields[7]));
				Time requestTime = new Time();
				requestTime.SetTime(Time.sdf.parse(fields[0]));
				requests.add(new AuctionRequest(source, dest, requestTime, maxWaitTime));
			}
			br.close();
			fr.close();
		}
		catch (ParseException pe) {
			pe.printStackTrace();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		Collections.sort(requests);
		return requests;
	}
	
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
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		System.out.println(String.format("maxLat: %f, maxLng: %f, minLat: %f, minLng: %f\n", maxLat, maxLng, minLat, minLng));
	}*/
	
	private static final double maxLat = 41.0;
	private static final double minLat = 40.0;
	private static final double maxLng = -73.0;
	private static final double minLng = -74.5;
	private static final Random rand = new Random();
	private static GPSPoint NewRandomPoint() {
		double lat = minLat + (rand.nextDouble() * (maxLat - minLat));
		double lng = minLng + (rand.nextDouble() * (maxLng - minLng));
		return new GPSPoint(lat, lng);
	}
	
	public static ArrayList<AuctionDriver> GenerateDrivers(int size) {
		ArrayList<AuctionDriver> drivers = new ArrayList<AuctionDriver>();
		for (int d = 0; d < size; d++) {
			drivers.add(GetNewDriver());
		}
		Collections.sort(drivers);
		return drivers;
	}
	
	public static AuctionDriver GetNewDriver() {
		try {
			GPSPoint initialLoc = NewRandomPoint();
			Time start = new Time(Time.sdf.parse(filterStart));
			Time end = new Time(Time.sdf.parse(filterEnd));
			return new AuctionDriver(initialLoc, start, end);			
		}
		catch (ParseException pe) {
			pe.printStackTrace();
		}
		return null;
	}
	
	
	public static void main(String[] args) {
		//String file = "../Data/NYCTaxiDataset/Trips/Filtered/trips_2013_05_12.csv";
		String file = "../Data/NYCTaxiDataset/Trips";
		FilterData(new File(file));
	}
}
