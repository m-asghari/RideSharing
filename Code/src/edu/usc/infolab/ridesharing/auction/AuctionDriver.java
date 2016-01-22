package edu.usc.infolab.ridesharing.auction;

import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Request;

public class AuctionDriver extends Driver<GPSPoint, Request<GPSPoint>> {
	public double ComputeBid(Request<GPSPoint> r) {
		return 0;
	}

	@Override
	protected void AddRequest(Request<GPSPoint> r) {
		// TODO Auto-generated method stub
		
	}
}
