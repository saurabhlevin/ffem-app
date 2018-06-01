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

package org.akvo.caddisfly.sensor.turbidity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.GMailSender;
import org.akvo.caddisfly.util.ImageUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.io.File;

import timber.log.Timber;

/**
 * Activity that displays the results.
 */
public class TimeLapseResultActivity extends BaseActivity {
    private Button buttonSave;
    private LinearLayout layout;

    private TestInfo testInfo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strip_result);
        setTitle(R.string.result);

        testInfo = getIntent().getParcelableExtra(ConstantKey.TEST_INFO);

        buttonSave = findViewById(R.id.button_save);
        buttonSave.setOnClickListener(v -> {

            Intent resultIntent = new Intent();

            resultIntent.putExtra(testInfo.getResults().get(0).getCode(), "Contaminated");

            setResult(Activity.RESULT_OK, resultIntent);

            File imageFile = new File(PreferencesUtil.getString(this, "firstFile", ""));

            sendEmail(imageFile, "", "");

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

        testInfo.getResults().get(0).setResult(1, 0, 0);
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
    }

    private void createView(TestInfo testInfo) {

        for (Result result : testInfo.getResults()) {
            if (result.getName().equals("Photo")) {
                inflateView(result.getName(), "", null);
            } else {
                inflateView(result.getName(), "No Result", null);
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

    private void sendEmail(File imageFile, String from, String to) {
        new Thread(() -> {
            try {
                GMailSender sender = new GMailSender(from, to);
                sender.sendMail("Result", "<b>Contaminated</b><img src=\"cid:" +
                        imageFile.getName() + "\" />", imageFile, from, to);
            } catch (Exception e) {
                Timber.e(e);
            }
        }).start();
    }
}