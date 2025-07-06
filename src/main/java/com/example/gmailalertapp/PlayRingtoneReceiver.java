package com.example.gmailalertapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

public class PlayRingtoneReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String uriStr = intent.getStringExtra("ringtone_uri");
        if (uriStr == null) {
            Log.e("PlayRingtoneReceiver", "No ringtone URI provided");
            return;
        }

        try {
            Uri uri = Uri.parse(uriStr);
            Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
            Log.d("PlayRingtoneReceiver", "Broadcast received. URI: " + uriStr);

            if (ringtone != null) {
                ringtone.play();
                Log.d("PlayRingtoneReceiver", "Playing ringtone");
            } else {
                Log.e("PlayRingtoneReceiver", "Ringtone is null");
            }
        } catch (Exception e) {
            Log.e("PlayRingtoneReceiver", "Failed to play ringtone", e);
        }
    }
}
