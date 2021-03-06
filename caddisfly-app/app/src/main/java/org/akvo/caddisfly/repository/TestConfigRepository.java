package org.akvo.caddisfly.repository;


import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.dao.CalibrationDao;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.entity.CalibrationDetail;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.ColorItem;
import org.akvo.caddisfly.model.TestConfig;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestSampleType;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.AssetsManager;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class TestConfigRepository {

    private static final HashMap<String, List<TestInfo>> testMap = new HashMap<>();

    private final AssetsManager assetsManager;

    public TestConfigRepository() {
        assetsManager = new AssetsManager();
    }

    public List<TestInfo> getTests(TestType testType) {
        return getTests(testType, TestSampleType.ALL);
    }

    /**
     * Get list of tests by type of test.
     *
     * @param testType the test type
     * @return the list of tests
     */
    public List<TestInfo> getTests(TestType testType, TestSampleType testSampleType) {

        List<TestInfo> testInfoList = new ArrayList<>();

        if (testSampleType == TestSampleType.ALL) {
            if (testMap.containsKey(testType.toString())) {
                return testMap.get(testType.toString());
            }
        } else {
            if (testMap.containsKey(testType.toString() + testSampleType.toString())) {
                return testMap.get(testType.toString() + testSampleType.toString());
            }
        }

        try {
            testInfoList = new Gson().fromJson(assetsManager.getJson(), TestConfig.class).getTests();

            for (int i = testInfoList.size() - 1; i >= 0; i--) {
                if (testInfoList.get(i).getSubtype() != testType) {
                    testInfoList.remove(i);
                } else if (testSampleType != TestSampleType.ALL
                        && testInfoList.get(i).getSampleType() != testSampleType) {
                    testInfoList.remove(i);
                }
            }

            if (testType == TestType.BLUETOOTH) {
                Collections.sort(testInfoList, (object1, object2) ->
                        ("000000000".substring(object1.getMd610Id().length()) + object1.getMd610Id())
                                .compareToIgnoreCase(("000000000".substring(object2.getMd610Id().length())
                                        + object2.getMd610Id())));
            } else {
                Collections.sort(testInfoList, (object1, object2) ->
                        object1.getName().compareToIgnoreCase(object2.getName()));
            }

            if (AppPreferences.isDiagnosticMode()) {
                addExperimentalTests(testType, testSampleType, testInfoList);
            }

            TestConfig testConfig = new Gson().fromJson(assetsManager.getCustomJson(), TestConfig.class);
            if (testConfig != null) {
                List<TestInfo> customList = testConfig.getTests();

                for (int i = customList.size() - 1; i >= 0; i--) {
                    if (customList.get(i).getSubtype() != testType) {
                        customList.remove(i);
                    } else if (testSampleType != TestSampleType.ALL
                            && customList.get(i).getSampleType() != testSampleType) {
                        customList.remove(i);
                    }
                }

                if (customList.size() > 0) {
                    Collections.sort(customList, (object1, object2) ->
                            object1.getName().compareToIgnoreCase(object2.getName()));

                    testInfoList.add(new TestInfo("Custom"));

                    testInfoList.addAll(customList);
                }
            }


        } catch (Exception e) {
            Timber.e(e);
        }

        if (testSampleType == TestSampleType.ALL) {
            testMap.put(testType.toString(), testInfoList);
        } else {
            testMap.put(testType.toString() + testSampleType.toString(), testInfoList);
        }

        for (int i = testInfoList.size() - 1; i >= 0; i--) {
            TestInfo testInfo = testInfoList.get(i);
            localizeTestName(testInfo);
        }

        return testInfoList;
    }

    private void addExperimentalTests(TestType testType, TestSampleType testSampleType, List<TestInfo> testInfoList) {
        TestConfig testConfig = new Gson().fromJson(assetsManager.getExperimentalJson(), TestConfig.class);
        if (testConfig != null) {
            List<TestInfo> experimentalList = testConfig.getTests();

            for (int i = experimentalList.size() - 1; i >= 0; i--) {
                if (experimentalList.get(i).getSubtype() != testType) {
                    experimentalList.remove(i);
                } else if (testSampleType != TestSampleType.ALL
                        && experimentalList.get(i).getSampleType() != testSampleType) {
                    experimentalList.remove(i);
                }
            }

            if (experimentalList.size() > 0) {
                Collections.sort(experimentalList, (object1, object2) ->
                        object1.getName().compareToIgnoreCase(object2.getName()));

                testInfoList.add(new TestInfo("Experimental"));

                testInfoList.addAll(experimentalList);
            }
        }
    }

    /**
     * Get the test details from json config.
     *
     * @param id the test id
     * @return the test object
     */
    public TestInfo getTestInfo(final String id) {

        TestInfo testInfo;
        testInfo = getTestInfoItem(assetsManager.getJson(), id);

        if (testInfo == null) {
            if (AppPreferences.isDiagnosticMode()) {
                testInfo = getTestInfoItem(assetsManager.getExperimentalJson(), id);
            }

            if (testInfo == null) {
                testInfo = getTestInfoItem(assetsManager.getCustomJson(), id);
            }
        }
        if (testInfo != null) {
            localizeTestName(testInfo);
        }

        return testInfo;
    }

    private void localizeTestName(TestInfo testInfo) {
        if (testInfo.getName() != null) {
            String name = testInfo.getName().toLowerCase()
                    .replace(")", "")
                    .replace("(", "")
                    .replace(" ", "_");
            int nameId = CaddisflyApp.getApp().getResources()
                    .getIdentifier(name, "string",
                            CaddisflyApp.getApp().getPackageName());
            if (nameId > 0) {
                name = CaddisflyApp.getApp().getString(nameId);
                if (!name.equals(".")) {
                    testInfo.setName(name);
                }
            }
        }
    }

    @Nullable
    private TestInfo getTestInfoItem(String json, String id) {

        List<TestInfo> testInfoList;
        try {
            TestConfig testConfig = new Gson().fromJson(json, TestConfig.class);
            if (testConfig != null) {
                testInfoList = testConfig.getTests();

                for (TestInfo testInfo : testInfoList) {
                    if (testInfo.getUuid().equalsIgnoreCase(id)) {

                        if (testInfo.getSubtype() == TestType.CHAMBER_TEST ||
                                testInfo.getSubtype() == TestType.CUVETTE_BLUETOOTH) {

                            CalibrationDao dao = CaddisflyApp.getApp().getDb().calibrationDao();

                            // if range values are defined as comma delimited text then convert to array
                            convertRangePropertyToArray(testInfo);

                            List<Calibration> calibrations = dao.getAll(testInfo.getUuid());

                            if (calibrations.size() < 1) {

                                calibrations = getPlaceHolderCalibrations(testInfo);

                                // get any calibrations saved by previous version of the app
                                List<Calibration> calibrationsOld = getBackedUpCalibrations(testInfo);

                                for (Calibration calibration : calibrations) {
                                    for (Calibration calibrationOld : calibrationsOld) {
                                        if (calibration.value == calibrationOld.value) {
                                            calibration.color = calibrationOld.color;
                                        }
                                    }
                                }

                                if (calibrations.size() > 0) {
                                    dao.insertAll(calibrations);
                                }
                            }
                            testInfo.setCalibrations(calibrations);
                        }
                        return testInfo;
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            // do nothing
        }

        return null;
    }

    private List<Calibration> getPlaceHolderCalibrations(TestInfo testInfo) {
        List<Calibration> calibrations = new ArrayList<>();

        for (ColorItem colorItem : testInfo.getResults().get(0).getColors()) {
            Calibration calibration = new Calibration();
            calibration.uid = testInfo.getUuid();
            calibration.color = Color.TRANSPARENT;
            calibration.value = colorItem.getValue();
            calibrations.add(calibration);
        }

        return calibrations;
    }

    private List<Calibration> getBackedUpCalibrations(TestInfo testInfo) {
        List<Calibration> calibrations = new ArrayList<>();

        Context context = CaddisflyApp.getApp();

        List<ColorItem> colors = testInfo.getResults().get(0).getColors();
        for (ColorItem color : colors) {
            String key = String.format(Locale.US, "%s-%.2f",
                    testInfo.getUuid(), color.getValue());
            Calibration calibration = new Calibration();
            calibration.uid = testInfo.getUuid();
            calibration.color = PreferencesUtil.getInt(context, key, 0);
            calibration.value = color.getValue();
            calibrations.add(calibration);
        }

        CalibrationDetail calibrationDetail = new CalibrationDetail();
        calibrationDetail.uid = testInfo.getUuid();
        long date = PreferencesUtil.getLong(context,
                testInfo.getUuid(), R.string.calibrationDateKey);
        if (date > 0) {
            calibrationDetail.date = date;
        }
        long expiry = PreferencesUtil.getLong(context,
                testInfo.getUuid(), R.string.calibrationExpiryDateKey);
        if (expiry > 0) {
            calibrationDetail.expiry = expiry;
        }

        CalibrationDao dao = CaddisflyApp.getApp().getDb().calibrationDao();
        dao.insert(calibrationDetail);

        if (calibrations.size() < 1) {
            try {
                calibrations = SwatchHelper.loadCalibrationFromFile(testInfo, "_AutoBackup");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            boolean colorFound = false;
            for (Calibration calibration : calibrations) {
                if (calibration.color != 0) {
                    colorFound = true;
                }
            }
            if (!colorFound) {
                try {
                    calibrations = SwatchHelper.loadCalibrationFromFile(testInfo, "_AutoBackup");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return calibrations;
    }

    private void convertRangePropertyToArray(TestInfo testInfo) {
        // If colors are defined as comma delimited range values then create array
        try {
            if (testInfo.getResults().get(0).getColors().size() == 0
                    && !testInfo.getRanges().isEmpty()) {
                String[] values = testInfo.getRanges().split(",");
                for (String value : values) {
                    testInfo.getResults().get(0).getColors()
                            .add(new ColorItem(Double.parseDouble(value)));
                }
            }
        } catch (NumberFormatException ignored) {
            // do nothing
        }
    }

    public void clear() {
        testMap.clear();
    }
}
