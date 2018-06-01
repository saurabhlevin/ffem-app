package org.akvo.caddisfly.sensor.turbidity;

import android.Manifest;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.PermissionsDelegate;
import org.akvo.caddisfly.helper.SoundPoolPlayer;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.titration.ui.ResultActivity;
import org.akvo.caddisfly.sensor.titration.ui.TitrationTestHandler;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

public class TimeLapseActivity extends BaseActivity {

    private static final int INITIAL_DELAY = 25000;

    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    TextView textInterval;
    int interval = 0;
    int numberOfSamples;
    private SoundPoolPlayer sound;
    private View layoutWait;
    private LinearLayout layoutDetails;
    private TextView textCountdown;
    private Calendar futureDate;
    private Runnable runnable;
    private Handler handler;
    private String uuid;
    private TestInfo testInfo;
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            sound.playShortResource(R.raw.beep);

            uuid = "df3d1009-2112-4d95-a6f9-fdc4b5633ec9";

            int delayMinute;

            File folder = FileHelper.getFilesDir(FileHelper.FileType.TEMP_IMAGE,
                    intent.getStringExtra("savePath"));

            delayMinute = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                    "colif_IntervalMinutes", "1"));

            numberOfSamples = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                    "colif_NumberOfSamples", "1"));

            File[] files = folder.listFiles();
            if (files != null) {
                if (files.length >= numberOfSamples) {
                    TurbidityConfig.stopRepeatingAlarm(context, uuid);
                    showResult();
                    finish();
                } else {
                    textInterval.setText(String.format(Locale.getDefault(),
                            "Done: %d of %d", files.length, numberOfSamples));
                    futureDate = Calendar.getInstance();
                    futureDate.add(Calendar.MINUTE, delayMinute);
                }
            }
        }
    };
    private boolean showTimer = true;

    private void startCountdownTimer() {
        showTimer = true;
        handler = new Handler();
        handler.removeCallbacks(runnable);
        runnable = new Runnable() {
            @Override
            public void run() {
                if (showTimer) {
                    handler.postDelayed(this, 1000);
                    try {
                        Calendar currentDate = Calendar.getInstance();
                        if (futureDate != null && !currentDate.after(futureDate)) {
                            long diff = futureDate.getTimeInMillis() - currentDate.getTimeInMillis();
                            long days = diff / (24 * 60 * 60 * 1000);
                            diff -= days * (24 * 60 * 60 * 1000);
                            long hours = diff / (60 * 60 * 1000);
                            diff -= hours * (60 * 60 * 1000);
                            long minutes = diff / (60 * 1000);
                            diff -= minutes * (60 * 1000);
                            long seconds = diff / 1000;
                            textCountdown.setText(String.format(Locale.getDefault(),
                                    "%02d:%02d:%02d", hours, minutes, seconds));
                        }
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_lapse);

        sound = new SoundPoolPlayer(this);

        uuid = "df3d1009-2112-4d95-a6f9-fdc4b5633ec9";

        layoutWait = findViewById(R.id.layoutWait);
        layoutDetails = findViewById(R.id.layoutDetails);
        textInterval = findViewById(R.id.textInterval);

        Fragment fragment;
        Bundle bundle = new Bundle();

        final TextView textTitle = findViewById(R.id.textTitle);

        testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);

        fragment = new TimeLapsePreferenceFragment();

        bundle.putString("uuid", uuid);
        fragment.setArguments(bundle);

        textTitle.setText(testInfo.getName());
        setTitle(testInfo.getName());

        getFragmentManager().beginTransaction()
                .add(R.id.layoutContent4, fragment)
                .commit();

        LinearLayout layoutTitleBar = findViewById(R.id.layoutTitleBar);
        layoutTitleBar.setVisibility(View.GONE);

        layoutWait.setVisibility(View.VISIBLE);
        layoutDetails.setVisibility(View.GONE);

//        if (TurbidityConfig.isAlarmRunning(this, uuid)) {
//            Log.e("TimeLapse", "Already Running Alarm");
//        }

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-event-name"));

        Button buttonStart = findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(v -> {

            numberOfSamples = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                    "colif_NumberOfSamples", "1"));

            if (AppPreferences.useExternalCamera()) {
                permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            }

            if (permissionsDelegate.hasPermissions(permissions)) {
                startTest();
            } else {
                permissionsDelegate.requestPermissions(permissions);
            }
        });
        textCountdown = findViewById(R.id.textCountdown);
    }

    private void startTest() {

        layoutWait.setVisibility(View.GONE);
        layoutDetails.setVisibility(View.VISIBLE);

        Calendar startDate = Calendar.getInstance();

        String details = "";
        if (testInfo.getUuid().equals("colif")) {

            String media = PreferencesUtil.getString(this, getString(R.string.colif_brothMediaKey), "");
            String volume = PreferencesUtil.getString(this, getString(R.string.colif_volumeKey), "");
            String description = PreferencesUtil.getString(this, getString(R.string.colif_testDescriptionKey), "");

            details = "_" + Build.MODEL.replace("_", "-") + "-"
                    + media + "_"
                    + volume + "ml_"
                    + description;

            if (media.isEmpty() || volume.isEmpty() || description.isEmpty()) {
                AlertUtil.showAlert(this, R.string.required, "All fields must be filled", R.string.ok,
                        (dialogInterface, i) -> finish(), null, null);
                return;
            }
        }

        File folder = FileHelper.getFilesDir(FileHelper.FileType.TEMP_IMAGE, testInfo.getName());
        if (folder.exists()) {
//            FileUtil.deleteRecursive(folder);
        }

        PreferencesUtil.setString(this, R.string.turbiditySavePathKey,
                testInfo.getName() + File.separator + "_"
                        + new SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(startDate.getTime()) + details);

        TurbidityConfig.setRepeatingAlarm(this, INITIAL_DELAY, testInfo.getUuid());

        String date = new SimpleDateFormat("dd MMM yyy HH:mm", Locale.US).format(startDate.getTime());
        ((TextView) findViewById(R.id.textSubtitle))
                .setText(String.format(Locale.getDefault(), "%s %s", "Started", date));

        futureDate = Calendar.getInstance();
        futureDate.add(Calendar.MILLISECOND, INITIAL_DELAY);

        interval = Integer.parseInt(PreferencesUtil.getString(CaddisflyApp.getApp(),
                "colif_IntervalMinutes", "1"));

        textInterval.setText(String.format(Locale.getDefault(), "Done: %d of %d", 0, numberOfSamples));

        startCountdownTimer();
    }

    public void showResult() {
        Intent resultIntent = new Intent(getIntent());
        resultIntent.setClass(this, TimeLapseResultActivity.class);
        resultIntent.putExtra(ConstantKey.TEST_INFO, testInfo);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(resultIntent);
        ResultActivity.setDecodeData(TitrationTestHandler.getDecodeData());
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        showTimer = false;
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCountdownTimer();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showTimer = false;
        handler.removeCallbacks(runnable);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        TurbidityConfig.stopRepeatingAlarm(this, uuid);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {

        if (layoutDetails.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, "Test cancelled", Toast.LENGTH_LONG).show();
        }

        showTimer = false;
        handler.removeCallbacks(runnable);

        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        TurbidityConfig.stopRepeatingAlarm(this, uuid);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (permissionsDelegate.resultGranted(requestCode, grantResults)) {
            startTest();
        } else {
            String message = getString(R.string.cameraAndStoragePermissions);

            AlertUtil.showSettingsSnackbar(this,
                    getWindow().getDecorView().getRootView(), message);
        }
    }
}
