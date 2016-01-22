package edu.usc.infolab.geom;

public class GPSPoint extends Point {
	private double _lat;
	private double _lng;
	
	public GPSPoint(double lat, double lng) {
		super(lat,lng);
		this._lat = lat;
		this._lng = lng;
	}
	
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
	
	//returned distance is in mile!
	@Override
	public double Distance(Point o) {
		GPSPoint other = (GPSPoint)o;
		double theta = this._lng - other._lng;
		double dist = 
				Math.sin(deg2rad(this._lat)) * Math.sin(deg2rad(other._lat)) +
				Math.cos(deg2rad(this._lat)) * Math.cos(deg2rad(other._lat)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		return dist;
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
	public Point clone() {
		return new GPSPoint(this);
	}

}
