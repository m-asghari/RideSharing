package edu.usc.infolab.ridesharing;

import edu.usc.infolab.geom.Point;

public class Request<P extends Point> {
	private static int reqCtr = 0;
	
	public class AssignmentStat {
		private AssignmentStat(AssignmentStat other) {
			this.assigned = other.assigned;
			this.bidComputationTime = other.bidComputationTime;
			this.selectWinnerTime = other.selectWinnerTime;
		}
		
		public boolean assigned;
		public int bidComputationTime;
		public int selectWinnerTime;
		
		public AssignmentStat clone() {
			return new AssignmentStat(this);
		}
	}
	
	public int id;
	public AssignmentStat stats;
	
	public Time requestTime;
	public P source;
	public P destination;
	public double optDistance;
	
	public Request() {
		this.id = reqCtr++;
		
		this.optDistance = ShortestPath(source, destination);
	}
	
	@SuppressWarnings("unchecked")
	protected Request(Request<P> other) {
		this.id = other.id;
		this.stats = other.stats.clone();
		this.requestTime = other.requestTime.clone();
		this.source = (P)other.source.clone();
		this.destination = (P)other.destination.clone();
		this.optDistance = other.optDistance;
	}
	
	private double ShortestPath(P s, P d) {
		return s.Distance(d);
	}
	
	public Request<P> clone() {
		return new Request<P>(this);
	}
}
