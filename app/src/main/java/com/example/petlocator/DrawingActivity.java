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
import android.graphics.Path;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.petlocator.databinding.ActivityDrawingBinding;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DrawingActivity extends AppCompatActivity implements ColorPickerDialogListener  {

    private Bitmap bitmap;
    private Canvas canvas;
    private ImageView imageView;
    private int paintColor = Color.BLACK;
    MediaPlayer mediaPlayer;
    private Path drawPath;
    private Paint drawPaint;
    private int currentBrushSize = 50;
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
            private float lastX, lastY;
            private boolean isDrawing = false;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isDrawing = true;
                        lastX = event.getX();
                        lastY = event.getY();
                        drawPath.reset();
                        drawPath.moveTo(lastX, lastY);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isDrawing) {
                            float dx = Math.abs(event.getX() - lastX);
                            float dy = Math.abs(event.getY() - lastY);
                            if (dx >= 4 || dy >= 4) {
                                drawPath.quadTo(lastX, lastY, (event.getX() + lastX) / 2, (event.getY() + lastY) / 2);
                                lastX = event.getX();
                                lastY = event.getY();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        isDrawing = false;
                        drawPath.lineTo(event.getX(), event.getY());
                        break;
                    default:
                        return false;
                }

                // Set the color of the paint
                drawPaint.setColor(paintColor);

                // Draw the path on the canvas
                canvas.drawPath(drawPath, drawPaint);

                // Update the image view to display the new canvas
                imageView.setImageBitmap(bitmap);

                // Return true to indicate that the touch event was handled
                return true;
            }
        });


        binding.brushSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBrushSizeDialog();//dialog for choosing stroke width
            }
        });

        binding.btnColorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPickerDialog();
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

                // Create a Path for drawing
                drawPath = new Path();

                // Create a Paint for the Path
                drawPaint = new Paint();
                drawPaint.setStyle(Paint.Style.STROKE);
                drawPaint.setStrokeJoin(Paint.Join.ROUND);
                drawPaint.setStrokeCap(Paint.Cap.ROUND);
                drawPaint.setStrokeWidth(20);

                // Set the image view to display the bitmap
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    private void showLeaveDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.DialogStyle);
        dialogBuilder.setTitle("Выход с холста");
        dialogBuilder.setMessage("Вы уверены, что хотите покинуть холст? Весь прогресс сбросится");

        dialogBuilder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogBuilder.show();
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


    private void showBrushSizeDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.DialogStyle);
        View dialogView = getLayoutInflater().inflate(R.layout.brush_size_dialog, null);
        dialogBuilder.setView(dialogView);

        final SeekBar seekBar = dialogView.findViewById(R.id.brush_size_progress_bar);
        seekBar.setProgress(currentBrushSize); // Устанавливаем значение SeekBar в соответствии с текущим размером кисти

        // Добавляем обработчик изменения значения для SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Обновляем размер кисти в соответствии с текущим значением SeekBar
                drawPaint.setStrokeWidth(progress);
                currentBrushSize = progress; // Сохраняем текущий размер кисти
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Этот метод вызывается, когда пользователь начинает перемещать ползунок
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Этот метод вызывается, когда пользователь отпускает ползунок
            }
        });

        dialogBuilder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int brushSize = seekBar.getProgress();
                drawPaint.setStrokeWidth(brushSize);
                currentBrushSize = brushSize; // Сохраняем текущий размер кисти
            }
        });

        dialogBuilder.setNegativeButton("Отмена", null);

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }


    private void showColorPickerDialog() {
        ColorPickerDialog.newBuilder()
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowPresets(false)
                .setColor(paintColor)
                .setDialogId(1)
                .show(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.draw_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.back) {
            mediaPlayer.start();
            showLeaveDialog();

            return true;
        }

        if (id == R.id.clear){
            mediaPlayer.start();
            refreshDialog(); //refresh
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        paintColor = color;
        imageView.invalidate();
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }
}