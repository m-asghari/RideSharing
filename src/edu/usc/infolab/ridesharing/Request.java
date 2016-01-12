package edu.usc.infolab.ridesharing;

import java.awt.geom.Point2D;

public class Request {
	public class AssignmentStat {
		public boolean assigned;
		public int bidComputationTime;
		public int selectWinnerTime;		
	}
	public AssignmentStat stats;
	
	public Time requestTime;
	public Time startTime;
	public Time endTime;
	public Point2D.Double source;
	public Point2D.Double destination;
}
