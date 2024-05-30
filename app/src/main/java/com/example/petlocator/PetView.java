package com.example.petlocator;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

public class PetView extends AppCompatImageView {

    private Pet pet;

    public PetView(Context context) {
        super(context);
    }

    public PetView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

}
