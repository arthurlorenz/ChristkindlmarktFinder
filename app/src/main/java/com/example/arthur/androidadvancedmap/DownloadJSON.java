package com.example.arthur.androidadvancedmap;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by arthur on 01.10.16.
 */

public class DownloadJSON extends AsyncTask<String, Void, JSONObject> {

    private IOnDownloadCompleted listener;

    public DownloadJSON(IOnDownloadCompleted listener) {
        this.listener = listener;
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        try {
            URL url = new URL(strings[0]);
            InputStream inputStream = url.openConnection().getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
            return jsonObject;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        try {
            listener.handleJSON(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Interface to handle JSONObject after downloading it
     */
    public interface IOnDownloadCompleted {
        void handleJSON(JSONObject jsonObject) throws JSONException;
    }
}
