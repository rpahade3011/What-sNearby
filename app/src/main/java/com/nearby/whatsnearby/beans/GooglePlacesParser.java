package com.nearby.whatsnearby.beans;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GooglePlacesParser{
    String JSON;
    ArrayList<GooglePlacesBean> placesList = new ArrayList<>();

    public GooglePlacesParser(String JSON){
        this.JSON = JSON;
    }

    public ArrayList<GooglePlacesBean> getPlaces(){
        placesList = new ArrayList<>();

        try {
            JSONObject main = new JSONObject(JSON);
            JSONArray predictions = main.getJSONArray("predictions");
            for(int i = 0; i < predictions.length(); i++){
                GooglePlacesBean bean = new GooglePlacesBean();
                JSONObject prediction = predictions.getJSONObject(i);
                String description = prediction.getString("description");
                String placeId = prediction.getString("place_id");
                bean.setDescription(description);
                bean.setPlaceId(placeId);
                placesList.add(bean);
            }
        }catch (JSONException ex){
            Log.e("GooglePlacesParser", "Error while parsing", ex);
        }
        return placesList;
    }
}