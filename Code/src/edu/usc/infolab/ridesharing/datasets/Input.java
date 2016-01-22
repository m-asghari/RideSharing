package edu.usc.infolab.ridesharing.datasets;

import java.util.ArrayList;

import edu.usc.infolab.geom.Point;
import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Request;

public abstract class Input<P extends Point, R extends Request<P>, D extends Driver<P, R>> {
	public ArrayList<R> requests;
	public ArrayList<D> drivers;
}
