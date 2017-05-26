package edu.usc.infolab.ridesharing.auction;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;

import java.util.ArrayList;

/**
 * Created by Mohammad on 4/19/2017.
 *
 * Implementation of the AuctionDriver class using an exhaustive search to find the best schedule.
 */
public class ESAuctionDriver extends AuctionDriver {

    public ESAuctionDriver(GPSPoint initialLoc, Time start, Time end) {
        super(initialLoc, start, end);
    }

    public ESAuctionDriver(String[] args) {
        super(args);
    }

    public ProfitCostSchedule CanService(AuctionRequest request, Time time) {
        if (this.acceptedRequests.size() + this.onBoardRequests.size() >= this.maxPassenger) {
            return ProfitCostSchedule.WorstPCS();
        }
        ProfitCostSchedule currentPCS = Utils.PRICING_MODEL.GetProfitAndCost(this, this._schedule, time, true);
        ProfitCostSchedule bestPCS = LaunchFindBestPCS(request, time);
        if (bestPCS.schedule.size() <= currentPCS.schedule.size()
                || bestPCS.profit < currentPCS.profit) {
            return ProfitCostSchedule.WorstPCS();
        }
        double extraProfit = bestPCS.profit - currentPCS.profit;
        double extraCost = bestPCS.cost - currentPCS.cost;
        this.lastPCS = new ProfitCostSchedule(extraProfit, extraCost, bestPCS.schedule);
        return lastPCS;
    }

    protected ProfitCostSchedule LaunchFindBestPCS(AuctionRequest request, Time time) {
        if (this._schedule.size() > 12) {
            return InsertRequest(request, time);
        }
        ArrayList<GPSNode> fixedNodes = new ArrayList<GPSNode>();
        ArrayList<GPSNode> remainingNodes = new ArrayList<GPSNode>();
        // Only add source node of requests that haven't been picked up to insure the source node
        // gets inserted in the schedule before the destination node
        for (AuctionRequest req : this.acceptedRequests) {
            remainingNodes.add(req.source);
        }
        // For requests that have been picked up, add destination nodes.
        for (AuctionRequest req : this.onBoardRequests) {
            remainingNodes.add(req.destination);
        }
        remainingNodes.add(request.source);
        ProfitCostSchedule bestPCS = ProfitCostSchedule.WorstPCS();
        return FindBestPCS(fixedNodes, remainingNodes, bestPCS, time);
    }

    protected ProfitCostSchedule InsertRequest(AuctionRequest request, Time time) {
        ProfitCostSchedule bestPCS = ProfitCostSchedule.WorstPCS();
        for (int i = 0; i <= this._schedule.size(); i++) {
            ArrayList<GPSNode> newSchedule1 = new ArrayList<GPSNode>(this._schedule);
            newSchedule1.add(i, request.source);
            Utils.spComputations++;
            ProfitCostSchedule tempPCS = Utils.PRICING_MODEL.GetProfitAndCost(this, newSchedule1, time, false);
            if (tempPCS.profit <= bestPCS.profit)
                continue;
            for (int j = i + 1; j <= newSchedule1.size(); j++) {
                ArrayList<GPSNode> newSchedule2 = new ArrayList<GPSNode>(newSchedule1);
                newSchedule2.add(j, request.destination);
                Utils.spComputations++;
                ProfitCostSchedule pcs = Utils.PRICING_MODEL.GetProfitAndCost(this, newSchedule2, time, false);
                if (!pcs.schedule.isEmpty() && pcs.profit > bestPCS.profit) {
                    bestPCS = pcs;
                }
            }
        }
        return bestPCS;
    }

    /**
     * find the optimal costs, branch and bound algorithms
     */
    protected ProfitCostSchedule FindBestPCS(
            ArrayList<GPSNode> fixed,
            ArrayList<GPSNode> remaining,
            ProfitCostSchedule bestPCS,
            Time time) {
        for (GPSNode n : remaining) {
            ArrayList<GPSNode> fixedCopy = new ArrayList<GPSNode>(fixed);
            fixedCopy.add(n);
            Utils.spComputations++;
            ProfitCostSchedule pcs = Utils.PRICING_MODEL.GetProfitAndCost(this, fixedCopy, time, false);

            if (pcs.profit > bestPCS.profit) {
                ArrayList<GPSNode> remainingCopy = new ArrayList<GPSNode>(remaining);
                remainingCopy.remove(n);
                if (n.type == GPSNode.Type.source) {
                    remainingCopy.add(n.request.destination);
                }
                if (remainingCopy.isEmpty()) {
                    return new ProfitCostSchedule(pcs.profit, pcs.cost, fixedCopy);
                }
                ProfitCostSchedule newPCS = FindBestPCS(fixedCopy, remainingCopy, bestPCS, time);
                if (newPCS.profit > bestPCS.profit) {
                    bestPCS = newPCS;
                }
            }
        }
        return bestPCS;
    }
}
