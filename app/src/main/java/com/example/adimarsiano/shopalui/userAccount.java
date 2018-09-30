package com.example.adimarsiano.shopalui;

import android.net.Uri;

import java.net.URI;

public class userAccount {
    public String getDisplayName() {
        return displayName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    private String displayName;
    private String firstName;
    private String lastName;
    private String email;

    public Uri getImageUrl() {
        return imageUrl;
    }

    private Uri imageUrl;

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setImageUrl(Uri imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
