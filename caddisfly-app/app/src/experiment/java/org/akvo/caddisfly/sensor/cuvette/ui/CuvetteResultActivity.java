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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.cuvette.bluetooth.Constants;
import org.akvo.caddisfly.util.BluetoothChatService;

import java.lang.ref.WeakReference;

import timber.log.Timber;

public class CuvetteResultActivity extends AppCompatActivity
        implements DeviceListDialog.OnDeviceSelectedListener {

    private static final int REQUEST_ENABLE_BT = 3;
    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    MyInnerHandler mHandler = new MyInnerHandler(this);
    private WeakReference<CuvetteResultActivity> mActivity;

    // Layout Views
    private ListView mConversationView;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mConversationArrayAdapter;
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

//        sound = new SoundPoolPlayer(this);

        setContentView(R.layout.activity_cuvette_result);

        mConversationView = findViewById(R.id.in);

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

    private void showDeviceListDialog() {
        DialogFragment resultFragment = DeviceListDialog.newInstance();
        final android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
        resultFragment.setCancelable(false);
        resultFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        resultFragment.show(ft, "deviceList");
    }

    private void releaseResources() {

        if (mActivity != null) {
            mActivity.clear();
            mActivity = null;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseResources();
        if (mChatService != null) {
            mChatService.stop();
        }
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
        mChatService.connect(device, secure);
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

        Timber.d("setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<>(this, R.layout.message);

        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }


    @Override
    public void onDeviceSelected(String address) {
        connectDevice(address, true);
    }

    static class MyInnerHandler extends Handler {
        WeakReference<Activity> activityWeakReference;

        MyInnerHandler(Activity aFragment) {
            activityWeakReference = new WeakReference<>(aFragment);
        }

        @Override
        public void handleMessage(Message message) {
            CuvetteResultActivity activity = (CuvetteResultActivity) activityWeakReference.get();
            switch (message.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (message.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            activity.setStatus(activity.getString(R.string.title_connected_to,
                                    activity.mConnectedDeviceName));
                            activity.mConversationArrayAdapter.clear();
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
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) message.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    activity.mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) message.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, message.arg1);
                    activity.mConversationArrayAdapter.add(activity.mConnectedDeviceName + ":  " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    activity.mConnectedDeviceName = message.getData().getString(Constants.DEVICE_NAME);
//                    Toast.makeText(this, "Connected to "
//                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
//                    if (null != this) {
//                        Toast.makeText(this, msg.getData().getString(Constants.TOAST),
//                                Toast.LENGTH_SHORT).show();
//                    }
                    break;
            }
        }
    }
}
