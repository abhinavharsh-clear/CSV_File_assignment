package com.example.demo.model;

public class User {

    private int id;
    private String email;
    private String name;

    public User(int id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }

    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
}
