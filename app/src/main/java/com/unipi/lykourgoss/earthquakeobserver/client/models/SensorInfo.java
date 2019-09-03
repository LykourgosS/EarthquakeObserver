package com.unipi.lykourgoss.earthquakeobserver.client.models;

import android.hardware.Sensor;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 27,August,2019.
 */

public class SensorInfo implements Parcelable {

    // will be acquired after running ConfigDeviceActivity
    private float balanceSensorValue;

    // Sensor class info
    private String name;
    private String vendor;
    private int version;
    private int type;
    private float maxRange;
    private float power;
    private int minDelay;

    public SensorInfo() {
    }

    public SensorInfo(Sensor accelerometer, float balanceSensorValue) {
        this.balanceSensorValue = balanceSensorValue;
        this.name = accelerometer.getName();
        this.vendor = accelerometer.getVendor();
        this.version = accelerometer.getVersion();
        this.type = accelerometer.getType();
        this.maxRange = accelerometer.getMaximumRange();
        this.power = accelerometer.getPower();
        this.minDelay = accelerometer.getMinDelay();
    }

    public float getBalanceSensorValue() {
        return balanceSensorValue;
    }

    public String getName() {
        return name;
    }

    public String getVendor() {
        return vendor;
    }

    public int getVersion() {
        return version;
    }

    public int getType() {
        return type;
    }

    public float getMaxRange() {
        return maxRange;
    }

    public float getPower() {
        return power;
    }

    public int getMinDelay() {
        return minDelay;
    }

    // todo remove Parcelable functionality
    protected SensorInfo(Parcel in) {
        balanceSensorValue = in.readFloat();
        name = in.readString();
        vendor = in.readString();
        version = in.readInt();
        type = in.readInt();
        maxRange = in.readFloat();
        power = in.readFloat();
        minDelay = in.readInt();
    }

    public static final Creator<SensorInfo> CREATOR = new Creator<SensorInfo>() {
        @Override
        public SensorInfo createFromParcel(Parcel in) {
            return new SensorInfo(in);
        }

        @Override
        public SensorInfo[] newArray(int size) {
            return new SensorInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(balanceSensorValue);
        dest.writeString(name);
        dest.writeString(vendor);
        dest.writeInt(version);
        dest.writeInt(type);
        dest.writeFloat(maxRange);
        dest.writeFloat(power);
        dest.writeInt(minDelay);
    }
}
