package com.suliteos.towaso.user;

class Payment {

    private String isPaid;
    private long time;
    private String key;
    private String month;

    public Payment() {
    }

    String getKey() {
        return key;
    }

    void setKey(String key) {
        this.key = key;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    String getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(String isPaid) {
        this.isPaid = isPaid;
    }

    String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
}
