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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    @Test
    public void testOrderAttribute_fromLayoutXml() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flexbox_order_test);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertNotNull(flexboxLayout);
        assertThat(flexboxLayout.getChildCount(), is(4));
        // order: -1, index 1
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(0)).getText().toString(),
                is(String.valueOf(2)));
        // order: 0, index 2
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(1)).getText().toString(),
                is(String.valueOf(3)));
        // order: 1, index 3
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(2)).getText().toString(),
                is(String.valueOf(4)));
        // order: 2, index 0
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(3)).getText().toString(),
                is(String.valueOf(1)));
    }

    @Test
    public void testOrderAttribute_fromCode() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flexbox_order_test);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        TextView fifth = createTextView(activity, String.valueOf(5), 0);
        TextView sixth = createTextView(activity, String.valueOf(6), -10);
        flexboxLayout.addView(fifth);
        flexboxLayout.addView(sixth);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(flexboxLayout.getChildCount(), is(6));
        // order: -10, index 5
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(0)).getText().toString(),
                is(String.valueOf(6)));
        // order: -1, index 1
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(1)).getText().toString(),
                is(String.valueOf(2)));
        // order: 0, index 2
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(2)).getText().toString(),
                is(String.valueOf(3)));
        // order: 0, index 4
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(3)).getText().toString(),
                is(String.valueOf(5)));
        // order: 1, index 3
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(4)).getText().toString(),
                is(String.valueOf(4)));
        // order: 2, index 0
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(5)).getText().toString(),
                is(String.valueOf(1)));
    }

    private TextView createTextView(Context context, String text, int order) {
        TextView textView = new TextView(context);
        textView.setText(text);
        FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        lp.order = order;
        textView.setLayoutParams(lp);
        return textView;
    }
}
