package edu.usc.infolab.ridesharing.datasets;

import java.util.ArrayList;

import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Request;

public abstract class Input<R extends Request, D extends Driver<R>> {
	public ArrayList<R> requests;
	public ArrayList<D> drivers;
}
