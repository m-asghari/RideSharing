package edu.usc.infolab.ridesharing.algorithms;

import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;
import edu.usc.infolab.ridesharing.auction.AuctionDriver;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;
import edu.usc.infolab.ridesharing.auction.Bid;

import java.util.ArrayList;

public class SecondPriceAuctionWithReservedValueAlgorithm<D extends AuctionDriver> extends AuctionAlgorithm<D> {

	public SecondPriceAuctionWithReservedValueAlgorithm(Time startTime, int ati) {
		super(startTime, ati);
	}

	@Override
	public AuctionDriver SelectWinner(AuctionRequest r, ArrayList<Bid> bids) {
		if (bids.isEmpty())
			return null;
		Bid highestBid = null;
		@SuppressWarnings("unused")
		Bid secondHighestBid = null;
		double highestValue = Utils.Min_Double;
		double secondHighestValue = Utils.Min_Double;

		double maxDriverCost = 0;
		for (Bid bid : bids) {
			if (bid.profit > highestValue) {
				secondHighestValue = highestValue;
				secondHighestBid = highestBid;
				highestValue = bid.profit;
				highestBid = bid;
			} else if (bid.profit > secondHighestValue) {
				secondHighestValue = bid.profit;
				secondHighestBid = bid;
			}
			double driverCost = bid.driver.GetCost(r.optDistance, (double)r.optTime);
			maxDriverCost = (maxDriverCost < driverCost) ? driverCost : maxDriverCost;
		}
		if (highestBid == null || highestBid.profit < 0) {
			return null;
		}
		if (highestBid.profit == 0 && highestBid.schedule.isEmpty()) {
			return null;
		}
		double waitTimeFactor = Utils.GetWaitTimeFactor(r.maxWaitTime);
		double serverBid = (r.defaultFare - maxDriverCost) * 0.9;
		serverBid = 0.90 * highestValue;
		if (highestBid.profit < serverBid) {
		    r.stats.serverBidBetterThanFirstBid = 1;
		    //return null;
            serverBid = 0;
        }
        if (secondHighestBid == null || secondHighestBid.profit <= 0) {
            secondHighestValue = 0;
        }
        if (secondHighestValue < serverBid) {
		    secondHighestValue = serverBid;
		    r.stats.serverBidBetterThanSecondBid = 1;
        }
		AuctionDriver winner = highestBid.driver;
		r.serverProfit = secondHighestValue;
		this.profit += r.serverProfit;
		return winner;
	}

	@Override
	public String GetName() {
		return "SPARV";
	}

}
