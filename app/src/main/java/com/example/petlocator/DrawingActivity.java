package com.example.petlocator;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petlocator.databinding.ActivityDrawingBinding;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.Stack;

public class DrawingActivity extends AppCompatActivity implements ColorPickerDialogListener  {

    private Bitmap bitmap;
    private Canvas canvas;
    private ImageView imageView;
    private int paintColor = Color.BLACK;
    MediaPlayer mediaPlayer;
    private Path drawPath;
    private Path fillPath;
    private Paint drawPaint;
    private Paint erasePaint;
    private int currentBrushSize = 20;
    private int currentBrushOpacity = 255;
    ActivityDrawingBinding binding;
    private boolean[][] processed;

    private int drawingMode = DRAWING_MODE_BRUSH;

    private static final int DRAWING_MODE_BRUSH = 0;
    private static final int DRAWING_MODE_EYEDROPPER = 1;
    private static final int DRAWING_MODE_ERASER = 2;

    private View colorIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDrawingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        imageView = findViewById(R.id.image_view);


        mediaPlayer = MediaPlayer.create(this, R.raw.click);


        colorIndicator = new View(this);

        colorIndicator.setLayoutParams(new LinearLayout.LayoutParams(100, 100));

        colorIndicator.setBackgroundColor(paintColor);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) colorIndicator.getLayoutParams();
        layoutParams.setMargins(0, 15, 0, 0);
        colorIndicator.setLayoutParams(layoutParams);

        binding.drawingLayout.addView(colorIndicator);

        // Set up touch events for drawing
        imageView.setOnTouchListener(new View.OnTouchListener() {
            private float lastX, lastY;
            private boolean isDrawing = false;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (drawingMode == DRAWING_MODE_BRUSH) { //BRUSH
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
                                    // Save the current point as the end of the previous line
                                    float midX = (event.getX() + lastX) / 2;
                                    float midY = (event.getY() + lastY) / 2;
                                    drawPath.quadTo(lastX, lastY, midX, midY);
                                    canvas.drawPath(drawPath, drawPaint);
                                    drawPath.reset();
                                    drawPath.moveTo(midX, midY);

                                    // Draw a line between the previous and current points with the desired opacity
                                    drawPaint.setAlpha(currentBrushOpacity);
                                    canvas.drawLine(lastX, lastY, midX, midY, drawPaint);

                                    lastX = event.getX();
                                    lastY = event.getY();
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isDrawing = false;
                            drawPath.lineTo(event.getX(), event.getY());
                            // Draw the final line between the previous and current points with the desired opacity
                            drawPaint.setAlpha(currentBrushOpacity);
                            canvas.drawPath(drawPath, drawPaint);
                            canvas.drawLine(lastX, lastY, event.getX(), event.getY(), drawPaint);
                            break;
                        default:
                            return false;
                    }
                } else if (drawingMode == DRAWING_MODE_EYEDROPPER) { //EYEDROPPER
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        // Взять цвет пикселя по координатам нажатия
                        int color = bitmap.getPixel((int) event.getX(), (int) event.getY());
                        // Установить его в качестве текущего цвета кисти
                        paintColor = color;
                        drawPaint.setColor(paintColor);

                        colorIndicator.setBackgroundColor(paintColor);
                    }
                } else if (drawingMode == DRAWING_MODE_ERASER) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            isDrawing = true;
                            lastX = event.getX();
                            lastY = event.getY();
                            drawPath.reset();
                            drawPath.moveTo(lastX, lastY);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (isDrawing){
                                float dx = Math.abs(event.getX() - lastX);
                                float dy = Math.abs(event.getY() - lastY);
                                if (dx >= 4 || dy >= 4) {
                                    // Save the current point as the end of the previous line
                                    float midX = (event.getX() + lastX) / 2;
                                    float midY = (event.getY() + lastY) / 2;
                                    drawPath.quadTo(lastX, lastY, midX, midY);
                                    canvas.drawPath(drawPath, erasePaint);
                                    drawPath.reset();
                                    drawPath.moveTo(midX, midY);

                                    // Draw a line between the previous and current points with the desired opacity
                                    canvas.drawLine(lastX, lastY, midX, midY, erasePaint);

                                    lastX = event.getX();
                                    lastY = event.getY();
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isDrawing = false;
                            drawPath.lineTo(event.getX(), event.getY());
                            canvas.drawPath(drawPath, erasePaint);
                            canvas.drawLine(lastX, lastY, event.getX(),event.getY(),erasePaint);
                            break;
                    }
                    erasePaint.setXfermode(null);
                }

                // Update the image view to display the new canvas
                imageView.setImageBitmap(bitmap);

                // Return true to indicate that the touch event was handled
                return true;
            }
        });


        binding.brushes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeBrushDialog();
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

                erasePaint = new Paint();
                erasePaint.setStyle(Paint.Style.STROKE);
                erasePaint.setStrokeJoin(Paint.Join.ROUND);
                erasePaint.setStrokeCap(Paint.Cap.ROUND);
                erasePaint.setStrokeWidth(currentBrushSize);
                erasePaint.setColor(Color.WHITE);
                erasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

                processed = new boolean[bitmap.getWidth()][bitmap.getHeight()];

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

        final SeekBar sizeSeekBar = dialogView.findViewById(R.id.brush_size_seek_bar);
        sizeSeekBar.setProgress(currentBrushSize);

        final SeekBar opacitySeekBar = dialogView.findViewById(R.id.brush_opacity_seek_bar);
        int initialOpacity = (int) (currentBrushOpacity * 100 / 255);
        opacitySeekBar.setProgress(initialOpacity);

        sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawPaint.setStrokeWidth(progress);
                currentBrushSize = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        opacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentBrushOpacity = (int) (progress * 255 / 100);
                drawPaint.setARGB(currentBrushOpacity, Color.red(paintColor), Color.green(paintColor), Color.blue(paintColor));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        dialogBuilder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int brushSize = sizeSeekBar.getProgress();
                drawPaint.setStrokeWidth(brushSize);
                currentBrushSize = brushSize;

                int brushOpacity = opacitySeekBar.getProgress();
                currentBrushOpacity = (int) (brushOpacity * 255 / 100);
                drawPaint.setARGB(currentBrushOpacity, Color.red(paintColor), Color.green(paintColor), Color.blue(paintColor));
            }
        });

        dialogBuilder.setNegativeButton("Отмена", null);

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }


    private void showChangeBrushDialog(){
        View dialogView = LayoutInflater.from(DrawingActivity.this).inflate(R.layout.brush_change_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(DrawingActivity.this);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        ImageButton brushButton = dialogView.findViewById(R.id.changeBrush);
        brushButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingMode = DRAWING_MODE_BRUSH;
                alertDialog.dismiss();
            }
        });

        ImageButton pipetteButton = dialogView.findViewById(R.id.changeColorPicker);
        pipetteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingMode = DRAWING_MODE_EYEDROPPER;
                alertDialog.dismiss();
                // Тут вы можете добавить код для перехода в режим пипетки
            }
        });


        ImageButton eraserButton = dialogView.findViewById(R.id.changeEraser);
        eraserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingMode = DRAWING_MODE_ERASER;
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void floodFill(int x, int y, int targetColor, int newColor) {
        if (x < 0 || x >= bitmap.getWidth() || y < 0 || y >= bitmap.getHeight()) {
            return;
        }

        int currentColor = bitmap.getPixel(x, y);
        if (currentColor != targetColor || processed[x][y]) {
            return;
        }

        bitmap.setPixel(x, y, newColor);
        processed[x][y] = true;

        Stack<Integer> stack = new Stack<>();
        stack.push(x);
        stack.push(y);

        while (!stack.isEmpty()) {
            y = stack.pop();
            x = stack.pop();

            if (x < 0 || x >= bitmap.getWidth() || y < 0 || y >= bitmap.getHeight()) {
                continue;
            }

            currentColor = bitmap.getPixel(x, y);
            if (currentColor != targetColor || processed[x][y]) {
                continue;
            }

            bitmap.setPixel(x, y, newColor);
            processed[x][y] = true;

            stack.push(x - 1);
            stack.push(y);
            stack.push(x + 1);
            stack.push(y);
            stack.push(x);
            stack.push(y - 1);
            stack.push(x);
            stack.push(y + 1);
        }

        imageView.invalidate();
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

            return true;
        }

        if (id == R.id.btnSave){
            mediaPlayer.start();
            showSaveDialog();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        paintColor = color;
        drawPaint.setColor(paintColor);
        drawPaint.setAlpha(currentBrushOpacity);
        imageView.invalidate();

        // Вызываем метод для изменения цвета кисти и обновления цвета фона View для индикатора цвета
        changeBrushColor(color);
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    private void changeBrushColor(int newColor) {

        colorIndicator.setBackgroundColor(paintColor);
    }

}