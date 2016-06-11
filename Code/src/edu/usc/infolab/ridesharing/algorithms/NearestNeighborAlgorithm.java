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
		ArrayList<AuctionDriver> sortedDrivers = SortPotentialDrivers(request, potentialDrivers);
		for (AuctionDriver driver : sortedDrivers) {
			ProfitCostSchedule bestPCS = driver.CanService(request, time);
			if (bestPCS.profit > 0) {
				driver.AddRequest(request, time);
				request.serverProfit = bestPCS.profit;
				this.profit += bestPCS.profit;
				Time end = new Time();
				request.stats.assignmentTime = end.SubtractInMillis(start);
				return Status.ASSIGNED;
			}
		}
		return Status.NOT_ASSIGNED;		
	}

	private ArrayList<AuctionDriver> SortPotentialDrivers(
			AuctionRequest request,	ArrayList<AuctionDriver> potentialDrivers) {
		ArrayList<AuctionDriver> sortedDrivers = new ArrayList<AuctionDriver>();
		while (!potentialDrivers.isEmpty()) {
			AuctionDriver nearestDriver = null;
			double minDistance = Utils.Max_Double;
			for (AuctionDriver driver : potentialDrivers) {
				double distance = driver.loc.DistanceInMilesAndMillis(request.source.point).First;
				if (distance < minDistance) {
					nearestDriver = driver;
					minDistance = distance;
				}
			}
			sortedDrivers.add(nearestDriver);
			potentialDrivers.remove(nearestDriver);
		}
		return sortedDrivers;
	}

}
