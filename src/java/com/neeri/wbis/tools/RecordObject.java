package com.neeri.wbis.tools;

import java.util.ArrayList;

public class RecordObject {

    private String title;
    private String description;
    private double latitude, longitude;
    private ArrayList<String> images;
    private String UName;
    private String UEmail;
    private String UContact;
    private String record_id;

    public String getRecord_id() {
        return record_id;
    }

    public void setRecord_id(String record_id) {
        this.record_id = record_id;
    }

    public RecordObject() {
    }

    public RecordObject(String title, String description, double latitude, double longitude,
                        ArrayList<String> images, String user_name, String user_email, String user_contact) {
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.images = images;
        this.UName = user_name;
        this.UEmail = user_email;
        this.UContact = user_contact;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public void setUName(String UName) {
        this.UName = UName;
    }

    public void setUEmail(String UEmail) {
        this.UEmail = UEmail;
    }

    public void setUContact(String UContact) {
        this.UContact = UContact;
    }

    public String getDescription() {
        return this.description;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public ArrayList<String> getImages() {
        return this.images;
    }

    public String getUName() {
        return this.UName;
    }

    public String getUEmail() {
        return this.UEmail;
    }

    public String getUContact() {
        return this.UContact;
    }
}
