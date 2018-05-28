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

package org.akvo.caddisfly.sensor.titration.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.model.Result;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.titration.models.DecodeData;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.ImageUtil;

import static org.akvo.caddisfly.sensor.titration.decode.DecodeProcessor.applyGrayScale;

/**
 * Activity that displays the results.
 */
public class ResultActivity extends BaseActivity {
    private static final int IMG_WIDTH = 500;
    private static DecodeData mDecodeData;
    private final SparseArray<String> resultStringValues = new SparseArray<>();
    private final SparseArray<String> brackets = new SparseArray<>();
    int measureStart = 0;
    int measureEnd = 0;
    float liquidLevel = -1;
    private Button buttonSave;
    private Button buttonCancel;
    private Bitmap totalImage;
    private Bitmap finalImage;
    //    private String totalImageUrl;
    private LinearLayout layout;

    private TestInfo testInfo;

    public static void setDecodeData(DecodeData decodeData) {
        mDecodeData = decodeData;
    }

    /**
     * Converts YUV420 NV21 to RGB8888
     *
     * @param data   byte array on YUV420 NV21 format.
     * @param width  pixels width
     * @param height pixels height
     * @return a RGB8888 pixels int array. Where each int is a pixels ARGB.
     */
    public static int[] convertYUV420_NV21toRGB8888(byte[] data, int width, int height) {
        int size = width * height;
        int offset = size;
        int[] pixels = new int[size];
        int u, v, y1, y2, y3, y4;

        // i percorre os Y and the final pixels
        // k percorre os pixles U e V
        for (int i = 0, k = 0; i < size; i += 2, k += 2) {
            y1 = data[i] & 0xff;
            y2 = data[i + 1] & 0xff;
            y3 = data[width + i] & 0xff;
            y4 = data[width + i + 1] & 0xff;

            u = data[offset + k] & 0xff;
            v = data[offset + k + 1] & 0xff;
            u = u - 128;
            v = v - 128;

            pixels[i] = convertYUVtoRGB(y1, u, v);
            pixels[i + 1] = convertYUVtoRGB(y2, u, v);
            pixels[width + i] = convertYUVtoRGB(y3, u, v);
            pixels[width + i + 1] = convertYUVtoRGB(y4, u, v);

            if (i != 0 && (i + 2) % width == 0)
                i += width;
        }

        return pixels;
    }

    private static int convertYUVtoRGB(int y, int u, int v) {
        int r, g, b;

        r = y + (int) (1.402f * v);
        g = y - (int) (0.344f * u + 0.714f * v);
        b = y + (int) (1.772f * u);
        r = r > 255 ? 255 : r < 0 ? 0 : r;
        g = g > 255 ? 255 : g < 0 ? 0 : g;
        b = b > 255 ? 255 : b < 0 ? 0 : b;
        return 0xff000000 | (b << 16) | (g << 8) | r;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strip_result);
        setTitle(R.string.result);

        testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);

        buttonSave = findViewById(R.id.button_save);
        buttonSave.setOnClickListener(v -> {

            Intent resultIntent = new Intent();

            resultIntent.putExtra(testInfo.getResults().get(0).getCode(),
                    testInfo.getResults().get(0).getResult());
            resultIntent.putExtra(testInfo.getResults().get(1).getCode(),
                    testInfo.getResults().get(1).getResult());
            resultIntent.putExtra(testInfo.getResults().get(2).getCode(),
                    ImageUtil.encodeImage(finalImage));

            setResult(Activity.RESULT_OK, resultIntent);

            finish();

//            String path;

//            if (totalImage != null) {
//
//                // store image on sd card
//                path = FileUtil.writeBitmapToExternalStorage(totalImage,
//                        FileHelper.FileType.RESULT_IMAGE, totalImageUrl);
//
//                intent.putExtra(ConstantKey.IMAGE, path);
//
//                if (path.length() == 0) {
//                    totalImageUrl = "";
//                }
//            } else {
//                totalImageUrl = "";
//            }

//            JSONObject resultJsonObj = TestConfigHelper.getJsonResult(testInfo,
//                    resultStringValues, brackets, -1, totalImageUrl);

        });

        buttonCancel = findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(v -> {
            Intent intent = new Intent(getIntent());
            setResult(RESULT_CANCELED, intent);
            finish();
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finalImage.recycle();
    }

    @Override
    public void onResume() {
        super.onResume();
        layout = findViewById(R.id.layout_results);
        layout.removeAllViews();

        TestInfo testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);

        computeResults(testInfo);
        showResults(testInfo);
    }

    private void computeResults(TestInfo testInfo) {

        byte[] iDataArray = mDecodeData.getDecodeImageByteArray();
        int rowStride = mDecodeData.getDecodeWidth();
        int frameSize = rowStride * mDecodeData.getDecodeHeight();

        int width = mDecodeData.getDecodeWidth();
        int height = mDecodeData.getDecodeHeight();

        int[] pixels = applyGrayScale(iDataArray, width, height);

        Bitmap tempImage = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);

        int[] finalPixels = convertYUV420_NV21toRGB8888(iDataArray, width, height);

        Bitmap tempFinalImage = Bitmap.createBitmap(finalPixels, width, height, Bitmap.Config.ARGB_8888);

//        Log.e("TITRATION", "Start ***************************************************");

        int top = (int) mDecodeData.getPatternInfo().getTopLeft().getY();
        int bottom = (int) mDecodeData.getPatternInfo().getBottomLeft().getY();
        int left = (int) mDecodeData.getPatternInfo().getBottomLeft().getX();
        int right = (int) mDecodeData.getPatternInfo().getTopRight().getX();

        totalImage = Bitmap.createBitmap(tempImage, left, top, right - left, bottom - top, null, false);

        finalImage = Bitmap.createBitmap(tempFinalImage, left, top, right - left, bottom - top, null, false);

        int center = totalImage.getHeight() / 2;
        int measureLine = (int) (totalImage.getHeight() * 0.7);

        int threshold = 110;
        int measureThreshold = 255;
        int measureCount = 0;

        int col = 30;
        while (col < totalImage.getWidth() - 30) {
            int measurePixel = totalImage.getPixel(col, measureLine);
            int measurePixelCompare = totalImage.getPixel(col - 15, measureLine);
            if (Math.abs(Color.red(measurePixel) - Color.red(measurePixelCompare)) > 50) {
                if (Color.red(measurePixel) < Color.red(measurePixelCompare)) {
                    if (Color.red(measurePixel) < measureThreshold) {
                        measureThreshold = Color.red(measurePixel);
                    }

                    for (int i = col; i < col + 15; i++) {
                        measurePixel = totalImage.getPixel(col, measureLine);
//                        Log.e("TITRATION", i + " : " + String.valueOf(Color.red(measurePixel)));
                    }
                    measureCount++;

                    if (measureCount == 1) {
                        measureStart = col;
                    } else if (measureCount == 16) {
                        measureEnd = col;
                    }


                    col += 15;

//                    Log.e("TITRATION", col + "MEASURE COUNT : " + measureCount);
                }
            }
            col++;
//            Log.e("TITRATION", col + " : " + String.valueOf(Color.red(measurePixel)));
        }


        int liquidLevelStart = 0;

        if (measureCount == 16) {

            measureThreshold = measureThreshold + 20;

//            Log.e("TITRATION", "Measure threshold: " + measureThreshold);

//            for (int x = 20; x < totalImage.getWidth() / 8; x++) {
//                int measurePixel = totalImage.getPixel(x, measureLine);
//                if (Color.red(measurePixel) < measureThreshold) {
//                    measureStart = x;
//                    break;
//                }
//            }
//
//            for (int x = totalImage.getWidth() - 20; x >= (totalImage.getWidth() - totalImage.getWidth() / 8); x--) {
//                int measurePixel = totalImage.getPixel(x, measureLine);
//                if (Color.red(measurePixel) < measureThreshold) {
//                    measureEnd = x;
//                    break;
//                }
//            }

//            int count = 0;
//            for (int x = measureStart; x < totalImage.getWidth(); x++) {
//                int pixel = totalImage.getPixel(x, center);
//
//                if (Color.red(pixel) < threshold) {
//                    if (count == 0) {
//                        liquidLevelStart = x;
//                    }
//                    count++;
//                } else {
//                    count = 0;
//                }
//
//                if (count > 80) {
//                    break;
//                }
//                Log.e("TITRATION", String.valueOf(Color.red(pixel)));
//            }

            int count = 0;
            for (int x = measureEnd; x >= measureStart; x--) {
                int pixel = totalImage.getPixel(x, center);

                if (Color.red(pixel) > threshold) {
                    if (count == 0) {
                        liquidLevelStart = x;
                    }
                    count++;
                } else {
                    count = 0;
                }

                if (count > 30) {
                    break;
                }
                Log.e("TITRATION", String.valueOf(Color.red(pixel)));
            }

            try {
                if (measureStart > 0) {
                    int totalMeasure = measureEnd - measureStart;

                    liquidLevel = measureEnd - liquidLevelStart;

                    liquidLevel = (150f * liquidLevel) / totalMeasure;
                }
            } catch (Exception e) {
                e.printStackTrace();
                liquidLevel = -1;
            }
        }

        testInfo.getResults().get(0).setResult(liquidLevel, 0, 0);
        testInfo.getResults().get(1).setResult(liquidLevel, 0, 0);

//        Log.e("TITRATION", "End ***************************************************");
//        Log.e("TITRATION", "");

        mDecodeData.addStripImage(pixels, 0);

        Bitmap canvasBitmap = Bitmap.createBitmap(finalImage.getWidth(), finalImage.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(canvasBitmap);
        canvas.drawBitmap(finalImage, 0, 0, null);

        Paint bluePaint = new Paint();
        bluePaint.setAntiAlias(true);
        bluePaint.setColor(Color.BLACK);

        if (measureStart > 0) {
            drawTriangle(canvas, bluePaint, measureStart, center - 90, 30);
        }
        if (measureEnd > 0) {
            drawTriangle(canvas, bluePaint, measureEnd, center - 90, 30);
        }
        if (liquidLevel > 0) {
            Paint greenPaint = new Paint();
            greenPaint.setAntiAlias(true);
            greenPaint.setColor(Color.rgb(0, 153, 0));

            drawTriangle(canvas, greenPaint, liquidLevelStart, center - 110, 60);
//            canvas.drawCircle(liquidLevelStart, center, 25, greenPaint);
        }

        finalImage = Bitmap.createScaledBitmap(canvasBitmap, (int) (finalImage.getWidth() * 0.8),
                (int) (finalImage.getHeight() * 0.8), true);
        tempFinalImage.recycle();
        totalImage.recycle();
        tempImage.recycle();
    }

    // displays results and creates image to send to database
    // if patchResultList is null, it means that the strip was not found.
    // In that case, we show a default error image.
    private void showResults(TestInfo testInfo) {

        // create and display view with results
        // here, the total result image is also created
        createView(testInfo);

        // show buttons
        buttonSave.setVisibility(View.VISIBLE);
        buttonCancel.setVisibility(View.VISIBLE);
    }

    private void createView(TestInfo testInfo) {

        for (Result result : testInfo.getResults()) {
            if (result.getName().equals("Titration Photo")) {
                inflateView(result.getName(), "", finalImage);
            } else {
                if (liquidLevel > 0) {
                    // create image to display on screen
                    inflateView(result.getName(), result.getResult() + " " + result.getUnit(), null);
                } else {
                    inflateView(result.getName(), "No Result", null);
                }
            }
        }

    }

    @SuppressLint("InflateParams")
    private void inflateView(String title, String valueString, Bitmap resultImage) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout itemResult;
        if (inflater != null) {
            itemResult = (LinearLayout) inflater.inflate(R.layout.item_result, null, false);
            TextView textTitle = itemResult.findViewById(R.id.text_title);
            textTitle.setText(title);

            if (resultImage != null) {
                resultImage = ImageUtil.rotateImage(resultImage, 90);
                ImageView imageResult = itemResult.findViewById(R.id.image_result);
                imageResult.setImageBitmap(resultImage);
            }

            TextView textResult = itemResult.findViewById(R.id.text_result);
            if (valueString.isEmpty()) {
                textResult.setVisibility(View.GONE);
            } else {
                textResult.setText(valueString);
            }

            layout.addView(itemResult);
        }
    }

    public void drawTriangle(Canvas canvas, Paint paint, int x, int y, int width) {
        int halfWidth = width / 2;

        Path path = new Path();
        path.moveTo(x, y + halfWidth); // Top
        path.lineTo(x - halfWidth, y - halfWidth); // Bottom left
        path.lineTo(x + halfWidth, y - halfWidth); // Bottom right
        path.lineTo(x, y + halfWidth); // Back to Top
        path.close();

        canvas.drawPath(path, paint);
    }
}