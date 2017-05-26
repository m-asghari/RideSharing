package edu.usc.infolab.ridesharing.auction;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.geom.GPSNode.Type;
import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.TimeDistancePair;
import edu.usc.infolab.ridesharing.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class ShortestPathDriver extends ESAuctionDriver {

  /**
   * @param initialLoc
   * @param start
   * @param end
   */
  public ShortestPathDriver(GPSPoint initialLoc, Time start, Time end) {
    super(initialLoc,start,end);
    // TODO(masghari): Auto-generated constructor stub
  }
  
  @Override
  public Bid ComputeBid(AuctionRequest request, Time time) {
    if (this.acceptedRequests.size() + this.onBoardRequests.size() >= this.maxPassenger)
      return Bid.WorstBid();
    ProfitCostSchedule currentPCS = GetScheduleCost(this._schedule, time, true);
    ProfitCostSchedule bestPCS = LaunchFindBestPCS(request, time);
    if (bestPCS.schedule.size() < this._schedule.size()) {
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
  
  @Override
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
      ProfitCostSchedule pcs = GetScheduleCost(fixedCopy, time, false);

      if (pcs.cost < bestPCS.cost) {
        ArrayList<GPSNode> remainingCopy = new ArrayList<GPSNode>(remaining);
        remainingCopy.remove(n);
        if (n.type == Type.source) {
          remainingCopy.add(n.request.destination);
        }
        if (remainingCopy.isEmpty()) {
          return new ProfitCostSchedule(pcs.profit, pcs.cost, fixedCopy);
        }
        ProfitCostSchedule newPCS = FindBestPCS(fixedCopy, remainingCopy, bestPCS, time);
        if (newPCS.cost < bestPCS.cost) {
          bestPCS = newPCS;
        }
      }
    }
    return bestPCS;
  }
  
  /**
   * get the profit of current schedule
   *
   * @param schedule
   * @param start
   * @param currentSchedule - true if the method is being called on current schedule, otherwise 
   * false
   * @return the profit and cost of input schedule
   */
  protected ProfitCostSchedule GetScheduleCost(
          ArrayList<GPSNode> schedule, Time start, boolean currentSchedule) {
    //double fare = this.collectedFare;
    //double cost = this.GetCost(_paidTravelledDistance, 0.);
    Time time = start.clone();
    GPSPoint loc = this.loc;
    double dist = travelledDistance;
    if (schedule.isEmpty()) {
      return new ProfitCostSchedule(-1, dist - this.travelledDistance, schedule);
    }
    HashMap<AuctionRequest, Time> pickUpTimes = new HashMap<AuctionRequest, Time>();
    HashMap<AuctionRequest, Double> pickUpDist = new HashMap<AuctionRequest, Double>();

    //Pair<Double, Double> initTrip = loc.DistanceInMilesAndMillis(schedule.get(0).point);
    //if (this.onBoardRequests.size() > 0) {
    //  initTrip = new Pair<Double, Double>(0., 0.);
    //}
    for (GPSNode n : schedule) {
      TimeDistancePair trip = loc.DistanceInMilesAndMillis(n.point);
      time.AddMillis(trip.time.intValue());
      dist += trip.distance;
      loc = n.point;
      AuctionRequest request = (AuctionRequest) n.request;
      if (n.type == Type.source) {
        if (!currentSchedule && time.compareTo(request.latestPickUpTime) > 0) {
          return ProfitCostSchedule.WorstPCS();
        }
        pickUpTimes.put(request, time.clone());
        pickUpDist.put(request, dist);
      }
      if (n.type == Type.destination) {
        @SuppressWarnings("unused")
        int tripTime =
            time.SubtractInMinutes(
                (pickUpTimes.get(request) != null) ? pickUpTimes.get(request) : request.pickUpTime);
        double tripDist =
            dist
                - ((pickUpDist.get(request) != null)
                    ? pickUpDist.get(request)
                    : request.pickUpDistance);
        double detour = tripDist - request.optDistance;
        if (!currentSchedule && !Utils.IsAcceptableDetour(detour, request.optDistance))
          return ProfitCostSchedule.WorstPCS();
      }
    }
    double travelled = dist - this.travelledDistance;
    return new ProfitCostSchedule(-1, travelled, schedule);
  }
  
  @Override
  public void AddRequest(AuctionRequest request, Time time) {
    ProfitCostSchedule beforPCS = Utils.PRICING_MODEL.GetProfitAndCost(this, _schedule, time, true);
    super.AddRequest(request,time);
    ProfitCostSchedule afterPCS = Utils.PRICING_MODEL.GetProfitAndCost(this, _schedule, time, true);
    request.serverProfit = (afterPCS.profit - beforPCS.profit);    
  }
}

