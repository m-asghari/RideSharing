package edu.usc.infolab.ridesharing.kinetictree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.kinetictree.KTNode.Type;

public class KineticTree {
	private KTNode _root;
	private KTNode _rootCopy;
	
	public KineticTree(GPSPoint loc) {
		this._root.loc = loc;
	}
	
	public void SetRoot(KTNode node) {
		for (Iterator<KTNode> it = _root.next.iterator(); it.hasNext();) {
			if (!node.equals(it.next())) {
				it.remove();
			}
		}
		if (_root.next.size() != 1) {
			System.out.println("root does not containt node as child.");
		}
		node.loc = _root.loc;
		_root = node;
	}
	
	public void AddMostRecentRequest() {
		_rootCopy.loc = _root.loc;
		_root = _rootCopy;
	}
	
	public KTTrip InsertRequest(KTRequest r) {
		KTNode rSrc = new KTNode(r.source, Type.Source, r);
		KTNode rDst = new KTNode(r.destination, Type.Destination, r);
		ArrayList<KTNode> rPoints = new ArrayList<KTNode>();
		rPoints.add(rSrc);
		rPoints.add(rDst);
		_rootCopy = _root.clone();
		if (InsertNodes(_rootCopy, rPoints, 0)) {
			KTTrip bestTrip = FindBestTrip(_rootCopy);
			//root node is the current location and we don't want the current location
			//in the trip
			bestTrip.RemoveFirst();
			return bestTrip;
		}
		else
			return null;
	}
	
	private boolean InsertNodes(KTNode node, ArrayList<KTNode> points, double depth) {
		KTNode newNode = points.get(0);
		if (!Feasible(node, newNode, depth + node.loc.Distance(newNode.loc))) {
			return false;
		}
		boolean fail = true;
		for (KTNode n : node.next) {
			double detour = node.Distance(newNode) + newNode.Distance(n) - node.Distance(n);
			if (CopyNode(newNode, n, detour)) {
				fail = false;
			}
		}
		if (!fail && points.size() > 1) {
			ArrayList<KTNode> newPoints = new ArrayList<KTNode>(points);
			newPoints.remove(0);
			if (!InsertNodes(newNode, newPoints, -1 * points.get(0).Distance(points.get(1))))
				fail = true;
		}
		for (Iterator<KTNode> it = node.next.iterator(); it.hasNext();) {
			KTNode n = it.next();
			if (!InsertNodes(n, points, depth + node.Distance(n))) {
				it.remove();
			}
		}
		if (!fail) {
			node.next.add(newNode);
			return true;
		} else if (node.next.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	private boolean Feasible(KTNode node, KTNode newNode, double detour) {
		KTRequest r = newNode.request;
		if (newNode.type == Type.Source) {
			if (detour > r.maxWaitingTime)
				return false;
		}
		else if (newNode.type == Type.Destination) {
			if (detour > (1+r.serviceConstraint)*r.optDistance)
				return false;
		}
		return true;
	}
	
	private boolean CopyNode(KTNode node, KTNode source, double detour) {
		boolean retVal = false;
		if (Feasible(source, detour)) {
			KTNode sourceCopy = new KTNode(source.loc, source.type, source.request);
			node.next.add(sourceCopy);
			if (source.next.isEmpty()) {
				return true;
			}
			for (KTNode n : source.next) {
				if (CopyNode(sourceCopy, n, detour)) {
					retVal = true;
				};
			}
		}
		return retVal;
	}
	
	private boolean Feasible(KTNode node, double detour) {
		if (node.Delta > detour)
			return true;
		return false;
	}
	
	public KTTrip FindBestTrip() {
		return FindBestTrip(_root);
	}
	
	// To be able to find trip either on _root or _rootCopy
	private KTTrip FindBestTrip(KTNode root) {
		return FindBestTrip(root, new KTTrip());
	}
	
	private KTTrip FindBestTrip(KTNode node, KTTrip trip) {
		trip.AddNode(node);
		if (node.next.isEmpty()) {
			return trip;
		}
		KTTrip best = new KTTrip();
		for (KTNode n : node.next) {
			KTTrip newTrip = FindBestTrip(n, trip);
			if (newTrip.compareTo(best) < 0) {
				best = newTrip;
			}
		}
		return best;
	}
	
	public void UpdateDeltas(HashMap<Integer, Double> distanceSinceRequest,
			HashMap<Integer, Double> distanceSincePickUp) {
		ComputeDelta(_root, 0, distanceSinceRequest, distanceSincePickUp);
	}
	
	private double ComputeDelta(KTNode node, double depth,
			HashMap<Integer, Double> distanceSinceRequest,
			HashMap<Integer, Double> distanceSincePickUp) {
		KTRequest r = node.request;
		if (node.type == Type.Source) {
			node.delta = r.maxWaitingTime - (distanceSinceRequest.get(r.id) + depth);
			distanceSinceRequest.remove(r.id);
			distanceSincePickUp.put(r.id, 0.);		
		} else if (node.type == Type.Destination) {
			node.delta = ((1+r.serviceConstraint)*r.optDistance) - (distanceSincePickUp.get(r.id) + depth);
		}
		double maxChildDelta = Double.MIN_VALUE;
		for (KTNode child : node.next) {
			double newDelta = ComputeDelta(child, depth + node.Distance(child), distanceSinceRequest, distanceSincePickUp);
			if (newDelta > maxChildDelta) {
				maxChildDelta = newDelta;
			}
		}
		node.Delta = Math.min(maxChildDelta, node.delta);
		return node.Delta;
	}
}
