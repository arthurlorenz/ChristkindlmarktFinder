package com.example.arthur.androidadvancedmap.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.example.arthur.androidadvancedmap.DownloadJSON;
import com.example.arthur.androidadvancedmap.MapsActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;

/**
 * Created by arthur on 24.10.16.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String URL_MAERKTE = "http://data.wien.gv.at/daten/geo?service=WFS&request=GetFeature&version=1.1.0&typeName=ogdwien:ADVENTMARKTOGD&srsName=EPSG:4326&outputFormat=json";
    private Context context;

    // Define a variable to contain a content resolver instance
    ContentResolver mContentResolver;
    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
        this.context = context;
    }
    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        DownloadJSON.IOnDownloadCompleted onMarktDownloadCompleted = new DownloadJSON.IOnDownloadCompleted() {
            @Override
            public void handleJSON(JSONObject jsonObject) throws JSONException {
                //parses result JSON and sets marker
                writeJSONinFile(jsonObject);
                writeJSONinFile2(jsonObject);
            }
        };

        new DownloadJSON(onMarktDownloadCompleted).execute(URL_MAERKTE);

    }

    private void writeJSONinFile2(JSONObject jsonObject) {
        MapsActivity mapsActivity = new MapsActivity();
        mapsActivity.writeJSONinFile(jsonObject);
    }


    public void writeJSONinFile(JSONObject jsonObject) {

        if (jsonObject != null) {
            String filename = "jsonData";
            String string = jsonObject.toString();
            FileOutputStream outputStream;

            try {
                outputStream = this.context.openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ;
        }
    }
}
