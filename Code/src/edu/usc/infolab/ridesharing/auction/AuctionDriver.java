package edu.usc.infolab.ridesharing.auction;

import java.util.ArrayList;
import java.util.HashMap;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.geom.GPSNode.Type;
import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Pair;
import edu.usc.infolab.ridesharing.Time;

public class AuctionDriver extends Driver<AuctionRequest> {
	private ProfitCostSchedule lastPCS;
	
	public AuctionDriver(GPSPoint initialLoc, Time start, Time end) {
		super(initialLoc, start, end);
		lastPCS = null;
	}
	
	public AuctionDriver(String[] args) {
		super(args);
	}

	public Bid ComputeBid(AuctionRequest request, Time time) {
		if (this.acceptedRequests.size() + this.onBoardRequests.size() >= this.maxPassenger)
			return Bid.WorstBid();
		ProfitCostSchedule currentPCS = GetProfitAndCost(this._schedule, time);
		ProfitCostSchedule bestPCS = LaunchFindBestPCS(request, time);
		if (bestPCS.profit < currentPCS.profit) {
			return Bid.WorstBid();
		}
		double extraProfit = bestPCS.profit - currentPCS.profit;
		double extraCost = bestPCS.cost - currentPCS.cost;
		Bid bid = new Bid(this, bestPCS.schedule, extraProfit, extraCost);
		bid.distToPickup = this.loc.Distance(request.source.point).First;
		request.stats.acceptableBids++;
		this.lastPCS = new ProfitCostSchedule(extraProfit, extraCost, bestPCS.schedule);
		return bid;
	}
	
	public ProfitCostSchedule CanService(AuctionRequest request, Time time) {
		if (this.acceptedRequests.size() + this.onBoardRequests.size() >= this.maxPassenger) {
			return ProfitCostSchedule.WorstPCS();
		}
		ProfitCostSchedule currentPCS = GetProfitAndCost(this._schedule, time);
		ProfitCostSchedule bestPCS = LaunchFindBestPCS(request, time);
		if (bestPCS.schedule.size() <= currentPCS.schedule.size() || bestPCS.profit < currentPCS.profit) {
			return ProfitCostSchedule.WorstPCS();
		}
		CheckSchedule(bestPCS.schedule);
		double extraProfit = bestPCS.profit - currentPCS.profit;
		double extraCost = bestPCS.cost - currentPCS.cost;
		this.lastPCS = new ProfitCostSchedule(extraProfit, extraCost, bestPCS.schedule); 
		return lastPCS;
	}
	
	private void CheckSchedule(ArrayList<GPSNode> newSchedule) {
		for (GPSNode node : this._schedule) {
			if (!newSchedule.contains(node)) {
				System.out.println("Something's Wrong");
			}
		}
	}
	
	private ProfitCostSchedule LaunchFindBestPCS(AuctionRequest request, Time time) {
		ArrayList<GPSNode> fixedNodes = new ArrayList<GPSNode>();
		ArrayList<GPSNode> remainingNodes = new ArrayList<GPSNode>();
		// Only add source node of requests that haven't been picked up to insure the source node
		// gets inserted in the schedule before the destination node
		for (AuctionRequest req : this.acceptedRequests) {
			remainingNodes.add(req.source);
		}
		// For requests that have been picked up, add destination nodes.
		for (AuctionRequest req : this.onBoardRequests) {
			remainingNodes.add(req.destination);
		}
		remainingNodes.add(request.source);
		ProfitCostSchedule bestPCS = ProfitCostSchedule.WorstPCS();
		return FindBestPCS(fixedNodes, remainingNodes, bestPCS, time);
	}

	/*
	 * 
	 */
	private ProfitCostSchedule FindBestPCS(ArrayList<GPSNode> fixed, ArrayList<GPSNode> remaining, ProfitCostSchedule bestPCS, Time time) {
		for (GPSNode n : remaining) {
			ArrayList<GPSNode> fixedCopy = new ArrayList<GPSNode>(fixed);
			fixedCopy.add(n);
			ProfitCostSchedule pcs = GetProfitAndCost(fixedCopy, time);
			
			if (pcs.profit > bestPCS.profit) {
				ArrayList<GPSNode> remainingCopy = new ArrayList<GPSNode>(remaining);
				remainingCopy.remove(n);
				if (n.type == Type.source) {
					remainingCopy.add((GPSNode)n.request.destination);
				}
				if (remainingCopy.isEmpty()) {
					return new ProfitCostSchedule(pcs.profit, pcs.cost, fixedCopy);
				}
				ProfitCostSchedule newPCS = FindBestPCS(fixedCopy, remainingCopy, bestPCS, time);
				if (newPCS.profit > bestPCS.profit) {
					bestPCS = newPCS;
				}
			}
		}
		return bestPCS;
	}

	private ProfitCostSchedule GetProfitAndCost(ArrayList<GPSNode> schedule, Time start) {
		Double fare = this.collectedFare;
		Double cost = this.GetCost(_paidTravelledDistance, 0.);
		Time time = start.clone();
		GPSPoint loc = this.loc;
		Double dist = travelledDistance;
		if (schedule.isEmpty()) {
			return new ProfitCostSchedule(fare - cost, cost, schedule);
		}
		HashMap<AuctionRequest, Time> pickUpTimes = new HashMap<AuctionRequest, Time>();
		HashMap<AuctionRequest, Double> pickUpDist = new HashMap<AuctionRequest, Double>();
		
		Pair<Double, Double> initTrip = loc.Distance(schedule.get(0).point);
		if (this.onBoardRequests.size() > 0) {
			initTrip = new Pair<Double, Double>(0., 0.);
		}
		for (GPSNode n : schedule) {
			Pair<Double, Double> trip = loc.Distance(n.point);
			time.Add(trip.Second.intValue());
			dist += trip.First;
			loc = n.point;
			AuctionRequest request = (AuctionRequest)n.request;
			if (n.type == Type.source) {
				if (time.compareTo(request.latestPickUpTime) > 0) {
					return ProfitCostSchedule.WorstPCS();
				}
				pickUpTimes.put(request, time.clone());
				pickUpDist.put(request, dist);
			}
			if (n.type == Type.destination) {
				@SuppressWarnings("unused")
				int tripTime = time.Subtract(
						(pickUpTimes.get(request) != null) ? pickUpTimes.get(request) : request.pickUpTime);
				Double tripDist = dist - 
						((pickUpDist.get(request) != null) ? pickUpDist.get(request) : request.pickUpDistance);
				Double detour = tripDist - request.optDistance;
				fare += request.profile(detour) * request.defaultFare;
				cost = this.GetCost(_paidTravelledDistance + (dist - (travelledDistance + initTrip.First)), 
						(double)time.Subtract(start) - initTrip.Second); 
			}
		}
		Double profit = fare - cost;
		return new ProfitCostSchedule(profit, cost, schedule);
	}
	
	@Override
	protected double GetIncome(AuctionRequest request) {
		return this.income + (request.finalFare - request.serverProfit);
	}

	@Override
	public void AddRequest(AuctionRequest request) {
		this._schedule = new ArrayList<GPSNode>(lastPCS.schedule);
		this.acceptedRequests.add(request);
	}
}

