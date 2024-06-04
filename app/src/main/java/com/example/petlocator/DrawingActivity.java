package com.example.petlocator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DrawingActivity extends AppCompatActivity {

    private Bitmap bitmap;
    private Canvas canvas;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        imageView = findViewById(R.id.image_view);

        // Set up touch events for drawing
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // Get the current touch position
                float x = event.getX();
                float y = event.getY();

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

        // Add a save button to the layout
        Button saveButton = new Button(this);
        saveButton.setText("Сохранить");
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save the bitmap and return to the UserActivity
                Uri imageUri = saveBitmapToFile(bitmap);
                Intent resultIntent = new Intent();
                resultIntent.setData(imageUri);
                resultIntent.putExtra("image_uri", imageUri.toString());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        // Add the save button to the layout
        LinearLayout layout = findViewById(R.id.drawing_layout);
        layout.addView(saveButton);

        // Create a bitmap to use as a canvas when the image view is laid out
        imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remove the listener to prevent multiple calls
                imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Create a bitmap to use as a canvas
                bitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);

                // Set the image view to display the bitmap
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    // Метод для сохранения Bitmap в файл и получения Uri этого файла
    private Uri saveBitmapToFile(Bitmap bitmap) {
        File imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "pet_image.png");
        try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
        } catch (IOException e) {
            Log.e("LOGTAG", "Failed to save bitmap to file", e);
            return null;
        }
        return Uri.fromFile(imageFile);
    }
}