package com.example.petlocator;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petlocator.databinding.ActivityClickBinding;

public class ClickActivity extends AppCompatActivity {

    private int clicks = 0;
    private MediaPlayer mediaPlayer;
    private Animation rotateAnimation;

    ActivityClickBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClickBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mediaPlayer = MediaPlayer.create(this, com.example.petlocator.R.raw.bark);

        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_animation);

        binding.resizeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicks++;
                TextView clicksTextView = findViewById(R.id.clicks_text_view);
                clicksTextView.setText(String.valueOf(clicks));

                mediaPlayer.start();

                binding.resizeImageView.startAnimation(rotateAnimation);
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