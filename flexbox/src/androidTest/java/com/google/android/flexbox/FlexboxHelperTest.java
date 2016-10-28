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

package com.google.android.flexbox;

import com.google.android.flexbox.test.FlexboxTestActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import static com.google.android.flexbox.test.IsEqualAllowingError.isEqualAllowingError;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link FlexboxHelper}.
 */
@RunWith(AndroidJUnit4.class)
public class FlexboxHelperTest {

    @Rule
    public ActivityTestRule<FlexboxTestActivity> mActivityRule =
            new ActivityTestRule<>(FlexboxTestActivity.class);

    private FlexboxHelper mFlexboxHelper;

    private FlexContainer mFlexContainer;

    @Before
    public void setUp() {
        mFlexContainer = new FakeFlexContainer();
        mFlexboxHelper = new FlexboxHelper(mFlexContainer);
    }

    @Test
    public void testCalculateHorizontalFlexLines() throws Throwable {
        Activity activity = mActivityRule.getActivity();
        FlexboxLayout.LayoutParams lp1 = new FlexboxLayout.LayoutParams(100, 100);
        View view1 = new View(activity);
        view1.setLayoutParams(lp1);
        FlexboxLayout.LayoutParams lp2 = new FlexboxLayout.LayoutParams(200, 100);
        View view2 = new View(activity);
        view2.setLayoutParams(lp2);
        FlexboxLayout.LayoutParams lp3 = new FlexboxLayout.LayoutParams(300, 100);
        View view3 = new View(activity);
        view3.setLayoutParams(lp3);
        FlexboxLayout.LayoutParams lp4 = new FlexboxLayout.LayoutParams(400, 100);
        View view4 = new View(activity);
        view4.setLayoutParams(lp4);
        mFlexContainer.addView(view1);
        mFlexContainer.addView(view2);
        mFlexContainer.addView(view3);
        mFlexContainer.addView(view4);
        mFlexContainer.setFlexWrap(FlexWrap.WRAP);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec
                .makeMeasureSpec(1000, View.MeasureSpec.UNSPECIFIED);

        FlexboxHelper.FlexLinesResult result = mFlexboxHelper
                .calculateHorizontalFlexLines(widthMeasureSpec, heightMeasureSpec);

        assertEquals(3, result.mFlexLines.size());
        assertEquals(300, result.mFlexLines.get(0).getMainSize());
        assertEquals(300, result.mFlexLines.get(1).getMainSize());
        assertEquals(400, result.mFlexLines.get(2).getMainSize());
        assertEquals(100, result.mFlexLines.get(0).getCrossSize());
        assertEquals(100, result.mFlexLines.get(1).getCrossSize());
        assertEquals(100, result.mFlexLines.get(2).getCrossSize());

        assertEquals(0, mFlexboxHelper.mIndexToFlexLine.get(0));
        assertEquals(0, mFlexboxHelper.mIndexToFlexLine.get(1));
        assertEquals(1, mFlexboxHelper.mIndexToFlexLine.get(2));
        assertEquals(2, mFlexboxHelper.mIndexToFlexLine.get(3));
    }

    @Test
    public void testCalculateVerticalFlexLines() throws Throwable {
        Activity activity = mActivityRule.getActivity();
        FlexboxLayout.LayoutParams lp1 = new FlexboxLayout.LayoutParams(100, 100);
        View view1 = new View(activity);
        view1.setLayoutParams(lp1);
        FlexboxLayout.LayoutParams lp2 = new FlexboxLayout.LayoutParams(100, 200);
        View view2 = new View(activity);
        view2.setLayoutParams(lp2);
        FlexboxLayout.LayoutParams lp3 = new FlexboxLayout.LayoutParams(100, 300);
        View view3 = new View(activity);
        view3.setLayoutParams(lp3);
        FlexboxLayout.LayoutParams lp4 = new FlexboxLayout.LayoutParams(100, 400);
        View view4 = new View(activity);
        view4.setLayoutParams(lp4);
        mFlexContainer.addView(view1);
        mFlexContainer.addView(view2);
        mFlexContainer.addView(view3);
        mFlexContainer.addView(view4);
        mFlexContainer.setFlexWrap(FlexWrap.WRAP);
        mFlexContainer.setFlexDirection(FlexDirection.COLUMN);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY);

        FlexboxHelper.FlexLinesResult result = mFlexboxHelper
                .calculateVerticalFlexLines(widthMeasureSpec, heightMeasureSpec);

        assertEquals(3, result.mFlexLines.size());
        assertEquals(300, result.mFlexLines.get(0).getMainSize());
        assertEquals(300, result.mFlexLines.get(1).getMainSize());
        assertEquals(400, result.mFlexLines.get(2).getMainSize());
        assertEquals(100, result.mFlexLines.get(0).getCrossSize());
        assertEquals(100, result.mFlexLines.get(1).getCrossSize());
        assertEquals(100, result.mFlexLines.get(2).getCrossSize());

        assertEquals(0, mFlexboxHelper.mIndexToFlexLine.get(0));
        assertEquals(0, mFlexboxHelper.mIndexToFlexLine.get(1));
        assertEquals(1, mFlexboxHelper.mIndexToFlexLine.get(2));
        assertEquals(2, mFlexboxHelper.mIndexToFlexLine.get(3));
    }

    @Test
    public void testDetermineMainSize_direction_row_flexGrowSet() throws Throwable {
        Activity activity = mActivityRule.getActivity();
        FlexboxLayout.LayoutParams lp1 = new FlexboxLayout.LayoutParams(100, 100);
        View view1 = new View(activity);
        view1.setLayoutParams(lp1);
        FlexboxLayout.LayoutParams lp2 = new FlexboxLayout.LayoutParams(200, 100);
        lp2.setFlexGrow(1.0f);
        View view2 = new View(activity);
        view2.setLayoutParams(lp2);
        FlexboxLayout.LayoutParams lp3 = new FlexboxLayout.LayoutParams(300, 100);
        View view3 = new View(activity);
        view3.setLayoutParams(lp3);
        FlexboxLayout.LayoutParams lp4 = new FlexboxLayout.LayoutParams(400, 100);
        lp4.setFlexGrow(2.0f);
        View view4 = new View(activity);
        view4.setLayoutParams(lp4);
        mFlexContainer.addView(view1);
        mFlexContainer.addView(view2);
        mFlexContainer.addView(view3);
        mFlexContainer.addView(view4);
        mFlexContainer.setFlexDirection(FlexDirection.ROW);
        mFlexContainer.setFlexWrap(FlexWrap.WRAP);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec
                .makeMeasureSpec(1000, View.MeasureSpec.UNSPECIFIED);
        FlexboxHelper.FlexLinesResult result = mFlexboxHelper
                .calculateHorizontalFlexLines(widthMeasureSpec, heightMeasureSpec);
        mFlexContainer.setFlexLines(result.mFlexLines);
        boolean[] childrenFrozen = new boolean[4];
        mFlexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec, childrenFrozen);

        assertThat(view1.getMeasuredWidth(), is(100));
        assertThat(view1.getMeasuredHeight(), is(100));
        // view2 will expand to fill the left space in the first flex line since flex grow is set
        assertThat(view2.getMeasuredWidth(), is(400));
        assertThat(view2.getMeasuredHeight(), is(100));
        assertThat(view3.getMeasuredWidth(), is(300));
        assertThat(view3.getMeasuredHeight(), is(100));
        // view4 will expand to fill the left space in the first flex line since flex grow is set
        assertThat(view4.getMeasuredWidth(), is(500));
        assertThat(view4.getMeasuredHeight(), is(100));
    }

    @Test
    public void testDetermineMainSize_direction_column_flexGrowSet() throws Throwable {
        Activity activity = mActivityRule.getActivity();
        FlexboxLayout.LayoutParams lp1 = new FlexboxLayout.LayoutParams(100, 100);
        View view1 = new View(activity);
        view1.setLayoutParams(lp1);
        FlexboxLayout.LayoutParams lp2 = new FlexboxLayout.LayoutParams(100, 200);
        lp2.setFlexGrow(1.0f);
        View view2 = new View(activity);
        view2.setLayoutParams(lp2);
        FlexboxLayout.LayoutParams lp3 = new FlexboxLayout.LayoutParams(100, 300);
        View view3 = new View(activity);
        view3.setLayoutParams(lp3);
        FlexboxLayout.LayoutParams lp4 = new FlexboxLayout.LayoutParams(100, 400);
        lp4.setFlexGrow(2.0f);
        View view4 = new View(activity);
        view4.setLayoutParams(lp4);
        mFlexContainer.addView(view1);
        mFlexContainer.addView(view2);
        mFlexContainer.addView(view3);
        mFlexContainer.addView(view4);
        mFlexContainer.setFlexDirection(FlexDirection.COLUMN);
        mFlexContainer.setFlexWrap(FlexWrap.WRAP);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec
                .makeMeasureSpec(500, View.MeasureSpec.EXACTLY);
        FlexboxHelper.FlexLinesResult result = mFlexboxHelper
                .calculateVerticalFlexLines(widthMeasureSpec, heightMeasureSpec);
        mFlexContainer.setFlexLines(result.mFlexLines);
        boolean[] childrenFrozen = new boolean[4];
        mFlexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec, childrenFrozen);

        assertThat(view1.getMeasuredWidth(), is(100));
        assertThat(view1.getMeasuredHeight(), is(100));
        assertThat(view2.getMeasuredWidth(), is(100));
        // view2 will expand to fill the left space in the first flex line since flex grow is set
        assertThat(view2.getMeasuredHeight(), is(400));
        assertThat(view3.getMeasuredWidth(), is(100));
        assertThat(view3.getMeasuredHeight(), is(300));
        assertThat(view4.getMeasuredWidth(), is(100));
        // view4 will expand to fill the left space in the first flex line since flex grow is set
        assertThat(view4.getMeasuredHeight(), is(500));
    }

    @Test
    public void testDetermineMainSize_direction_row_flexShrinkSet() throws Throwable {
        Activity activity = mActivityRule.getActivity();
        FlexboxLayout.LayoutParams lp1 = new FlexboxLayout.LayoutParams(200, 100);
        View view1 = new View(activity);
        view1.setLayoutParams(lp1);
        FlexboxLayout.LayoutParams lp2 = new FlexboxLayout.LayoutParams(200, 100);
        View view2 = new View(activity);
        view2.setLayoutParams(lp2);
        FlexboxLayout.LayoutParams lp3 = new FlexboxLayout.LayoutParams(200, 100);
        View view3 = new View(activity);
        view3.setLayoutParams(lp3);
        FlexboxLayout.LayoutParams lp4 = new FlexboxLayout.LayoutParams(200, 100);
        View view4 = new View(activity);
        view4.setLayoutParams(lp4);
        mFlexContainer.addView(view1);
        mFlexContainer.addView(view2);
        mFlexContainer.addView(view3);
        mFlexContainer.addView(view4);
        mFlexContainer.setFlexDirection(FlexDirection.ROW);
        mFlexContainer.setFlexWrap(FlexWrap.NOWRAP);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec
                .makeMeasureSpec(1000, View.MeasureSpec.UNSPECIFIED);
        FlexboxHelper.FlexLinesResult result = mFlexboxHelper
                .calculateHorizontalFlexLines(widthMeasureSpec, heightMeasureSpec);
        mFlexContainer.setFlexLines(result.mFlexLines);
        boolean[] childrenFrozen = new boolean[4];
        mFlexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec, childrenFrozen);

        // Flex shrink is set to 1.0 (default value) for all views.
        // They should be shrank equally for the amount overflown the width
        assertThat(view1.getMeasuredWidth(), is(125));
        assertThat(view1.getMeasuredHeight(), is(100));
        assertThat(view2.getMeasuredWidth(), is(125));
        assertThat(view2.getMeasuredHeight(), is(100));
        assertThat(view3.getMeasuredWidth(), is(125));
        assertThat(view3.getMeasuredHeight(), is(100));
        assertThat(view4.getMeasuredWidth(), is(125));
        assertThat(view4.getMeasuredHeight(), is(100));
    }

    @Test
    public void testDetermineMainSize_direction_column_flexShrinkSet() throws Throwable {
        Activity activity = mActivityRule.getActivity();
        FlexboxLayout.LayoutParams lp1 = new FlexboxLayout.LayoutParams(100, 200);
        View view1 = new View(activity);
        view1.setLayoutParams(lp1);
        FlexboxLayout.LayoutParams lp2 = new FlexboxLayout.LayoutParams(100, 200);
        View view2 = new View(activity);
        view2.setLayoutParams(lp2);
        FlexboxLayout.LayoutParams lp3 = new FlexboxLayout.LayoutParams(100, 200);
        View view3 = new View(activity);
        view3.setLayoutParams(lp3);
        FlexboxLayout.LayoutParams lp4 = new FlexboxLayout.LayoutParams(100, 200);
        View view4 = new View(activity);
        view4.setLayoutParams(lp4);
        mFlexContainer.addView(view1);
        mFlexContainer.addView(view2);
        mFlexContainer.addView(view3);
        mFlexContainer.addView(view4);
        mFlexContainer.setFlexDirection(FlexDirection.COLUMN);
        mFlexContainer.setFlexWrap(FlexWrap.NOWRAP);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec
                .makeMeasureSpec(500, View.MeasureSpec.EXACTLY);
        FlexboxHelper.FlexLinesResult result = mFlexboxHelper
                .calculateVerticalFlexLines(widthMeasureSpec, heightMeasureSpec);
        mFlexContainer.setFlexLines(result.mFlexLines);
        boolean[] childrenFrozen = new boolean[4];
        mFlexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec, childrenFrozen);

        // Flex shrink is set to 1.0 (default value) for all views.
        // They should be shrank equally for the amount overflown the height
        assertThat(view1.getMeasuredWidth(), is(100));
        assertThat(view1.getMeasuredHeight(), is(125));
        assertThat(view2.getMeasuredWidth(), is(100));
        assertThat(view2.getMeasuredHeight(), is(125));
        assertThat(view3.getMeasuredWidth(), is(100));
        assertThat(view3.getMeasuredHeight(), is(125));
        assertThat(view4.getMeasuredWidth(), is(100));
        assertThat(view4.getMeasuredHeight(), is(125));
    }

    @Test
    public void testDetermineCrossSize_direction_row_alignContent_stretch() throws Throwable {
        Activity activity = mActivityRule.getActivity();
        FlexboxLayout.LayoutParams lp1 = new FlexboxLayout.LayoutParams(100, 100);
        View view1 = new View(activity);
        view1.setLayoutParams(lp1);
        FlexboxLayout.LayoutParams lp2 = new FlexboxLayout.LayoutParams(200, 100);
        View view2 = new View(activity);
        view2.setLayoutParams(lp2);
        FlexboxLayout.LayoutParams lp3 = new FlexboxLayout.LayoutParams(300, 100);
        View view3 = new View(activity);
        view3.setLayoutParams(lp3);
        FlexboxLayout.LayoutParams lp4 = new FlexboxLayout.LayoutParams(400, 100);
        View view4 = new View(activity);
        view4.setLayoutParams(lp4);
        mFlexContainer.addView(view1);
        mFlexContainer.addView(view2);
        mFlexContainer.addView(view3);
        mFlexContainer.addView(view4);
        mFlexContainer.setFlexDirection(FlexDirection.ROW);
        mFlexContainer.setFlexWrap(FlexWrap.WRAP);
        mFlexContainer.setAlignContent(AlignContent.STRETCH);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec
                .makeMeasureSpec(1000, View.MeasureSpec.EXACTLY);
        FlexboxHelper.FlexLinesResult result = mFlexboxHelper
                .calculateHorizontalFlexLines(widthMeasureSpec, heightMeasureSpec);
        mFlexContainer.setFlexLines(result.mFlexLines);
        boolean[] childrenFrozen = new boolean[4];
        mFlexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec, childrenFrozen);
        mFlexboxHelper
                .determineCrossSize(widthMeasureSpec, heightMeasureSpec, 0);
        mFlexboxHelper.stretchViews();

        // align content is set to Align.STRETCH, the cross size for each flex line is stretched
        // to distribute the remaining free space along the cross axis
        // (remaining height in this case)
        assertThat(view1.getMeasuredHeight(), isEqualAllowingError(333));
        assertThat(view2.getMeasuredHeight(), isEqualAllowingError(333));
        assertThat(view3.getMeasuredHeight(), isEqualAllowingError(333));
        assertThat(view4.getMeasuredHeight(), isEqualAllowingError(333));
    }

    @Test
    public void testDetermineCrossSize_direction_column_alignContent_stretch() throws Throwable {
        Activity activity = mActivityRule.getActivity();
        FlexboxLayout.LayoutParams lp1 = new FlexboxLayout.LayoutParams(100, 100);
        View view1 = new View(activity);
        view1.setLayoutParams(lp1);
        FlexboxLayout.LayoutParams lp2 = new FlexboxLayout.LayoutParams(100, 200);
        View view2 = new View(activity);
        view2.setLayoutParams(lp2);
        FlexboxLayout.LayoutParams lp3 = new FlexboxLayout.LayoutParams(100, 300);
        View view3 = new View(activity);
        view3.setLayoutParams(lp3);
        FlexboxLayout.LayoutParams lp4 = new FlexboxLayout.LayoutParams(100, 400);
        View view4 = new View(activity);
        view4.setLayoutParams(lp4);
        mFlexContainer.addView(view1);
        mFlexContainer.addView(view2);
        mFlexContainer.addView(view3);
        mFlexContainer.addView(view4);
        mFlexContainer.setFlexDirection(FlexDirection.COLUMN);
        mFlexContainer.setFlexWrap(FlexWrap.WRAP);
        mFlexContainer.setAlignContent(AlignContent.STRETCH);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec
                .makeMeasureSpec(500, View.MeasureSpec.EXACTLY);
        FlexboxHelper.FlexLinesResult result = mFlexboxHelper
                .calculateVerticalFlexLines(widthMeasureSpec, heightMeasureSpec);
        mFlexContainer.setFlexLines(result.mFlexLines);
        boolean[] childrenFrozen = new boolean[4];
        mFlexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec, childrenFrozen);
        mFlexboxHelper
                .determineCrossSize(widthMeasureSpec, heightMeasureSpec, 0);
        mFlexboxHelper.stretchViews();

        // align content is set to Align.STRETCH, the cross size for each flex line is stretched
        // to distribute the remaining free space along the cross axis
        // (remaining width in this case)
        assertThat(view1.getMeasuredWidth(), isEqualAllowingError(333));
        assertThat(view2.getMeasuredWidth(), isEqualAllowingError(333));
        assertThat(view3.getMeasuredWidth(), isEqualAllowingError(333));
        assertThat(view4.getMeasuredWidth(), isEqualAllowingError(333));
    }
}
