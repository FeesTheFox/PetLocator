package com.example.petlocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.petlocator.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sp;

    ActivityMainBinding binding;
    FirebaseAuth auth; //For authorizing user
    FirebaseDatabase db; //Data base
    DatabaseReference users; //Table in Data base

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater()); //work with binding
        View view = binding.getRoot();
        setContentView(view);
        auth = FirebaseAuth.getInstance(); //Authorization launch
        db = FirebaseDatabase.getInstance(); //Connecting to Data base
        users = db.getReference("Users"); //Table with user's data

        //creating instance for logo
        ImageView imageView = findViewById(R.id.logo);

        //finding an 8 images for an animation
        ImageView[] imageViews = new ImageView[8];
        imageViews[0] = findViewById(R.id.image1);
        imageViews[1] = findViewById(R.id.image2);
        imageViews[2] = findViewById(R.id.image3);
        imageViews[3] = findViewById(R.id.image4);
        imageViews[4] = findViewById(R.id.image5);
        imageViews[5] = findViewById(R.id.image6);
        imageViews[6] = findViewById(R.id.image7);
        imageViews[7] = findViewById(R.id.image8);

        //getting the display metrics
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        //getting the center coords
        int centerX = screenWidth / 2 - 120;
        int centerY = screenHeight / 2 + 40;

        //calculating the circle radius
        int radius = Math.min(centerX, centerY);

        //creating ValueAnimator
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1000); //Length of animation (in millisecs)
        animator.setInterpolator(new LinearInterpolator()); //Interpolar type
        animator.setRepeatCount(ValueAnimator.INFINITE); //on infinite loop

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = animation.getAnimatedFraction();
                for (int i = 0; i < imageViews.length; i++) {
                    // Calculating angle for each ImageView
                    float angle = (float) (2 * Math.PI * (i + progress) / imageViews.length);
                    // Calculating coords for each ImageView
                    int x = (int) (centerX + radius * Math.cos(angle));
                    int y = (int) (centerY + radius * Math.sin(angle));
                    // Installing the coords for each ImageView
                    imageViews[i].setX(x);
                    imageViews[i].setY(y);
                }
            }
        });

        animator.start();

        //creating animation based on xml file
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.up_down);
        imageView.startAnimation(animation);

        //pressing registration button
        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterWindow();
            }
        });
        //pressing sign in button
        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignInWindow();
            }
        });
    }

    private void showRegisterWindow() {
        //dialog window when Registration
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Register");
        dialog.setMessage("Enter data for registration");
        LayoutInflater inflater = LayoutInflater.from(this);
        View register_window = inflater.inflate(R.layout.register_window, null);
        dialog.setView(register_window);
        final EditText email = register_window.findViewById(R.id.emailField);
        final EditText pass = register_window.findViewById(R.id.passField);
        final EditText name = register_window.findViewById(R.id.nameField);
        final EditText phone = register_window.findViewById(R.id.phoneField);

        String[] choosen_role = new String[1]; //getting role
        RadioGroup group = register_window.findViewById(R.id.radiogroup);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = group.findViewById(checkedId);
                String role = radioButton.getText().toString();
                choosen_role[0] = role;
            }
        });


        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (TextUtils.isEmpty(email.getText().toString())) {
                    Snackbar.make(binding.root,
                            "Enter your email address",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(name.getText().toString())) {
                    Snackbar.make(binding.root,
                            "Enter your name",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(phone.getText().toString())) {
                    Snackbar.make(binding.root,
                            "Enter your phone number",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (pass.getText().toString().length () < 5) {
                    Snackbar.make(binding.root,
                            "Enter the password at least 5 characters",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }if (choosen_role[0] == null){
                    Snackbar.make(binding.root,
                            "Pick a role",
                            Snackbar.LENGTH_SHORT).show();
                }
                //User's registration
                auth.createUserWithEmailAndPassword(email.getText().toString(),
                        pass.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                User user = new User();
                                user.setEmail(email.getText().toString());
                                user.setName(name.getText().toString());
                                user.setPhone(phone.getText().toString());
                                user.setPass(pass.getText().toString());
                                user.setRole(choosen_role[0].toString());
                                //adding new user in the table (key - ID of record)
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Snackbar.make(binding.root, "Success!",
                                                        Snackbar.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(binding.root, e.getMessage(),
                                                        Snackbar.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(binding.root, e.getMessage(),
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        dialog.show();
    }

    private void showSignInWindow() {
        //dialog window when Signing In
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Sign In");
        dialog.setMessage("Enter log in data");
        LayoutInflater inflater = LayoutInflater.from(this);
        View sign_in_window = inflater.inflate(R.layout.sign_in_window, null);
        dialog.setView(sign_in_window);
        final EditText email = sign_in_window.findViewById(R.id.emailField);
        final EditText pass = sign_in_window.findViewById(R.id.passField);

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (TextUtils.isEmpty(email.getText().toString())){
                    Snackbar.make(binding.root,
                            "Enter your email address",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (pass.getText().toString().length() < 5){
                    Snackbar.make(binding.root,
                            "Enter the password at least 5 characters",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }
                //user's authorizing
                auth.signInWithEmailAndPassword(email.getText().toString(),
                        pass.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) { //success
                        FirebaseDatabase database = FirebaseDatabase
                                .getInstance("https://petlocator-d7771-default-rtdb.firebaseio.com/");
                        DatabaseReference tableUsers = database.getReference("Users");
                        tableUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot child : snapshot.getChildren()) {
                                    User user = child.getValue(User.class);
                                    if (user.getEmail().equals(email.getText().toString())) {
                                        if (user.getRole().equals("User")){
                                            Intent intent = new Intent(MainActivity.this, MapActivity.class);
                                            intent.putExtra("Email", user.getEmail());
                                            intent.putExtra("Name", user.getName());
                                            intent.putExtra("Phone", user.getPhone());
                                            intent.putExtra("Role", user.getRole());
                                            startActivity(intent);
                                        }
                                        if (user.getRole().equals("Super User")){
                                            Intent intent = new Intent(MainActivity.this, MapActivity2.class);
                                            intent.putExtra("Email", user.getEmail());
                                            intent.putExtra("Name", user.getName());
                                            intent.putExtra("Phone", user.getPhone());
                                            intent.putExtra("Role", user.getRole());
                                            startActivity(intent);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) { //Fail
                        Snackbar.make(binding.root,
                                "Authorization Error: "+
                                e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });

            }
        });
        dialog.show();
    }

}