package com.example.petlocator;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private MediaPlayer mediaPlayer1;
    private SwitchCompat musicSwitch;
    private static final String PREF_NAME = "music_pref";
    private static final String PREF_KEY = "music_state";
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private List<Polyline> petPolylines;
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
        petPolylines = new ArrayList<>();

        // Load pets data
        loadPetsData();

        createMapView(); //initializes map

        mediaPlayer = MediaPlayer.create(this,R.raw.menu_music); //creates music
        mediaPlayer.setLooping(true); //sets music on the loop


        mediaPlayer1 = MediaPlayer.create(this, R.raw.click);

        musicSwitch = findViewById(R.id.musicSwitch);

        binding.trailSwitch.setChecked(true);

        boolean isChecked = getMusicStateFromPreferences();
        musicSwitch.setChecked(isChecked);
        if (isChecked) {
            mediaPlayer.start();
        }

        //turns music on and off
        musicSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    mediaPlayer.start();
                } else {
                    mediaPlayer.pause();
                }

                saveMusicStateToPreferences(isChecked);
            }
        });


        binding.trailSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (Polyline polyline : petPolylines) {
                    polyline.setVisible(isChecked);
                }
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_profile) {
            mediaPlayer1.start();
            // Handle click on "Profile" menu item
            Intent intent = getIntent();
            Intent intent1 = new Intent(MapActivity2.this, UserActivity.class);

            String role = intent.getStringExtra("Role");
            if (role != null) {
                intent1.putExtra("Role", role);
            } else {
                Log.e("MapActivity", "Role is null or not found in intent extras");
            }
            intent1.putExtra("Phone", intent.getStringExtra("Phone"));
            intent1.putExtra("Email", intent.getStringExtra("Email"));
            intent1.putExtra("Name", intent.getStringExtra("Name"));
            intent1.putExtra("Role", intent.getStringExtra("Role"));
            startActivity(intent1);

            return true;
        }

        if (id == R.id.howto){
            mediaPlayer1.start();
            Intent intent = new Intent(MapActivity2.this, HowToActivity.class);
            startActivity(intent);

            return true;
        }

        if (id == R.id.menu_info) {
            mediaPlayer1.start();
            // Handle click on "Info" menu item
            Intent intent = new Intent(MapActivity2.this, InfoActivity.class);
            startActivity(intent);

            return true;
        }

        if (id == R.id.update){
            mediaPlayer1.start();
            recreate();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //saves music state in SharedPreferences
    private void saveMusicStateToPreferences(boolean state) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_KEY, state);
        editor.apply();
    }

    //gets music state from SharedPreferences
    private boolean getMusicStateFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return sharedPreferences.getBoolean(PREF_KEY, false); // false - default value
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // stop and releases Mediaplayer on rotation
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
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

                            for (Polyline polyline : petPolylines) {
                                polyline.remove();
                            }
                            petPolylines.clear();

                            // Add markers for pets around user marker
                            addPetsAroundUserMarker(latLng);

                            // Start moving pets
                            startMovingPets();

                            isNewMarkerAdded = true; // sets flag for a new marker
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
                .transform(new RoundedCornersTransformation(16))
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

    private void showSnackbar(final String petName, final float distance, final LatLng petLocation) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                Geocoder geocoder = new Geocoder(MapActivity2.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(petLocation.latitude, petLocation.longitude, 1);
                    if (addresses != null && addresses.size() > 0) {
                        String street = addresses.get(0).getThoroughfare();
                        if (street != null) {
                            return street;
                        }
                    }
                } catch (IOException e) {
                    Log.e("MapActivity2", "Error getting address from location", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(String street) {
                if (street != null) {
                    int dist = Math.round(distance);
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), petName + " очень далеко! " + dist + " м. от дома на улице " + street, Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(Color.parseColor("#8B4513"));
                    snackbar.setTextColor(Color.parseColor("#F0E68C"));
                    snackbar.show();
                }
            }
        }.execute();
    }


    private void addPetsAroundUserMarker(LatLng userLocation) {
        petMarkers.clear(); // clears the markers list
        petPolylines.clear();

        // adds markers into petMarkers list
        for (Pet pet : pets) {
            // Generate random location around user location
            double newLatitude = userLocation.latitude + Math.random() * 0.001 - 0.0005;
            double newLongitude = userLocation.longitude + Math.random() * 0.001 - 0.0005;

            // Check if new location is within map bounds
            LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            if (bounds.contains(new LatLng(newLatitude, newLongitude)) && !petMarkers.contains(pet)) {
                // Add marker for pet
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(newLatitude, newLongitude));

                int color = Color.argb(255, (int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256));
                pet.setColor(color);

                // Create a custom marker icon with pet's name and image
                int index = pets.indexOf(pet);
                BitmapDescriptor markerIcon = createMarkerIcon(pet, index);
                markerOptions.icon(markerIcon);

                Marker petMarker = mMap.addMarker(markerOptions);
                petMarkers.add(petMarker); // Add the marker to the list

                // Update pet's location
                pet.setLatitude(newLatitude);
                pet.setLongitude(newLongitude);
                pet.setPreviousLatitude(newLatitude);
                pet.setPreviousLongitude(newLongitude);


                PolylineOptions polylineOptions = new PolylineOptions()
                        .add(new LatLng(pet.getPreviousLatitude(), pet.getPreviousLongitude()), new LatLng(newLatitude, newLongitude))
                        .width(5)
                        .color(pet.getColor());
                Polyline petPolyline = mMap.addPolyline(polylineOptions);
                petPolylines.add(petPolyline);

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

                        // Update pet's marker position with Interpolator
                        LatLng petLocation = new LatLng(pet.getLatitude(), pet.getLongitude());
                        Marker petMarker = petMarkers.get(i);
                        long duration = 1000; // Set the duration of the animation to 1 second
                        LinearInterpolator interpolator = new LinearInterpolator(); // or TimeInterpolator interpolator = new LinearInterpolator();
                        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                        valueAnimator.setDuration(duration);
                        valueAnimator.setInterpolator((android.animation.TimeInterpolator) interpolator);
                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float animatedFraction = animation.getAnimatedFraction();
                                LatLng newPosition = SphericalUtil.interpolate(petMarker.getPosition(), petLocation, animatedFraction);
                                petMarker.setPosition(newPosition);
                            }
                        });
                        valueAnimator.start();
                        newPetMarkers.add(petMarker); // Adding an updated marker into list of pet markers

                        Polyline petPolyline = petPolylines.get(i);
                        List<LatLng> points = petPolyline.getPoints();
                        points.add(petLocation);
                        petPolyline.setPoints(points);

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
                                showSnackbar(pet.getpetName(),distance, petLocation);
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

                    addPetsToContainer();
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


    private void addPetsToContainer() {
        LinearLayout petsContainer = findViewById(R.id.pets_container);
        petsContainer.removeAllViews(); // Clear the container

        for (int i = 0; i < pets.size(); i++) {
            Pet pet = pets.get(i);

            PetView petView = new PetView(this);
            petView.setPet(pet);
            Glide.with(this)
                    .load(pet.getImageResource())
                    .transform(new RoundedCornersTransformation(16))
                    .apply(new RequestOptions().override(200, 200))
                    .into(petView);
            petsContainer.addView(petView);


            petView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // Set background to semi-transparent when touched
                            v.setAlpha(0.5f);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            // Reset background transparency when not touched
                            v.setAlpha(1.0f);
                            break;
                    }
                    return false;
                }
            });

            // Add an onClick listener for the pet view
            petView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mediaPlayer1.start();
                    Pet selectedPet = ((PetView) v).getPet();
                    LatLng petLocation = new LatLng(selectedPet.getLatitude(), selectedPet.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(petLocation, 18));

                    for (Polyline polyline : petPolylines) {
                        if ((polyline.getPoints().get(0).equals(new LatLng(selectedPet.getPreviousLatitude(), selectedPet.getPreviousLongitude())) &&
                                polyline.getPoints().get(polyline.getPoints().size() - 1).equals(petLocation)) &&
                                polyline.getColor() == selectedPet.getColor()) {
                            polyline.setWidth(10);
                        } else {
                            polyline.setWidth(5);
                        }
                    }
                }
            });

            petView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mediaPlayer1.start();
                    Pet selectedPet = ((PetView) v).getPet();

                    // Create a PopupWindow
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View popupView = inflater.inflate(R.layout.popup_window, null);
                    int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                    int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    boolean focusable = true;
                    final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                    // Set the pet's name in the PopupWindow
                    TextView petNameTextView = popupView.findViewById(R.id.pet_name);
                    petNameTextView.setText(selectedPet.getpetName());

                    // Show the PopupWindow above the PetView
                    int[] location = new int[2];
                    v.getLocationOnScreen(location);
                    popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, location[0], location[1] - popupView.getHeight());

                    // Dismiss the PopupWindow when the user clicks outside of it
                    popupView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            popupWindow.dismiss();
                            return true;
                        }
                    });

                    // Dismiss the PopupWindow automatically after 3 seconds
                    Handler handler = new Handler();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            popupWindow.dismiss();
                        }
                    };
                    handler.postDelayed(runnable, 3000);

                    return true;
                }
            });

            // Add a space between the pet views
            if (i < pets.size() - 1) {
                View space = new View(this);
                space.setLayoutParams(new LinearLayout.LayoutParams(16, 0)); // 16dp space
                petsContainer.addView(space);
            }
        }
    }
}