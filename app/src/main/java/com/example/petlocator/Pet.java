package com.example.petlocator;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

public class Pet {
    String species, age, owner, petname, petId;
    private double latitude, longitude;
    public Pet(){}
    public Pet(String species, String age, String owner, String petname, String petId, double latitude, double longitude){
        this.age = age;
        this.species = species;
        this.owner = owner;
        this.petname = petname;
        this.petId = "PET" + System.currentTimeMillis(); //calling a method for an unique Id
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAge() {return age;}
    public void setAge(String age) {this.age = age;}

    public String getOwner() {return owner;}
    public void setOwner(String owner) {this.owner = owner;}

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


}
