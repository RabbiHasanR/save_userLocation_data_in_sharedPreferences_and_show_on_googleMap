package com.example.android.memorableplace;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback , GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;

    LocationManager locationManager;
    LocationListener locationListener;
    final int REQUEST_CODE=1;


    //get user location on map
    public void centerMapLocation(Location location, String title) {
        if(location!=null){
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12));
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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
        mMap.setOnMapLongClickListener(this);
        Intent intent = getIntent();
        //Toast.makeText(this, Integer.toString(intent.getIntExtra("index",1)), Toast.LENGTH_SHORT).show();

        //check get intent null or not and is it initial listview index
        if (intent.getIntExtra("index", 1) == 0) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    centerMapLocation(location, "Your location");
                    Log.i("Location",location.toString());
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };
            //get location permission from user device
            checkPermission();
        }
        else{
            //identify save location on map and show it
            Location placeLocation=new Location(LocationManager.GPS_PROVIDER);
            if(MainActivity.location.get(intent.getIntExtra("index",1))!=null && MainActivity.location.size()>0){
                placeLocation.setLatitude(MainActivity.location.get(intent.getIntExtra("index",1)).latitude);
                placeLocation.setLongitude(MainActivity.location.get(intent.getIntExtra("index",1)).longitude);

                centerMapLocation(placeLocation,MainActivity.places.get(intent.getIntExtra("index",1)));
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }else{
                    Toast.makeText(this, "Permission was denied 1", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Permission was denied 2", Toast.LENGTH_SHORT).show();
        }
        }


    //check for get location permission
    public void checkPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            //get user last known location
            Location lastKnownLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            centerMapLocation(lastKnownLocation,"Your location");
        }
    }

    //long press click on map and save location and place to listview
    @Override
    public void onMapLongClick(LatLng latLng) {

        Geocoder geocoder=new Geocoder(getApplicationContext(), Locale.getDefault());
        String address="";
        try{
            List<Address> listAddress=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);

            if(listAddress!=null && listAddress.size()>0){
                if(listAddress.get(0).getSubThoroughfare()!=null){
                    address+=listAddress.get(0).getSubThoroughfare()+" ";
                }
                address+=listAddress.get(0).getThoroughfare();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if(address.equals("")){
            SimpleDateFormat sdf=new SimpleDateFormat("HH:mm yyyy-MM-dd");
            address+=sdf.format(new Date());
        }
        mMap.addMarker(new MarkerOptions().position(latLng).title(address));
        //save location and place to listview
        MainActivity.places.add(address);
        MainActivity.location.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged();
        // save data to sharedPreference
        saveData(MainActivity.places,MainActivity.location);
        Toast.makeText(this, "Location saved!", Toast.LENGTH_SHORT).show();
    }

    //save data to sharedPreference
    public void saveData(ArrayList<String> places,ArrayList<LatLng> location){
        SharedPreferences sharedPreferences=this.getSharedPreferences("com.example.android.memorableplace",Context.MODE_PRIVATE);
        try {
            ArrayList<String> latitude=new ArrayList<>();
            ArrayList<String> longitude=new ArrayList<>();
            for(LatLng coord:location){
                latitude.add(Double.toString(coord.latitude));
                longitude.add(Double.toString(coord.longitude));
            }
            sharedPreferences.edit().putString("places",ObjectSerializer.serialize(places)).apply();
            sharedPreferences.edit().putString("lat",ObjectSerializer.serialize(latitude)).apply();
            sharedPreferences.edit().putString("lon",ObjectSerializer.serialize(longitude)).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
