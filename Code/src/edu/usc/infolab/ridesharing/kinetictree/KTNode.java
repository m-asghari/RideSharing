package edu.usc.infolab.ridesharing.kinetictree;

import java.util.ArrayList;

import edu.usc.infolab.geom.GPSPoint;
import edu.usc.infolab.ridesharing.Pair;


public class KTNode {
	public enum Type {
		Root,
		Source,
		Destination
	}
	
	public GPSPoint loc;
	public ArrayList<KTNode> next;
	public Type type;
	public KTRequest request;
	
	//public ArrayList<KTRequest> allActive;
	//public ArrayList<KTRequest> activeRequested;
	//public ArrayList<KTRequest> activePickedUp;
	//public HashMap<KTRequest, Double> distanceSinceRequest;
	//public HashMap<KTRequest, Double> distanceSincePickedUp;
	
	public double delta;
	public double Delta;
	
	public KTNode() {
		Initialize();
	}
	
	public KTNode(GPSPoint l, Type t, KTRequest r) {
		Initialize();
		this.loc = (GPSPoint)l.clone();
		this.type = t;
		this.request = r;
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
		this.loc = (GPSPoint)other.loc.clone();
		this.next = new ArrayList<KTNode>(other.next);
		this.type = other.type;
		this.request = other.request;
		//allActive = new ArrayList<KTRequest>(other.allActive);
		//activeRequested = new ArrayList<KTRequest>(other.activeRequested);
		//activePickedUp = new ArrayList<KTRequest>(activePickedUp);
	}
	
	public Pair<Double, Double> Distance(KTNode other) {
		return this.loc.Distance(other.loc);
	}
	
	public KTNode clone(){
		return new KTNode(this);
	}
}
