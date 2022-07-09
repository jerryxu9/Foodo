package com.example.foodo.objects;

public class FoodoListCard {
    private String name;
    private String id;
    private String username;
    private String userID;

    public FoodoListCard(String name, String id, String username, String userID) {
        this.name = name;
        this.id = id;
        this.userID = userID;
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }
}
