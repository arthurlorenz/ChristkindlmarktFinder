package com.example.arthur.androidadvancedmap;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.PeriodicSync;
import android.content.SyncInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.arthur.androidadvancedmap.sync.StubProvider;
import com.example.arthur.androidadvancedmap.util.BitmapUtil;
import com.example.arthur.androidadvancedmap.util.MyLocationUtil;
import com.example.arthur.androidadvancedmap.util.PermissionUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LatLng currentPosition;
    private CameraPosition lastCameraPosition;
    private static final String URL_MAERKTE = "http://data.wien.gv.at/daten/geo?service=WFS&request=GetFeature&version=1.1.0&typeName=ogdwien:ADVENTMARKTOGD&srsName=EPSG:4326&outputFormat=json";
    private Polyline currentPolyline;
    private boolean currentPolylineIsVisible = false;
    private final List<LatLng> maerkteList = new ArrayList<>();


    // Constants
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.example.arthur.androidadvancedmap";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "arthur.example.com";
    // The account name
    public static final String ACCOUNT = "dummyaccount";
    // Instance fields
    Account mAccount;
    ContentResolver mResolver;


    private Button overviewButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS},1);
        }


        mAccount = CreateSyncAccount(this);
        if(mAccount == null)
            Log.d("Account", "null");

        // Get the content resolver for your app
        mResolver = getContentResolver();
        /*
         * Turn on periodic syncing
         */
        ContentResolver.addPeriodicSync(
                mAccount,
                AUTHORITY,
                Bundle.EMPTY,
                60*60);

        List<PeriodicSync> list = ContentResolver.getPeriodicSyncs(mAccount,AUTHORITY);
        Log.d("list",list.size()+"");
        for (PeriodicSync info: list) {
            Log.d("SyncInfo", info.toString());
        }

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        try {
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mapFragment.getMapAsync(this);

        setButtonClickListeners();

        checkPermissions();

        MyLocationUtil.LocationResult locationResult = new MyLocationUtil.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
            }
        };
        MyLocationUtil myLocation = new MyLocationUtil();
        myLocation.getLocation(this, locationResult);

        File fileDir = getFilesDir();
        File file = new File(fileDir.getAbsolutePath()+"/jsonData");
        Date lastModDate = new Date(file.lastModified());
        Log.d("fil","File last modified @ : "+ lastModDate.toString());
        Log.d("fil",fileDir.getAbsolutePath());

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            setUpChristkindlmaerkte();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //sets new click listeners defined in onMarkerClick() and onMapClick()
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);

        if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                moveCamera(maerkteList);
            }
        });
    }

    /**
     * gets polyline from Google Directions API
     * @param start startpoint of polyline
     * @param end endpoint of polyline
     */
    private void drawPolyline(LatLng start, LatLng end) {
        if (currentPolyline != null)
            currentPolyline.remove();

        DownloadJSON.IOnDownloadCompleted onDownloadCompleted = new DownloadJSON.IOnDownloadCompleted() {
            @Override
            public void handleJSON(JSONObject jsonObject) throws JSONException {
                GDirectionsJSON directionsJSON = new GDirectionsJSON(jsonObject);
                PolylineOptions polylineOptions = directionsJSON.getPolyline();
                if (mMap != null) {
                    currentPolyline = mMap.addPolyline(polylineOptions);
                    currentPolyline.setColor(Color.rgb(0, 121, 76));
                    currentPolylineIsVisible = true;
                }
            }
        };

        DirectionsObject dir1 = new DirectionsObject(start, end);

        new DownloadJSON(onDownloadCompleted).execute(dir1.getURLString());
    }

    /**
     * gets data from data.gv.at and adds marker
     */
    private void setUpChristkindlmaerkte() throws JSONException {
        JSONObject jsonObject = readJSONfromFile();
        if (jsonObject != null) {
            setMaerkteMarker(ChristkindlmarktParser.parseJSONToList(jsonObject));
        } else {
            DownloadJSON.IOnDownloadCompleted onMarktDownloadCompleted = new DownloadJSON.IOnDownloadCompleted() {
                @Override
                public void handleJSON(JSONObject jsonObject) throws JSONException {
                    //parses result JSON and sets marker
                    setMaerkteMarker(ChristkindlmarktParser.parseJSONToList(jsonObject));
                    writeJSONinFile(jsonObject);
                }
            };

            new DownloadJSON(onMarktDownloadCompleted).execute(URL_MAERKTE);
        }
    }

    /**
     * sets marker
     * @param christkindlmarktList
     */
    public void setMaerkteMarker(List<Christkindlmarkt> christkindlmarktList) {

        if (christkindlmarktList != null && !christkindlmarktList.isEmpty() && mMap != null) {

            Bitmap bitmap = BitmapUtil.getBitmap(getBaseContext(), R.drawable.ic_christbaum2);

            for (int i = 0; i < christkindlmarktList.size(); i++) {
                mMap.addMarker(new MarkerOptions()
                        .position(christkindlmarktList.get(i).getPosition())
                        .title(christkindlmarktList.get(i).getName())
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                );

                maerkteList.add(christkindlmarktList.get(i).getPosition());
            }
        }
    }

    /**
     * override listener to draw a polyline from current position to the clicked Christkindlmarkt
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        lastCameraPosition = mMap.getCameraPosition();
        LatLng end = marker.getPosition();
        drawPolyline(currentPosition, end);
        List<LatLng> latLngs = new ArrayList<>();
        latLngs.add(currentPosition);
        latLngs.add(end);
        moveCamera(latLngs);
        return true;
    }

    /**
     * checks if app has all permissions needed
     */
    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_COARSE_LOCATION, true);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (currentPolyline != null && currentPolylineIsVisible) {
            currentPolyline.remove();
            currentPolylineIsVisible = false;
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(lastCameraPosition));
        }
    }

    public void moveCamera(List<LatLng> latLngs) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : latLngs) {
            builder.include(latLng);
        }
        //set camera to see all Christkindlmärkte
        if (!latLngs.isEmpty())
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
    }

    public void setButtonClickListeners() {
        overviewButton = (Button) findViewById(R.id.overview_button);
        overviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!maerkteList.isEmpty())
                    moveCamera(maerkteList);
                else
                    makeToast("Märkte konnten nicht geladen werden!");
            }
        });
    }

    public JSONObject readJSONfromFile() {
        FileInputStream inputStream;
        StringBuilder sb = new StringBuilder();
        JSONObject jsonObject = null;

        try {
            inputStream = openFileInput("jsonData");
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String jsonString = String.valueOf(sb);
            Log.d("json", jsonString);
            if (jsonString != "")
                jsonObject = new JSONObject(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public void writeJSONinFile(JSONObject jsonObject) {

        if (jsonObject != null) {
            String filename = "jsonData";
            String string = jsonObject.toString();
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ;
        }
    }

    public void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            Log.d("account", "if");
            return newAccount;
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
            Log.d("account", "else");
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.d("account", "permission");
                return null;
            }
            return accountManager.getAccountsByType(ACCOUNT_TYPE)[0];
        }
        //return newAccount;
    }

}
