package com.example.petlocator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class ShopAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<HashMap<String, Object>> shopItems;

    public ShopAdapter(Context context, ArrayList<HashMap<String, Object>> shopItems) {
        this.context = context;
        this.shopItems = shopItems;
    }

    @Override
    public int getCount() {
        return shopItems.size();
    }

    @Override
    public Object getItem(int position) {
        return shopItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.shop_list, parent, false);

        HashMap<String, Object> item = shopItems.get(position);
        boolean bought = (boolean) item.get("bought");

        ImageView itemImage = view.findViewById(R.id.item_image);
        itemImage.setImageResource((int) item.get("image"));

        TextView itemName = view.findViewById(R.id.item_name);
        itemName.setText((String) item.get("name"));

        TextView itemPrice = view.findViewById(R.id.item_price);
        if (bought) {
            itemPrice.setVisibility(View.GONE);
        } else {
            itemPrice.setVisibility(View.VISIBLE);
            itemPrice.setText("Price: " + item.get("price") + " Coins");
        }

        Button buyButton = view.findViewById(R.id.buy_button);
        if (bought) {
            buyButton.setVisibility(View.GONE);
        } else {
            buyButton.setVisibility(View.VISIBLE);
            buyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Реализовать логику покупки предмета
                }
            });
        }

        Button useButton = view.findViewById(R.id.use_button);
        if (bought) {
            useButton.setVisibility(View.VISIBLE);
            useButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Реализовать логику использования предмета
                }
            });
        } else {
            useButton.setVisibility(View.GONE);
        }

        return view;
    }
}
