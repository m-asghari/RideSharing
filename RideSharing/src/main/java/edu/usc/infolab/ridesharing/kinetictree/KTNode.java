package edu.usc.infolab.ridesharing.kinetictree;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Request;
import edu.usc.infolab.ridesharing.TimeDistancePair;

import java.util.ArrayList;


public class KTNode extends GPSNode {
	
	public ArrayList<KTNode> next;
	
	//public ArrayList<Request> allActive;
	//public ArrayList<Request> activeRequested;
	//public ArrayList<Request> activePickedUp;
	//public HashMap<Request, Double> distanceSinceRequest;
	//public HashMap<Request, Double> distanceSincePickedUp;
	
	public double delta;
	public double Delta;
	public TimeDistancePair toParent;
	
	public KTNode() {
		super();
		Initialize();
	}
	
	public KTNode(GPSPoint point, Type type, Request request) {
		super(point, type, request);
		Initialize();
	}
	
	private void Initialize() {
		this.delta = 0;
		this.Delta = 0;
		this.toParent = new TimeDistancePair(0., 0.);
		this.next = new ArrayList<>();
		//allActive = new ArrayList<Request>();
		//activeRequested = new ArrayList<Request>();
		//activePickedUp = new ArrayList<Request>();
		//distanceSinceRequest = new HashMap<Request, Double>();
		//distanceSincePickedUp = new HashMap<Request, Double>();
	}
	
	protected KTNode(KTNode other) {
		this.point = other.point.clone();
		this.next = new ArrayList<>();
		for (KTNode otherNode : other.next) {
			this.next.add(otherNode.clone());
		}
		this.type = other.type;
		this.request = (other.type == Type.root) ? null : other.request;
		this.delta = other.delta;
		this.Delta = other.Delta;
		this.toParent = new TimeDistancePair(other.toParent.distance, other.toParent.time);
		//allActive = new ArrayList<Request>(other.allActive);
		//activeRequested = new ArrayList<Request>(other.activeRequested);
		//activePickedUp = new ArrayList<Request>(activePickedUp);
	}
	
	@Override
	public KTNode clone(){
		return new KTNode(this);
	}
}
