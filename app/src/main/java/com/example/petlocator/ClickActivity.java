package com.example.petlocator;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
    private Animation rotateAnimation;

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String userId;

    private ImageView imageView;
    private boolean[] isReached = {false, false, false, false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click);

        mediaPlayer = MediaPlayer.create(this, R.raw.bark);
        mediaPlayer1 = MediaPlayer.create(this, R.raw.yippie);

        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_animation);

        // Инициализируем Firebase Database и Firebase Auth
        firebaseDatabase = FirebaseDatabase.getInstance("https://petlocator-d7771-default-rtdb.firebaseio.com/");
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            // Получаем уникальный идентификатор пользователя
            userId = firebaseUser.getUid();

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
                }
                clicks += clicksToAdd;

                mediaPlayer.start();

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

        if (clicksCount >= 1600) {
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

        // Отображаем количество кликов на экране
        TextView clicksTextView = findViewById(R.id.clicks_text_view);
        clicksTextView.setText(clicksText);
    }

    private void showSnackbar(String message) {
        Snackbar.make(imageView, message, Snackbar.LENGTH_LONG).show();
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
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
