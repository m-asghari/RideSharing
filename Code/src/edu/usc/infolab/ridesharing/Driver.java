package edu.usc.infolab.ridesharing;

import java.util.ArrayList;

import javax.activity.InvalidActivityException;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.geom.GPSNode.Type;
import edu.usc.infolab.geom.GPSPoint;

public abstract class Driver<R extends Request> implements Comparable<Driver<R>>{
	private static int driverCtr = 0;
	
	public GPSPoint loc;
	public ArrayList<R> acceptedRequests;
	public ArrayList<R> onBoardRequests;
	public ArrayList<R> servicedRequests;
	protected ArrayList<GPSNode> _schedule;
	
	public int id;
	
	public double travelledDistance;
	
	public double collectedFare;
	public double income;
	
	protected boolean _getPaid;
	protected double _paidTravelledDistance;
	
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
		this.travelledDistance = 0;
		this.maxPassenger = 5;
		this._getPaid = false;
		this._paidTravelledDistance = 0;
		this.collectedFare = 0;
		this.income = 0;
	}
	
	public Driver(String[] args) {
		try {
			if (args.length < 7) {
				throw new InvalidActivityException("Not Enough Arguments for Driver.");
			}
			this.id = Integer.parseInt(args[0]);
			//args[1] servicedRequest.size()
			this.travelledDistance = Double.parseDouble(args[2]);
			this._getPaid = Boolean.parseBoolean(args[3]);
			this._paidTravelledDistance = Double.parseDouble(args[4]);
			this.collectedFare = Double.parseDouble(args[5]);
			this.income = Double.parseDouble(args[6]);
		} catch (InvalidActivityException iae) {
			iae.printStackTrace();
		}
	}
	
	public ArrayList<R> UpdateLocation(double length, Time time) {
		ArrayList<R> finishedRequests = new ArrayList<R>();
		if (_schedule.isEmpty()) 
			return finishedRequests;
		GPSNode dest = _schedule.get(0);
		double dist = loc.DistanceInMilesAndMillis(dest.point).First;
		if (dist > length) {
			MoveTowards(dest.point, length);
			UpdateTravelledDistance(length);
		} else {
			this.loc.Set(dest.point);
			
			UpdateTravelledDistance(dist);
			finishedRequests = NewPointUpdates(time);
			Time newTime = time.clone();
			newTime.AddMillis(GPSPoint.TravelTimeInMillis(dist));
			
			if (_schedule.size() > 0) {
				finishedRequests.addAll(UpdateLocation(length - dist, newTime));
			}
		}
		return finishedRequests;
	}
	

	public String PrintShortResults() {
		StringBuilder results = new StringBuilder();
		results.append(String.format("%d,%d,%.2f,%s,%.2f,%.2f,%.2f,", 
				id, servicedRequests.size(), travelledDistance,
				Boolean.toString(_getPaid), _paidTravelledDistance,
				collectedFare, income));		
		return results.toString();
	}
	
	public String PrintLongResults() {
		StringBuilder results = new StringBuilder();
		results.append(String.format("id: %d\n"
				+ "Serviced Requests: %d\n"
				+ "Travelled Distance: %.2f, Paid Travelled Distance: %.2f\n"
				+ "Collected Fare: %.2f, Income: %.2f\n", 
				id, servicedRequests.size(), travelledDistance,
				_paidTravelledDistance, collectedFare, income));		
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
		this.travelledDistance += length;
		if (_getPaid) {
			this._paidTravelledDistance += length;
		}
	}
	
	/*
	 * Updates to driver's schedule or any other field once
	 * a driver reaches a new point on it's schedule goes
	 * here.
	 */
	protected ArrayList<R> NewPointUpdates(Time time) {
		ArrayList<R> finishedRequests = new ArrayList<R>();
		GPSNode currentNode = _schedule.remove(0);
		@SuppressWarnings("unchecked")
		R request = (R)currentNode.request;
		if (currentNode.type == Type.source) {
			PickUpUpdates(request, time);
		}
		if (currentNode.type == Type.destination) {
			DropOffUpdates(request, time);
			finishedRequests.add(request);
		}
		return finishedRequests;
	}
	
	protected void PickUpUpdates(R request, Time time) {
		request.pickUpTime.SetTime(time);
		request.pickUpDistance = travelledDistance;
		this._getPaid = true;
		this.acceptedRequests.remove(request);
		this.onBoardRequests.add(request);
	}
	
	protected void DropOffUpdates(R request, Time time) {
		request.dropOffTime.SetTime(time);
		request.dropOffDistance = travelledDistance;
		request.actualDistance = travelledDistance - request.pickUpDistance;
		request.detour = request.actualDistance - request.optDistance;
		request.actualTime = time.SubtractInMinutes(request.pickUpTime);
		request.finalFare = request.profile(request.detour) * request.defaultFare;
		this.onBoardRequests.remove(request);
		this.servicedRequests.add(request);
		if (this._schedule.isEmpty()) {
			_getPaid = false;
		}
		this.collectedFare += request.finalFare;
		this.income = GetIncome(request, time);
	}
	
	protected double GetIncome(R request, Time time) {
		return GetCost(_paidTravelledDistance, null);
	}

	// if time provided, will be in millisecods
	protected double GetCost(Double dist, Double time) {
		return 1 * dist;
	}
	
	public abstract void AddRequest(R r, Time time);
	
	@Override
	public int compareTo(Driver<R> o) {
		return this.start.compareTo(o.start);
	}
	
	public void Check(Time time) {}
}
