package edu.usc.infolab.ridesharing;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Time implements Comparable<Time>{
	private static final double MillisInMinute = 60 * 1000;
	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Calendar _cal;
	
	private Time(Time t){
		_cal = Calendar.getInstance();
		_cal.setTime(t._cal.getTime());
	}
	
	
	public Time(Date d) {
		_cal = Calendar.getInstance();
		_cal.setTime(d);
	}
	
	public Time() {
		_cal = Calendar.getInstance();
	}
	
	public void SetTime(Time t) {
		_cal.setTime(t._cal.getTime());
	}
	
	public void SetTime(Date d) {
		_cal.setTime(d);
	}
	
	public Date GetTime() {
		return _cal.getTime();
	}
	
	public void Add(int field, int value) {
		_cal.add(field, value);
	}
	
	public void Add(int value) {
		this.Add(Calendar.MINUTE, value);
	}

	@Override
	public int compareTo(Time o) {
		return _cal.compareTo(o._cal);
	}
	
	public Time clone(){
		return new Time(this);
	}
	
	public int SubtractMillis(Time t) {
		return (int)(this._cal.getTimeInMillis() - t._cal.getTimeInMillis());
	}

	public int Subtract(Time t) {
		return this.getTimeInMinutes() - t.getTimeInMinutes();
	}
	
	public int Subtract(int minutes) {
		return this.getTimeInMinutes() - minutes;
	}
	
	private int getTimeInMinutes() {
		return (int)(this._cal.getTimeInMillis() / MillisInMinute);
	}
	
	@Override
	public String toString() {
		return Time.sdf.format(this._cal.getTime());
	}
	
}
