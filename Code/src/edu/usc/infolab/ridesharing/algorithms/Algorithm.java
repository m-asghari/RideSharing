package edu.usc.infolab.ridesharing.algorithms;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import edu.usc.infolab.geom.Point;
import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Status;
import edu.usc.infolab.ridesharing.Time;

public abstract class Algorithm<P extends Point, R extends Request<P>, D extends Driver<P, R>> {
	private Time currentTime;
	private int advanceTimeInterval;
	
	public ArrayList<D> activeDrivers;
	public ArrayList<R> activeRequests;
	
	
	public Algorithm(Time startTime, int ati) {
		currentTime = (Time)startTime.clone();
		advanceTimeInterval = ati;
		activeDrivers = new ArrayList<D>();
		activeRequests = new ArrayList<R>();
	}
	
	public void Run(ArrayList<R> requests, ArrayList<D> drivers) {
		ArrayList<R> remainingRequests = new ArrayList<R>(requests);
		ArrayList<D> remainingDrivers = new ArrayList<D>(drivers);
		
		while (remainingRequests.size() > 0) {
			//Add newly activated drivers
			while (remainingDrivers.get(0).start.compareTo(currentTime) <= 0) {
				activeDrivers.add(remainingDrivers.remove(0));
			}
			
			while (remainingRequests.get(0).requestTime.compareTo(currentTime) <= 0) {
				R r = remainingRequests.get(0);
				if (ProcessRequest(r) == Status.ASSIGNED) {
					activeRequests.add(r);
				}
				remainingRequests.remove(r);
			}
			
			for (Iterator<D> it = activeDrivers.iterator(); it.hasNext();) {
				D driver = it.next();
				
				// What the driver has to do in current frame
								
				if (driver.end.compareTo(currentTime) > 0) {
					it.remove();
				}
			}
			
			currentTime.Add(Calendar.MILLISECOND, advanceTimeInterval);
		}
	}
	
	public abstract Status ProcessRequest(Request<P> r);
}
