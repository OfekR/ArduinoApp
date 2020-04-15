package com.example.arduino.utilities;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import com.example.arduino.R;

public class MediaPlayerWrapper {
    private MediaPlayer mySong;
    private int songPosition;
    private int songSrc;
    private Context context;

    public MediaPlayerWrapper(int src,Context givenContext) {
        songSrc = src;
        context = givenContext;
    }


    public void StartOrResume() {
        if(mySong == null) {
            mySong = MediaPlayer.create(context ,songSrc);
            mySong.start();
        }
        else {
            mySong.seekTo(songPosition);
            mySong.start();
        }
    }

    public void Pause() {
        if(mySong != null) {
            mySong.pause();
            songPosition = mySong.getCurrentPosition();
        }
    }

    public void Destroy() {
        if (mySong != null) {
            mySong.release();
            mySong = null;
        }
    }
}
