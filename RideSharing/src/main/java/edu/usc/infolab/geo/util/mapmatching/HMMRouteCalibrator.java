package edu.usc.infolab.geo.util.mapmatching;

import com.google.gson.Gson;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;
import edu.usc.infolab.geo.model.IndexedDirectedGraph;
import edu.usc.infolab.geo.model.MatchedTrajectory;
import edu.usc.infolab.geo.model.Trajectory;
import edu.usc.infolab.geo.model.TrjRecord;
import edu.usc.infolab.geo.util.EdgeHelper;
import edu.usc.infolab.geo.util.NodeHelper;
import org.geotools.graph.path.Path;
import org.geotools.graph.path.Walk;
import org.geotools.graph.structure.DirectedEdge;
import org.geotools.graph.structure.Edge;
import org.opengis.feature.simple.SimpleFeature;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Hidden Markov Model based Route Calibrator.
 *
 * @author Yaguang
 */
public class HMMRouteCalibrator implements RouteCalibrator {

    final private IndexedDirectedGraph graph;

    /**
     * Standard deviation of the distance from the trajectory point to its actual position.
     */
    final private static double sigma = 10;
    final private static double sSigma = 1 / (sigma * sigma);
    /**
     * The average distance difference between Euclidean point distance and shortest path distance.
     */
    final private static double beta = 5;
    final private static double sBeta = -1 / beta;
    final private Logger logger = Logger.getLogger(this.getClass().getName());
    private boolean logAStarFailure = false;


    /**
     * Represents a path and corresponding information in the Viterbi Searching process.
     *
     * @author Yaguang
     */
    private static class HMMNode {
        private HMMNode parent = null;
        private double prob;
        private long edgeId;
        private int endInd;

        public HMMNode(HMMNode parent, double prob, long edgeId, int endInd) {
            this.parent = parent;
            this.prob = prob;
            this.edgeId = edgeId;
            this.endInd = endInd;
        }


        /**
         * Gets the previous node for backtracking.
         *
         * @return
         */
        public HMMNode getParent() {
            return parent;
        }

        /**
         * Gets the probability of the corresponding path.
         *
         * @return
         */
        public double getProb() {
            return prob;
        }

        /**
         * Gets the corresponding edge id, i.e., the Markov status id.
         *
         * @return
         */
        public long getEdgeId() {
            return edgeId;
        }

        /**
         * Gets the index of the corresponding trajectory points.
         *
         * @return
         */
        @SuppressWarnings("unused")
        public int getEndInd() {
            return endInd;
        }

        public String toString() {
            return new Gson().toJson(this);
        }
    }

    /**
     * @param b
     */
    public void setWriteLogAStarFailure(boolean b) {
        this.logAStarFailure = b;
    }

    double getEmissionProbability(Edge e, Coordinate coord) {
        double prob = Double.NEGATIVE_INFINITY;
        LineString line = (LineString) ((SimpleFeature) e.getObject()).getDefaultGeometry();
        try {
            LocationIndexedLine indexedLine = new LocationIndexedLine(line);
            LinearLocation projectInd = indexedLine.project(coord);
            Coordinate projection = indexedLine.extractPoint(projectInd);
            double distance = projection.distance(coord);
            prob = -0.5 * distance * distance * sSigma;
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return prob;
    }

    double getTransitionProbability(Edge e1, Coordinate p1, Edge e2, Coordinate p2) {
        double euclideanDist = 0;
        euclideanDist = p1.distance(p2);
        double bound = Math.max(200, euclideanDist * 2);
        double rnDist = graph.getRNDist(e1, p1, e2, p2, bound);
        double diff = Math.abs(rnDist - euclideanDist);
        double prob = diff * sBeta;
        return prob;
    }


    private HashSet<Edge> getCandidateEdges(Coordinate p, double radius) {
        Envelope env = new Envelope(p);
        env.expandBy(radius);
        return graph.queryEdges(env);
    }

    public HMMRouteCalibrator(IndexedDirectedGraph graph) {
        this.graph = graph;
    }


    public Walk calibrate(List<Coordinate> coords) {
        List<Edge> edges = matchCoordsToEdges(coords);
        return buildWalk(edges);
    }

    private List<Edge> matchCoordsToEdges(List<Coordinate> coords) {
        coords = new ArrayList<>(coords);
        coords.add(coords.get(coords.size() - 1));
        int trjSize = coords.size();
        Coordinate startPoint = coords.get(0);
        double radius = 100; // 100 m.
        double maxRadius = 200;
        List<Edge> currentStates = new ArrayList<>(getCandidateEdges(startPoint, radius));
        HashMap<Long, HMMNode> T = new HashMap<>();
        List<Edge> edges = new ArrayList<>();
        // Gets initial states.
        for (Edge e : currentStates) {
            double prob = getEmissionProbability(e, startPoint);
            long edgeId = graph.getInnerEdgeId(e);
            T.put(edgeId, new HMMNode(null, prob, edgeId, 0));
        }

        // Forward Viterbi.
        for (int ind = 0; ind < trjSize - 1; ++ind) {
            HashMap<Long, HMMNode> U = new HashMap<>();
            double highest = Double.NEGATIVE_INFINITY;
            List<Edge> nextStates = null;
            double currentRadius = radius;
            while (currentRadius <= maxRadius && Double.isInfinite(highest)) {
                nextStates = new ArrayList<>(getCandidateEdges(coords.get(ind + 1), currentRadius));
                for (Edge nextState : nextStates) {
                    double valMax = Double.NEGATIVE_INFINITY;
                    HMMNode argMax = null;
                    for (Edge currentState : currentStates) {
                        HMMNode n = T.get(graph.getInnerEdgeId(currentState));
                        double vProb = n.getProb();
                        if (Double.isInfinite(vProb)) {
                            continue;
                        }
                        double ep = getEmissionProbability(currentState, coords.get(ind));
                        double tp = getTransitionProbability(currentState, coords.get(ind), nextState,
                                coords.get(ind + 1));
                        vProb += ep + tp;
                        if (vProb > valMax) {
                            valMax = vProb;
                            argMax = n;
                        }
                        if (vProb > highest) {
                            highest = vProb;
                        }
                    }
                    U.put(graph.getInnerEdgeId(nextState),
                            new HMMNode(argMax, valMax, graph.getInnerEdgeId(nextState), ind + 1));
                }
                currentRadius *= 2;
            }
            if (Double.isInfinite(highest)) {
                // Handle HMM break
                logger.severe(String.format("HMM break at position: %d.", ind));
                // Reinitialize start point
                startPoint = coords.get(ind + 1);
                edges.addAll(backtracking(T));

                // Set probability
                for (Edge e : nextStates) {
                    double prob = getEmissionProbability(e, startPoint);
                    U.put(graph.getInnerEdgeId(e), new HMMNode(null, prob, graph.getInnerEdgeId(e), ind + 1));
                }
            }
            // Move to the next states.
            T = U;
            currentStates = nextStates;
        }
        edges.addAll(backtracking(T));
        return edges;
    }

    @Override
    public MatchedTrajectory calibrate(Trajectory trajectory) {
        List<TrjRecord> records = trajectory.getRecords();
        List<Long> times = records.stream().map(TrjRecord::getTime).collect(Collectors.toList());
        List<Coordinate> coordinates = records.stream().map(TrjRecord::getLocation).collect(Collectors.toList());
        List<Edge> edges = matchCoordsToEdges(coordinates);
        // Interpolate times.
        if (edges.isEmpty()) {
            throw new InvalidParameterException("Empty edge list.");
        }

        List<MatchedTrajectory.EdgeWithTime> edgeWithTimes = new ArrayList<>();
        // Time passing the previous node.
        long prevNodeTime = times.get(0);
        int size = Math.min(edges.size(), records.size());
        for (int i = 1; i < size; ++i) {
            DirectedEdge prevEdge = (DirectedEdge) edges.get(i - 1);
            DirectedEdge currentEdge = (DirectedEdge) edges.get(i);
            // Not passing any node, then continue.
            if (currentEdge.equals(prevEdge))
                continue;
            // Passing one or more nodes.
            double dist1 = graph.getRNDist(prevEdge, records.get(i - 1).getLocation(),
                    prevEdge, new NodeHelper(prevEdge.getOutNode()).getCoordinate());
            double totalDistance = graph.getRNDist(prevEdge, records.get(i - 1).getLocation(),
                    currentEdge, records.get(i).getLocation());
            long duration = times.get(i) - times.get(i - 1);
            double speed = totalDistance / duration;
//            long curNodeTime = (long) (dist1 / speed + prevNodeTime);
            long curNodeTime = (long) (dist1 / speed) + times.get(i - 1);
            edgeWithTimes.add(new MatchedTrajectory.EdgeWithTime(prevEdge, prevNodeTime, curNodeTime));
            prevNodeTime = curNodeTime;
            Walk path = graph.queryShortestPath(prevEdge.getOutNode(), currentEdge.getInNode());
            double pathLength = 0;
            for (Object e : path.getEdges()) {
                EdgeHelper eh = new EdgeHelper((Edge) e);
                curNodeTime = (long) (prevNodeTime + eh.getLength() / speed);
                pathLength += eh.getLength();
                edgeWithTimes.add(new MatchedTrajectory.EdgeWithTime((DirectedEdge) e, prevNodeTime, curNodeTime));
                prevNodeTime = curNodeTime;
            }
            if (pathLength > totalDistance) {
                System.out.println(String.format("Path longer than total distance. %f, %f", pathLength, totalDistance));
            }
        }
        return new MatchedTrajectory(trajectory, edgeWithTimes.subList(1, edgeWithTimes.size()));
    }

    /**
     * Connects nodes that are not directly connected using shortest path.
     *
     * @param edges
     * @return
     */
    private Walk buildWalk(List<Edge> edges) {
        Walk walk = new Walk();
        int edgeNum = edges.size();
        if (edgeNum == 0)
            throw new InvalidParameterException("Empty edge list.");
        DirectedEdge previousEdge = (DirectedEdge) edges.get(0);
        walk.add(previousEdge.getInNode());
        walk.add(previousEdge.getOutNode());
        for (int i = 1; i < edgeNum; ++i) {
            DirectedEdge currentEdge = (DirectedEdge) edges.get(i);
            if (currentEdge.equals(previousEdge))
                continue;
            if (!currentEdge.getInNode().equals(previousEdge.getOutNode())) {
                // Gets shortest path.
                Path path = graph.queryShortestPath(previousEdge.getOutNode(), currentEdge.getInNode());
                for (int j = 1; j < path.size(); ++j) {
                    walk.add(path.get(j));
                }
            }
            walk.add(currentEdge.getOutNode());
            previousEdge = currentEdge;
        }
        return walk;
    }

    private List<Edge> backtracking(HashMap<Long, HMMNode> T) {
        List<Edge> edges = new ArrayList<Edge>();
        // Finds the path with maximum probability.
        HMMNode maxNode = null;
        double maxVal = Double.NEGATIVE_INFINITY;
        for (Map.Entry<Long, HMMNode> pair : T.entrySet()) {
            if (pair.getValue().getProb() > maxVal) {
                maxVal = pair.getValue().getProb();
                maxNode = pair.getValue();
            }
        }
        // backtracking.
        while (maxNode != null) {
            edges.add(graph.getEdge(maxNode.getEdgeId()));
            maxNode = maxNode.getParent();
        }
        Collections.reverse(edges);
        return edges;
    }
}
