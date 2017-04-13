package com.example.arthur.androidadvancedmap;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by arthur on 10.10.16.
 */

public class Christkindlmarkt {

    private LatLng position;
    private String name;
    private String adresse;
    private String datum;
    private String oeffnungszeit;
    private boolean isSilvestermarkt;

    public Christkindlmarkt(LatLng position, String name, String adresse, String datum, String oeffnungszeit, boolean isSilvestermarkt) {
        this.position = position;
        this.name = name;
        this.adresse = adresse;
        this.datum = datum;
        this.oeffnungszeit = oeffnungszeit;
        this.isSilvestermarkt = isSilvestermarkt;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public String getOeffnungszeit() {
        return oeffnungszeit;
    }

    public void setOeffnungszeit(String oeffnungszeit) {
        this.oeffnungszeit = oeffnungszeit;
    }


    public boolean isSilvestermarkt() {
        return isSilvestermarkt;
    }

    public void setSilvestermarkt(boolean silvestermarkt) {
        isSilvestermarkt = silvestermarkt;
    }
}
