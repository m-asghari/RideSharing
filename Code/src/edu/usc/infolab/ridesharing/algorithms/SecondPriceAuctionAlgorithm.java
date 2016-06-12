package edu.usc.infolab.ridesharing.algorithms;

import java.util.ArrayList;
import java.util.Collections;

import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.auction.AuctionDriver;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;
import edu.usc.infolab.ridesharing.auction.Bid;

public class SecondPriceAuctionAlgorithm extends AuctionAlgorithm {

	public SecondPriceAuctionAlgorithm(Time startTime, int ati) {
		super(startTime, ati);
	}

	@Override
	public AuctionDriver SelectWinner(AuctionRequest r, ArrayList<Bid> bids) {
		if (bids.isEmpty())
			return null;
		Collections.sort(bids);
		int lastIndex = bids.size() - 1;
		if (bids.get(lastIndex).value < 0) {
			return null;
		}
		if (bids.get(lastIndex).value == 0 && bids.get(lastIndex).schedule.size() == 0) {
			return null;
		}
		// winner is the last driver with highest bid
		AuctionDriver winner = bids.get(lastIndex).driver;
		double secondHighestBid = 0;
		// using the second highest bid as the profit
		if (bids.size() > 1 && bids.get(lastIndex - 1).value > 0)
			secondHighestBid = bids.get(lastIndex - 1).value;
		this.profit += secondHighestBid;
		r.serverProfit = secondHighestBid;
		return winner;
	}
	
	@Override
	protected String GetName() {
		return "SPA";
	}
}
