package edu.usc.infolab.ridesharing;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.geom.GPSNode.Type;
import edu.usc.infolab.geom.GPSPoint;

public class Request implements Comparable<Request>{
	private static int reqCtr = 0;
	
	public class AssignmentStat {
		public AssignmentStat() {}
		
		private AssignmentStat(AssignmentStat other) {
			this.assigned = other.assigned;
			this.bidComputationTime = other.bidComputationTime;
			this.selectWinnerTime = other.selectWinnerTime;
			this.potentialDrivers = other.potentialDrivers;
			this.acceptableBids = other.acceptableBids;
		}
		
		public boolean assigned;
		public int bidComputationTime;
		public int selectWinnerTime;
		public int potentialDrivers;
		public int acceptableBids;
		
		public AssignmentStat clone() {
			return new AssignmentStat(this);
		}
	}
	
	public int id;
	public AssignmentStat stats;
	
	public GPSNode source;
	public GPSNode destination;
	
	public Time requestTime;
	public int maxWaitTime;
	public Time latestPickUpTime;	
	
	public int optTime;
	public double optDistance;
	
	public Time pickUpTime;
	public double pickUpDistance;
	
	public Time dropOffTime;
	public double dropOffDistance;
	
	public double detour;
	public int actualTime;
	public double actualDistance;	
	
	public Request(GPSPoint source, GPSPoint dest, Time requestTime, int maxWaitTime) {
		this.id = reqCtr++;
		this.stats = new AssignmentStat();
		this.source = new GPSNode(source, Type.source, this);
		this.destination = new GPSNode(dest, Type.destination, this);
		this.requestTime = requestTime.clone();
		this.maxWaitTime = maxWaitTime;
		this.latestPickUpTime = requestTime.clone();
		this.latestPickUpTime.Add(maxWaitTime);
		Pair<Double, Double> shortestPath = ShortestPath(this.source.point, this.destination.point); 
		this.optDistance = shortestPath.First;
		this.optTime = shortestPath.Second.intValue();
		this.pickUpTime = new Time();
		this.pickUpDistance = -1;
		this.dropOffTime = new Time();
		this.dropOffDistance = -1;
		this.detour = -1;
		this.actualTime = -1;
		this.actualDistance = -1;
	}
	
	protected Request(Request other) {
		this.id = other.id;
		this.stats = other.stats.clone();
		this.source = other.source.clone();
		this.destination = other.destination.clone();
		this.requestTime = other.requestTime.clone();
		this.maxWaitTime = other.maxWaitTime;
		this.latestPickUpTime = other.latestPickUpTime.clone();
		this.optTime = other.optTime;
		this.optDistance = other.optDistance;
		this.pickUpTime = other.pickUpTime.clone();
		this.pickUpDistance = other.pickUpDistance;
		this.dropOffTime = other.dropOffTime.clone();
		this.dropOffDistance = other.dropOffDistance;
		this.actualTime = other.actualTime;
		this.actualDistance = other.actualDistance;
	}
	
	public void PickUp(double distance, Time time) {
		this.pickUpTime.SetTime(time);;
		this.pickUpDistance = distance;		
	}
	
	public void DropOff(double distance, Time time) {
		this.dropOffTime.SetTime(time);
		this.dropOffDistance = distance;
		this.actualDistance = distance - this.pickUpDistance;
		this.detour = this.actualDistance - this.optDistance;
		this.actualTime = time.Subtract(this.pickUpTime);
	}
	
	/*
	 * Depending on the type of P it can either be the Euclidean distance or
	 * the shortest path on road network.
	 */
	private Pair<Double, Double> ShortestPath(GPSPoint s,GPSPoint d) {
		return s.Distance(d);
	}
	
	public Request clone() {
		return new Request(this);
	}

	@Override
	public int compareTo(Request o) {
		return requestTime.compareTo(o.requestTime);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Request) {
			Request otherRequest = (Request)obj;
			return (this.id == otherRequest.id);
		}
		return false;		
	}
	
	//id,stats.assigned,stats.bidTime,stats.selectTime,stats.potentialDrivers,stats.acceptableBids,source,destination,rTime,maxW,latestPickUpTime,optTime,OptDist,pickUpTime,pickUpDistance,dropOffTime,dropOffDistance,detour,actualTime,actualDistance
	public String PrintShortResults() {
		StringBuilder results = new StringBuilder();
		results.append(String.format("%d,%s,%d,%d,%d,%d,%s,%s,%s,%d,%s,%d,%.2f,%s,%.2f,%s,%.f,%.2f,%d,%.2f",
				id, 
				stats.assigned, stats.bidComputationTime, stats.selectWinnerTime, stats.potentialDrivers, stats.acceptableBids, 
				source.toString(), destination.toString(), 
				requestTime.toString(), maxWaitTime, latestPickUpTime.toString(), 
				optTime, optDistance, 
				pickUpTime.toString(), pickUpDistance,
				dropOffTime.toString(),	dropOffDistance,
				detour, actualTime, actualDistance));
		return results.toString();
	}
	
	public String PrintLongResults() {
		StringBuilder results = new StringBuilder();
		results.append(String.format("id:%d\n"
				+ "Assigned: %s, Bid Time: %d, Select Time: %d, #Potential Drivers: %d, #Acceptable Bids: %d\n"
				+ "Source: %s, Destination: %s\n"
				+ "Request Time: %s, Max Wait Time: %d, Latest PickUp Time: %s\n"
				+ "Opt Time: %d, Opt Distance: %.2f\n"
				+ "PickUp Time: %s, PickUp Distance: %.2f\n"
				+ "DropOff Time: %s, DropOff Distance: %.f\n"
				+ "Detour: %.2f, Actual Time: %d, Actual Distance%.2f\n",
				id, 
				stats.assigned, stats.bidComputationTime, stats.selectWinnerTime, stats.potentialDrivers, stats.acceptableBids, 
				source.toString(), destination.toString(), 
				requestTime.toString(), maxWaitTime, latestPickUpTime.toString(), 
				optTime, optDistance, 
				pickUpTime.toString(), pickUpDistance,
				dropOffTime.toString(),	dropOffDistance,
				detour, actualTime, actualDistance));
		return results.toString();
	}
	
	@Override
	public String toString() {
		return String.format("%d\n", id);
	}
}
