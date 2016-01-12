package edu.usc.infolab.ridesharing;

import java.util.Calendar;

public class Time implements Comparable<Time>{
	private Calendar _cal;
	
	private Time(Time t){
		_cal = Calendar.getInstance();
		_cal.setTime(t._cal.getTime());
	}
	
	public Time() {
		_cal = Calendar.getInstance();
	}
	
	public void Add(int field, int value) {
		_cal.add(field, value);
	}

	@Override
	public int compareTo(Time o) {
		return _cal.compareTo(o._cal);
	}
	
	@Override
	public Object clone(){
		return new Time(this);
	}

	public int Subtract(Time t) {
		return (int)(this._cal.getTimeInMillis() - t._cal.getTimeInMillis());
	}
	
}
