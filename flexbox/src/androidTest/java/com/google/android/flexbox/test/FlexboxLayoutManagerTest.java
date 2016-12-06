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
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
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

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.google.android.flexbox.test.IsEqualAllowingError.isEqualAllowingError;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

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
        assertThat(flexboxLayoutManager.getFlexWrap(), is(FlexWrap.WRAP_REVERSE));
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
        assertThat(flexboxLayoutManager.getFlexWrap(), is(FlexWrap.WRAP_REVERSE));

        flexboxLayoutManager.setFlexDirection(FlexDirection.COLUMN);
        flexboxLayoutManager.setFlexWrap(FlexWrap.NOWRAP);
        flexboxLayoutManager.setJustifyContent(JustifyContent.CENTER);
        flexboxLayoutManager.setAlignItems(AlignItems.FLEX_END);
        flexboxLayoutManager.setAlignContent(AlignContent.SPACE_BETWEEN);
        assertThat(flexboxLayoutManager.getFlexDirection(), is(FlexDirection.COLUMN));
        assertThat(flexboxLayoutManager.getFlexWrap(), is(FlexWrap.NOWRAP));
        assertThat(flexboxLayoutManager.getJustifyContent(), is(JustifyContent.CENTER));
        assertThat(flexboxLayoutManager.getAlignItems(), is(AlignItems.FLEX_END));
        assertThat(flexboxLayoutManager.getAlignContent(), is(AlignContent.SPACE_BETWEEN));
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

