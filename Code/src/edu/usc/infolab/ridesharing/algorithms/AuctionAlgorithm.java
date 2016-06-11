package edu.usc.infolab.ridesharing.algorithms;

import java.util.ArrayList;

import edu.usc.infolab.ridesharing.Status;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;
import edu.usc.infolab.ridesharing.auction.AuctionDriver;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;
import edu.usc.infolab.ridesharing.auction.Bid;
import edu.usc.infolab.ridesharing.datasets.real.nyctaxi.AuctionInput;

public abstract class AuctionAlgorithm extends Algorithm<AuctionRequest, AuctionDriver> {
	public Double profit;
	
	// time should be in minutes
	public static Double FARE(Double distance, int time) {
		return 5 + (2. * distance);
	}

	public AuctionAlgorithm(Time startTime, int ati) {
		super(startTime, ati);
		profit = 0.;
	}

	@Override
	public Status ProcessRequest(AuctionRequest r, Time time) {
		ArrayList<AuctionDriver> potentialDrivers = GetPotentialDrivers(r);
		r.stats.potentialDrivers = potentialDrivers.size();
		ArrayList<Bid> bids = new ArrayList<Bid>();
		
		int maxBidComputation = Utils.Min_Integer;
		for (AuctionDriver d : potentialDrivers) {
			Time start = new Time();
			bids.add(d.ComputeBid(r, time));
			Time end = new Time();
			int bidTimeMillis = end.SubtractInMillis(start);
			if (bidTimeMillis > maxBidComputation) {
				maxBidComputation = bidTimeMillis;
			}
		}
		r.stats.schedulingTime = maxBidComputation;
		
		Time start = new Time();
		AuctionDriver selectedDriver = SelectWinner(r, bids);
		Time end = new Time();
		r.stats.assignmentTime = end.SubtractInMillis(start);
		if (selectedDriver == null) {
			return Status.NOT_ASSIGNED;
		}
		selectedDriver.AddRequest(r, time);
		return Status.ASSIGNED;
	}
	
	@Override
	protected AuctionDriver GetNewDriver() {
		return AuctionInput.GetNewDriver();
	}

	public abstract AuctionDriver SelectWinner(AuctionRequest r, ArrayList<Bid> bids);

}