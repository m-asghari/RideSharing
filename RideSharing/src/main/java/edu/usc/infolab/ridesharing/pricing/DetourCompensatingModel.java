package edu.usc.infolab.ridesharing.pricing;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.geom.GPSNode.Type;
import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.*;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;
import edu.usc.infolab.ridesharing.auction.ProfitCostSchedule;

import java.util.ArrayList;
import java.util.HashMap;

public class DetourCompensatingModel extends PricingModel {
private static DetourCompensatingModel _defaultInstance;
  
  private DetourCompensatingModel() {}
  
  public static DetourCompensatingModel getInstance() {
    if (_defaultInstance == null) {
      _defaultInstance = new DetourCompensatingModel();
    }
    return _defaultInstance;
  }

  @Override
  public <R extends Request, D extends Driver<R>> ProfitCostSchedule GetProfitAndCost(D driver,
                                                                                      ArrayList<GPSNode> schedule, Time start, boolean currentSchedule) {
    double fare = driver.collectedFare;
    double cost = driver.GetCost(driver._paidTravelledDistance, 0.);
    Time time = start.clone();
    GPSPoint loc = driver.loc;
    double dist = driver.travelledDistance;
    if (schedule.isEmpty()) {
      return new ProfitCostSchedule(fare - cost, cost, schedule);
    }
    HashMap<AuctionRequest, Time> pickUpTimes = new HashMap<AuctionRequest, Time>();
    HashMap<AuctionRequest, Double> pickUpDist = new HashMap<AuctionRequest, Double>();

    TimeDistancePair initTrip = loc.DistanceInMilesAndMillis(schedule.get(0).point);
    if (driver.onBoardRequests.size() > 0) {
      initTrip = new TimeDistancePair(0., 0.);
    }
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
        fare += ComputeFinalFare(request, detour);
        cost = ComputeDriverIncome(
            driver,
            -1 /* On Board Passenger is not important in this model */,
            driver._paidTravelledDistance + (dist - (driver.travelledDistance + initTrip.distance)),
            time.SubtractInMillis(start) - initTrip.time);
      }
    }
    double profit = fare - cost;
    return new ProfitCostSchedule(profit, cost, schedule);
  }
  
  @Override
  public double ComputeFinalFare(Request request, double detour) {
    return request.profile(detour) * request.defaultFare;
  }

  @Override
  public <R extends Request, D extends Driver<R>> double ComputeDriverIncome(
      D driver, int onBoardPassengers, double distance, double time) {
    return driver.GetCost(distance, time);
  }

  @Override
  public double DefaultFare(double distance, int time) {
    return 2 * distance;
  }
}

