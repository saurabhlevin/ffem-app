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

package org.akvo.caddisfly.mock;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.CameraDialog;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.util.ApiUtil;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import timber.log.Timber;


/**
 * A simple {@link android.app.Fragment} subclass.
 * Use the {@link CameraDialogFragmentMock#newInstance} factory method to
 * create an instance of this fragment.
 */
@SuppressWarnings("deprecation")
public class CameraDialogFragmentMock extends CameraDialog {

    private static final int RADIUS = 40;

    private int mNumberOfPhotosToTake;
    private int mPhotoCurrentCount = 0;
    private long mSamplingDelay;
    private boolean mCancelled = false;
    private CameraMock mCamera;
    // View to display the camera output.
    private CameraPreview mCameraPreview;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CameraDialogFragment.
     */
    public static CameraDialogFragmentMock newInstance() {
        return new CameraDialogFragmentMock();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_camera, container, false);

        ButterKnife.bind(this, view);

        // Create preview and set it as the content
        if (!safeCameraOpenInView(view)) {
            return null;
        }

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Override
    public void takePictureSingle() {
        mNumberOfPhotosToTake = 1;
        mPhotoCurrentCount = 0;
        takePicture();
    }

    @Override
    public void takePictures(int count, long delay) {
        mNumberOfPhotosToTake = count;
        mPhotoCurrentCount = 0;
        mSamplingDelay = delay;

        final Handler handler = new Handler();
        handler.postDelayed(this::takePicture, delay);
    }

    private void takePicture() {
        PictureCallback localCallback = new PictureCallback();
        try {
            mCamera.takePicture(null, null, localCallback);
        } catch (Exception ignored) {

        }
    }

    @Override
    public void stopCamera() {
        mCancelled = true;
    }

    /**
     * Checks if requested number of pictures were taken
     *
     * @return true if completed, false if not
     */
    private boolean hasCompleted() {
        return mPhotoCurrentCount >= mNumberOfPhotosToTake;
    }

    private boolean safeCameraOpenInView(View view) {
        boolean opened;

        mCamera = ApiUtil.getCameraMockInstance();
        opened = (mCamera != null);

        if (opened) {
            mCameraPreview = new CameraPreview(getActivity().getBaseContext(), ApiUtil.getCameraInstance());
            FrameLayout layoutCameraPreview = view.findViewById(R.id.layoutCameraPreview);
            layoutCameraPreview.addView(mCameraPreview);

            mCameraPreview.startCameraPreview();
        }
        return opened;
    }

    @Override
    public void onPause() {
        super.onPause();
        mCancelled = true;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (getActivity() instanceof Cancelled) {
            ((Cancelled) getActivity()).dialogCancelled();
        }
        super.onCancel(dialog);
    }


    @SuppressLint("ViewConstructor")
    static class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

        private static final double ASPECT_TOLERANCE = 0.1;
        private final SurfaceHolder mHolder;
        private final Paint circleStroke;
        private final List<Camera.Size> mSupportedPreviewSizes;
        private Camera mCamera;
        @SuppressWarnings("unused")
        private List<String> mSupportedFlashModes;
        private Camera.Size mPreviewSize;

        CameraPreview(Context context, Camera camera) {
            super(context);
            //setCamera(camera);
            mCamera = camera;

            // supported preview sizes
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            //mHolder.setKeepScreenOn(true);
            // deprecated setting, but required on Android versions prior to 3.0
            //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            circleStroke = new Paint();
            circleStroke.setColor(Color.YELLOW);
            circleStroke.setStyle(Paint.Style.STROKE);
            circleStroke.setStrokeWidth(5);

            setWillNotDraw(false);
        }

        public void startCameraPreview() {
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch (Exception e) {
                Timber.e(e);
            }
        }


        public void surfaceCreated(SurfaceHolder holder) {
            // empty. surfaceChanged will take care of stuff
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCamera != null) {
                mCamera.stopPreview();
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.
            if (mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or reformatting changes here
            // start preview with new settings
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                mCamera.setParameters(parameters);
                mCamera.setDisplayOrientation(SensorConstants.DEGREES_90);
            } catch (Exception e) {
                Timber.e(e);
            }

            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                Timber.e(e);
            }
            mCamera.startPreview();
        }

        private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
            double targetRatio = (double) h / w;

            if (sizes == null) {
                return null;
            }

            Camera.Size optimalSize = null;
            double minDiff = Double.MAX_VALUE;

            for (Camera.Size size : sizes) {
                double ratio = (double) size.height / size.width;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                    continue;
                }

                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }

            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (Camera.Size size : sizes) {
                    if (Math.abs(size.height - h) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - h);
                    }
                }
            }

            return optimalSize;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int w = getWidth();
            int h = getHeight();

            canvas.drawCircle(w / 2f, h / 2f, RADIUS, circleStroke);

            super.onDraw(canvas);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

            if (mSupportedPreviewSizes != null) {
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            }

            float ratio;
            if (mPreviewSize.height >= mPreviewSize.width) {
                ratio = (float) mPreviewSize.height / (float) mPreviewSize.width;
            } else {
                ratio = (float) mPreviewSize.width / (float) mPreviewSize.height;
            }

            setMeasuredDimension(width, (int) (width * ratio));
        }
    }

    class PictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            mPhotoCurrentCount++;
            if (!mCancelled) {
                for (PictureTaken pictureTakenObserver : pictureTakenObservers) {
                    pictureTakenObserver.onPictureTaken(bytes, hasCompleted());
                }
                if (!hasCompleted()) {
                    try {
                        camera.startPreview();
                        final Handler handler = new Handler();
                        handler.postDelayed(CameraDialogFragmentMock.this::takePicture, mSamplingDelay);
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
            }
        }
    }
}
