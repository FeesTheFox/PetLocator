package com.example.petlocator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.petlocator.databinding.ActivityInfoBinding;

public class InfoActivity extends AppCompatActivity {

    ActivityInfoBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}