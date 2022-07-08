package com.example.foodo.objects;

public class RestaurantCard {
    private String name, address, rating, status, id;
    private double lat, lng;
    private boolean addButtonEnabled;

    public RestaurantCard(String name, String address, String rating, String status, String id, double lat, double lng, boolean addButtonEnabled){
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.status = status;
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.addButtonEnabled = addButtonEnabled;
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

    public double getLat() {
        return lat;
    }

    public double getLng(){
        return lng;
    }

    public boolean getAddButtonEnabled() {
        return addButtonEnabled;
    }
}
