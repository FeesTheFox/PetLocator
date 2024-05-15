package com.example.petlocator;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

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

        //Vector resource image
        Drawable drawable = AppCompatResources.getDrawable(mContext, R.drawable.img);
        imageView.setImageDrawable(drawable);

        return convertView;
    }

}



