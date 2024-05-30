package com.example.petlocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String userId;
    private int clicks;

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
                    // Получаем количество кликов из базы данных Firebase
                    clicks = snapshot.getValue(Integer.class);

                    // Отображаем количество кликов на экране
                    binding.coinCountText.setText(String.valueOf(clicks));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Обрабатываем ошибку загрузки данных из базы данных Firebase
                }
            });
        }

        ArrayList<HashMap<String, Object>> shopItems = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Object> item1 = new HashMap<String, Object>();
        item1.put("id", "1");
        item1.put("name", "Волчица древних грёз");
        item1.put("price", 0);
        item1.put("image", R.drawable.img_resize);
        item1.put("bought", true);
        item1.put("buy_button_visibility", View.GONE);
        item1.put("use_button_visibility", View.VISIBLE);
        shopItems.add(item1);

        HashMap<String, Object> item2 = new HashMap<String, Object>();
        item2.put("id", "2");
        item2.put("name", "Тигрица великая");
        item2.put("price", 300);
        item2.put("image", R.drawable.img_resize2);
        item2.put("bought", false);
        item2.put("buy_button_visibility", View.VISIBLE);
        item2.put("use_button_visibility", View.GONE);
        shopItems.add(item2);

        HashMap<String, Object> item3 = new HashMap<String, Object>();
        item3.put("id", "3");
        item3.put("name", "Лиса потерянных надежд");
        item3.put("price", 750);
        item3.put("image", R.drawable.img_resize3);
        item3.put("bought",false);
        item3.put("buy_button_visibility", View.VISIBLE);
        item3.put("use_button_visibility", View.GONE);
        shopItems.add(item3);

        HashMap<String, Object> item4 = new HashMap<String, Object>();
        item4.put("id", "4");
        item4.put("name", "Красная панда");
        item4.put("price", 2500);
        item4.put("image", R.drawable.img_resize4);
        item4.put("bought",false);
        item4.put("buy_button_visibility", View.VISIBLE);
        item4.put("use_button_visibility", View.GONE);
        shopItems.add(item4);

        HashMap<String, Object> item5 = new HashMap<String, Object>();
        item5.put("id", "5");
        item5.put("name", "Львица прекрасная");
        item5.put("price", 5000);
        item5.put("image", R.drawable.img_resize5);
        item5.put("bought",false);
        item5.put("buy_button_visibility", View.VISIBLE);
        item5.put("use_button_visibility", View.GONE);
        shopItems.add(item5);

        ShopAdapter adapter = new ShopAdapter(this, shopItems);
        ListView shopList = binding.shopList;
        shopList.setAdapter(adapter);

        shopList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, Object> item = (HashMap<String, Object>) parent.getItemAtPosition(position);
                String itemName = (String) item.get("name");
                int itemPrice = (int) item.get("price");
                boolean itemBought = (boolean) item.get("bought");

                if (itemBought) {
                    Toast.makeText(ShopActivity.this, "Этот предмет уже куплен", Toast.LENGTH_SHORT).show();
                } else if (clicks >= itemPrice) {
                    clicks -= itemPrice;
                    binding.coinCountText.setText(String.valueOf(clicks));
                    item.put("bought", true);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(ShopActivity.this, "Вы купили " + itemName, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShopActivity.this, "Недостаточно монет-гавов", Toast.LENGTH_SHORT).show();
                }
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
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void onBuyButtonClicked(View view) {
        View parent = (View) view.getParent();
        TextView itemNameTextView = parent.findViewById(R.id.item_name);
        TextView itemPriceTextView = parent.findViewById(R.id.item_price);
        Button buyButton = parent.findViewById(R.id.buy_button);
        Button useButton = parent.findViewById(R.id.use_button);

        String itemName = itemNameTextView.getText().toString();
        int itemPrice = Integer.parseInt(itemPriceTextView.getText().toString().split(" ")[1]);

        if (clicks >= itemPrice) {
            clicks -= itemPrice;
            binding.coinCountText.setText(String.valueOf(clicks));

            // Обновить базу данных Firebase
            DatabaseReference userClicksRef = firebaseDatabase.getReference("Users").child(userId).child("clicks");
            userClicksRef.setValue(clicks);

            buyButton.setVisibility(View.GONE);
            useButton.setVisibility(View.VISIBLE);
            itemPriceTextView.setVisibility(View.GONE);

            Toast.makeText(ShopActivity.this, "Вы купили " + itemName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ShopActivity.this, "Недостаточно монет-гавов", Toast.LENGTH_SHORT).show();
        }
    }

    public void onUseButtonClicked(View view) {
        // Реализовать логику использования предмета
        Toast.makeText(ShopActivity.this, "Вы использовали предмет", Toast.LENGTH_SHORT).show();
    }
}