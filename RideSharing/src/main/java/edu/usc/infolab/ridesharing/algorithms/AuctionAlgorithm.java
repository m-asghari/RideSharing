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

public abstract class AuctionAlgorithm<D extends AuctionDriver> extends Algorithm<AuctionRequest, D> {
    public Double profit;

    public AuctionAlgorithm(Time startTime, int ati) {
        super(startTime, ati);
        profit = 0.;
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
        int expectedBids = (int) ((rand.nextGaussian() * 3) + 7);
        ArrayList<Bid> selectedBids = new ArrayList<>();
        if (bids.size() > expectedBids) {
            for (int i = 0; i < expectedBids; i++) {
                selectedBids.add(bids.get(rand.nextInt(expectedBids)));
            }
        } else {
            selectedBids = new ArrayList<>(bids);
        }
        AuctionDriver selectedDriver = SelectWinner(r, selectedBids);
        Time end = new Time();
        r.stats.assignmentTime = end.SubtractInMillis(start);
        if (selectedDriver == null) {
            return Status.NOT_ASSIGNED;
        }
        selectedDriver.AddRequest(r, time);
        return Status.ASSIGNED;
    }

    //@Override
    //protected D GetNewDriver() {
    //	return (D)AuctionInput.GetNewDriver();
    //}

    public abstract AuctionDriver SelectWinner(AuctionRequest r, ArrayList<Bid> bids);
}
