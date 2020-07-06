package com.offlineprogrammer.kidzstarz.starz;

import java.util.Calendar;
import java.util.Date;

public class Starz {
    private Date createdDate;
    private String kidUUID;
    private String desc;
    private Integer count;
    private String type;
    private String firestoreImageUri;


    public Starz(String kidUUID, String desc, Integer count, String type) {
        this.kidUUID = kidUUID;
        this.desc = desc;
        this.count = count;
        this.type = type;
        this.createdDate = Calendar.getInstance().getTime();
    }

    public Starz() {

    }

    public String getKidUUID() {
        return kidUUID;
    }

    public void setKidUUID(String kidUUID) {
        this.kidUUID = kidUUID;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getFirestoreImageUri() {
        return firestoreImageUri;
    }

    public void setFirestoreImageUri(String firestoreImageUri) {
        this.firestoreImageUri = firestoreImageUri;
    }
}
