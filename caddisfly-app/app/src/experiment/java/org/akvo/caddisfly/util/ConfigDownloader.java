/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.util;


import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.akvo.caddisfly.BuildConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.AppConfig;
import org.akvo.caddisfly.diagnostic.ConfigTask;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.TestListActivity;
import org.akvo.caddisfly.viewmodel.TestListViewModel;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static org.akvo.caddisfly.helper.CameraHelper.getMaxSupportedMegaPixelsByCamera;

public class ConfigDownloader {

    /**
     * Download latest version of the experimental config file.
     *
     * @param activity          the activity
     * @param configSyncHandler the callback
     */
    public static void syncExperimentalConfig(Activity activity,
                                              TestListActivity.SyncCallbackInterface configSyncHandler) {
        if (NetUtil.isNetworkAvailable(activity)) {
            Date todayDate = Calendar.getInstance().getTime();
            ConfigTask configTask = new ConfigTask(activity, configSyncHandler);

            configTask.execute(AppConfig.EXPERIMENT_TESTS_URL + "?" + todayDate.getTime(),
                    FileHelper.FileType.EXP_CONFIG.toString());

            final TestListViewModel viewModel =
                    ViewModelProviders.of((FragmentActivity) activity).get(TestListViewModel.class);

            viewModel.clearTests();

        } else {
            Toast.makeText(activity,
                    "No data connection. Please connect to the internet and try again.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Download latest version of the experimental config file.
     *
     * @param activity the activity
     */
    public static void syncFfemExperimentalConfig(Activity activity) {

        if (NetUtil.isNetworkAvailable(activity)) {
            Date todayDate = Calendar.getInstance().getTime();
            ConfigTask configTask = new ConfigTask(activity, null);

            configTask.execute(AppConfig.EXPERIMENT_TESTS_FFEM_URL + "?" + todayDate.getTime(),
                    FileHelper.FileType.FFEM_EXP_CONFIG.toString());

        }
    }

    public static void sendDataToCloudDatabase(Context context, TestInfo testInfo) {

        ProgressDialog pd;

        if (!NetUtil.isNetworkAvailable(context)) {
            Toast.makeText(context,
                    "No data connection. Please connect to the internet and try again.", Toast.LENGTH_LONG).show();
            return;
        }

        String deviceId = PreferencesUtil.getString(context, R.string.deviceIdKey, "0");
        if (Integer.valueOf(deviceId) < 1) {
            AlertUtil.showMessage(context, R.string.error, "Please set Device ID in settings before sending.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        File root = Environment.getExternalStorageDirectory();

        pd = new ProgressDialog(context);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();

        new Thread(() -> {

            boolean isSending = false;
            for (Calibration calibration :
                    testInfo.getCalibrations()) {

                if (calibration.image != null && calibration.croppedImage != null) {

                    isSending = true;

                    File dir = new File(root.getAbsolutePath() + FileHelper.ROOT_DIRECTORY + "/result-images");
                    Uri file = Uri.fromFile(new File(dir, calibration.image));
                    StorageReference riversRef = storageReference.child("calibration-images/" + calibration.image);

                    riversRef.putFile(file)
                            .addOnSuccessListener(taskSnapshot -> {

                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                Uri file1 = Uri.fromFile(new File(dir, calibration.croppedImage));
                                StorageReference riversRef1 = storageReference.child("calibration-images/" + calibration.croppedImage);

                                riversRef1.putFile(file1)
                                        .addOnSuccessListener(taskSnapshot1 -> {

                                            // Get a URL to the uploaded content
                                            Uri downloadUrl1 = taskSnapshot1.getDownloadUrl();
                                            // Create a new calibration with a first and last name
                                            Map<String, Object> cal = new HashMap<>();
                                            cal.put("deviceId", deviceId);
                                            cal.put("id", calibration.uid);
                                            cal.put("value", calibration.value);
                                            cal.put("color", calibration.color);
                                            cal.put("r", Color.red(calibration.color));
                                            cal.put("g", Color.green(calibration.color));
                                            cal.put("b", Color.blue(calibration.color));
                                            cal.put("date", calibration.date);
                                            cal.put("version", BuildConfig.VERSION_CODE);
                                            cal.put("manufacturer", Build.MANUFACTURER);
                                            cal.put("device", Build.MODEL);
                                            cal.put("product", Build.PRODUCT);
                                            cal.put("os", "Android - " + Build.VERSION.RELEASE + " ("
                                                    + Build.VERSION.SDK_INT + ")");
                                            cal.put("megaPixel", getMaxSupportedMegaPixelsByCamera(context));
                                            cal.put("appName", context.getString(R.string.appName));
                                            cal.put("appVersion", CaddisflyApp.getAppVersion());

                                            if (downloadUrl != null) {
                                                cal.put("image", downloadUrl.toString());
                                            }
                                            if (downloadUrl1 != null) {
                                                cal.put("croppedImage", downloadUrl1.toString());
                                            }

                                            db.collection(calibration.uid)
                                                    .document(String.valueOf(deviceId + "-" + calibration.date))
                                                    .set(cal)
                                                    .addOnSuccessListener(documentReference -> {
                                                        if (pd.isShowing()) {
                                                            pd.dismiss();
                                                        }

                                                        Toast.makeText(context, "Data sent", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        if (pd.isShowing()) {
                                                            pd.dismiss();
                                                        }

                                                        Toast.makeText(context,
                                                                "Unable to send. Check connection", Toast.LENGTH_SHORT).show();

                                                        Timber.w("Error adding document", e);
                                                    });


                                        })
                                        .addOnFailureListener(exception -> {
                                            if (pd.isShowing()) {
                                                pd.dismiss();
                                            }
                                            Toast.makeText(context,
                                                    "Unable to send. Check connection", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(exception -> {
                                if (pd.isShowing()) {
                                    pd.dismiss();
                                }

                                Toast.makeText(context,
                                        "Unable to send. Check connection", Toast.LENGTH_SHORT).show();
                            });
                }

            }

            if (!isSending) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
            }
        }).start();
    }
}
