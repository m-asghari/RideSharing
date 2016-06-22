package edu.usc.infolab.ridesharing.pricing;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.geom.GPSNode.Type;
import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Pair;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;
import edu.usc.infolab.ridesharing.auction.ProfitCostSchedule;

import java.util.ArrayList;
import java.util.HashMap;

public class PerDistanceModel extends PricingModel {
  private static PerDistanceModel _defaultInstance;
  
  private PerDistanceModel() {}
  
  public static PerDistanceModel getInstance() {
    if (_defaultInstance == null) {
      _defaultInstance = new PerDistanceModel();
    }
    return _defaultInstance;
  }

  @Override
  public <R extends Request, D extends Driver<R>> ProfitCostSchedule GetProfitAndCost(
      D driver, ArrayList<GPSNode> schedule, Time start, boolean currentSchedule) {
    double collectedFare = driver.perDistanceIncome;
    Time time = start.clone();
    GPSPoint loc = driver.loc;
    double dist = driver.travelledDistance;
    if (schedule.isEmpty()) {
      return new ProfitCostSchedule(0.2 * collectedFare, 0.8 * collectedFare, schedule);
    }
    HashMap<AuctionRequest, Time> pickUpTimes = new HashMap<AuctionRequest, Time>();
    HashMap<AuctionRequest, Double> pickUpDist = new HashMap<AuctionRequest, Double>();

    Pair<Double, Double> initTrip = loc.DistanceInMilesAndMillis(schedule.get(0).point);
    if (driver.onBoardRequests.size() > 0) {
      initTrip = new Pair<Double, Double>(0., 0.);
    }
    int onBoardPassengers = driver.onBoardRequests.size();
    for (GPSNode n : schedule) {
      Pair<Double, Double> trip = loc.DistanceInMilesAndMillis(n.point);
      time.AddMillis(trip.Second.intValue());
      dist += trip.First;
      collectedFare += ComputeDriverIncome(driver, onBoardPassengers, dist, trip.Second);
      loc = n.point;
      AuctionRequest request = (AuctionRequest) n.request;
      if (n.type == Type.source) {
        if (!currentSchedule && time.compareTo(request.latestPickUpTime) > 0) {
          return ProfitCostSchedule.WorstPCS();
        }
        pickUpTimes.put(request, time.clone());
        pickUpDist.put(request, dist);
        onBoardPassengers++;
      }
      if (n.type == Type.destination) {
        double tripDist =
            dist
                - ((pickUpDist.get(request) != null)
                    ? pickUpDist.get(request)
                    : request.pickUpDistance);
        double detour = tripDist - request.optDistance;
        if (!currentSchedule && !Utils.IsAcceptableDetour(detour, request.optDistance))
          return ProfitCostSchedule.WorstPCS();
        onBoardPassengers--;
      }
    }
    double profit = 0.2 * collectedFare;
    double cost = 0.8 * collectedFare;
    return new ProfitCostSchedule(profit, cost, schedule);
  }

  @Override
  public double ComputeFinalFare(Request request, double detour) {
    return request.perDistanceFare;
  }

  @Override
  public <R extends Request, D extends Driver<R>> double ComputeDriverIncome(
      D driver, int onBoardPassengers, double distance, double time) {
    double base = 1.25 * driver.GetCost(distance, time);
    if (onBoardPassengers > 1) {
      return 1. * base;
    }
    return base;
  }

  @Override
  public double DefaultFare(double distance, int time) {
    return 1.25 * distance;
  }
}

