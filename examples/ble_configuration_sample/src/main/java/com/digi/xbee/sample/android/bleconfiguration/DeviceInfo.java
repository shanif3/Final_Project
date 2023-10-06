package com.digi.xbee.sample.android.bleconfiguration;

public class DeviceInfo {
    public String getAddress() {
        return address;
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

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    private String address;
    private String name;
    private String deviceType;
    private String location;
    private String lastModified;

    public DeviceInfo(String address, String name, String deviceType, String location, String lastModified) {
        this.address = address;
        this.name = name;
        this.deviceType = deviceType;
        this.location = location;
        this.lastModified = lastModified;
    }
}
