package com.example.petlocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.petlocator.databinding.ActivityHowToBinding;
import com.example.petlocator.databinding.ActivityInfoBinding;

public class HowToActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    ActivityHowToBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHowToBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mediaPlayer = MediaPlayer.create(this, R.raw.click);
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
}