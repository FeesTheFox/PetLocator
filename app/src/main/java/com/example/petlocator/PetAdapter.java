package com.example.petlocator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class PetAdapter extends ArrayAdapter<Pet> {
    private Context mContext;
    private List<Pet> mPets;

    public PetAdapter(Context context, ArrayList<Pet> pets) {
        super(context, 0, pets);
        mContext = context;
        mPets = pets;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.pet, parent, false);
        }

        Pet currentPet = mPets.get(position);

        TextView speciesTextView = convertView.findViewById(R.id.speciesTextView);
        TextView ageTextView = convertView.findViewById(R.id.ageTextView);
        TextView nameTextView = convertView.findViewById(R.id.nameTextView);
        ImageView imageView = convertView.findViewById(R.id.pet_image);

        speciesTextView.setText(currentPet.getSpecies());
        ageTextView.setText(currentPet.getAge());
        nameTextView.setText(currentPet.getpetName());


        RelativeLayout rootLayout = convertView.findViewById(R.id.root);
        rootLayout.setBackgroundResource(R.drawable.list_item_background);

        // Use Glide to load the pet's image from the Firebase Storage
        Glide.with(mContext)
                .load(currentPet.getImageResource())
                .transform(new RoundedCornersTransformation(16))
                .placeholder(R.drawable.img) // You can set a placeholder drawable to display while the image is loading
                .into(imageView);

        return convertView;
    }

}


