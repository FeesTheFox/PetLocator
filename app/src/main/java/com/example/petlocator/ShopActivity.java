package com.example.petlocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.petlocator.databinding.ActivityShopBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ShopActivity extends AppCompatActivity {
    ActivityShopBinding binding;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String userId;
    private TextView clicksValueTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Инициализируем Firebase Database и Firebase Auth
        firebaseDatabase = FirebaseDatabase.getInstance("https://petlocator-d7771-default-rtdb.firebaseio.com/");
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            // Получаем уникальный идентификатор пользователя
            userId = firebaseUser.getUid();

            // Загружаем клики из базы данных Firebase
            DatabaseReference clicksRef = firebaseDatabase.getReference("Users").child(userId).child("clicks");
            clicksRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Getting clicks from database
                    int clicks = snapshot.getValue(Integer.class);

                    // Отображаем количество кликов в монетах на экране
                    clicksValueTextView.setText(String.valueOf(clicks));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

        // Getting link
        clicksValueTextView = findViewById(R.id.coin_count_text);


        ArrayList<HashMap<String, Object>> shopItems = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Object> item1 = new HashMap<String, Object>();
        item1.put("id", "1");
        item1.put("Имя", "Волчица древних грёз");
        item1.put("Цена", "150");
        item1.put("Изображение", R.drawable.img_resize);
        shopItems.add(item1);

        HashMap<String, Object> item2 = new HashMap<String, Object>();
        item2.put("id", "2");
        item2.put("Имя", "Тигрица великая");
        item2.put("Цена", "300");
        item2.put("Изображение", R.drawable.img_resize2);
        shopItems.add(item2);

        HashMap<String, Object> item3 = new HashMap<String, Object>();
        item3.put("id", "3");
        item3.put("Имя", "Лиса потерянных надежд");
        item3.put("Цена", "750");
        item3.put("Изображение", R.drawable.img_resize3);
        shopItems.add(item3);

        HashMap<String, Object> item4 = new HashMap<String, Object>();
        item4.put("id", "4");
        item4.put("Имя", "Красная панда");
        item4.put("Цена", "2500");
        item4.put("Изображение", R.drawable.img_resize4);
        shopItems.add(item4);

        HashMap<String, Object> item5 = new HashMap<String, Object>();
        item5.put("id", "5");
        item5.put("Имя", "Львица прекрасная");
        item5.put("Цена", "5000");
        item5.put("Изображение", R.drawable.img_resize5);
        shopItems.add(item5);


        SimpleAdapter adapter = new SimpleAdapter(
                this,
                shopItems,
                R.layout.shop_list,
                new String[] {"Изображение","Имя", "Цена"},
                new int[] {R.id.item_image, R.id.item_name, R.id.item_price}
        );

        ListView shopList = (ListView) findViewById(R.id.shop_list);
        shopList.setAdapter(adapter);
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