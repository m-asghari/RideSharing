package edu.usc.infolab.geom;

import edu.usc.infolab.ridesharing.Pair;
import edu.usc.infolab.ridesharing.Request;

public class GPSNode{
	public enum Type {
		root,
		source,
		destination
	}
	
	public GPSPoint point;
	public Type type;
	public Request request;
	
	public GPSNode() {
		
	}
	
	public GPSNode(GPSPoint point, Type type, Request request) {
		this.point = point;
		this.type = type;
		this.request = request;
	}
	
	private GPSNode(GPSNode other) {
		this.point = other.point;
		this.type = other.type;
		this.request = other.request;
	}
	
	/*
	 * Return a pair of <Distance, Time> for the distance and travel time
	 * of the shortest path between this and p.
	 */
	public Pair<Double, Double> distance(GPSNode other) {
		return this.point.Distance(other.point);
	}
	
	@Override
	public GPSNode clone() {
		return new GPSNode(this);
	}
	
	@Override
	public String toString() {
		if (type == Type.root) {
			return "Root";
		}
		String ch = "?";
		switch (type) {
		case source:
			ch = "s";
			break;
		case destination:
			ch = "d";
			break;
		case root:
			ch = "?";
			break;
		}
		return String.format("%s%d", ch, request.id);
	}
	
	

}
