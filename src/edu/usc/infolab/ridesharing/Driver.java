package edu.usc.infolab.ridesharing;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public abstract class Driver {
	public ArrayList<Request> activeRequests;
	public ArrayList<Request> servicedRequests;
	public ArrayList<Point2D.Double> schedule;
	
	public Time start;
	public Time end;
	
	public abstract double ComputeBid(Request r);
}
