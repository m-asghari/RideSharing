
package edu.usc.infolab.geom;

import edu.usc.infolab.ridesharing.Pair;
import edu.usc.infolab.ridesharing.Time;
import edu.usc.infolab.ridesharing.TimeDistancePair;
import edu.usc.infolab.ridesharing.Utils;
import edu.usc.infolab.shortestpath.ShortestPathUtil;

public class GPSPoint {
    // default speed (mile/minute)
    private static double defaultSpeed = 1; //~40 miles per hour

    public static int TravelTimeInMinutes(Double dist) {
        return (int)(dist/defaultSpeed);
    }
    
    public static int TravelTimeInMillis(Double dist) {
        return (int)((dist * Time.MillisInMinute)/defaultSpeed);
    }
    
    private double _lat;
    private double _lng;
    
    public GPSPoint(double lat, double lng) {
        this._lat = lat;
        this._lng = lng;
    }
    
    //TODO(mohammad): make private and use clone
    public GPSPoint(GPSPoint other) {
        this._lat = other._lat;
        this._lng = other._lng;
    }
    
    public void Update(double lat, double lng) {
        this._lat = lat;
        this._lng = lng;
    }
    
    public void Set(GPSPoint p) {
        this.Update(p._lat, p._lng);
    }
    
    public void MoveTowards(GPSPoint p, double length) {
        double dist = this.DistanceInMilesAndMillis(p).distance;
        if (dist < length)
            length = dist;
        Double deltaLat = length * (p._lat - this._lat) / dist;
        Double newLat = this._lat + deltaLat;
        Double deltaLng = length * (p._lng - this._lng) / dist;
        Double newLng = this._lng + deltaLng;
        this.Update(newLat, newLng);
    }
    
    public boolean In(double minLat, double maxLat, double minLng, double maxLng) {
        if (this._lat < minLat || this._lat > maxLat || this._lng < minLng || this._lng > maxLng)
            return false;
        return true;
    }
    
    //returned distance is in mile!
    //returned time in milliseconds
    public TimeDistancePair DistanceInMilesAndMillis(GPSPoint other) {
        switch (Utils.distanceType) {
            case Euclidean:
                return EuclideanDistanceInMilesAndMillis(other);
            case Network:
                return NetworkDistanceInMilesAndMillis(other);
        }
        // default
        return EuclideanDistanceInMilesAndMillis(other);
    }

    public TimeDistancePair EuclideanDistanceInMilesAndMillis(GPSPoint other) {
        double theta = this._lng - other._lng;
        double dist =
                Math.sin(deg2rad(this._lat)) * Math.sin(deg2rad(other._lat)) +
                        Math.cos(deg2rad(this._lat)) * Math.cos(deg2rad(other._lat)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return new TimeDistancePair(dist, ((dist*Time.MillisInMinute)/ defaultSpeed));
    }

    private TimeDistancePair NetworkDistanceInMilesAndMillis(GPSPoint other) {
        try {
            double distMeter = ShortestPathUtil.GetShortestPath(this._lat, this._lng, other._lat, other._lng);
            double distMiles = distMeter / 1600.;
            return new TimeDistancePair(distMiles, ((distMiles*Time.MillisInMinute)/defaultSpeed));
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Network Distance Threw Exception. Computing Euclidean");
            return EuclideanDistanceInMilesAndMillis(other);
        }
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
        return String.format("%f,%f", this._lat, this._lng);
    }

}