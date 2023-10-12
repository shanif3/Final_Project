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

    public String getHashedLocation() {
        return hashedLocation;
    }

    public void setHashedLocation(String hashedLocation) {
        this.hashedLocation = hashedLocation;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VehicleType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(VehicleType deviceType) {
        this.deviceType = deviceType;
    }

    public GeoLocation getLocation() {
        return location;
    }

    public void setLocation(GeoLocation location) {
        this.location = location;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

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
