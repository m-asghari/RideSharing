package edu.usc.infolab.ridesharing;

import java.util.ArrayList;

import javax.activity.InvalidActivityException;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.geom.GPSPoint;

public abstract class Driver<R extends Request> implements Comparable<Driver<R>>{
	private static int driverCtr = 0;
	
	public GPSPoint loc;
	public ArrayList<R> acceptedRequests;
	public ArrayList<R> onBoardRequests;
	public ArrayList<R> servicedRequests;
	protected ArrayList<GPSNode> _schedule;
	
	public int id;
	
	protected double _travelledDistance;
	
	public Time start;
	public Time end;
	public int maxPassenger;
		
	public Driver(GPSPoint initialLoc, Time start, Time end) {
		this.id = driverCtr++;
		this.loc = initialLoc.clone();
		this.start = start.clone();
		this.end = end.clone();
		this.acceptedRequests = new ArrayList<R>();
		this.onBoardRequests = new ArrayList<R>();
		this.servicedRequests = new ArrayList<R>();
		this._schedule = new ArrayList<GPSNode>();
		_travelledDistance = 0;
		maxPassenger = 5;
	}
	
	public Driver(String[] args) {
		try {
			if (args.length < 3) {
				throw new InvalidActivityException("Not Enough Arguments for Driver.");
			}
			this.id = Integer.parseInt(args[0]);
			//args[1] servicedRequest.size()
			this._travelledDistance = Double.parseDouble(args[2]);
		} catch (InvalidActivityException iae) {
			iae.printStackTrace();
		}
	}
	
	public ArrayList<R> UpdateLocation(double length, Time time) {
		ArrayList<R> finishedRequests = new ArrayList<R>();
		if (_schedule.isEmpty()) 
			return finishedRequests;
		GPSNode dest = _schedule.get(0);
		double dist = loc.Distance(dest.point).First;
		if (dist > length) {
			MoveTowards(dest.point, length);
			UpdateTravelledDistance(length);
		} else {
			this.loc.Set(dest.point);
			
			UpdateTravelledDistance(dist);
			finishedRequests = NewPointUpdates(time);
			Time newTime = time.clone();
			newTime.Add(GPSPoint.TravelTimeInMinutes(dist));
			
			if (_schedule.size() > 0) {
				finishedRequests.addAll(UpdateLocation(length - dist, newTime));
			}
		}
		return finishedRequests;
	}
	

	public String PrintShortResults() {
		StringBuilder results = new StringBuilder();
		results.append(String.format("%d,%d,%.2f,", 
				id, servicedRequests.size(), _travelledDistance));		
		return results.toString();
	}
	
	public String PrintLongResults() {
		StringBuilder results = new StringBuilder();
		results.append(String.format("id: %d\n"
				+ "Serviced Requests: %d, Travelled Distance: %.2f\n", 
				id, servicedRequests.size(), _travelledDistance));		
		return results.toString();
	}
	
	@Override
	public String toString() {
		return String.format("%d\n", id);
	}
	
	/*
	 * Moves the driver toward a certain point. Depending on the type
	 * of loc (Either Euclidean Point or GPS Point) the driver can move
	 * along the straight line between two points or along the road network.
	 */
	private void MoveTowards(GPSPoint dest, Double length) {
		loc.MoveTowards(dest, length);
	}
	
	protected void UpdateTravelledDistance(double length) {
		this._travelledDistance += length;
	}
	
	/*
	 * Updates to driver's schedule or any other field once
	 * a driver reaches a new point on it's schedule goes
	 * here.
	 */
	protected abstract ArrayList<R> NewPointUpdates(Time time);
	
	public abstract void AddRequest(R r);
	
	@Override
	public int compareTo(Driver<R> o) {
		return this.start.compareTo(o.start);
	}
}
