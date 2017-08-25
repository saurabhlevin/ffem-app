package org.akvo.caddisfly.mock;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;

import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.util.ApiUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class CameraMock extends Camera {

    // convert bitmap to byte array
    private static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    public static CameraMock open() {
        return new CameraMock();
    }

    public void takePicture(Object o, Object o1, CameraDialogFragmentMock.PictureCallback localCallback) {

        File file = new File(FileHelper.getFilesDir(FileHelper.FileType.IMAGE), SensorConstants.TEST_IMAGE_FILE);

        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());

        localCallback.onPictureTaken(getBytes(bitmap), ApiUtil.getCameraInstance());
    }

    public void stopPreview() {

    }

    public void release() {

    }
}
