package edu.usc.infolab.ridesharing.kinetictree;

import java.util.ArrayList;

import edu.usc.infolab.geom.GPSNode;
import edu.usc.infolab.geom.GPSPoint;


public class KTNode extends GPSNode{
	
	public ArrayList<KTNode> next;
	
	//public ArrayList<KTRequest> allActive;
	//public ArrayList<KTRequest> activeRequested;
	//public ArrayList<KTRequest> activePickedUp;
	//public HashMap<KTRequest, Double> distanceSinceRequest;
	//public HashMap<KTRequest, Double> distanceSincePickedUp;
	
	public double delta;
	public double Delta;
	
	public KTNode() {
		super();
		Initialize();
	}
	
	public KTNode(GPSPoint point, Type type, KTRequest request) {
		super(point, type, request);
		Initialize();
	}
	
	private void Initialize() {
		this.delta = 0;
		this.Delta = 0;
		this.next = new ArrayList<KTNode>();
		//allActive = new ArrayList<KTRequest>();
		//activeRequested = new ArrayList<KTRequest>();
		//activePickedUp = new ArrayList<KTRequest>();
		//distanceSinceRequest = new HashMap<KTRequest, Double>();
		//distanceSincePickedUp = new HashMap<KTRequest, Double>();
	}
	
	protected KTNode(KTNode other) {
		this.point = other.point.clone();
		this.next = new ArrayList<KTNode>();
		for (KTNode otherNode : other.next) {
			this.next.add(otherNode.clone());
		}
		this.type = other.type;
		this.request = (other.type == Type.root) ? null : other.request;
		this.delta = other.delta;
		this.Delta = other.Delta;
		//allActive = new ArrayList<KTRequest>(other.allActive);
		//activeRequested = new ArrayList<KTRequest>(other.activeRequested);
		//activePickedUp = new ArrayList<KTRequest>(activePickedUp);
	}
	
	@Override
	public KTNode clone(){
		return new KTNode(this);
	}
}
