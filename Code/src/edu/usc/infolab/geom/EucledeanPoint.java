package edu.usc.infolab.geom;

import java.awt.geom.Point2D;

public class EucledeanPoint extends Point {
	private Point2D.Double _p;
	
	public EucledeanPoint(double x, double y) {
		super(x,y);
		_p = new Point2D.Double(x, y);
	}
	
	private EucledeanPoint(EucledeanPoint other) {
		super(other._p.x, other._p.y);
		_p.setLocation(other._p);
	}
	
	@Override
	public void Update(double x, double y) {
		super.Update(x,y);
		_p.setLocation(x, y);
	};

	@Override
	public double Distance(Point o) {
		EucledeanPoint other = (EucledeanPoint)o;
		return this._p.distance(other._p);
	}

	@Override
	public Point clone() {
		return new EucledeanPoint(this);
	}
}
