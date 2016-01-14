package edu.usc.infolab.ridesharing.algorithms;

import java.util.ArrayList;

import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Pair;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Status;
import edu.usc.infolab.ridesharing.Time;

public abstract class AuctionAlgorithm extends Algorithm {

	public AuctionAlgorithm(Time startTime, int ati) {
		super(startTime, ati);
	}

	@Override
	public Status ProcessRequest(Request r) {
		ArrayList<Driver> potentialDrivers = GetPotentialDrivers(r);
		ArrayList<Pair<Driver, Double>> bids = new ArrayList<Pair<Driver, Double>>();
		
		int maxBidComputation = Integer.MIN_VALUE;
		for (Driver d : potentialDrivers) {
			Time start = new Time();
			bids.add(new Pair<Driver, Double>(d, d.ComputeBid(r)));
			Time end = new Time();
			int bidTimeMillis = end.Subtract(start);
			if (bidTimeMillis > maxBidComputation) {
				maxBidComputation = bidTimeMillis;
			}
		}
		r.stats.bidComputationTime = maxBidComputation;
		
		Time start = new Time();
		Driver selectedDriver = SelectWinner(bids);
		Time end = new Time();
		r.stats.selectWinnerTime = end.Subtract(start);
		
		return null;
	}
	
	private ArrayList<Driver> GetPotentialDrivers(Request r) {
		return activeDrivers;
	}

	public abstract Driver SelectWinner(ArrayList<Pair<Driver, Double>> bids);

}
