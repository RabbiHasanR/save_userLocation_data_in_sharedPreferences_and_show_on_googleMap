package com.example.android.memorableplace;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> places;
    static  ArrayList<LatLng> location;
    static ArrayAdapter arrayAdapter;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        places=new ArrayList<String>();
        location=new ArrayList<LatLng>();
        places.clear();
        location.clear();
        getData();
        ListView listView=(ListView)findViewById(R.id.listView);
        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,places);
        listView.setAdapter(arrayAdapter);

        //pass listview index main activity to maps activity
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent=new Intent(getApplicationContext(),MapsActivity.class);
                    intent.putExtra("index",i);
                    startActivity(intent);

            }
        });
    }

    public void getData(){
        SharedPreferences sharedPreferences=this.getSharedPreferences("com.example.android.memorableplace",Context.MODE_PRIVATE);
        ArrayList<String> latitude=new ArrayList<>();
        ArrayList<String> longitude=new ArrayList<>();

        //clear all array list for beggening
        latitude.clear();
        longitude.clear();
        try {
            //get places,latitude and longitude string from sharedPreference
            places=(ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("places",ObjectSerializer.serialize(new ArrayList<String>())));
            latitude=(ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lat",ObjectSerializer.serialize(new ArrayList<String>())));
            longitude=(ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lon",ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(places.size()>0&&latitude.size()>0&&longitude.size()>0){
            if(places.size()==latitude.size() && places.size()==longitude.size()){
                for(int i=0;i<latitude.size();i++){
                    //convert latitude and longitude string to double and add to location ArrayList
                    location.add(new LatLng(Double.parseDouble(latitude.get(i)),Double.parseDouble(longitude.get(i))));
                }
            }
        }
        else {
            places.add("Add memorable places...");
            location.add(new LatLng(0,0));
        }
    }


}
