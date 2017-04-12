package edu.usc.infolab.ridesharing.auction;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.geom.GPSNode.Type;
import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;

import java.util.ArrayList;

public class AuctionDriver extends Driver<AuctionRequest> {
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
      ProfitCostSchedule tempPCS = Utils.PRICING_MODEL.GetProfitAndCost(this, newSchedule1, time, false);
      if (tempPCS.profit <= bestPCS.profit)
    	  continue;
      for (int j = i + 1; j <= newSchedule1.size(); j++) {
        ArrayList<GPSNode> newSchedule2 = new ArrayList<GPSNode>(newSchedule1);
        newSchedule2.add(j, request.destination);
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
      ProfitCostSchedule pcs = Utils.PRICING_MODEL.GetProfitAndCost(this, fixedCopy, time, false);

      if (pcs.profit > bestPCS.profit) {
        ArrayList<GPSNode> remainingCopy = new ArrayList<GPSNode>(remaining);
        remainingCopy.remove(n);
        if (n.type == Type.source) {
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

  @Override
  protected ArrayList<AuctionRequest> NewPointUpdates(Time time) {
    ArrayList<AuctionRequest> finishedRequests = super.NewPointUpdates(time);
    return finishedRequests;
  }

  @Override
  protected double GetIncome(AuctionRequest request, Time time) {
    return this.income + (request.finalFare - request.serverProfit);
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
