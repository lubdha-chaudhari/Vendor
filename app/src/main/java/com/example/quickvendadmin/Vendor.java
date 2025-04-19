package com.example.quickvendadmin;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.PropertyName;

import java.util.List;

public class Vendor {

    private String stallName;
    private String category;
    private String contactNumber;
    private boolean isOnline;
    private String address;
    private GeoPoint location; // Store latitude and longitude
    private List<MenuItem> menuItems; // List of menu items
    private String profileImageEncrypted; // URL of profile image
    private String timestamp; // Timestamp of when the profile was created/updated

    // Default constructor required for Firestore
    public Vendor() {}

    public Vendor(String stallName, String category, String contactNumber, boolean isOnline,
                  String address, GeoPoint location, List<MenuItem> menuItems,
                  String profileImageEncrypted, String timestamp) {
        this.stallName = stallName;
        this.category = category;
        this.contactNumber = contactNumber;
        this.isOnline = isOnline;
        this.address = address;
        this.location = location;
        this.menuItems = menuItems;
        this.profileImageEncrypted = profileImageEncrypted;
        this.timestamp = timestamp;
    }

    public String getStallName() {
        return stallName;
    }

    public void setStallName(String stallName) {
        this.stallName = stallName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    @PropertyName("isOnline")
    public boolean isOnline() {
        return isOnline;
    }

    @PropertyName("isOnline")
    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    public String getProfileImageEncrypted() {
        return profileImageEncrypted;
    }

    public void setProfileImageEncrypted(String profileImageEncrypted) {
        this.profileImageEncrypted = profileImageEncrypted;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
