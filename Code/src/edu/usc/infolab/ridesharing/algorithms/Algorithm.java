package edu.usc.infolab.ridesharing.algorithms;

import java.util.ArrayList;
import java.util.Iterator;

import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Status;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.launcher.ResultGenerator;

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
		ArrayList<D> allDrivers = new ArrayList<D>(drivers);
		int reqCount = 0;
		while (totalDriversAvailability() < preferredTotalDriversAvailability) {
			for (int i = 0; i < 100; i++) {
				D driver = GetNewDriver();
				activeDrivers.add(driver);
				allDrivers.add(driver);
			}
		}
		while (activeRequests.size() > 0 || remainingRequests.size() > 0) {
			while (!remainingRequests.isEmpty() && remainingRequests.get(0).requestTime.compareTo(currentTime) <= 0) {
				
				/*while (totalDriversAvailability() < preferredTotalDriversAvailability) {
					for (int i = 0; i < 1; i++) {
						D driver = GetNewDriver();
						activeDrivers.add(driver);
						allDrivers.add(driver);
					}
				}*/
				
				R r = remainingRequests.get(0);
				if (ProcessRequest(r, currentTime) == Status.ASSIGNED) {
					r.stats.assigned = 1;
					activeRequests.add(r);
				}
				reqCount++;
				if (reqCount % 100 == 0) {
					System.out.println(String.format("Processed %d requests.", reqCount));
				}
				remainingRequests.remove(r);
			}
			
			for (Iterator<D> it = activeDrivers.iterator(); it.hasNext();) {
				D driver = it.next();
				
				ArrayList<R> doneRequests = driver.UpdateLocation(1, currentTime);
				for (R r : doneRequests) {
					activeRequests.remove(r);
				}
								
				if (driver.end.compareTo(currentTime) < 0 && driver.onBoardRequests.size() == 0 && driver.acceptedRequests.size() == 0) {
					it.remove();
				}
			}
			
			currentTime.Add(advanceTimeInterval);
		}
		ResultGenerator.SaveData(GetName(), requests, allDrivers);
		System.out.println(ResultGenerator.ShortSummary(requests, allDrivers));
	}
	
	protected ArrayList<D> GetPotentialDrivers(R r) {
		ArrayList<D> potentialDrivers = new ArrayList<D>();
		for (D driver : this.activeDrivers) {
			if (driver.acceptedRequests.size() + driver.onBoardRequests.size() >= driver.maxPassenger)
				continue;
			Double time = driver.loc.Distance(r.source.point).Second;
			Time eat = currentTime.clone();
			eat.Add(time.intValue());
			if (eat.compareTo(r.latestPickUpTime) <= 0) {
				potentialDrivers.add(driver);
			}
		}
		return potentialDrivers;
	}
	
	private int preferredTotalDriversAvailability = 10;
	private int totalDriversAvailability() {
		int availability = 0;
		for (D d : activeDrivers) {
			availability += (d.maxPassenger - (d.acceptedRequests.size() + d.onBoardRequests.size()));
		}
		return availability;
	}
	
	public abstract Status ProcessRequest(R r, Time time);
	
	protected abstract D GetNewDriver();
	
	protected abstract String GetName();
}
