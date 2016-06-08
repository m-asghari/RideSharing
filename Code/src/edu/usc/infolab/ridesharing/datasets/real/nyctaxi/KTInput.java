package edu.usc.infolab.ridesharing.datasets.real.nyctaxi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;

import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.kinetictree.KTDriver;
import edu.usc.infolab.ridesharing.kinetictree.KTRequest;

public class KTInput extends NYTaxiInput<KTRequest, KTDriver> {	
	/*
	 * Format of files should be:
	 * pickUp_DateTime, dropOff_DataTime, passenger_count, tripTime_Seconds, tripDistance, pickUp_lng, pickUp_lat, dropOff_lng, dropOff_lat
	 */
	public static ArrayList<KTRequest> GenerateRequests(File inFile) {
		ArrayList<KTRequest> requests = new ArrayList<KTRequest>();
		try {
			FileReader fr = new FileReader(inFile);
			BufferedReader br = new BufferedReader(fr);
			
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] fields = line.replace(", ", ",").split(",");
				Double dist = Double.parseDouble(fields[4]);
				if (dist.compareTo(minTripLength) < 0)
					continue;
				GPSPoint source = new GPSPoint(Double.parseDouble(fields[6]), Double.parseDouble(fields[5]));
				GPSPoint dest = new GPSPoint(Double.parseDouble(fields[8]), Double.parseDouble(fields[7]));
				Time requestTime = new Time();
				requestTime.SetTime(Time.sdf.parse(fields[0]));
				requests.add(new KTRequest(source, dest, requestTime, maxWaitTime));
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
	
	public static KTDriver GetNewDriver() {
		try {
			GPSPoint initialLoc = NewRandomPoint();
			Time start = new Time(Time.sdf.parse(filterStart));
			Time end = new Time(Time.sdf.parse(filterEnd));
			return new KTDriver(initialLoc, start, end);			
		}
		catch (ParseException pe) {
			pe.printStackTrace();
		}
		return null;
	}
}
