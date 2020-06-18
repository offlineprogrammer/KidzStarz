package com.stuartminion.kidzstarz.user;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class User {

    private String userId;
    private String userEmail;
    private Date dateCreated;

    public User(String userId, String userEmail, Date dateCreated) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.dateCreated=dateCreated;
    }

    public User() {

    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", this.userId);
        result.put("userEmail", this.userEmail);
        result.put("dateCreated", this.dateCreated);
        return result;
    }
}
