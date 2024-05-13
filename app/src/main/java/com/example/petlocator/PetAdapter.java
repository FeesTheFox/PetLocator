package com.example.petlocator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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

        speciesTextView.setText(currentPet.getSpecies());
        ageTextView.setText(currentPet.getAge());
        nameTextView.setText(currentPet.getpetName());

        return convertView;
    }
}
