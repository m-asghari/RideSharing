package edu.usc.infolab.ridesharing.auction;

import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.algorithms.AuctionAlgorithm;

public class AuctionRequest extends Request {
	public double serverProfit;
	public double defaultFare;
	public double finalFare;
	
	public AuctionRequest(GPSPoint source, GPSPoint dest, Time requestTime,
			int maxWaitTime) {
		super(source, dest, requestTime, maxWaitTime);
		serverProfit = 0;
		defaultFare = AuctionAlgorithm.FARE(optDistance, optTime);
		finalFare = -1;
	}

	public Double profile(Double detour) {
		if (detour.compareTo(60.) < 0)
			return 1. - (0.00025 * Math.pow(detour, 2));
		return 0.1;
	}
	
	@Override
	public void DropOff(double distance, Time time) {
		super.DropOff(distance, time);
		this.finalFare = this.profile(this.detour) * this.defaultFare;
	}
	
	public String PrintShortResults() {
		StringBuilder results = new StringBuilder();
		results.append(super.PrintShortResults());
		results.append(String.format("%.2f,%.2f,%.2f",
				defaultFare, finalFare, serverProfit));
		return results.toString();
	}
	
	public String PrintLongResults() {
		StringBuilder results = new StringBuilder();
		results.append(super.PrintLongResults());
		results.append(String.format("Default Fare: %.2f, Final Fare: %.2f, Server Profit: %.2f",
				defaultFare, finalFare, serverProfit));
		return results.toString();
	}
}
