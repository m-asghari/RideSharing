package edu.usc.infolab.ridesharing;

import java.util.ArrayList;

import edu.usc.infolab.geom.Point;

public abstract class Driver<P extends Point, R extends Request<P>> {
	public P loc;
	public ArrayList<R> activeRequests;
	public ArrayList<R> servicedRequests;
	protected ArrayList<P> _schedule;
	
	protected double _travelledDistance;
	
	public Time start;
	public Time end;
	
	public void UpdateLocation(double length) {
		if (_schedule.isEmpty()) return;
		P dest = _schedule.get(0);
		double dist = loc.Distance(dest);
		if (dist > length) {
			MoveTowards(dest, length);
			UpdateTravelledDistance(length);
		} else {
			this.loc.Set(dest);
			
			NewPointUpdates();
			UpdateTravelledDistance(dist);
			
			if (_schedule.size() > 0) {
				UpdateLocation(length - dist);
			}
		}
	}
	
	private void MoveTowards(P dest, Double length) {
		loc.MoveTowards(dest, length);
	}
	
	protected void UpdateTravelledDistance(double length) {
		this._travelledDistance += length;
	}
	
	protected void NewPointUpdates() {
		_schedule.remove(0);
	}
	
	protected abstract void AddRequest(R r);
}
