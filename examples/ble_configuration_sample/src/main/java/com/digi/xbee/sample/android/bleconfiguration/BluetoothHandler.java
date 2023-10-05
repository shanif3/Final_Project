package com.digi.xbee.sample.android.bleconfiguration;

public class BluetoothHandler {

    private String deviceAddress = "";

    // Private static instance variable to hold the single instance of the class.
    private static BluetoothHandler instance;

    // Private constructor to prevent instantiation from other classes.
    private BluetoothHandler() {
        // Initialize your instance variables here if needed.
    }

    // Public method to provide access to the single instance of the class.
    public static BluetoothHandler getInstance() {
        if (instance == null) {
            // Create a new instance if it doesn't exist yet.
            instance = new BluetoothHandler();
        }
        return instance;
    }


    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }
}
