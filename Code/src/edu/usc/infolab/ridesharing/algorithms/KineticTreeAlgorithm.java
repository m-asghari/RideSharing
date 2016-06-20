package edu.usc.infolab.ridesharing.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.ridesharing.Status;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.Utils;
import edu.usc.infolab.ridesharing.auction.AuctionDriver;
import edu.usc.infolab.ridesharing.auction.AuctionRequest;
import edu.usc.infolab.ridesharing.auction.Bid;
import edu.usc.infolab.ridesharing.kinetictree.KTDriver;
import edu.usc.infolab.ridesharing.kinetictree.KTRequest;

public class KineticTreeAlgorithm extends Algorithm<KTRequest, KTDriver> {

	public KineticTreeAlgorithm(Time startTime, int ati) {
		super(startTime, ati);
	}

	@Override
	public Status ProcessRequest(KTRequest r, Time time) {
		ArrayList<KTDriver> potentialDrivers = GetPotentialDrivers(r);
		r.stats.potentialDrivers = potentialDrivers.size();		
		
		HashMap<KTDriver, Double> insertCosts = new HashMap<KTDriver, Double>();
		Time start = new Time();
		for (KTDriver d : potentialDrivers) {
			insertCosts.put(d, d.InsertRequest(r));
		}
		
		KTDriver selectedDriver = null;
		double minCost = Utils.Max_Double;
		for (Entry<KTDriver, Double> e : insertCosts.entrySet()) {
			if (e.getValue() != null && e.getValue() < minCost) {
				minCost = e.getValue();
				selectedDriver = e.getKey();
			}
		}
		if (selectedDriver == null) {
			return Status.NOT_ASSIGNED;
		}
		selectedDriver.AddRequest(r, time);
		Time end = new Time();
		r.stats.schedulingTime = end.SubtractInMillis(start);
		
		AuctionDriver mostProfitable = null;
        double maxProfit = Utils.Min_Double;
        double ktProfit = 0;
        for (KTDriver d : potentialDrivers) {
            AuctionDriver auctionDriver = GetAuctionDriver(d);
            Bid bid = auctionDriver.ComputeBid(GetAuctionRequest(r), time);
            if (bid.value > 0 && bid.value > maxProfit) {
                maxProfit = bid.value;
                mostProfitable = bid.driver;
            }
            if (d.id == selectedDriver.id) {
              ktProfit = bid.value;
            }
        }
		if (mostProfitable != null) {
		  if (mostProfitable.id == selectedDriver.id) {
		    r.stats.mostProfitable = 1;
		  }
		  if (ktProfit < 0) {
		    r.stats.looseMoney = 1;
		  }
		  if (ktProfit > 0) {
		    r.stats.profitDiff = maxProfit - ktProfit;
		  }
		}
		return Status.ASSIGNED;
	}
	
	private AuctionDriver GetAuctionDriver(KTDriver driver) {
		AuctionDriver auctionDriver = new AuctionDriver(driver.loc, driver.start, driver.end);
		auctionDriver._schedule = new ArrayList<GPSNode>();
		for (GPSNode node : driver._schedule) {
			GPSNode newNode = node.clone();
			newNode.request = GetAuctionRequest((KTRequest)node.request);
			auctionDriver._schedule.add(newNode);
		}
		auctionDriver.acceptedRequests = GetAuctionRequests(driver.acceptedRequests);
		auctionDriver.collectedFare = driver.collectedFare;
		auctionDriver.id = driver.id;
		auctionDriver.income = driver.income;
		auctionDriver.onBoardRequests = GetAuctionRequests(driver.onBoardRequests);
		auctionDriver.servicedRequests = GetAuctionRequests(driver.servicedRequests);
		auctionDriver.travelledDistance = driver.travelledDistance;
		auctionDriver._getPaid = driver._getPaid;
		auctionDriver._paidTravelledDistance = driver._paidTravelledDistance;
		return auctionDriver;
	}
	
	private ArrayList<AuctionRequest> GetAuctionRequests(ArrayList<KTRequest> requests) {
		ArrayList<AuctionRequest> auctionRequests = new ArrayList<AuctionRequest>();
		for (KTRequest request : requests) {
			auctionRequests.add(GetAuctionRequest(request));
		}
		
		return auctionRequests;
	}
	
	private AuctionRequest GetAuctionRequest(KTRequest request) {
		AuctionRequest auctionRequest = new AuctionRequest(request.source.point, request.destination.point, request.requestTime.clone(), request.maxWaitTime);
		auctionRequest.id = request.id;
		auctionRequest.actualDistance = request.actualDistance;
		auctionRequest.actualTime = request.actualTime;
		auctionRequest.defaultFare = request.defaultFare;
		auctionRequest.detour = request.detour;
		auctionRequest.dropOffDistance = request.dropOffDistance;
		auctionRequest.dropOffTime = request.dropOffTime.clone();
		auctionRequest.finalFare = request.finalFare;
		auctionRequest.latestPickUpTime = request.latestPickUpTime.clone();
		auctionRequest.pickUpDistance = request.pickUpDistance;
		auctionRequest.pickUpTime = request.pickUpTime.clone();
		auctionRequest.stats = request.stats.clone();
		return auctionRequest;
	}

	//@Override
	//protected KTDriver GetNewDriver() {
	//	return KTInput.GetNewDriver();
	//}
	
	@Override
	public String GetName() {
		return "KTA";
	}

}
