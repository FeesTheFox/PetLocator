package com.example.petlocator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.petlocator.databinding.ActivityMapBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MapActivity extends AppCompatActivity {

    GoogleMap mMap;

    ArrayList<Pet> dogsList;

    ActivityMapBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater()); //work with binding
        View view = binding.getRoot();
        setContentView(view);




        createMapView(); //initializes map

        binding.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                Intent intent1 = new Intent(MapActivity.this, UserActivity.class);


                String role = intent.getStringExtra("Role");
                if (role != null) {
                    intent1.putExtra("Role", role);
                } else {
                    Log.e("MapActivity", "Role is null or not found in intent extras");
                }
                intent1.putExtra("Email", intent.getStringExtra("Email"));
                intent1.putExtra("Name", intent.getStringExtra("Name"));
                intent1.putExtra("Role",intent.getStringExtra("Role"));
                startActivity(intent1);
            }
        });


        binding.info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, InfoActivity.class);
                startActivity(intent);
            }
        });
    }


    //work with map
    private void createMapView() {
        MapView mapView = findViewById(R.id.mapView);
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
//                    if (googleMap != null){
//                        for (Pet pet : dogsList){
//                            Marker marker = googleMap.addMarker(new MarkerOptions()
//                                    .position(new LatLng(pet.getLatitude(), pet.getLongitude()))
//                                    .title(pet.getpetName())
//                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
//                        }
//                    }
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Error creating map", Toast.LENGTH_SHORT).show();
        }
    }

}