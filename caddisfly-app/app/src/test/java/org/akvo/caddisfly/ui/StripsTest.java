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

package org.akvo.caddisfly.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.common.ConstantKey;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowLooper;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class StripsTest {

    @Test
    public void titleIsCorrect() {

        Activity activity = Robolectric.setupActivity(TestListActivity.class);
        TextView textView = activity.findViewById(R.id.textToolbarTitle);
        assertEquals(textView.getText(), "Select Test");
    }

    @Test
    public void testCount() {

        Intent intent = new Intent();
        intent.putExtra(ConstantKey.TYPE, TestType.STRIP_TEST);

        ActivityController controller = Robolectric.buildActivity(TestListActivity.class, intent).create();

        controller.start().visible();

        Activity activity = (Activity) controller.get();

        RecyclerView recyclerView = activity.findViewById(R.id.list_types);

        assertSame(2, recyclerView.getChildCount());

        TestInfoAdapter adapter = (TestInfoAdapter) recyclerView.getAdapter();
        recyclerView.getAdapter();
        assertEquals("Water - Potassium",
                adapter.getItemAt(1).getName());
        assertEquals("Water - Potassium",
                ((TextView) recyclerView.getChildAt(1).findViewById(R.id.text_title)).getText());
    }

    @Test
    public void testTitles() {
        Intent intent = new Intent();
        intent.putExtra(ConstantKey.TYPE, TestType.STRIP_TEST);

        ActivityController controller = Robolectric.buildActivity(TestListActivity.class, intent).create();

        controller.start().visible();

        Activity activity = (Activity) controller.get();

        RecyclerView recyclerView = activity.findViewById(R.id.list_types);

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            TestInfo testInfo = ((TestInfoAdapter) recyclerView.getAdapter()).getItemAt(0);
            String title = testInfo.getName();
            assertEquals(title,
                    ((TextView) recyclerView.getChildAt(0).findViewById(R.id.text_title)).getText());
        }
    }

    @Test
    public void clickTest() {

        Intent intent = new Intent();
        intent.putExtra(ConstantKey.TYPE, TestType.STRIP_TEST);

        ActivityController controller = Robolectric.buildActivity(TestListActivity.class, intent).create();

        controller.start().visible();

        Activity activity = (Activity) controller.get();

        RecyclerView recyclerView = activity.findViewById(R.id.list_types);

        assertSame(2, recyclerView.getChildCount());

        recyclerView.getChildAt(1).performClick();

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        Intent nextIntent = shadowOf(activity).getNextStartedActivity();

        if (nextIntent.getComponent() != null) {
            assertEquals(TestActivity.class.getCanonicalName(),
                    nextIntent.getComponent().getClassName());
        }
    }

    @Test
    public void clickHome() {

        Activity activity = Robolectric.setupActivity(TestListActivity.class);

        ShadowActivity shadowActivity = shadowOf(activity);
        shadowActivity.clickMenuItem(android.R.id.home);
        Intent intent = shadowOf(activity).getNextStartedActivity();

        assertNull(intent);
    }

}
