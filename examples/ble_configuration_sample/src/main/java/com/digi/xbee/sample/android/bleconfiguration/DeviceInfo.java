package com.digi.xbee.sample.android.bleconfiguration;

import com.firebase.geofire.GeoLocation;

public class DeviceInfo {
    private String hashedLocation;

    public String getAddress() {
        return address;
    }

    private String address;
    private String name;
    private VehicleType deviceType;
    private GeoLocation location;
    private String lastModified;
    private String sessionId;

    public DeviceInfo(String address, String name, VehicleType deviceType, GeoLocation location, String hashedLocation, String lastModified, String sessionId) {
        this.address = address;
        this.name = name;
        this.deviceType = deviceType;
        this.location = location;
        this.hashedLocation = hashedLocation;
        this.lastModified = lastModified;
        this.sessionId = sessionId;
    }

    enum VehicleType {
        CAR,
        TRUCK,
        GHOST_CAR
    }
}
