package com.example.arthur.androidadvancedmap;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by arthur on 01.10.16.
 */

public class DirectionsObject {

    LatLng startPoint;
    LatLng endPoint;
    private static final String API_KEY = "AIzaSyASiLrO1twQzYSYgz7ynd4Smn87wmNgKs8";

    public DirectionsObject(LatLng startPoint, LatLng endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    public LatLng getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(LatLng endPoint) {
        this.endPoint = endPoint;
    }

    public LatLng getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(LatLng startPoint) {
        this.startPoint = startPoint;
    }

    public String getURLString() {
        //build URL
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("https://maps.googleapis.com/maps/api/directions/json?");
        urlBuilder.append("origin="+startPoint.latitude+","+startPoint.longitude);
        urlBuilder.append("&destination="+endPoint.latitude+","+endPoint.longitude);
        urlBuilder.append("&mode=walking");
        urlBuilder.append("&key="+API_KEY);
        return urlBuilder.toString();
    }
}
