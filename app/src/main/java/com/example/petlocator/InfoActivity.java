package com.example.petlocator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.example.petlocator.databinding.ActivityInfoBinding;

public class InfoActivity extends AppCompatActivity {

    ActivityInfoBinding binding;
    private static final int REQUEST_CODE = 100; // You can use any integer value
    public static final int NOTIFICATION_ID = 1;

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


        binding.btNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(InfoActivity.this, Notification_Activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                // Create a PendingIntent for the Intent
                PendingIntent pendingIntent = PendingIntent.getActivity(InfoActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
                // Check if the app has the POST_NOTIFICATIONS permission
                if (ContextCompat.checkSelfPermission(InfoActivity.this, Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED) {

                    // Create a channel for notifications
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        CharSequence name = "MyChannel";
                        String description = "MyChannel Description";
                        int importance = NotificationManager.IMPORTANCE_DEFAULT;
                        NotificationChannel channel = new NotificationChannel("1", name, importance);
                        channel.setDescription(description);
                        channel.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.menu_music), null); // You need to provide the music_menu sound file in the raw folder
                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
                        notificationManager.createNotificationChannel(channel);
                    }

                    // Create a notification
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(InfoActivity.this, "1")
                            .setSmallIcon(R.mipmap.notification) // You need to provide a notification icon
                            .setContentTitle("Haiii :3")
                            .setContentText("Thanks for using our app!")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(pendingIntent);

                    // Send the notification
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(InfoActivity.this);
                    notificationManager.notify(0, builder.build());

                } else {
                    // The app doesn't have the permission, request it
                    ActivityCompat.requestPermissions(InfoActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            REQUEST_CODE); // REQUEST_CODE is an integer you can define
                }
            }
        });

    }
}
