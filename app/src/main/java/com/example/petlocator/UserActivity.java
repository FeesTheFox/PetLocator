package com.example.petlocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;


import com.bumptech.glide.Glide;
import com.example.petlocator.databinding.ActivityUserBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    private static final int IMAGE_PICKER_REQUEST_CODE = 1001;
    private static final int CAMERA_REQUEST_CODE = 1002;
    private Uri imageUri;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        imageView = findViewById(R.id.imageView);
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

        // Deletes pet from both Database and list while pressed on the item list
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


        // When clicked once on the item list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEditDogDialog(position);
            }
        });

    }

    // Editing the pet data
    private void showEditDogDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_dog_window, null);
        builder.setView(dialogView);

        final EditText species = dialogView.findViewById(R.id.editTextSpecies);
        final EditText age = dialogView.findViewById(R.id.editTextAge);
        final EditText name = dialogView.findViewById(R.id.editTextPetName);
        final ImageView imageView = dialogView.findViewById(R.id.imageView);

        // Populate the fields with the current pet data
        Pet pet = dogsList.get(position);
        pet.setNew(false);
        species.setText(pet.getSpecies());
        age.setText(pet.getAge());
        name.setText(pet.getpetName());
        Glide.with(this).load(pet.getImageResource()).into(imageView);

        Button buttonSelectImage = dialogView.findViewById(R.id.button_select_image);
        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicker();
            }
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Update the pet data in the list and the database
                pet.setPetId(pet.getPetId());
                pet.setSpecies(species.getText().toString());
                pet.setAge(age.getText().toString());
                pet.setpetName(name.getText().toString());

                if (imageUri != null) {
                    // Save the new pet image in Firebase Storage and get the download URL
                    savePetImageInFirebaseStorage(imageUri, pet);
                } else {
                    // Use the existing pet image
                    updatePetInDatabase(pet);
                    dogsList.set(position, pet);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setTitle("Edit pet");
        builder.show();
    }

    // Updates the pet in the database
    private void updatePetInDatabase(Pet pet) {
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference currentUserPetsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserUid).child("pets");

        Map<String, Object> petUpdates = new HashMap<>();
        if (pet.getSpecies() != null && !pet.getSpecies().isEmpty()) {
            petUpdates.put("species", pet.getSpecies());
        }
        if (pet.getAge() != null&& !pet.getAge().isEmpty()) {
            petUpdates.put("age", pet.getAge());
        }
        if (pet.getpetName() != null&& !pet.getpetName().isEmpty()) {
            petUpdates.put("petName", pet.getpetName());
        }
        if (pet.getImageResource() !=null&& !pet.getImageResource().isEmpty()) {
            petUpdates.put("imageResource", pet.getImageResource());
        }

        if (pet.getPetId() != null) {
            // Обновляем существующего питомца
            currentUserPetsRef.child(pet.getPetId()).updateChildren(petUpdates);
        } else {
            // Добавляем нового питомца
            String newPetId = currentUserPetsRef.push().getKey();
            pet.setPetId(newPetId);
            currentUserPetsRef.child(newPetId).setValue(pet);
        }
    }




    //Dialog of adding pet
    private void showAddDogDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_dog_window, null);
        builder.setView(dialogView);

        // Info about a pet -> in a list
        final EditText species = dialogView.findViewById(R.id.editTextSpecies);
        final EditText age = dialogView.findViewById(R.id.editTextAge);
        final EditText name = dialogView.findViewById(R.id.editTextPetName);
        ImageView imageView = dialogView.findViewById(R.id.imageView);

        Button buttonSelectImage = dialogView.findViewById(R.id.button_select_image);
        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicker();
            }
        });

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (imageUri == null) {
                    // Показываем Snackbar с сообщением
                    Snackbar.make(binding.getRoot(), "Please select an image for your pet", Snackbar.LENGTH_SHORT).show();
                } else {
                    // Создаем объект питомца и сохраняем его в базе данных
                    Pet pet = new Pet();
                    pet.setSpecies(species.getText().toString());
                    pet.setAge(age.getText().toString());
                    pet.setpetName(name.getText().toString());

                    savePetImageInFirebaseStorage(imageUri, pet);
                }
            }
        });

        // If cancels, dialog is canceled
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setTitle("Add a pet");
        builder.show();
    }
    private void savePetImageInFirebaseStorage(Uri imageUri, final Pet pet) {
        // Get a reference to the Firebase Cloud Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Create a unique filename for the pet image
        String filename = UUID.randomUUID().toString();

        // Create a reference to the pet image in Firebase Cloud Storage
        StorageReference petImageRef = storageRef.child("pet_images/" + filename);

        // Upload the pet image to Firebase Cloud Storage
        petImageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the download Url of the pet image
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Save the pet with the image download Url in the Firebase Realtime Database
                                pet.setImageResource(uri.toString());
                                if (pet.isNew()) {
                                    addPetToUserNode(pet);
                                } else {
                                    updatePetInDatabase(pet);
                                }
                                int index = dogsList.indexOf(pet);
                                if (index == -1) {
                                    dogsList.add(pet);
                                } else {
                                    dogsList.set(index, pet);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
    }

    private void showImagePicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an image")
                .setItems(new CharSequence[]{"Gallery", "Camera"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The "which" argument contains the index position of the selected item
                        if (which == 0) {
                            // Open gallery
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_PICKER_REQUEST_CODE);
                        } else if (which == 1) {
                            // Open camera
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                            }
                        }
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imageUri != null && imageView != null) {
                Glide.with(this).load(imageUri).into(imageView);
            }
        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            if (imageBitmap != null) {
                // Convert bitmap to Uri and save it to imageUri
                Uri tempUri = getImageUri(getApplicationContext(), imageBitmap);
                imageUri = tempUri;

                // Load the image into imageView using Glide
                if (imageView != null) {
                    Glide.with(this).load(imageUri).into(imageView);
                }
            }
        }
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    protected void onPause() {
        super.onPause(); //saves data with Shared Preferences in a profile
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("Name", binding.name.getText().toString());
        editor.putString("Email", binding.mail.getText().toString());
        editor.apply();
    }

    private void addPetToUserNode(Pet pet) {
        // Get a reference to the user's pets node in the Firebase Realtime Database
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userPetsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserUid).child("pets");

        // Save pet in the user node in the Database
        String petId = userPetsRef.push().getKey();
        pet.setPetId(petId);
        userPetsRef.child(petId).setValue(pet);

        // Add pet to the local list and update the adapter
        dogsList.add(pet);
        adapter.notifyDataSetChanged();
    }


    //User's avatar


}
