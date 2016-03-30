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

package com.google.android.libraries.flexbox.test;


import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.google.android.libraries.flexbox.FlexboxLayout;

import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration tests for {@link FlexboxLayout}.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class FlexboxAndroidTest {

    @Rule
    public ActivityTestRule<FlexboxTestActivity> mActivityRule =
            new ActivityTestRule<>(FlexboxTestActivity.class);

    @Test
    public void testLoadFromLayoutXml() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flexbox_simple);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertNotNull(flexboxLayout);
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_ROW_REVERSE));
        assertThat(flexboxLayout.getJustifyContent(), is(FlexboxLayout.JUSTIFY_CONTENT_CENTER));
        assertThat(flexboxLayout.getAlignContent(), is(FlexboxLayout.ALIGN_CONTENT_CENTER));
        assertThat(flexboxLayout.getAlignItems(), is(FlexboxLayout.ALIGN_ITEMS_CENTER));
        assertThat(flexboxLayout.getChildCount(), is(1));

        View child = flexboxLayout.getChildAt(0);
        FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) child.getLayoutParams();
        assertThat(lp.order, is(2));
        assertThat(lp.flexGrow, is(1));
        assertThat(lp.alignSelf, is(FlexboxLayout.LayoutParams.ALIGN_SELF_STRETCH));
    }
}
