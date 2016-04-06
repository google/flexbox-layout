/*
 * Copyright 2016 Google Inc. All rights reserved.
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

package com.google.android.apps.flexbox.test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.google.android.apps.flexbox.MainActivity;
import com.google.android.apps.flexbox.R;
import com.google.android.libraries.flexbox.FlexboxLayout;

import android.content.pm.ActivityInfo;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration tests for {@link MainActivity}.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testAddFlexItem() {
        MainActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertNotNull(flexboxLayout);
        int beforeCount = flexboxLayout.getChildCount();
        onView(withId(R.id.add_fab)).perform(click());

        assertThat(flexboxLayout.getChildCount(), is(beforeCount + 1));
    }

    @Test
    public void testRemoveFlexItem() {
        MainActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertNotNull(flexboxLayout);
        int beforeCount = flexboxLayout.getChildCount();
        onView(withId(R.id.remove_fab)).perform(click());

        assertThat(flexboxLayout.getChildCount(), is(beforeCount - 1));
    }

    @Test
    public void testConfigurationChange() {
        MainActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertNotNull(flexboxLayout);
        onView(withId(R.id.add_fab)).perform(click());
        onView(withId(R.id.add_fab)).perform(click());
        int beforeCount = flexboxLayout.getChildCount();

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // Verify the flex items are restored across the configuration change.
        assertThat(flexboxLayout.getChildCount(), is(beforeCount));
    }
}
