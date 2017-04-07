package edu.usc.infolab.geom;

import edu.usc.infolab.ridesharing.Pair;

public abstract class Point {
	private double _a;
	private double _b;
	
	public Point(double a, double b) {
		this._a = a;
		this._b = b;
	}
	
	public void Set(Point p) {
		this.Update(p._a, p._b);
	}
	
	public void Update(double a, double b) {
		this._a = a;
		this._b = b;
	}
	
	public void MoveTowards(Point p, double length) {
		Double dist = this.Distance(p).First;
		if (dist < length)
			length = dist;
		Double deltaA = length * (p._a - this._a) / dist;
		Double newA = this._a + deltaA;
		Double deltaB = length * (p._b - this._b) / dist;
		Double newB = this._b + deltaB;
		this.Update(newA, newB);
	}
	
	/*
	 * Return a pair of <Distance, Time> for the distance and travel time
	 * of the shortest path between this and p.
	 */
	public abstract Pair<Double, Double> Distance(Point p);
	
	@Override
	public abstract Point clone();
}
