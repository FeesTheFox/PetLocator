package com.example.petlocator;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.petlocator.databinding.ActivityUserBinding;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private static final int DRAWING_REQUEST_CODE = 1003;
    private Uri imageUri;
    private ImageView imageView;
    private MediaPlayer mediaPlayer;
    private AlertDialog addDogDialog;
    private ImageView addDogImageView;

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
            binding.phone.setText(intent.getStringExtra("Phone"));

        } else {
            binding.name.setText(sp.getString("Name", ""));
            binding.mail.setText(sp.getString("Email", ""));
            binding.phone.setText(sp.getString("Phone", ""));
        }

        currentUserEmail = sp.getString("Email", "");

        mediaPlayer = MediaPlayer.create(this, R.raw.click);

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


        dogsList = new ArrayList<>(); //creating a list instance
        //creating an adapter instance based on a list
        adapter = new PetAdapter(this, dogsList);
        ListView listView = findViewById(R.id.doglist);
        listView.setAdapter(adapter);

        //when pressed Add a pet
        binding.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                showAddDogDialog();
            }
        });

        binding.clicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                Intent intent1 = new Intent(UserActivity.this, ClickActivity.class);
                startActivity(intent1);
            }
        });

        binding.suggestions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                Intent intent1 = new Intent(UserActivity.this, SuggestionActivity.class);
                startActivity(intent1);
            }
        });

        //gets a path for User -> pets
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserUid).child("pets");

        // Deletes pet from both Database and list while pressed on the item list
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this, R.style.DialogStyle);
                builder.setMessage("Вы уверены, что хотите удалить питомца?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() { //if add
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mediaPlayer.start();
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
                        }).setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mediaPlayer.start();
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
                mediaPlayer.start();
                showEditDogDialog(position);
            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.back_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();



        if (id == R.id.back) {
            mediaPlayer.start();
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Editing the pet data
    private void showEditDogDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogStyle);
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
        Glide.with(this).load(pet.getImageResource()).transform(new RoundedCornersTransformation(16)).into(imageView);

        // sets imageUri for a current pet
        imageUri = pet.getNewImageUri();

        Button buttonSelectImage = dialogView.findViewById(R.id.button_select_image);
        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicker();
                pet.setNewImageUri(imageUri);
            }
        });

        builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mediaPlayer.start();
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

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mediaPlayer.start();
                dialog.cancel();
            }
        });

        builder.setTitle("Изменить данные");
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
        if (pet.getNewImageUri() != null) {
            // Save the new pet image in Firebase Storage and get the download URL
            savePetImageInFirebaseStorage(pet.getNewImageUri(), pet);
        } else if (pet.getImageResource() != null && !pet.getImageResource().isEmpty()) {
            petUpdates.put("imageResource", pet.getImageResource());
        }
        if (pet.getPetId() != null) {
            // Updates current pet
            currentUserPetsRef.child(pet.getPetId()).updateChildren(petUpdates);
        } else {
            // Adding a new pet
            String newPetId = currentUserPetsRef.push().getKey();
            pet.setPetId(newPetId);
            currentUserPetsRef.child(newPetId).setValue(pet);
        }
    }


    //Dialog of adding pet
    private void showAddDogDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogStyle);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_dog_window, null);
        builder.setView(dialogView);

        // Info about a pet -> in a list
        final EditText species = dialogView.findViewById(R.id.editTextSpecies);
        final EditText age = dialogView.findViewById(R.id.editTextAge);
        final EditText name = dialogView.findViewById(R.id.editTextPetName);
        addDogImageView = dialogView.findViewById(R.id.imageView);

        Button buttonSelectImage = dialogView.findViewById(R.id.button_select_image);
        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicker();
            }
        });

        builder.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mediaPlayer.start();
                if (imageUri == null) {
                    // Shows the snackbar with the message
                    Snackbar.make(binding.getRoot(), "Пожалуйста, выберите фотографию для питомца", Snackbar.LENGTH_SHORT).show();
                } else {
                    // Creates a new pet object and saves it in the database
                    Pet pet = new Pet();
                    pet.setSpecies(species.getText().toString());
                    pet.setAge(age.getText().toString());
                    pet.setpetName(name.getText().toString());

                    savePetImageInFirebaseStorage(imageUri, pet);

                    // sets imageUri null after saving new pet
                    imageUri = null;
                }
            }
        });

        // If cancels, dialog is canceled
        builder.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mediaPlayer.start();
                dialog.cancel();
            }
        });

        builder.setTitle("Добавление питомца");
        addDogDialog = builder.show();
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
        builder.setTitle("Выберите фотографию")
                .setItems(new CharSequence[]{"Галерея", "Камера"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The "which" argument contains the index position of the selected item
                        if (which == 0) {
                            mediaPlayer.start();
                            // Open gallery
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Выберите фотографию"), IMAGE_PICKER_REQUEST_CODE);
                        } else if (which == 1) {
                            mediaPlayer.start();
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

        imageView = findViewById(R.id.imageView);

        if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imageUri != null && imageView != null) {
                Glide.with(this).load(imageUri).transform(new RoundedCornersTransformation(16)).into(imageView);
            }
            if (addDogDialog != null && addDogImageView != null) {
                Glide.with(this)
                        .load(imageUri)
                        .into(addDogImageView);
            }


        }else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            if (imageBitmap != null) {
                // Convert bitmap to Uri and save it to imageUri
                Uri tempUri = getImageUri(getApplicationContext(), imageBitmap);
                imageUri = tempUri;

                // Load the image into imageView using Glide
                if (imageView != null) {
                    Glide.with(this).load(imageUri).transform(new RoundedCornersTransformation(16)).into(imageView);
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

}
