package org.akvo.caddisfly;
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

import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoActivityResumedException;
import android.test.ActivityInstrumentationTestCase2;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.MainActivity;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.object.HasToString.hasToString;
import static org.hamcrest.text.StringStartsWith.startsWith;
import static org.hamcrest.Matchers.not;

public class EspressoTest
        extends ActivityInstrumentationTestCase2<MainActivity> {
    public EspressoTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        getActivity();
    }

    @SuppressWarnings("EmptyMethod")
    public void testA() {
    }

    public void testAbout() {
        onView(withId(R.id.action_settings))
                .perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        Espresso.pressBack();
    }

    public void testCalibrateSwatches() {
        onView(withId(R.id.action_settings))
                .perform(click());

        onView(withText(R.string.calibrateColors))
                .perform(click());

        onView(withId(R.id.action_swatches))
                .perform(click());

        Espresso.pressBack();

        onView(withId(R.id.action_swatches)).check(matches(isDisplayed()));

        Espresso.pressBack();

        onView(withText(R.string.calibrateColors)).check(matches(isDisplayed()));

        Espresso.pressBack();

        onView(withId(R.id.action_settings)).check(matches(isDisplayed()));
    }

    public void testChangeTestType() {
        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.calibrateColors)).perform(click());

        onView(withText("0.0")).perform(click());

        Espresso.pressBack();

        onView(withId(R.id.actionbar_spinner)).perform(click());

        onView(withText("Free Chlorine")).perform(click());

        onView(withText("2.0")).perform(click());

        Espresso.pressBack();

        onView(withId(R.id.actionbar_spinner)).perform(click());

        onView(withText("Nitrite")).perform(click());

        onView(withText("3.0")).perform(click());

        Espresso.pressBack();

        onView(withId(R.id.actionbar_spinner)).perform(click());

        onView(withText("pH")).perform(click());

        onView(withText("9.0")).perform(click());

        onView(withId(R.id.startButton)).perform(click());

        //onView(withText(R.string.selectDilution)).check(matches(isDisplayed()));

        //onView(withId(android.R.id.button2)).perform(click());

        Espresso.pressBack();

        Espresso.pressBack();

        Espresso.pressBack();
    }

    public void testLanguage() {
        onView(withId(R.id.action_settings))
                .perform(click());

        onView(withText(R.string.language))
                .perform(click());

        onData(hasToString(startsWith("العربية"))).perform(click());
    }

    public void testLanguage2() {
        onView(withId(R.id.action_settings))
                .perform(click());

        onView(withText(R.string.language))
                .perform(click());

        onData(hasToString(startsWith("हिंदी"))).perform(click());
    }

    public void testLanguage3() {
        onView(withId(R.id.action_settings))
                .perform(click());

        onView(withText(R.string.language))
                .perform(click());

        onData(hasToString(startsWith("Français"))).perform(click());
    }

    public void testLanguage4() {
        onView(withId(R.id.action_settings))
                .perform(click());

        onView(withText(R.string.language))
                .perform(click());

        onData(hasToString(startsWith("English"))).perform(click());
    }

    public void testStartASurvey() {
        onView(withId(R.id.surveyButton)).check(matches(isClickable()));

        onView(withId(R.id.surveyButton)).perform(click());

        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.hundredPercentButton)).check(matches(isDisplayed()));

        onView(withId(R.id.fiftyPercentButton)).check(matches(isDisplayed()));

        onView(withId(R.id.twentyFivePercentButton)).check(matches(isDisplayed()));

        onView(withId(R.id.hundredPercentButton)).perform(click());

        onView(withId(R.id.startButton)).perform(click());

        onView(withId(R.id.placeInStandText)).check(matches(isDisplayed()));

        try {
            Espresso.pressBack();
        } catch (NoActivityResumedException ignored) {
        }
    }

    public void testCalibrateSensor(){
        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.calibrateSensorSummary)).check(matches(isDisplayed()));

        onView(withText(R.string.calibrateSensor)).perform(click());

        onView(withId(R.id.startButton)).perform(click());

        onView(withText(R.string.notConnected)).check(matches(isDisplayed()));
        onView(withText(R.string.deviceConnectSensor)).check(matches(isDisplayed()));

        onView(withId(android.R.id.button2)).perform(click());

        Espresso.pressBack();

        onView(withText(R.string.calibrateSensorSummary)).check(matches(isDisplayed()));

        Espresso.pressBack();
    }

    public void testUpdate(){

        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.updateCheck)).check(matches(isDisplayed()));

        onView(withText(R.string.updateSummary)).check(matches(isDisplayed()));

        onView(withText(R.string.updateCheck)).perform(click());

        onView(withText(R.string.dataConnection)).check(matches(isDisplayed()));
        onView(withText(R.string.enableInternet)).check(matches(isDisplayed()));

        //onView(withId(android.R.id.button2)).perform(click());

        Espresso.pressBack();

        onView(withText(R.string.updateSummary)).check(matches(isDisplayed()));

        Espresso.pressBack();

    }

    public void testDeveloperMode(){

        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        for (int i = 0; i < 10; i++) {
            onView(withId(R.id.logoImageView)).perform(click());
        }

        onView(withText(R.string.enableUserMode)).check(matches(isDisplayed()));

        Espresso.pressBack();

        onView(withText(R.string.calibrateColors)).perform(click());

        onView(withId(R.id.action_swatches)).perform(click());

        Espresso.pressBack();

        Espresso.pressBack();

        onView(withText(R.string.about)).check(matches(isDisplayed())).perform(click());

        onView(withId(R.id.disableDeveloperButton)).perform(click());

        onView(withId(R.id.disableDeveloperButton)).check(matches(not(isDisplayed())));

        Espresso.pressBack();

    }

    public void testStartCalibrate() {
        onView(withId(R.id.action_settings)).perform(click());

        onView(withText(R.string.calibrateColors)).perform(click());

        onView(withId(R.id.actionbar_spinner)).perform(click());

        onView(withText("pH")).perform(click());

        onView(withText("9.0")).perform(click());

        onView(withId(R.id.startButton)).perform(click());

        //onView(withText(R.string.startTestConfirm)).check(matches(isDisplayed()));

        try {
            Thread.sleep(70000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.okButton)).perform(click());

        Espresso.pressBack();

        Espresso.pressBack();
    }


//    public void tearDown() throws Exception {
//        Thread.sleep(1000);
//        goBackN();
//        super.tearDown();
//    }
//
//    private void goBackN() {
//        final int N = 3; // how many times to hit back button
//        try {
//            for (int i = 0; i < N; i++)
//                Espresso.pressBack();
//        } catch (NoActivityResumedException e) {
//            //Log.e(TAG, "Closed all activities", e);
//        }
//    }
}