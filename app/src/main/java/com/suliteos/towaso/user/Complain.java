package com.suliteos.towaso.user;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

class Complain {

    private String title;
    private String desc;
    private GeoPoint loc;
    private String email;
    private String holding;
    private String phone;
    private String imageUrl;
    private String status;
    private Date timeStamp;
    private String key;

    public Complain() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    void setDesc(String desc) {
        this.desc = desc;
    }

    public GeoPoint getLoc() {
        return loc;
    }

    public void setLoc(GeoPoint loc) {
        this.loc = loc;
    }

    public String getEmail() {
        return email;
    }

    void setEmail(String email) {
        this.email = email;
    }

    public String getHolding() {
        return holding;
    }

    public void setHolding(String holding) {
        this.holding = holding;
    }

    public String getPhone() {
        return phone;
    }

    void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
