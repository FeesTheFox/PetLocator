package com.example.petlocator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.petlocator.databinding.ActivityMap2Binding;
import com.example.petlocator.databinding.ActivityMapBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

public class MapActivity2 extends AppCompatActivity {
    GoogleMap googleMap;

    private MediaPlayer mediaPlayer;
    private SwitchCompat musicSwitch;
    ActivityMap2Binding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMap2Binding.inflate(getLayoutInflater()); //work with binding
        View view = binding.getRoot();
        setContentView(view);

        createMapView(); //initializes map

        mediaPlayer = MediaPlayer.create(this,R.raw.menu_music); //creates music
        mediaPlayer.setLooping(true); //sets music on the loop

        musicSwitch = findViewById(R.id.musicSwitch);

        //turns music on and off
        musicSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    mediaPlayer.start();
                } else {
                    mediaPlayer.pause();
                }
            }
        });

        binding.profile.setOnClickListener(new View.OnClickListener() { //profile button
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                Intent intent1 = new Intent(MapActivity2.this, UserActivity.class); //transfers info
                intent1.putExtra("Email", intent.getStringExtra("Email"));
                intent1.putExtra("Name", intent.getStringExtra("Name"));
                intent1.putExtra("Role",intent.getStringExtra("Role"));
                startActivity(intent1);
            }
        });

        binding.info.setOnClickListener(new View.OnClickListener() { //info button
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity2.this, InfoActivity.class);
                startActivity(intent);
            }
        });
    }

    //creates map on a view
    private void createMapView() { //map
        MapView mapView = findViewById(R.id.mapView2);
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    // Your code for working with googleMap
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Error creating map", Toast.LENGTH_SHORT).show();
        }
    }

}