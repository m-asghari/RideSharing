package edu.usc.infolab.ridesharing.algorithms;

import edu.usc.infolab.ridesharing.Status;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;
import edu.usc.infolab.ridesharing.auction.AuctionDriver;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;
import edu.usc.infolab.ridesharing.auction.Bid;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class AuctionAlgorithm<D extends AuctionDriver> extends Algorithm<AuctionRequest, D> {
    public double profit;
    public double fpaProfit;
    public double spaProfit;
    public double sparvProfit;

    public AuctionAlgorithm(Time startTime, int ati) {
        super(startTime, ati);
        profit = 0.;
        fpaProfit = 0.f;
        spaProfit = 0.f;
        sparvProfit = 0.f;
    }

    /**
     * Bid the request independently from each driver
     */
    @Override
    public Status ProcessRequest(AuctionRequest r, Time time) {
        ArrayList<D> potentialDrivers = GetPotentialDrivers(r);
        r.stats.potentialDrivers = potentialDrivers.size();
        ArrayList<Bid> bids = new ArrayList<Bid>();
        // select the best bid
        int maxBidComputation = Utils.Min_Integer;
        for (AuctionDriver d : potentialDrivers) {
            Utils.spComputations = 0;
            Calendar start = Calendar.getInstance();
            // insert request into one driver's schedule
            Bid bid = d.ComputeBid(r, time);
            Calendar end = Calendar.getInstance();
			if (bid.driver != null) {
			    bids.add(bid);
            }

            int bidTimeMillis = (int) (end.getTimeInMillis() - start.getTimeInMillis());
            if (bidTimeMillis > 10) {
                Utils.spComputations = 0;
                Calendar s = Calendar.getInstance();
                d.ComputeBid(r, time);
                Calendar e = Calendar.getInstance();
                int bidTimeMillis2 = (int) (e.getTimeInMillis() - s.getTimeInMillis());
                if (bidTimeMillis2 < bidTimeMillis) {
                    bidTimeMillis = bidTimeMillis2;
                }
            }
            r.stats.spComputations.add(Utils.spComputations);
            // track the computation time, i.e., the maximal time of one auction driver
            if (bidTimeMillis > maxBidComputation) {
                maxBidComputation = bidTimeMillis;
            }
        }
        r.stats.schedulingTime = maxBidComputation;

        Time start = new Time();
        // select the bids based on the auction algorithm
        Random rand = new Random();
        //int expectedBids = (int) ((rand.nextGaussian() * 3) + 7);
        int expectedBids = Math.max((int)((double)bids.size()/20), (int) ((rand.nextGaussian() * 3) + 10));
        ArrayList<Bid> selectedBids = new ArrayList<>();
        if (bids.size() > expectedBids) {
            for (int i = 0; i < expectedBids; i++) {
                selectedBids.add(bids.get(rand.nextInt(expectedBids)));
            }
        } else {
            selectedBids = new ArrayList<>(bids);
        }
        AuctionDriver selectedDriver = SelectWinner(r, selectedBids);
        //AuctionDriver selectedDriver = SelectWinner(r, bids);
        Time end = new Time();
        r.stats.assignmentTime = end.SubtractInMillis(start);
        if (selectedDriver == null) {
            return Status.NOT_ASSIGNED;
        }
        selectedDriver.AddRequest(r, time);
        return Status.ASSIGNED;
    }

    public AuctionDriver SelectWinner(AuctionRequest r, ArrayList<Bid> bids) {
        ArrayList<Bid> modifiedBids = new ArrayList<>();
        //double waitTimeFactor = Utils.GetWaitTimeFactor(r.maxWaitTime);
        //double cheatingFactor = (double)(bids.size()-1)/(double)bids.size() * (1 + waitTimeFactor);
        double cheatingFactor = (bids.size() > 1) ? (double)(bids.size()-1)/(double)bids.size() : 1;
        for (Bid bid : bids) {
            if (bid.driver.isCheater) {
                double diff = (1.f - cheatingFactor) * bid.profit;
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
        Bid secondHighestBid = null;
        double highestValue = Utils.Min_Double;
        double secondHighestValue = Utils.Min_Double;

        double maxDriverCost = 0;
        for (Bid bid : bids) {
            double driverCost = bid.driver.GetCost(r.optDistance, (double)r.optTime);
            maxDriverCost = (maxDriverCost < driverCost) ? driverCost : maxDriverCost;
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
        //double serverBid = (r.defaultFare - maxDriverCost);
        double serverBid = 0.90 * highestValue;
        if (highestBid == null || highestBid.profit < 0) {
            return null;
        }
        if (highestBid.profit == 0 && highestBid.schedule.isEmpty()) {
            return null;
        }
        if (secondHighestBid == null || secondHighestBid.profit <= 0) {
            secondHighestValue = 0;
        }
        if (highestBid.profit < serverBid) {
            r.stats.serverBidBetterThanFirstBid = 1;
            serverBid = 0;
        }
        if (secondHighestValue < serverBid) {
            r.stats.serverBidBetterThanSecondBid = 1;
        }
        if (highestModifiedBid.driver.isCheater) {
            r.stats.cheatingHelpedWinner = 1;
        }
        AuctionDriver winner = highestBid.driver;

        r.fpaProfit = highestModifiedValue;
        this.fpaProfit += r.fpaProfit;
        r.spaProfit = secondHighestValue;
        this.spaProfit += r.spaProfit;
        r.sparvProfit = (secondHighestValue < serverBid) ? serverBid : secondHighestValue;
        this.sparvProfit += r.sparvProfit;

        return winner;
    }

    @Override
    public String GetName() {
        return "AUC";
    }
}
