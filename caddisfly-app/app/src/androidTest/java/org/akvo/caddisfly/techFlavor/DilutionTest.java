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

package org.akvo.caddisfly.techFlavor;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.ui.MainActivity;
import org.akvo.caddisfly.util.TestConstant;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.akvo.caddisfly.util.TestHelper.clickExternalSourceButton;
import static org.akvo.caddisfly.util.TestHelper.gotoSurveyForm;
import static org.akvo.caddisfly.util.TestHelper.loadData;
import static org.akvo.caddisfly.util.TestHelper.mCurrentLanguage;
import static org.akvo.caddisfly.util.TestHelper.mDevice;
import static org.akvo.caddisfly.util.TestUtil.sleep;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

/**
 * Testing the product flavor for specific customer
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class DilutionTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @BeforeClass
    public static void initialize() {
        if (mDevice == null) {
            mDevice = UiDevice.getInstance(getInstrumentation());

            for (int i = 0; i < 5; i++) {
                mDevice.pressBack();
            }
        }
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    @Before
    public void setUp() {

        loadData(mActivityTestRule.getActivity(), mCurrentLanguage);

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(mActivityTestRule.getActivity());
        prefs.edit().clear().apply();

        CaddisflyApp.getApp().setCurrentTestInfo(new TestInfo(null,
                TestType.COLORIMETRIC_LIQUID, new String[]{}, new String[]{}, new String[]{}, null, null, null));
    }

    @Test
    public void dilutionTest() {
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.buttonCalibrate), withText("Calibrate"),
                        childAtPosition(
                                allOf(withId(R.id.mainLayout),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                3),
                        isDisplayed()));
        appCompatButton.perform(click());

        onView(withText("0.00 ppm")).check(matches(isDisplayed()));

        onView(withText("0.50 ppm")).check(matches(isDisplayed()));

        onView(withText("1.00 ppm")).check(matches(isDisplayed()));

        onView(withText("2.00 ppm")).check(matches(isDisplayed()));

        onView(withText("3.00 ppm")).check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.buttonSurvey), withText("Survey"),
                        childAtPosition(
                                allOf(withId(R.id.mainLayout),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                0),
                        isDisplayed()));
        appCompatButton2.perform(click());

        gotoSurveyForm();

        clickExternalSourceButton(TestConstant.NEXT);

        clickExternalSourceButton(TestConstant.NEXT);

        clickExternalSourceButton(0);

        sleep(1000);

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.buttonCustomDilution), withText("Custom Dilution"),
                        childAtPosition(
                                allOf(withId(R.id.layoutDilutions),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                5),
                        isDisplayed()));
        appCompatButton3.perform(click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.editDilutionFactor),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.custom),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("25"), closeSoftKeyboard());

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                allOf(withClassName(is("com.android.internal.widget.ButtonBarLayout")),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                3)),
                                3),
                        isDisplayed()));
        appCompatButton4.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.textDilution), withText("25 Times Dilution"),
                        childAtPosition(
                                allOf(withId(R.id.layoutTitleBar),
                                        childAtPosition(
                                                IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                                1)),
                                1),
                        isDisplayed()));
        textView.check(matches(withText("25 Times Dilution")));

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.textTitle), withText("Water - Chromium"),
                        childAtPosition(
                                allOf(withId(R.id.layoutTitleBar),
                                        childAtPosition(
                                                IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                                1)),
                                0),
                        isDisplayed()));
        textView2.check(matches(withText("Water - Chromium")));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.textSubtitle), withText("Place device face down on a flat surface"),
                        childAtPosition(
                                allOf(withId(R.id.layoutTitleBar),
                                        childAtPosition(
                                                IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                                1)),
                                2),
                        isDisplayed()));
        textView3.check(matches(withText("Place device face down on a flat surface")));

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.textTitle), withText("Water - Chromium"),
                        childAtPosition(
                                allOf(withId(R.id.layoutTitleBar),
                                        childAtPosition(
                                                withId(R.id.layoutDilutions),
                                                1)),
                                0),
                        isDisplayed()));
        textView4.check(matches(withText("Water - Chromium")));

        ViewInteraction button = onView(
                allOf(withId(R.id.buttonNoDilution),
                        childAtPosition(
                                allOf(withId(R.id.layoutDilutions),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                2),
                        isDisplayed()));
        button.check(matches(isDisplayed()));

        ViewInteraction button2 = onView(
                allOf(withId(R.id.buttonDilution1),
                        childAtPosition(
                                allOf(withId(R.id.layoutDilutions),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                3),
                        isDisplayed()));
        button2.check(matches(isDisplayed()));

        ViewInteraction button3 = onView(
                allOf(withId(R.id.buttonDilution2),
                        childAtPosition(
                                allOf(withId(R.id.layoutDilutions),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                4),
                        isDisplayed()));
        button3.check(matches(isDisplayed()));

        ViewInteraction button4 = onView(
                allOf(withId(R.id.buttonCustomDilution),
                        childAtPosition(
                                allOf(withId(R.id.layoutDilutions),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                5),
                        isDisplayed()));
        button4.check(matches(isDisplayed()));

    }
}
