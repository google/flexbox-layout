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
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import static com.google.android.flexbox.FlexboxItemDecoration.HORIZONTAL;
import static com.google.android.flexbox.FlexboxItemDecoration.VERTICAL;
import static com.google.android.flexbox.test.IsEqualAllowingError.isEqualAllowingError;

import static junit.framework.Assert.assertTrue;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
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
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.AlignSelf;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxItemDecoration;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration tests for {@link FlexboxLayoutManager}.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class FlexboxLayoutManagerTest {

    @Rule
    public ActivityTestRule<FlexboxTestActivity> mActivityRule =
            new ActivityTestRule<>(FlexboxTestActivity.class);

    @Test
    @FlakyTest
    public void testLoadFromXml() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview_reverse);
            }
        });
        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        assertThat(recyclerView, is(notNullValue()));
        assertThat(layoutManager, is(instanceOf(FlexboxLayoutManager.class)));
        FlexboxLayoutManager flexboxLayoutManager = (FlexboxLayoutManager) layoutManager;
        assertThat(flexboxLayoutManager.getFlexDirection(), is(FlexDirection.ROW_REVERSE));
        assertThat(flexboxLayoutManager.getFlexWrap(), is(FlexWrap.WRAP));
    }

    @Test
    @FlakyTest
    public void testChangeAttributesFromCode() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview_reverse);
            }
        });
        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        assertThat(recyclerView, is(notNullValue()));
        assertThat(layoutManager, is(instanceOf(FlexboxLayoutManager.class)));
        FlexboxLayoutManager flexboxLayoutManager = (FlexboxLayoutManager) layoutManager;
        assertThat(flexboxLayoutManager.getFlexDirection(), is(FlexDirection.ROW_REVERSE));
        assertThat(flexboxLayoutManager.getFlexWrap(), is(FlexWrap.WRAP));

        flexboxLayoutManager.setFlexDirection(FlexDirection.COLUMN);
        flexboxLayoutManager.setFlexWrap(FlexWrap.NOWRAP);
        flexboxLayoutManager.setJustifyContent(JustifyContent.CENTER);
        flexboxLayoutManager.setAlignItems(AlignItems.FLEX_END);
        assertThat(flexboxLayoutManager.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(flexboxLayoutManager.getFlexWrap(), is(FlexWrap.NOWRAP));
        assertThat(flexboxLayoutManager.getJustifyContent(), is(JustifyContent.CENTER));
        assertThat(flexboxLayoutManager.getAlignItems(), is(AlignItems.FLEX_END));
    }

    @Test
    @FlakyTest
    public void testAddViewHolders_direction_row_not_wrapped() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                adapter.addItem(createLayoutParams(activity, 120, 80));
                adapter.addItem(createLayoutParams(activity, 120, 80));
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW));
        assertThat(layoutManager.getFlexWrap(), is(FlexWrap.WRAP));
        // Only 2 items are added and all items should be attached (visible). So both
        // getChildCount and getFlexItemCount(including detached items) should return the same value
        assertThat(layoutManager.getFlexItemCount(), is(2));
        assertThat(layoutManager.getChildCount(), is(2));
        assertThat(layoutManager.getFlexLines().size(), is(1));
    }

    @Test
    @FlakyTest
    public void testAddViewHolders_direction_row_wrapped() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                adapter.addItem(createLayoutParams(activity, 120, 80));
                adapter.addItem(createLayoutParams(activity, 120, 80));
                adapter.addItem(createLayoutParams(activity, 120, 80));
                adapter.addItem(createLayoutParams(activity, 120, 80));
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // 4 items are added and all items should be attached (visible). So both
        // getChildCount and getFlexItemCount(including detached items) should return the same value
        assertThat(layoutManager.getFlexItemCount(), is(4));
        assertThat(layoutManager.getChildCount(), is(4));
        assertThat(layoutManager.getFlexLines().size(), is(2));
    }

    @Test
    @FlakyTest
    public void testAddViewHolders_direction_row_partOfItems_detached() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                adapter.addItem(createLayoutParams(activity, 150, 90));
                adapter.addItem(createLayoutParams(activity, 150, 90));
                adapter.addItem(createLayoutParams(activity, 150, 90));
                adapter.addItem(createLayoutParams(activity, 150, 90));
                adapter.addItem(createLayoutParams(activity, 150, 90));
                adapter.addItem(createLayoutParams(activity, 150, 90));
                adapter.addItem(createLayoutParams(activity, 150, 90));
                adapter.addItem(createLayoutParams(activity, 150, 90));
                adapter.addItem(createLayoutParams(activity, 150, 90));
                // RecyclerView width: 320, height: 240.
                // Computed FlexContainer width: 320, height: 450 
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // In total 9 items are added but the seventh item and the items follow aren't attached
        // (invisible) so are not included in the count of of the getChildCount.
        assertThat(layoutManager.getFlexItemCount(), is(9));
        assertThat(layoutManager.getChildCount(), is(6));

        // At first only the visible area of the flex lines are calculated
        assertThat(layoutManager.getFlexLines().size(), is(3));
    }

    @Test
    @FlakyTest
    public void testAddViewHolders_direction_row_scrollVertically() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                adapter.addItem(createLayoutParams(activity, 150, 90));
                adapter.addItem(createLayoutParams(activity, 150, 90));
                adapter.addItem(createLayoutParams(activity, 150, 90));
                adapter.addItem(createLayoutParams(activity, 150, 90));
                adapter.addItem(createLayoutParams(activity, 150, 90));
                adapter.addItem(createLayoutParams(activity, 150, 90));
                adapter.addItem(createLayoutParams(activity, 150, 90));
                adapter.addItem(createLayoutParams(activity, 150, 90));
                adapter.addItem(createLayoutParams(activity, 150, 90));
                // RecyclerView width: 320, height: 240.
                // Computed FlexContainer width: 320, height: 450 
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // In total 9 items are added but the seventh item and the items follow aren't attached
        // (invisible) so are not included in the count of of the getChildCount.
        assertThat(layoutManager.getFlexItemCount(), is(9));
        assertThat(layoutManager.getChildCount(), is(6));
        // At first only the visible area of the flex lines are calculated
        assertThat(layoutManager.getFlexLines().size(), is(3));

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        assertThat(layoutManager.getFlexItemCount(), is(9));
        // The RecyclerView is swiped to top until it reaches the bottom of the view.
        // The number of the visible views should be 5
        assertThat(layoutManager.getChildCount(), is(5));
        // Since the RecyclerView is swiped to the bottom, all flex lines should be calculated
        // by now
        assertThat(layoutManager.getFlexLines().size(), is(5));
    }

    @Test
    @FlakyTest
    public void testFlexGrow() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 150, 130);
                lp1.setFlexGrow(1.0f);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 150, 130);
                lp2.setFlexGrow(1.0f);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 150, 130);
                lp3.setFlexGrow(1.0f);
                adapter.addItem(lp3);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // The flexGrow parameters for all LayoutParams are set to 1.0, expecting each child to
        // fill the horizontal remaining space
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(2));
        assertThat(layoutManager.getChildAt(0).getWidth(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 160)));
        assertThat(layoutManager.getChildAt(1).getWidth(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 160)));
        assertThat(layoutManager.getChildAt(2).getWidth(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 320)));
    }

    @Test
    @FlakyTest
    public void testAddViewHolders_direction_column_partOfItems_detached() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                adapter.addItem(createLayoutParams(activity, 120, 100));
                adapter.addItem(createLayoutParams(activity, 120, 100));
                adapter.addItem(createLayoutParams(activity, 120, 100));
                adapter.addItem(createLayoutParams(activity, 120, 100));
                adapter.addItem(createLayoutParams(activity, 120, 100));
                adapter.addItem(createLayoutParams(activity, 120, 100));
                adapter.addItem(createLayoutParams(activity, 120, 100));
                adapter.addItem(createLayoutParams(activity, 120, 100));
                adapter.addItem(createLayoutParams(activity, 120, 100));

                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                // RecyclerView width: 320, height: 240.
                // Computed FlexContainer width: 450, height: 240
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // In total 9 items are added but the seventh item and the items after aren't attached
        // (invisible) so are not included in the count of of the getChildCount.
        assertThat(layoutManager.getFlexItemCount(), is(9));
        assertThat(layoutManager.getChildCount(), is(6));

        // At first only the visible area of the flex lines are calculated
        assertThat(layoutManager.getFlexLines().size(), is(3));
    }

    @Test
    @FlakyTest
    public void testAddViewHolders_direction_column_scrollHorizontally() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                adapter.addItem(createLayoutParams(activity, 120, 100));
                adapter.addItem(createLayoutParams(activity, 120, 100));
                adapter.addItem(createLayoutParams(activity, 120, 100));
                adapter.addItem(createLayoutParams(activity, 120, 100));
                adapter.addItem(createLayoutParams(activity, 120, 100));
                adapter.addItem(createLayoutParams(activity, 120, 100));
                adapter.addItem(createLayoutParams(activity, 120, 100));
                adapter.addItem(createLayoutParams(activity, 120, 100));
                adapter.addItem(createLayoutParams(activity, 120, 100));

                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                // RecyclerView width: 320, height: 240.
                // Computed FlexContainer width: 500, height: 240
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // In total 9 items are added but the seventh item and the items follow aren't attached
        // (invisible) so are not included in the count of of the getChildCount.
        assertThat(layoutManager.getFlexItemCount(), is(9));
        assertThat(layoutManager.getChildCount(), is(6));
        // At first only the visible area of the flex lines are calculated
        assertThat(layoutManager.getFlexLines().size(), is(3));

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        assertThat(layoutManager.getFlexItemCount(), is(9));
        // The RecyclerView is swiped to top until it reaches the right edge of the view.
        // The number of the visible views should be 5
        assertThat(layoutManager.getChildCount(), is(5));
        // Since the RecyclerView is swiped to the bottom, all flex lines should be calculated
        // by now
        assertThat(layoutManager.getFlexLines().size(), is(5));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_flexStart_direction_row() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp3);
                layoutManager.setJustifyContent(JustifyContent.FLEX_START);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.FLEX_START));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 0)));
        assertThat(layoutManager.getChildAt(0).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 50)));
        assertThat(layoutManager.getChildAt(1).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 50)));
        assertThat(layoutManager.getChildAt(1).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 100)));
        assertThat(layoutManager.getChildAt(2).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 100)));
        assertThat(layoutManager.getChildAt(2).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 150)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_flexEnd_direction_row() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp3);
                layoutManager.setJustifyContent(JustifyContent.FLEX_END);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.FLEX_END));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 170)));
        assertThat(layoutManager.getChildAt(0).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 220)));
        assertThat(layoutManager.getChildAt(1).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 220)));
        assertThat(layoutManager.getChildAt(1).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 270)));
        assertThat(layoutManager.getChildAt(2).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 270)));
        assertThat(layoutManager.getChildAt(2).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 320)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_center_direction_row() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp3);
                layoutManager.setJustifyContent(JustifyContent.CENTER);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.CENTER));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 85)));
        assertThat(layoutManager.getChildAt(0).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 135)));
        assertThat(layoutManager.getChildAt(1).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 135)));
        assertThat(layoutManager.getChildAt(1).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 185)));
        assertThat(layoutManager.getChildAt(2).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 185)));
        assertThat(layoutManager.getChildAt(2).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 235)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceAround_direction_row() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp3);
                layoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.SPACE_AROUND));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 28)));
        assertThat(layoutManager.getChildAt(0).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 78)));
        assertThat(layoutManager.getChildAt(1).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 135)));
        assertThat(layoutManager.getChildAt(1).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 185)));
        assertThat(layoutManager.getChildAt(2).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 242)));
        assertThat(layoutManager.getChildAt(2).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 292)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceBetween_direction_row() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp3);
                layoutManager.setJustifyContent(JustifyContent.SPACE_BETWEEN);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.SPACE_BETWEEN));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 0)));
        assertThat(layoutManager.getChildAt(0).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 50)));
        assertThat(layoutManager.getChildAt(1).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 135)));
        assertThat(layoutManager.getChildAt(1).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 185)));
        assertThat(layoutManager.getChildAt(2).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 270)));
        assertThat(layoutManager.getChildAt(2).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 320)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_flexStart_direction_rowReverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp3);
                layoutManager.setFlexDirection(FlexDirection.ROW_REVERSE);
                layoutManager.setJustifyContent(JustifyContent.FLEX_START);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW_REVERSE));
        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.FLEX_START));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 270)));
        assertThat(layoutManager.getChildAt(0).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 320)));
        assertThat(layoutManager.getChildAt(1).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 220)));
        assertThat(layoutManager.getChildAt(1).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 270)));
        assertThat(layoutManager.getChildAt(2).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 170)));
        assertThat(layoutManager.getChildAt(2).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 220)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_flexEnd_direction_rowReverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp3);
                layoutManager.setFlexDirection(FlexDirection.ROW_REVERSE);
                layoutManager.setJustifyContent(JustifyContent.FLEX_END);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW_REVERSE));
        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.FLEX_END));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 100)));
        assertThat(layoutManager.getChildAt(0).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 150)));
        assertThat(layoutManager.getChildAt(1).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 50)));
        assertThat(layoutManager.getChildAt(1).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 100)));
        assertThat(layoutManager.getChildAt(2).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 0)));
        assertThat(layoutManager.getChildAt(2).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 50)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_center_direction_rowReverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp3);
                layoutManager.setFlexDirection(FlexDirection.ROW_REVERSE);
                layoutManager.setJustifyContent(JustifyContent.CENTER);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW_REVERSE));
        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.CENTER));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 185)));
        assertThat(layoutManager.getChildAt(0).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 235)));
        assertThat(layoutManager.getChildAt(1).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 135)));
        assertThat(layoutManager.getChildAt(1).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 185)));
        assertThat(layoutManager.getChildAt(2).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 85)));
        assertThat(layoutManager.getChildAt(2).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 135)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceAround_direction_rowReverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp3);
                layoutManager.setFlexDirection(FlexDirection.ROW_REVERSE);
                layoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW_REVERSE));
        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.SPACE_AROUND));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 242)));
        assertThat(layoutManager.getChildAt(0).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 292)));
        assertThat(layoutManager.getChildAt(1).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 135)));
        assertThat(layoutManager.getChildAt(1).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 185)));
        assertThat(layoutManager.getChildAt(2).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 28)));
        assertThat(layoutManager.getChildAt(2).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 78)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceBetween_direction_rowReverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 50, 100);
                adapter.addItem(lp3);
                layoutManager.setFlexDirection(FlexDirection.ROW_REVERSE);
                layoutManager.setJustifyContent(JustifyContent.SPACE_BETWEEN);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW_REVERSE));
        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.SPACE_BETWEEN));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 270)));
        assertThat(layoutManager.getChildAt(0).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 320)));
        assertThat(layoutManager.getChildAt(1).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 135)));
        assertThat(layoutManager.getChildAt(1).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 185)));
        assertThat(layoutManager.getChildAt(2).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 0)));
        assertThat(layoutManager.getChildAt(2).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 50)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_flexStart_direction_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp3);
                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                layoutManager.setJustifyContent(JustifyContent.FLEX_START);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.FLEX_START));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 0)));
        assertThat(layoutManager.getChildAt(0).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 50)));
        assertThat(layoutManager.getChildAt(1).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 50)));
        assertThat(layoutManager.getChildAt(1).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 100)));
        assertThat(layoutManager.getChildAt(2).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 100)));
        assertThat(layoutManager.getChildAt(2).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 150)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_flexEnd_direction_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp3);
                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                layoutManager.setJustifyContent(JustifyContent.FLEX_END);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.FLEX_END));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 90)));
        assertThat(layoutManager.getChildAt(0).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 140)));
        assertThat(layoutManager.getChildAt(1).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 140)));
        assertThat(layoutManager.getChildAt(1).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 190)));
        assertThat(layoutManager.getChildAt(2).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 190)));
        assertThat(layoutManager.getChildAt(2).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 240)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_center_direction_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp3);
                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                layoutManager.setJustifyContent(JustifyContent.CENTER);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.CENTER));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 45)));
        assertThat(layoutManager.getChildAt(0).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 95)));
        assertThat(layoutManager.getChildAt(1).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 95)));
        assertThat(layoutManager.getChildAt(1).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 145)));
        assertThat(layoutManager.getChildAt(2).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 145)));
        assertThat(layoutManager.getChildAt(2).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 195)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceAround_direction_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp3);
                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                layoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.SPACE_AROUND));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 15)));
        assertThat(layoutManager.getChildAt(0).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 65)));
        assertThat(layoutManager.getChildAt(1).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 95)));
        assertThat(layoutManager.getChildAt(1).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 145)));
        assertThat(layoutManager.getChildAt(2).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 175)));
        assertThat(layoutManager.getChildAt(2).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 225)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceBetween_direction_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp3);
                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                layoutManager.setJustifyContent(JustifyContent.SPACE_BETWEEN);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.SPACE_BETWEEN));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 0)));
        assertThat(layoutManager.getChildAt(0).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 50)));
        assertThat(layoutManager.getChildAt(1).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 95)));
        assertThat(layoutManager.getChildAt(1).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 145)));
        assertThat(layoutManager.getChildAt(2).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 190)));
        assertThat(layoutManager.getChildAt(2).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 240)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_flexStart_direction_columnReverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp3);
                layoutManager.setFlexDirection(FlexDirection.COLUMN_REVERSE);
                layoutManager.setJustifyContent(JustifyContent.FLEX_START);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN_REVERSE));
        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.FLEX_START));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 190)));
        assertThat(layoutManager.getChildAt(0).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 240)));
        assertThat(layoutManager.getChildAt(1).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 140)));
        assertThat(layoutManager.getChildAt(1).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 190)));
        assertThat(layoutManager.getChildAt(2).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 90)));
        assertThat(layoutManager.getChildAt(2).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 140)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_flexEnd_direction_columnReverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp3);
                layoutManager.setFlexDirection(FlexDirection.COLUMN_REVERSE);
                layoutManager.setJustifyContent(JustifyContent.FLEX_END);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN_REVERSE));
        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.FLEX_END));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 100)));
        assertThat(layoutManager.getChildAt(0).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 150)));
        assertThat(layoutManager.getChildAt(1).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 50)));
        assertThat(layoutManager.getChildAt(1).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 100)));
        assertThat(layoutManager.getChildAt(2).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 0)));
        assertThat(layoutManager.getChildAt(2).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 50)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_center_direction_columnReverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp3);
                layoutManager.setFlexDirection(FlexDirection.COLUMN_REVERSE);
                layoutManager.setJustifyContent(JustifyContent.CENTER);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN_REVERSE));
        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.CENTER));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 145)));
        assertThat(layoutManager.getChildAt(0).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 195)));
        assertThat(layoutManager.getChildAt(1).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 95)));
        assertThat(layoutManager.getChildAt(1).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 145)));
        assertThat(layoutManager.getChildAt(2).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 45)));
        assertThat(layoutManager.getChildAt(2).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 95)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceAround_direction_columnReverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp3);
                layoutManager.setFlexDirection(FlexDirection.COLUMN_REVERSE);
                layoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN_REVERSE));
        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.SPACE_AROUND));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 175)));
        assertThat(layoutManager.getChildAt(0).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 225)));
        assertThat(layoutManager.getChildAt(1).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 95)));
        assertThat(layoutManager.getChildAt(1).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 145)));
        assertThat(layoutManager.getChildAt(2).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 15)));
        assertThat(layoutManager.getChildAt(2).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 65)));
    }

    @Test
    @FlakyTest
    public void testJustifyContent_spaceBetween_direction_columnReverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 100, 50);
                adapter.addItem(lp3);
                layoutManager.setFlexDirection(FlexDirection.COLUMN_REVERSE);
                layoutManager.setJustifyContent(JustifyContent.SPACE_BETWEEN);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN_REVERSE));
        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.SPACE_BETWEEN));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 190)));
        assertThat(layoutManager.getChildAt(0).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 240)));
        assertThat(layoutManager.getChildAt(1).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 95)));
        assertThat(layoutManager.getChildAt(1).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 145)));
        assertThat(layoutManager.getChildAt(2).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 0)));
        assertThat(layoutManager.getChildAt(2).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 50)));
    }

    @Test
    @FlakyTest
    public void testLargeItem_scrollFast_direction_row() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                for (int i = 0; i < 200; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 100, 50);
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW));
        assertThat(layoutManager.getFlexItemCount(), is(200));
        // Only the visible items
        assertThat(layoutManager.getChildCount(), is(not(200)));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        // Should be scrolled to the bottom by now
        assertThat(layoutManager.getFlexItemCount(), is(200));
        // Only the visible items
        assertThat(layoutManager.getChildCount(), is(not(200)));

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        // Should be scrolled to the top
        assertThat(layoutManager.getFlexItemCount(), is(200));
        // Only the visible items
        assertThat(layoutManager.getChildCount(), is(not(200)));
    }

    @Test
    @FlakyTest
    public void testLargeItem_scrollFast_direction_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                for (int i = 0; i < 200; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 70, 80);
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(layoutManager.getFlexItemCount(), is(200));
        // Only the visible items
        assertThat(layoutManager.getChildCount(), is(not(200)));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));

        // Should be scrolled to the right edge by now
        assertThat(layoutManager.getFlexItemCount(), is(200));
        // Only the visible items
        assertThat(layoutManager.getChildCount(), is(not(200)));

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT));
        // Should be scrolled to the left edge by now
        assertThat(layoutManager.getFlexItemCount(), is(200));
        // Only the visible items
        assertThat(layoutManager.getChildCount(), is(not(200)));
    }

    @Test
    @FlakyTest
    public void testAlignItems_stretch_direction_row() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                layoutManager.setAlignItems(AlignItems.STRETCH);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 70, 80);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 70, 50);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 70, 30);
                adapter.addItem(lp3);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW));
        assertThat(layoutManager.getAlignItems(), is(AlignItems.STRETCH));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        // Verify all items heights are stretched
        assertThat(layoutManager.getChildAt(0).getHeight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        assertThat(layoutManager.getChildAt(1).getHeight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        assertThat(layoutManager.getChildAt(2).getHeight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
    }

    @Test
    @FlakyTest
    public void testAlignItems_stretch_direction_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                layoutManager.setAlignItems(AlignItems.STRETCH);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 80, 70);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 50, 70);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 30, 70);
                adapter.addItem(lp3);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(layoutManager.getAlignItems(), is(AlignItems.STRETCH));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        // Verify all items widths are stretched
        assertThat(layoutManager.getChildAt(0).getWidth(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        assertThat(layoutManager.getChildAt(1).getWidth(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        assertThat(layoutManager.getChildAt(2).getWidth(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
    }

    @Test
    @FlakyTest
    public void testAlignSelf_stretch_direction_row() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                layoutManager.setAlignItems(AlignItems.FLEX_START);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 70, 80);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 70, 50);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 70, 30);
                lp3.setAlignSelf(AlignSelf.STRETCH);
                adapter.addItem(lp3);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW));
        assertThat(layoutManager.getAlignItems(), is(AlignItems.FLEX_START));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        // Verify the item whose align self is set to stretch is stretched
        assertThat(layoutManager.getChildAt(0).getHeight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        assertThat(layoutManager.getChildAt(1).getHeight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 50)));
        assertThat(layoutManager.getChildAt(2).getHeight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
    }

    @Test
    @FlakyTest
    public void testAlignSelf_stretch_direction_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                layoutManager.setAlignItems(AlignItems.FLEX_START);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 80, 70);
                adapter.addItem(lp1);
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 50, 70);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 30, 70);
                lp3.setAlignSelf(AlignSelf.STRETCH);
                adapter.addItem(lp3);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(layoutManager.getAlignItems(), is(AlignItems.FLEX_START));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        // Verify the item whose align self is set to stretch is stretched
        assertThat(layoutManager.getChildAt(0).getWidth(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
        assertThat(layoutManager.getChildAt(1).getWidth(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 50)));
        assertThat(layoutManager.getChildAt(2).getWidth(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
    }

    @Test
    @FlakyTest
    public void testStretchViews_from_middle_direction_row() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                layoutManager.setAlignItems(AlignItems.STRETCH);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                for (int i = 0; i < 50; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 70, 80);
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW));
        assertThat(layoutManager.getAlignItems(), is(AlignItems.STRETCH));

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layoutManager.setAlignItems(AlignItems.STRETCH);
                FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 70, 20);
                // Add an item whose height is less than the other items.
                // But with alignItems set to stretch, the height of the item should be stretched
                adapter.addItem(lp);
                adapter.notifyDataSetChanged();
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getChildAt(layoutManager.getChildCount() - 1).getHeight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80)));
    }

    @Test
    @FlakyTest
    public void testStretchViews_from_middle_direction_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                layoutManager.setAlignItems(AlignItems.STRETCH);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                for (int i = 0; i < 50; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 70, 50);
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(layoutManager.getAlignItems(), is(AlignItems.STRETCH));

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_RIGHT,
                GeneralLocation.TOP_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_RIGHT,
                GeneralLocation.TOP_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_RIGHT,
                GeneralLocation.TOP_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_RIGHT,
                GeneralLocation.TOP_LEFT));
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layoutManager.setAlignItems(AlignItems.STRETCH);
                FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 20, 50);
                // Add an item whose width is less than the other items.
                // But with alignItems set to stretch, the width of the item should be stretched
                adapter.addItem(lp);
                adapter.notifyDataSetChanged();
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getChildAt(layoutManager.getChildCount() - 1).getWidth(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 70)));
    }

    @Test
    @FlakyTest
    public void testScrollToTop_middleItem_as_anchorPosition() throws Throwable {
        // There was an issue that the anchor position was based on the first item in the first
        // visible flex line when scrolling to top. But the anchor position should be based on the
        // flex line position (view which has the minimum top position in the same flex line)
        // This test verifies the issue is fixed.
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        final int positionInSecondLine = 6;
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                layoutManager.setAlignItems(AlignItems.FLEX_START);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                for (int i = 0; i < 50; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 70, 80);
                    if (i == positionInSecondLine) {
                        // Change the height from other items in the second line, not the first item
                        // in the second line
                        lp = createLayoutParams(activity, 70, 130);
                    }
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
                // Each line has 4 (320 / 70) flex items and 12 (50 / 4) lines in total
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW));

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        // By this moment reached to the bottom

        // Now scrolling to the top to see if the views in the first flex line is correctly placed
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));

        assertThat(layoutManager.getChildAt(positionInSecondLine).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 210))); // 80 + 130
        // Verify that the view in the same line's cross axis position is correct
        assertThat(layoutManager.getChildAt(positionInSecondLine - 1).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 160))); // 80 + 80
    }

    @Test
    @FlakyTest
    public void testScrollToLeft_middleItem_as_anchorPosition() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        final int positionInSecondLine = 6;
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                layoutManager.setAlignItems(AlignItems.FLEX_START);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                for (int i = 0; i < 50; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 80, 50);
                    if (i == positionInSecondLine) {
                        // Change the width from other items in the second line, not the first item
                        // in the second line
                        lp = createLayoutParams(activity, 130, 50);
                    }
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
                // Each line has 4 (240 / 50) flex items and 12 (50 / 4) lines in total
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN));

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        // By this moment reached to the right edge

        // Now scrolling to the left to see if the views in the first flex line is correctly placed
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT));

        assertThat(layoutManager.getChildAt(positionInSecondLine).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 210))); // 80 + 130
        // Verify that the view in the same line's cross axis position is correct
        assertThat(layoutManager.getChildAt(positionInSecondLine - 1).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 160))); // 80 + 80
    }

    @Test
    @FlakyTest
    public void testScrollToBottom_middleItem_as_anchorPosition() throws Throwable {
        // There was an issue that the anchor position was based on the last item in the last
        // visible flex line when scrolling to bottom. But the anchor position should be based on
        // the
        // flex line position (view which has the maximum bottom position in the same flex line)
        // This test verifies the issue is fixed.
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        final int positionInSecondBottomLine = 45;
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                for (int i = 0; i < 50; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 70, 80);
                    if (i == positionInSecondBottomLine) {
                        // Change the height from other items in the second bottom line
                        lp = createLayoutParams(activity, 70, 130);
                    }
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
                // Each line has 4 (320 / 70) flex items and 12 (50 / 4) lines in total
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW));

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        // By this moment reached to the bottom

        // 4 comes from the number of flex items - positionInSecondBottomLine
        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
        View anchorView = layoutManager.getChildAt(layoutManager.getChildCount() - 4);
        assertThat(recyclerView.getBottom() - anchorView.getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 210))); // 80 + 130
    }

    @Test
    @FlakyTest
    public void testScrollToTop_direction_rowReverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.ROW_REVERSE);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                for (int i = 0; i < 50; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 70, 80);
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
                // Each line has 4 (320 / 70) flex items and 12 (50 / 4) lines in total
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW_REVERSE));

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        // By this moment reached to the bottom

        // Now scrolling to the top to see if the views in the first flex line is correctly placed
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));

        assertThat(((TextView) layoutManager.getChildAt(0)).getText().toString(), is("1"));
    }

    @Test
    @FlakyTest
    public void testFlexGrow_only_oneItem_has_positive_direction_row() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                for (int i = 0; i < 4; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 100, 80);
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
                // Flex line 1: 3 items
                // Flex line 2: 1 item
                // Give the second item in the first line a positive flex grow
                adapter.getItemAt(0).setHeight(TestUtil.dpToPixel(activity, 140));
                adapter.getItemAt(1).setFlexGrow(1.0f);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW));
        // Verify the vertical position (cross size) of the second line is correctly positioned
        assertThat(layoutManager.getChildAt(3).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 140)));
    }

    @Test
    @FlakyTest
    public void testFlexGrow_only_oneItem_has_positive_direction_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                for (int i = 0; i < 4; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 70, 70);
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
                // Flex line 1: 3 items
                // Flex line 2: 1 item
                // Give the second item in the first line a positive flex grow
                adapter.getItemAt(0).setWidth(TestUtil.dpToPixel(activity, 120));
                adapter.getItemAt(1).setFlexGrow(1.0f);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN));
        // Verify the horizontal position (cross size) of the second line is correctly positioned
        assertThat(layoutManager.getChildAt(3).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 120)));
    }

    @Test
    @FlakyTest
    public void testFirstReferenceView_middleOf_line_used_as_anchor() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                layoutManager.setAlignItems(AlignItems.FLEX_END);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 100, 80);
                adapter.addItem(lp1);
                // The second view in the first line has the maximum height in the same line
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 100, 180);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 100, 80);
                adapter.addItem(lp3);
                for (int i = 0; i < 30; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 100, 80);
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        // By this moment reached to the bottom

        // Now scrolling to the top to see if the views in the first flex line is correctly placed
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW));

        // The top coordinate of the first view should be the height of the second view minus the
        // height of the first view (180 - 80)
        assertThat(layoutManager.getChildAt(0).getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 100)));
    }

    @Test
    @FlakyTest
    public void testLastReferenceView_middleOf_line_used_as_anchor() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                layoutManager.setAlignItems(AlignItems.FLEX_START);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);

                for (int i = 0; i < 30; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 100, 80);
                    adapter.addItem(lp);
                }
                FlexboxLayoutManager.LayoutParams lp1 = createLayoutParams(activity, 100, 80);
                adapter.addItem(lp1);
                // The second view in the last line has the maximum height in the same line
                FlexboxLayoutManager.LayoutParams lp2 = createLayoutParams(activity, 100, 180);
                adapter.addItem(lp2);
                FlexboxLayoutManager.LayoutParams lp3 = createLayoutParams(activity, 100, 80);
                adapter.addItem(lp3);
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        // By this moment reached to the bottom

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW));

        // The bottom coordinate of the first view in the last line should be the height of the
        // second view in the last line minus the height of the first view in the last line
        // (180 - 80)
        assertThat(layoutManager.getChildAt(layoutManager.getChildCount() - 2).getBottom() -
                        layoutManager.getChildAt(layoutManager.getChildCount() - 3).getBottom(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 100)));
    }

    @Test
    @FlakyTest
    public void testRotateScreen_direction_row() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                layoutManager.setAlignItems(AlignItems.STRETCH);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);

                for (int i = 0; i < 30; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 100, 100);
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.CENTER));

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW));
        View anchorView = layoutManager.getChildAt(0);
        int offset = anchorView.getTop();
        assertTrue(offset < 0);

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

        // Verify that offset position is preserved for the first visible view after the rotation
        View anchorAfterRotate = layoutManager.getChildAt(0);
        assertTrue(anchorAfterRotate.getTop() < 0);
    }

    @Test
    @FlakyTest
    public void testRotateScreen_direction_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                layoutManager.setAlignItems(AlignItems.STRETCH);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);

                for (int i = 0; i < 30; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 100, 100);
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER));

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN));
        View anchorView = layoutManager.getChildAt(0);
        int offset = anchorView.getLeft();
        assertTrue(offset < 0);

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

        // Verify that offset position is preserved for the first visible view after the rotation
        View anchorAfterRotate = layoutManager.getChildAt(0);
        assertTrue(anchorAfterRotate.getLeft() < 0);
    }

    @Test
    @FlakyTest
    public void testDecoration_direction_row() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        final Drawable drawable = ResourcesCompat.getDrawable(activity.getResources(),
                R.drawable.divider, null);
        final FlexboxItemDecoration itemDecoration = new FlexboxItemDecoration(activity);
        itemDecoration.setDrawable(drawable);

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.addItemDecoration(itemDecoration);
                recyclerView.setAdapter(adapter);

                for (int i = 0; i < 10; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 90, 100);
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW));
        View view2 = layoutManager.getChildAt(1);
        // 90 (view width) + 10 (divider width)
        assertThat(view2.getLeft(), isEqualAllowingError(TestUtil.dpToPixel(activity, 100), 2));
        View view3 = layoutManager.getChildAt(2);
        // 90 (view width) + 10 (divider width) + 90(view width) + 10 (divider width)
        assertThat(view3.getLeft(), isEqualAllowingError(TestUtil.dpToPixel(activity, 200), 2));
        View view4 = layoutManager.getChildAt(3);
        // 100 (view height) + 15 (divider height)
        assertThat(view4.getTop(), isEqualAllowingError(TestUtil.dpToPixel(activity, 115), 2));
        View view7 = layoutManager.getChildAt(6);
        // 100 (view height) + 15 (divider height) + 100 (view height) + 15 (divider height)
        assertThat(view7.getTop(), isEqualAllowingError(TestUtil.dpToPixel(activity, 230), 2));

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.removeItemDecoration(itemDecoration);
                itemDecoration.setOrientation(HORIZONTAL);
                recyclerView.addItemDecoration(itemDecoration);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        view2 = layoutManager.getChildAt(1);
        // 90 (view width)
        assertThat(view2.getLeft(), isEqualAllowingError(TestUtil.dpToPixel(activity, 90), 2));
        view3 = layoutManager.getChildAt(2);
        // 90 (view width) + 90(view width)
        assertThat(view3.getLeft(), isEqualAllowingError(TestUtil.dpToPixel(activity, 180), 2));
        view4 = layoutManager.getChildAt(3);
        // 100 (view height) + 15 (divider height)
        assertThat(view4.getTop(), isEqualAllowingError(TestUtil.dpToPixel(activity, 115), 2));
        view7 = layoutManager.getChildAt(6);
        // 100 (view height) + 15 (divider height) + 100 (view height) + 15 (divider height)
        assertThat(view7.getTop(), isEqualAllowingError(TestUtil.dpToPixel(activity, 230), 2));

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.removeItemDecoration(itemDecoration);
                itemDecoration.setOrientation(VERTICAL);
                recyclerView.addItemDecoration(itemDecoration);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        view2 = layoutManager.getChildAt(1);
        // 90 (view width) + 10 (divider width)
        assertThat(view2.getLeft(), isEqualAllowingError(TestUtil.dpToPixel(activity, 100), 2));
        view3 = layoutManager.getChildAt(2);
        // 90 (view width) + 10 (divider width) + 90(view width) + 10 (divider width)
        assertThat(view3.getLeft(), isEqualAllowingError(TestUtil.dpToPixel(activity, 200), 2));
        view4 = layoutManager.getChildAt(3);
        // 100 (view height)
        assertThat(view4.getTop(), isEqualAllowingError(TestUtil.dpToPixel(activity, 100), 2));
        view7 = layoutManager.getChildAt(6);
        // 100 (view height) + 100 (view height)
        assertThat(view7.getTop(), isEqualAllowingError(TestUtil.dpToPixel(activity, 200), 2));
    }

    @Test
    @FlakyTest
    public void testDecoration_direction_rowReverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        final Drawable drawable = ResourcesCompat.getDrawable(activity.getResources(),
                R.drawable.divider, null);
        final FlexboxItemDecoration itemDecoration = new FlexboxItemDecoration(activity);
        itemDecoration.setDrawable(drawable);

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.ROW_REVERSE);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.addItemDecoration(itemDecoration);
                recyclerView.setAdapter(adapter);

                for (int i = 0; i < 10; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 90, 100);
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW_REVERSE));
        View view1 = layoutManager.getChildAt(0);
        View view2 = layoutManager.getChildAt(1);
        // 90 (view width) + 10 (divider width)
        assertThat(view1.getRight() - view2.getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 100), 2));
        View view3 = layoutManager.getChildAt(2);
        // 90 (view width) + 10 (divider width) + 90(view width) + 10 (divider width)
        assertThat(view1.getRight() - view3.getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 200), 2));
        View view4 = layoutManager.getChildAt(3);
        // 100 (view height) + 15 (divider height)
        assertThat(view4.getTop(), isEqualAllowingError(TestUtil.dpToPixel(activity, 115), 2));
        View view7 = layoutManager.getChildAt(6);
        // 100 (view height) + 15 (divider height) + 100 (view height) + 15 (divider height)
        assertThat(view7.getTop(), isEqualAllowingError(TestUtil.dpToPixel(activity, 230), 2));

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.removeItemDecoration(itemDecoration);
                itemDecoration.setOrientation(HORIZONTAL);
                recyclerView.addItemDecoration(itemDecoration);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        view1 = layoutManager.getChildAt(0);
        view2 = layoutManager.getChildAt(1);
        // 90 (view width)
        assertThat(view1.getRight() - view2.getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 90), 2));
        view3 = layoutManager.getChildAt(2);
        // 90 (view width) + 90(view width)
        assertThat(view1.getRight() - view3.getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 180), 2));
        view4 = layoutManager.getChildAt(3);
        // 100 (view height) + 15 (divider height)
        assertThat(view4.getTop(), isEqualAllowingError(TestUtil.dpToPixel(activity, 115), 2));
        view7 = layoutManager.getChildAt(6);
        // 100 (view height) + 15 (divider height) + 100 (view height) + 15 (divider height)
        assertThat(view7.getTop(), isEqualAllowingError(TestUtil.dpToPixel(activity, 230), 2));

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.removeItemDecoration(itemDecoration);
                itemDecoration.setOrientation(VERTICAL);
                recyclerView.addItemDecoration(itemDecoration);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        view1 = layoutManager.getChildAt(0);
        view2 = layoutManager.getChildAt(1);
        // 90 (view width) + 10 (divider width)
        assertThat(view1.getRight() - view2.getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 100), 2));
        view3 = layoutManager.getChildAt(2);
        // 90 (view width) + 10 (divider width) + 90(view width) + 10 (divider width)
        assertThat(view1.getRight() - view3.getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 200), 2));
        view4 = layoutManager.getChildAt(3);
        // 100 (view height)
        assertThat(view4.getTop(), isEqualAllowingError(TestUtil.dpToPixel(activity, 100), 2));
        view7 = layoutManager.getChildAt(6);
        // 100 (view height) + 100 (view height)
        assertThat(view7.getTop(), isEqualAllowingError(TestUtil.dpToPixel(activity, 200), 2));
    }

    @Test
    @FlakyTest
    public void testDecoration_direction_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        final Drawable drawable = ResourcesCompat.getDrawable(activity.getResources(),
                R.drawable.divider, null);
        final FlexboxItemDecoration itemDecoration = new FlexboxItemDecoration(activity);
        itemDecoration.setDrawable(drawable);

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.addItemDecoration(itemDecoration);
                recyclerView.setAdapter(adapter);

                for (int i = 0; i < 10; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 90, 65);
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN));
        View view2 = layoutManager.getChildAt(1);
        // 65 (view height) + 15 (divider height)
        assertThat(view2.getTop(), isEqualAllowingError(TestUtil.dpToPixel(activity, 80), 2));
        View view3 = layoutManager.getChildAt(2);
        // 65 (view height) + 15 (divider height) + 65 (view height) + 15 (divider height)
        assertThat(view3.getTop(), isEqualAllowingError(TestUtil.dpToPixel(activity, 160), 2));
        View view4 = layoutManager.getChildAt(3);
        // 90 (view width) + 10 (divider width)
        assertThat(view4.getLeft(), isEqualAllowingError(TestUtil.dpToPixel(activity, 100), 2));
        View view7 = layoutManager.getChildAt(6);
        // 90 (view width) + 10 (divider width) + 90 (view width) + 10 (divider width)
        assertThat(view7.getLeft(), isEqualAllowingError(TestUtil.dpToPixel(activity, 200), 2));

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.removeItemDecoration(itemDecoration);
                itemDecoration.setOrientation(HORIZONTAL);
                recyclerView.addItemDecoration(itemDecoration);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        view2 = layoutManager.getChildAt(1);
        // 65 (view height) + 15 (divider height)
        assertThat(view2.getTop(), isEqualAllowingError(TestUtil.dpToPixel(activity, 80), 2));
        view3 = layoutManager.getChildAt(2);
        // 65 (view height) + 15 (divider height) + 65 (view height) + 15 (divider height)
        assertThat(view3.getTop(), isEqualAllowingError(TestUtil.dpToPixel(activity, 160), 2));
        view4 = layoutManager.getChildAt(3);
        // 90 (view width)
        assertThat(view4.getLeft(), isEqualAllowingError(TestUtil.dpToPixel(activity, 90), 2));
        view7 = layoutManager.getChildAt(6);
        // 90 (view width) + 90 (view width)
        assertThat(view7.getLeft(), isEqualAllowingError(TestUtil.dpToPixel(activity, 180), 2));

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.removeItemDecoration(itemDecoration);
                itemDecoration.setOrientation(VERTICAL);
                recyclerView.addItemDecoration(itemDecoration);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        view2 = layoutManager.getChildAt(1);
        // 65 (view height)
        assertThat(view2.getTop(), isEqualAllowingError(TestUtil.dpToPixel(activity, 65), 2));
        view3 = layoutManager.getChildAt(2);
        // 65 (view height) + 65 (view height)
        assertThat(view3.getTop(), isEqualAllowingError(TestUtil.dpToPixel(activity, 130), 2));
        view4 = layoutManager.getChildAt(3);
        // 90 (view width) + 10 (divider width)
        assertThat(view4.getLeft(), isEqualAllowingError(TestUtil.dpToPixel(activity, 100), 2));
        view7 = layoutManager.getChildAt(6);
        // 90 (view width) + 10 (divider width) + 90 (view width) + 10 (divider width)
        assertThat(view7.getLeft(), isEqualAllowingError(TestUtil.dpToPixel(activity, 200), 2));
    }

    @Test
    @FlakyTest
    public void testDecoration_direction_columnReverse() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        final Drawable drawable = ResourcesCompat.getDrawable(activity.getResources(),
                R.drawable.divider, null);
        final FlexboxItemDecoration itemDecoration = new FlexboxItemDecoration(activity);
        itemDecoration.setDrawable(drawable);

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.COLUMN_REVERSE);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.addItemDecoration(itemDecoration);
                recyclerView.setAdapter(adapter);

                for (int i = 0; i < 10; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 90, 65);
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN_REVERSE));
        View view1 = layoutManager.getChildAt(0);
        View view2 = layoutManager.getChildAt(1);
        // 65 (view height) + 15 (divider height)
        assertThat(view1.getTop() - view2.getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80), 2));
        View view3 = layoutManager.getChildAt(2);
        // 65 (view height) + 15 (divider height) + 65 (view height) + 15 (divider height)
        assertThat(view1.getTop() - view3.getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 160), 2));
        View view4 = layoutManager.getChildAt(3);
        // 90 (view width) + 10 (divider width)
        assertThat(view4.getLeft(), isEqualAllowingError(TestUtil.dpToPixel(activity, 100), 2));
        View view7 = layoutManager.getChildAt(6);
        // 90 (view width) + 10 (divider width) + 90 (view width) + 10 (divider width)
        assertThat(view7.getLeft(), isEqualAllowingError(TestUtil.dpToPixel(activity, 200), 2));

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.removeItemDecoration(itemDecoration);
                itemDecoration.setOrientation(HORIZONTAL);
                recyclerView.addItemDecoration(itemDecoration);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        view1 = layoutManager.getChildAt(0);
        view2 = layoutManager.getChildAt(1);
        // 65 (view height) + 15 (divider height)
        assertThat(view1.getTop() - view2.getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 80), 2));
        view3 = layoutManager.getChildAt(2);
        // 65 (view height) + 15 (divider height) + 65 (view height) + 15 (divider height)
        assertThat(view1.getTop() - view3.getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 160), 2));
        view4 = layoutManager.getChildAt(3);
        // 90 (view width)
        assertThat(view4.getLeft(), isEqualAllowingError(TestUtil.dpToPixel(activity, 90), 2));
        view7 = layoutManager.getChildAt(6);
        // 90 (view width) + 90 (view width)
        assertThat(view7.getLeft(), isEqualAllowingError(TestUtil.dpToPixel(activity, 180), 2));

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.removeItemDecoration(itemDecoration);
                itemDecoration.setOrientation(VERTICAL);
                recyclerView.addItemDecoration(itemDecoration);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        view1 = layoutManager.getChildAt(0);
        view2 = layoutManager.getChildAt(1);
        // 65 (view height)
        assertThat(view1.getTop() - view2.getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 65), 2));
        view3 = layoutManager.getChildAt(2);
        // 65 (view height) + 65 (view height)
        assertThat(view1.getTop() - view3.getTop(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 130), 2));
        view4 = layoutManager.getChildAt(3);
        // 90 (view width) + 10 (divider width)
        assertThat(view4.getLeft(), isEqualAllowingError(TestUtil.dpToPixel(activity, 100), 2));
        view7 = layoutManager.getChildAt(6);
        // 90 (view width) + 10 (divider width) + 90 (view width) + 10 (divider width)
        assertThat(view7.getLeft(), isEqualAllowingError(TestUtil.dpToPixel(activity, 200), 2));
    }

    @Test
    @FlakyTest
    public void testScrollToPosition_direction_row() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                for (int i = 0; i < 150; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 100, 70);
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
                // Flex line 1: 3 items
                // Flex line 2: 3 items
                // ....
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW));

        final int scrollTo = 42;
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layoutManager.scrollToPosition(scrollTo);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // Each flex line should have 3 items in this test's configuration.
        // After scrolling to the position of 42 (% 3 == 0), the first visible item should
        // be the 42'th item
        assertThat(((TextView) layoutManager.getChildAt(0)).getText().toString(),
                is(String.valueOf(scrollTo + 1)));

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        // Scroll enough that 42'th item becomes off screen to the top

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layoutManager.scrollToPosition(scrollTo);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // The 42'th item should be at the bottom of the screen.
        // The last visible item should be 42 + 3 since the last visible item is at the last
        // of the bottom flex line
        assertThat(((TextView) layoutManager.getChildAt(
                layoutManager.getChildCount() - 1)).getText().toString(),
                is(String.valueOf(scrollTo + 3)));
    }

    @Test
    @FlakyTest
    public void testScrollToPosition_direction_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                for (int i = 0; i < 150; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 100, 70);
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
                // Flex line 1: 3 items
                // Flex line 2: 3 items
                // ....
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN));

        final int scrollTo = 42;
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layoutManager.scrollToPosition(scrollTo);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // Each flex line should have 3 items in this test's configuration.
        // After scrolling to the position of 42 (% 3 == 0), the first visible item should
        // be the 42'th item
        assertThat(((TextView) layoutManager.getChildAt(0)).getText().toString(),
                is(String.valueOf(scrollTo + 1)));

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT));
        // Scroll enough that 42'th item becomes off screen to the left

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layoutManager.scrollToPosition(scrollTo);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // The 42'th item should be at the bottom of the screen.
        // The last item should be the (42 + 3)'th item since it should be also the last item in the
        // bottom flex line
        assertThat(((TextView) layoutManager.getChildAt(
                layoutManager.getChildCount() - 1)).getText().toString(),
                is(String.valueOf(scrollTo + 3)));
    }

    @Test
    @FlakyTest
    public void testScrollToPosition_scrollToNewItem_direction_row() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                for (int i = 0; i < 6; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 100, 70);
                    adapter.addItem(lp);
                }
                // There should be 2 lines
                // RecyclerView width: 320, height: 240.
                // Flex line 1: 3 items
                // Flex line 2: 3 items
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW));

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 100, 70);
                adapter.addItem(lp);
                layoutManager.scrollToPosition(adapter.getItemCount() - 1);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // ChildCount (visible views) should be 6 + 1,
        // which before fixing https://github.com/google/flexbox-layout/issues/206, only the new
        // item was visible
        assertThat(layoutManager.getChildCount(), is(7));
    }

    @Test
    @FlakyTest
    public void testScrollToPosition_scrollToNewItem_direction_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                for (int i = 0; i < 6; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 100, 70);
                    adapter.addItem(lp);
                }
                // There should be 2 lines
                // RecyclerView width: 320, height: 240.
                // Flex line 1: 3 items
                // Flex line 2: 3 items
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.COLUMN));

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 100, 70);
                adapter.addItem(lp);
                layoutManager.scrollToPosition(adapter.getItemCount() - 1);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // ChildCount (visible views) should be 6 + 1,
        // which before fixing https://github.com/google/flexbox-layout/issues/206, only the new
        // item was visible
        assertThat(layoutManager.getChildCount(), is(7));
    }

    @Test
    @FlakyTest
    public void testScrollToStart_secondLineHasMoreItemThanFirst() throws Throwable {
        // This test verifies the case that the first line disappears as the user first scrolls to
        // the bottom enough that the first line becomes invisible then the user scrolls toward
        // start on the condition that the second line has more items than the first line
        // https://github.com/google/flexbox-layout/issues/228

        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                FlexboxLayoutManager.LayoutParams first = new FlexboxLayoutManager.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, TestUtil.dpToPixel(activity, 70));
                adapter.addItem(first);
                for (int i = 0; i < 50; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 100, 70);
                    adapter.addItem(lp);
                }
                // The first line has 1 item, the following lines have more than 1 items
                // RecyclerView width: 320, height: 240.
                // Flex line 1: 1 items
                // Flex line 2: 3 items
                // Flex line 3: 3 items
                // ...
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(layoutManager.getFlexDirection(), is(FlexDirection.ROW));

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER));
        // At this moment, the first item should become invisible
        View firstView = layoutManager.getChildAt(0);
        assertThat(((TextView) firstView).getText().toString(), is(not("1")));

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER));

        // The first visible item should be "1", which before fixing the issue
        // https://github.com/google/flexbox-layout/issues/228, the first line disappeared.
        firstView = layoutManager.getChildAt(0);
        assertThat(((TextView) firstView).getText().toString(), is("1"));
    }

    @Test
    @FlakyTest
    public void testNestedRecyclerViews_direction_row() throws Throwable {
        // This test verifies the nested RecyclerViews.
        // The outer RecyclerView scrolls vertical using LinearLayoutManager.
        // The inner RecyclerViews use FlexboxLayoutManager with flexDirection == ROW and
        // height of the RecyclerView is set to "wrap_content", which before fixing
        // https://github.com/google/flexbox-layout/issues/208, the height of the inner
        // RecyclerViews were set to 0.
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final LinearLayoutManager outerLayoutManager = new LinearLayoutManager(activity);
        final NestedOuterAdapter adapter = new NestedOuterAdapter(FlexDirection.ROW);
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                outerLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(outerLayoutManager);
                recyclerView.setAdapter(adapter);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        NestedOuterAdapter.OuterViewHolder viewHolder = adapter.getViewHolder(0);
        assertThat(viewHolder.mInnerRecyclerView.getHeight(), is(not(0)));
    }

    @Test
    @FlakyTest
    public void testNestedRecyclerViews_direction_column() throws Throwable {
        // This test verifies the nested RecyclerViews.
        // The outer RecyclerView scrolls horizontally using LinearLayoutManager.
        // The inner RecyclerViews use FlexboxLayoutManager with flexDirection == COLUMN and
        // width of the RecyclerView is set to "wrap_content", which before fixing
        // https://github.com/google/flexbox-layout/issues/208, the width of the inner
        // RecyclerViews were set to 0.
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final LinearLayoutManager outerLayoutManager = new LinearLayoutManager(activity);
        final NestedOuterAdapter adapter = new NestedOuterAdapter(FlexDirection.COLUMN);
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                outerLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                recyclerView.setLayoutManager(outerLayoutManager);
                recyclerView.setAdapter(adapter);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        NestedOuterAdapter.OuterViewHolder viewHolder = adapter.getViewHolder(0);
        assertThat(viewHolder.mInnerRecyclerView.getWidth(), is(not(0)));
    }

    @Test
    @FlakyTest
    public void testFindVisibleChild_direction_row() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);

                for (int i = 0; i < 50; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 100, 75);
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
                // At first three completely visible lines.
                // Flex line 1, item count 3 (0, 1, 2)
                // Flex line 2, item count 3 (3, 4, 5)
                // Flex line 3, item count 3 (6, 7, 8)
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.findFirstCompletelyVisibleItemPosition(), is(0));
        assertThat(layoutManager.findFirstVisibleItemPosition(), is(0));
        assertThat(layoutManager.findLastCompletelyVisibleItemPosition(), is(8));
        assertThat(layoutManager.findLastVisibleItemPosition(), is(11));

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER,
                GeneralLocation.TOP_CENTER));
        // Scroll by about half of the height of the RecyclerView
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.findFirstCompletelyVisibleItemPosition(), is(6));
        assertThat(layoutManager.findFirstVisibleItemPosition(), is(3));
        assertThat(layoutManager.findLastCompletelyVisibleItemPosition(), is(11));
        assertThat(layoutManager.findLastVisibleItemPosition(), is(14));
    }

    @Test
    @FlakyTest
    public void testFindVisibleChild_direction_column() throws Throwable {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        final FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        final TestAdapter adapter = new TestAdapter();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.recyclerview);
                RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);

                for (int i = 0; i < 50; i++) {
                    FlexboxLayoutManager.LayoutParams lp = createLayoutParams(activity, 100, 75);
                    adapter.addItem(lp);
                }
                // RecyclerView width: 320, height: 240.
                // At first three completely visible lines.
                // Flex line 1, item count 3 (0, 1, 2)
                // Flex line 2, item count 3 (3, 4, 5)
                // Flex line 3, item count 3 (6, 7, 8)
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.findFirstCompletelyVisibleItemPosition(), is(0));
        assertThat(layoutManager.findFirstVisibleItemPosition(), is(0));
        assertThat(layoutManager.findLastCompletelyVisibleItemPosition(), is(8));
        assertThat(layoutManager.findLastVisibleItemPosition(), is(11));

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER,
                GeneralLocation.CENTER_LEFT));
        // Scroll by about half of the width of the RecyclerView
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.findFirstCompletelyVisibleItemPosition(), is(6));
        assertThat(layoutManager.findFirstVisibleItemPosition(), is(3));
        assertThat(layoutManager.findLastCompletelyVisibleItemPosition(), is(11));
        assertThat(layoutManager.findLastVisibleItemPosition(), is(14));
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

