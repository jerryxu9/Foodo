package com.example.foodo.objects;

public class RestaurantCard {
    private final String name, address, rating, status, googlePlacesID, cardID;
    private final double lat, lng;
    private final boolean isInFoodoList;

    public RestaurantCard(String name, String address, String rating, String status, String googlePlacesID, String cardID, double lat, double lng, boolean isInFoodoList) {
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.status = status;
        this.googlePlacesID = googlePlacesID;
        this.cardID = cardID;
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

    public String getGooglePlacesID() {
        return googlePlacesID;
    }

    public String getCardID() {
        return cardID;
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
