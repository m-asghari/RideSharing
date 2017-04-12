package edu.usc.infolab.geom;

import edu.usc.infolab.ridesharing.TimeDistancePair;

import java.awt.geom.Point2D;

public class EuclideanPoint extends Point {
	
	private static Double defaultSpeed = 1.;
	
	private Point2D.Double _p;
	
	public EuclideanPoint(double x, double y) {
		super(x,y);
		_p = new Point2D.Double(x, y);
	}
	
	private EuclideanPoint(EuclideanPoint other) {
		super(other._p.x, other._p.y);
		_p.setLocation(other._p);
	}
	
	@Override
	public void Update(double x, double y) {
		super.Update(x,y);
		_p.setLocation(x, y);
	}

	@Override
	public TimeDistancePair Distance(Point o) {
		EuclideanPoint other = (EuclideanPoint)o;
		double dist = this._p.distance(other._p);
		return new TimeDistancePair(dist, dist/defaultSpeed);
	}

	@Override
	public Point clone() {
		return new EuclideanPoint(this);
	}
}
