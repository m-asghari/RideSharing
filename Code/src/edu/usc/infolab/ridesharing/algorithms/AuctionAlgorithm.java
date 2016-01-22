package edu.usc.infolab.ridesharing.algorithms;

import java.util.ArrayList;

import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Pair;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Status;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.auction.AuctionDriver;

public abstract class AuctionAlgorithm extends Algorithm<GPSPoint, Request<GPSPoint>, AuctionDriver> {

	public AuctionAlgorithm(Time startTime, int ati) {
		super(startTime, ati);
	}

	@Override
	public Status ProcessRequest(Request<GPSPoint> r) {
		ArrayList<AuctionDriver> potentialDrivers = GetPotentialDrivers(r);
		ArrayList<Pair<AuctionDriver, Double>> bids = new ArrayList<Pair<AuctionDriver, Double>>();
		
		int maxBidComputation = Integer.MIN_VALUE;
		for (AuctionDriver d : potentialDrivers) {
			Time start = new Time();
			bids.add(new Pair<AuctionDriver, Double>(d, d.ComputeBid(r)));
			Time end = new Time();
			int bidTimeMillis = end.Subtract(start);
			if (bidTimeMillis > maxBidComputation) {
				maxBidComputation = bidTimeMillis;
			}
		}
		r.stats.bidComputationTime = maxBidComputation;
		
		Time start = new Time();
		AuctionDriver selectedDriver = SelectWinner(bids);
		Time end = new Time();
		r.stats.selectWinnerTime = end.Subtract(start);
		
		return null;
	}
	
	private ArrayList<AuctionDriver> GetPotentialDrivers(Request<GPSPoint> r) {
		return activeDrivers;
	}

	public abstract AuctionDriver SelectWinner(ArrayList<Pair<AuctionDriver, Double>> bids);

}
