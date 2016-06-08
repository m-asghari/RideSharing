package edu.usc.infolab.ridesharing.kinetictree;

import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Time;

public class KTRequest extends Request {
	public double serviceConstraint;
	
	public KTRequest(GPSPoint source, GPSPoint destination, Time requestTime, int maxWaitTime) {
		super(source, destination, requestTime, maxWaitTime);
		this.serviceConstraint = 0.9;
	}
	
	protected KTRequest(KTRequest other) {
		super(other);
		this.serviceConstraint = other.serviceConstraint;
	}
	
	public KTRequest clone() {
		return new KTRequest(this);
	}
}
