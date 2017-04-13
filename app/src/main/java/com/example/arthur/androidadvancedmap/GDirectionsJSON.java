package com.example.arthur.androidadvancedmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by arthur on 01.10.16.
 */

public class GDirectionsJSON {

    private JSONObject responseJSON;

    public GDirectionsJSON(JSONObject responseJSON) {
        this.responseJSON = responseJSON;
    }

    public JSONObject getResponseJSON() {
        return responseJSON;
    }

    public PolylineOptions getPolyline() throws JSONException {
        //get overview_polyline from response JSONObject
        JSONArray routesArray = (JSONArray) responseJSON.get("routes");
        JSONObject overviewPolyline = routesArray.getJSONObject(0).getJSONObject("overview_polyline");
        String points = overviewPolyline.getString("points");

        //decode list of points
        List<LatLng> latLngs = PolyUtil.decode(points);

        //return PolylineOption with LatLngs
        return new PolylineOptions().addAll(latLngs);
    }

}
