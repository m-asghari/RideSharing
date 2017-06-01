package edu.usc.infolab.ridesharing.algorithms;

import edu.usc.infolab.ridesharing.Status;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;
import edu.usc.infolab.ridesharing.auction.*;

import java.util.ArrayList;

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
        ESAuctionDriver nearestDriver = null;
        double nnProfit = 0;
        while (!potentialDrivers.isEmpty()) {
            Utils.spComputations = 0;
            nearestDriver = null;
            double minDistance = Utils.Max_Double;
            for (AuctionDriver driver : potentialDrivers) {
                double distance = driver.loc.DistanceInMilesAndMillis(request.source.point).distance;
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestDriver = (ESAuctionDriver) driver;
                }
            }
            if (nearestDriver == null) return Status.NOT_ASSIGNED;
            ProfitCostSchedule bestPCS = nearestDriver.CanService(request, time);
            request.stats.spComputations.add(Utils.spComputations);
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


        /*
         * The following code is used to compare the profit if we used an auction driver.
	     *
        if (nearestDriver != null) {
            AuctionDriver mostProfitable = null;
            double maxProfit = Utils.Min_Double;
            for (AuctionDriver driver : potentialDrivers) {
                Bid bid = driver.ComputeBid(request, time);
                if (driver.id == nearestDriver.id) {
                    continue;
                }
                if (bid.profit > 0 && bid.profit > maxProfit) {
                    maxProfit = bid.profit;
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
        }*/

        return retStatus;
    }
}
