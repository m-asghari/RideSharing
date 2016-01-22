package edu.usc.infolab.ridesharing.kinetictree;

import java.util.ArrayList;

import edu.usc.infolab.geom.GPSPoint;

public class KTTrip implements Comparable<KTTrip>{
	private ArrayList<KTNode> _nodes;
	private double _length;
	
	public KTTrip() {
		_nodes = new ArrayList<KTNode>();
		_length = 0;
	}
	
	protected KTTrip(KTTrip other) {
		this._nodes = new ArrayList<KTNode>(other._nodes);
		this._length = other._length;
	}
	
	public void AddNode(KTNode node) {
		if (!_nodes.isEmpty()) {
			_length += _nodes.get(_nodes.size()-1).Distance(node);
		}
		_nodes.add(node);
	}
	
	public KTNode RemoveFirst() {
		if (_nodes.isEmpty()) {
			return null;
		}
		if (_nodes.size() <= 2) {
			_length = 0;
		} else {
			_length -= _nodes.get(0).Distance(_nodes.get(1));
		}
		return _nodes.remove(0);	
	}
	
	public KTNode RemoveLast() {
		if (_nodes.isEmpty()) {
			return null;
		}
		int size = _nodes.size();
		if (size <= 2) {
			_length = 0;
		} else {
			_length = _nodes.get(size - 1).Distance(_nodes.get(size - 2));
		}
		return _nodes.remove(size - 1);
	}

	public KTNode Get(int index) {
		return _nodes.get(index);
	}
	
	public double Length() {
		return _length;
	}
	
	public ArrayList<GPSPoint> GetPoints() {
		ArrayList<GPSPoint> points = new ArrayList<GPSPoint>();
		for (KTNode n : _nodes) {
			points.add(new GPSPoint(n.loc));
		}
		return points;
	}
	
	public KTTrip clone() {
		return new KTTrip(this);
	}

	@Override
	public int compareTo(KTTrip other) {
		if (this._nodes.size() == other._nodes.size()) {
			if (this._length < other._length)
				return -1;
			if (this._length > other._length)
				return 1;
			return 0;
		}
		return -1 * (this._nodes.size() - other._nodes.size());
	}

}
