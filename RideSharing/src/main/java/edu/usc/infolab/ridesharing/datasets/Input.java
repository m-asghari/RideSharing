package edu.usc.infolab.ridesharing.datasets;

import edu.usc.infolab.ridesharing.Driver;
import edu.usc.infolab.ridesharing.Request;

import java.util.ArrayList;

public abstract class Input<R extends Request, D extends Driver<R>> {
	public ArrayList<R> requests;
	public ArrayList<D> drivers;
}
