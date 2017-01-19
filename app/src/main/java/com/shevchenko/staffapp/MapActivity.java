package com.shevchenko.staffapp;

/**
 * Created by shevchenko on 2015-11-30.
 * This is the screen that shows the google map.
 * This map shows the user`s current position and all tasks` position in one screen.
 * so the user can confirm the task`s position.
 */

import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.shevchenko.staffapp.Common.Common;
import com.shevchenko.staffapp.Model.CompleteTask;
import com.shevchenko.staffapp.Model.LocationLoader;
import com.shevchenko.staffapp.Model.TaskInfo;

import java.util.ArrayList;


public class MapActivity extends FragmentActivity implements OnMapClickListener {

    private GoogleMap mMap;
    LocationLoader mLocationLoader;
    private Location mNewLocation;
    private ArrayList<Marker> makerList;
    private LatLng latLng;
    LatLngBounds bounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        makerList = new ArrayList<Marker>();
        MapsInitializer.initialize(getApplicationContext());
        GooglePlayServicesUtil.isGooglePlayServicesAvailable(MapActivity.this);
        mMap = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        if(mMap != null){
            init();
        }
        mLocationLoader = new LocationLoader(this, false);
        mLocationLoader.SetOnLoadEventListener(new LocationLoader.OnLoadEventListener() {
            @Override
            public void onLocationChanged(Location location) {
                mNewLocation = location;
                init();
                //mUpdateLocationHandler.sendEmptyMessage(0);
            }

            @Override
            public void onAddressChanged(String strAddress) {

            }

            @Override
            public void onError(int iErrorCode) {
                init();
            }
        });
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
                if (!Common.getInstance().latitude.equals("")) {
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,
                            20);
                    mMap.animateCamera(cu);
                }

            }
        });
        //mLocationLoader.Start();
        //init();

    }

    @Override
    public void onMapClick(LatLng point) {
        Point screenPt = mMap.getProjection().toScreenLocation(point);
        LatLng latLng = mMap.getProjection().fromScreenLocation(screenPt);


        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(mMap != null)
            init();
    }
    private void init() {
        if(Common.getInstance().latitude.equals(""))
            return;
        latLng = new LatLng(Double.parseDouble(Common.getInstance().latitude), Double.parseDouble(Common.getInstance().longitude));

        MarkerOptions optCur = new MarkerOptions();
        optCur.position(latLng);
        optCur.title("Current Position");
        optCur.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        makerList.add(mMap.addMarker(optCur));

        ArrayList<String> doubleList = new ArrayList<String >();
        for (int i = 0; i < Common.getInstance().arrIncompleteTasks.size(); i++){
            TaskInfo task = Common.getInstance().arrIncompleteTasks.get(i);
            LatLng lat = new LatLng(Double.parseDouble(task.getLatitude()), Double.parseDouble(task.getLongitude()));
            double distance = spacing(lat);
            doubleList.add(String.valueOf(distance));
            Log.d("Distance", String.valueOf(distance));

        }
        double minDis = 0.0;
        int id = 0;
        int count = 0;
        ArrayList<TaskInfo> taskList = new ArrayList<TaskInfo>();
        for(int i =0 ; i < Common.getInstance().arrIncompleteTasks.size(); i++)
            taskList.add(Common.getInstance().arrIncompleteTasks.get(i));

        ArrayList<TaskInfo> resultList = new ArrayList<TaskInfo>();
        if(doubleList.size() != 0) {
            do {
                minDis = Double.parseDouble(doubleList.get(0));
                for (int i = 0; i < doubleList.size(); i++) {
                    if (minDis >= Double.parseDouble(doubleList.get(i))) {
                        id = i;
                        minDis = Double.parseDouble(doubleList.get(i));
                    }
                }
                Log.d("MinDistance", String.valueOf(minDis));

                resultList.add(taskList.get(id));
                taskList.remove(id);
                doubleList.remove(id);
                if (taskList.size() == 0) break;
                count++;
            } while (count < 3);
        }

        for (int i = 0; i < resultList.size(); i++) {
            TaskInfo task = new TaskInfo();
            task = resultList.get(i);
            MarkerOptions optSecond = new MarkerOptions();
            //optSecond.position(new LatLng(latitude + 0.02, longitude + 0.02));
            optSecond.position(new LatLng(Double.parseDouble(task.getLatitude()), Double.parseDouble(task.getLongitude())));
            optSecond.icon(BitmapDescriptorFactory.fromResource(R.drawable.pending));
            //mMap.addMarker(optSecond).showInfoWindow();
            makerList.add(mMap.addMarker(optSecond));
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : makerList) {
            builder.include(marker.getPosition());
        }
        bounds = builder.build();
        int padding = 20; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,
                padding);
        //mMap.moveCamera(cu);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.animateCamera(cu);
        for (int i = 0; i < Common.getInstance().arrCompleteTasks.size(); i++) {
            CompleteTask task = new CompleteTask();
            task = Common.getInstance().arrCompleteTasks.get(i);
            MarkerOptions optSecond = new MarkerOptions();
            //optSecond.position(new LatLng(latitude + 0.02, longitude + 0.02));
            optSecond.position(new LatLng(Double.parseDouble(task.latitude), Double.parseDouble(task.longitude)));
            optSecond.icon(BitmapDescriptorFactory.fromResource(R.drawable.complete));
            //mMap.addMarker(optSecond).showInfoWindow();
            makerList.add(mMap.addMarker(optSecond));
        }
    }
    private double spacing(LatLng event) {
        double x = latLng.latitude - event.latitude;
        double y = latLng.longitude - event.longitude;
        //return FloatMath.sqrt(x * x + y * y);
        return Math.sqrt(x * x + y * y);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mLocationLoader.Stop();
    }
}

