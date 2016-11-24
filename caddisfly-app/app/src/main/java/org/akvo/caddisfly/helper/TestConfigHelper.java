/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.helper;

import android.os.Build;
import android.support.annotation.StringRes;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.sensor.SensorConstants;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.AssetsManager;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.PreferencesUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

/**
 * Utility functions to parse a text config json text
 */
public final class TestConfigHelper {

    // Files
    private static final String CONFIG_FILE = "tests.json";
    private static final int DEFAULT_MONTHS_VALID = 6;
    private static final int BIT_MASK = 0x00FFFFFF;

    private TestConfigHelper() {
    }

    /**
     * Returns a TestInfo instance filled with test config for the given uuid
     *
     * @param uuid the test uuid
     * @return the TestInfo instance
     */
    public static TestInfo loadTestByUuid(String uuid) {

        if (uuid != null && !uuid.isEmpty()) {

            for (int i = 0; i < 3; i++) {

                String jsonText;
                switch (i) {
                    case 1:
                        // Load any custom tests from the custom test config file
                        File file = new File(FileHelper.getFilesDir(FileHelper.FileType.CONFIG), CONFIG_FILE);
                        jsonText = FileUtil.loadTextFromFile(file);
                        break;
                    case 2:
                        // Load any experimental tests if app is in diagnostic mode
                        jsonText = AssetsManager.getInstance().loadJSONFromAsset("experimental_tests_config.json");
                        break;
                    default:
                        // Load the pre-configured tests from the app
                        jsonText = AssetsManager.getInstance().loadJSONFromAsset("tests_config.json");
                }
                try {
                    JSONArray array = new JSONObject(jsonText).getJSONArray("tests");
                    for (int j = 0; j < array.length(); j++) {
                        JSONObject item = array.getJSONObject(j);

                        JSONArray uuidArray = item.getJSONArray("uuid");
                        for (int k = 0; k < uuidArray.length(); k++) {
                            if (uuid.equalsIgnoreCase(uuidArray.getString(k))) {
                                return loadTest(item);
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
        return null;
    }

    /**
     * Load all the tests and their configurations from the json config text
     *
     * @return ArrayList of TestInfo instances filled with config
     */
    public static ArrayList<TestInfo> loadTestsList() {

        ArrayList<TestInfo> tests = new ArrayList<>();

        // Load the pre-configured tests from the app
        loadTests(tests, AssetsManager.getInstance().loadJSONFromAsset("tests_config.json"), false, -1);

        // Load any custom tests from the custom test config file
        File file = new File(FileHelper.getFilesDir(FileHelper.FileType.CONFIG), CONFIG_FILE);
        if (file.exists()) {
            loadTests(tests, FileUtil.loadTextFromFile(file), false, R.string.customTests);
        }

        // Load any experimental tests if app is in diagnostic mode
        if (AppPreferences.isDiagnosticMode()) {
            loadTests(tests, AssetsManager.getInstance().loadJSONFromAsset("experimental_tests_config.json"),
                    true, R.string.experimental);
        }

        return tests;
    }

    private static void loadTests(ArrayList<TestInfo> tests, String jsonText,
                                  boolean isDiagnostic, @StringRes int groupName) {

        int groupIndex = tests.size();

        JSONArray array;
        try {
            array = new JSONObject(jsonText).getJSONArray("tests");
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject item = array.getJSONObject(i);

                    // get uuids
                    JSONArray uuidArray = item.getJSONArray("uuid");
                    ArrayList<String> uuids = new ArrayList<>();
                    for (int ii = 0; ii < uuidArray.length(); ii++) {

                        String newUuid = uuidArray.getString(ii);
                        boolean found = false;
                        for (TestInfo test : tests) {
                            for (String uuid : test.getUuid()) {
                                if (uuid.equalsIgnoreCase(newUuid)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (found) {
                                break;
                            }
                        }
                        if (!found) {
                            uuids.add(newUuid);
                        }
                    }

                    if (uuids.isEmpty()) {
                        continue;
                    }

                    TestInfo testInfo = loadTest(item);
                    if (testInfo != null) {
                        testInfo.setIsDiagnostic(isDiagnostic);
                    }

                    //Create TestInfo object
                    tests.add(testInfo);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // If custom tests were added then add a dummy test object for group name
            if (tests.size() > groupIndex && groupName != -1) {
                TestInfo testGroup = new TestInfo();
                testGroup.setGroup(true);
                testGroup.setRequiresCalibration(true);
                testGroup.setGroupName(groupName);
                if (isDiagnostic) {
                    testGroup.setIsDiagnostic(true);
                }
                tests.add(groupIndex, testGroup);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private static TestInfo loadTest(JSONObject item) {

        TestInfo testInfo = null;
        try {
            //Get the test type
            TestType type;
            if (item.has("subtype")) {
                switch (item.getString("subtype")) {
                    case "color":
                    case "colour":
                        type = TestType.COLORIMETRIC_LIQUID;
                        break;
                    case "strip":
                    case "striptest":
                        type = TestType.COLORIMETRIC_STRIP;
                        break;
                    case "sensor":
                        type = TestType.SENSOR;
                        break;
                    case "coliform":
                    case "coliforms":
                        type = TestType.TURBIDITY_COLIFORMS;
                        break;
                    default:
                        return null;
                }
            } else {
                return null;
            }

            //Get the name for this test
            JSONArray nameArray = item.getJSONArray("name");

            HashMap<String, String> namesHashTable =
                    new HashMap<>(nameArray.length(), nameArray.length());

            //Load test names in different languages
            for (int j = 0; j < nameArray.length(); j++) {
                if (!nameArray.isNull(j)) {
                    Iterator iterator = nameArray.getJSONObject(j).keys();
                    String key = (String) iterator.next();
                    String name = nameArray.getJSONObject(j).getString(key);
                    namesHashTable.put(key, name);
                }
            }

            //Load results
            JSONArray resultsArray = null;
            if (item.has("results")) {
                resultsArray = item.getJSONArray("results");
            }

            //Load the dilution percentages
            String dilutions = "0";
            if (item.has("dilutions")) {
                dilutions = item.getString("dilutions");
                if (dilutions.isEmpty()) {
                    dilutions = "0";
                }
            }
            String[] dilutionsArray = dilutions.split(",");

            //Load the ranges
            String ranges = "0";
            if (item.has("ranges")) {
                ranges = item.getString("ranges");
            }

            String[] rangesArray = ranges.split(",");

            String[] defaultColorsArray = new String[0];
            if (item.has("defaultColors")) {
                String defaultColors = item.getString("defaultColors");
                defaultColorsArray = defaultColors.split(",");
            }

            // get uuids
            JSONArray uuidArray = item.getJSONArray("uuid");
            ArrayList<String> uuids = new ArrayList<>();
            for (int ii = 0; ii < uuidArray.length(); ii++) {
                String newUuid = uuidArray.getString(ii);
                if (!uuids.contains(newUuid)) {
                    uuids.add(newUuid);
                }
            }

            testInfo = new TestInfo(namesHashTable, type, rangesArray,
                    defaultColorsArray, dilutionsArray, uuids, resultsArray);

            testInfo.setShortCode(item.has("shortCode") ? item.getString("shortCode") : "");

            testInfo.setHueTrend(item.has("hueTrend") ? item.getInt("hueTrend") : 0);

            testInfo.setUseGrayScale(item.has("grayScale") && item.getBoolean("grayScale"));

            testInfo.setMonthsValid(item.has("monthsValid") ? item.getInt("monthsValid") : DEFAULT_MONTHS_VALID);

            //if calibrate not specified then default to false otherwise use specified value
            testInfo.setRequiresCalibration(item.has("calibrate") && item.getString("calibrate").equalsIgnoreCase("true"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return testInfo;
    }

    public static JSONObject getJsonResult(TestInfo testInfo, ArrayList<String> results, int color,
                                           String resultImageUrl) {

        JSONObject resultJson = new JSONObject();

        try {

            resultJson.put(SensorConstants.TYPE, SensorConstants.TYPE_NAME);
            resultJson.put(SensorConstants.NAME, testInfo.getName());
            resultJson.put(SensorConstants.UUID, testInfo.getUuid());


            JSONArray resultsJsonArray = new JSONArray();
            for (TestInfo.SubTest subTest : testInfo.getSubTests()) {
                JSONObject subTestJson = new JSONObject();
                subTestJson.put(SensorConstants.NAME, subTest.getDesc());
                subTestJson.put(SensorConstants.UNIT, subTest.getUnit());
                subTestJson.put(SensorConstants.ID, subTest.getId());

                // If a result exists for the sub test id then add it
                if (results.size() >= subTest.getId()) {
                    subTestJson.put(SensorConstants.VALUE, results.get(subTest.getId() - 1));
                }

                if (color > -1) {
                    subTestJson.put("resultColor", Integer.toHexString(color & BIT_MASK));

                    // Add calibration details to result
                    subTestJson.put("calibratedDate", testInfo.getCalibrationDateString());
                    subTestJson.put("reagentExpiry", testInfo.getExpiryDateString());
                    subTestJson.put("reagentBatch", testInfo.getBatchNumber());

                    JSONArray calibrationSwatches = new JSONArray();
                    for (Swatch swatch : testInfo.getSwatches()) {
                        calibrationSwatches.put(Integer.toHexString(swatch.getColor() & BIT_MASK));
                    }
                    subTestJson.put("calibration", calibrationSwatches);
                }

                resultsJsonArray.put(subTestJson);
            }

            resultJson.put(SensorConstants.RESULT, resultsJsonArray);

            if (!resultImageUrl.isEmpty()) {
                resultJson.put(SensorConstants.IMAGE, resultImageUrl);
            }

            // Add current date to result
            resultJson.put("testDate", new SimpleDateFormat(SensorConstants.DATE_TIME_FORMAT, Locale.US)
                    .format(Calendar.getInstance().getTime()));

            // Add user preference details to the result
            resultJson.put(SensorConstants.USER, TestConfigHelper.getUserPreferences());

            // Add app details to the result
            resultJson.put(SensorConstants.APP, TestConfigHelper.getAppDetails());

            // Add standard diagnostic details to the result
            resultJson.put(SensorConstants.DEVICE, TestConfigHelper.getDeviceDetails());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultJson;
    }

    private static JSONObject getDeviceDetails() throws JSONException {
        JSONObject details = new JSONObject();
        details.put("model", Build.MODEL);
        details.put("product", Build.PRODUCT);
        details.put("manufacturer", Build.MANUFACTURER);
        details.put("os", "Android - " + Build.VERSION.RELEASE + " ("
                + Build.VERSION.SDK_INT + ")");
        details.put("country", Locale.getDefault().getCountry());
        details.put("language", Locale.getDefault().getLanguage());
        return details;
    }

    private static JSONObject getAppDetails() throws JSONException {
        JSONObject details = new JSONObject();
        details.put("appVersion", CaddisflyApp.getAppVersion());
        // The current active language of the app
        details.put("language", CaddisflyApp.getAppLanguage());
        return details;
    }

    private static JSONObject getUserPreferences() throws JSONException {
        JSONObject details = new JSONObject();
        details.put("backDropDetection", !AppPreferences.getNoBackdropDetection());
        details.put("language", PreferencesUtil.getString(CaddisflyApp.getApp(), R.string.languageKey, ""));
        return details;
    }

    /**
     * Returns a Uuid for the given shortCode
     *
     * @param shortCode the test shortCode
     * @return the Uuid
     */
    @Deprecated
    public static String getUuidFromShortCode(String shortCode) {

        if (!shortCode.isEmpty()) {

            // Load the pre-configured tests from the app
            String jsonText = AssetsManager.getInstance().loadJSONFromAsset("tests_config.json");
            try {
                JSONArray array = new JSONObject(jsonText).getJSONArray("tests");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    if (item.has("shortCode") && shortCode.equalsIgnoreCase(item.getString("shortCode"))) {
                        return item.getJSONArray("uuid").getString(0);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
