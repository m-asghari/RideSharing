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
  public double profit;

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
    Status retStatus = Status.NOT_ASSIGNED;
    ArrayList<AuctionDriver> potentialDrivers = GetPotentialDrivers(request);
    request.stats.potentialDrivers = potentialDrivers.size();
    
    Time start = new Time();
    AuctionDriver nearestDriver = null;
    double nnProfit = 0;
    while (!potentialDrivers.isEmpty()) {    	
    	nearestDriver = null;
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
    		nnProfit = bestPCS.profit;
    		Time end = new Time();
    		request.stats.assignmentTime = end.SubtractInMillis(start);
    		retStatus = Status.ASSIGNED;
    		break;
    	}
    	potentialDrivers.remove(nearestDriver);
    }
    
    if (nearestDriver != null) {
      AuctionDriver mostProfitable = null;
      double maxProfit = Utils.Min_Double;
      for (AuctionDriver driver : potentialDrivers) {
          Bid bid = driver.ComputeBid(request, time);
          if (driver.id == nearestDriver.id) {
            continue;
          }
          if (bid.value > 0 && bid.value > maxProfit) {
              maxProfit = bid.value;
              mostProfitable = bid.driver;
          }        
      }
      if (mostProfitable != null) {
        if (mostProfitable.id == nearestDriver.id) {
          request.stats.mostProfitable = 1;
        }
        if (nnProfit < 0) {
          request.stats.looseMoney = 1;
        }
        if (nnProfit > 0) {
          request.stats.profitDiff = maxProfit - nnProfit;
        }
        
      }
      
    }
    
    return retStatus;
  }
}
