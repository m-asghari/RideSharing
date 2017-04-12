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
		KTTrip bestTrip = _ktree.InsertRequest(request);
		if (bestTrip != null)
			return bestTrip.Length() + loc.DistanceInMilesAndMillis(bestTrip.Get(0).point).distance;
		else
			return null;
	}
	
	@Override
	public void AddRequest(Request r, Time time) {
		this.acceptedRequests.add(r);
		_ktree.AddMostRecentRequest();
		_currentTrip = _ktree.FindBestTrip();
		_distanceSinceRequest.put(r.id, 0.);
		_ktree.UpdateDeltas(
				new HashMap<>(_distanceSinceRequest),
				new HashMap<>(_distanceSincePickUp));
		_schedule = new ArrayList<>(_currentTrip.GetNodes());
	}
	
	/*private void Check2() {
		if (_currentTrip == null) {
			return;
		}
		for (int i = 0; i < _currentTrip.nodes.size() - 1; i++) {
			if (!_currentTrip.Get(i).next.contains(_currentTrip.Get(i+1))) {
				System.out.println("Check2 Went Wrong");
				_ktree.FindBestTrip();
			}			
		}
	}
	
	private void Check() {
		KTNode currentTreeNode = _ktree._root;
		if (_currentTrip == null)
			return;
		if (!_currentTrip.Get(0).equals(currentTreeNode)) {
			System.out.println("Check Went Wrong");
		}
		for (int i = 1; i < _currentTrip.nodes.size(); i++) {
			if (!currentTreeNode.next.contains(_currentTrip.Get(i))) {
				System.out.println("Check Went Wrong");
			}
			int index = currentTreeNode.next.indexOf(_currentTrip.Get(i));
			currentTreeNode = currentTreeNode.next.get(index);
		}
	}*/
	
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
