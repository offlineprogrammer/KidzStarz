package com.offlineprogrammer.kidzstarz.user;

import com.google.firebase.database.Exclude;
import com.offlineprogrammer.kidzstarz.kid.Kid;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class User {

    private String userId;
    private String userEmail;
    private Date dateCreated;
    private ArrayList<Kid> kidz = new ArrayList<>();
    private String fcmInstanceId;

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
        result.put("fcmInstanceId", this.fcmInstanceId);
        result.put("kidz", this.kidz);
        return result;
    }

    public ArrayList<Kid> getKidz() {
        return kidz;
    }

    public void setKidz(ArrayList<Kid> kidz) {
        this.kidz = kidz;
    }

    public String getFcmInstanceId() {
        return fcmInstanceId;
    }

    public void setFcmInstanceId(String fcmInstanceId) {
        this.fcmInstanceId = fcmInstanceId;
    }
}
