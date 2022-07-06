package com.example.foodo.objects;

public class RestaurantCard {
    private String name, address, rating, status, id;

    public RestaurantCard(String name, String address, String rating, String status, String id){
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.status = status;
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public String getAddress(){
        return address;
    }

    public String getRating(){
        return rating;
    }

    public String getStatus(){
        return status;
    }

    public String getId(){
        return id;
    }
}
