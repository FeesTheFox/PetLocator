package com.example.petlocator;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petlocator.databinding.ActivityClickBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private Animation rotateAnimation;

    ActivityClickBinding binding;

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String userId;

    TextView clicksTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClickBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mediaPlayer = MediaPlayer.create(this, com.example.petlocator.R.raw.bark);

        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_animation);

        // Инициализируем Firebase Database и Firebase Auth
        firebaseDatabase = FirebaseDatabase.getInstance("https://petlocator-d7771-default-rtdb.firebaseio.com/");
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        binding.shop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClickActivity.this, ShopActivity.class);
                startActivity(intent);
            }
        });

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
                    clicksTextView.setText(String.valueOf(clicks));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Обрабатываем ошибку загрузки данных из базы данных Firebase
                }
            });
        }

        // Получаем ссылку на элемент TextView с идентификатором clicks_text_view
        clicksTextView = findViewById(R.id.clicks_text_view);

        binding.resizeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicks++;
                clicksTextView.setText(String.valueOf(clicks));

                mediaPlayer.start();

                binding.resizeImageView.startAnimation(rotateAnimation);

                HashMap<String, Object> data = new HashMap<>();
                data.put("clicks", clicks);

                firebaseDatabase.getReference("Users").child(userId).updateChildren(data);
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
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}