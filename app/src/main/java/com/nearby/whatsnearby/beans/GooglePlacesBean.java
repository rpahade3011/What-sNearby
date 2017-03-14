package com.nearby.whatsnearby.beans;

import java.io.Serializable;

public class GooglePlacesBean implements Serializable {
    private String description;
    private String placeId;

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public String getPlaceId(){
        return placeId;
    }

    public void setPlaceId(String placeId){
        this.placeId = placeId;
    }
}
