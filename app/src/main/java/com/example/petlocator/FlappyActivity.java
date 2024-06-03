package com.example.petlocator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FlappyActivity extends AppCompatActivity {
    private com.example.petlocator.databinding.ActivityFlappyBinding binding;

    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.example.petlocator.databinding.ActivityFlappyBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Find the WebView in your layout
        webView = binding.flappyBird;

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
        webView.loadUrl("https://flappy-bird.io");
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