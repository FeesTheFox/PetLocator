package com.example.petlocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;


import com.example.petlocator.databinding.ActivityUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;

public class UserActivity extends AppCompatActivity {
    private ArrayList<Pet> dogsList;
    private PetAdapter adapter;
    private ActivityUserBinding binding;
    private SharedPreferences sp;
    private DatabaseReference dogsRef;
    private DatabaseReference userRef;
    private String currentUserEmail;
    private String currentUserRole;
    private static final String PREFS_FILE = "User_account";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        setContentView(view);

        Intent intent = getIntent();
        if (intent.hasExtra("Name")) {
            binding.name.setText(intent.getStringExtra("Name"));
            binding.mail.setText(intent.getStringExtra("Email"));

        } else {
            binding.name.setText(sp.getString("Name", ""));
            binding.mail.setText(sp.getString("Email", ""));
        }

        currentUserEmail = sp.getString("Email", "");

        //getting the ID of a user
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserUid);


        //loads dogList from database for user
        if (currentUserEmail != null &&  !currentUserEmail.isEmpty()) {
            userRef = FirebaseDatabase.getInstance("https://petlocator-d7771-default-rtdb.firebaseio.com/").getReference("Users");

            currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    dogsList.clear();
                    if (dataSnapshot.hasChild("pets")) {
                        DataSnapshot petsSnapshot = dataSnapshot.child("pets");
                        for (DataSnapshot petSnapshot : petsSnapshot.getChildren()) {
                            Pet pet = petSnapshot.getValue(Pet.class);
                            dogsList.add(pet);
                        }
                    }
                    Log.d("UserActivity", "Number of pets retrieved: " + dogsList.size());
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("UserActivity", "loadDogs:onCancelled: ", error.toException());
                }
            });
        }

        //when button back is pressed
        binding.gone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        dogsList = new ArrayList<>(); //creating a list instance
        //creating an adapter instance based on a list
        adapter = new PetAdapter(this, dogsList);
        ListView listView = findViewById(R.id.doglist);
        listView.setAdapter(adapter);

        //when pressed Add a pet
        binding.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDogDialog();
            }
        });


        //gets a path for User -> pets
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserUid).child("pets");

        // Deletes pet from both Database and list
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
                builder.setMessage("Are you sure you want to delete pet from the list?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() { //if add
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String petIdToDelete = dogsList.get(position).getPetId(); //gets pet ID
                                Query query = databaseReference.orderByChild("petId").equalTo(petIdToDelete); //seeks for petId in Database by the name "petId"
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            snapshot.getRef().removeValue();//removes pet in database
                                            dogsList.remove(position); //removes pet in the list in the app
                                            adapter.notifyDataSetChanged(); //updates data
                                            break;
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        // Handling errors
                                    }
                                });
                            }
                            //cancels the pet removal
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.show();
                return true;
            }
        });
    }

    //Dialog of adding pet
    private void showAddDogDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_dog_window, null); //inflates the adding pet xml
        builder.setView(dialogView);

        //Info about a pet -> in a list
        final EditText species = dialogView.findViewById(R.id.editTextSpecies);
        final EditText age = dialogView.findViewById(R.id.editTextAge);
        final EditText name = dialogView.findViewById(R.id.editTextPetName);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //creating a Pet object
                Pet pet = new Pet(); //calls the object
                pet.setSpecies(species.getText().toString()); //gets the pet's species
                pet.setAge(age.getText().toString()); //gets the pet's age
                pet.setpetName(name.getText().toString()); //gets the pet's name

                //gets the user's ID from database
                String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                //getting the User's pets
                DatabaseReference currentUserPetsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserUid).child("pets");

                // Save pet in the user node in the Database
                addPetToUserNode(currentUserPetsRef, pet);
            }
        });
        //If cancels, dialog is canceled
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setTitle("Add a pet");
        builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause(); //saves data with Shared Preferences in a profile
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("Name", binding.name.getText().toString());
        editor.putString("Email", binding.mail.getText().toString());
        editor.apply();
    }

    private void addPetToUserNode(DatabaseReference userPetsRef, Pet pet) {
        // Save pet in the user node in the Database
        String petId = userPetsRef.push().getKey(); //pushes petID inside the pets list
        pet.setPetId(petId); //sets a random ID
        String petKey = userPetsRef.push().getKey(); //pushes petKey inside the pets list
        userPetsRef.child(petKey).setValue(pet); //sets a key

        // Add pet to the local list and update the adapter
        dogsList.add(pet);
        adapter.notifyDataSetChanged();
    }
}
