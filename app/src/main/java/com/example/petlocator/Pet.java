package com.example.petlocator;

import android.net.Uri;

public class Pet {
    String species, age, petname, petId;
    private double latitude, longitude;
    private String  imageResource;
    public Pet(){}
    public Pet(String species, String age, String petname, String petId, double latitude, double longitude, String imageResource){
        this.age = age;
        this.species = species;
        this.petname = petname;
        this.petId = "PET" + System.currentTimeMillis(); //calling a method for a unique Id
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageResource = imageResource;
    }

    public String getAge() {return age;}
    public void setAge(String age) {this.age = age;}


    public String getSpecies() {return species;}
    public void setSpecies(String species) {this.species = species;}

    public String getpetName() {return petname;}
    public void setpetName(String name) {this.petname = name;}
    public String getPetId(){
        return petId;
    }

    public void setPetId(String petId) {
        this.petId = petId;
    }

    public double getLatitude() {return latitude;}

    public void setLatitude(double latitude) {this.latitude = latitude;}

    public double getLongitude() {return longitude;}

    public void setLongitude(double longitude) {this.longitude = longitude;}

    public String getImageResource() {return imageResource;}

    public void setImageResource(String imageResource) {this.imageResource = imageResource;}
}
