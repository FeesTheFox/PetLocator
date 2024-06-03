package com.example.petlocator;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petlocator.databinding.ActivityClickBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ClickActivity extends AppCompatActivity {

    private int clicks = 0;
    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayer1;
    private MediaPlayer mediaPlayer2;
    private Animation rotateAnimation;

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String userId;
    ActivityClickBinding binding;

    private ImageView imageView;
    private boolean[] isReached = {false, false, false, false, false};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.example.petlocator.databinding.ActivityClickBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mediaPlayer = MediaPlayer.create(this, R.raw.bark);
        mediaPlayer1 = MediaPlayer.create(this, R.raw.yippie);
        mediaPlayer2 = MediaPlayer.create(this, R.raw.meow);

        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_animation);

        // Инициализируем Firebase Database и Firebase Auth
        firebaseDatabase = FirebaseDatabase.getInstance("https://petlocator-d7771-default-rtdb.firebaseio.com/");
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        binding.gameReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ClickActivity.this);
                dialogBuilder.setTitle("Начать игру заново?");
                dialogBuilder.setMessage("Вы уверены, что хотите начать игру заново?");
                dialogBuilder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Сбрасываем счётчик кликера
                        clicks = 0;
                        if (firebaseUser != null) {
                            HashMap<String, Object> data = new HashMap<>();
                            data.put("clicks", clicks);
                            firebaseDatabase.getReference("Users").child(userId).updateChildren(data);
                        }
                        setImageAndClicksText();
                    }
                });
                dialogBuilder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
            }
        });

        binding.catSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Сохраняем значение переключателя в базу данных Firebase
                HashMap<String, Object> data = new HashMap<>();
                data.put("catSwitch", isChecked);
                firebaseDatabase.getReference("Users").child(userId).updateChildren(data)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // Отображаем количество кликов на экране
                                setImageAndClicksText();
                            }
                        });
            }
        });

        if (firebaseUser != null) {
            // Получаем уникальный идентификатор пользователя
            userId = firebaseUser.getUid();

            DatabaseReference userRef = firebaseDatabase.getReference("Users").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String role = snapshot.child("role").getValue(String.class);
                        if ("Супер пользователь".equals(role)) {
                            binding.catSwitch.setVisibility(View.VISIBLE);
                        } else {
                            binding.catSwitch.setVisibility(View.GONE);
                        }

                        // Загружаем значение переключателя из базы данных Firebase

                        if (snapshot.child("catSwitch").exists()) {
                            boolean catSwitch = snapshot.child("catSwitch").getValue(Boolean.class);
                            binding.catSwitch.setChecked(catSwitch);
                        } else {
                            // Устанавливаем значение по умолчанию для переключателя
                            binding.catSwitch.setChecked(false);
                            // Сохраняем значение по умолчанию в базе данных Firebase
                            HashMap<String, Object> data = new HashMap<>();
                            data.put("catSwitch", false);
                            firebaseDatabase.getReference("Users").child(userId).updateChildren(data);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Обработать ошибку
                }
            });

            // Загружаем клики из базы данных Firebase
            DatabaseReference clicksRef = firebaseDatabase.getReference("Users").child(userId).child("clicks");
            clicksRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Проверяем, существует ли значение кликов в базе данных Firebase
                    if (snapshot.exists()) {
                        // Получаем количество кликов из базы данных Firebase
                        clicks = snapshot.getValue(Integer.class);
                    } else {
                        // Устанавливаем начальное значение кликов равным 0
                        clicks = 0;
                        // Сохраняем начальное значение кликов в базе данных Firebase
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("clicks", clicks);
                        firebaseDatabase.getReference("Users").child(userId).updateChildren(data);
                    }
                    // Отображаем количество кликов на экране
                    setImageAndClicksText();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Обрабатываем ошибку загрузки данных из базы данных Firebase
                }
            });
        }

        // Получаем ссылку на элемент ImageView с идентификатором resize_image_view
        imageView = findViewById(R.id.resize_image_view);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clicksToAdd = 1;
                if (clicks >= 200 && clicks < 600) {
                    clicksToAdd = 2;
                } else if (clicks >= 600 && clicks < 1000) {
                    clicksToAdd = 3;
                } else if (clicks >= 1000) {
                    clicksToAdd = 4;
                } else if (clicks >= 3000) {
                    clicksToAdd = 5;
                }
                clicks += clicksToAdd;

                // Проверяем, включен ли переключатель
                if (binding.catSwitch.isChecked()) {
                    // Проигрываем мяуканье
                    mediaPlayer2.start();
                } else {
                    // Проигрываем лай
                    mediaPlayer.start();
                }

                imageView.startAnimation(rotateAnimation);

                if (firebaseUser != null) {
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("clicks", clicks);

                    firebaseDatabase.getReference("Users").child(userId).updateChildren(data)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // Отображаем количество кликов на экране
                                    setImageAndClicksText();
                                }
                            });
                }
            }
        });
    }

    private void setImageAndClicksText() {
        int clicksCount = clicks;
        String clicksText = String.valueOf(clicksCount);

        // Проверяем, включен ли переключатель
        if (binding.catSwitch.isChecked()) {
            // Изменяем изображение на cat.xml, cat2.xml, cat3.xml и т.д. в зависимости от количества кликов
            if (clicksCount >= 3000) {
                imageView.setImageResource(R.drawable.cat6);
                if (clicksCount == 3000 && !isReached[4]) {
                    showSnackbar("Так держать, вы добрались до 3000");
                    mediaPlayer1.start();
                    isReached[4] = true;
                }
            } else if (clicksCount >= 1600) {
                imageView.setImageResource(R.drawable.cat5);
                if (clicksCount == 1600 && !isReached[3]) {
                    showSnackbar("Так держать, вы добрались до 1600");
                    mediaPlayer1.start();
                    isReached[3] = true;
                }
            } else if (clicksCount >= 1000) {
                imageView.setImageResource(R.drawable.cat4);
                if (clicksCount == 1000 && !isReached[2]) {
                    showSnackbar("Так держать, вы добрались до 1000");
                    mediaPlayer1.start();
                    isReached[2] = true;
                }
            } else if (clicksCount >= 600) {
                imageView.setImageResource(R.drawable.cat3);
                if (clicksCount == 600 && !isReached[1]) {
                    showSnackbar("Так держать, вы добрались до 600");
                    mediaPlayer1.start();
                    isReached[1] = true;
                }
            } else if (clicksCount >= 200) {
                imageView.setImageResource(R.drawable.cat2);
                if (clicksCount == 200 && !isReached[0]) {
                    showSnackbar("Так держать, вы добрались до 200");
                    mediaPlayer1.start();
                    isReached[0] = true;
                }
            } else {
                imageView.setImageResource(R.drawable.cat);
            }

            binding.headerTit2.setText("Нажми на кошку!");
        } else {
            // Изменяем изображение в зависимости от количества кликов
            if (clicksCount >= 3000) {
                imageView.setImageResource(R.drawable.img_resize6);
                if (clicksCount == 3000 && !isReached[4]) {
                    showSnackbar("Так держать, вы добрались до 3000");
                    mediaPlayer1.start();
                    isReached[4] = true;
                }
            } else if (clicksCount >= 1600) {
                imageView.setImageResource(R.drawable.img_resize5);
                if (clicksCount == 1600 && !isReached[3]) {
                    showSnackbar("Так держать, вы добрались до 1600");
                    mediaPlayer1.start();
                    isReached[3] = true;
                }
            } else if (clicksCount >= 1000) {
                imageView.setImageResource(R.drawable.img_resize4);
                if (clicksCount == 1000 && !isReached[2]) {
                    showSnackbar("Так держать, вы добрались до 1000");
                    mediaPlayer1.start();
                    isReached[2] = true;
                }
            } else if (clicksCount >= 600) {
                imageView.setImageResource(R.drawable.img_resize3);
                if (clicksCount == 600 && !isReached[1]) {
                    showSnackbar("Так держать, вы добрались до 600");
                    mediaPlayer1.start();
                    isReached[1] = true;
                }
            } else if (clicksCount >= 200) {
                imageView.setImageResource(R.drawable.img_resize2);
                if (clicksCount == 200 && !isReached[0]) {
                    showSnackbar("Так держать, вы добрались до 200");
                    mediaPlayer1.start();
                    isReached[0] = true;
                }
            } else {
                imageView.setImageResource(R.drawable.img_resize);
            }

            binding.headerTit2.setText("Нажми на собаку!");
        }

        // Отображаем количество кликов на экране
        TextView clicksTextView = findViewById(R.id.clicks_text_view);
        clicksTextView.setText(clicksText);
    }

    private void showSnackbar(String message) {
        Snackbar.make(imageView, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.back) {
            finish();

            return true;
        }
        if (id == R.id.Flappy){
            Intent intent = new Intent(ClickActivity.this, FlappyActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
