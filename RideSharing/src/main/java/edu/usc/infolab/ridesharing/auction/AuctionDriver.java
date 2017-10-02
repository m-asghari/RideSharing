package edu.usc.infolab.ridesharing.auction;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.geom.GPSNode.Type;
import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;

import java.util.ArrayList;

public abstract class AuctionDriver extends Driver<AuctionRequest> {
    protected ProfitCostSchedule lastPCS;

    public AuctionDriver(GPSPoint initialLoc, Time start, Time end) {
        super(initialLoc, start, end);
        lastPCS = null;
    }

    public AuctionDriver(String[] args) {
        super(args);
    }

    public Bid ComputeBid(AuctionRequest request, Time time) {
        if (this.acceptedRequests.size() + this.onBoardRequests.size() >= this.maxPassenger)
            return Bid.WorstBid();
        ProfitCostSchedule currentPCS = Utils.PRICING_MODEL.GetProfitAndCost(this, this._schedule, time, true);
        ProfitCostSchedule bestPCS = LaunchFindBestPCS(request, time);
        if (bestPCS.profit < currentPCS.profit) {
            return Bid.WorstBid();
        }
        double extraProfit = bestPCS.profit - currentPCS.profit;
        double extraCost = bestPCS.cost - currentPCS.cost;
        Bid bid = new Bid(this, bestPCS.schedule, extraProfit, extraCost);
        bid.distToPickup = this.loc.DistanceInMilesAndMillis(request.source.point).distance;
        request.stats.acceptableBids++;
        this.lastPCS = new ProfitCostSchedule(extraProfit, extraCost, bestPCS.schedule);
        return bid;
    }

    protected abstract ProfitCostSchedule LaunchFindBestPCS(AuctionRequest request, Time time);

    @Override
    protected ArrayList<AuctionRequest> NewPointUpdates(Time time) {
        ArrayList<AuctionRequest> finishedRequests = super.NewPointUpdates(time);
        return finishedRequests;
    }

    @Override
    protected double GetIncome(AuctionRequest request, Time time) {
        if (request.finalFare - request.serverProfit < 0) {
            System.out.println("Something wrong.");
        }
        return this.income + (request.finalFare - request.fpaProfit);
    }

    @Override
    public void AddRequest(AuctionRequest request, Time time) {
        Check(time);
        this._schedule = new ArrayList<GPSNode>(lastPCS.schedule);
        Check(time);
        this.acceptedRequests.add(request);
    }

    @Override
    public void Check(Time time) {
    }
}
