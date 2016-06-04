package edu.usc.infolab.ridesharing.kinetictree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Time;

public class KTDriver extends Driver<KTRequest> {
	public KTDriver(GPSPoint initialLoc, Time start, Time end) {
		super(initialLoc, start, end);
		_ktree = new KineticTree(this.loc);
		_distanceSinceRequest = new HashMap<Integer, Double>();
		_distanceSincePickUp = new HashMap<Integer, Double>();
	}

	private KineticTree _ktree;
	private KTTrip _currentTrip;
	private HashMap<Integer, Double> _distanceSinceRequest;
	private HashMap<Integer, Double> _distanceSincePickUp;
	
	
	public Double InsertRequest(KTRequest request) {
		KTTrip bestTrip = _ktree.InsertRequest(request);
		if (bestTrip != null)
			return bestTrip.Length() + loc.Distance(bestTrip.Get(0).loc).First;
		else
			return null;
	}
	
	@Override
	public void AddRequest(KTRequest r) {
		_ktree.AddMostRecentRequest();
		_distanceSinceRequest.put(r.id, 0.);
		_ktree.UpdateDeltas(_distanceSinceRequest, _distanceSincePickUp);
		_currentTrip = _ktree.FindBestTrip();
		//TODO(mohammad): un-comment next line and fix the error. KTNode should inherit from Node<> and ...
		//_schedule = _currentTrip.GetPoints();
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
	protected ArrayList<KTRequest> NewPointUpdates(Time time) {
		ArrayList<KTRequest> finishedRequests = new ArrayList<KTRequest>();
		_schedule.remove(0);
		KTNode node = _currentTrip.RemoveFirst();
		_ktree.SetRoot(node);
		switch (node.type) {
			case Source:
				_distanceSincePickUp.put(node.request.id, 0.);
				_distanceSinceRequest.remove(node.request.id);
				break;
			case Destination:
				_distanceSincePickUp.remove(node.request.id);
				finishedRequests.add(node.request);
				break;
			case Root:
				break;
		}
		return finishedRequests;
	}
}
