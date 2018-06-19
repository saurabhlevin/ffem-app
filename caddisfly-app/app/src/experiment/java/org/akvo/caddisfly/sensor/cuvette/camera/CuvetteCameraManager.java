package org.akvo.caddisfly.sensor.cuvette.camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.LocalBroadcastManager;

import org.akvo.caddisfly.common.ChamberTestConfig;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.ColorInfo;
import org.akvo.caddisfly.model.ResultDetail;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.CameraPreview;
import org.akvo.caddisfly.util.ColorUtil;
import org.akvo.caddisfly.util.ImageUtil;

public class CuvetteCameraManager {
    private final TestInfo testInfo;
    private LocalBroadcastManager localBroadcastManager;
    private Handler mCameraHandler;
    private Camera mCamera;

    private byte[] bytes;

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] imageData, Camera camera) {

            if (bytes != null && bytes.length > 0 && AppPreferences.isTestMode()) {
                // Use dialog_device_list image if we are in dialog_device_list mode

            } else {
                int width = camera.getParameters().getPreviewSize().width;
                int height = camera.getParameters().getPreviewSize().height;
                int[] finalPixels = ImageUtil.convertYUV420_NV21toRGB8888(imageData, width, height);

                Bitmap tempFinalImage = Bitmap.createBitmap(finalPixels, width, height, Bitmap.Config.ARGB_8888);
                Bitmap croppedBitmap = ImageUtil.getCroppedBitmap(tempFinalImage,
                        ChamberTestConfig.SAMPLE_CROP_LENGTH_DEFAULT);

                ColorInfo photoColor = ColorUtil.getColorFromBitmap(croppedBitmap,
                        ChamberTestConfig.SAMPLE_CROP_LENGTH_DEFAULT);

                ResultDetail resultDetail = SwatchHelper.analyzeColor(testInfo.getSwatches().size(),
                        photoColor, testInfo.getSwatches());

                Intent localIntent = new Intent("CUVETTE_RESULT_ACTION");
                localIntent.putExtra("cuvette_result", String.valueOf(resultDetail.getResult()) + "\n" +
                        ColorUtil.getColorRgbString(resultDetail.getColor()));

                localBroadcastManager.sendBroadcast(localIntent);
            }
        }
    };

    public CuvetteCameraManager(Context context, TestInfo testInfo) {
        this.testInfo = testInfo;
        if (AppPreferences.isTestMode()) {
            bytes = ImageUtil.loadImageBytes(testInfo.getName(), FileHelper.FileType.TEST_IMAGE);
        }

        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public CameraPreview initCamera(Context context) {
        startCameraThread();

        // open the camera and create a preview surface for it
        CameraPreview cameraPreview = new CameraPreview(context, Camera.Parameters.FLASH_MODE_TORCH);
        mCamera = cameraPreview.getCamera();

        return cameraPreview;
    }

    public void setDecodeImageCaptureRequest() {
        if (mCameraHandler != null && mCamera != null) {
            try {
                mCameraHandler.post(() -> {
                    if (mCamera != null) {
                        mCamera.setOneShotPreviewCallback(previewCallback);
                    }
                });
            } catch (Exception ignored) {
                // do nothing
            }
        }
    }

    public void stopCamera() {
        mCamera = null;
        mCameraHandler = null;
    }

    /**
     * Starts a background thread and its Handler.
     */
    private void startCameraThread() {
        HandlerThread mCameraThread = new HandlerThread("CameraBackground");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
    }
}
