package com.example.gotix.model;

public class Event {
    private String id;
    private String title;
    private String date;
    private String location;
    private String category;

    public Event(String id, String title, String date, String location, String category) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.location = location;
        this.category = category;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }
}
