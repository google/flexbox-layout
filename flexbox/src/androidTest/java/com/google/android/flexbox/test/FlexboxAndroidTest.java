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

package com.google.android.flexbox.test;


import com.google.android.flexbox.AlignContent;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewAssertion;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.res.ResourcesCompat;
import android.test.FlakyTest;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.PositionAssertions.isAbove;
import static android.support.test.espresso.assertion.PositionAssertions.isBelow;
import static android.support.test.espresso.assertion.PositionAssertions.isBottomAlignedWith;
import static android.support.test.espresso.assertion.PositionAssertions.isLeftAlignedWith;
import static android.support.test.espresso.assertion.PositionAssertions.isLeftOf;
import static android.support.test.espresso.assertion.PositionAssertions.isRightAlignedWith;
import static android.support.test.espresso.assertion.PositionAssertions.isRightOf;
import static android.support.test.espresso.assertion.PositionAssertions.isTopAlignedWith;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Integration tests for {@link FlexboxLayout}.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class FlexboxAndroidTest {

    private static final int TOLERANCE = 3;

    @Rule
    public ActivityTestRule<FlexboxTestActivity> mActivityRule =
            new ActivityTestRule<>(FlexboxTestActivity.class);

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testLoadFromLayoutXml() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_simple);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertNotNull(flexboxLayout);
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW_REVERSE));
        assertThat(flexboxLayout.getJustifyContent(), is(JustifyContent.CENTER));
        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.CENTER));
        assertThat(flexboxLayout.getAlignItems(), is(AlignItems.CENTER));
        assertThat(flexboxLayout.getChildCount(), is(1));

        View child = flexboxLayout.getChildAt(0);
        FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) child.getLayoutParams();
        assertThat(lp.order, is(2));
        assertThat(lp.flexGrow, is(1f));
        assertThat(lp.alignSelf, is(FlexboxLayout.LayoutParams.ALIGN_SELF_STRETCH));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testOrderAttribute_fromLayoutXml() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_order_test);
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
    @FlakyTest(tolerance = TOLERANCE)
    public void testOrderAttribute_fromCode() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_order_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                TextView fifth = createTextView(activity, String.valueOf(5), 0);
                TextView sixth = createTextView(activity, String.valueOf(6), -10);
                flexboxLayout.addView(fifth);
                flexboxLayout.addView(sixth);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
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

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testChangeOrder_fromChildSetLayoutParams() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_order_test);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        final FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                .findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getChildCount(), is(4));
        // order: -1, index 1
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(0)).getText().toString(),
                is(String.valueOf(2)));
        // order: 0, index 2
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(1)).getText().toString(),
                is(String.valueOf(3)));
        // order: 0, index 3
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(2)).getText().toString(),
                is(String.valueOf(4)));
        // order: 2, index 0
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(3)).getText().toString(),
                is(String.valueOf(1)));

        // By changing the order and calling the setLayoutParams, the reordered array in the
        // FlexboxLayout (mReordereredIndices) will be recreated without adding a new View.
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View view1 = flexboxLayout.getChildAt(0);
                FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams)
                        view1.getLayoutParams();
                lp.order = -3;
                view1.setLayoutParams(lp);
            }
        });
        // order: -3, index 0
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(3)).getText().toString(),
                is(String.valueOf(1)));
        // order: -1, index 1
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(0)).getText().toString(),
                is(String.valueOf(2)));
        // order: 0, index 2
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(1)).getText().toString(),
                is(String.valueOf(3)));
        // order: 1, index 3
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(2)).getText().toString(),
                is(String.valueOf(4)));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testOrderAttribute_addViewInMiddle() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_order_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                TextView fifth = createTextView(activity, String.valueOf(5), 0);
                // Add the new TextView in the middle of the indices
                flexboxLayout.addView(fifth, 2);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertNotNull(flexboxLayout);
        assertThat(flexboxLayout.getChildCount(), is(5));
        // order: -1, index 1
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(0)).getText().toString(),
                is(String.valueOf(2)));
        // order: 0, index 2
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(1)).getText().toString(),
                is(String.valueOf(5)));
        // order: 0, index 3
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(2)).getText().toString(),
                is(String.valueOf(3)));
        // order: 0, index 4
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(3)).getText().toString(),
                is(String.valueOf(4)));
        // order: 2, index 0
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(4)).getText().toString(),
                is(String.valueOf(1)));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testOrderAttribute_removeLastView() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_order_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.removeViewAt(flexboxLayout.getChildCount() - 1);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertNotNull(flexboxLayout);
        assertThat(flexboxLayout.getChildCount(), is(3));
        // order: -1, index 1
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(0)).getText().toString(),
                is(String.valueOf(2)));
        // order: 0, index 2
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(1)).getText().toString(),
                is(String.valueOf(3)));
        // order: 2, index 0
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(2)).getText().toString(),
                is(String.valueOf(1)));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testOrderAttribute_removeViewInMiddle() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_order_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.removeViewAt(2);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertNotNull(flexboxLayout);
        assertThat(flexboxLayout.getChildCount(), is(3));
        // order: -1, index 1
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(0)).getText().toString(),
                is(String.valueOf(2)));
        // order: 0, index 3
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(1)).getText().toString(),
                is(String.valueOf(4)));
        // order: 2, index 0
        assertThat(((TextView) flexboxLayout.getReorderedChildAt(2)).getText().toString(),
                is(String.valueOf(1)));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexWrap_wrap() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flex_wrap_test);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        // The width of the FlexboxLayout is not enough for placing the three text views.
        // The third text view should be placed below the first one
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        assertThat(flexboxLayout.getFlexLines().size(), is(2));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexWrap_nowrap() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flex_wrap_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexWrap(FlexWrap.NOWRAP);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.NOWRAP));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        // The width of the FlexboxLayout is not enough for placing the three text views.
        // But the flexWrap attribute is set to NOWRAP, the third text view is placed
        // to the right of the second one and overflowing the parent FlexboxLayout.
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        assertThat(flexboxLayout.getFlexLines().size(), is(1));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexWrap_wrap_reverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flex_wrap_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexWrap(FlexWrap.WRAP_REVERSE);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP_REVERSE));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        // The width of the FlexboxLayout is not enough for placing the three text views.
        // There should be two flex lines same as WRAP, but the layout starts from bottom
        // to top in WRAP_REVERSE
        onView(withId(R.id.text3)).check(isAbove(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isAbove(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        assertThat(flexboxLayout.getFlexLines().size(), is(2));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexWrap_wrap_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flex_wrap_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        // The height of the FlexboxLayout is not enough for placing the three text views.
        // The third text view should be placed right of the first one
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        assertThat(flexboxLayout.getFlexLines().size(), is(2));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexWrap_nowrap_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flex_wrap_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                flexboxLayout.setFlexWrap(FlexWrap.NOWRAP);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.NOWRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        // The height of the FlexboxLayout is not enough for placing the three text views.
        // But the flexWrap attribute is set to NOWRAP, the third text view is placed
        // below the second one and overflowing the parent FlexboxLayout.
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        assertThat(flexboxLayout.getFlexLines().size(), is(1));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexWrap_wrap_reverse_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flex_wrap_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                flexboxLayout.setFlexWrap(FlexWrap.WRAP_REVERSE);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP_REVERSE));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        // The width of the FlexboxLayout is not enough for placing the three text views.
        // There should be two flex lines same as WRAP, but the layout starts from right
        // to left in WRAP_REVERSE
        onView(withId(R.id.text3)).check(isLeftOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isLeftOf(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        assertThat(flexboxLayout.getFlexLines().size(), is(2));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexItem_match_parent() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flex_item_match_parent);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        TextView text3 = (TextView) activity.findViewById(R.id.text3);

        assertThat(text1.getWidth(), is(flexboxLayout.getWidth()));
        assertThat(text2.getWidth(), is(flexboxLayout.getWidth()));
        assertThat(text3.getWidth(), is(flexboxLayout.getWidth()));
        assertThat(flexboxLayout.getHeight(),
                is(text1.getHeight() + text2.getHeight() + text3.getHeight()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexItem_match_parent_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flex_item_match_parent_direction_column);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        TextView text3 = (TextView) activity.findViewById(R.id.text3);

        assertThat(text1.getHeight(), is(flexboxLayout.getHeight()));
        assertThat(text2.getHeight(), is(flexboxLayout.getHeight()));
        assertThat(text3.getHeight(), is(flexboxLayout.getHeight()));
        assertThat(flexboxLayout.getWidth(),
                is(text1.getWidth() + text2.getWidth() + text3.getWidth()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexboxLayout_wrapContent() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flexbox_wrap_content);
            }
        });
        // The parent FlexboxLayout's layout_width and layout_height are set to wrap_content
        // The size of the FlexboxLayout is aligned with three text views.

        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));

        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));

        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexboxLayout_wrapped_with_ScrollView() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flexbox_wrapped_with_scrollview);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));

        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));

        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));

        // The heightMode of the FlexboxLayout is set as MeasureSpec.UNSPECIFIED, the height of the
        // layout will be expanded to include the all children views
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(flexboxLayout.getHeight(), is(textView1.getHeight() + textView3.getHeight()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexboxLayout_wrapped_with_HorizontalScrollView() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(
                        R.layout.activity_flexbox_wrapped_with_horizontalscrollview);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));

        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));

        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));

        // The widthMode of the FlexboxLayout is set as MeasureSpec.UNSPECIFIED, the widht of the
        // layout will be expanded to include the all children views
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(flexboxLayout.getWidth(), is(textView1.getWidth() + textView2.getWidth() +
                textView3.getWidth()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testJustifyContent_flexStart() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_justify_content_test);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(), is(JustifyContent.FLEX_START));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testJustifyContent_flexStart_withParentPadding() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_justify_content_with_parent_padding);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(), is(JustifyContent.FLEX_START));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));
        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        // Both the parent FrameLayout and the FlexboxLayout have different padding values
        // but the text1.getLeft should be the padding value for the FlexboxLayout, not including
        // the parent's padding value
        assertThat(text1.getLeft(), is(flexboxLayout.getPaddingLeft()));
        assertThat(text1.getTop(), is(flexboxLayout.getPaddingTop()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testJustifyContent_flexEnd() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_justify_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setJustifyContent(JustifyContent.FLEX_END);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(), is(JustifyContent.FLEX_END));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftOf(withId(R.id.text3)));
        onView(withId(R.id.text1)).check(isLeftOf(withId(R.id.text2)));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testJustifyContent_flexEnd_withParentPadding() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_justify_content_with_parent_padding);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setJustifyContent(JustifyContent.FLEX_END);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(), is(JustifyContent.FLEX_END));
        onView(withId(R.id.text2)).check(isLeftOf(withId(R.id.text3)));
        onView(withId(R.id.text1)).check(isLeftOf(withId(R.id.text2)));
        TextView text3 = (TextView) activity.findViewById(R.id.text3);
        // Both the parent FrameLayout and the FlexboxLayout have different padding values
        // but the text3.getRight should be the padding value for the FlexboxLayout, not including
        // the parent's padding value
        assertThat(flexboxLayout.getWidth() - text3.getRight(),
                is(flexboxLayout.getPaddingRight()));
        assertThat(text3.getTop(), is(flexboxLayout.getPaddingTop()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testJustifyContent_center() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_justify_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setJustifyContent(JustifyContent.CENTER);
            }
        });

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getJustifyContent(), is(JustifyContent.CENTER));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int space = flexboxLayout.getWidth() - textView1.getWidth() - textView2.getWidth() -
                textView3.getWidth();
        space = space / 2;
        assertTrue(space - 1 <= textView1.getLeft() && textView1.getLeft() <= space + 1);
        assertTrue(space - 1 <= flexboxLayout.getRight() - textView3.getRight()
                && flexboxLayout.getRight() - textView3.getRight() <= space + 1);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testJustifyContent_center_withParentPadding() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_justify_content_with_parent_padding);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setJustifyContent(JustifyContent.CENTER);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getJustifyContent(), is(JustifyContent.CENTER));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int space = flexboxLayout.getWidth() - textView1.getWidth() - textView2.getWidth() -
                textView3.getWidth() - flexboxLayout.getPaddingLeft() -
                flexboxLayout.getPaddingRight();
        space = space / 2;
        assertTrue(space - 1 <= textView1.getLeft() - flexboxLayout.getPaddingLeft()
                && textView1.getLeft() - flexboxLayout.getPaddingLeft() <= space + 1);
        assertTrue(space - 1 <= flexboxLayout.getWidth() - textView3.getRight()
                - flexboxLayout.getPaddingRight()
                && flexboxLayout.getWidth() - textView3.getRight() - flexboxLayout.getPaddingRight()
                <= space + 1);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testJustifyContent_spaceBetween() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_justify_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setJustifyContent(JustifyContent.SPACE_BETWEEN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(),
                is(JustifyContent.SPACE_BETWEEN));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int space = flexboxLayout.getWidth() - textView1.getWidth() - textView2.getWidth() -
                textView3.getWidth();
        space = space / 2;
        assertTrue(space - 1 <= textView2.getLeft() - textView1.getRight() &&
                textView2.getLeft() - textView1.getRight() <= space + 1);
        assertTrue(space - 1 <= textView3.getLeft() - textView2.getRight() &&
                textView3.getLeft() - textView2.getRight() <= space + 1);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testJustifyContent_spaceBetween_withPadding() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final int padding = 40;
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_justify_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setJustifyContent(JustifyContent.SPACE_BETWEEN);
                flexboxLayout.setPadding(padding, padding, padding, padding);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(),
                is(JustifyContent.SPACE_BETWEEN));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int space = flexboxLayout.getWidth() - textView1.getWidth() - textView2.getWidth() -
                textView3.getWidth() - padding * 2;
        space = space / 2;
        assertThat(textView1.getLeft(), is(padding));
        assertThat(flexboxLayout.getRight() - textView3.getRight(), is(padding));
        assertTrue(space - 1 <= textView2.getLeft() - textView1.getRight() &&
                textView2.getLeft() - textView1.getRight() <= space + 1);
        assertTrue(space - 1 <= textView3.getLeft() - textView2.getRight() &&
                textView3.getLeft() - textView2.getRight() <= space + 1);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testJustifyContent_spaceAround() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_justify_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setJustifyContent(JustifyContent.SPACE_AROUND);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(),
                is(JustifyContent.SPACE_AROUND));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int space = flexboxLayout.getWidth() - textView1.getWidth() - textView2.getWidth() -
                textView3.getWidth();
        space = space / 6; // Divide by the number of children * 2
        assertTrue(space - 1 <= textView1.getLeft() && textView1.getLeft() <= space + 1);
        int spaceLowerBound = space * 2 - 1;
        int spaceUpperBound = space * 2 + 1;
        assertTrue(spaceLowerBound <= textView2.getLeft() - textView1.getRight() &&
                textView2.getLeft() - textView1.getRight() <= spaceUpperBound);
        assertTrue(spaceLowerBound <= textView3.getLeft() - textView2.getRight() &&
                textView3.getLeft() - textView2.getRight() <= spaceUpperBound);
        assertTrue(space - 1 <= flexboxLayout.getRight() - textView3.getRight() &&
                flexboxLayout.getRight() - textView3.getRight() <= space + 1);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testJustifyContent_spaceAround_withPadding() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final int padding = 40;
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_justify_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setJustifyContent(JustifyContent.SPACE_AROUND);
                flexboxLayout.setPadding(padding, padding, padding, padding);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(),
                is(JustifyContent.SPACE_AROUND));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int space = flexboxLayout.getWidth() - textView1.getWidth() - textView2.getWidth() -
                textView3.getWidth() - padding * 2;
        space = space / 6; // Divide by the number of children * 2
        assertTrue(space - 1 <= textView1.getLeft() - padding
                && textView1.getLeft() - padding <= space + 1);
        int spaceLowerBound = space * 2 - 1;
        int spaceUpperBound = space * 2 + 1;
        assertTrue(spaceLowerBound <= textView2.getLeft() - textView1.getRight() &&
                textView2.getLeft() - textView1.getRight() <= spaceUpperBound);
        assertTrue(spaceLowerBound <= textView3.getLeft() - textView2.getRight() &&
                textView3.getLeft() - textView2.getRight() <= spaceUpperBound);
        assertTrue(space - 1 <= flexboxLayout.getRight() - textView3.getRight() - padding &&
                flexboxLayout.getRight() - textView3.getRight() - padding <= space + 1);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testJustifyContent_flexStart_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_justify_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(), is(JustifyContent.FLEX_START));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testJustifyContent_flexEnd_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_justify_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setJustifyContent(JustifyContent.FLEX_END);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(), is(JustifyContent.FLEX_END));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isAbove(withId(R.id.text3)));
        onView(withId(R.id.text1)).check(isAbove(withId(R.id.text2)));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testJustifyContent_center_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_justify_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setJustifyContent(JustifyContent.CENTER);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(), is(JustifyContent.CENTER));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int space = flexboxLayout.getHeight() - textView1.getHeight() - textView2.getHeight() -
                textView3.getHeight();
        space = space / 2;
        assertTrue(space - 1 <= textView1.getTop() && textView1.getTop() <= space + 1);
        assertTrue(space - 1 <= flexboxLayout.getBottom() - textView3.getBottom() &&
                flexboxLayout.getBottom() - textView3.getBottom() <= space + 1);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testJustifyContent_spaceBetween_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_justify_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setJustifyContent(JustifyContent.SPACE_BETWEEN);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(),
                is(JustifyContent.SPACE_BETWEEN));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int space = flexboxLayout.getHeight() - textView1.getHeight() - textView2.getHeight() -
                textView3.getHeight();
        space = space / 2;
        assertTrue(space - 1 <= textView2.getTop() - textView1.getBottom() &&
                textView2.getTop() - textView1.getBottom() <= space + 1);
        assertTrue(space - 1 <= textView3.getTop() - textView2.getBottom() &&
                textView3.getTop() - textView2.getBottom() <= space + 1);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testJustifyContent_spaceBetween_flexDirection_column_withPadding()
            throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final int padding = 40;
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_justify_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setJustifyContent(JustifyContent.SPACE_BETWEEN);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                flexboxLayout.setPadding(padding, padding, padding, padding);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(),
                is(JustifyContent.SPACE_BETWEEN));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int space = flexboxLayout.getHeight() - textView1.getHeight() - textView2.getHeight() -
                textView3.getHeight() - padding * 2;
        space = space / 2;
        assertThat(textView1.getTop(), is(padding));
        assertThat(flexboxLayout.getBottom() - textView3.getBottom(), is(padding));
        assertTrue(space - 1 <= textView2.getTop() - textView1.getBottom() &&
                textView2.getTop() - textView1.getBottom() <= space + 1);
        assertTrue(space - 1 <= textView3.getTop() - textView2.getBottom() &&
                textView3.getTop() - textView2.getBottom() <= space + 1);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testJustifyContent_spaceAround_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_justify_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setJustifyContent(JustifyContent.SPACE_AROUND);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(),
                is(JustifyContent.SPACE_AROUND));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        float space = flexboxLayout.getHeight() - textView1.getHeight() - textView2.getHeight() -
                textView3.getHeight();
        space = space / 6; // Divide by the number of children * 2
        assertTrue(space - 1 <= textView1.getTop() && textView1.getTop() <= space + 1);
        float spaceLowerBound = space * 2 - 1;
        float spaceUpperBound = space * 2 + 1;
        assertTrue(spaceLowerBound <= textView2.getTop() - textView1.getBottom() &&
                textView2.getTop() - textView1.getBottom() <= spaceUpperBound);
        assertTrue(spaceLowerBound <= textView3.getTop() - textView2.getBottom() &&
                textView3.getTop() - textView2.getBottom() <= spaceUpperBound);
        assertTrue(space - 1 <= flexboxLayout.getBottom() - textView3.getBottom() &&
                flexboxLayout.getBottom() - textView3.getBottom() <= space + 1);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testJustifyContent_spaceAround_flexDirection_column_withPadding() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final int padding = 40;
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_justify_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setJustifyContent(JustifyContent.SPACE_AROUND);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                flexboxLayout.setPadding(padding, padding, padding, padding);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(),
                is(JustifyContent.SPACE_AROUND));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        float space = flexboxLayout.getHeight() - textView1.getHeight() - textView2.getHeight() -
                textView3.getHeight() - padding * 2;
        space = space / 6; // Divide by the number of children * 2
        assertTrue(space - 1 <= textView1.getTop() - padding
                && textView1.getTop() - padding <= space + 1);
        float spaceLowerBound = space * 2 - 1;
        float spaceUpperBound = space * 2 + 1;
        assertTrue(spaceLowerBound <= textView2.getTop() - textView1.getBottom() &&
                textView2.getTop() - textView1.getBottom() <= spaceUpperBound);
        assertTrue(spaceLowerBound <= textView3.getTop() - textView2.getBottom() &&
                textView3.getTop() - textView2.getBottom() <= spaceUpperBound);
        assertTrue(space - 1 <= flexboxLayout.getBottom() - textView3.getBottom() - padding &&
                flexboxLayout.getBottom() - textView3.getBottom() - padding <= space + 1);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexGrow_withExactParentLength() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flex_grow_test);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        // the third TextView is expanded to the right edge of the FlexboxLayout
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(textView3.getWidth(),
                is(flexboxLayout.getWidth() - textView1.getWidth() - textView2.getWidth()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexGrow_withExactParentLength_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flex_grow_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        // the third TextView is expanded to the bottom edge of the FlexboxLayout
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(textView3.getHeight(),
                is(flexboxLayout.getHeight() - textView1.getHeight() - textView2.getHeight()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexGrow_including_view_gone() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flex_grow_test);
                TextView textView2 = (TextView) activity.findViewById(R.id.text2);
                textView2.setVisibility(View.GONE);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        // the third TextView is expanded to the right edge of the FlexboxLayout
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text1)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(textView2.getVisibility(), is(View.GONE));
        assertThat(textView3.getWidth(), is(flexboxLayout.getWidth() - textView1.getWidth()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignContent_stretch() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_content_test);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.STRETCH));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        // the third TextView is wrapped to the next flex line
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));

        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int flexLineCrossSize = flexboxLayout.getHeight() / 2;
        // Two flex line's cross sizes are expanded to the half of the height of the FlexboxLayout.
        // The third textView's top should be aligned witht the second flex line.
        assertThat(textView3.getTop(), is(flexLineCrossSize));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignContent_flexStart() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignContent(AlignContent.FLEX_START);
            }
        });

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.FLEX_START));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        // the third TextView is wrapped to the next flex line
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(textView3.getTop(), is(textView1.getHeight()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignContent_flexEnd() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignContent(AlignContent.FLEX_END);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.FLEX_END));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isAbove(withId(R.id.text3)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isAbove(withId(R.id.text3)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(textView1.getBottom(), is(flexboxLayout.getBottom() - textView3.getHeight()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignContent_flexEnd_parentPadding() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignContent(AlignContent.FLEX_END);
                flexboxLayout.setPadding(32, 32, 32, 32);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.FLEX_END));
        onView(withId(R.id.text1)).check(isAbove(withId(R.id.text3)));
        onView(withId(R.id.text2)).check(isAbove(withId(R.id.text3)));

        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(textView3.getBottom(),
                is(flexboxLayout.getBottom() - flexboxLayout.getPaddingBottom()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignContent_flexEnd_parentPadding_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignContent(AlignContent.FLEX_END);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                flexboxLayout.setPadding(32, 32, 32, 32);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.FLEX_END));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isLeftOf(withId(R.id.text3)));
        onView(withId(R.id.text2)).check(isLeftOf(withId(R.id.text3)));

        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(textView3.getRight(),
                is(flexboxLayout.getRight() - flexboxLayout.getPaddingRight()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignContent_center() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignContent(AlignContent.CENTER);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.CENTER));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text1)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int spaceAboveAndBottom = flexboxLayout.getHeight() - textView1.getHeight() - textView3
                .getHeight();
        spaceAboveAndBottom /= 2;

        assertTrue(spaceAboveAndBottom - 1 <= textView1.getTop()
                && textView1.getTop() <= spaceAboveAndBottom + 1);
        assertTrue(flexboxLayout.getBottom() - spaceAboveAndBottom - 1 <= textView3.getBottom() &&
                textView3.getBottom() <= flexboxLayout.getBottom() - spaceAboveAndBottom + 1);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignContent_spaceBetween() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignContent(AlignContent.SPACE_BETWEEN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.SPACE_BETWEEN));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignContent_spaceBetween_withPadding() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignContent(AlignContent.SPACE_BETWEEN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.SPACE_BETWEEN));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignContent_spaceAround() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignContent(AlignContent.SPACE_AROUND);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.SPACE_AROUND));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);

        int spaceAround = flexboxLayout.getHeight() - textView1.getHeight() - textView3.getHeight();
        spaceAround /= 4; // Divide by the number of flex lines * 2

        assertTrue(spaceAround - 1 <= textView1.getTop() &&
                textView1.getTop() <= spaceAround + 1);
        int spaceLowerBound = textView1.getBottom() + spaceAround * 2 - 1;
        int spaceUpperBound = textView1.getBottom() + spaceAround * 2 + 1;
        assertTrue(spaceLowerBound <= textView3.getTop() && textView3.getTop() <= spaceUpperBound);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignContent_stretch_parentWrapContent() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                ViewGroup.LayoutParams parentLp = flexboxLayout.getLayoutParams();
                parentLp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                flexboxLayout.setLayoutParams(parentLp);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.STRETCH));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        // the third TextView is wrapped to the next flex line
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));

        // alignContent is only effective if the parent's height/width mode is MeasureSpec.EXACTLY.
        // The size of the flex lines don't change even if the alingContent is set to
        // ALIGN_CONTENT_STRETCH
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(textView3.getTop(), is(textView1.getHeight()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignContent_stretch_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.STRETCH));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        // the third TextView is wrapped to the next flex line
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));

        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int flexLineCrossSize = flexboxLayout.getWidth() / 2;
        // Two flex line's cross sizes are expanded to the half of the width of the FlexboxLayout.
        // The third textView's left should be aligned with the second flex line.
        assertThat(textView3.getLeft(), is(flexLineCrossSize));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignContent_flexStart_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignContent(AlignContent.FLEX_START);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.FLEX_START));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        // the third TextView is wrapped to the next flex line
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(textView3.getLeft(), is(textView1.getWidth()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignContent_flexEnd_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignContent(AlignContent.FLEX_END);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.FLEX_END));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftOf(withId(R.id.text3)));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text3)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(textView1.getRight(), is(flexboxLayout.getRight() - textView3.getWidth()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignContent_center_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignContent(AlignContent.CENTER);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.CENTER));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text1)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int spaceLeftAndRight = flexboxLayout.getWidth() - textView1.getWidth()
                - textView3.getWidth();
        spaceLeftAndRight /= 2;

        assertTrue(spaceLeftAndRight - 1 <= textView1.getLeft() &&
                textView1.getLeft() <= spaceLeftAndRight + 1);
        int spaceLowerBound = flexboxLayout.getRight() - spaceLeftAndRight - 1;
        int spaceUpperBound = flexboxLayout.getRight() - spaceLeftAndRight + 1;
        assertTrue(
                spaceLowerBound <= textView3.getRight() && textView3.getRight() <= spaceUpperBound);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignContent_spaceBetween_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignContent(AlignContent.SPACE_BETWEEN);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.SPACE_BETWEEN));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignContent_spaceAround_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignContent(AlignContent.SPACE_AROUND);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.SPACE_AROUND));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);

        int spaceAround = flexboxLayout.getWidth() - textView1.getWidth() - textView3.getWidth();
        spaceAround /= 4; // Divide by the number of flex lines * 2

        assertTrue(spaceAround - 1 <= textView1.getLeft() &&
                textView1.getLeft() <= spaceAround + 1);
        int spaceLowerBound = textView1.getRight() + spaceAround * 2 - 1;
        int spaceUpperBound = textView1.getRight() + spaceAround * 2 + 1;
        assertTrue(spaceLowerBound <= textView3.getLeft() &&
                textView3.getLeft() <= spaceUpperBound);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignContent_stretch_parentWrapContent_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                ViewGroup.LayoutParams parentLp = flexboxLayout.getLayoutParams();
                parentLp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                flexboxLayout.setLayoutParams(parentLp);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.STRETCH));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        // the third TextView is wrapped to the next flex line
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));

        // alignContent is only effective if the parent's height/width mode is MeasureSpec.EXACTLY.
        // The size of the flex lines don't change even if the alingContent is set to
        // ALIGN_CONTENT_STRETCH
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(textView3.getLeft(), is(textView1.getWidth()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignItems_stretch() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_stretch_test);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(AlignItems.STRETCH));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));

        // There should be 2 flex lines in the layout with the given layout.
        int flexLineSize = flexboxLayout.getHeight() / 2;
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertTrue(flexLineSize - 1 <= textView1.getHeight()
                && textView1.getHeight() <= flexLineSize + 1);
        assertTrue(flexLineSize - 1 <= textView2.getHeight() &&
                flexLineSize <= flexLineSize + 1);
        assertTrue(flexLineSize - 1 <= textView3.getHeight() &&
                textView3.getHeight() <= flexLineSize + 1);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignSelf_stretch() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_self_stretch_test);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));

        // There should be 2 flex lines in the layout with the given layout.
        // Only the first TextView's alignSelf is set to ALIGN_SELF_STRETCH
        int flexLineSize = flexboxLayout.getHeight() / 2;
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertTrue(flexLineSize - 1 <= textView1.getHeight() &&
                textView1.getHeight() <= flexLineSize + 1);
        assertThat(textView2.getHeight(), not(flexLineSize));
        assertThat(textView3.getHeight(), not(flexLineSize));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignSelf_stretch_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_self_stretch_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));

        // There should be 2 flex lines in the layout with the given layout.
        // Only the first TextView's alignSelf is set to ALIGN_SELF_STRETCH
        int flexLineSize = flexboxLayout.getWidth() / 2;
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertTrue(flexLineSize - 1 <= textView1.getWidth()
                && textView1.getWidth() <= flexLineSize + 1);
        assertThat(textView2.getWidth(), not(flexLineSize));
        assertThat(textView3.getWidth(), not(flexLineSize));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignItems_flexStart() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_items_test);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(AlignItems.FLEX_START));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));

        // There should be 2 flex lines in the layout with the given layout.
        int flexLineSize = flexboxLayout.getHeight() / 2;
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(textView1.getHeight(), not(flexLineSize));
        assertThat(textView2.getHeight(), not(flexLineSize));
        assertThat(textView3.getHeight(), not(flexLineSize));
        assertTrue(flexLineSize - 1 <= textView3.getTop() &&
                textView3.getTop() <= flexLineSize + 1);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignItems_flexEnd() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_items_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignItems(AlignItems.FLEX_END);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(AlignItems.FLEX_END));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));

        // There should be 2 flex lines in the layout with the given layout.
        int flexLineSize = flexboxLayout.getHeight() / 2;
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(textView1.getHeight(), not(flexLineSize));
        assertThat(textView2.getHeight(), not(flexLineSize));
        assertThat(textView3.getHeight(), not(flexLineSize));
        assertTrue(flexLineSize - 1 <= textView1.getBottom() &&
                textView1.getBottom() <= flexLineSize + 1);
        assertTrue(flexLineSize - 1 <= textView2.getBottom() &&
                textView2.getBottom() <= flexLineSize + 1);
        assertThat(textView3.getBottom(), is(flexboxLayout.getBottom()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignItems_flexEnd_parentPadding() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_items_parent_padding_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignItems(AlignItems.FLEX_END);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(AlignItems.FLEX_END));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        assertThat(textView1.getBottom(),
                is(flexboxLayout.getBottom() - flexboxLayout.getPaddingBottom()));
        assertThat(textView2.getBottom(),
                is(flexboxLayout.getBottom() - flexboxLayout.getPaddingBottom()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignItems_flexEnd_parentPadding_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_items_parent_padding_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignItems(AlignItems.FLEX_END);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(AlignItems.FLEX_END));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        assertThat(textView1.getRight(),
                is(flexboxLayout.getRight() - flexboxLayout.getPaddingRight()));
        assertThat(textView2.getRight(),
                is(flexboxLayout.getRight() - flexboxLayout.getPaddingRight()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignItems_center() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_items_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignItems(AlignItems.CENTER);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(AlignItems.CENTER));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));

        // There should be 2 flex lines in the layout with the given layout.
        int flexLineSize = flexboxLayout.getHeight() / 2;
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        // All TextView's heights are the same. No issues should be found if using the first
        // TextView to calculate the space above and below
        int spaceAboveAndBelow = (flexLineSize - textView1.getHeight()) / 2;
        assertThat(textView1.getHeight(), not(flexLineSize));
        assertThat(textView2.getHeight(), not(flexLineSize));
        assertThat(textView3.getHeight(), not(flexLineSize));
        assertTrue(spaceAboveAndBelow - 1 <= textView1.getTop() &&
                textView1.getTop() <= spaceAboveAndBelow + 1);
        assertTrue(spaceAboveAndBelow - 1 <= textView2.getTop() &&
                textView2.getTop() <= spaceAboveAndBelow + 1);
        assertTrue(flexLineSize + spaceAboveAndBelow - 1 <= textView3.getTop() &&
                textView3.getTop() <= flexLineSize + spaceAboveAndBelow + 1);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignItems_flexEnd_wrapReverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_items_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexWrap(FlexWrap.WRAP_REVERSE);
                flexboxLayout.setAlignItems(AlignItems.FLEX_END);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(AlignItems.FLEX_END));
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP_REVERSE));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));

        // There should be 2 flex lines in the layout with the given layout.
        int flexLineSize = flexboxLayout.getHeight() / 2;
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);

        assertThat(textView1.getHeight(), not(flexLineSize));
        assertThat(textView2.getHeight(), not(flexLineSize));
        assertThat(textView3.getHeight(), not(flexLineSize));
        int lowerBound = flexboxLayout.getHeight() - flexLineSize - 1;
        int upperBound = flexboxLayout.getHeight() - flexLineSize + 1;
        assertTrue(lowerBound <= textView1.getTop() && textView1.getTop() <= upperBound);
        assertTrue(lowerBound <= textView2.getTop() && textView2.getTop() <= upperBound);
        assertThat(textView3.getTop(), is(0));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignItems_center_wrapReverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_items_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexWrap(FlexWrap.WRAP_REVERSE);
                flexboxLayout.setAlignItems(AlignItems.CENTER);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(AlignItems.CENTER));
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP_REVERSE));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));

        // There should be 2 flex lines in the layout with the given layout.
        int flexLineSize = flexboxLayout.getHeight() / 2;
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);

        // All TextView's heights are the same. No issues should be found if using the first
        // TextView to calculate the space above and below
        int spaceAboveAndBelow = (flexLineSize - textView1.getHeight()) / 2;
        assertThat(textView1.getHeight(), not(flexLineSize));
        assertThat(textView2.getHeight(), not(flexLineSize));
        assertThat(textView3.getHeight(), not(flexLineSize));
        int lowerBound = flexboxLayout.getHeight() - spaceAboveAndBelow - 1;
        int upperBound = flexboxLayout.getHeight() - spaceAboveAndBelow + 1;
        assertTrue(lowerBound <= textView1.getBottom() && textView1.getBottom() <= upperBound);
        assertTrue(lowerBound <= textView2.getBottom() && textView2.getBottom() <= upperBound);
        assertTrue(flexboxLayout.getHeight() - flexLineSize - spaceAboveAndBelow - 1 <=
                textView3.getBottom() &&
                textView3.getBottom()
                        <= flexboxLayout.getHeight() - flexLineSize - spaceAboveAndBelow + 1);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignItems_flexStart_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_items_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(AlignItems.FLEX_START));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));

        // There should be 2 flex lines in the layout with the given layout.
        int flexLineSize = flexboxLayout.getWidth() / 2;
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(textView1.getWidth(), not(flexLineSize));
        assertThat(textView2.getWidth(), not(flexLineSize));
        assertThat(textView3.getWidth(), not(flexLineSize));
        assertTrue(flexLineSize - 1 <= textView3.getLeft() &&
                textView3.getLeft() <= flexLineSize + 1);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignItems_flexEnd_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_items_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignItems(AlignItems.FLEX_END);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(AlignItems.FLEX_END));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));

        // There should be 2 flex lines in the layout with the given layout.
        int flexLineSize = flexboxLayout.getHeight() / 2;
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(textView1.getWidth(), not(flexLineSize));
        assertThat(textView2.getWidth(), not(flexLineSize));
        assertThat(textView3.getWidth(), not(flexLineSize));
        assertTrue(flexLineSize - 1 <= textView1.getRight()
                && textView1.getRight() <= flexLineSize + 1);
        assertTrue(flexLineSize - 1 <= textView2.getRight()
                && textView2.getRight() <= flexLineSize + 1);
        assertThat(textView3.getRight(), is(flexboxLayout.getRight()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignItems_center_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_items_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignItems(AlignItems.CENTER);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(AlignItems.CENTER));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));

        // There should be 2 flex lines in the layout with the given layout.
        int flexLineSize = flexboxLayout.getWidth() / 2;
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        // All TextView's widths are the same. No issues should be found if using the first
        // TextView to calculate the space left and right
        int spaceLeftAndRight = (flexLineSize - textView1.getWidth()) / 2;
        assertThat(textView1.getWidth(), not(flexLineSize));
        assertThat(textView2.getWidth(), not(flexLineSize));
        assertThat(textView3.getWidth(), not(flexLineSize));
        assertTrue(spaceLeftAndRight - 1 <= textView1.getLeft() &&
                textView1.getLeft() <= spaceLeftAndRight + 1);
        assertTrue(spaceLeftAndRight - 1 <= textView2.getLeft() &&
                textView2.getLeft() <= spaceLeftAndRight + 1);
        assertTrue(flexLineSize + spaceLeftAndRight - 1 <= textView3.getLeft() &&
                textView2.getLeft() <= flexLineSize + spaceLeftAndRight + 1);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignItems_flexEnd_wrapReverse_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_items_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexWrap(FlexWrap.WRAP_REVERSE);
                flexboxLayout.setAlignItems(AlignItems.FLEX_END);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(AlignItems.FLEX_END));
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP_REVERSE));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));

        // There should be 2 flex lines in the layout with the given layout.
        int flexLineSize = flexboxLayout.getWidth() / 2;
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);

        assertThat(textView1.getWidth(), not(flexLineSize));
        assertThat(textView2.getWidth(), not(flexLineSize));
        assertThat(textView3.getWidth(), not(flexLineSize));
        int lowerBound = flexboxLayout.getWidth() - flexLineSize - 1;
        int upperBound = flexboxLayout.getWidth() - flexLineSize + 1;
        assertTrue(lowerBound <= textView1.getLeft() && textView1.getLeft() <= upperBound);
        assertTrue(lowerBound <= textView2.getLeft() && textView2.getLeft() <= upperBound);
        assertThat(textView3.getLeft(), is(0));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignItems_center_wrapReverse_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_items_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexWrap(FlexWrap.WRAP_REVERSE);
                flexboxLayout.setAlignItems(AlignItems.CENTER);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(AlignItems.CENTER));
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP_REVERSE));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));

        // There should be 2 flex lines in the layout with the given layout.
        int flexLineSize = flexboxLayout.getWidth() / 2;
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);

        // All TextView's widths are the same. No issues should be found if using the first
        // TextView to calculate the space above and below
        int spaceLeftAndRight = (flexLineSize - textView1.getWidth()) / 2;
        assertThat(textView1.getWidth(), not(flexLineSize));
        assertThat(textView2.getWidth(), not(flexLineSize));
        assertThat(textView3.getWidth(), not(flexLineSize));
        int lowerBound = flexboxLayout.getWidth() - spaceLeftAndRight - 1;
        int upperBound = flexboxLayout.getWidth() - spaceLeftAndRight + 1;
        assertTrue(lowerBound <= textView1.getRight() && textView1.getRight() <= upperBound);
        assertTrue(lowerBound <= textView2.getRight() && textView2.getRight() <= upperBound);
        assertTrue(flexboxLayout.getWidth() - flexLineSize - spaceLeftAndRight - 1 <=
                textView3.getRight() &&
                textView3.getRight()
                        <= flexboxLayout.getWidth() - flexLineSize - spaceLeftAndRight + 1);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignItems_baseline() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_items_baseline_test);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int topPluBaseline1 = textView1.getTop() + textView1.getBaseline();
        int topPluBaseline2 = textView2.getTop() + textView2.getBaseline();
        int topPluBaseline3 = textView3.getTop() + textView3.getBaseline();

        assertThat(topPluBaseline1, is(topPluBaseline2));
        assertThat(topPluBaseline2, is(topPluBaseline3));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testAlignItems_baseline_wrapReverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_items_baseline_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexWrap(FlexWrap.WRAP_REVERSE);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int bottomPluBaseline1 = textView1.getBottom() + textView1.getBaseline();
        int bottomPluBaseline2 = textView2.getBottom() + textView2.getBaseline();
        int bottomPluBaseline3 = textView3.getBottom() + textView3.getBaseline();

        assertThat(bottomPluBaseline1, is(bottomPluBaseline2));
        assertThat(bottomPluBaseline2, is(bottomPluBaseline3));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexDirection_row_reverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flex_wrap_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexDirection(FlexDirection.ROW_REVERSE);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW_REVERSE));
        // The layout direction should be right to left
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexDirection_column_reverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flex_wrap_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN_REVERSE);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getFlexDirection(),
                is(FlexDirection.COLUMN_REVERSE));
        onView(withId(R.id.text1)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isAbove(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexBasisPercent_wrap() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flex_basis_percent_test);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        // The text1 length is 50%, the text2 length is 60% and the wrap property is WRAP,
        // the text2 should be on the second flex line.
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        FlexboxLayout.LayoutParams lp1 = (FlexboxLayout.LayoutParams) textView1.getLayoutParams();
        FlexboxLayout.LayoutParams lp2 = (FlexboxLayout.LayoutParams) textView2.getLayoutParams();
        assertThat(textView1.getWidth(),
                is(Math.round(flexboxLayout.getWidth() * lp1.flexBasisPercent)));
        assertThat(textView2.getWidth(),
                is(Math.round(flexboxLayout.getWidth() * lp2.flexBasisPercent)));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexBasisPercent_nowrap() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flex_basis_percent_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexWrap(FlexWrap.NOWRAP);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        // The text1 length is 50%, the text2 length is 60% and the text3 has the fixed width,
        // but the flex wrap attribute is NOWRAP, and flexShrink attributes for all
        // children are the default value (1), three text views are shrank to fit in a single flex
        // line.
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.NOWRAP));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int totalWidth = textView1.getWidth() + textView2.getWidth() + textView3.getWidth();
        // Allowing minor different length with the flex container since the sum of the three text
        // views width is not always the same as the flex container's main size caused by round
        // errors in calculating the percent lengths.
        assertTrue(totalWidth >= flexboxLayout.getWidth() - 3 ||
                totalWidth <= flexboxLayout.getWidth() + 3);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexBasisPercent_wrap_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flex_basis_percent_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        // The text1 length is 50%, the text2 length is 60% and the wrap property is WRAP,
        // the text2 should be on the second flex line.
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        FlexboxLayout.LayoutParams lp1 = (FlexboxLayout.LayoutParams) textView1.getLayoutParams();
        FlexboxLayout.LayoutParams lp2 = (FlexboxLayout.LayoutParams) textView2.getLayoutParams();
        assertThat(textView1.getHeight(),
                is(Math.round(flexboxLayout.getHeight() * lp1.flexBasisPercent)));
        assertThat(textView2.getHeight(),
                is(Math.round(flexboxLayout.getHeight() * lp2.flexBasisPercent)));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFlexBasisPercent_nowrap_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flex_basis_percent_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexWrap(FlexWrap.NOWRAP);
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        // The text1 length is 50%, the text2 length is 60% and the text3 has the fixed height,
        // but the flex wrap attribute is NOWRAP, and flexShrink attributes for all
        // children are the default value (1), three text views are shrank to fit in a single
        // flex line.
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.NOWRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int totalHeight = textView1.getHeight() + textView2.getHeight() + textView3.getHeight();
        // Allowing minor different length with the flex container since the sum of the three text
        // views width is not always the same as the flex container's main size caused by round
        // errors in calculating the percent lengths.
        assertTrue(totalHeight >= flexboxLayout.getHeight() - 3 ||
                totalHeight <= flexboxLayout.getHeight() + 3);
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testMinWidth_initial_width_less_than_minWidth() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_minwidth_test);
            }
        });

        // This test case verifies if the minWidth attribute works as a minimum constraint
        // If the initial view width is less than the value of minWidth.
        // The textView1's layout_width is set to wrap_content and its text is "1" apparently
        // the initial measured width is less than the value of layout_minWidth (100dp)
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        int minWidth = ((FlexboxLayout.LayoutParams) textView1.getLayoutParams()).minWidth;

        onView(withId(R.id.text1)).check(hasWidth(minWidth));
        onView(withId(R.id.text2)).check(hasWidth(flexboxLayout.getWidth() - minWidth));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testMinWidth_works_as_lower_bound_shrink_to() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_minwidth_lower_bound_test);
            }
        });

        // This test case verifies if the minWidth attribute works as a lower bound
        // when the view would shrink less than the minWidth if the minWidth weren't set
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        TextView textView4 = (TextView) activity.findViewById(R.id.text4);
        int minWidth = ((FlexboxLayout.LayoutParams) textView1.getLayoutParams()).minWidth;

        onView(withId(R.id.text1)).check(hasWidth(minWidth));
        assertEquals(flexboxLayout.getWidth(),
                textView1.getWidth() + textView2.getWidth() + textView3.getWidth() + textView4
                        .getWidth());
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testMinHeight_initial_height_less_than_minHeight() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_minheight_test);
            }
        });

        // This test case verifies if the minHeight attribute works as a minimum constraint
        // If the initial view height is less than the value of minHeight.
        // The textView1's layout_height is set to wrap_content and its text is "1" apparently
        // the initial measured height is less than the value of layout_minHeight (100dp)
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        int minHeight = ((FlexboxLayout.LayoutParams) textView1.getLayoutParams()).minHeight;

        onView(withId(R.id.text1)).check(hasHeight(minHeight));
        onView(withId(R.id.text2)).check(hasHeight(flexboxLayout.getHeight() - minHeight));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testMinHeight_works_as_lower_bound_shrink_to() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_minheight_lower_bound_test);
            }
        });

        // This test case verifies if the minHeight attribute works as a lower bound
        // when the view would shrink less than the minHeight if the minHeight weren't set
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        TextView textView4 = (TextView) activity.findViewById(R.id.text4);
        int minHeight = ((FlexboxLayout.LayoutParams) textView1.getLayoutParams()).minHeight;

        onView(withId(R.id.text1)).check(hasHeight(minHeight));
        assertEquals(flexboxLayout.getHeight(),
                textView1.getHeight() + textView2.getHeight() + textView3.getHeight()
                        + textView4.getHeight());
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testMaxWidth_initial_width_more_than_maxWidth() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_maxwidth_test);
            }
        });

        // This test case verifies if the maxWidth attribute works as a maximum constraint
        // ff the initial view width is more than the value of maxWidth.
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        int maxWidth = ((FlexboxLayout.LayoutParams) textView1.getLayoutParams()).maxWidth;

        onView(withId(R.id.text1)).check(hasWidth(maxWidth));
        onView(withId(R.id.text2)).check(hasWidth(flexboxLayout.getWidth() - maxWidth));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testMaxWidth_works_as_upper_bound_expand_to() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_maxwidth_upper_bound_test);
            }
        });

        // This test case verifies if the maxWidth attribute works as a upper bound
        // when the view would expand more than the maxWidth if the maxWidth weren't set
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        int maxWidth = ((FlexboxLayout.LayoutParams) textView1.getLayoutParams()).maxWidth;

        onView(withId(R.id.text1)).check(hasWidth(maxWidth));
        assertEquals(flexboxLayout.getWidth(), textView1.getWidth() + textView2.getWidth());
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testMaxHeight_initial_height_more_than_maxHeight() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_maxheight_test);
            }
        });

        // This test case verifies if the maxHeight attribute works as a maximum constraint
        // ff the initial view height is more than the value of maxHeight.
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        int maxHeight = ((FlexboxLayout.LayoutParams) textView1.getLayoutParams()).maxHeight;

        onView(withId(R.id.text1)).check(hasHeight(maxHeight));
        onView(withId(R.id.text2)).check(hasHeight(flexboxLayout.getHeight() - maxHeight));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testMaxHeight_works_as_lower_bound_expand_to() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_maxheight_upper_bound_test);
            }
        });

        // This test case verifies if the maxHeight attribute works as a upper bound
        // when the view would expand more than the maxHeight if the maxHeight weren't set
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        int maxHeight = ((FlexboxLayout.LayoutParams) textView1.getLayoutParams()).maxHeight;

        onView(withId(R.id.text1)).check(hasHeight(maxHeight));
        assertEquals(flexboxLayout.getHeight(), textView1.getHeight() + textView2.getHeight());
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testView_visibility_gone() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_views_visibility_gone);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        // The text1 and text2's visibility are gone, so the visible view starts from text3
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text4)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text4)).check(isRightOf(withId(R.id.text3)));
        onView(withId(R.id.text5)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text5)).check(isBelow(withId(R.id.text3)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        TextView textView4 = (TextView) activity.findViewById(R.id.text4);
        TextView textView5 = (TextView) activity.findViewById(R.id.text5);
        assertThat(textView1.getVisibility(), is(View.GONE));
        assertThat(textView2.getVisibility(), is(View.GONE));
        assertThat(textView4.getLeft(), is(textView3.getRight()));
        assertThat(textView5.getTop(), is(textView3.getBottom()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testView_visibility_gone_first_item_in_flex_line_horizontal() throws Throwable {
        // This test verifies if the FlexboxLayout is visible when the visibility of the first
        // flex item in the second flex line (or arbitrary flex lines other than the first flex line)
        // is set to "gone"
        // There was an issue reported for that
        // https://github.com/google/flexbox-layout/issues/47
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(
                        R.layout.activity_visibility_gone_first_item_in_flex_line_row);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);

        assertTrue(flexboxLayout.getHeight() > 0);
        assertThat(flexboxLayout.getHeight(), is(textView1.getHeight() + textView3.getHeight()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testView_visibility_gone_first_item_in_flex_line_vertical() throws Throwable {
        // This test verifies if the FlexboxLayout is visible when the visibility of the first
        // flex item in the second flex line (or arbitrary flex lines other than the first flex line)
        // is set to "gone"
        // There was an issue reported for that
        // https://github.com/google/flexbox-layout/issues/47
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(
                        R.layout.activity_visibility_gone_first_item_in_flex_line_column);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);

        assertTrue(flexboxLayout.getWidth() > 0);
        assertThat(flexboxLayout.getWidth(), is(textView1.getWidth() + textView3.getWidth()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testView_visibility_invisible() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_views_visibility_invisible);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        // The text1 and text2's visibility are invisible, these views take space like visible views
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text1)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(textView1.getVisibility(), is(View.INVISIBLE));
        assertThat(textView2.getVisibility(), is(View.INVISIBLE));
        assertThat(textView3.getTop(), is(textView1.getBottom()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testWrapBefore() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_wrap_before_test);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        // layout_wrapBefore for the text2 and text3 are set to true, the text2 and text3 should
        // be the first item for each flex line.
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        final TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(flexboxLayout.getHeight(), is(textView1.getHeight() + textView2.getHeight() +
                textView3.getHeight()));

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FlexboxLayout.LayoutParams lp2 = (FlexboxLayout.LayoutParams)
                        textView2.getLayoutParams();
                lp2.wrapBefore = false;
                textView2.setLayoutParams(lp2);
            }
        });

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));
        assertThat(flexboxLayout.getHeight(), is(textView1.getHeight() + textView3.getHeight()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testWrapBefore_nowrap() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_wrap_before_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setFlexWrap(FlexWrap.NOWRAP);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        // layout_wrapBefore for the text2 and text3 are set to true, but the flexWrap is set to
        // NOWRAP, three text views should not be wrapped.
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.NOWRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testWrap_parentPadding_horizontal() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_wrap_parent_padding_horizontal_test);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));
        // The sum of width of TextView1 and TextView2 is not enough for wrapping, but considering
        // parent padding, the second TextView should be wrapped
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        assertThat(flexboxLayout.getHeight(),
                is(flexboxLayout.getPaddingTop() + flexboxLayout.getPaddingBottom() +
                        text1.getHeight() + text2.getHeight()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testWrap_parentPadding_vertical() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_wrap_parent_padding_vertical_test);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        // The sum of height of TextView1 and TextView2 is not enough for wrapping, but considering
        // parent padding, the second TextView should be wrapped
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        assertThat(flexboxLayout.getWidth(),
                is(flexboxLayout.getPaddingLeft() + flexboxLayout.getPaddingRight() +
                        text1.getWidth() + text2.getWidth()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testWrap_childMargin_horizontal() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_wrap_child_margin_horizontal_test);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));
        // The sum of width of TextView1 and TextView2 is not enough for wrapping, but considering
        // the margin for the TextView2, the second TextView should be wrapped
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        FlexboxLayout.LayoutParams lp2 = (FlexboxLayout.LayoutParams) text2.getLayoutParams();
        assertThat(flexboxLayout.getHeight(),
                is(text1.getHeight() + text2.getHeight() + lp2.topMargin + lp2.bottomMargin));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFirstItemLarge_horizontal() throws Throwable {
        // This test verifies a empty flex line is not added when the first flex item is large
        // and judged wrapping is required with the first item.
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_first_item_large_horizontal_test);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));
        assertThat(flexboxLayout.getAlignItems(), is(AlignItems.STRETCH));
        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.STRETCH));
        // The sum of width of TextView1 and TextView2 is not enough for wrapping, but considering
        // the margin for the TextView2, the second TextView should be wrapped
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        TextView text3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(flexboxLayout.getHeight(),
                is(text1.getHeight() + text2.getHeight() + text3.getHeight()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testFirstItemLarge_vertical() throws Throwable {
        // This test verifies a empty flex line is not added when the first flex item is large
        // and judged wrapping is required with the first item.
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_first_item_large_vertical_test);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(flexboxLayout.getAlignItems(), is(AlignItems.STRETCH));
        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.STRETCH));
        // The sum of width of TextView1 and TextView2 is not enough for wrapping, but considering
        // the margin for the TextView2, the second TextView should be wrapped
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        TextView text3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(flexboxLayout.getWidth(),
                is(text1.getWidth() + text2.getWidth() + text3.getWidth()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testWrap_childMargin_vertical() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_wrap_child_margin_vertical_test);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        // The sum of height of TextView1 and TextView2 is not enough for wrapping, but considering
        // the margin of the TextView2, the second TextView should be wrapped
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        FlexboxLayout.LayoutParams lp2 = (FlexboxLayout.LayoutParams) text2.getLayoutParams();
        assertThat(flexboxLayout.getWidth(),
                is(text1.getWidth() + text2.getWidth() + lp2.leftMargin + lp2.rightMargin));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testEmptyChildren() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_empty_children);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getChildCount(), is(0));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testDivider_directionRow_verticalBeginning() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_divider_test_direction_row);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));
        assertThat(flexboxLayout.getShowDividerVertical(),
                is(FlexboxLayout.SHOW_DIVIDER_BEGINNING));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        TextView text3 = (TextView) activity.findViewById(R.id.text3);
        Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider, null);
        assertNotNull(divider);
        int widthSumFirstRow = text1.getWidth() + text2.getWidth() + text3.getWidth() + divider
                .getIntrinsicWidth();
        assertThat(text3.getRight(), is(widthSumFirstRow));
        assertThat(text1.getLeft(), is(not(flexboxLayout.getLeft())));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testDivider_directionRow_verticalMiddle() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_divider_test_direction_row);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setShowDividerVertical(FlexboxLayout.SHOW_DIVIDER_MIDDLE);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));
        assertThat(flexboxLayout.getShowDividerVertical(), is(FlexboxLayout.SHOW_DIVIDER_MIDDLE));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        TextView text3 = (TextView) activity.findViewById(R.id.text3);
        Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider, null);
        assertNotNull(divider);
        // Three text views are placed in the first row, thus two vertical middle dividers should
        // be placed
        int widthSumFirstRow = text1.getWidth() + text2.getWidth() + text3.getWidth()
                + divider.getIntrinsicWidth() * 2;
        assertThat(text3.getRight(), is(widthSumFirstRow));
        assertThat(text1.getLeft(), is(flexboxLayout.getLeft()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testDivider_directionRow_verticalEnd() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_divider_test_direction_row);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setShowDividerVertical(FlexboxLayout.SHOW_DIVIDER_END);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));
        assertThat(flexboxLayout.getShowDividerVertical(), is(FlexboxLayout.SHOW_DIVIDER_END));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        TextView text3 = (TextView) activity.findViewById(R.id.text3);
        Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider, null);
        assertNotNull(divider);
        // Three text views are placed in the first row, thus two vertical middle dividers should
        // be placed
        int widthSumFirstRow = text1.getWidth() + text2.getWidth() + text3.getWidth()
                + divider.getIntrinsicWidth();
        assertThat(text3.getRight() + divider.getIntrinsicWidth(), is(widthSumFirstRow));
        assertThat(text1.getLeft(), is(flexboxLayout.getLeft()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testDivider_directionRow_verticalAll() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_divider_test_direction_row);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setShowDividerVertical(
                        FlexboxLayout.SHOW_DIVIDER_BEGINNING | FlexboxLayout.SHOW_DIVIDER_MIDDLE
                                | FlexboxLayout.SHOW_DIVIDER_END);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));
        assertThat(flexboxLayout.getShowDividerVertical(),
                is(FlexboxLayout.SHOW_DIVIDER_BEGINNING | FlexboxLayout.SHOW_DIVIDER_MIDDLE |
                        FlexboxLayout.SHOW_DIVIDER_END));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        TextView text3 = (TextView) activity.findViewById(R.id.text3);
        Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider, null);
        assertNotNull(divider);
        // Three text views are placed in the first row, thus two vertical middle dividers should
        // be placed
        int widthSumFirstRow = text1.getWidth() + text2.getWidth() + text3.getWidth()
                + divider.getIntrinsicWidth() * 4;
        assertThat(text3.getRight() + divider.getIntrinsicWidth(), is(widthSumFirstRow));
        assertThat(text1.getLeft(), is(not(flexboxLayout.getLeft())));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testDivider_directionRow_horizontalBeginning() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider, null);
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_divider_test_direction_row);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setDividerDrawableHorizontal(divider);
                flexboxLayout.setShowDividerHorizontal(FlexboxLayout.SHOW_DIVIDER_BEGINNING);
                flexboxLayout.setShowDividerVertical(FlexboxLayout.SHOW_DIVIDER_NONE);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));
        assertThat(flexboxLayout.getShowDividerHorizontal(),
                is(FlexboxLayout.SHOW_DIVIDER_BEGINNING));
        assertThat(flexboxLayout.getShowDividerVertical(), is(FlexboxLayout.SHOW_DIVIDER_NONE));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text4 = (TextView) activity.findViewById(R.id.text4);
        assertNotNull(divider);
        int heightSum = text1.getHeight() + text4.getHeight() + divider.getIntrinsicHeight();
        assertThat(text4.getBottom(), is(heightSum));
        assertThat(text1.getTop(), is(not(flexboxLayout.getTop())));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testDivider_directionRow_horizontalMiddle() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_divider_test_direction_row);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                Drawable divider = ResourcesCompat
                        .getDrawable(activity.getResources(), R.drawable.divider, null);
                flexboxLayout.setDividerDrawableHorizontal(divider);
                flexboxLayout.setShowDividerHorizontal(FlexboxLayout.SHOW_DIVIDER_MIDDLE);
                flexboxLayout.setShowDividerVertical(FlexboxLayout.SHOW_DIVIDER_NONE);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));
        assertThat(flexboxLayout.getShowDividerHorizontal(), is(FlexboxLayout.SHOW_DIVIDER_MIDDLE));
        assertThat(flexboxLayout.getShowDividerVertical(), is(FlexboxLayout.SHOW_DIVIDER_NONE));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text4 = (TextView) activity.findViewById(R.id.text4);
        Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider, null);
        assertNotNull(divider);
        int heightSum = text1.getHeight() + text4.getHeight() + divider.getIntrinsicHeight();
        assertThat(text4.getBottom(), is(heightSum));
        assertThat(text1.getTop(), is(flexboxLayout.getTop()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testDivider_directionRow_horizontalEnd() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_divider_test_direction_row);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                Drawable divider = ResourcesCompat
                        .getDrawable(activity.getResources(), R.drawable.divider, null);
                flexboxLayout.setDividerDrawableHorizontal(divider);
                flexboxLayout.setShowDividerHorizontal(FlexboxLayout.SHOW_DIVIDER_END);
                flexboxLayout.setShowDividerVertical(FlexboxLayout.SHOW_DIVIDER_NONE);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));
        assertThat(flexboxLayout.getShowDividerHorizontal(), is(FlexboxLayout.SHOW_DIVIDER_END));
        assertThat(flexboxLayout.getShowDividerVertical(), is(FlexboxLayout.SHOW_DIVIDER_NONE));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text4 = (TextView) activity.findViewById(R.id.text4);
        Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider, null);
        assertNotNull(divider);
        int heightSum = text1.getHeight() + text4.getHeight() + divider.getIntrinsicHeight();
        assertThat(text4.getBottom() + divider.getIntrinsicHeight(), is(heightSum));
        assertThat(text1.getTop(), is(flexboxLayout.getTop()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testDivider_directionRow_horizontalAll() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_divider_test_direction_row);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                Drawable divider = ResourcesCompat
                        .getDrawable(activity.getResources(), R.drawable.divider, null);
                flexboxLayout.setDividerDrawableHorizontal(divider);
                flexboxLayout.setShowDividerHorizontal(
                        FlexboxLayout.SHOW_DIVIDER_BEGINNING | FlexboxLayout.SHOW_DIVIDER_MIDDLE
                                | FlexboxLayout.SHOW_DIVIDER_END);
                flexboxLayout.setShowDividerVertical(FlexboxLayout.SHOW_DIVIDER_NONE);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));
        assertThat(flexboxLayout.getShowDividerHorizontal(),
                is(FlexboxLayout.SHOW_DIVIDER_BEGINNING | FlexboxLayout.SHOW_DIVIDER_MIDDLE |
                        FlexboxLayout.SHOW_DIVIDER_END));
        assertThat(flexboxLayout.getShowDividerVertical(), is(FlexboxLayout.SHOW_DIVIDER_NONE));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text4 = (TextView) activity.findViewById(R.id.text4);
        Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider, null);
        assertNotNull(divider);
        int heightSum = text1.getHeight() + text4.getHeight() + divider.getIntrinsicHeight() * 3;
        assertThat(text4.getBottom() + divider.getIntrinsicHeight(), is(heightSum));
        assertThat(text1.getTop(), is(not(flexboxLayout.getTop())));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testDivider_directionRow_all_thickDivider() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_divider_test_direction_row);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                Drawable thickDivider = ResourcesCompat
                        .getDrawable(activity.getResources(), R.drawable.divider_thick, null);
                flexboxLayout.setDividerDrawableVertical(thickDivider);
                flexboxLayout.setShowDividerVertical(
                        FlexboxLayout.SHOW_DIVIDER_BEGINNING | FlexboxLayout.SHOW_DIVIDER_MIDDLE
                                | FlexboxLayout.SHOW_DIVIDER_END);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));
        assertThat(flexboxLayout.getShowDividerVertical(),
                is(FlexboxLayout.SHOW_DIVIDER_BEGINNING | FlexboxLayout.SHOW_DIVIDER_MIDDLE |
                        FlexboxLayout.SHOW_DIVIDER_END));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        TextView text3 = (TextView) activity.findViewById(R.id.text3);
        Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider_thick, null);
        // The sum of three text views and the sum of thick dividers don't fit in one line.
        // The last text view should be placed to the next line.
        assertNotNull(divider);
        int widthSumFirstRow = text1.getWidth() + text2.getWidth()
                + divider.getIntrinsicWidth() * 3;
        assertThat(text2.getRight() + divider.getIntrinsicWidth(), is(widthSumFirstRow));
        assertThat(text1.getLeft(), is(not(flexboxLayout.getLeft())));
        assertThat(text3.getBottom(), is(text1.getHeight() + text2.getHeight()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testDivider_directionColumn_horizontalBeginning() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_divider_test_direction_column);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(flexboxLayout.getShowDividerHorizontal(),
                is(FlexboxLayout.SHOW_DIVIDER_BEGINNING));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        TextView text3 = (TextView) activity.findViewById(R.id.text3);
        Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider, null);
        assertNotNull(divider);
        int heightSumFirstRow = text1.getHeight() + text2.getHeight() + text3.getHeight() + divider
                .getIntrinsicHeight();
        assertThat(text3.getBottom(), is(heightSumFirstRow));
        assertThat(text1.getTop(), is(not(flexboxLayout.getTop())));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testDivider_directionColumn_horizontalMiddle() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_divider_test_direction_column);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setShowDividerHorizontal(FlexboxLayout.SHOW_DIVIDER_MIDDLE);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(flexboxLayout.getShowDividerHorizontal(), is(FlexboxLayout.SHOW_DIVIDER_MIDDLE));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        TextView text3 = (TextView) activity.findViewById(R.id.text3);
        Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider, null);
        assertNotNull(divider);
        int heightSumFirstRow = text1.getHeight() + text2.getHeight() + text3.getHeight() + divider
                .getIntrinsicHeight() * 2;
        assertThat(text3.getBottom(), is(heightSumFirstRow));
        assertThat(text1.getTop(), is(flexboxLayout.getTop()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testDivider_directionColumn_horizontalEnd() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_divider_test_direction_column);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setShowDividerHorizontal(FlexboxLayout.SHOW_DIVIDER_END);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(flexboxLayout.getShowDividerHorizontal(), is(FlexboxLayout.SHOW_DIVIDER_END));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        TextView text3 = (TextView) activity.findViewById(R.id.text3);
        Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider, null);
        assertNotNull(divider);
        int heightSumFirstRow = text1.getHeight() + text2.getHeight() + text3.getHeight() + divider
                .getIntrinsicHeight();
        assertThat(text3.getBottom() + divider.getIntrinsicHeight(), is(heightSumFirstRow));
        assertThat(text1.getTop(), is(flexboxLayout.getTop()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testDivider_directionColumn_horizontalAll() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_divider_test_direction_column);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setShowDividerHorizontal(
                        FlexboxLayout.SHOW_DIVIDER_BEGINNING | FlexboxLayout.SHOW_DIVIDER_MIDDLE
                                | FlexboxLayout.SHOW_DIVIDER_END);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(flexboxLayout.getShowDividerHorizontal(),
                is(FlexboxLayout.SHOW_DIVIDER_BEGINNING | FlexboxLayout.SHOW_DIVIDER_MIDDLE |
                        FlexboxLayout.SHOW_DIVIDER_END));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        TextView text3 = (TextView) activity.findViewById(R.id.text3);
        Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider, null);
        assertNotNull(divider);
        int heightSumFirstRow = text1.getHeight() + text2.getHeight() + text3.getHeight() + divider
                .getIntrinsicHeight() * 4;
        assertThat(text3.getBottom() + divider.getIntrinsicHeight(), is(heightSumFirstRow));
        assertThat(text1.getTop(), is(not(flexboxLayout.getTop())));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testDivider_directionColumn_verticalBeginning() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_divider_test_direction_column);
                Drawable divider = ResourcesCompat
                        .getDrawable(activity.getResources(), R.drawable.divider, null);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setDividerDrawableVertical(divider);
                flexboxLayout.setShowDividerVertical(FlexboxLayout.SHOW_DIVIDER_BEGINNING);
                flexboxLayout.setShowDividerHorizontal(FlexboxLayout.SHOW_DIVIDER_NONE);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(flexboxLayout.getShowDividerVertical(),
                is(FlexboxLayout.SHOW_DIVIDER_BEGINNING));
        assertThat(flexboxLayout.getShowDividerHorizontal(), is(FlexboxLayout.SHOW_DIVIDER_NONE));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text4 = (TextView) activity.findViewById(R.id.text4);
        Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider, null);
        assertNotNull(divider);
        int widthSum = text1.getWidth() + text4.getWidth() + divider.getIntrinsicWidth();
        assertThat(text4.getRight(), is(widthSum));
        assertThat(text1.getLeft(), is(not(flexboxLayout.getLeft())));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testDivider_directionColumn_verticalMiddle() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_divider_test_direction_column);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                Drawable divider = ResourcesCompat
                        .getDrawable(activity.getResources(), R.drawable.divider, null);
                flexboxLayout.setDividerDrawableVertical(divider);
                flexboxLayout.setShowDividerVertical(FlexboxLayout.SHOW_DIVIDER_MIDDLE);
                flexboxLayout.setShowDividerHorizontal(FlexboxLayout.SHOW_DIVIDER_NONE);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(flexboxLayout.getShowDividerVertical(), is(FlexboxLayout.SHOW_DIVIDER_MIDDLE));
        assertThat(flexboxLayout.getShowDividerHorizontal(), is(FlexboxLayout.SHOW_DIVIDER_NONE));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text4 = (TextView) activity.findViewById(R.id.text4);
        Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider, null);
        assertNotNull(divider);
        int widthSum = text1.getWidth() + text4.getWidth() + divider.getIntrinsicWidth();
        assertThat(text4.getRight(), is(widthSum));
        assertThat(text1.getLeft(), is(flexboxLayout.getLeft()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testDivider_directionColumn_verticalEnd() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_divider_test_direction_column);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                Drawable divider = ResourcesCompat
                        .getDrawable(activity.getResources(), R.drawable.divider, null);
                flexboxLayout.setDividerDrawableHorizontal(divider);
                flexboxLayout.setShowDividerVertical(FlexboxLayout.SHOW_DIVIDER_END);
                flexboxLayout.setShowDividerHorizontal(FlexboxLayout.SHOW_DIVIDER_NONE);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(flexboxLayout.getShowDividerVertical(), is(FlexboxLayout.SHOW_DIVIDER_END));
        assertThat(flexboxLayout.getShowDividerHorizontal(), is(FlexboxLayout.SHOW_DIVIDER_NONE));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text4 = (TextView) activity.findViewById(R.id.text4);
        Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider, null);
        assertNotNull(divider);
        int widthSum = text1.getWidth() + text4.getWidth() + divider.getIntrinsicWidth();
        assertThat(text4.getRight() + divider.getIntrinsicWidth(), is(widthSum));
        assertThat(text1.getLeft(), is(flexboxLayout.getLeft()));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testDivider_directionColumn_verticalAll() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_divider_test_direction_column);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                Drawable divider = ResourcesCompat
                        .getDrawable(activity.getResources(), R.drawable.divider, null);
                flexboxLayout.setDividerDrawableVertical(divider);
                flexboxLayout.setShowDividerVertical(
                        FlexboxLayout.SHOW_DIVIDER_BEGINNING | FlexboxLayout.SHOW_DIVIDER_MIDDLE
                                | FlexboxLayout.SHOW_DIVIDER_END);
                flexboxLayout.setShowDividerHorizontal(FlexboxLayout.SHOW_DIVIDER_NONE);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(flexboxLayout.getShowDividerVertical(),
                is(FlexboxLayout.SHOW_DIVIDER_BEGINNING | FlexboxLayout.SHOW_DIVIDER_MIDDLE |
                        FlexboxLayout.SHOW_DIVIDER_END));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text4 = (TextView) activity.findViewById(R.id.text4);
        Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider, null);
        assertNotNull(divider);
        int widthSum = text1.getWidth() + text4.getWidth() + divider.getIntrinsicWidth() * 3;
        assertThat(text4.getRight() + divider.getIntrinsicWidth(), is(widthSum));
        assertThat(text1.getLeft(), is(not(flexboxLayout.getLeft())));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testDivider_directionColumn_vertical_horizontal_All() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_divider_test_direction_column);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                Drawable divider = ResourcesCompat
                        .getDrawable(activity.getResources(), R.drawable.divider, null);
                flexboxLayout.setDividerDrawable(divider);
                flexboxLayout.setShowDivider(
                        FlexboxLayout.SHOW_DIVIDER_BEGINNING | FlexboxLayout.SHOW_DIVIDER_MIDDLE
                                | FlexboxLayout.SHOW_DIVIDER_END);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(flexboxLayout.getShowDividerVertical(),
                is(FlexboxLayout.SHOW_DIVIDER_BEGINNING | FlexboxLayout.SHOW_DIVIDER_MIDDLE |
                        FlexboxLayout.SHOW_DIVIDER_END));
        assertThat(flexboxLayout.getShowDividerHorizontal(),
                is(FlexboxLayout.SHOW_DIVIDER_BEGINNING | FlexboxLayout.SHOW_DIVIDER_MIDDLE |
                        FlexboxLayout.SHOW_DIVIDER_END));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        TextView text3 = (TextView) activity.findViewById(R.id.text3);
        TextView text4 = (TextView) activity.findViewById(R.id.text4);
        Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider, null);
        assertNotNull(divider);
        int heightSum = text1.getHeight() + text2.getHeight() + text3.getHeight()
                + divider.getIntrinsicHeight() * 4;
        int widthSum = text1.getWidth() + text4.getWidth() + divider.getIntrinsicWidth() * 3;
        assertThat(text3.getBottom() + divider.getIntrinsicHeight(), is(heightSum));
        assertThat(text4.getRight() + divider.getIntrinsicWidth(), is(widthSum));
        assertThat(text1.getLeft(), is(not(flexboxLayout.getLeft())));
        assertThat(text1.getTop(), is(not(flexboxLayout.getTop())));
    }

    @Test
    @FlakyTest(tolerance = TOLERANCE)
    public void testDivider_directionColumn_all_thickDivider() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_divider_test_direction_column);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                Drawable thickDivider = ResourcesCompat
                        .getDrawable(activity.getResources(), R.drawable.divider_thick, null);
                flexboxLayout.setDividerDrawableHorizontal(thickDivider);
                flexboxLayout.setShowDividerHorizontal(
                        FlexboxLayout.SHOW_DIVIDER_BEGINNING | FlexboxLayout.SHOW_DIVIDER_MIDDLE
                                | FlexboxLayout.SHOW_DIVIDER_END);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(flexboxLayout.getShowDividerHorizontal(),
                is(FlexboxLayout.SHOW_DIVIDER_BEGINNING | FlexboxLayout.SHOW_DIVIDER_MIDDLE |
                        FlexboxLayout.SHOW_DIVIDER_END));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        TextView text3 = (TextView) activity.findViewById(R.id.text3);
        Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider_thick, null);
        // The sum of three text views and the sum of thick dividers don't fit in one line.
        // The last text view should be placed to the next line.
        assertNotNull(divider);
        int heightSum = text1.getHeight() + text2.getHeight()
                + divider.getIntrinsicHeight() * 3;
        assertThat(text2.getBottom() + divider.getIntrinsicHeight(), is(heightSum));
        assertThat(text1.getTop(), is(not(flexboxLayout.getTop())));
        assertThat(text3.getRight(), is(text1.getWidth() + text3.getWidth()));
    }

    private TextView createTextView(Context context, String text, int order) {
        TextView textView = new TextView(context);
        textView.setText(text);
        FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.order = order;
        textView.setLayoutParams(lp);
        return textView;
    }

    private ViewAssertion hasWidth(final int width) {
        return matches(new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("expected width: " + width);
            }

            @Override
            protected void describeMismatchSafely(View item, Description mismatchDescription) {
                mismatchDescription.appendText("actual width: " + item.getWidth());
            }

            @Override
            protected boolean matchesSafely(View item) {
                return item.getWidth() == width;
            }
        });
    }

    private ViewAssertion hasHeight(final int height) {
        return matches(new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("expected height: " + height);
            }

            @Override
            protected void describeMismatchSafely(View item, Description mismatchDescription) {
                mismatchDescription.appendText("actual height: " + item.getHeight());
            }

            @Override
            protected boolean matchesSafely(View item) {
                return item.getHeight() == height;
            }
        });
    }
}
