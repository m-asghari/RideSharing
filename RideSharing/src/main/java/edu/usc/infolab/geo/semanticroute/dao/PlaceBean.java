package edu.usc.infolab.geo.semanticroute.dao;

import com.google.maps.model.LatLng;
import org.postgis.PGgeometry;

public class PlaceBean implements java.io.Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String place_id;
    private String name;
    private double lat;
    private double lng;
    private String address;
    /**
     * Types separated by "|".
     */
    private String types;
    private double score;
    private PGgeometry location;

    public LatLng getLatLng() {
        return new LatLng(lat, lng);
    }

    public String getLatLngString() {
        return getLatLng().toString();
    }

    public PGgeometry getLocation() {
        return location;
    }

    public void setLocation(PGgeometry location) {
        this.location = location;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }


    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String placeId) {
        this.place_id = placeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public PlaceBean() {
    }

    @Override
    public int hashCode() {
        return this.place_id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof PlaceBean) {
            result = ((PlaceBean) obj).place_id.equals(this.place_id);
        }
        return result;
    }
}
