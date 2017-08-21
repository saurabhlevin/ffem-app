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

package org.akvo.caddisfly.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.AppConfig;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.ApkHelper;
import org.akvo.caddisfly.helper.CameraHelper;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.preference.SettingsActivity;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.liquid.CalibrateListActivity;
import org.akvo.caddisfly.sensor.colorimetry.liquid.ColorimetryLiquidActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.ui.TestTypeListActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.io.File;
import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends BaseActivity {

//    private static final long DAYS_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int AUTO_FINISH_DELAY_MILLIS = 4000;
    private static final int PERMISSION_ALL = 1;
    private static final float SNACK_BAR_LINE_SPACING = 1.4f;
//    final GregorianCalendar appExpiryDate = new GregorianCalendar(AppConfig.APP_EXPIRY_YEAR,
//            AppConfig.APP_EXPIRY_MONTH - 1, AppConfig.APP_EXPIRY_DAY);
    private final WeakRefHandler refreshHandler = new WeakRefHandler(this);
    private final Handler finishOnSurveyOpenedHandler = new Handler();

    @BindView(R.id.coordinatorLayout)
    View coordinatorLayout;

    @BindView(R.id.layoutDiagnostics)
    View layoutDiagnostics;

    @BindView(R.id.textVersionExpiry)
    TextView textVersionExpiry;

    private Runnable finishRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        makeUpgrades();

        if (!ApkHelper.isStoreVersion(this)) {

//            DateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
//            textVersionExpiry.setText(String.format("Version expiry: %s", df.format(appExpiryDate.getTime())));

            textVersionExpiry.setVisibility(View.GONE);

            // If app has expired then close this activity
//            ApkHelper.isAppVersionExpired(this);
        }
    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        if (!ApkHelper.isStoreVersion(this)) {
//            if (intent.hasExtra("appExpiryNotification")) {
//
//                GregorianCalendar now = new GregorianCalendar();
//                long difference = appExpiryDate.getTimeInMillis() - now.getTimeInMillis();
//
//                int remainingDays = (int) (difference / DAYS_IN_MILLIS);
//
//                String message = String.format("%s%n%n%s%n%n%s",
//                        String.format(getResources().getQuantityString(R.plurals.appWillExpireInDays, remainingDays), remainingDays),
//                        getString(R.string.thisIsATestVersion),
//                        getString(R.string.uninstallAndInstallFromStore));
//
//                AlertDialog.Builder builder;
//                builder = new AlertDialog.Builder(this);
//
//                builder.setTitle(R.string.appName)
//                        .setMessage(message)
//                        .setCancelable(false);
//
//                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(@NonNull DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                });
//
//                final AlertDialog alertDialog = builder.create();
//                alertDialog.show();
//            }
//        }
//    }

    /**
     * Navigate to the survey
     */
    @OnClick(R.id.buttonSurvey)
    void navigateToSurvey() {
        Intent intent = getPackageManager()
                .getLaunchIntentForPackage(AppConfig.FLOW_SURVEY_PACKAGE_NAME);
        if (intent == null) {
            alertDependantAppNotFound();
        } else {

            finishRunnable = this::finish;

            finishOnSurveyOpenedHandler.postDelayed(finishRunnable, AUTO_FINISH_DELAY_MILLIS);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    @OnClick(R.id.buttonCalibrate)
    public void navigateToCalibrate() {

        //Hard coded to load chromium only
        CaddisflyApp.getApp().loadTestConfigurationByUuid(SensorConstants.CHROMIUM_ID);

        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (AppPreferences.useExternalCamera()) {
            permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }

        if (!ApiUtil.hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_ALL);
        } else {
            startCalibration();
        }

//        final Intent intent = new Intent(getBaseContext(), TypeListActivity.class);
//        startActivity(intent);
    }

    private void startCalibration() {
        //Only start the colorimetry calibration if the device has a camera flash
        if (AppPreferences.useExternalCamera()
                || CameraHelper.hasFeatureCameraFlash(this, R.string.cannotCalibrate, R.string.ok, null)) {

            final Intent intent;
            if (getIntent().getBooleanExtra("runTest", false)) {
                intent = new Intent(this, ColorimetryLiquidActivity.class);
            } else {
                intent = new Intent(this, CalibrateListActivity.class);
            }
            startActivity(intent);
        }
    }


    @OnClick(R.id.fabDisableDiagnostics)
    public void disableDiagnostics() {
        Toast.makeText(getBaseContext(), getString(R.string.diagnosticModeDisabled),
                Toast.LENGTH_SHORT).show();

        AppPreferences.disableDiagnosticMode();

        switchLayoutForDiagnosticOrUserMode();

        changeActionBarStyleBasedOnCurrentMode();
    }

    private void startStripTest() {
        final Intent intent = new Intent(getBaseContext(), TestTypeListActivity.class);
        intent.putExtra("internal", true);
        startActivity(intent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        setTitle(R.string.appName);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // The app is active again, stop the timer that is about to close the app
        finishOnSurveyOpenedHandler.removeCallbacks(finishRunnable);

        switchLayoutForDiagnosticOrUserMode();

        CaddisflyApp.getApp().setAppLanguage(null, false, refreshHandler);

        if (PreferencesUtil.getBoolean(this, R.string.themeChangedKey, false)) {
            PreferencesUtil.setBoolean(this, R.string.themeChangedKey, false);
            refreshHandler.sendEmptyMessage(0);
        }
    }

    /**
     * Show the diagnostic mode layout
     */
    private void switchLayoutForDiagnosticOrUserMode() {
        if (AppPreferences.isDiagnosticMode()) {
            layoutDiagnostics.setVisibility(View.VISIBLE);
        } else {
            layoutDiagnostics.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.actionSettings) {
            final Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, 100);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && PreferencesUtil.getBoolean(this, R.string.refreshKey, false)) {
            PreferencesUtil.setBoolean(this, R.string.refreshKey, false);
            this.recreate();
        }
    }

    /**
     * Alert shown when a feature is not supported by the device
     */
    private void alertFeatureNotSupported() {
        String message = String.format("%s%n%n%s", getString(R.string.phoneDoesNotSupport),
                getString(R.string.pleaseContactSupport));

        AlertUtil.showMessage(this, R.string.notSupported, message);
    }

    private void alertDependantAppNotFound() {
        String message = String.format("%s%n%n%s", getString(R.string.errorAkvoFlowRequired),
                getString(R.string.pleaseContactSupport));

        AlertUtil.showMessage(this, R.string.notFound, message);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        final Activity activity = this;
        if (requestCode == PERMISSION_ALL) {
            // If request is cancelled, the result arrays are empty.
            boolean granted = false;
            for (int grantResult : grantResults) {
                if (grantResult != PERMISSION_GRANTED) {
                    granted = false;
                    break;
                } else {
                    granted = true;
                }
            }
            if (granted) {
                startCalibration();
                //startStripTest();
            } else {
                String message = getString(R.string.storagePermission);
                if (AppPreferences.useExternalCamera()) {
                    message = getString(R.string.storagePermission);
                }
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
                        .setAction("SETTINGS", view -> ApiUtil.startInstalledAppDetailsActivity(activity));

                TypedValue typedValue = new TypedValue();
                getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);

                snackbar.setActionTextColor(typedValue.data);
                View snackView = snackbar.getView();
                TextView textView = snackView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setHeight(getResources().getDimensionPixelSize(R.dimen.snackBarHeight));
                textView.setLineSpacing(0, SNACK_BAR_LINE_SPACING);
                textView.setTextColor(Color.WHITE);
                snackbar.show();
            }
        }
    }

    // TODO: remove upgrade code when obsolete
    private void makeUpgrades() {
        upgradeFolder("FLUOR", SensorConstants.FLUORIDE_ID);
        upgradeFolder("CHLOR", SensorConstants.FREE_CHLORINE_ID);

        // change the old name of custom config folder from config to custom-config
        final File oldFolder = new File(FileUtil.getFilesStorageDir(CaddisflyApp.getApp(), false)
                + FileHelper.ROOT_DIRECTORY + File.separator + "config");
        final File newFolder = new File(FileUtil.getFilesStorageDir(CaddisflyApp.getApp(), false)
                + FileHelper.ROOT_DIRECTORY + File.separator + "custom-config");

        if (oldFolder.exists() && oldFolder.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            oldFolder.renameTo(newFolder);
        }
    }

    // TODO: remove upgrade code when obsolete
    private void upgradeFolder(String code, String uuid) {

        // change calibration folder names to have uuid instead of the old 5 letter codes
        final File sourcePath = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION, code);
        final File destinationPath = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION, uuid);
        if (sourcePath.exists() && sourcePath.isDirectory()) {
            File[] sourceFiles = sourcePath.listFiles();
            if (sourceFiles != null) {
                for (File file : sourceFiles) {
                    File destinationFile = new File(destinationPath + File.separator + file.getName());
                    //noinspection ResultOfMethodCallIgnored
                    file.renameTo(destinationFile);
                }

                sourceFiles = sourcePath.listFiles();
                if (sourceFiles != null && sourceFiles.length == 0) {
                    //noinspection ResultOfMethodCallIgnored
                    sourcePath.delete();
                }
            }
        }
    }

    /**
     * Handler to restart the app after language has been changed
     */
    private static class WeakRefHandler extends Handler {
        private final WeakReference<Activity> ref;

        WeakRefHandler(Activity ref) {
            this.ref = new WeakReference<>(ref);
        }

        @Override
        public void handleMessage(Message msg) {
            Activity f = ref.get();
            if (f != null) {
                f.recreate();
            }
        }
    }
}

