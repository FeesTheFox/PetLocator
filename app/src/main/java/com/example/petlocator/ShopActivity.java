    package com.example.petlocator;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;

    import android.os.Bundle;
    import android.util.Log;
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
    import com.google.android.gms.tasks.OnCompleteListener;
    import com.google.android.gms.tasks.Task;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.Logger;
    import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;



    public class ShopActivity extends AppCompatActivity {
        ActivityShopBinding binding;
        FirebaseDatabase firebaseDatabase;
        private ShopAdapter shopAdapter;
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

                DatabaseReference purchasesRef = firebaseDatabase.getReference("Users").child(userId).child("Purchases");
                purchasesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            // Если узла "Purchases" нет, то создаем его
                            purchasesRef.setValue(new HashMap<>());
                        }
                        // Создаем список идентификаторов купленных предметов
                        ArrayList<String> purchasedIds = new ArrayList<>();
                        for (DataSnapshot purchaseSnapshot : snapshot.getChildren()) {
                            HashMap<String, Object> item = (HashMap<String, Object>) purchaseSnapshot.getValue();
                            String itemId = (String) item.get("id");
                            purchasedIds.add(itemId);
                            Log.d("FirebaseIDPURCHASED", String.valueOf(purchasedIds));
                        }

                        // Обновляем элементы в списке
                        for (int i = 0; i < shopAdapter.getCount(); i++) {
                            HashMap<String, Object> shopItem = (HashMap<String, Object>) shopAdapter.getItem(i);
                            String itemId = (String) shopItem.get("id");
                            Log.d("FirebaseIDPURCHASE", itemId );
                            if (purchasedIds.contains(itemId)) {
                                shopItem.put("bought", true);
                                shopItem.put("buy_button_visibility", View.GONE);
                                shopItem.put("use_button_visibility", View.VISIBLE);
                            }
                        }

                        // Обновляем отображение списка
                        shopAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error loading purchases", error.toException());
                    }
                });
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
                        Log.e("Firebase", "Error loading clicks", error.toException());
                    }
                });

                // Создаем список для элементов магазина
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

                // Создаем адаптер для списка
                shopAdapter = new ShopAdapter(ShopActivity.this, shopItems);

                // Назначаем адаптер списку
                ListView shopList = binding.shopList;
                shopList.setAdapter(shopAdapter);

                shopList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // Получаем информацию о выбранном предмете
                        HashMap<String, Object> selectedItem = (HashMap<String, Object>) parent.getItemAtPosition(position);

                        if (!(boolean) selectedItem.get("bought")) {
                            // Получаем цену предмета
                            int price = (int) selectedItem.get("price");

                            // Проверяем, что у пользователя достаточно монет для покупки
                            if (clicks >= price) {
                                // Вычитаем цену предмета из количества монет пользователя
                                clicks -= price;

                                // Обновляем количество монет пользователя в базе данных Firebase
                                DatabaseReference userClicksRef = firebaseDatabase.getReference("Users").child(userId).child("clicks");
                                userClicksRef.setValue(clicks);

                                // Создаем новую запись для покупки в узле Purchases для текущего пользователя в базе данных Firebase
                                DatabaseReference purchaseRef = firebaseDatabase.getReference("Users").child(userId).child("Purchases").push();

                                // Создаем карту с информацией о покупке
                                HashMap<String, Object> purchaseMap = new HashMap<>();
                                purchaseMap.put("id", selectedItem.get("id"));
                                purchaseMap.put("name", selectedItem.get("name"));
                                purchaseMap.put("price", selectedItem.get("price"));
                                purchaseMap.put("image", selectedItem.get("image"));

                                // Сохраняем информацию о покупке в базе данных Firebase
                                purchaseRef.setValue(purchaseMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Обновляем информацию о предмете в списке, устанавливая флаг bought в true
                                            selectedItem.put("bought", true);
                                            selectedItem.put("buy_button_visibility", View.GONE);
                                            selectedItem.put("use_button_visibility", View.VISIBLE);

                                            // Обновляем отображение списка
                                            shopAdapter.notifyDataSetChanged();

                                            // Показываем сообщение об успешной покупке
                                            Toast.makeText(ShopActivity.this, "Вы купили " + selectedItem.get("name"), Toast.LENGTH_SHORT).show();
                                        } else {
                                            // Показываем сообщение об ошибке при покупке
                                            Toast.makeText(ShopActivity.this, "Произошла ошибка при покупке", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                // Показываем сообщение об недостаточном количестве монет для покупки
                                Toast.makeText(ShopActivity.this, "Недостаточно монет-гавов", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Показываем сообщение об том, что предмет уже куплен
                            Toast.makeText(ShopActivity.this, "Этот предмет уже куплен", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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

                // Обновить значение bought для соответствующего предмета в списке shopItems
                for (int i = 0; i < shopAdapter.getCount(); i++) {
                    HashMap<String, Object> item = (HashMap<String, Object>) shopAdapter.getItem(i);
                    if (item.get("name").equals(itemName)) {
                        item.put("bought", true);
                        item.put("buy_button_visibility", View.GONE);
                        item.put("use_button_visibility", View.VISIBLE);
                        break;
                    }
                }

                // Обновить отображение списка
                shopAdapter.notifyDataSetChanged();

                Toast.makeText(ShopActivity.this, "Вы купили " + itemName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ShopActivity.this, "Недостаточно монет-гавов", Toast.LENGTH_SHORT).show();
            }
        }

        public void onUseButtonClicked(View view) {
            // Реализовать логику использования предмета
            Toast.makeText(ShopActivity.this, "Вы использовали предмет", Toast.LENGTH_SHORT).show();
        }

        public String getUserId() {
            return userId;
        }
    }