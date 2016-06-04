package edu.usc.infolab.ridesharing.kinetictree;

import edu.usc.infolab.ridesharing.Request;

public class KTRequest extends Request {
	int maxWaitingTime; //seconds -> speed: 1 unit of length per second
	double serviceConstraint;
	
	protected KTRequest(KTRequest other) {
		super(other);
		this.maxWaitingTime = other.maxWaitingTime;
		this.serviceConstraint = other.serviceConstraint;
	}
	
	public KTRequest clone() {
		return new KTRequest(this);
	}
}
