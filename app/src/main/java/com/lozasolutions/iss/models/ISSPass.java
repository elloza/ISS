package com.lozasolutions.iss.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Loza on 19/12/2015.
 */
public class ISSPass implements Parcelable {

    Long duration;
    Long risetime;
    Float longitude;
    Float latitude;
    String address;

    public ISSPass(Long duration, Long risetime, Float longitude, Float latitude, String address) {
        this.duration = duration;
        this.risetime = risetime;
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
    }


    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getRisetime() {
        return risetime;
    }

    public void setRisetime(Long risetime) {
        this.risetime = risetime;
    }


    // Parcelling part
    public ISSPass(Parcel in){

        duration = in.readLong();
        risetime = in.readLong();
        longitude = in.readFloat();
        latitude = in.readFloat();
        address = in.readString();

    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeLong(duration);
        dest.writeLong(risetime);
        dest.writeFloat(longitude);
        dest.writeFloat(latitude);
        dest.writeString(address);

    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ISSPass createFromParcel(Parcel in) {
            return new ISSPass(in);
        }

        public ISSPass[] newArray(int size) {
            return new ISSPass[size];
        }
    };


}
