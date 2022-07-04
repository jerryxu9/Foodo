package com.example.foodo.objects;

public class RestaurantCard {
    private String name, address, rating, phoneNumber, status, id;

    public RestaurantCard(String name, String address, String rating, String phoneNumber, String status, String id){
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public String getAddress(){
        return address;
    }

    public void setRating(String rating){
        this.rating = rating;
    }

    public String getRating(){
        return rating;
    }

    public void setPhoneNumber(String rating){
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public String getStatus(){
        return status;
    }

    public String getId(){
        return id;
    }

}
