package edu.usc.infolab.ridesharing.kinetictree;

import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;

public class KTRequest extends Request {
	public double serviceConstraint;
	
	public KTRequest(GPSPoint source, GPSPoint destination, Time requestTime, int maxWaitTime) {
		super(source, destination, requestTime, maxWaitTime);
		this.serviceConstraint = Utils.MaxDetourRelative;
	}
	
	protected KTRequest(KTRequest other) {
		super(other);
		this.serviceConstraint = other.serviceConstraint;
	}
	
	@Override
	public KTRequest clone() {
		return new KTRequest(this);
	}
}
