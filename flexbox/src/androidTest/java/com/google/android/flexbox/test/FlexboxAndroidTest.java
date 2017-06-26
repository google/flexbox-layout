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

import static com.google.android.flexbox.test.IsEqualAllowingError.isEqualAllowingError;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewAssertion;
import android.support.test.filters.FlakyTest;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.flexbox.AlignContent;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexLine;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;


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
    @FlakyTest
    public void testLoadFromLayoutXml() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_simple);

        assertNotNull(flexboxLayout);
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW_REVERSE));
        assertThat(flexboxLayout.getJustifyContent(), is(JustifyContent.CENTER));
        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.CENTER));
        assertThat(flexboxLayout.getAlignItems(), is(AlignItems.CENTER));
        assertThat(flexboxLayout.getChildCount(), is(1));

        View child = flexboxLayout.getChildAt(0);
        FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) child.getLayoutParams();
        assertThat(lp.getOrder(), is(2));
        assertThat(lp.getFlexGrow(), is(1f));
        assertThat(lp.getAlignSelf(), is(AlignItems.STRETCH));
    }

    @Test
    @FlakyTest
    public void testOrderAttribute_fromLayoutXml() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_order_test);

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
    @FlakyTest
    public void testOrderAttribute_fromCode() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_order_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        TextView fifth = createTextView(activity, String.valueOf(5), 0);
                        TextView sixth = createTextView(activity, String.valueOf(6), -10);
                        flexboxLayout.addView(fifth);
                        flexboxLayout.addView(sixth);
                    }
                });

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
    @FlakyTest
    public void testChangeOrder_fromChildSetLayoutParams() throws Throwable {
        final FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_order_test);

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
                lp.setOrder(-3);
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
    @FlakyTest
    public void testOrderAttribute_addViewInMiddle() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_order_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        TextView fifth = createTextView(activity, String.valueOf(5), 0);
                        // Add the new TextView in the middle of the indices
                        flexboxLayout.addView(fifth, 2);
                    }
                });

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
    @FlakyTest
    public void testOrderAttribute_removeLastView() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_order_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.removeViewAt(flexboxLayout.getChildCount() - 1);
                    }
                });

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
    @FlakyTest
    public void testOrderAttribute_removeViewInMiddle() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_order_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.removeViewAt(2);
                    }
                });

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
    @FlakyTest
    public void testFlexWrap_wrap() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_flex_wrap_test);

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
        List<FlexLine> flexLines = flexboxLayout.getFlexLines();
        assertThat(flexLines.size(), is(2));
        FlexLine flexLine1 = flexLines.get(0);
        FlexboxTestActivity activity = mActivityRule.getActivity();
        assertThat(flexLine1.getMainSize(), is(TestUtil.dpToPixel(activity, 320)));
        assertThat(flexLine1.getCrossSize(), is(TestUtil.dpToPixel(activity, 120)));
        FlexLine flexLine2 = flexLines.get(1);
        assertThat(flexLine2.getMainSize(), is(TestUtil.dpToPixel(activity, 160)));
        assertThat(flexLine2.getCrossSize(), is(TestUtil.dpToPixel(activity, 120)));
    }

    @Test
    @FlakyTest
    public void testFlexWrap_nowrap() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_flex_wrap_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexWrap(FlexWrap.NOWRAP);
                        flexboxLayout.setAlignItems(AlignItems.FLEX_START);
                    }
                });

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
        List<FlexLine> flexLines = flexboxLayout.getFlexLines();
        assertThat(flexLines.size(), is(1));
        FlexLine flexLine = flexLines.get(0);
        FlexboxTestActivity activity = mActivityRule.getActivity();
        assertThat(flexLine.getMainSize(), is(TestUtil.dpToPixel(activity, 480)));
        assertThat(flexLine.getCrossSize(), is(TestUtil.dpToPixel(activity, 300)));
    }

    @Test
    @FlakyTest
    public void testFlexWrap_wrap_reverse() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_flex_wrap_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexWrap(FlexWrap.WRAP_REVERSE);
                    }
                });

        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP_REVERSE));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        // The width of the FlexboxLayout is not enough for placing the three text views.
        // There should be two flex lines same as WRAP, but the layout starts from bottom
        // to top in FlexWrap.WRAP_REVERSE
        onView(withId(R.id.text3)).check(isAbove(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isAbove(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        assertThat(flexboxLayout.getFlexLines().size(), is(2));
    }

    @Test
    @FlakyTest
    public void testFlexWrap_wrap_flexDirection_column() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_flex_wrap_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
    @FlakyTest
    public void testFlexWrap_nowrap_flexDirection_column() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_flex_wrap_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                        flexboxLayout.setFlexWrap(FlexWrap.NOWRAP);
                    }
                });

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
    @FlakyTest
    public void testFlexWrap_wrap_reverse_flexDirection_column() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_flex_wrap_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                        flexboxLayout.setFlexWrap(FlexWrap.WRAP_REVERSE);
                    }
                });

        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP_REVERSE));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        // The width of the FlexboxLayout is not enough for placing the three text views.
        // There should be two flex lines same as WRAP, but the layout starts from right
        // to left in FlexWrap.WRAP_REVERSE
        onView(withId(R.id.text3)).check(isLeftOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isLeftOf(withId(R.id.text2)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        assertThat(flexboxLayout.getFlexLines().size(), is(2));
    }

    @Test
    @FlakyTest
    public void testFlexItem_match_parent() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_flex_item_match_parent);
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
    @FlakyTest
    public void testFlexItem_match_parent_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_flex_item_match_parent_direction_column,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
    @FlakyTest
    public void testFlexboxLayout_wrapContent() throws Throwable {
        createFlexboxLayout(R.layout.activity_flexbox_wrap_content);
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
    @FlakyTest
    public void testFlexboxLayout_wrapped_with_ScrollView() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_flexbox_wrapped_with_scrollview);

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
    @FlakyTest
    public void testFlexboxLayout_wrapped_with_HorizontalScrollView() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_flexbox_wrapped_with_horizontalscrollview);

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
    @FlakyTest
    public void testJustifyContent_flexStart() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test);

        assertThat(flexboxLayout.getJustifyContent(), is(JustifyContent.FLEX_START));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isRightOf(withId(R.id.text2)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_flexStart_withParentPadding() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_justify_content_with_parent_padding);

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
    @FlakyTest
    public void testJustifyContent_flexEnd() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setJustifyContent(JustifyContent.FLEX_END);
                    }
                });

        assertThat(flexboxLayout.getJustifyContent(), is(JustifyContent.FLEX_END));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftOf(withId(R.id.text3)));
        onView(withId(R.id.text1)).check(isLeftOf(withId(R.id.text2)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_flexEnd_withParentPadding() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_justify_content_with_parent_padding,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setJustifyContent(JustifyContent.FLEX_END);
                    }
                });

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
    @FlakyTest
    public void testJustifyContent_center() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setJustifyContent(JustifyContent.CENTER);
                    }
                });

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
        assertThat(textView1.getLeft(), isEqualAllowingError(space));
        assertThat(flexboxLayout.getRight() - textView3.getRight(), isEqualAllowingError(space));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_center_withParentPadding() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_justify_content_with_parent_padding,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setJustifyContent(JustifyContent.CENTER);
                    }
                });

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
        assertThat(textView1.getLeft() - flexboxLayout.getPaddingLeft(),
                isEqualAllowingError(space));
        assertThat(flexboxLayout.getWidth() - textView3.getRight()
                - flexboxLayout.getPaddingRight(), isEqualAllowingError(space));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceBetween() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setJustifyContent(
                                JustifyContent.SPACE_BETWEEN);
                    }
                });

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
        assertThat(textView2.getLeft() - textView1.getRight(), isEqualAllowingError(space));
        assertThat(textView3.getLeft() - textView2.getRight(), isEqualAllowingError(space));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceBetween_withPadding() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final int padding = 40;
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setJustifyContent(
                                JustifyContent.SPACE_BETWEEN);
                        flexboxLayout.setPadding(padding, padding, padding, padding);
                    }
                });

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
        assertThat(textView2.getLeft() - textView1.getRight(), isEqualAllowingError(space));
        assertThat(textView3.getLeft() - textView2.getRight(), isEqualAllowingError(space));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceAround() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setJustifyContent(JustifyContent.SPACE_AROUND);
                    }
                });

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
        int spaceInMiddle = space * 2;
        assertThat(textView2.getLeft() - textView1.getRight(), isEqualAllowingError(spaceInMiddle));
        assertThat(textView3.getLeft() - textView2.getRight(), isEqualAllowingError(spaceInMiddle));
        assertThat(flexboxLayout.getRight() - textView3.getRight(), isEqualAllowingError(space));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceAround_withPadding() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final int padding = 40;
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setJustifyContent(JustifyContent.SPACE_AROUND);
                        flexboxLayout.setPadding(padding, padding, padding, padding);
                    }
                });

        assertThat(flexboxLayout.getJustifyContent(),
                is(JustifyContent.SPACE_AROUND));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int space = flexboxLayout.getWidth() - textView1.getWidth() - textView2.getWidth() -
                textView3.getWidth() - padding * 2;
        space = space / 6; // Divide by the number of children * 2
        assertThat(textView1.getLeft() - padding, isEqualAllowingError(space));

        int spaceInMiddle = space * 2;
        assertThat(textView2.getLeft() - textView1.getRight(), isEqualAllowingError(spaceInMiddle));
        assertThat(textView3.getLeft() - textView2.getRight(), isEqualAllowingError(spaceInMiddle));
        assertThat(flexboxLayout.getRight() - textView3.getRight() - padding,
                isEqualAllowingError(space));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_flexStart_flexDirection_column() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
    @FlakyTest
    public void testJustifyContent_flexEnd_flexDirection_column() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setJustifyContent(JustifyContent.FLEX_END);
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

        assertThat(flexboxLayout.getJustifyContent(), is(JustifyContent.FLEX_END));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isAbove(withId(R.id.text3)));
        onView(withId(R.id.text1)).check(isAbove(withId(R.id.text2)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_center_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setJustifyContent(JustifyContent.CENTER);
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
        assertThat(textView1.getTop(), isEqualAllowingError(space));
        assertThat(flexboxLayout.getBottom() - textView3.getBottom(), isEqualAllowingError(space));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceBetween_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setJustifyContent(
                                JustifyContent.SPACE_BETWEEN);
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
        assertThat(textView2.getTop() - textView1.getBottom(), isEqualAllowingError(space));
        assertThat(textView3.getTop() - textView2.getBottom(), isEqualAllowingError(space));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceBetween_flexDirection_column_withPadding()
            throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final int padding = 40;
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setJustifyContent(
                                JustifyContent.SPACE_BETWEEN);
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                        flexboxLayout.setPadding(padding, padding, padding, padding);
                    }
                });

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
        assertThat(textView2.getTop() - textView1.getBottom(), isEqualAllowingError(space));
        assertThat(textView3.getTop() - textView2.getBottom(), isEqualAllowingError(space));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceAround_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setJustifyContent(JustifyContent.SPACE_AROUND);
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

        assertThat(flexboxLayout.getJustifyContent(),
                is(JustifyContent.SPACE_AROUND));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int space = flexboxLayout.getHeight() - textView1.getHeight() - textView2.getHeight() -
                textView3.getHeight();
        space = space / 6; // Divide by the number of children * 2
        assertThat(textView1.getTop(), isEqualAllowingError(space));
        int spaceInMiddle = space * 2;
        assertThat(textView2.getTop() - textView1.getBottom(), isEqualAllowingError(spaceInMiddle));
        assertThat(textView3.getTop() - textView2.getBottom(), isEqualAllowingError(spaceInMiddle));
        assertThat(flexboxLayout.getBottom() - textView3.getBottom(), isEqualAllowingError(space));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceAround_flexDirection_column_withPadding() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final int padding = 40;
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setJustifyContent(JustifyContent.SPACE_AROUND);
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                        flexboxLayout.setPadding(padding, padding, padding, padding);
                    }
                });

        assertThat(flexboxLayout.getJustifyContent(),
                is(JustifyContent.SPACE_AROUND));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int space = flexboxLayout.getHeight() - textView1.getHeight() - textView2.getHeight() -
                textView3.getHeight() - padding * 2;
        space = space / 6; // Divide by the number of children * 2
        assertThat(textView1.getTop() - padding, isEqualAllowingError(space));
        int spaceInMiddle = space * 2;
        assertThat(textView2.getTop() - textView1.getBottom(), isEqualAllowingError(spaceInMiddle));
        assertThat(textView3.getTop() - textView2.getBottom(), isEqualAllowingError(spaceInMiddle));
        assertThat(flexboxLayout.getBottom() - textView3.getBottom() - padding,
                isEqualAllowingError(space));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceAround_including_gone_views() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_justify_content_with_gone,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setJustifyContent(JustifyContent.SPACE_AROUND);
                    }
                }
        );

        assertThat(flexboxLayout.getJustifyContent(),
                is(JustifyContent.SPACE_AROUND));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int space = flexboxLayout.getWidth() - textView1.getWidth() - textView3.getWidth();
        space = space / 4; // Divide by the number of visible children * 2
        assertThat(textView1.getLeft(), isEqualAllowingError(space));
        int spaceInMiddle = space * 2;
        assertThat(textView3.getLeft() - textView1.getRight(), isEqualAllowingError(spaceInMiddle));
        assertThat(flexboxLayout.getRight() - textView3.getRight(), isEqualAllowingError(space));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceBetween_including_gone_views() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_justify_content_with_gone,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setJustifyContent(
                                JustifyContent.SPACE_BETWEEN);
                    }
                });

        assertThat(flexboxLayout.getJustifyContent(),
                is(JustifyContent.SPACE_BETWEEN));

        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int space = flexboxLayout.getWidth() - textView1.getWidth() - textView3.getWidth();
        assertThat(textView3.getLeft() - textView1.getRight(), isEqualAllowingError(space));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceAround_including_gone_views_direction_column()
            throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_justify_content_with_gone,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                        flexboxLayout.setJustifyContent(JustifyContent.SPACE_AROUND);
                    }
                });

        assertThat(flexboxLayout.getFlexDirection(),
                is(FlexDirection.COLUMN));
        assertThat(flexboxLayout.getJustifyContent(),
                is(JustifyContent.SPACE_AROUND));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int space = flexboxLayout.getHeight() - textView1.getHeight() - textView3.getHeight();
        space = space / 4; // Divide by the number of visible children * 2
        assertThat(textView1.getTop(), isEqualAllowingError(space));
        int spaceInMiddle = space * 2;
        assertThat(textView3.getTop() - textView1.getBottom(), isEqualAllowingError(spaceInMiddle));
        assertThat(flexboxLayout.getBottom() - textView3.getBottom(), isEqualAllowingError(space));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceBetween_including_gone_views_direction_column()
            throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_justify_content_with_gone,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                        flexboxLayout.setJustifyContent(
                                JustifyContent.SPACE_BETWEEN);
                    }
                });

        assertThat(flexboxLayout.getFlexDirection(),
                is(FlexDirection.COLUMN));
        assertThat(flexboxLayout.getJustifyContent(),
                is(JustifyContent.SPACE_BETWEEN));

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int space = flexboxLayout.getHeight() - textView1.getHeight() - textView3.getHeight();
        assertThat(textView3.getTop() - textView1.getBottom(), isEqualAllowingError(space));
    }

    @Test
    @FlakyTest
    public void testFlexGrow_withExactParentLength() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_flex_grow_test);

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
    @FlakyTest
    public void testFlexGrow_withExactParentLength_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_flex_grow_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
    @FlakyTest
    public void testFlexGrow_including_view_gone() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_flex_grow_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
                        textView2.setVisibility(View.GONE);
                    }
                });

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
    @FlakyTest
    public void testAlignContent_stretch() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test);

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
    @FlakyTest
    public void testAlignContent_flexStart() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setAlignContent(AlignContent.FLEX_START);
                    }
                });

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
    @FlakyTest
    public void testAlignContent_flexEnd() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setAlignContent(AlignContent.FLEX_END);
                    }
                });

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
    @FlakyTest
    public void testAlignContent_flexEnd_parentPadding() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setAlignContent(AlignContent.FLEX_END);
                        flexboxLayout.setPadding(32, 32, 32, 32);
                    }
                });

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.FLEX_END));
        onView(withId(R.id.text1)).check(isAbove(withId(R.id.text3)));
        onView(withId(R.id.text2)).check(isAbove(withId(R.id.text3)));

        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(textView3.getBottom(),
                is(flexboxLayout.getBottom() - flexboxLayout.getPaddingBottom()));
    }

    @Test
    @FlakyTest
    public void testAlignContent_flexEnd_parentPadding_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setAlignContent(AlignContent.FLEX_END);
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                        flexboxLayout.setPadding(32, 32, 32, 32);
                    }
                });

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.FLEX_END));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isLeftOf(withId(R.id.text3)));
        onView(withId(R.id.text2)).check(isLeftOf(withId(R.id.text3)));

        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        assertThat(textView3.getRight(),
                is(flexboxLayout.getRight() - flexboxLayout.getPaddingRight()));
    }

    @Test
    @FlakyTest
    public void testAlignContent_center() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setAlignContent(AlignContent.CENTER);
                    }
                });

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.CENTER));

        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isBelow(withId(R.id.text1)));

        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        int spaceAboveAndBottom = flexboxLayout.getHeight() - textView1.getHeight() - textView3
                .getHeight();
        spaceAboveAndBottom /= 2;

        assertThat(textView1.getTop(), isEqualAllowingError(spaceAboveAndBottom));
        assertThat(flexboxLayout.getBottom() - textView3.getBottom(),
                isEqualAllowingError(spaceAboveAndBottom));
        assertThat(flexboxLayout.getFlexLines().size(), is(2));
    }

    @Test
    @FlakyTest
    public void testAlignContent_spaceBetween() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setAlignContent(AlignContent.SPACE_BETWEEN);
                    }
                });

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.SPACE_BETWEEN));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        assertThat(flexboxLayout.getFlexLines().size(), is(2));
    }

    @Test
    @FlakyTest
    public void testAlignContent_spaceBetween_withPadding() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setAlignContent(AlignContent.SPACE_BETWEEN);
                    }
                });

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.SPACE_BETWEEN));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
    }

    @Test
    @FlakyTest
    public void testAlignContent_spaceAround() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setAlignContent(AlignContent.SPACE_AROUND);
                    }
                });

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.SPACE_AROUND));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);

        int spaceAround = flexboxLayout.getHeight() - textView1.getHeight() - textView3.getHeight();
        spaceAround /= 4; // Divide by the number of flex lines * 2

        assertThat(textView1.getTop(), isEqualAllowingError(spaceAround));
        int spaceInMiddle = textView1.getBottom() + spaceAround * 2;
        assertThat(textView3.getTop(), isEqualAllowingError(spaceInMiddle));
        assertThat(flexboxLayout.getFlexLines().size(), is(2));
    }

    @Test
    @FlakyTest
    public void testAlignContent_stretch_parentWrapContent() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        ViewGroup.LayoutParams parentLp = flexboxLayout.getLayoutParams();
                        parentLp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        flexboxLayout.setLayoutParams(parentLp);
                    }
                });

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
        assertThat(flexboxLayout.getFlexLines().size(), is(2));
    }

    @Test
    @FlakyTest
    public void testAlignContent_stretch_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
    @FlakyTest
    public void testAlignContent_flexStart_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setAlignContent(AlignContent.FLEX_START);
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
    @FlakyTest
    public void testAlignContent_flexEnd_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setAlignContent(AlignContent.FLEX_END);
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
    @FlakyTest
    public void testAlignContent_center_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setAlignContent(AlignContent.CENTER);
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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

        assertThat(textView1.getLeft(), isEqualAllowingError(spaceLeftAndRight));
        assertThat(textView3.getRight(),
                isEqualAllowingError(flexboxLayout.getRight() - spaceLeftAndRight));
    }

    @Test
    @FlakyTest
    public void testAlignContent_spaceBetween_flexDirection_column() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setAlignContent(AlignContent.SPACE_BETWEEN);
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
    @FlakyTest
    public void testAlignContent_spaceAround_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setAlignContent(AlignContent.SPACE_AROUND);
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.SPACE_AROUND));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBelow(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);

        int spaceAround = flexboxLayout.getWidth() - textView1.getWidth() - textView3.getWidth();
        spaceAround /= 4; // Divide by the number of flex lines * 2

        assertThat(textView1.getLeft(), isEqualAllowingError(spaceAround));
        int spaceInMiddle = textView1.getRight() + spaceAround * 2;
        assertThat(textView3.getLeft(), isEqualAllowingError(spaceInMiddle));
    }

    @Test
    @FlakyTest
    public void testAlignContent_stretch_parentWrapContent_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        ViewGroup.LayoutParams parentLp = flexboxLayout.getLayoutParams();
                        parentLp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                        flexboxLayout.setLayoutParams(parentLp);
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
    @FlakyTest
    public void testAlignContent_flexEnd_wrapReverse_contentOverflowed() throws Throwable {
        FlexboxLayout flexboxLayout =
                createFlexboxLayout(R.layout.activity_align_content_test_overflowed,
                        new Configuration() {
                            @Override
                            public void apply(FlexboxLayout flexboxLayout) {
                                flexboxLayout.setAlignContent(AlignContent.FLEX_END);
                                flexboxLayout.setFlexWrap(FlexWrap.WRAP_REVERSE);
                            }
                        });
        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.FLEX_END));
        onView(withId(R.id.text6)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text5)).check(isLeftOf(withId(R.id.text6)));
        onView(withId(R.id.text4)).check(isBelow(withId(R.id.text6)));
    }

    @Test
    @FlakyTest
    public void testAlignContent_flexStart_wrapReverse_contentOverflowed() throws Throwable {
        FlexboxLayout flexboxLayout =
                createFlexboxLayout(R.layout.activity_align_content_test_overflowed,
                        new Configuration() {
                            @Override
                            public void apply(FlexboxLayout flexboxLayout) {
                                flexboxLayout.setAlignContent(
                                        AlignContent.FLEX_START);
                                flexboxLayout.setFlexWrap(FlexWrap.WRAP_REVERSE);
                            }
                        });
        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.FLEX_START));
        onView(withId(R.id.text1)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isAbove(withId(R.id.text1)));
    }

    @Test
    @FlakyTest
    public void testAlignContent_spaceBetween_wrapReverse_contentOverflowed() throws Throwable {
        FlexboxLayout flexboxLayout =
                createFlexboxLayout(R.layout.activity_align_content_test_overflowed,
                        new Configuration() {
                            @Override
                            public void apply(FlexboxLayout flexboxLayout) {
                                flexboxLayout.setAlignContent(
                                        AlignContent.SPACE_BETWEEN);
                                flexboxLayout.setFlexWrap(FlexWrap.WRAP_REVERSE);
                            }
                        });
        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.SPACE_BETWEEN));
        onView(withId(R.id.text1)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightOf(withId(R.id.text1)));
        onView(withId(R.id.text3)).check(isAbove(withId(R.id.text1)));
    }

    @Test
    @FlakyTest
    public void testAlignContent_center_wrapReverse_contentOverflowed() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout =
                createFlexboxLayout(R.layout.activity_align_content_test_overflowed,
                        new Configuration() {
                            @Override
                            public void apply(FlexboxLayout flexboxLayout) {
                                flexboxLayout.setAlignContent(AlignContent.CENTER);
                                flexboxLayout.setFlexWrap(FlexWrap.WRAP_REVERSE);
                            }
                        });
        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.CENTER));
        TextView textView6 = (TextView) activity.findViewById(R.id.text6);
        TextView textView4 = (TextView) activity.findViewById(R.id.text4);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);

        assertThat(textView6.getTop() - flexboxLayout.getTop(), isEqualAllowingError(
                (flexboxLayout.getHeight() - textView6.getHeight() - textView4.getHeight()
                        - textView2.getHeight()) / 2));
    }

    @Test
    @FlakyTest
    public void testAlignContent_spaceAround_wrapReverse_contentOverflowed() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout =
                createFlexboxLayout(R.layout.activity_align_content_test_overflowed,
                        new Configuration() {
                            @Override
                            public void apply(FlexboxLayout flexboxLayout) {
                                flexboxLayout.setAlignContent(
                                        AlignContent.SPACE_AROUND);
                                flexboxLayout.setFlexWrap(FlexWrap.WRAP_REVERSE);
                            }
                        });
        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.SPACE_AROUND));
        TextView textView6 = (TextView) activity.findViewById(R.id.text6);
        TextView textView4 = (TextView) activity.findViewById(R.id.text4);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);

        assertThat(textView6.getTop() - flexboxLayout.getTop(), isEqualAllowingError(
                (flexboxLayout.getHeight() - textView6.getHeight() - textView4.getHeight()
                        - textView2.getHeight()) / 2));
    }

    @Test
    @FlakyTest
    public void testAlignItems_stretch() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_stretch_test);

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
        assertThat(textView1.getHeight(), isEqualAllowingError(flexLineSize));
        assertThat(textView2.getHeight(), isEqualAllowingError(flexLineSize));
        assertThat(textView3.getHeight(), isEqualAllowingError(flexLineSize));
    }

    @Test
    @FlakyTest
    public void testAlignSelf_stretch() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_align_self_stretch_test);

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
        assertThat(textView1.getHeight(), isEqualAllowingError(flexLineSize));
        assertThat(textView2.getHeight(), not(flexLineSize));
        assertThat(textView3.getHeight(), not(flexLineSize));
    }

    @Test
    @FlakyTest
    public void testAlignSelf_stretch_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_self_stretch_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
        assertThat(textView1.getWidth(), isEqualAllowingError(flexLineSize));
        assertThat(textView2.getWidth(), not(flexLineSize));
        assertThat(textView3.getWidth(), not(flexLineSize));
    }

    @Test
    @FlakyTest
    public void testAlignItems_flexStart() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test);

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
        assertThat(textView3.getTop(), isEqualAllowingError(flexLineSize));
    }

    @Test
    @FlakyTest
    public void testAlignItems_flexEnd() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setAlignItems(AlignItems.FLEX_END);
                    }
                });

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
        assertThat(textView1.getBottom(), isEqualAllowingError(flexLineSize));
        assertThat(textView2.getBottom(), isEqualAllowingError(flexLineSize));
        assertThat(textView3.getBottom(), is(flexboxLayout.getBottom()));
    }

    @Test
    @FlakyTest
    public void testAlignItems_flexEnd_parentPadding() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_align_items_parent_padding_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setAlignItems(AlignItems.FLEX_END);
                    }
                });

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
    @FlakyTest
    public void testAlignItems_flexEnd_parentPadding_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_align_items_parent_padding_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setAlignItems(AlignItems.FLEX_END);
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
    @FlakyTest
    public void testAlignItems_center() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setAlignItems(AlignItems.CENTER);
                    }
                });

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
        assertThat(textView1.getTop(), isEqualAllowingError(spaceAboveAndBelow));
        assertThat(textView2.getTop(), isEqualAllowingError(spaceAboveAndBelow));
        assertThat(textView3.getTop(), isEqualAllowingError(flexLineSize + spaceAboveAndBelow));
    }

    @Test
    @FlakyTest
    public void testAlignItems_flexEnd_wrapReverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexWrap(FlexWrap.WRAP_REVERSE);
                        flexboxLayout.setAlignItems(AlignItems.FLEX_END);
                    }
                });

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
        assertThat(textView1.getTop(),
                isEqualAllowingError(flexboxLayout.getHeight() - flexLineSize));
        assertThat(textView2.getTop(),
                isEqualAllowingError(flexboxLayout.getHeight() - flexLineSize));
        assertThat(textView3.getTop(), is(0));
    }

    @Test
    @FlakyTest
    public void testAlignItems_center_wrapReverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexWrap(FlexWrap.WRAP_REVERSE);
                        flexboxLayout.setAlignItems(AlignItems.CENTER);
                    }
                });

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
        assertThat(textView1.getBottom(),
                isEqualAllowingError(flexboxLayout.getHeight() - spaceAboveAndBelow));
        assertThat(textView2.getBottom(),
                isEqualAllowingError(flexboxLayout.getHeight() - spaceAboveAndBelow));
        assertThat(textView3.getBottom(), isEqualAllowingError(
                flexboxLayout.getHeight() - flexLineSize - spaceAboveAndBelow));
    }

    @Test
    @FlakyTest
    public void testAlignItems_flexStart_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
        assertThat(textView3.getLeft(), isEqualAllowingError(flexLineSize));
    }

    @Test
    @FlakyTest
    public void testAlignItems_flexEnd_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setAlignItems(AlignItems.FLEX_END);
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
        assertThat(textView1.getRight(), isEqualAllowingError(flexLineSize));
        assertThat(textView2.getRight(), isEqualAllowingError(flexLineSize));
        assertThat(textView3.getRight(), is(flexboxLayout.getRight()));
    }

    @Test
    @FlakyTest
    public void testAlignItems_center_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setAlignItems(AlignItems.CENTER);
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
        assertThat(textView1.getLeft(), isEqualAllowingError(spaceLeftAndRight));
        assertThat(textView2.getLeft(), isEqualAllowingError(spaceLeftAndRight));
        assertThat(textView3.getLeft(), isEqualAllowingError(flexLineSize + spaceLeftAndRight));
    }

    @Test
    @FlakyTest
    public void testAlignItems_flexEnd_wrapReverse_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexWrap(FlexWrap.WRAP_REVERSE);
                        flexboxLayout.setAlignItems(AlignItems.FLEX_END);
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
        assertThat(textView1.getLeft(),
                isEqualAllowingError(flexboxLayout.getWidth() - flexLineSize));
        assertThat(textView2.getLeft(),
                isEqualAllowingError(flexboxLayout.getWidth() - flexLineSize));
        assertThat(textView3.getLeft(), is(0));
    }

    @Test
    @FlakyTest
    public void testAlignItems_center_wrapReverse_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexWrap(FlexWrap.WRAP_REVERSE);
                        flexboxLayout.setAlignItems(AlignItems.CENTER);
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
        assertThat(textView1.getRight(),
                isEqualAllowingError(flexboxLayout.getWidth() - spaceLeftAndRight));
        assertThat(textView2.getRight(),
                isEqualAllowingError(flexboxLayout.getWidth() - spaceLeftAndRight));
        assertThat(textView3.getRight(),
                isEqualAllowingError(flexboxLayout.getWidth() - flexLineSize - spaceLeftAndRight));
    }

    @Test
    @FlakyTest
    public void testAlignItems_baseline() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        createFlexboxLayout(R.layout.activity_align_items_baseline_test);
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
    @FlakyTest
    public void testAlignItems_baseline_wrapReverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        createFlexboxLayout(R.layout.activity_align_items_baseline_test, new Configuration() {
            @Override
            public void apply(FlexboxLayout flexboxLayout) {
                flexboxLayout.setFlexWrap(FlexWrap.WRAP_REVERSE);
            }
        });
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
    @FlakyTest
    public void testFlexDirection_row_reverse() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_flex_wrap_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexDirection(FlexDirection.ROW_REVERSE);
                    }
                });

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
    @FlakyTest
    public void testFlexDirection_column_reverse() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_flex_wrap_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN_REVERSE);
                    }
                });

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
    @FlakyTest
    public void testFlexBasisPercent_wrap() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_flex_basis_percent_test);

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
                is(Math.round(flexboxLayout.getWidth() * lp1.getFlexBasisPercent())));
        assertThat(textView2.getWidth(),
                is(Math.round(flexboxLayout.getWidth() * lp2.getFlexBasisPercent())));
    }

    @Test
    @FlakyTest
    public void testFlexBasisPercent_nowrap() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_flex_basis_percent_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexWrap(FlexWrap.NOWRAP);
                    }
                });

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
        assertThat(flexboxLayout.getWidth(), isEqualAllowingError(totalWidth));
    }

    @Test
    @FlakyTest
    public void testFlexBasisPercent_wrap_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_flex_basis_percent_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
                is(Math.round(flexboxLayout.getHeight() * lp1.getFlexBasisPercent())));
        assertThat(textView2.getHeight(),
                is(Math.round(flexboxLayout.getHeight() * lp2.getFlexBasisPercent())));
    }

    @Test
    @FlakyTest
    public void testFlexBasisPercent_nowrap_flexDirection_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_flex_basis_percent_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexWrap(FlexWrap.NOWRAP);
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });

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
        assertThat(flexboxLayout.getHeight(), isEqualAllowingError(totalHeight));
    }

    @Test
    @FlakyTest
    public void testMinWidth_initial_width_less_than_minWidth() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();

        // This test case verifies if the minWidth attribute works as a minimum constraint
        // If the initial view width is less than the value of minWidth.
        // The textView1's layout_width is set to wrap_content and its text is "1" apparently
        // the initial measured width is less than the value of layout_minWidth (100dp)
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_minwidth_test);
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        int minWidth = ((FlexboxLayout.LayoutParams) textView1.getLayoutParams()).getMinWidth();

        onView(withId(R.id.text1)).check(hasWidth(minWidth));
        onView(withId(R.id.text2)).check(hasWidth(flexboxLayout.getWidth() - minWidth));
    }

    @Test
    @FlakyTest
    public void testMinWidth_works_as_lower_bound_shrink_to() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();

        // This test case verifies if the minWidth attribute works as a lower bound
        // when the view would shrink less than the minWidth if the minWidth weren't set
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_minwidth_lower_bound_test);
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        TextView textView4 = (TextView) activity.findViewById(R.id.text4);
        int minWidth = ((FlexboxLayout.LayoutParams) textView1.getLayoutParams()).getMinWidth();

        onView(withId(R.id.text1)).check(hasWidth(minWidth));
        assertEquals(flexboxLayout.getWidth(),
                textView1.getWidth() + textView2.getWidth() + textView3.getWidth() + textView4
                        .getWidth());
    }

    @Test
    @FlakyTest
    public void testMinHeight_initial_height_less_than_minHeight() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();

        // This test case verifies if the minHeight attribute works as a minimum constraint
        // If the initial view height is less than the value of minHeight.
        // The textView1's layout_height is set to wrap_content and its text is "1" apparently
        // the initial measured height is less than the value of layout_minHeight (100dp)
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_minheight_test);
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        int minHeight = ((FlexboxLayout.LayoutParams) textView1.getLayoutParams()).getMinHeight();

        onView(withId(R.id.text1)).check(hasHeight(minHeight));
        onView(withId(R.id.text2)).check(hasHeight(flexboxLayout.getHeight() - minHeight));
    }

    @Test
    @FlakyTest
    public void testMinHeight_works_as_lower_bound_shrink_to() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();

        // This test case verifies if the minHeight attribute works as a lower bound
        // when the view would shrink less than the minHeight if the minHeight weren't set
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_minheight_lower_bound_test);
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);
        TextView textView4 = (TextView) activity.findViewById(R.id.text4);
        int minHeight = ((FlexboxLayout.LayoutParams) textView1.getLayoutParams()).getMinHeight();

        onView(withId(R.id.text1)).check(hasHeight(minHeight));
        assertEquals(flexboxLayout.getHeight(),
                textView1.getHeight() + textView2.getHeight() + textView3.getHeight()
                        + textView4.getHeight());
    }

    @Test
    @FlakyTest
    public void testMaxWidth_initial_width_more_than_maxWidth() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();

        // This test case verifies if the maxWidth attribute works as a maximum constraint
        // ff the initial view width is more than the value of maxWidth.
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_maxwidth_test);
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        int maxWidth = ((FlexboxLayout.LayoutParams) textView1.getLayoutParams()).getMaxWidth();

        onView(withId(R.id.text1)).check(hasWidth(maxWidth));
        onView(withId(R.id.text2)).check(hasWidth(flexboxLayout.getWidth() - maxWidth));
    }

    @Test
    @FlakyTest
    public void testMaxWidth_works_as_upper_bound_expand_to() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();

        // This test case verifies if the maxWidth attribute works as a upper bound
        // when the view would expand more than the maxWidth if the maxWidth weren't set
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_maxwidth_upper_bound_test);
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        int maxWidth = ((FlexboxLayout.LayoutParams) textView1.getLayoutParams()).getMaxWidth();

        onView(withId(R.id.text1)).check(hasWidth(maxWidth));
        assertEquals(flexboxLayout.getWidth(), textView1.getWidth() + textView2.getWidth());
    }

    @Test
    @FlakyTest
    public void testMaxHeight_initial_height_more_than_maxHeight() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();

        // This test case verifies if the maxHeight attribute works as a maximum constraint
        // ff the initial view height is more than the value of maxHeight.
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_maxheight_test);
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        int maxHeight = ((FlexboxLayout.LayoutParams) textView1.getLayoutParams()).getMaxHeight();

        onView(withId(R.id.text1)).check(hasHeight(maxHeight));
        onView(withId(R.id.text2)).check(hasHeight(flexboxLayout.getHeight() - maxHeight));
    }

    @Test
    @FlakyTest
    public void testMaxHeight_works_as_lower_bound_expand_to() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();

        // This test case verifies if the maxHeight attribute works as a upper bound
        // when the view would expand more than the maxHeight if the maxHeight weren't set
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_maxheight_upper_bound_test);
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView2 = (TextView) activity.findViewById(R.id.text2);
        int maxHeight = ((FlexboxLayout.LayoutParams) textView1.getLayoutParams()).getMaxHeight();

        onView(withId(R.id.text1)).check(hasHeight(maxHeight));
        assertEquals(flexboxLayout.getHeight(), textView1.getHeight() + textView2.getHeight());
    }

    @Test
    @FlakyTest
    public void testView_visibility_gone() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_views_visibility_gone);

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
    @FlakyTest
    public void testView_visibility_gone_first_item_in_flex_line_horizontal() throws Throwable {
        // This test verifies if the FlexboxLayout is visible when the visibility of the first
        // flex item in the second flex line (or arbitrary flex lines other than the first flex
        // line)
        // is set to "gone"
        // There was an issue reported for that
        // https://github.com/google/flexbox-layout/issues/47
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_visibility_gone_first_item_in_flex_line_row);

        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);

        assertTrue(flexboxLayout.getHeight() > 0);
        assertThat(flexboxLayout.getHeight(), is(textView1.getHeight() + textView3.getHeight()));
    }

    @Test
    @FlakyTest
    public void testView_visibility_gone_first_item_in_flex_line_vertical() throws Throwable {
        // This test verifies if the FlexboxLayout is visible when the visibility of the first
        // flex item in the second flex line (or arbitrary flex lines other than the first flex
        // line)
        // is set to "gone"
        // There was an issue reported for that
        // https://github.com/google/flexbox-layout/issues/47
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_visibility_gone_first_item_in_flex_line_column);

        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP));
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));
        TextView textView1 = (TextView) activity.findViewById(R.id.text1);
        TextView textView3 = (TextView) activity.findViewById(R.id.text3);

        assertTrue(flexboxLayout.getWidth() > 0);
        assertThat(flexboxLayout.getWidth(), is(textView1.getWidth() + textView3.getWidth()));
    }

    @Test
    @FlakyTest
    public void testView_visibility_invisible() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_views_visibility_invisible);

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
    @FlakyTest
    public void testWrapBefore() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_wrap_before_test);

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
                lp2.setWrapBefore(false);
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
    @FlakyTest
    public void testWrapBefore_nowrap() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_wrap_before_test,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexWrap(FlexWrap.NOWRAP);
                    }
                });

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
    @FlakyTest
    public void testWrap_parentPadding_horizontal() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_wrap_parent_padding_horizontal_test);

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
    @FlakyTest
    public void testWrap_parentPadding_vertical() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_wrap_parent_padding_vertical_test);

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
    @FlakyTest
    public void testWrap_childMargin_horizontal() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_wrap_child_margin_horizontal_test);

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
    @FlakyTest
    public void testFirstItemLarge_horizontal() throws Throwable {
        // This test verifies a empty flex line is not added when the first flex item is large
        // and judged wrapping is required with the first item.
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_first_item_large_horizontal_test);

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
    @FlakyTest
    public void testFirstItemLarge_vertical() throws Throwable {
        // This test verifies a empty flex line is not added when the first flex item is large
        // and judged wrapping is required with the first item.
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_first_item_large_vertical_test);

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
    @FlakyTest
    public void testWrap_childMargin_vertical() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_wrap_child_margin_vertical_test);

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
    @FlakyTest
    public void testEmptyChildren() throws Throwable {
        FlexboxLayout flexboxLayout = createFlexboxLayout(R.layout.activity_empty_children);

        assertThat(flexboxLayout.getChildCount(), is(0));
    }

    @Test
    @FlakyTest
    public void testDivider_directionRow_verticalBeginning() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_row);
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
        List<FlexLine> flexLines = flexboxLayout.getFlexLines();
        assertThat(flexLines.size(), is(2));
        FlexLine flexLine1 = flexLines.get(0);
        // The right should be 90 * 3 + 10 (divider)
        assertThat(flexLine1.getMainSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 280)));
        assertThat(flexLine1.getCrossSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        FlexLine flexLine2 = flexLines.get(1);
        // The right should be 140 * 2 + 10 (divider)
        assertThat(flexLine2.getMainSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 290)));
        assertThat(flexLine2.getCrossSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
    }

    @Test
    @FlakyTest
    public void testDivider_directionRow_verticalMiddle() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_row,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setShowDividerVertical(FlexboxLayout.SHOW_DIVIDER_MIDDLE);
                    }
                }
        );
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
        List<FlexLine> flexLines = flexboxLayout.getFlexLines();
        assertThat(flexLines.size(), is(2));
        FlexLine flexLine1 = flexLines.get(0);
        // The right should be 90 * 3 + 10 * 2(divider)
        assertThat(flexLine1.getMainSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 290)));
        assertThat(flexLine1.getCrossSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        FlexLine flexLine2 = flexLines.get(1);
        // The right should be 140 * 2 + 10 (divider)
        assertThat(flexLine2.getMainSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 290)));
        assertThat(flexLine2.getCrossSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
    }

    @Test
    @FlakyTest
    public void testDivider_directionRow_verticalEnd() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_row,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setShowDividerVertical(FlexboxLayout.SHOW_DIVIDER_END);
                    }
                }
        );

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
        List<FlexLine> flexLines = flexboxLayout.getFlexLines();
        assertThat(flexLines.size(), is(2));
        FlexLine flexLine1 = flexLines.get(0);
        // The right should be 90 * 3 + 10 (divider)
        assertThat(flexLine1.getMainSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 280)));
        assertThat(flexLine1.getCrossSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        FlexLine flexLine2 = flexLines.get(1);
        // The right should be 140 * 2 + 10 (divider)
        assertThat(flexLine2.getMainSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 290)));
        assertThat(flexLine2.getCrossSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
    }

    @Test
    @FlakyTest
    public void testDivider_directionRow_verticalAll() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_row,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setShowDividerVertical(
                                FlexboxLayout.SHOW_DIVIDER_BEGINNING
                                        | FlexboxLayout.SHOW_DIVIDER_MIDDLE
                                        | FlexboxLayout.SHOW_DIVIDER_END);
                    }
                }
        );

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
        List<FlexLine> flexLines = flexboxLayout.getFlexLines();
        assertThat(flexLines.size(), is(2));
        FlexLine flexLine1 = flexLines.get(0);
        // The right should be 90 * 3 + 10 * 4 (divider)
        assertThat(flexLine1.getMainSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 310)));
        assertThat(flexLine1.getCrossSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        FlexLine flexLine2 = flexLines.get(1);
        // The right should be 140 * 2 + 10 * 3 (divider)
        assertThat(flexLine2.getMainSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 310)));
        assertThat(flexLine2.getCrossSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
    }

    @Test
    @FlakyTest
    public void testDivider_directionRow_horizontalBeginning() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final Drawable divider = ResourcesCompat
                .getDrawable(activity.getResources(), R.drawable.divider, null);
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_row,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setDividerDrawableHorizontal(divider);
                        flexboxLayout.setShowDividerHorizontal(
                                FlexboxLayout.SHOW_DIVIDER_BEGINNING);
                        flexboxLayout.setShowDividerVertical(FlexboxLayout.SHOW_DIVIDER_NONE);
                    }
                }
        );

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
        List<FlexLine> flexLines = flexboxLayout.getFlexLines();
        assertThat(flexLines.size(), is(2));
        FlexLine flexLine1 = flexLines.get(0);
        // There is a horizontal divider at the beginning. Top and bottom coordinates are shifted
        // by the amount of 15
        // The right should be 90 * 3
        assertThat(flexLine1.getMainSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 270)));
        assertThat(flexLine1.getCrossSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        FlexLine flexLine2 = flexLines.get(1);
        assertThat(flexLine2.getMainSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 280)));
        assertThat(flexLine2.getCrossSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
    }

    @Test
    @FlakyTest
    public void testDivider_directionRow_horizontalMiddle() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_row,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        Drawable divider = ResourcesCompat
                                .getDrawable(activity.getResources(), R.drawable.divider, null);
                        flexboxLayout.setDividerDrawableHorizontal(divider);
                        flexboxLayout.setShowDividerHorizontal(FlexboxLayout.SHOW_DIVIDER_MIDDLE);
                        flexboxLayout.setShowDividerVertical(FlexboxLayout.SHOW_DIVIDER_NONE);
                    }
                }
        );

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
        List<FlexLine> flexLines = flexboxLayout.getFlexLines();
        assertThat(flexLines.size(), is(2));
        FlexLine flexLine1 = flexLines.get(0);
        // The right should be 90 * 3
        assertThat(flexLine1.getMainSize(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 270)));
        assertThat(flexLine1.getCrossSize(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        // There is a horizontal divider at the middle. Top and bottom coordinates are shifted
        // by the amount of 15
        FlexLine flexLine2 = flexLines.get(1);
        // The right should be 140
        assertThat(flexLine2.getMainSize(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 280)));
        assertThat(flexLine2.getCrossSize(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
    }

    @Test
    @FlakyTest
    public void testDivider_directionRow_horizontalEnd() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_row,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        Drawable divider = ResourcesCompat
                                .getDrawable(activity.getResources(), R.drawable.divider, null);
                        flexboxLayout.setDividerDrawableHorizontal(divider);
                        flexboxLayout.setShowDividerHorizontal(FlexboxLayout.SHOW_DIVIDER_END);
                        flexboxLayout.setShowDividerVertical(FlexboxLayout.SHOW_DIVIDER_NONE);
                    }
                }
        );

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
        List<FlexLine> flexLines = flexboxLayout.getFlexLines();
        assertThat(flexLines.size(), is(2));
        FlexLine flexLine1 = flexLines.get(0);
        // The right should be 90 * 3
        assertThat(flexLine1.getMainSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 270)));
        assertThat(flexLine1.getCrossSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        // There is a horizontal divider at the middle. Top and bottom coordinates are shifted
        // by the amount of 15
        FlexLine flexLine2 = flexLines.get(1);
        // The right should be 140
        assertThat(flexLine2.getMainSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 280)));
        assertThat(flexLine2.getCrossSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
    }

    @Test
    @FlakyTest
    public void testDivider_directionRow_horizontalAll() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_row,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        Drawable divider = ResourcesCompat
                                .getDrawable(activity.getResources(), R.drawable.divider, null);
                        flexboxLayout.setDividerDrawableHorizontal(divider);
                        flexboxLayout.setShowDividerHorizontal(
                                FlexboxLayout.SHOW_DIVIDER_BEGINNING
                                        | FlexboxLayout.SHOW_DIVIDER_MIDDLE
                                        | FlexboxLayout.SHOW_DIVIDER_END);
                        flexboxLayout.setShowDividerVertical(FlexboxLayout.SHOW_DIVIDER_NONE);
                    }
                }
        );

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
    @FlakyTest
    public void testDivider_directionRow_all_thickDivider() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_row,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        Drawable thickDivider = ResourcesCompat
                                .getDrawable(activity.getResources(), R.drawable.divider_thick,
                                        null);
                        flexboxLayout.setDividerDrawableVertical(thickDivider);
                        flexboxLayout.setShowDividerVertical(
                                FlexboxLayout.SHOW_DIVIDER_BEGINNING
                                        | FlexboxLayout.SHOW_DIVIDER_MIDDLE
                                        | FlexboxLayout.SHOW_DIVIDER_END);
                    }
                }
        );

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
    @FlakyTest
    public void testDivider_directionColumn_horizontalBeginning() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_column);

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
        List<FlexLine> flexLines = flexboxLayout.getFlexLines();
        assertThat(flexLines.size(), is(2));
        FlexLine flexLine1 = flexLines.get(0);
        assertThat(flexLine1.getCrossSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        // The bottom should be 90 * 3 + 15
        assertThat(flexLine1.getMainSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 285)));
        FlexLine flexLine2 = flexLines.get(1);
        assertThat(flexLine2.getCrossSize(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        // The bottom should be 140 * 2 + 15
        assertThat(flexLine2.getMainSize(), isEqualAllowingError(TestUtil.dpToPixel(activity, 295)));
    }

    @Test
    @FlakyTest
    public void testDivider_directionColumn_horizontalMiddle() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_column,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setShowDividerHorizontal(FlexboxLayout.SHOW_DIVIDER_MIDDLE);
                    }
                }
        );

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
        List<FlexLine> flexLines = flexboxLayout.getFlexLines();
        assertThat(flexLines.size(), is(2));
        FlexLine flexLine1 = flexLines.get(0);
        assertThat(flexLine1.getCrossSize(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        // The bottom should be 90 * 3 + 15 * 2
        assertThat(flexLine1.getMainSize(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 300)));
        FlexLine flexLine2 = flexLines.get(1);
        assertThat(flexLine2.getCrossSize(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        // The bottom should be 140 * 2 + 15
        assertThat(flexLine2.getMainSize(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 295)));
    }

    @Test
    @FlakyTest
    public void testDivider_directionColumn_horizontalEnd() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_column,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setShowDividerHorizontal(FlexboxLayout.SHOW_DIVIDER_END);
                    }
                }
        );

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
        List<FlexLine> flexLines = flexboxLayout.getFlexLines();
        assertThat(flexLines.size(), is(2));
        FlexLine flexLine1 = flexLines.get(0);
        assertThat(flexLine1.getCrossSize(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        // The bottom should be 90 * 3 + 15
        assertThat(flexLine1.getMainSize(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 285)));
        FlexLine flexLine2 = flexLines.get(1);
        assertThat(flexLine2.getCrossSize(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        // The bottom should be 140 * 2 + 15
        assertThat(flexLine2.getMainSize(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 295)));
    }

    @Test
    @FlakyTest
    public void testDivider_directionColumn_horizontalAll() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_column,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setShowDividerHorizontal(
                                FlexboxLayout.SHOW_DIVIDER_BEGINNING
                                        | FlexboxLayout.SHOW_DIVIDER_MIDDLE
                                        | FlexboxLayout.SHOW_DIVIDER_END);
                    }
                }
        );

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
        List<FlexLine> flexLines = flexboxLayout.getFlexLines();
        assertThat(flexLines.size(), is(2));
        FlexLine flexLine1 = flexLines.get(0);
        assertThat(flexLine1.getCrossSize(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        // The bottom should be 90 * 3 + 15 * 4
        assertThat(flexLine1.getMainSize(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 330)));
        FlexLine flexLine2 = flexLines.get(1);
        assertThat(flexLine2.getCrossSize(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        // The bottom should be 140 * 2 + 15 * 3
        assertThat(flexLine2.getMainSize(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 325)));
    }

    @Test
    @FlakyTest
    public void testDivider_directionColumn_verticalBeginning() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_column,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        Drawable divider = ResourcesCompat
                                .getDrawable(activity.getResources(), R.drawable.divider, null);
                        flexboxLayout.setDividerDrawableVertical(divider);
                        flexboxLayout.setShowDividerVertical(FlexboxLayout.SHOW_DIVIDER_BEGINNING);
                        flexboxLayout.setShowDividerHorizontal(FlexboxLayout.SHOW_DIVIDER_NONE);
                    }
                });

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
    @FlakyTest
    public void testDivider_directionColumn_verticalMiddle() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_column,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        Drawable divider = ResourcesCompat
                                .getDrawable(activity.getResources(), R.drawable.divider, null);
                        flexboxLayout.setDividerDrawableVertical(divider);
                        flexboxLayout.setShowDividerVertical(FlexboxLayout.SHOW_DIVIDER_MIDDLE);
                        flexboxLayout.setShowDividerHorizontal(FlexboxLayout.SHOW_DIVIDER_NONE);
                    }
                });

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
    @FlakyTest
    public void testDivider_directionColumn_verticalEnd() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_column,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        Drawable divider = ResourcesCompat
                                .getDrawable(activity.getResources(), R.drawable.divider, null);
                        flexboxLayout.setDividerDrawableHorizontal(divider);
                        flexboxLayout.setShowDividerVertical(FlexboxLayout.SHOW_DIVIDER_END);
                        flexboxLayout.setShowDividerHorizontal(FlexboxLayout.SHOW_DIVIDER_NONE);
                    }
                });

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
    @FlakyTest
    public void testDivider_directionColumn_verticalAll() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_column,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        Drawable divider = ResourcesCompat
                                .getDrawable(activity.getResources(), R.drawable.divider, null);
                        flexboxLayout.setDividerDrawableVertical(divider);
                        flexboxLayout.setShowDividerVertical(
                                FlexboxLayout.SHOW_DIVIDER_BEGINNING
                                        | FlexboxLayout.SHOW_DIVIDER_MIDDLE
                                        | FlexboxLayout.SHOW_DIVIDER_END);
                        flexboxLayout.setShowDividerHorizontal(FlexboxLayout.SHOW_DIVIDER_NONE);
                    }
                });

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
    @FlakyTest
    public void testDivider_directionColumn_vertical_horizontal_All() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_column,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        Drawable divider = ResourcesCompat
                                .getDrawable(activity.getResources(), R.drawable.divider, null);
                        flexboxLayout.setDividerDrawable(divider);
                        flexboxLayout.setShowDivider(
                                FlexboxLayout.SHOW_DIVIDER_BEGINNING
                                        | FlexboxLayout.SHOW_DIVIDER_MIDDLE
                                        | FlexboxLayout.SHOW_DIVIDER_END);
                    }
                });

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
    @FlakyTest
    public void testDivider_directionColumn_all_thickDivider() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_column,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        Drawable thickDivider = ResourcesCompat
                                .getDrawable(activity.getResources(), R.drawable.divider_thick,
                                        null);
                        flexboxLayout.setDividerDrawableHorizontal(thickDivider);
                        flexboxLayout.setShowDividerHorizontal(
                                FlexboxLayout.SHOW_DIVIDER_BEGINNING
                                        | FlexboxLayout.SHOW_DIVIDER_MIDDLE
                                        | FlexboxLayout.SHOW_DIVIDER_END);
                    }
                });

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

    @Test
    @FlakyTest
    public void testZeroWidth_wrapContentHeight_positiveFlexGrow() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_zero_width_positive_flexgrow);

        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        // Both text view's layout_width is set to 0dp, layout_height is set to wrap_content and
        // layout_flexGrow is set to 1. And the text2 has a longer text than the text1.
        // So if the cross size calculation (height) is wrong, the height of two text view do not
        // match because text2 is trying to expand vertically.
        // This assertion verifies that isn't happening. Finally both text views expand horizontally
        // enough to contain their texts in one line.
        assertThat(text1.getHeight(), is(text2.getHeight()));
        assertThat(text1.getWidth() + text2.getWidth(),
                isEqualAllowingError(flexboxLayout.getWidth()));
    }

    @Test
    @FlakyTest
    public void testZeroHeight_wrapContentWidth_positiveFlexGrow() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_zero_height_positive_flexgrow);

        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        TextView text2 = (TextView) activity.findViewById(R.id.text2);
        assertThat(text1.getWidth(), is(not(text2.getWidth())));
        assertThat(text1.getHeight() + text2.getHeight(),
                isEqualAllowingError(flexboxLayout.getHeight()));
    }

    @Test
    @FlakyTest
    public void testFlexDirection_row_alignItems_center_margin_oneSide() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_direction_row_align_items_center_margin_oneside);

        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        assertThat(text1.getTop(), isEqualAllowingError(TestUtil.dpToPixel(activity, 30)));
        assertThat(flexboxLayout.getBottom() - text1.getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 50)));
    }

    @Test
    @FlakyTest
    public void testFlexDirection_column_alignItems_center_margin_oneSide() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_direction_column_align_items_center_margin_oneside);

        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));

        TextView text1 = (TextView) activity.findViewById(R.id.text1);
        assertThat(text1.getLeft(), isEqualAllowingError(TestUtil.dpToPixel(activity, 30)));
        assertThat(flexboxLayout.getRight() - text1.getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 50)));
    }

    @Test
    @FlakyTest
    public void testChildBottomMarginIncluded_flexContainerWrapContent_directionRow_flexGrow()
            throws Throwable {
        // This test is to verify the case where:
        //   - layout_height is set to wrap_content for the FlexboxLayout
        //   - Bottom (or top) margin is set to a child
        //   - The child which the has the bottom (top) margin has the largest height in the
        //     same flex line (or only a single child exists)
        //   - The child has a positive layout_flexGrow attribute set
        //  If these conditions were met, the height of the FlexboxLayout didn't take the bottom
        //  margin on the child into account
        //  See https://github.com/google/flexbox-layout/issues/154
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_wrap_content_child_bottom_margin_row_grow);

        // layout_height for text1: 24dp, layout_marginBottom: 12dp
        assertThat(flexboxLayout.getHeight(),
                isEqualAllowingError(TestUtil.dpToPixel(mActivityRule.getActivity(), 36)));
    }

    @Test
    @FlakyTest
    public void testChildEndMarginIncluded_flexContainerWrapContent_directionColumn_flexGrow()
            throws Throwable {
        // This test is to verify the case where:
        //   - layout_width is set to wrap_content for the FlexboxLayout
        //   - End (or start) margin is set to a child
        //   - The child which the has the end (start) margin has the largest width in the
        //     same flex line (or only a single child exists)
        //   - The child has a positive layout_flexGrow attribute set
        //  If these conditions were met, the width of the FlexboxLayout didn't take the bottom
        //  margin on the child into account
        //  See https://github.com/google/flexbox-layout/issues/154
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_wrap_content_child_bottom_margin_column_grow);

        // layout_width for text1: 24dp, layout_marginEnd: 12dp
        assertThat(flexboxLayout.getWidth(),
                isEqualAllowingError(TestUtil.dpToPixel(mActivityRule.getActivity(), 36)));
    }

    @Test
    @FlakyTest
    public void testChildBottomMarginIncluded_flexContainerWrapContent_directionRow_flexShrink()
            throws Throwable {
        // This test is to verify the case where:
        //   - layout_height is set to wrap_content for the FlexboxLayout
        //   - flex_wrap is set to nowrap for the FlexboxLayout
        //   - Bottom (or top) margin is set to a child
        //   - The child which the has the bottom (top) margin has the largest height in the
        //     same flex line
        //   - The child has a positive layout_flexShrink attribute set
        //   - The sum of children width overflows parent's width (shrink will happen)
        //  If these conditions were met, the height of the FlexboxLayout didn't take the bottom
        //  margin on the child into account
        //  See https://github.com/google/flexbox-layout/issues/154
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_wrap_content_child_bottom_margin_row_shrink);

        // layout_height for text1: 24dp, layout_marginBottom: 12dp
        assertThat(flexboxLayout.getHeight(),
                isEqualAllowingError(TestUtil.dpToPixel(mActivityRule.getActivity(), 36)));
    }

    @Test
    @FlakyTest
    public void testChildBottomMarginIncluded_flexContainerWrapContent_directionColumn_flexShrink()
            throws Throwable {
        // This test is to verify the case where:
        //   - layout_width is set to wrap_content for the FlexboxLayout
        //   - flex_wrap is set to nowrap for the FlexboxLayout
        //   - End (or start) margin is set to a child
        //   - The child which the has the end (start) margin has the largest width in the
        //     same flex line
        //   - The child has a positive layout_flexShrink attribute set
        //   - The sum of children height overflows parent's height (shrink will happen)
        //  If these conditions were met, the height of the FlexboxLayout didn't take the bottom
        //  margin on the child into account
        //  See https://github.com/google/flexbox-layout/issues/154
        FlexboxLayout flexboxLayout = createFlexboxLayout(
                R.layout.activity_wrap_content_child_bottom_margin_column_shrink);

        // layout_width for text1: 24dp, layout_marginEnd: 12dp
        assertThat(flexboxLayout.getWidth(),
                isEqualAllowingError(TestUtil.dpToPixel(mActivityRule.getActivity(), 36)));
    }

    @Test
    @FlakyTest
    public void testChildNeedsRemeasure_row() throws Throwable {
        createFlexboxLayout(R.layout.activity_child_needs_remeasure_row);
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
    }

    @Test
    @FlakyTest
    public void testChildNeedsRemeasure_column() throws Throwable {
        createFlexboxLayout(R.layout.activity_child_needs_remeasure_column);
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
    }

    @Test
    @FlakyTest
    public void testFirstViewGone_firstLineSingleItem_row() throws Throwable {
        // This test verifies the case where the first view's visibility is gone and the second
        // view is in the next flex line. In that case, the second view's position is misplaced.
        // https://github.com/google/flexbox-layout/issues/283
        createFlexboxLayout(R.layout.activity_first_view_gone_first_line_single_item);
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
    }

    @Test
    @FlakyTest
    public void testFirstViewGone_firstLineSingleItem_column() throws Throwable {
        // This test verifies the case where the first view's visibility is gone and the second
        // view is in the next flex line. In that case, the second view's position is misplaced.
        // https://github.com/google/flexbox-layout/issues/283
        createFlexboxLayout(R.layout.activity_first_view_gone_first_line_single_item,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
    }

    @Test
    @FlakyTest
    public void testFirstViewGone_flexGrowSetForRestOfItems_row() throws Throwable {
        // This test verifies the case where the first view's visibility is gone and the second
        // view and third view have the layout_flexGrow attribute set. In that case, the second
        // view's position is misplaced and the third view becomes invisible .
        // https://github.com/google/flexbox-layout/issues/303
        createFlexboxLayout(R.layout.activity_first_view_gone_layout_grow_set_for_rest);
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)));
    }

    @Test
    @FlakyTest
    public void testFirstViewGone_flexGrowSetForRestOfItems_column() throws Throwable {
        // This test verifies the case where the first view's visibility is gone and the second
        // view and third view have the layout_flexGrow attribute set. In that case, the second
        // view's position is misplaced and the third view becomes invisible .
        // https://github.com/google/flexbox-layout/issues/303
        createFlexboxLayout(R.layout.activity_first_view_gone_layout_grow_set_for_rest,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)));
    }

    @Test
    @FlakyTest
    public void testFirstViewGone_flexShrinkSetForRestOfItems_row() throws Throwable {
        createFlexboxLayout(R.layout.activity_first_view_gone_layout_shrink_set_for_rest);
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
    }

    @Test
    @FlakyTest
    public void testFirstViewGone_flexShrinkSetForRestOfItems_column() throws Throwable {
        createFlexboxLayout(R.layout.activity_first_view_gone_layout_shrink_set_for_rest,
                new Configuration() {
                    @Override
                    public void apply(FlexboxLayout flexboxLayout) {
                        flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
                    }
                });
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)));
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)));
    }

    private FlexboxLayout createFlexboxLayout(@LayoutRes final int activityLayoutResId)
            throws Throwable {
        return createFlexboxLayout(activityLayoutResId, Configuration.EMPTY);
    }

    private FlexboxLayout createFlexboxLayout(@LayoutRes final int activityLayoutResId,
            final Configuration configuration) throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(activityLayoutResId);
                FlexboxLayout flexboxLayout = (FlexboxLayout) activity
                        .findViewById(R.id.flexbox_layout);
                configuration.apply(flexboxLayout);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        return (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
    }

    private static TextView createTextView(Context context, String text, int order) {
        TextView textView = new TextView(context);
        textView.setText(text);
        FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setOrder(order);
        textView.setLayoutParams(lp);
        return textView;
    }

    private static ViewAssertion hasWidth(final int width) {
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

    private static ViewAssertion hasHeight(final int height) {
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

    private interface Configuration {

        Configuration EMPTY = new Configuration() {
            @Override
            public void apply(FlexboxLayout flexboxLayout) {
            }
        };

        void apply(FlexboxLayout flexboxLayout);
    }
}
