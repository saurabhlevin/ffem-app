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

package org.akvo.caddisfly.sensor.colorimetry.strip.camera;

import android.content.Context;
import android.os.AsyncTask;

import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.FileStorage;
import org.akvo.caddisfly.util.detector.FinderPatternInfo;
import org.akvo.caddisfly.util.detector.FinderPatternInfoToJson;

/**
 * Created by linda on 11/19/15
 */
class StoreDataTask extends AsyncTask<Void, Void, Boolean> {

    private final int imageCount;
    private final byte[] data;
    private final FinderPatternInfo info;
    private final CameraViewListener listener;
    private final Context context;

    StoreDataTask(Context listener,
                  int imageCount, byte[] data, FinderPatternInfo info) {

        this.listener = (CameraViewListener) listener;
        this.imageCount = imageCount;
        this.data = data == null ? null : data.clone();
        this.info = info;
        this.context = listener;

    }

    // The data here is still in the original YUV preview format.
    @Override
    protected Boolean doInBackground(Void... params) {
        FileStorage.writeByteArray(context, data, Constant.DATA + imageCount);
        String json = FinderPatternInfoToJson.toJson(info);
        FileStorage.writeToInternalStorage(context, Constant.INFO + imageCount, json);
        return true;
    }

    @Override
    protected void onPostExecute(Boolean written) {
        listener.dataSent();
    }
}
