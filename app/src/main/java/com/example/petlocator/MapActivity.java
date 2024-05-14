package com.example.petlocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    GoogleMap mMap;
    List<Pet> pets;
    Marker userMarker;
    List<Marker> petMarkers;

    ActivityMapBinding binding;
    private DatabaseReference userRef;
    private String currentUserEmail;

    private Handler handler = new Handler();
    private Runnable runnable;
    private boolean isNewMarkerAdded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater()); //work with binding
        View view = binding.getRoot();
        setContentView(view);

        // Get the current user's UID
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Create a reference to the current user's node in the database
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserUid);

        // Initialize pets list
        pets = new ArrayList<>();
        petMarkers = new ArrayList<>();

        // Refresh pet data every 5 seconds
        runnable = new Runnable() {
            @Override
            public void run() {
                pets.clear(); // очищаем список питомцев
                for (Marker marker : petMarkers) {
                    marker.remove(); // удаляем все маркеры питомцев с карты
                }
                petMarkers.clear(); // очищаем список маркеров питомцев

                if (currentUserUid != null && !currentUserUid.isEmpty()) {
                    currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userDataSnapshot) {
                            if (userDataSnapshot.exists()) {
                                User user = userDataSnapshot.getValue(User.class);

                                if (userDataSnapshot.hasChild("pets")) {
                                    DataSnapshot petsSnapshot = userDataSnapshot.child("pets");
                                    for (DataSnapshot petSnapshot : petsSnapshot.getChildren()) {
                                        Pet pet = petSnapshot.getValue(Pet.class);
                                        pets.add(pet);
                                    }
                                }

                                Log.d("Doggies", "Pets of user: " + pets.toString());
                            } else {
                                Log.d("UserActivity", "User data not found");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.w("UserActivity", "loadDogs:onCancelled: ", error.toException());
                        }
                    });
                }
                handler.postDelayed(this, 5000); // 5000 milliseconds = 5 seconds
            }
        };
        handler.post(runnable);

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
                    mMap = googleMap;

                    // Add a listener for map clicks
                    mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            // Remove previous user marker
                            if (userMarker != null) {
                                userMarker.remove();
                            }

                            // Add new user marker
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng);
                            userMarker = mMap.addMarker(markerOptions);

                            // Remove previous pet markers
                            for (Marker marker : petMarkers) {
                                marker.remove();
                            }
                            petMarkers.clear();

                            // Add markers for pets around user marker
                            addPetsAroundUserMarker(latLng);

                            // Start moving pets
                            startMovingPets();
                        }
                    });
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Error creating map", Toast.LENGTH_SHORT).show();
        }
    }

    private void addPetsAroundUserMarker(LatLng userLocation) {
        for (Pet pet : pets) {
            // Generate random location around user location
            double newLatitude = userLocation.latitude + Math.random() * 0.001 - 0.0005;
            double newLongitude = userLocation.longitude + Math.random() * 0.001 - 0.0005;

            // Check if new location is within map bounds
            LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            if (bounds.contains(new LatLng(newLatitude, newLongitude))) {
                // Add marker for pet
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(newLatitude, newLongitude)).title(pet.getpetName());
                Marker petMarker = mMap.addMarker(markerOptions);
                petMarkers.add(petMarker);

                // Update pet's location
                pet.setLatitude(newLatitude);
                pet.setLongitude(newLongitude);
            }
        }
    }

    private void startMovingPets() {
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Проверяем, что все питомцы уже добавлены на карту
                if (petMarkers.size() >= pets.size()) {
                    List<Marker> newPetMarkers = new ArrayList<>(); // Создаем новый список маркеров питомцев

                    // Update pets' locations
                    for (int i = 0; i < pets.size(); i++) {
                        Pet pet = pets.get(i);
                        // Update pet's location here. You can use some randomization to make the movement chaotic.
                        // For example:
                        double newLatitude = pet.getLatitude() + Math.random() * 0.001 - 0.0005;
                        double newLongitude = pet.getLongitude() + Math.random() * 0.001 - 0.0005;
                        pet.setLatitude(newLatitude);
                        pet.setLongitude(newLongitude);

                        // Update pet's marker position
                        LatLng petLocation = new LatLng(pet.getLatitude(), pet.getLongitude());
                        Marker petMarker = petMarkers.get(i);
                        petMarker.setPosition(petLocation);
                        newPetMarkers.add(petMarker); // Добавляем обновленный маркер в новый список маркеров питомцев
                    }

                    petMarkers = newPetMarkers; // Обновляем список маркеров питомцев новым списком

                    // Repeat this every 1 second
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(runnable);
    }
}

