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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import android.support.test.rule.ActivityTestRule;
import android.view.View;

import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Unit tests for {@link FlexboxHelper}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlexboxHelperTest {

    @Rule
    public ActivityTestRule<FlexboxTestActivity> mActivityRule =
            new ActivityTestRule<>(FlexboxTestActivity.class);

    private FlexboxHelper mFlexboxHelper;

    @Mock
    private View mockView1;

    @Mock
    private View mockView2;

    @Mock
    private View mockView3;

    @Mock
    private View mockView4;

    @Before
    public void setUp() {
        FlexContainer flexContainer = new FakeFlexContainer();
        mFlexboxHelper = new FlexboxHelper(flexContainer);

        Mockito.when(mockView1.getMeasuredWidth()).thenReturn(100);
        Mockito.when(mockView2.getMeasuredWidth()).thenReturn(200);
        Mockito.when(mockView3.getMeasuredWidth()).thenReturn(300);
        Mockito.when(mockView4.getMeasuredWidth()).thenReturn(400);
    }

    @Test
    public void testCalculateHorizontalFlexLines() {
        final FlexboxTestActivity activity = mActivityRule.getActivity();
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec
                .makeMeasureSpec(1000, View.MeasureSpec.UNSPECIFIED);

        List<FlexLine> result = mFlexboxHelper
                .calculateHorizontalFlexLines(widthMeasureSpec, heightMeasureSpec);

        assertEquals(3, result.size());
    }
}
