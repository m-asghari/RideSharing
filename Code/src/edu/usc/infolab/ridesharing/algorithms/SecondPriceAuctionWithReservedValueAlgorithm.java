package edu.usc.infolab.ridesharing.algorithms;

import java.util.ArrayList;
import java.util.Collections;

import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.auction.AuctionDriver;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;
import edu.usc.infolab.ridesharing.auction.Bid;

public class SecondPriceAuctionWithReservedValueAlgorithm extends AuctionAlgorithm {

	public SecondPriceAuctionWithReservedValueAlgorithm(Time startTime, int ati) {
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
		AuctionDriver winner = bids.get(lastIndex).driver;
		r.serverProfit = bids.get(lastIndex).value;
		this.profit += r.serverProfit;
		return winner;
	}

	@Override
	protected String GetName() {
		return "SPARV";
	}

}
