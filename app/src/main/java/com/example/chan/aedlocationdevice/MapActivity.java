package com.example.chan.aedlocationdevice;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,NavigationView.OnNavigationItemSelectedListener  {


    private static final String TAG = "MapActivity";
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float zoomDefault = 15f;
    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocation;
    private LatLng testAEDPos = new LatLng(52.2443069,-7.0955176);
    private String testAED = "Test AED";
    private LatLng getTestAEDPos1 = new LatLng(52.2473024,-7.095067);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mDrawerLayout =(DrawerLayout) findViewById(R.id.drawer_layout1);
        mToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.open,R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
          NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view1);
        if (mNavigationView != null) {
            mNavigationView.setNavigationItemSelectedListener(this);
        }

        mLocationPermissionsGranted = getLocPermissions();
        if (mLocationPermissionsGranted == true) {
            initMap();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;



        if (mLocationPermissionsGranted) {
            getUserLoc();
            addAEDLoc(testAEDPos,testAED); //testing
            addAEDLoc(getTestAEDPos1,testAED);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);


        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.nav_maps){
             Toast.makeText(this, "hello change",Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, MapActivity.class);
            startActivity(i);
        }
        if( id == R.id.nav_home){
            Toast.makeText(this, "hello main change",Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
        return false;

    }
    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }
    private void addAEDLoc(LatLng AEDpos,String title){
        MarkerOptions options = new MarkerOptions()
                .position(AEDpos)
                .title(title);
        mMap.addMarker(options);
    }
    private void getUserLoc(){
        Log.d(TAG , "getDeviceLoc: getting user current Location");
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){
                Task loc = mFusedLocation.getLastLocation();
                loc.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "Location found");
                            Location currentLoc = (Location) task.getResult();
                            LatLng coordinates = new LatLng(currentLoc.getLatitude(),currentLoc.getLongitude());
                            moveCam(zoomDefault,coordinates);

                        }
                        else{
                            Log.d(TAG, "Current Location not found");
                            Toast.makeText(MapActivity.this, "Cant find you at this moment",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        catch (SecurityException error){
            Log.e(TAG,"getUserLoc: SecurityException: "+ error.getMessage());
        }

    }
    private void moveCam( float zoom,LatLng pos){
        Log.d(TAG,"moving to new position");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos,zoom));
    }
    private boolean getLocPermissions(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            return true;
        }
        else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){ //change to for each
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    initMap();
                }
            }
        }
    }


}
