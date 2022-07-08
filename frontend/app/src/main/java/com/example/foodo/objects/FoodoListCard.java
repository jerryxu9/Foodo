package com.example.foodo.objects;

public class FoodoListCard {
    private String name;
    private String id;

    public FoodoListCard(String name, String id) {
        this.name = name;
        this.id = id;
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
}
