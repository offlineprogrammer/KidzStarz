package com.offlineprogrammer.kidzstarz;

import android.app.Application;

import com.offlineprogrammer.kidzstarz.user.User;

public class KidzStarz extends Application {
    private User m_User;

    @Override
    public void onCreate() {
        super.onCreate();
    }



    public User getUser() {
        return m_User;
    }

    public void setUser(User user) {
        this.m_User = user;
    }



}
