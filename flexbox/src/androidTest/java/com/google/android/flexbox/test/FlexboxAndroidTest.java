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


import com.google.android.flexbox.FlexboxLayout;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
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
import static android.support.test.espresso.matcher.ViewMatchers.withId;
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
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_ROW_REVERSE));
        assertThat(flexboxLayout.getJustifyContent(), is(FlexboxLayout.JUSTIFY_CONTENT_CENTER));
        assertThat(flexboxLayout.getAlignContent(), is(FlexboxLayout.ALIGN_CONTENT_CENTER));
        assertThat(flexboxLayout.getAlignItems(), is(FlexboxLayout.ALIGN_ITEMS_CENTER));
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
    public void testFlexWrap_wrap() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_flex_wrap_test);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getFlexWrap(), is(FlexboxLayout.FLEX_WRAP_WRAP));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        // The width of the FlexboxLayout is not enough for placing the three text views.
        // The third text view should be placed below the first one
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
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
                flexboxLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_NOWRAP);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexboxLayout.FLEX_WRAP_NOWRAP));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        // The width of the FlexboxLayout is not enough for placing the three text views.
        // But the flexWrap attribute is set to FLEX_WRAP_NOWRAP, the third text view is placed
        // to the right of the second one and overflowing the parent FlexboxLayout.
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
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
                flexboxLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP_REVERSE);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexboxLayout.FLEX_WRAP_WRAP_REVERSE));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        // The width of the FlexboxLayout is not enough for placing the three text views.
        // There should be two flex lines same as FLEX_WRAP_WRAP, but the layout starts from bottom
        // to top in FLEX_WRAP_WRAP_REVERSE
        onView(withId(R.id.text3)).check(isAbove(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isAbove(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
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
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getFlexWrap(), is(FlexboxLayout.FLEX_WRAP_WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        // The height of the FlexboxLayout is not enough for placing the three text views.
        // The third text view should be placed right of the first one
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
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
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
                flexboxLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_NOWRAP);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexboxLayout.FLEX_WRAP_NOWRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        // The height of the FlexboxLayout is not enough for placing the three text views.
        // But the flexWrap attribute is set to FLEX_WRAP_NOWRAP, the third text view is placed
        // below the second one and overflowing the parent FlexboxLayout.
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
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
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
                flexboxLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP_REVERSE);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexWrap(), is(FlexboxLayout.FLEX_WRAP_WRAP_REVERSE));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        // The width of the FlexboxLayout is not enough for placing the three text views.
        // There should be two flex lines same as FLEX_WRAP_WRAP, but the layout starts from right
        // to left in FLEX_WRAP_WRAP_REVERSE
        onView(withId(R.id.text3)).check(isLeftOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isLeftOf(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
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
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
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

        assertThat(flexboxLayout.getJustifyContent(), is(FlexboxLayout.JUSTIFY_CONTENT_FLEX_START));
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

        assertThat(flexboxLayout.getJustifyContent(), is(FlexboxLayout.JUSTIFY_CONTENT_FLEX_START));
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
                flexboxLayout.setJustifyContent(FlexboxLayout.JUSTIFY_CONTENT_FLEX_END);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(), is(FlexboxLayout.JUSTIFY_CONTENT_FLEX_END));
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
                flexboxLayout.setJustifyContent(FlexboxLayout.JUSTIFY_CONTENT_FLEX_END);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(), is(FlexboxLayout.JUSTIFY_CONTENT_FLEX_END));
        onView(withId(R.id.text2)).check(isLeftOf(withId(R.id.text3)));
        onView(withId(R.id.text1)).check(isLeftOf(withId(R.id.text2)));
        TextView text3 = (TextView) activity.findViewById(R.id.text3);
        // Both the parent FrameLayout and the FlexboxLayout have different padding values
        // but the text3.getRight should be the padding value for the FlexboxLayout, not including
        // the parent's padding value
        assertThat(flexboxLayout.getWidth() - text3.getRight(), is(flexboxLayout.getPaddingRight()));
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
                flexboxLayout.setJustifyContent(FlexboxLayout.JUSTIFY_CONTENT_CENTER);
            }
        });

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getJustifyContent(), is(FlexboxLayout.JUSTIFY_CONTENT_CENTER));
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
                flexboxLayout.setJustifyContent(FlexboxLayout.JUSTIFY_CONTENT_CENTER);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getJustifyContent(), is(FlexboxLayout.JUSTIFY_CONTENT_CENTER));
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
                flexboxLayout.setJustifyContent(FlexboxLayout.JUSTIFY_CONTENT_SPACE_BETWEEN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(),
                is(FlexboxLayout.JUSTIFY_CONTENT_SPACE_BETWEEN));
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
                flexboxLayout.setJustifyContent(FlexboxLayout.JUSTIFY_CONTENT_SPACE_BETWEEN);
                flexboxLayout.setPadding(padding, padding, padding, padding);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(),
                is(FlexboxLayout.JUSTIFY_CONTENT_SPACE_BETWEEN));

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
                flexboxLayout.setJustifyContent(FlexboxLayout.JUSTIFY_CONTENT_SPACE_AROUND);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(),
                is(FlexboxLayout.JUSTIFY_CONTENT_SPACE_AROUND));
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
                flexboxLayout.setJustifyContent(FlexboxLayout.JUSTIFY_CONTENT_SPACE_AROUND);
                flexboxLayout.setPadding(padding, padding, padding, padding);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(),
                is(FlexboxLayout.JUSTIFY_CONTENT_SPACE_AROUND));

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
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(), is(FlexboxLayout.JUSTIFY_CONTENT_FLEX_START));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
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
                flexboxLayout.setJustifyContent(FlexboxLayout.JUSTIFY_CONTENT_FLEX_END);
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(), is(FlexboxLayout.JUSTIFY_CONTENT_FLEX_END));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
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
                flexboxLayout.setJustifyContent(FlexboxLayout.JUSTIFY_CONTENT_CENTER);
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(), is(FlexboxLayout.JUSTIFY_CONTENT_CENTER));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
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
                flexboxLayout.setJustifyContent(FlexboxLayout.JUSTIFY_CONTENT_SPACE_BETWEEN);
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(),
                is(FlexboxLayout.JUSTIFY_CONTENT_SPACE_BETWEEN));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
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
                flexboxLayout.setJustifyContent(FlexboxLayout.JUSTIFY_CONTENT_SPACE_BETWEEN);
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
                flexboxLayout.setPadding(padding, padding, padding, padding);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(),
                is(FlexboxLayout.JUSTIFY_CONTENT_SPACE_BETWEEN));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));

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
                flexboxLayout.setJustifyContent(FlexboxLayout.JUSTIFY_CONTENT_SPACE_AROUND);
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(),
                is(FlexboxLayout.JUSTIFY_CONTENT_SPACE_AROUND));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
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
                flexboxLayout.setJustifyContent(FlexboxLayout.JUSTIFY_CONTENT_SPACE_AROUND);
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
                flexboxLayout.setPadding(padding, padding, padding, padding);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getJustifyContent(),
                is(FlexboxLayout.JUSTIFY_CONTENT_SPACE_AROUND));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));

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
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
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

        assertThat(flexboxLayout.getAlignContent(), is(FlexboxLayout.ALIGN_CONTENT_STRETCH));
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
                flexboxLayout.setAlignContent(FlexboxLayout.ALIGN_CONTENT_FLEX_START);
            }
        });

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getAlignContent(), is(FlexboxLayout.ALIGN_CONTENT_FLEX_START));
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
                flexboxLayout.setAlignContent(FlexboxLayout.ALIGN_CONTENT_FLEX_END);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(FlexboxLayout.ALIGN_CONTENT_FLEX_END));
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
    public void testAlignContent_center() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_content_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignContent(FlexboxLayout.ALIGN_CONTENT_CENTER);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getAlignContent(), is(FlexboxLayout.ALIGN_CONTENT_CENTER));
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
                flexboxLayout.setAlignContent(FlexboxLayout.ALIGN_CONTENT_SPACE_BETWEEN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(FlexboxLayout.ALIGN_CONTENT_SPACE_BETWEEN));
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
                flexboxLayout.setAlignContent(FlexboxLayout.ALIGN_CONTENT_SPACE_BETWEEN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(FlexboxLayout.ALIGN_CONTENT_SPACE_BETWEEN));
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
                flexboxLayout.setAlignContent(FlexboxLayout.ALIGN_CONTENT_SPACE_AROUND);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(FlexboxLayout.ALIGN_CONTENT_SPACE_AROUND));
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

        assertThat(flexboxLayout.getAlignContent(), is(FlexboxLayout.ALIGN_CONTENT_STRETCH));
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
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(FlexboxLayout.ALIGN_CONTENT_STRETCH));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
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
                flexboxLayout.setAlignContent(FlexboxLayout.ALIGN_CONTENT_FLEX_START);
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });

        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getAlignContent(), is(FlexboxLayout.ALIGN_CONTENT_FLEX_START));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
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
                flexboxLayout.setAlignContent(FlexboxLayout.ALIGN_CONTENT_FLEX_END);
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(FlexboxLayout.ALIGN_CONTENT_FLEX_END));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
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
                flexboxLayout.setAlignContent(FlexboxLayout.ALIGN_CONTENT_CENTER);
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getAlignContent(), is(FlexboxLayout.ALIGN_CONTENT_CENTER));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
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
                flexboxLayout.setAlignContent(FlexboxLayout.ALIGN_CONTENT_SPACE_BETWEEN);
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(FlexboxLayout.ALIGN_CONTENT_SPACE_BETWEEN));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
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
                flexboxLayout.setAlignContent(FlexboxLayout.ALIGN_CONTENT_SPACE_AROUND);
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(FlexboxLayout.ALIGN_CONTENT_SPACE_AROUND));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
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
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignContent(), is(FlexboxLayout.ALIGN_CONTENT_STRETCH));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
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

        assertThat(flexboxLayout.getAlignItems(), is(FlexboxLayout.ALIGN_ITEMS_STRETCH));
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
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
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

        assertThat(flexboxLayout.getAlignItems(), is(FlexboxLayout.ALIGN_ITEMS_FLEX_START));
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
                flexboxLayout.setAlignItems(FlexboxLayout.ALIGN_ITEMS_FLEX_END);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(FlexboxLayout.ALIGN_ITEMS_FLEX_END));
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
    public void testAlignItems_center() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_align_items_test);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                flexboxLayout.setAlignItems(FlexboxLayout.ALIGN_ITEMS_CENTER);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(FlexboxLayout.ALIGN_ITEMS_CENTER));
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
                flexboxLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP_REVERSE);
                flexboxLayout.setAlignItems(FlexboxLayout.ALIGN_ITEMS_FLEX_END);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(FlexboxLayout.ALIGN_ITEMS_FLEX_END));
        assertThat(flexboxLayout.getFlexWrap(), is(FlexboxLayout.FLEX_WRAP_WRAP_REVERSE));
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
                flexboxLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP_REVERSE);
                flexboxLayout.setAlignItems(FlexboxLayout.ALIGN_ITEMS_CENTER);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(FlexboxLayout.ALIGN_ITEMS_CENTER));
        assertThat(flexboxLayout.getFlexWrap(), is(FlexboxLayout.FLEX_WRAP_WRAP_REVERSE));
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
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(FlexboxLayout.ALIGN_ITEMS_FLEX_START));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
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
                flexboxLayout.setAlignItems(FlexboxLayout.ALIGN_ITEMS_FLEX_END);
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(FlexboxLayout.ALIGN_ITEMS_FLEX_END));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
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
                flexboxLayout.setAlignItems(FlexboxLayout.ALIGN_ITEMS_CENTER);
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(FlexboxLayout.ALIGN_ITEMS_CENTER));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
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
                flexboxLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP_REVERSE);
                flexboxLayout.setAlignItems(FlexboxLayout.ALIGN_ITEMS_FLEX_END);
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(FlexboxLayout.ALIGN_ITEMS_FLEX_END));
        assertThat(flexboxLayout.getFlexWrap(), is(FlexboxLayout.FLEX_WRAP_WRAP_REVERSE));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
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
                flexboxLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP_REVERSE);
                flexboxLayout.setAlignItems(FlexboxLayout.ALIGN_ITEMS_CENTER);
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getAlignItems(), is(FlexboxLayout.ALIGN_ITEMS_CENTER));
        assertThat(flexboxLayout.getFlexWrap(), is(FlexboxLayout.FLEX_WRAP_WRAP_REVERSE));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
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
                flexboxLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_WRAP_REVERSE);
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
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_ROW_REVERSE);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_ROW_REVERSE));
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
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN_REVERSE);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        assertThat(flexboxLayout.getFlexDirection(),
                is(FlexboxLayout.FLEX_DIRECTION_COLUMN_REVERSE));
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

        // The text1 length is 50%, the text2 length is 60% and the wrap property is FLEX_WRAP_WRAP,
        // the text2 should be on the second flex line.
        assertThat(flexboxLayout.getFlexWrap(), is(FlexboxLayout.FLEX_WRAP_WRAP));
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
                flexboxLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_NOWRAP);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        // The text1 length is 50%, the text2 length is 60% and the text3 has the fixed width,
        // but the flex wrap attribute is FLEX_WRAP_NOWRAP, and flexShrink attributes for all
        // children are the default value (1), three text views are shrank to fit in a single flex
        // line.
        assertThat(flexboxLayout.getFlexWrap(), is(FlexboxLayout.FLEX_WRAP_NOWRAP));
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
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        // The text1 length is 50%, the text2 length is 60% and the wrap property is FLEX_WRAP_WRAP,
        // the text2 should be on the second flex line.
        assertThat(flexboxLayout.getFlexWrap(), is(FlexboxLayout.FLEX_WRAP_WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
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
                flexboxLayout.setFlexWrap(FlexboxLayout.FLEX_WRAP_NOWRAP);
                flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            }
        });
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);

        // The text1 length is 50%, the text2 length is 60% and the text3 has the fixed height,
        // but the flex wrap attribute is FLEX_WRAP_NOWRAP, and flexShrink attributes for all
        // children are the default value (1), three text views are shrank to fit in a single
        // flex line.
        assertThat(flexboxLayout.getFlexWrap(), is(FlexboxLayout.FLEX_WRAP_NOWRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_COLUMN));
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
        assertThat(flexboxLayout.getFlexWrap(), is(FlexboxLayout.FLEX_WRAP_WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_ROW));
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
        assertThat(flexboxLayout.getFlexWrap(), is(FlexboxLayout.FLEX_WRAP_WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexboxLayout.FLEX_DIRECTION_ROW));
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

    private TextView createTextView(Context context, String text, int order) {
        TextView textView = new TextView(context);
        textView.setText(text);
        FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.order = order;
        textView.setLayoutParams(lp);
        return textView;
    }
}
