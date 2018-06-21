package org.akvo.caddisfly.sensor.cuvette.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.chamber.ChamberCameraPreview;
import org.akvo.caddisfly.sensor.cuvette.bluetooth.Constants;
import org.akvo.caddisfly.sensor.cuvette.camera.CuvetteCameraManager;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.BluetoothChatService;

import java.lang.ref.WeakReference;

import timber.log.Timber;

@SuppressWarnings("deprecation")
public class CuvetteMeasureActivity extends BaseActivity
        implements DeviceListDialog.OnDeviceSelectedListener,
        DeviceListDialog.OnDeviceCancelListener {

    private static final int REQUEST_ENABLE_BT = 3;
    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    MyInnerHandler mHandler = new MyInnerHandler(this);
    @Nullable
    private WeakReference<Camera> wrCamera;
    private Camera mCamera;
    private ChamberCameraPreview mCameraPreview;
    private FrameLayout previewLayout;
    private WeakReference<CuvetteMeasureActivity> mActivity;
    private CuvetteCameraManager cuvetteCameraManager;
    private Handler mCameraHandler;
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                cuvetteCameraManager.setDecodeImageCaptureRequest();
            } finally {
                mCameraHandler.postDelayed(mStatusChecker, 2000);
            }
        }
    };
    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;
    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;
    /**
     * `
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;
    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sendMessage(intent.getStringExtra("cuvette_result"));
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_run_cuvette_test);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver,
                new IntentFilter("CUVETTE_RESULT_ACTION")
        );
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        TestInfo testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);

        if (testInfo != null && testInfo.getUuid() != null) {
            setTitle(testInfo.getName());
        } else {
            finish();
        }

        if (cuvetteCameraManager == null) {
            cuvetteCameraManager = new CuvetteCameraManager(this, testInfo);
        }

        if (mChatService == null || mChatService.getState() == BluetoothChatService.STATE_NONE) {
            showDeviceListDialog();
        } else {

            // Performing this check in onResume() covers the case in which BT was
            // not enabled during onStart(), so we were paused to enable it...
            // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
            if (mChatService != null) {
                // Only if the state is STATE_NONE, do we know that we haven't started already
                if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                    // Start the Bluetooth chat services
                    mChatService.start();
                }
            }
        }
    }

    DialogFragment deviceDialog;
    private void showDeviceListDialog() {
        deviceDialog = DeviceListDialog.newInstance();
        final android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
        deviceDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        deviceDialog.show(ft, "deviceList");
    }

    private void startCameraPreview() {
        previewLayout = findViewById(R.id.camera_preview);
        mCameraPreview = cuvetteCameraManager.initCamera(this);

        mCamera = mCameraPreview.getCamera();

        if (mCamera == null) {
            Toast.makeText(this.getApplicationContext(), "Could not instantiate the camera",
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            try {
                wrCamera = new WeakReference<>(mCamera);
                previewLayout.removeAllViews();
                if (mCameraPreview != null) {
                    previewLayout.addView(mCameraPreview);
                } else {
                    finish();
                }

            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    /**
     * Turn flash on.
     */
    public void turnFlashOn() {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();

        String flashMode = Camera.Parameters.FLASH_MODE_TORCH;
        parameters.setFlashMode(flashMode);

        mCamera.setParameters(parameters);
    }

    private void releaseResources() {
        if (mCamera != null) {
            cuvetteCameraManager.stopCamera();
            mCamera.setOneShotPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        if (mActivity != null) {
            mActivity.clear();
            mActivity = null;
        }
        if (wrCamera != null) {
            wrCamera.clear();
            wrCamera = null;
        }

        if (mCameraPreview != null && previewLayout != null) {
//            previewLayout.removeView(mCameraPreview);
            mCameraPreview = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        if (mCameraHandler != null) {
            mCameraHandler.removeCallbacks(mStatusChecker);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();

        releaseResources();

        if (mChatService != null) {
            mChatService.stop();
        }
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

//    /**
//     * Makes this device discoverable for 300 seconds (5 minutes).
//     */
//    private void ensureDiscoverable() {
//        if (mBluetoothAdapter.getScanMode() !=
//                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
//            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//            startActivity(discoverableIntent);
//        }
//    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
//            Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
//            mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    protected void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * Establish connection with other device
     *
     * @param address An {@link Intent}
     * @param secure  Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(String address, boolean secure) {
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        try {
            mChatService.connect(device, secure);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void onBackPressed() {
//        if (deviceDialog.isVisible()) {
//            deviceDialog.dismiss();
//        }
//        super.onBackPressed();
//        finish();
//    }

    @Override
    public void onDeviceSelected(String address) {
        connectDevice(address, true);

        mCameraHandler = new Handler();

        (new Handler()).postDelayed(() -> {
            startCameraPreview();
            turnFlashOn();
            startRepeatingTask();
        }, 8000);
    }

    @Override
    public void onDeviceCancel() {
        finish();
    }

    static class MyInnerHandler extends Handler {
        WeakReference<Activity> activityWeakReference;

        MyInnerHandler(Activity aFragment) {
            activityWeakReference = new WeakReference<>(aFragment);
        }

        @Override
        public void handleMessage(Message message) {
            CuvetteMeasureActivity activity = (CuvetteMeasureActivity) activityWeakReference.get();
            switch (message.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (message.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            activity.setStatus(activity.getString(R.string.title_connected_to,
                                    activity.mConnectedDeviceName));
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            activity.setStatus(R.string.deviceConnecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            activity.setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    activity.mConnectedDeviceName = message.getData().getString(Constants.DEVICE_NAME);
//                    Toast.makeText(this, "Connected to "
//                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
