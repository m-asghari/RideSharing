package edu.usc.infolab.ridesharing.kinetictree;

import edu.usc.infolab.geom.GPSNode.Type;
import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.FailureReason;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.TimeDistancePair;
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

  KTNode GetRoot() {
      return _root;
  }

  public KTNode InsertRequest(Request r) {
      KTNode rSrc = new KTNode(r.source.point, Type.source, r);
      KTNode rDst = new KTNode(r.destination.point, Type.destination, r);
      ArrayList<KTNode> rPoints = new ArrayList<KTNode>();
      rPoints.add(rDst);
      _rootCopy = _root.clone();
      if (NewInsertNodes(_rootCopy, rSrc, rPoints, 0)) {
          return _rootCopy;
      } else {
          return null;
      }
  }

  /*public KTTrip InsertRequest(Request r) {
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
  }*/

  // This method is similar to InsertNodes. Reading it might just be a little easier!!
  private boolean NewInsertNodes(KTNode root, KTNode node, ArrayList<KTNode> remaining, double depth) {
    TimeDistancePair rootToNode = root.point.DistanceInMilesAndMillis(node.point);
    Utils.spComputations++;
    if (!Feasible(
            root,
            node,
            depth + rootToNode.distance)) {
      return false;
    }
    boolean fail = false;
    KTNode nodeCopy = node.clone();
    for (KTNode n : root.next) {
        TimeDistancePair nodeCopyToN = nodeCopy.DistanceInMilesAndMillis(n);
        Utils.spComputations++;
        double detour =
                rootToNode.distance
                        + nodeCopyToN.distance
                        - n.fromParent.distance;
        if (!CopyNode(nodeCopy, n, detour, nodeCopyToN)) {
            fail = true;
        }
    }
    if (!fail && !remaining.isEmpty()) {
      ArrayList<KTNode> newRemaining = new ArrayList<KTNode>(remaining);
      KTNode newNode = newRemaining.remove(0);
      Utils.spComputations++;
      if (!NewInsertNodes(
              nodeCopy, newNode, newRemaining, -1 * node.DistanceInMilesAndMillis(newNode).distance))
        fail = true;
    }
    for (Iterator<KTNode> it = root.next.iterator(); it.hasNext(); ) {
      KTNode n = it.next();
      Utils.spComputations++;
      if (!NewInsertNodes(n, node, remaining, depth + root.DistanceInMilesAndMillis(n).distance)) {
        it.remove();
      }
    }
    if (!fail) {
      root.next.add(nodeCopy);
      nodeCopy.fromParent = rootToNode.clone();
      return true;
    } else if (root.next.isEmpty()) {
      return false;
    } else {
      return true;
    }
  }

  /*private boolean InsertNodes(KTNode node, ArrayList<KTNode> points, double depth) {
    if (!Feasible(
        node,
        points.get(0),
        depth + node.point.DistanceInMilesAndMillis(points.get(0).point).distance)) {
      return false;
    }
    boolean fail = false;
    KTNode newNode = points.get(0).clone();
    for (KTNode n : node.next) {
      double detour =
          node.DistanceInMilesAndMillis(newNode).distance
              + newNode.DistanceInMilesAndMillis(n).distance
              - node.DistanceInMilesAndMillis(n).distance;
      if (!CopyNode(newNode, n, detour)) {
        fail = true;
      }
    }
    if (!fail && points.size() > 1) {
      ArrayList<KTNode> newPoints = new ArrayList<KTNode>(points);
      newPoints.remove(0);
      if (!InsertNodes(
          newNode, newPoints, -1 * points.get(0).DistanceInMilesAndMillis(points.get(1)).distance))
        fail = true;
    }
    for (Iterator<KTNode> it = node.next.iterator(); it.hasNext(); ) {
      KTNode n = it.next();
      if (!InsertNodes(n, points, depth + node.DistanceInMilesAndMillis(n).distance)) {
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
  }*/

  private boolean Feasible(@SuppressWarnings("unused") KTNode node, KTNode newNode, double detour) {
    Request r = newNode.request;
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

    /**
     * Recursively copies source under node (if source can tolerate detour)
     */
    /*private boolean CopyNode(KTNode node, KTNode source, double detour) {
        boolean retVal = false;
        if (Feasible(source, detour)) {
            KTNode sourceCopy = new KTNode(source.point, source.type, source.request);
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
    }*/

  /**
   * Recursively copies source under node (if source can tolerate detour)
   */
  private boolean CopyNode(KTNode node, KTNode source, double detour, TimeDistancePair nodeToSource) {
    boolean retVal = false;
    if (Feasible(source, detour)) {
      KTNode sourceCopy = new KTNode(source.point, source.type, source.request);
      node.next.add(sourceCopy);
      sourceCopy.fromParent = nodeToSource.clone();
      if (source.next.isEmpty()) {
        return true;
      }
      for (KTNode n : source.next) {
        if (CopyNode(sourceCopy, n, detour, n.fromParent)) {
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

  /*public KTTrip FindBestTrip() {
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
    Request r = node.request;
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
          node.delta = ((1 + r.maxRelDetour) * r.optDistance) - (distanceSincePickUp.get(r.id) + depth);
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
              depth + node.DistanceInMilesAndMillis(child).distance,
              new HashMap<>(distanceSinceRequest),
              new HashMap<>(distanceSincePickUp));
      if (newDelta > maxChildDelta) {
        maxChildDelta = newDelta;
      }
    }
    node.Delta = Math.min(maxChildDelta, node.delta);
    return node.Delta;
  }
}
