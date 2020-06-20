package com.offlineprogrammer.kidzstarz.kid;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Kid implements Parcelable {
    private String kidName;
    private String monsterImageResourceName;
    private Date createdDate;
    private int happyStarz;
    private int sadStarz;


    public Kid(String kidName, String monsterImageResourceName, Date createdDate) {

        this.kidName = kidName;
        this.monsterImageResourceName = monsterImageResourceName;
        this.createdDate = createdDate;
    }

    public Kid() {

    }


    protected Kid(Parcel in) {
        kidName = in.readString();
        monsterImageResourceName = in.readString();

    }

    public static final Creator<Kid> CREATOR = new Creator<Kid>() {
        @Override
        public Kid createFromParcel(Parcel in) {
            return new Kid(in);
        }

        @Override
        public Kid[] newArray(int size) {
            return new Kid[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(kidName);
        dest.writeString(monsterImageResourceName);

    }

    @NonNull
    public String getKidName() {
        return kidName;
    }

    public void setKidName(@NonNull final String kidName) {
        this.kidName = kidName;
    }


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("kidName", this.kidName);
        result.put("monsterImageResourceName", this.monsterImageResourceName);
        result.put("createdDate", this.createdDate);

        return result;
    }

    @Override
    public String toString() {
        return "Kid{" +

                ", kidName='" + kidName + '\'' +
                '}';
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }


    public String getMonsterImageResourceName() {
        return monsterImageResourceName;
    }

    public void setMonsterImageResourceName(String monsterImageResourceName) {
        this.monsterImageResourceName = monsterImageResourceName;
    }

    public int getSadStarz() {
        return sadStarz;
    }

    public void setSadStarz(int sadStarz) {
        this.sadStarz = sadStarz;
    }

    public int getHappyStarz() {
        return happyStarz;
    }

    public void setHappyStarz(int happyStarz) {
        this.happyStarz = happyStarz;
    }
}
