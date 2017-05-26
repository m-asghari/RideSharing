package edu.usc.infolab.ridesharing.kinetictree;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.pricing.DetourCompensatingModel;

import java.util.ArrayList;

public class KTTrip implements Comparable<KTTrip>{
	public ArrayList<KTNode> nodes;
	private double _length;
	private double _totalFare;
	
	public KTTrip() {
		nodes = new ArrayList<>();
		_length = 0;
		_totalFare = 0;
	}
	
	protected KTTrip(KTTrip other) {
		this.nodes = new ArrayList<>(other.nodes);
		this._length = other._length;
		this._totalFare = other._totalFare;
	}
	
	/*public void AddNode(KTNode node) {
		if (!nodes.isEmpty()) {
			_length += nodes.get(nodes.size()-1).DistanceInMilesAndMillis(node).First;
		}
		nodes.add(node);
	}*/
	
	public void AddToFirst(KTNode node) {
		nodes.add(0, node);
		if (nodes.size() > 1) {
			_length += node.DistanceInMilesAndMillis(nodes.get(1)).distance;
		}
		if (node.type == GPSNode.Type.destination) {
			Request r = node.request;
			_totalFare += DetourCompensatingModel.getInstance().ComputeFinalFare(r, node.delta);
		}
	}
	
	void RemoveFirst() {
		if (nodes.isEmpty()) {
			return;
		}
		if (nodes.size() <= 2) {
			_length = 0;
		} else {
			_length -= nodes.get(0).DistanceInMilesAndMillis(nodes.get(1)).distance;
			//_length -= nodes.get(1).fromParent.distance;
		}
		return;
	}
	
	/*public KTNode RemoveLast() {
		if (nodes.isEmpty()) {
			return null;
		}
		int size = nodes.size();
		if (size <= 2) {
			_length = 0;
		} else {
			_length -= nodes.get(size - 1).DistanceInMilesAndMillis(nodes.get(size - 2)).First;
		}
		return nodes.remove(size - 1);
	}*/

	public KTNode Get(int index) {
		return nodes.get(index);
	}
	
	public double Length() {
		return _length;
	}
	
	public ArrayList<GPSNode> GetNodes() {
		ArrayList<GPSNode> retNodes = new ArrayList<GPSNode>();
		for (KTNode n : nodes) {
			retNodes.add(new GPSNode(n.point, n.type, n.request));
		}
		retNodes.remove(0);
		return retNodes;
	}
	
	@Override
	public KTTrip clone() {
		return new KTTrip(this);
	}
	
	void UpdateRoot() {
		nodes.remove(0);
	}

	@Override
	public int compareTo(KTTrip other) {
		if (this.nodes.size() == other.nodes.size()) {
			if (this._length < other._length)
				return -1;
			if (this._length > other._length)
				return 1;
			return 0;
		}
		return -1 * (this.nodes.size() - other.nodes.size());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof KTTrip)) {
			return false;
		}
		KTTrip other = (KTTrip)obj;
		if (this.nodes.size() != other.nodes.size())
			return false;
		for (int i = 0; i < this.nodes.size(); i++) {
			if (!nodes.get(i).equals(other.nodes.get(i)))
				return false;
		}
		return true;
	}

	public boolean MoreProfitibleThan(Driver driver, KTTrip other) {
		if (this.nodes.size() > other.nodes.size())
			return true;
		double thisIncome =
                DetourCompensatingModel.getInstance()
                        .ComputeDriverIncome(driver, driver.onBoardRequests.size(), _length, 0);
		double otherIncome =
                DetourCompensatingModel.getInstance()
                        .ComputeDriverIncome(driver, driver.onBoardRequests.size(), other._length, 0);
		if (_totalFare - thisIncome >= other._totalFare - otherIncome)
		    return true;
		return false;
	}
}
