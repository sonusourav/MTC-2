package com.suliteos.towaso.user;

import com.google.firebase.firestore.GeoPoint;

public class User {

    private String name;
    private String state;
    private String imageUrl;
    private String district;
    private int ward;
    private String phone;
    private int perMonth;
    private String type;
    private String subType;
    private String houseHold;
    private String houseName;
    private String area;
    private GeoPoint loc;
    private int floor;
    private int house;

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public int getWard() {
        return ward;
    }

    public void setWard(int ward) {
        this.ward = ward;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getPerMonth() {
        return perMonth;
    }

    public void setPerMonth(int perMonth) {
        this.perMonth = perMonth;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getHouseHold() {
        return houseHold;
    }

    public void setHouseHold(String houseHold) {
        this.houseHold = houseHold;
    }

    public String getHouseName() {
        return houseName;
    }

    public void setHouseName(String houseName) {
        this.houseName = houseName;
    }

    public String getArea() {
        return area;
    }

    public GeoPoint getLoc() {
        return loc;
    }

    public void setLoc(GeoPoint loc) {
        this.loc = loc;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getHouse() {
        return house;
    }

    public void setHouse(int house) {
        this.house = house;
    }
}