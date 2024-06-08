package com.example.petlocator;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petlocator.databinding.ActivityRequisiteBinding;

public class RequisiteActivity extends AppCompatActivity {
    ActivityRequisiteBinding binding;
    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequisiteBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mediaPlayer = MediaPlayer.create(this, R.raw.click);
        WebView webView = findViewById(R.id.webView);

        binding.boosty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                String url = "https://github.com/FeesTheFox";
                webView.loadUrl(url);
                webView.setVisibility(View.VISIBLE);
            }
        });

        // Launches WebView
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);


        binding.email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                AlertDialog.Builder dialog = new AlertDialog.Builder(RequisiteActivity.this, R.style.DialogStyle);
                dialog.setTitle("Написать на почту");
                dialog.setMessage("Вы хотите написать на почту feesblock@gmail.com?");
                dialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mediaPlayer.start();
                        String email = "feesblock@gmail.com";
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("message/rfc822");
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                });
                dialog.setNegativeButton("Нет", null);
                dialog.show();
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
}