package edu.usc.infolab.ridesharing.algorithms;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Status;
import edu.usc.infolab.ridesharing.Time;

public abstract class Algorithm {
	private Time currentTime;
	private int advanceTimeInterval;
	
	public ArrayList<Driver> activeDrivers;
	public ArrayList<Request> activeRequests;
	
	
	public Algorithm(Time startTime, int ati) {
		currentTime = (Time)startTime.clone();
		advanceTimeInterval = ati;
		activeDrivers = new ArrayList<Driver>();
		activeRequests = new ArrayList<Request>();
	}
	
	public void Run(ArrayList<Request> requests, ArrayList<Driver> drivers) {
		ArrayList<Request> remainingRequests = new ArrayList<Request>(requests);
		ArrayList<Driver> remainingDrivers = new ArrayList<Driver>(drivers);
		
		while (remainingRequests.size() > 0) {
			//Add newly activated drivers
			while (remainingDrivers.get(0).start.compareTo(currentTime) <= 0) {
				activeDrivers.add(remainingDrivers.remove(0));
			}
			
			while (remainingRequests.get(0).requestTime.compareTo(currentTime) <= 0) {
				Request r = remainingRequests.get(0);
				if (ProcessRequest(r) == Status.ASSIGNED) {
					activeRequests.add(r);
				}
				remainingRequests.remove(r);
			}
			
			for (Iterator<Driver> it = activeDrivers.iterator(); it.hasNext();) {
				Driver driver = it.next();
				
				// What the driver has to do in current frame
								
				if (driver.end.compareTo(currentTime) > 0) {
					it.remove();
				}
			}
			
			currentTime.Add(Calendar.MILLISECOND, advanceTimeInterval);
		}
	}
	
	public abstract Status ProcessRequest(Request r);
}
