package com.example.foodo.objects;

public class RestaurantCard {
    private final String name;
    private final String address;
    private final String rating;
    private final String status;
    private final String id;
    private final double lat;
    private final double lng;
    private final boolean isInFoodoList;

    public RestaurantCard(String name, String address, String rating, String status, String id, double lat, double lng, boolean isInFoodoList) {
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.status = status;
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.isInFoodoList = isInFoodoList;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getRating() {
        return rating;
    }

    public String getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public boolean getInFoodoList() {
        return isInFoodoList;
    }
}
