package com.example.arduino.gameScreen;

import android.os.Parcel;
import android.os.Parcelable;

public class DocumentMover implements Parcelable {
    private String id;
    private Long _bestTime;
    private Long _gamesLost;
    private Long _gamesPlayed;
    private Long _gamesWon;
    private Long _hitsPercentage;
    private Long _mostBombHits ;
    private Long _mostLaserHits;
    private Long _totalBombHits;
    private Long _totalHits;
    private Long _totalPoints;
    private Long _totalShots;


    public DocumentMover(String id, Long _bestTime, Long _gamesLost, Long _gamesPlayed, Long _gamesWon, Long _hitsPercentage, Long _mostBombHits, Long _mostLaserHits, Long _totalBombHits, Long _totalHits, Long _totalPoints, Long _totalShots) {
        this.id = id;
        this._bestTime = _bestTime;
        this._gamesLost = _gamesLost;
        this._gamesPlayed = _gamesPlayed;
        this._gamesWon = _gamesWon;
        this._hitsPercentage = _hitsPercentage;
        this._mostBombHits = _mostBombHits;
        this._mostLaserHits = _mostLaserHits;
        this._totalBombHits = _totalBombHits;
        this._totalHits = _totalHits;
        this._totalPoints = _totalPoints;
        this._totalShots = _totalShots;
    }

    public String getId() {
        return id;
    }

    public Long get_bestTime() {
        return _bestTime;
    }

    public Long get_gamesLost() {
        return _gamesLost;
    }

    public Long get_gamesPlayed() {
        return _gamesPlayed;
    }

    public Long get_gamesWon() {
        return _gamesWon;
    }

    public Long get_hitsPercentage() {
        return _hitsPercentage;
    }

    public Long get_mostBombHits() {
        return _mostBombHits;
    }

    public Long get_mostLaserHits() {
        return _mostLaserHits;
    }

    public Long get_totalBombHits() {
        return _totalBombHits;
    }

    public Long get_totalHits() {
        return _totalHits;
    }

    public Long get_totalPoints() {
        return _totalPoints;
    }

    public Long get_totalShots() {
        return _totalShots;
    }

    public static Creator<DocumentMover> getCREATOR() {
        return CREATOR;
    }

    protected DocumentMover(Parcel in) {
        id = in.readString();
        if (in.readByte() == 0) {
            _bestTime = null;
        } else {
            _bestTime = in.readLong();
        }
        if (in.readByte() == 0) {
            _gamesLost = null;
        } else {
            _gamesLost = in.readLong();
        }
        if (in.readByte() == 0) {
            _gamesPlayed = null;
        } else {
            _gamesPlayed = in.readLong();
        }
        if (in.readByte() == 0) {
            _gamesWon = null;
        } else {
            _gamesWon = in.readLong();
        }
        if (in.readByte() == 0) {
            _hitsPercentage = null;
        } else {
            _hitsPercentage = in.readLong();
        }
        if (in.readByte() == 0) {
            _mostBombHits = null;
        } else {
            _mostBombHits = in.readLong();
        }
        if (in.readByte() == 0) {
            _mostLaserHits = null;
        } else {
            _mostLaserHits = in.readLong();
        }
        if (in.readByte() == 0) {
            _totalBombHits = null;
        } else {
            _totalBombHits = in.readLong();
        }
        if (in.readByte() == 0) {
            _totalHits = null;
        } else {
            _totalHits = in.readLong();
        }
        if (in.readByte() == 0) {
            _totalPoints = null;
        } else {
            _totalPoints = in.readLong();
        }
        if (in.readByte() == 0) {
            _totalShots = null;
        } else {
            _totalShots = in.readLong();
        }
    }

    public static final Creator<DocumentMover> CREATOR = new Creator<DocumentMover>() {
        @Override
        public DocumentMover createFromParcel(Parcel in) {
            return new DocumentMover(in);
        }

        @Override
        public DocumentMover[] newArray(int size) {
            return new DocumentMover[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        if (_bestTime == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(_bestTime);
        }
        if (_gamesLost == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(_gamesLost);
        }
        if (_gamesPlayed == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(_gamesPlayed);
        }
        if (_gamesWon == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(_gamesWon);
        }
        if (_hitsPercentage == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(_hitsPercentage);
        }
        if (_mostBombHits == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(_mostBombHits);
        }
        if (_mostLaserHits == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(_mostLaserHits);
        }
        if (_totalBombHits == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(_totalBombHits);
        }
        if (_totalHits == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(_totalHits);
        }
        if (_totalPoints == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(_totalPoints);
        }
        if (_totalShots == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(_totalShots);
        }
    }
}
