package edu.usc.infolab.ridesharing.algorithms;

import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;
import edu.usc.infolab.ridesharing.auction.AuctionDriver;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;
import edu.usc.infolab.ridesharing.auction.Bid;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Mohammad on 5/31/2017.
 */
public class FirstPriceAuctionAlgorithm<D extends AuctionDriver> extends AuctionAlgorithm<D> {
    private double cheatingProb = 0.75;
    public FirstPriceAuctionAlgorithm(Time startTime, int ati) {
        super(startTime, ati);
    }

    @Override
    public AuctionDriver SelectWinner(AuctionRequest r, ArrayList<Bid> bids) {
        if (bids.isEmpty())
            return null;
        ArrayList<Bid> modifiedBids = new ArrayList<>();
        double waitTimeFactor = Utils.GetWaitTimeFactor(r.maxWaitTime);
        double cheatingFactor = (double)(bids.size()-1)/(double)bids.size() * (1 + waitTimeFactor);
        for (Bid bid : bids) {
            if (bid.driver.isCheater) {
                double diff = cheatingFactor * bid.profit;
                modifiedBids.add(new Bid(bid.driver, bid.schedule, bid.profit - diff, bid.cost+diff));
            } else {
                modifiedBids.add(new Bid(bid.driver, bid.schedule, bid.profit, bid.cost));
            }
        }
        Bid highestModifiedBid = null;
        double highestModifiedValue = Utils.Min_Double;
        for (Bid bid : modifiedBids) {
            if (bid.profit > highestModifiedValue) {
                highestModifiedValue = bid.profit;
                highestModifiedBid = bid;
            }
        }

        Bid highestBid = null;
        double highestValue = Utils.Min_Double;
        for (Bid bid : bids) {
            if (bid.profit > highestValue) {
                highestValue = bid.profit;
                highestBid = bid;
            }
        }
        if (highestModifiedBid == null || highestModifiedBid.profit < 0) {
            return null;
        }
        if (highestModifiedBid.profit == 0 && highestModifiedBid.schedule.isEmpty()) {
            return null;
        }
        if (highestBid.driver.id != highestModifiedBid.driver.id) {
            r.stats.cheatingHelpedWinner = 1;
        }
        AuctionDriver winner = highestModifiedBid.driver;
        r.serverProfit = highestModifiedValue;
        //r.serverProfit = highestValue;
        this.profit += r.serverProfit;
        return winner;
    }

    @Override
    public String GetName() {
        return "FPA";
    }
}
