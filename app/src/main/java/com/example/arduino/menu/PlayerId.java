package com.example.arduino.menu;

import android.os.Parcel;
import android.os.Parcelable;

public class PlayerId implements Parcelable {
    private String playerid;

    public PlayerId(String playerid) {
        this.playerid = playerid;
    }

    private PlayerId(Parcel in) {
        playerid = in.readString();
    }

    public static final Creator<PlayerId> CREATOR = new Creator<PlayerId>() {
        @Override
        public PlayerId createFromParcel(Parcel in) {
            return new PlayerId(in);
        }

        @Override
        public PlayerId[] newArray(int size) {
            return new PlayerId[size];
        }
    };

    public String getPlayerid() {
        return playerid;
    }

    public void setPlayerid(String playerid) {
        this.playerid = playerid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(playerid);
    }
}
