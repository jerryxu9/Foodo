package com.example.foodo.objects;

public class RestaurantCard {

    private String name;
    private String address;
    private String rating;
    private String status;
    private final String googlePlacesID;
    private final String cardID;
    private String username;
    private final String userID;
    private double lat;
    private double lng;
    private final boolean isInFoodoList;
    private boolean isVisited;

    public RestaurantCard(String googlePlacesID, String cardID, boolean isInFoodoList, String userID) {
        // Make a distinction between the restaurant card ID (as stored in the database)
        // and the google place_id of the restaurant the RestaurantCard represents
        this.googlePlacesID = googlePlacesID;
        this.cardID = cardID;
        this.isInFoodoList = isInFoodoList;
        this.isVisited = false;
        this.userID = userID;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setLat(double lat){
        this.lat = lat;
    }

    public void setLng(double lng){
        this.lng = lng;
    }

    public void setRestaurantName(String name){
        this.name = name;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public void setRating(String rating){
        this.rating = rating;
    }

    public void setStatus(String status){
        this.status = status;
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

    public boolean getVisited() {
        return this.isVisited;
    }

    public void setVisited(boolean isVisited) {
        this.isVisited = isVisited;
    }

    public String getUsername() {
        return username;
    }

    public String getUserID() {
        return userID;
    }

}
