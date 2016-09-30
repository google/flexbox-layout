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

import static junit.framework.Assert.assertEquals;

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
    }
}
