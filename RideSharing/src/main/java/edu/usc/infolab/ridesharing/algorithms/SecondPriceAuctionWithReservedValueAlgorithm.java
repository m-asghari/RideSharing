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
		}
		if (highestBid == null)
			return null;
		if (highestBid.profit < 0) {
			return null;
		}
		if (highestBid.profit == 0 && highestBid.schedule.isEmpty()) {
			return null;
		}
		AuctionDriver winner = highestBid.driver;
		r.serverProfit = highestBid.profit;
		this.profit += r.serverProfit;
		return winner;
	}

	@Override
	public String GetName() {
		return "SPARV";
	}

}
