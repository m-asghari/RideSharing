package edu.usc.infolab.ridesharing.kinetictree;

import java.util.HashMap;
import java.util.Map.Entry;

import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Driver;

public class KTDriver extends Driver<GPSPoint, KTRequest> {
	private KineticTree _ktree;
	private KTTrip _currentTrip;
	private HashMap<Integer, Double> _distanceSinceRequest;
	private HashMap<Integer, Double> _distanceSincePickUp;
	
	public KTDriver() {
		super();
		_ktree = new KineticTree(this.loc);
		_distanceSinceRequest = new HashMap<Integer, Double>();
		_distanceSincePickUp = new HashMap<Integer, Double>();
	}
	
	public Double InsertRequest(KTRequest request) {
		KTTrip bestTrip = _ktree.InsertRequest(request);
		if (bestTrip != null)
			return bestTrip.Length() + loc.Distance(bestTrip.Get(0).loc);
		else
			return null;
	}
	
	@Override
	protected void AddRequest(KTRequest r) {
		_ktree.AddMostRecentRequest();
		_distanceSinceRequest.put(r.id, 0.);
		_ktree.UpdateDeltas(_distanceSinceRequest, _distanceSincePickUp);
		_currentTrip = _ktree.FindBestTrip();
		_schedule = _currentTrip.GetPoints();
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
	protected void NewPointUpdates() {
		super.NewPointUpdates();
		KTNode node = _currentTrip.RemoveFirst();
		_ktree.SetRoot(node);
		switch (node.type) {
			case Source:
				_distanceSincePickUp.put(node.request.id, 0.);
				_distanceSinceRequest.remove(node.request.id);
				break;
			case Destination:
				_distanceSincePickUp.remove(node.request.id);
				break;
			case Root:
				break;
		}
	}
}
