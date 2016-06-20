package edu.usc.infolab.ridesharing.algorithms;

import java.util.ArrayList;

import edu.usc.infolab.ridesharing.Status;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;
import edu.usc.infolab.ridesharing.auction.AuctionDriver;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;
import edu.usc.infolab.ridesharing.auction.Bid;
import edu.usc.infolab.ridesharing.auction.ProfitCostSchedule;

public class NearestNeighborAlgorithm extends Algorithm<AuctionRequest, AuctionDriver> {
  public Double profit;

  // time should be in minutes
  public static Double FARE(Double distance, int time) {
    return 2. * distance;
  }

  public NearestNeighborAlgorithm(Time startTime, int ati) {
    super(startTime, ati);
    profit = 0.;
  }

  //@Override
  //protected AuctionDriver GetNewDriver() {
  //  return AuctionInput.GetNewDriver();
  //}

  @Override
  public String GetName() {
    return "NN";
  }

  @Override
  public Status ProcessRequest(AuctionRequest request, Time time) {
    ArrayList<AuctionDriver> potentialDrivers = GetPotentialDrivers(request);
    request.stats.potentialDrivers = potentialDrivers.size();

    AuctionDriver mostProfitable = null;
    double maxProfit = Utils.Min_Double;
    for (AuctionDriver driver : potentialDrivers) {
    	Bid bid = driver.ComputeBid(request, time);
    	if (bid.value > 0 && bid.value > maxProfit) {
    		maxProfit = bid.value;
    		mostProfitable = bid.driver;
    	}
    }
    
    Time start = new Time();
    while (!potentialDrivers.isEmpty()) {    	
    	AuctionDriver nearestDriver = null;
    	double minDistance = Utils.Max_Double;
    	for (AuctionDriver driver : potentialDrivers) {
    		double distance = driver.loc.DistanceInMilesAndMillis(request.source.point).First;
    		if (distance < minDistance) {
    			minDistance = distance;
    			nearestDriver = driver;
    		}
    	}
    	if (nearestDriver == null) return Status.NOT_ASSIGNED;
    	ProfitCostSchedule bestPCS = nearestDriver.CanService(request, time);
    	if (bestPCS.profit > 0) {
    		nearestDriver.AddRequest(request, time);
    		request.serverProfit = bestPCS.profit;
    		this.profit += bestPCS.profit;
    		Time end = new Time();
    		request.stats.assignmentTime = end.SubtractInMillis(start);
    		if (mostProfitable != null && mostProfitable.id == nearestDriver.id) {
    			request.stats.mostProfitable = 1;
    		}
    		return Status.ASSIGNED;
    	}
    	potentialDrivers.remove(nearestDriver);
    }
    return Status.NOT_ASSIGNED;
  }
}
