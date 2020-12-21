package com.example.soc_macmini_15.musicplayer.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.example.soc_macmini_15.musicplayer.Activity.MainActivity;
import com.example.soc_macmini_15.musicplayer.General.Constant;

import java.util.Timer;
import java.util.TimerTask;

public class AppReceiver extends BroadcastReceiver {
    private MainActivity mainActivity = null;

    public AppReceiver() {
        super();
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mainActivity != null) {
            MediaPlayer mediaPlayer = mainActivity.getMediaPlayer();
            if (intent.getAction().equals(Constant.ACL_CONNECTED)) {
                if (!mediaPlayer.isPlaying()) mainActivity.playMusic();
            } else if (intent.getAction().equals(Constant.ACL_DISCONNECTED)) {
                if (mediaPlayer.isPlaying()) mainActivity.pauseMusic();
            } else if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        if (mediaPlayer.isPlaying()) mainActivity.pauseMusic();
                        break;
                    case 1:
                        if (!mediaPlayer.isPlaying()) mainActivity.playMusic();
                        break;
                    default:
                        Log.d("a", "I have no idea what the headset state is");
                }
            }
        }
    }
}