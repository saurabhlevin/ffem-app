/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.sensor.turbidity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.akvo.caddisfly.app.CaddisflyApp;

public class TurbidityStartReceiver extends BroadcastReceiver {

    public TurbidityStartReceiver() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent != null && TurbidityConfig.ACTION_ALARM_RECEIVER.equals(intent.getAction())) {

            String uuid = intent.getStringExtra("uuid");
            CaddisflyApp.getApp().loadTestConfigurationByUuid(uuid);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                TurbidityConfig.setRepeatingAlarm(context, -1, uuid);
            }

            String folderName = intent.getStringExtra("savePath");

            CameraHandler cameraHandler = new CameraHandler(context);
            cameraHandler.takePicture(folderName);
        }
    }
}
