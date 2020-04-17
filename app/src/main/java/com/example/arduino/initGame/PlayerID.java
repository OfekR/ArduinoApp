package com.example.arduino.initGame;

import android.os.Parcel;
import android.os.Parcelable;


/*
Not in use maybe need later for game
instead use playerId use simple number 1 and 2
 */
public class PlayerID implements Parcelable {
    private String joinPlayerId;
    private String createrPlayerId;

    public String getJoinPlayerId() {
        return joinPlayerId;
    }

    public void setJoinPlayerId(String joinPlayerId) {
        this.joinPlayerId = joinPlayerId;
    }

    public void setCreaterPlayerId(String createrPlayerId) {
        this.createrPlayerId = createrPlayerId;
    }

    public String getCreaterPlayerId() {
        return createrPlayerId;
    }

    public PlayerID() {
    }

    protected PlayerID(Parcel in) {
        joinPlayerId = in.readString();
        createrPlayerId = in.readString();
    }

    public static final Creator<PlayerID> CREATOR = new Creator<PlayerID>() {
        @Override
        public PlayerID createFromParcel(Parcel in) {
            return new PlayerID(in);
        }

        @Override
        public PlayerID[] newArray(int size) {
            return new PlayerID[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(joinPlayerId);
        dest.writeString(createrPlayerId);
    }
}
