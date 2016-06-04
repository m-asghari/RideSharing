package edu.usc.infolab.geom;

import edu.usc.infolab.ridesharing.Pair;

public class GPSPoint extends Point {	
	// default speed (mile/minute)
	private static double defaultSpeed = 1; //~40 miles per hour
	
	public static int TravelTimeInMinutes(Double dist) {
		return (int)(dist/defaultSpeed);
	}
	
	private double _lat;
	private double _lng;
	
	public GPSPoint(double lat, double lng) {
		super(lat,lng);
		this._lat = lat;
		this._lng = lng;
	}
	
	//TODO(mohammad): make private and use clone
	public GPSPoint(GPSPoint other) {
		super(other._lat, other._lng);
		this._lat = other._lat;
		this._lng = other._lng;
	}
	
	@Override
	public void Update(double lat, double lng) {
		super.Update(lat, lng);
		this._lat = lat;
		this._lng = lng;
	};
	
	public boolean In(double minLat, double maxLat, double minLng, double maxLng) {
		if (this._lat < minLat || this._lat > maxLat || this._lng < minLng || this._lng > maxLng)
			return false;
		return true;
	}
	
	//returned distance is in mile!
	@Override
	public Pair<Double, Double> Distance(Point o) {
		GPSPoint other = (GPSPoint)o;
		double theta = this._lng - other._lng;
		double dist = 
				Math.sin(deg2rad(this._lat)) * Math.sin(deg2rad(other._lat)) +
				Math.cos(deg2rad(this._lat)) * Math.cos(deg2rad(other._lat)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		return new Pair<Double, Double>(dist, dist/defaultSpeed);
	}
	
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts decimal degrees to radians             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double deg2rad(double deg) {
      return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double rad2deg(double rad) {
      return (rad * 180.0 / Math.PI);
    }

	@Override
	public GPSPoint clone() {
		return new GPSPoint(this);
	}
	
	@Override
	public String toString() {
		return String.format("lng: %.6f, lat: %.6f", this._lng, this._lat);
	}

}
