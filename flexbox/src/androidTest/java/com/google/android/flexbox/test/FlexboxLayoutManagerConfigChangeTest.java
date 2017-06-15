/*
 * Copyright 2017 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.flexbox.test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.filters.FlakyTest;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexLine;
import com.google.android.flexbox.FlexboxLayoutManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration tests for {@link FlexboxLayoutManager} with the Activity that handles configration
 * changes manually.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class FlexboxLayoutManagerConfigChangeTest {

    @Rule
    public ActivityTestRule<ConfigChangeActivity> mActivityRule =
            new ActivityTestRule<>(ConfigChangeActivity.class);

    @Test
    @FlakyTest
    public void testFlexLinesDiscardedOnOrientationChange_direction_row() throws Throwable {
        // Verifies the case that the calculated Flex lines are correctly discarded when a
        // orientation
        // happens with an Activity that handles configuration changes manually
        final ConfigChangeActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(activity);
        final TestAdapter adapter = new TestAdapter();

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                // This test assumes that the screen width and the height are different.
                recyclerView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                recyclerView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutManager.setFlexDirection(FlexDirection.ROW);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);

                for (int i = 0; i < 100; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 90, 65);
                    adapter.addItem(lp);
                }
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexLine firstLine = layoutManager.getFlexLines().get(0);
        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int orientation = activity.getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layoutManager.scrollToPosition(0);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexLine firstLineAfterRotation = layoutManager.getFlexLines().get(0);
        assertThat(firstLine.getMainSize(), is(not(firstLineAfterRotation.getMainSize())));
    }

    @Test
    @FlakyTest
    public void testFlexLinesDiscardedOnOrientationChange_direction_column() throws Throwable {
        // Verifies the case that the calculated Flex lines are correctly discarded when a
        // orientation
        // happens with an Activity that handles configuration changes manually
        final ConfigChangeActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(activity);
        final TestAdapter adapter = new TestAdapter();

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                // This test assumes that the screen width and the height are different.
                recyclerView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                recyclerView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);

                for (int i = 0; i < 100; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 90, 65);
                    adapter.addItem(lp);
                }
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexLine firstLine = layoutManager.getFlexLines().get(0);
        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int orientation = activity.getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layoutManager.scrollToPosition(0);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexLine firstLineAfterRotation = layoutManager.getFlexLines().get(0);
        assertThat(firstLine.getMainSize(), is(not(firstLineAfterRotation.getMainSize())));
    }

    /**
     * Creates a new flex item.
     *
     * @param context the context
     * @param width   in DP
     * @param height  in DP
     * @return the created {@link FlexboxLayoutManager.LayoutParams} instance
     */
    private FlexboxLayoutManager.LayoutParams createLayoutParams(Context context, int width,
            int height) {
        return new FlexboxLayoutManager.LayoutParams(
                TestUtil.dpToPixel(context, width),
                TestUtil.dpToPixel(context, height));
    }

    private static ViewAction swipe(CoordinatesProvider from, CoordinatesProvider to) {
        return new GeneralSwipeAction(Swipe.FAST, from, to, Press.FINGER);
    }
}

