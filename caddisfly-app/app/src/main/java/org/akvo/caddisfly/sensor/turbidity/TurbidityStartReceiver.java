package org.akvo.caddisfly.sensor.turbidity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.akvo.caddisfly.common.ConstantKey;

public class TurbidityStartReceiver extends BroadcastReceiver {

    public TurbidityStartReceiver() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent != null && TurbidityConfig.ACTION_ALARM_RECEIVER.equals(intent.getAction())) {

            String uuid = intent.getStringExtra("uuid");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                TurbidityConfig.setRepeatingAlarm(context, -1, uuid);
            }

            String folderName = intent.getStringExtra(ConstantKey.SAVE_FOLDER);

            CameraHandler cameraHandler = new CameraHandler(context);
            cameraHandler.takePicture(folderName);
        }
    }
}
