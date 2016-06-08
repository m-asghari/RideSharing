package edu.usc.infolab.ridesharing.algorithms;

import java.util.ArrayList;

import edu.usc.infolab.ridesharing.Status;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;
import edu.usc.infolab.ridesharing.auction.AuctionDriver;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;
import edu.usc.infolab.ridesharing.auction.ProfitCostSchedule;
import edu.usc.infolab.ridesharing.datasets.real.nyctaxi.AuctionInput;

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

	@Override
	protected AuctionDriver GetNewDriver() {
		return AuctionInput.GetNewDriver();
	}

	@Override
	protected String GetName() {
		return "NN";
	}

	@Override
	public Status ProcessRequest(AuctionRequest request, Time time) {
		ArrayList<AuctionDriver> potentialDrivers = GetPotentialDrivers(request);
		request.stats.potentialDrivers = potentialDrivers.size();
		
		Time start = new Time();
		AuctionDriver nearestDriver = null;
		double minDistance = Utils.Max_Double;
		for (AuctionDriver driver : potentialDrivers) {
			double distance = driver.loc.Distance(request.source.point).First;
			if (distance < minDistance) {
				minDistance = distance;
				nearestDriver = driver;
			}
		}
		Time end = new Time();
		request.stats.assignmentTime = end.SubtractMillis(start);
		
		start = new Time();
		if (nearestDriver != null) {
			ProfitCostSchedule bestPCS = nearestDriver.CanService(request, time);
			if (bestPCS.profit > ProfitCostSchedule.WorstPCS().profit) {
				nearestDriver.AddRequest(request);
				request.serverProfit = bestPCS.profit;
				this.profit += request.serverProfit;
				end = new Time();
				request.stats.assignmentTime = end.SubtractMillis(start);
				return Status.ASSIGNED;
			}
		}
		return Status.NOT_ASSIGNED;		
	}

}
