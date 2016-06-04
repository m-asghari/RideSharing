package edu.usc.infolab.ridesharing.algorithms;

import java.util.ArrayList;
import java.util.Iterator;

import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Status;
import edu.usc.infolab.ridesharing.Time;

public abstract class Algorithm<R extends Request, D extends Driver<R>> {
	protected Time currentTime;
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
		//ArrayList<D> remainingDrivers = new ArrayList<D>(drivers);
		//ArrayList<R> busyRequests = new ArrayList<R>();
		int reqCount = 0;
		while (activeRequests.size() > 0 || remainingRequests.size() > 0) {
			//Add newly activated drivers
			/*while (!remainingDrivers.isEmpty() && remainingDrivers.get(0).start.compareTo(currentTime) <= 0) {
				activeDrivers.add(remainingDrivers.remove(0));
			}*/
			
			while (!remainingRequests.isEmpty() && remainingRequests.get(0).requestTime.compareTo(currentTime) <= 0) {
				
				while (totalDriversAvailability() < preferredTotalDriversAvailability) {
					activeDrivers.add(GetNewDriver());
				}
				
				R r = remainingRequests.get(0);
				if (ProcessRequest(r, currentTime) == Status.ASSIGNED) {
					activeRequests.add(r);
				}
				System.out.println(String.format("Processed Request %d", reqCount++));
				remainingRequests.remove(r);
			}
			
			for (Iterator<D> it = activeDrivers.iterator(); it.hasNext();) {
				D driver = it.next();
				
				// What the driver has to do in current frame
				ArrayList<R> doneRequests = driver.UpdateLocation(1, currentTime);
				for (R r : doneRequests) {
					activeRequests.remove(r);
				}
								
				if (driver.end.compareTo(currentTime) < 0) {
					it.remove();
				}
			}
			
			currentTime.Add(advanceTimeInterval);
		}
	}
	
	private int preferredTotalDriversAvailability = 2000;
	private int totalDriversAvailability() {
		int availability = 0;
		for (D d : activeDrivers) {
			availability += (d.maxPassenger - (d.acceptedRequests.size() + d.onBoardRequests.size()));
		}
		return availability;
	}
	
	public abstract Status ProcessRequest(R r, Time time);
	
	protected abstract D GetNewDriver();
}
