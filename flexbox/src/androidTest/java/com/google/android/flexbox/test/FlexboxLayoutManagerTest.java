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
import android.support.test.filters.FlakyTest;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;

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
                adapter.addItem(createLayoutParams(activity, 150, 130));
                adapter.addItem(createLayoutParams(activity, 150, 130));
                adapter.addItem(createLayoutParams(activity, 150, 130));
                adapter.addItem(createLayoutParams(activity, 150, 130));
                adapter.addItem(createLayoutParams(activity, 150, 130));
                adapter.addItem(createLayoutParams(activity, 150, 130));
                adapter.addItem(createLayoutParams(activity, 150, 130));
                adapter.addItem(createLayoutParams(activity, 150, 130));
                adapter.addItem(createLayoutParams(activity, 150, 130));
                // RecyclerView width: 400, height: 300.
                // Computed FlexContainer width: 400, height: 650
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // In total 9 items are added but the seventh item and the items follow aren't attached
        // (invisible) so are not included in the count of of the getChildCount.
        assertThat(layoutManager.getFlexItemCount(), is(9));
        assertThat(layoutManager.getChildCount(), is(6));
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
                // RecyclerView width: 400, height: 300.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // The flexGrow parameters for all LayoutParams are set to 1.0, expecting each child to
        // fill the horizontal remaining space
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(2));
        assertThat(layoutManager.getChildAt(0).getWidth(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 200)));
        assertThat(layoutManager.getChildAt(1).getWidth(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 200)));
        assertThat(layoutManager.getChildAt(2).getWidth(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 400)));
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
                // RecyclerView width: 400, height: 300.
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
                // RecyclerView width: 400, height: 300.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.FLEX_END));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 250)));
        assertThat(layoutManager.getChildAt(0).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 300)));
        assertThat(layoutManager.getChildAt(1).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 300)));
        assertThat(layoutManager.getChildAt(1).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 350)));
        assertThat(layoutManager.getChildAt(2).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 350)));
        assertThat(layoutManager.getChildAt(2).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 400)));
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
                // RecyclerView width: 400, height: 300.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.CENTER));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 125)));
        assertThat(layoutManager.getChildAt(0).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 175)));
        assertThat(layoutManager.getChildAt(1).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 175)));
        assertThat(layoutManager.getChildAt(1).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 225)));
        assertThat(layoutManager.getChildAt(2).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 225)));
        assertThat(layoutManager.getChildAt(2).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 275)));
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
                // RecyclerView width: 400, height: 300.
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(layoutManager.getJustifyContent(), is(JustifyContent.SPACE_AROUND));
        assertThat(layoutManager.getFlexItemCount(), is(3));
        assertThat(layoutManager.getFlexLines().size(), is(1));
        assertThat(layoutManager.getChildAt(0).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 42)));
        assertThat(layoutManager.getChildAt(0).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 92)));
        assertThat(layoutManager.getChildAt(1).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 175)));
        assertThat(layoutManager.getChildAt(1).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 225)));
        assertThat(layoutManager.getChildAt(2).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 308)));
        assertThat(layoutManager.getChildAt(2).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 358)));
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
                // RecyclerView width: 400, height: 300.
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
                isEqualAllowingError(TestUtil.dpToPixel(activity, 175)));
        assertThat(layoutManager.getChildAt(1).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 225)));
        assertThat(layoutManager.getChildAt(2).getLeft(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 350)));
        assertThat(layoutManager.getChildAt(2).getRight(),
                isEqualAllowingError(TestUtil.dpToPixel(activity, 400)));
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
}
