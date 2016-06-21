package edu.usc.infolab.ridesharing.auction;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.geom.GPSNode.Type;
import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Pair;
import edu.usc.infolab.ridesharing.Time;

import java.util.ArrayList;
import java.util.HashMap;

public class TShareModelAuctionDriver extends AuctionDriver {
  private static final double INFLATE_PARAM = 0.5;

  /**
   * @param initialLoc
   * @param start
   * @param end
   */
  public TShareModelAuctionDriver(GPSPoint initialLoc, Time start, Time end) {
    super(initialLoc,start,end);
  }
  
  @Override
  public ProfitCostSchedule GetProfitAndCost(
      ArrayList<GPSNode> schedule, Time start, boolean currentSchedule) {
    double fare = this.collectedFare;
    double cost = this.income;
    Time time = start.clone();
    GPSPoint loc = this.loc;
    double dist = travelledDistance;
    if (schedule.isEmpty()) {
      return new ProfitCostSchedule(fare - cost, cost, schedule);
    }
    HashMap<AuctionRequest, Time> pickUpTimes = new HashMap<AuctionRequest, Time>();
    HashMap<AuctionRequest, Double> pickUpDist = new HashMap<AuctionRequest, Double>();

    Pair<Double, Double> initTrip = loc.DistanceInMilesAndMillis(schedule.get(0).point);
    if (this.onBoardRequests.size() > 0) {
      initTrip = new Pair<Double, Double>(0., 0.);
    }
    cost += GetCurrentCostPerLength(initTrip.First);
    for (GPSNode n : schedule) {
      Pair<Double, Double> trip = loc.DistanceInMilesAndMillis(n.point);
      time.AddMillis(trip.Second.intValue());
      dist += trip.First;
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
        fare += request.profile(detour) * request.defaultFare;
        cost =
            this.GetCost(
                _paidTravelledDistance + (dist - (travelledDistance + initTrip.First)),
                time.SubtractInMillis(start) - initTrip.Second);
      }
    }
    double profit = fare - cost;
    return new ProfitCostSchedule(profit, cost, schedule);
  }
  
  @Override
  protected void UpdateTravelledDistance(double length) {
    super.UpdateTravelledDistance(length);
    this.income += GetCurrentCostPerLength(length);
  }
  
  private double GetCurrentCostPerLength(double length) {
    return GetCurrentCostPerLength(length, this.onBoardRequests.size());
  }
  
  private double GetCurrentCostPerLength(double length, int onBoardPassengers) {
    if (onBoardPassengers == 0) {
      return 0;
    } else if (onBoardPassengers == 1) {
      return INCOME_PER_MILE * length;
    } else {
      return (1 + INFLATE_PARAM) * INCOME_PER_MILE * length;
    }
  }
  
}

