package com.example.foodo.objects;

public class FoodoListCard {
    private final String id;
    private final String username;
    private final String userID;
    private String name;

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
