package edu.usc.infolab.ridesharing.kinetictree;

import edu.usc.infolab.geom.GPSNode.Type;
import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.FailureReason;
import edu.usc.infolab.ridesharing.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class KineticTree {
  public KTNode _root;
  public KTNode _rootCopy;

  public KineticTree(GPSPoint point) {
    this._root = new KTNode(point, Type.root, null);
  }

  public KTNode SetRoot(KTNode tripNode) {
    if (!_root.next.contains(tripNode)) {
      System.out.println("root does not containt node as child.");
    }
    for (Iterator<KTNode> it = _root.next.iterator(); it.hasNext(); ) {
      if (!tripNode.equals(it.next())) {
        it.remove();
      }
    }
    _root = tripNode;
    _root.request = null;
    _root.type = Type.root;
    return _root;
  }

  public void AddMostRecentRequest() {
    _root = _rootCopy;
  }

  public KTTrip InsertRequest(KTRequest r) {
    KTNode rSrc = new KTNode(r.source.point, Type.source, r);
    KTNode rDst = new KTNode(r.destination.point, Type.destination, r);
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
    } else {
      return null;
    }
  }

  private boolean InsertNodes(KTNode node, ArrayList<KTNode> points, double depth) {
    if (!Feasible(
        node,
        points.get(0),
        depth + node.point.DistanceInMilesAndMillis(points.get(0).point).First)) {
      return false;
    }
    boolean fail = false;
    KTNode newNode = points.get(0).clone();
    for (KTNode n : node.next) {
      double detour =
          node.DistanceInMilesAndMillis(newNode).First
              + newNode.DistanceInMilesAndMillis(n).First
              - node.DistanceInMilesAndMillis(n).First;
      if (!CopyNode(newNode, n, detour)) {
        fail = true;
      }
    }
    if (!fail && points.size() > 1) {
      ArrayList<KTNode> newPoints = new ArrayList<KTNode>(points);
      newPoints.remove(0);
      if (!InsertNodes(
          newNode, newPoints, -1 * points.get(0).DistanceInMilesAndMillis(points.get(1)).First))
        fail = true;
    }
    for (Iterator<KTNode> it = node.next.iterator(); it.hasNext(); ) {
      KTNode n = it.next();
      if (!InsertNodes(n, points, depth + node.DistanceInMilesAndMillis(n).First)) {
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

  private boolean Feasible(@SuppressWarnings("unused") KTNode node, KTNode newNode, double detour) {
    KTRequest r = (KTRequest) newNode.request;
    if (newNode.type == Type.source) {
      if (detour > r.maxWaitTime) {
        r.stats.AddFailureReason(FailureReason.MaxWaitTime);
        return false;
      }
    } else if (newNode.type == Type.destination) {
      if (!Utils.IsAcceptableDetour(detour, r.optDistance)) {
        r.stats.AddFailureReason(FailureReason.ServiceConstraint);
        return false;
      }
    }
    return true;
  }

  private boolean CopyNode(KTNode node, KTNode source, double detour) {
    boolean retVal = false;
    if (Feasible(source, detour)) {
      KTNode sourceCopy = new KTNode(source.point, source.type, (KTRequest) source.request);
      node.next.add(sourceCopy);
      if (source.next.isEmpty()) {
        return true;
      }
      for (KTNode n : source.next) {
        if (CopyNode(sourceCopy, n, detour)) {
          retVal = true;
        }
      }
    }
    return retVal;
  }

  private boolean Feasible(KTNode node, double detour) {
    if (node.Delta > detour) {
      return true;
    }
    node.request.stats.AddFailureReason(FailureReason.CantCopy);
    return false;
  }

  public KTTrip FindBestTrip() {
    return FindBestTrip(_root);
  }

  private KTTrip FindBestTrip(KTNode node) {
    KTTrip bestTrip = new KTTrip();
    for (KTNode n : node.next) {
      KTTrip childBestTrip = FindBestTrip(n);
      if (childBestTrip.compareTo(bestTrip) < 0) {
        bestTrip = childBestTrip;
      }
    }
    bestTrip.AddToFirst(node);
    return bestTrip;
  }

  /*
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
  }*/

  public void UpdateDeltas(
      HashMap<Integer, Double> distanceSinceRequest, HashMap<Integer, Double> distanceSincePickUp) {
    ComputeDelta(_root, 0, distanceSinceRequest, distanceSincePickUp);
  }

  private double ComputeDelta(
      KTNode node,
      double depth,
      HashMap<Integer, Double> distanceSinceRequest,
      HashMap<Integer, Double> distanceSincePickUp) {
    KTRequest r = (KTRequest) node.request;
    if (node.type == Type.source) {
      node.delta = r.maxWaitTime - (distanceSinceRequest.get(r.id) + depth);
      distanceSinceRequest.remove(r.id);
      distanceSincePickUp.put(r.id, 0.);
    } else if (node.type == Type.destination) {
      //node.delta =
      //    ((1 + r.serviceConstraint) * r.optDistance) - (distanceSincePickUp.get(r.id) + depth);
      switch (Utils.detourConstraintMethod) {
        case FIXED:
          node.delta = (r.optDistance + Utils.MaxDetourFixed) - (distanceSincePickUp.get(r.id) + depth);
          break;
        case RELATIVE:
          node.delta = ((1 + r.serviceConstraint) * r.optDistance) - (distanceSincePickUp.get(r.id) + depth);
          break;
        default:
          break;
      }
    }
    if (node.next.isEmpty()) {
      node.Delta = node.delta;
      return node.Delta;
    }
    double maxChildDelta = Double.MIN_VALUE;
    for (KTNode child : node.next) {
      double newDelta =
          ComputeDelta(
              child,
              depth + node.DistanceInMilesAndMillis(child).First,
              new HashMap<Integer, Double>(distanceSinceRequest),
              new HashMap<Integer, Double>(distanceSincePickUp));
      if (newDelta > maxChildDelta) {
        maxChildDelta = newDelta;
      }
    }
    node.Delta = Math.min(maxChildDelta, node.delta);
    return node.Delta;
  }
}
