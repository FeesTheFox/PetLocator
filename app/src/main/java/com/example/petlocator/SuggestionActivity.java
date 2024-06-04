package com.example.petlocator;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petlocator.databinding.ActivitySuggestionBinding;

public class SuggestionActivity extends AppCompatActivity {
    ActivitySuggestionBinding binding;
    private WebView webView;
    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuggestionBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Find the WebView in your layout
        webView = binding.webView;
        mediaPlayer = MediaPlayer.create(this, R.raw.click);

        // Enable JavaScript for the WebView
        webView.getSettings().setJavaScriptEnabled(true);

        // Set a WebViewClient to the WebView, so it will not open the link in a browser
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Wait for the button to be rendered (this is just an example, you might need to adjust the delay)
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Execute the JavaScript to click the button
                        webView.evaluateJavascript("javascript:document.querySelector('button.accept-all').click();", null);
                    }
                }, 1000);
            }
        });

        // Load the URL
        webView.loadUrl("https://ru.wikihow.com/заботиться-о-домашнем-питомце");
    }

    // Add this method to handle back button presses
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
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
