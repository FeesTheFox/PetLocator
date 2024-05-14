package com.example.petlocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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
import com.google.android.material.snackbar.Snackbar;
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
    private static final double MAX_DISTANCE = 50;

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

        // Load pets data
        loadPetsData();

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

    private void createMapView() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
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

                            isNewMarkerAdded = true; // устанавливаем флаг добавления нового маркера
                        }
                    });
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Error creating map", Toast.LENGTH_SHORT).show();
        }
    }

    private BitmapDescriptor createMarkerIcon(String petName) {
        // Load the marker icon from resources
        VectorDrawableCompat vectorDrawable = VectorDrawableCompat.create(getResources(), R.drawable.img, null);
        if (vectorDrawable == null) {
            Log.e("ICON_LOAD", "Failed to load marker icon");
            return null;
        }

        // Create a bitmap with the pet name
        Bitmap textBitmap = createTextBitmap(petName);

        // Create a new bitmap with a larger size, and copy the icon and text into it
        int bitmapSize = 100;
        Bitmap bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, bitmapSize, bitmapSize);
        vectorDrawable.draw(canvas);
        int textMargin = 5;
        int textX = (bitmapSize - textBitmap.getWidth()) / 2;
        int textY = bitmapSize - textBitmap.getHeight() - textMargin;
        canvas.drawBitmap(textBitmap, textX, textY, null);

        // Return the BitmapDescriptor for the marker icon
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private Bitmap createTextBitmap(String text) {
        // Create a Paint object with the desired text properties
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(16);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);

        // Measure the text width and height
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int textWidth = bounds.width();
        int textHeight = bounds.height();

        // Create a new bitmap with the desired size
        int bitmapSize = 100;
        Bitmap bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888);

        // Draw the text on the bitmap
        Canvas canvas = new Canvas(bitmap);
        int textX = bitmapSize / 2;
        int textY = (bitmapSize + textHeight) / 2;
        canvas.drawText(text, textX, textY, paint);

        // Return the bitmap
        return bitmap;
    }

    private void showSnackbar(String petName) {
        Snackbar.make(findViewById(android.R.id.content), petName + " is too far away!", Snackbar.LENGTH_LONG).show();
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
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(newLatitude, newLongitude));

                // Create a custom marker icon with pet's name
                BitmapDescriptor markerIcon = createMarkerIcon(pet.getpetName());
                markerOptions.icon(markerIcon);

                Marker petMarker = mMap.addMarker(markerOptions);
                petMarkers.add(petMarker);

                // Update pet's location
                pet.setLatitude(newLatitude);
                pet.setLongitude(newLongitude);

                // Set the pet's name as the marker's title
                petMarker.setTitle(pet.getpetName());
            }
        }
    }



    //method for pets to move
    private void startMovingPets() {
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Checks, if all the pets are on map
                if (petMarkers.size() >= pets.size()) {
                    List<Marker> newPetMarkers = new ArrayList<>(); // Creates a new list of pet's markers

                    // Update pets' locations
                    for (int i = 0; i < pets.size(); i++) {
                        Pet pet = pets.get(i);
                        double newLatitude = pet.getLatitude() + Math.random() * 0.001 - 0.0005;
                        double newLongitude = pet.getLongitude() + Math.random() * 0.001 - 0.0005;
                        pet.setLatitude(newLatitude);
                        pet.setLongitude(newLongitude);

                        // Update pet's marker position
                        LatLng petLocation = new LatLng(pet.getLatitude(), pet.getLongitude());
                        Marker petMarker = petMarkers.get(i);
                        petMarker.setPosition(petLocation);
                        newPetMarkers.add(petMarker); // Adding an updated marker into list of pet markers

                        // Checks the range between user's marker and pet's marker
                        if (userMarker != null) {
                            Location userLocation = new Location("");
                            userLocation.setLatitude(userMarker.getPosition().latitude);
                            userLocation.setLongitude(userMarker.getPosition().longitude);

                            Location petLocationObj = new Location("");
                            petLocationObj.setLatitude(pet.getLatitude());
                            petLocationObj.setLongitude(pet.getLongitude());

                            float distance = userLocation.distanceTo(petLocationObj);
                            if (distance > MAX_DISTANCE) {
                                showSnackbar(pet.getpetName());
                            }
                        }
                    }

                    petMarkers = newPetMarkers; // Updating an marker list with a new list

                    // Repeat this every 1 second
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(runnable);
    }

    //method for loading data from database
    private void loadPetsData() {
        // Get the current user's UID
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Create a reference to the current user's node in the database
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserUid);

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
}



