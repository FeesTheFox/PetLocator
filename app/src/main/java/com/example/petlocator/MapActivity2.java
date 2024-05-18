package com.example.petlocator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.petlocator.databinding.ActivityMap2Binding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapActivity2 extends AppCompatActivity {
    GoogleMap mMap;
    List<Pet> pets;
    Marker userMarker;
    List<Marker> petMarkers;
    private DatabaseReference userRef;
    private String currentUserEmail;

    private Handler handler = new Handler();
    private Runnable runnable;

    private boolean isNewMarkerAdded = false;
    private static final double MAX_DISTANCE = 150;

    private MediaPlayer mediaPlayer;
    private SwitchCompat musicSwitch;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    ActivityMap2Binding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMap2Binding.inflate(getLayoutInflater()); //work with binding
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

    private void createMapView() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;

                    requestLocationPermission();
                    getDeviceLocation();

                    // Add a listener for map clicks
                    mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            if (!isNewMarkerAdded) {
                                return;
                            }

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

    private BitmapDescriptor createMarkerIcon(Pet pet, int index) {
        final BitmapDescriptor[] bitmapDescriptor = new BitmapDescriptor[1];

        Glide.with(this)
                .asBitmap()
                .load(pet.getImageResource())
                .apply(new RequestOptions().override(100, 100))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // Create a new bitmap with the pet name
                        Bitmap textBitmap = createTextBitmap(pet.getpetName());

                        // Create a new bitmap with a larger size, and copy the icon and text into it
                        int bitmapSize = 100;
                        Bitmap bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        canvas.drawBitmap(resource, 0, 0, null);
                        int textMargin = 5;
                        int textX = (bitmapSize - textBitmap.getWidth()) / 2;
                        int textY = bitmapSize - textBitmap.getHeight() - textMargin;
                        canvas.drawBitmap(textBitmap, textX, textY, null);

                        // Return the BitmapDescriptor for the marker icon
                        bitmapDescriptor[0] = BitmapDescriptorFactory.fromBitmap(bitmap);

                        if (petMarkers.size() > index) {
                            MapActivity.MarkerIconCallback callback = new MapActivity.MarkerIconCallback() {
                                @Override
                                public void onMarkerIconReady(BitmapDescriptor markerIcon) {
                                    // Update the marker icon
                                    Marker petMarker = petMarkers.get(index);
                                    petMarker.setIcon(markerIcon);
                                }
                            };
                            callback.onMarkerIconReady(bitmapDescriptor[0]);
                        }
                    }
                });

        return bitmapDescriptor[0];
    }




    private MapActivity.MarkerIconCallback markerIconCallback = new MapActivity.MarkerIconCallback() {
        @Override
        public void onMarkerIconReady(BitmapDescriptor markerIcon) {
            if (petMarkers.size() > 0) { // check if the list is not empty
                Marker petMarker = petMarkers.get(0); // get the first marker from the list
                petMarker.setIcon(markerIcon); // set the icon for the marker
                petMarkers.remove(0); // remove the marker from the list
            }
        }
    };

    interface MarkerIconCallback {
        void onMarkerIconReady(BitmapDescriptor markerIcon);
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
        petMarkers.clear(); // очищаем список petMarkers

        // добавляем маркеры в список petMarkers
        for (Pet pet : pets) {
            // Generate random location around user location
            double newLatitude = userLocation.latitude + Math.random() * 0.001 - 0.0005;
            double newLongitude = userLocation.longitude + Math.random() * 0.001 - 0.0005;

            // Check if new location is within map bounds
            LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            if (bounds.contains(new LatLng(newLatitude, newLongitude)) && !petMarkers.contains(pet)) {
                // Add marker for pet
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(newLatitude, newLongitude));

                // Create a custom marker icon with pet's name and image
                int index = pets.indexOf(pet);
                BitmapDescriptor markerIcon = createMarkerIcon(pet, index);
                markerOptions.icon(markerIcon);

                Marker petMarker = mMap.addMarker(markerOptions);
                petMarkers.add(petMarker); // Add the marker to the list

                // Update pet's location
                pet.setLatitude(newLatitude);
                pet.setLongitude(newLongitude);

                // Set the pet's name as the marker's title
                petMarker.setTitle(pet.getpetName());
            }
        }

        if (petMarkers.size() > 0) {


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
    void loadPetsData() {
        // Get the current user's UID
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Create a reference to the current user's node in the database
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserUid);

        // Initialize pets list
        pets = new ArrayList<>();
        petMarkers = new ArrayList<>();

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

                            // Add marker for pet
                            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(pet.getLatitude(), pet.getLongitude()));

                            // Create a custom marker icon with pet's name and image
                            int index = pets.indexOf(pet);
                            BitmapDescriptor markerIcon = createMarkerIcon(pet, index);
                            markerOptions.icon(markerIcon);

                            Marker petMarker = mMap.addMarker(markerOptions);
                            petMarkers.add(petMarker);

                            // Set the pet's name as the marker's title
                            petMarker.setTitle(pet.getpetName());
                        }
                    }

                    Log.d("Doggies", "Pets of user: " + pets.toString());

                    // Add markers for pets around user marker
                    if (userMarker != null) {
                        addPetsAroundUserMarker(userMarker.getPosition());
                    }
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

    //Geo location
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            enableMyLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void getDeviceLocation() {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    Log.d("MapActivity", "Current location: " + location.getLatitude() + ", " + location.getLongitude());

                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    // Remove previous user marker
                    if (userMarker != null) {
                        userMarker.remove();
                    }

                    // Add new user marker
                    MarkerOptions markerOptions = new MarkerOptions().position(currentLocation);
                    userMarker = mMap.addMarker(markerOptions);

                    // Remove previous pet markers
                    for (Marker marker : petMarkers) {
                        marker.remove();
                    }
                    petMarkers.clear();

                    isNewMarkerAdded = true; // sets flag of adding new marker

                    // Move camera to current location
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                }
            }
        });
    }
}