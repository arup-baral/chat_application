package com.example.android.quickchat.user;

import java.io.Serializable;

public class User implements Serializable {
    public String name, image, email, token, id;

    public User(String id, String name, String image, String email, String token) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.email = email;
        this.token = token;
    }

    public User() {}
}
