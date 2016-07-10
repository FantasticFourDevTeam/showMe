package com.example.FundigoApp.Customer.Social;

import com.parse.ParseGeoPoint;

public class MipoUser {

    String picUrl;
    String name;
    String userPhone;
    ParseGeoPoint userLocation;
    double dist;


    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public ParseGeoPoint getUserLocation() {
        return userLocation;
    }

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    public void setUserLocation(ParseGeoPoint userLocation) {
        this.userLocation = userLocation;
    }


    public MipoUser(String picUrl, String name,String userPhone) {

        this.picUrl = picUrl;
        this.name = name;
        this.userPhone = userPhone;

    }
}
