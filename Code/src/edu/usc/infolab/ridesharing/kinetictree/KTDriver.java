package edu.usc.infolab.ridesharing.kinetictree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import edu.usc.infolab.geom.GPSNode;
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
			return bestTrip.Length() + loc.Distance(bestTrip.Get(0).point).First;
		else
			return null;
	}
	
	@Override
	public void AddRequest(KTRequest r) {
		this.acceptedRequests.add(r);
		_ktree.AddMostRecentRequest();
		_currentTrip = _ktree.FindBestTrip();
		_distanceSinceRequest.put(r.id, 0.);
		_ktree.UpdateDeltas(
				new HashMap<Integer, Double>(_distanceSinceRequest),
				new HashMap<Integer, Double>(_distanceSincePickUp));
		_schedule = new ArrayList<GPSNode>(_currentTrip.GetNodes());
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
	protected ArrayList<KTRequest> NewPointUpdates(Time time) {
		ArrayList<KTRequest> finishedRequests = new ArrayList<KTRequest>();
		KTNode node = _currentTrip.Get(1);
		KTRequest request = (KTRequest)node.request;
		switch (node.type) {
			case source:
				_distanceSincePickUp.put(node.request.id, 0.);
				_distanceSinceRequest.remove(node.request.id);
				this.acceptedRequests.remove(request);
				this.onBoardRequests.add(request);
				break;
			case destination:
				_distanceSincePickUp.remove(node.request.id);
				this.onBoardRequests.remove(request);
				this.servicedRequests.add(request);
				finishedRequests.add(request);
				break;
			case root:
				break;
		}
		_ktree.SetRoot(node);
		_currentTrip.UpdateRoot();
		_schedule.remove(0);
		return finishedRequests;
	}
}
