package edu.usc.infolab.ridesharing.algorithms;

import java.util.ArrayList;

import edu.usc.infolab.ridesharing.Status;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;
import edu.usc.infolab.ridesharing.auction.AuctionDriver;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;
import edu.usc.infolab.ridesharing.auction.Bid;

public abstract class AuctionAlgorithm extends Algorithm<AuctionRequest, AuctionDriver> {
	public Double profit;
	
	// time should be in minutes
	public static Double FARE(Double distance, int time) {
		return 2. * distance;
	}

	public AuctionAlgorithm(Time startTime, int ati) {
		super(startTime, ati);
		profit = 0.;
	}

	@Override
	public Status ProcessRequest(AuctionRequest r, Time time) {
		System.out.println(String.format("Processing New Request"));
		ArrayList<AuctionDriver> potentialDrivers = GetPotentialDrivers(r);
		r.stats.potentialDrivers = potentialDrivers.size();
		ArrayList<Bid> bids = new ArrayList<Bid>();
		
		int maxBidComputation = Utils.Min_Integer;
		for (AuctionDriver d : potentialDrivers) {
			Time start = new Time();
			bids.add(d.ComputeBid(r, time));
			Time end = new Time();
			int bidTimeMillis = end.Subtract(start);
			if (bidTimeMillis > maxBidComputation) {
				maxBidComputation = bidTimeMillis;
			}
		}
		r.stats.bidComputationTime = maxBidComputation;
		
		Time start = new Time();
		AuctionDriver selectedDriver = SelectWinner(r, bids);
		Time end = new Time();
		r.stats.selectWinnerTime = end.Subtract(start);
		System.out.println(String.format(
				"Potential Drivers: %d, Acceptable Bids: %d", r.stats.potentialDrivers, r.stats.acceptableBids));
		if (selectedDriver == null) {
			System.out.println("Request Not Assigned.");
			return Status.NOT_ASSIGNED;
		}
		selectedDriver.AddRequest(r);
		System.out.println("Request Assigned.");
		return Status.ASSIGNED;
	}
	
	private ArrayList<AuctionDriver> GetPotentialDrivers(AuctionRequest r) {
		ArrayList<AuctionDriver> potentialDrivers = new ArrayList<AuctionDriver>();
		for (AuctionDriver driver : this.activeDrivers) {
			if (driver.acceptedRequests.size() + driver.onBoardRequests.size() >= driver.maxPassenger)
				continue;
			Double time = driver.loc.Distance(r.source.point).Second;
			Time eat = currentTime.clone();
			eat.Add(time.intValue());
			if (eat.compareTo(r.latestPickUpTime) <= 0) {
				potentialDrivers.add(driver);
			}
		}
		return potentialDrivers;
	}

	public abstract AuctionDriver SelectWinner(AuctionRequest r, ArrayList<Bid> bids);

}
