package edu.usc.infolab.ridesharing.kinetictree;

import java.util.ArrayList;

import edu.usc.infolab.geom.GPSNode;

public class KTTrip implements Comparable<KTTrip>{
	public ArrayList<KTNode> nodes;
	private double _length;
	
	public KTTrip() {
		nodes = new ArrayList<KTNode>();
		_length = 0;
	}
	
	protected KTTrip(KTTrip other) {
		this.nodes = new ArrayList<KTNode>(other.nodes);
		this._length = other._length;
	}
	
	public void AddNode(KTNode node) {
		if (!nodes.isEmpty()) {
			_length += nodes.get(nodes.size()-1).DistanceInMilesAndMillis(node).First;
		}
		nodes.add(node);
	}
	
	public void AddToFirst(KTNode node) {
		nodes.add(0, node);
		if (nodes.size() > 1) {
			_length += node.DistanceInMilesAndMillis(nodes.get(1)).First;
		}
	}
	
	public KTNode RemoveFirst() {
		if (nodes.isEmpty()) {
			return null;
		}
		if (nodes.size() <= 2) {
			_length = 0;
		} else {
			_length -= nodes.get(0).DistanceInMilesAndMillis(nodes.get(1)).First;
		}
		return nodes.remove(0);	
	}
	
	public KTNode RemoveLast() {
		if (nodes.isEmpty()) {
			return null;
		}
		int size = nodes.size();
		if (size <= 2) {
			_length = 0;
		} else {
			_length = nodes.get(size - 1).DistanceInMilesAndMillis(nodes.get(size - 2)).First;
		}
		return nodes.remove(size - 1);
	}

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
	
	public void UpdateRoot() {
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

}
