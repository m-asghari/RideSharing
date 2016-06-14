package edu.usc.infolab.ridesharing.algorithms;

import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Status;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.launcher.ResultGenerator;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class Algorithm<R extends Request, D extends Driver<R>> {
	protected Time currentTime;
	// Ding: vary this in the experiment
	private int advanceTimeInterval; 
	
	public ArrayList<D> activeDrivers;
	public ArrayList<R> activeRequests;
	
	public Algorithm(Time startTime, int ati) {
		currentTime = startTime.clone();
		advanceTimeInterval = ati;
		activeDrivers = new ArrayList<D>();
		activeRequests = new ArrayList<R>();
	}
	
	public String Run(ArrayList<R> requests, ArrayList<D> drivers) {
		ArrayList<R> remainingRequests = new ArrayList<R>(requests);
		activeDrivers = new ArrayList<>(drivers);
		ArrayList<D> allDrivers = new ArrayList<D>(drivers);
		int reqCount = 0;

		// maintain two lists of request
		while (activeRequests.size() > 0 || remainingRequests.size() > 0) {
		    // process each unexpired requests till the current time
			while (!remainingRequests.isEmpty() && remainingRequests.get(0).requestTime.compareTo(currentTime) <= 0) {				
				
			  
			    /*              
				 * add drivers into the simulation if it is less than the predefined number
				 * while (totalDriversAvailability() < preferredTotalDriversAvailability) {
					for (int i = 0; i < 1; i++) {
						D driver = GetNewDriver();
						activeDrivers.add(driver);
						allDrivers.add(driver);
					}
				}
				*/
				
				R r = remainingRequests.get(0);
				// process the request with different strategies, second price, first price, nearest neighbor, kinect tree strategy
				if (ProcessRequest(r, currentTime) == Status.ASSIGNED) {
					r.stats.assigned = 1;
					activeRequests.add(r);
				}
				reqCount++;
				if (reqCount % 5000 == 0) {
					System.out.println(String.format("Processed %d requests.", reqCount));
				}
				remainingRequests.remove(r);
			}
			
			// update drivers' locations
			for (Iterator<D> it = activeDrivers.iterator(); it.hasNext();) {
				D driver = it.next();
				
				driver.Check(currentTime);
				// All driver proceed 1 mile further?
				// Ding: Why not using ati here?? 
				ArrayList<R> doneRequests = driver.UpdateLocation(1, currentTime);
				driver.Check(currentTime);
				for (R r : doneRequests) {
					activeRequests.remove(r);
				}
				
				// remove the current driver if the driver's schedule is empty
				if (driver.end.compareTo(currentTime) < 0 && driver.onBoardRequests.size() == 0 && driver.acceptedRequests.size() == 0) {
					it.remove();
				}
			}
			
			// Ding: advance the current time in minutes, varying this parameter
			currentTime.AddMinutes(advanceTimeInterval);
		}
		ResultGenerator.SaveData(GetName(), requests, allDrivers);
		System.out.println(ResultGenerator.Summary(requests, allDrivers));
		return ResultGenerator.ShortSummary(requests, allDrivers);
	}
	
	protected ArrayList<D> GetPotentialDrivers(R r) {
		ArrayList<D> potentialDrivers = new ArrayList<D>();
		for (D driver : this.activeDrivers) {
			if (driver.acceptedRequests.size() + driver.onBoardRequests.size() >= driver.maxPassenger)
				continue;
			
			// the time from driver's location to the request's source point
			Double time = driver.loc.DistanceInMilesAndMillis(r.source.point).Second;
			Time eat = currentTime.clone();
			eat.AddMillis(time.intValue());

            //  cannot arrive before the maximal waiting time
			if (eat.compareTo(r.latestPickUpTime) <= 0) {
				potentialDrivers.add(driver);
			}
		}
		return potentialDrivers;
	}
	
	@SuppressWarnings("unused")
  private int preferredTotalDriversAvailability = 10;

	@SuppressWarnings("unused")
  private int totalDriversAvailability() {
		int availability = 0;
		for (D d : activeDrivers) {
			availability += (d.maxPassenger - (d.acceptedRequests.size() + d.onBoardRequests.size()));
		}
		return availability;
	}
	
	public abstract Status ProcessRequest(R r, Time time);
	
	protected abstract D GetNewDriver();
	
	public abstract String GetName();
}
