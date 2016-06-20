package edu.usc.infolab.ridesharing.algorithms;

import java.util.ArrayList;

import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;
import edu.usc.infolab.ridesharing.auction.AuctionDriver;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;
import edu.usc.infolab.ridesharing.auction.Bid;

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
			if (bid.value > highestValue) {
				secondHighestValue = highestValue;
				secondHighestBid = highestBid;
				highestValue = bid.value;
				highestBid = bid;
			} else if (bid.value > secondHighestValue) {
				secondHighestValue = bid.value;
				secondHighestBid = bid;
			}
		}
		if (highestBid == null)
			return null;
		if (highestBid.value < 0) {
			return null;
		}
		if (highestBid.value == 0 && highestBid.schedule.isEmpty()) {
			return null;
		}
		AuctionDriver winner = highestBid.driver;
		r.serverProfit = highestBid.value;
		this.profit += r.serverProfit;
		return winner;
	}

	@Override
	public String GetName() {
		return "SPARV";
	}

}
