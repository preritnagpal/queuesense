package com.queuesense.model;

public class User {

    private String name;
    private String email;
    private int token;
    private int position;
    private int waitTime;

    public User(String name, String email, int token, int position, int waitTime) {
        this.name = name;
        this.email = email;
        this.token = token;
        this.position = position;
        this.waitTime = waitTime;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getToken() { return token; }
    public int getPosition() { return position; }
    public int getWaitTime() { return waitTime; }
}