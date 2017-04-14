package edu.usc.infolab.ridesharing.kinetictree;

import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.Time;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class KTDriver extends Driver<Request> {
	public KTDriver(GPSPoint initialLoc, Time start, Time end) {
		super(initialLoc, start, end);
		_ktree = new KineticTree(this.loc);
		_distanceSinceRequest = new HashMap<>();
		_distanceSincePickUp = new HashMap<>();
	}

	private KineticTree _ktree;
	private KTTrip _currentTrip;
	private HashMap<Integer, Double> _distanceSinceRequest;
	private HashMap<Integer, Double> _distanceSincePickUp;
	
	
	public Double InsertRequest(Request request) {
		//KTTrip bestTrip = _ktree.InsertRequest(request);
		KTTrip bestTrip = null;
		KTNode rootCopy = _ktree.InsertRequest(request);
		if (rootCopy != null) {
			bestTrip = FindShortestTrip(rootCopy);
		}
		if (bestTrip != null)
			return bestTrip.Length() + loc.DistanceInMilesAndMillis(bestTrip.Get(0).point).distance;
		else
			return null;
	}

	private KTTrip FindShortestTrip(KTNode node) {
		KTTrip bestTrip = new KTTrip();
		for (KTNode n : node.next) {
			KTTrip childBestTrip = FindShortestTrip(n);
			if (childBestTrip.compareTo(bestTrip) < 0) {
				bestTrip = childBestTrip;
			}
		}
		bestTrip.AddToFirst(node);
		return bestTrip;
	}

    private KTTrip FindMostProfitableTrip(KTNode node) {
        KTTrip mostProfitableTrip = new KTTrip();
        for (KTNode child : node.next) {
            KTTrip childMostProfitibleTrip = FindMostProfitableTrip(child);
            if (childMostProfitibleTrip.MoreProfitibleThan(this, mostProfitableTrip)) {
                mostProfitableTrip = childMostProfitibleTrip;
            }
        }
        mostProfitableTrip.AddToFirst(node);
        return mostProfitableTrip;
    }
	
	@Override
	public void AddRequest(Request r, Time time) {
		this.acceptedRequests.add(r);
		_ktree.AddMostRecentRequest();
		_currentTrip = FindShortestTrip(_ktree.GetRoot());
		_distanceSinceRequest.put(r.id, 0.);
		_ktree.UpdateDeltas(
				new HashMap<>(_distanceSinceRequest),
				new HashMap<>(_distanceSincePickUp));
		_schedule = new ArrayList<>(_currentTrip.GetNodes());
	}
	
	@Override
	protected void UpdateTravelledDistance(double length) {
		super.UpdateTravelledDistance(length);
		for (Entry<Integer, Double> e : _distanceSinceRequest.entrySet()) {
			e.setValue(e.getValue() + length);
		}
		for (Entry<Integer, Double> e : _distanceSincePickUp.entrySet()) {
			e.setValue(e.getValue() + length);
		}
	}
	
	@Override
	protected ArrayList<Request> NewPointUpdates(Time time) {
		ArrayList<Request> finishedRequests = super.NewPointUpdates(time);
		_ktree.SetRoot(_currentTrip.Get(1));
		_currentTrip.UpdateRoot();
		return finishedRequests;
	}
	
	@Override
	protected void PickUpUpdates(Request request, Time time) {
		super.PickUpUpdates(request, time);
		_distanceSincePickUp.put(request.id, 0.);
		_distanceSinceRequest.remove(request.id);
	}
	
	@Override
	protected void DropOffUpdates(Request request, Time time) {
		super.DropOffUpdates(request, time);
		_distanceSincePickUp.remove(request.id);
	}
}
