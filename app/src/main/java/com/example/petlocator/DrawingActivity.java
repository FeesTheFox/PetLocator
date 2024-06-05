package com.example.petlocator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.petlocator.databinding.ActivityDrawingBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DrawingActivity extends AppCompatActivity {

    private Bitmap bitmap;
    private Canvas canvas;
    private ImageView imageView;
    MediaPlayer mediaPlayer;

    ActivityDrawingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDrawingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        imageView = findViewById(R.id.image_view);

        mediaPlayer = MediaPlayer.create(this, R.raw.click);

        // Set up touch events for drawing
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // Get the current touch position
                int x = (int) event.getX();
                int y = (int) event.getY();

                // Set the pixel at the touch position to black
                bitmap.setPixel(x, y, Color.BLACK);

                // Create a canvas for the bitmap
                Canvas canvas = new Canvas(bitmap);


                // Draw a circle at the touch position
                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                canvas.drawCircle(x, y, 10, paint);

                // Update the image view to display the new canvas
                imageView.setImageBitmap(bitmap);

                // Return true to indicate that the touch event was handled
                return true;
            }
        });

        binding.clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshDialog();
            }
        });


        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Вызываем диалоговое окно для сохранения рисунка
                showSaveDialog();
            }
        });


        // Create a bitmap to use as a canvas when the image view is laid out
        imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remove the listener to prevent multiple calls
                imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Create a bitmap to use as a canvas
                bitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);
                canvas.drawColor(Color.WHITE);

                // Set the image view to display the bitmap
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    private void refreshDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.DialogStyle);
        dialogBuilder.setTitle("Очищение холста");
        dialogBuilder.setMessage("Хотите очистить холст?");
        dialogBuilder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Create a new bitmap for the next drawing
                bitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);

                // Update the image view to display the new bitmap
                imageView.setImageBitmap(bitmap);
            }
        }).setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogBuilder.show();
    }

    private void showSaveDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.DialogStyle);
        dialogBuilder.setTitle("Сохранить рисунок");
        dialogBuilder.setMessage("Хотите сохранить рисунок?");
        dialogBuilder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fileName = "my_image_" + System.currentTimeMillis() + ".png";
                ImageSaver.saveImage(DrawingActivity.this, bitmap, fileName, new ImageSaver.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(Uri imageUri) {
                        // Выводим сообщение об успешном сохранении
                        Toast.makeText(DrawingActivity.this, "Рисунок сохранен", Toast.LENGTH_SHORT).show();


                    }
                });
            }
        });
        dialogBuilder.setNegativeButton("Нет", null);
        dialogBuilder.show();
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