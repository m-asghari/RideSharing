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
	public Double collectedFare;
	public Double income;
	
	protected boolean _getPaid;
	protected double _paidTravelledDistance;
	
	private Bid lastBid;
	
	public AuctionDriver(GPSPoint initialLoc, Time start, Time end) {
		super(initialLoc, start, end);
		_getPaid = false;
		_paidTravelledDistance = 0;
		collectedFare = 0.;
		income = 0.;
		lastBid = null;
	}
	
	public Bid ComputeBid(AuctionRequest r, Time time) {
		if (this.acceptedRequests.size() + this.onBoardRequests.size() >= this.maxPassenger)
			return Bid.WorstBid();
		ProfitCostSchedule currentPCS = GetProfitAndCost(this._schedule, time);
		if (currentPCS.profit < 0) {
			GetProfitAndCost(this._schedule, time);
		}
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
		remainingNodes.add(r.source);
		ProfitCostSchedule bestPCS = ProfitCostSchedule.WorstPCS();
		bestPCS = FindBestPCS(fixedNodes, remainingNodes, bestPCS, time);
		if (bestPCS.profit < currentPCS.profit) {
			return Bid.WorstBid();
		}
		double extraProfit = bestPCS.profit - currentPCS.profit;
		double extraCost = bestPCS.cost - currentPCS.cost;
		Bid bid = new Bid(this, bestPCS.schedule, extraProfit, extraCost);
		bid.distToPickup = this.loc.Distance(r.source.point).First;
		r.stats.acceptableBids++;
		this.lastBid = bid;
		return bid;
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
					if (pcs.profit < -10000) {
						if (pcs.profit > bestPCS.profit) {
							System.out.println("String");
						}
					}
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
		Double dist = _travelledDistance;
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
				cost = this.GetCost(_paidTravelledDistance + (dist - (_travelledDistance + initTrip.First)), 
						(double)time.Subtract(start) - initTrip.Second); 
			}
		}
		Double profit = fare - cost;
		return new ProfitCostSchedule(profit, cost, schedule);
	}
	
	private Double GetCost(Double dist, Double time) {
		return 1 * dist;
	}
	
	@Override
	protected void UpdateTravelledDistance(double length) {
		super.UpdateTravelledDistance(length);
		if (_getPaid) {
			this._paidTravelledDistance += length;
		}
	}

	@Override
	public void AddRequest(AuctionRequest r) {
		this._schedule = new ArrayList<GPSNode>(lastBid.schedule);
		this.acceptedRequests.add(r);
	}
	
	@Override
	protected ArrayList<AuctionRequest> NewPointUpdates(Time time) {
		ArrayList<AuctionRequest> finishedRequests = new ArrayList<AuctionRequest>();
		GPSNode currentNode = _schedule.remove(0);
		AuctionRequest request = (AuctionRequest)currentNode.request;
		if (currentNode.type == Type.source) {
			request.PickUp(this._travelledDistance, time);
			this._getPaid = true;
			this.acceptedRequests.remove(request);
			this.onBoardRequests.add(request);
		}
		if (currentNode.type == Type.destination) {
			request.DropOff(_travelledDistance, time);
			this.collectedFare += request.finalFare;
			this.income += request.finalFare - request.serverProfit;
			this.onBoardRequests.remove(request);
			this.servicedRequests.add(request);
			if (this._schedule.isEmpty()) {
				_getPaid = false;
			}
			finishedRequests.add(request);
		}
		return finishedRequests;
	}
	

	public String PrintShortResults() {
		StringBuilder results = new StringBuilder();
		results.append(super.PrintShortResults());
		results.append(String.format("%.2f,%.2f,%.2f", 
				_paidTravelledDistance, collectedFare, income));		
		return results.toString();
	}
	
	public String PrintLongResults() {
		StringBuilder results = new StringBuilder();
		results.append(super.PrintLongResults());
		results.append(String.format("Paid Travelled Distance: %.2f, Collected Fare: %.2f, Income: %.2f\n", 
				_paidTravelledDistance, collectedFare, income));		
		return results.toString();
	}
}

