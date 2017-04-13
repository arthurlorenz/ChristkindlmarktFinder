package com.example.arthur.androidadvancedmap;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arthur on 23.10.16.
 */

public class ChristkindlmarktParser {

    public static List<Christkindlmarkt> parseJSONToList(JSONObject jsonObject) throws JSONException {
        if(jsonObject != null) {
            JSONArray features = jsonObject.getJSONArray("features");
            List<Christkindlmarkt> christkindlmarktList = new ArrayList<>();
            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = (JSONObject) features.get(i);
                JSONObject geometry = (JSONObject) feature.get("geometry");
                JSONObject properties = (JSONObject) feature.get("properties");
                JSONArray coordinatesString = (JSONArray) geometry.get("coordinates");
                double[] coordinates = {
                        coordinatesString.getDouble(1),
                        coordinatesString.getDouble(0)
                };
                LatLng latLng = new LatLng(coordinates[0], coordinates[1]);
                christkindlmarktList.add(new Christkindlmarkt(
                        latLng,
                        (String) properties.get("BEZEICHNUNG"),
                        (String) properties.get("ADRESSE"),
                        (String) properties.get("DATUM"),
                        (String) properties.get("OEFFNUNGSZEIT"),
                        (((int) properties.get("SILVESTERMARKT")) == 1)
                ));
            }
            return christkindlmarktList;
        }
        return null;
    }

}
