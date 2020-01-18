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

package com.google.android.flexbox

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.widget.CompoundButtonCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.android.flexbox.test.FlexboxTestActivity
import com.google.android.flexbox.test.IsEqualAllowingError.Companion.isEqualAllowingError
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit tests for [FlexboxHelper].
 */
@RunWith(AndroidJUnit4::class)
class FlexboxHelperTest {

    private val LONG_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do " +
            "eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim " +
            "veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo " +
            "consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum " +
            "dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, " +
            "sunt in culpa qui officia deserunt mollit anim id est laborum."

    @JvmField
    @Rule
    var activityRule = ActivityTestRule(FlexboxTestActivity::class.java)

    private lateinit var flexboxHelper: FlexboxHelper

    private lateinit var flexContainer: FlexContainer

    @Before
    fun setUp() {
        flexContainer = FakeFlexContainer()
        flexboxHelper = FlexboxHelper(flexContainer)
    }

    @Test
    @Throws(Throwable::class)
    fun testCalculateHorizontalFlexLines() {
        val activity = activityRule.activity
        val lp1 = FlexboxLayout.LayoutParams(100, 100)
        val view1 = View(activity)
        view1.layoutParams = lp1
        val lp2 = FlexboxLayout.LayoutParams(200, 100)
        val view2 = View(activity)
        view2.layoutParams = lp2
        val lp3 = FlexboxLayout.LayoutParams(300, 100)
        val view3 = View(activity)
        view3.layoutParams = lp3
        val lp4 = FlexboxLayout.LayoutParams(400, 100)
        val view4 = View(activity)
        view4.layoutParams = lp4
        flexContainer.addView(view1)
        flexContainer.addView(view2)
        flexContainer.addView(view3)
        flexContainer.addView(view4)
        flexContainer.flexWrap = FlexWrap.WRAP
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.UNSPECIFIED)

        flexboxHelper.ensureIndexToFlexLine(flexContainer.flexItemCount)
        val result = FlexboxHelper.FlexLinesResult()
        flexboxHelper.calculateHorizontalFlexLines(result, widthMeasureSpec, heightMeasureSpec)

        assertEquals(3, result.mFlexLines.size)
        assertEquals(300, result.mFlexLines[0].mainSize)
        assertEquals(300, result.mFlexLines[1].mainSize)
        assertEquals(400, result.mFlexLines[2].mainSize)
        assertEquals(100, result.mFlexLines[0].crossSize)
        assertEquals(100, result.mFlexLines[1].crossSize)
        assertEquals(100, result.mFlexLines[2].crossSize)

        assertNotNull(flexboxHelper.mIndexToFlexLine)
        assertEquals(0, flexboxHelper.mIndexToFlexLine!![0])
        assertEquals(0, flexboxHelper.mIndexToFlexLine!![1])
        assertEquals(1, flexboxHelper.mIndexToFlexLine!![2])
        assertEquals(2, flexboxHelper.mIndexToFlexLine!![3])

        val firstLine = result.mFlexLines[0]
        assertEquals(0, firstLine.mFirstIndex)
        assertEquals(1, firstLine.mLastIndex)
        val secondLine = result.mFlexLines[1]
        assertEquals(2, secondLine.mFirstIndex)
        assertEquals(2, secondLine.mLastIndex)
        val thirdLine = result.mFlexLines[2]
        assertEquals(3, thirdLine.mFirstIndex)
        assertEquals(3, thirdLine.mLastIndex)
    }

    @Test
    @Throws(Throwable::class)
    fun testCalculateVerticalFlexLines() {
        val activity = activityRule.activity
        val lp1 = FlexboxLayout.LayoutParams(100, 100)
        val view1 = View(activity)
        view1.layoutParams = lp1
        val lp2 = FlexboxLayout.LayoutParams(100, 200)
        val view2 = View(activity)
        view2.layoutParams = lp2
        val lp3 = FlexboxLayout.LayoutParams(100, 300)
        val view3 = View(activity)
        view3.layoutParams = lp3
        val lp4 = FlexboxLayout.LayoutParams(100, 400)
        val view4 = View(activity)
        view4.layoutParams = lp4
        flexContainer.addView(view1)
        flexContainer.addView(view2)
        flexContainer.addView(view3)
        flexContainer.addView(view4)
        flexContainer.flexWrap = FlexWrap.WRAP
        flexContainer.flexDirection = FlexDirection.COLUMN
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.UNSPECIFIED)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY)

        flexboxHelper.ensureIndexToFlexLine(flexContainer.flexItemCount)
        val result = FlexboxHelper.FlexLinesResult()
        flexboxHelper.calculateVerticalFlexLines(result, widthMeasureSpec, heightMeasureSpec)

        assertEquals(3, result.mFlexLines.size)
        assertEquals(300, result.mFlexLines[0].mainSize)
        assertEquals(300, result.mFlexLines[1].mainSize)
        assertEquals(400, result.mFlexLines[2].mainSize)
        assertEquals(100, result.mFlexLines[0].crossSize)
        assertEquals(100, result.mFlexLines[1].crossSize)
        assertEquals(100, result.mFlexLines[2].crossSize)

        assertNotNull(flexboxHelper.mIndexToFlexLine)
        assertEquals(0, flexboxHelper.mIndexToFlexLine!![0])
        assertEquals(0, flexboxHelper.mIndexToFlexLine!![1])
        assertEquals(1, flexboxHelper.mIndexToFlexLine!![2])
        assertEquals(2, flexboxHelper.mIndexToFlexLine!![3])

        val firstLine = result.mFlexLines[0]
        assertEquals(0, firstLine.mFirstIndex)
        assertEquals(1, firstLine.mLastIndex)
        val secondLine = result.mFlexLines[1]
        assertEquals(2, secondLine.mFirstIndex)
        assertEquals(2, secondLine.mLastIndex)
        val thirdLine = result.mFlexLines[2]
        assertEquals(3, thirdLine.mFirstIndex)
        assertEquals(3, thirdLine.mLastIndex)
    }

    @Test
    @Throws(Throwable::class)
    fun testDetermineMainSize_direction_row_flexGrowSet() {
        val activity = activityRule.activity
        val lp1 = FlexboxLayout.LayoutParams(100, 100)
        val view1 = View(activity)
        view1.layoutParams = lp1
        val lp2 = FlexboxLayout.LayoutParams(200, 100)
        lp2.flexGrow = 1.0f
        val view2 = View(activity)
        view2.layoutParams = lp2
        val lp3 = FlexboxLayout.LayoutParams(300, 100)
        val view3 = View(activity)
        view3.layoutParams = lp3
        val lp4 = FlexboxLayout.LayoutParams(400, 100)
        lp4.flexGrow = 2.0f
        val view4 = View(activity)
        view4.layoutParams = lp4
        flexContainer.addView(view1)
        flexContainer.addView(view2)
        flexContainer.addView(view3)
        flexContainer.addView(view4)
        flexContainer.flexDirection = FlexDirection.ROW
        flexContainer.flexWrap = FlexWrap.WRAP
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.UNSPECIFIED)
        val result = FlexboxHelper.FlexLinesResult()
        flexboxHelper.calculateHorizontalFlexLines(result, widthMeasureSpec, heightMeasureSpec)
        flexContainer.flexLines = result.mFlexLines
        flexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec)

        assertThat(view1.measuredWidth, `is`(100))
        assertThat(view1.measuredHeight, `is`(100))
        // view2 will expand to fill the left space in the first flex line since flex grow is set
        assertThat(view2.measuredWidth, `is`(400))
        assertThat(view2.measuredHeight, `is`(100))
        assertThat(view3.measuredWidth, `is`(300))
        assertThat(view3.measuredHeight, `is`(100))
        // view4 will expand to fill the left space in the first flex line since flex grow is set
        assertThat(view4.measuredWidth, `is`(500))
        assertThat(view4.measuredHeight, `is`(100))
    }

    @Test
    @Throws(Throwable::class)
    fun testDetermineMainSize_direction_column_flexGrowSet() {
        val activity = activityRule.activity
        val lp1 = FlexboxLayout.LayoutParams(100, 100)
        val view1 = View(activity)
        view1.layoutParams = lp1
        val lp2 = FlexboxLayout.LayoutParams(100, 200)
        lp2.flexGrow = 1.0f
        val view2 = View(activity)
        view2.layoutParams = lp2
        val lp3 = FlexboxLayout.LayoutParams(100, 300)
        val view3 = View(activity)
        view3.layoutParams = lp3
        val lp4 = FlexboxLayout.LayoutParams(100, 400)
        lp4.flexGrow = 2.0f
        val view4 = View(activity)
        view4.layoutParams = lp4
        flexContainer.addView(view1)
        flexContainer.addView(view2)
        flexContainer.addView(view3)
        flexContainer.addView(view4)
        flexContainer.flexDirection = FlexDirection.COLUMN
        flexContainer.flexWrap = FlexWrap.WRAP
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.UNSPECIFIED)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY)
        val result = FlexboxHelper.FlexLinesResult()
        flexboxHelper.calculateVerticalFlexLines(result, widthMeasureSpec, heightMeasureSpec)
        flexContainer.flexLines = result.mFlexLines
        flexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec)

        assertThat(view1.measuredWidth, `is`(100))
        assertThat(view1.measuredHeight, `is`(100))
        assertThat(view2.measuredWidth, `is`(100))
        // view2 will expand to fill the left space in the first flex line since flex grow is set
        assertThat(view2.measuredHeight, `is`(400))
        assertThat(view3.measuredWidth, `is`(100))
        assertThat(view3.measuredHeight, `is`(300))
        assertThat(view4.measuredWidth, `is`(100))
        // view4 will expand to fill the left space in the first flex line since flex grow is set
        assertThat(view4.measuredHeight, `is`(500))
    }

    @Test
    @Throws(Throwable::class)
    fun testDetermineMainSize_direction_row_flexShrinkSet() {
        val activity = activityRule.activity
        val lp1 = FlexboxLayout.LayoutParams(200, 100)
        val view1 = View(activity)
        view1.layoutParams = lp1
        val lp2 = FlexboxLayout.LayoutParams(200, 100)
        val view2 = View(activity)
        view2.layoutParams = lp2
        val lp3 = FlexboxLayout.LayoutParams(200, 100)
        val view3 = View(activity)
        view3.layoutParams = lp3
        val lp4 = FlexboxLayout.LayoutParams(200, 100)
        val view4 = View(activity)
        view4.layoutParams = lp4
        flexContainer.addView(view1)
        flexContainer.addView(view2)
        flexContainer.addView(view3)
        flexContainer.addView(view4)
        flexContainer.flexDirection = FlexDirection.ROW
        flexContainer.flexWrap = FlexWrap.NOWRAP
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.UNSPECIFIED)
        val result = FlexboxHelper.FlexLinesResult()
        flexboxHelper.calculateVerticalFlexLines(result, widthMeasureSpec, heightMeasureSpec)
        flexContainer.flexLines = result.mFlexLines
        flexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec)

        // Flex shrink is set to 1.0 (default value) for all views.
        // They should be shrank equally for the amount overflown the width
        assertThat(view1.measuredWidth, `is`(125))
        assertThat(view1.measuredHeight, `is`(100))
        assertThat(view2.measuredWidth, `is`(125))
        assertThat(view2.measuredHeight, `is`(100))
        assertThat(view3.measuredWidth, `is`(125))
        assertThat(view3.measuredHeight, `is`(100))
        assertThat(view4.measuredWidth, `is`(125))
        assertThat(view4.measuredHeight, `is`(100))
    }

    @Test
    @Throws(Throwable::class)
    fun testDetermineMainSize_direction_column_flexShrinkSet() {
        val activity = activityRule.activity
        val lp1 = FlexboxLayout.LayoutParams(100, 200)
        val view1 = View(activity)
        view1.layoutParams = lp1
        val lp2 = FlexboxLayout.LayoutParams(100, 200)
        val view2 = View(activity)
        view2.layoutParams = lp2
        val lp3 = FlexboxLayout.LayoutParams(100, 200)
        val view3 = View(activity)
        view3.layoutParams = lp3
        val lp4 = FlexboxLayout.LayoutParams(100, 200)
        val view4 = View(activity)
        view4.layoutParams = lp4
        flexContainer.addView(view1)
        flexContainer.addView(view2)
        flexContainer.addView(view3)
        flexContainer.addView(view4)
        flexContainer.flexDirection = FlexDirection.COLUMN
        flexContainer.flexWrap = FlexWrap.NOWRAP
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.UNSPECIFIED)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY)
        val result = FlexboxHelper.FlexLinesResult()
        flexboxHelper.calculateVerticalFlexLines(result, widthMeasureSpec, heightMeasureSpec)
        flexContainer.flexLines = result.mFlexLines
        flexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec)

        // Flex shrink is set to 1.0 (default value) for all views.
        // They should be shrank equally for the amount overflown the height
        assertThat(view1.measuredWidth, `is`(100))
        assertThat(view1.measuredHeight, `is`(125))
        assertThat(view2.measuredWidth, `is`(100))
        assertThat(view2.measuredHeight, `is`(125))
        assertThat(view3.measuredWidth, `is`(100))
        assertThat(view3.measuredHeight, `is`(125))
        assertThat(view4.measuredWidth, `is`(100))
        assertThat(view4.measuredHeight, `is`(125))
    }

    @Test
    @Throws(Throwable::class)
    fun testDetermineMainSize_directionRow_fixedSizeViewAndShrinkable_doNotExceedMaxMainSize() {
        val activity = activityRule.activity
        val lp1 = FlexboxLayout.LayoutParams(100, 100)
        val view1 = View(activity)
        lp1.flexShrink = 0f
        view1.layoutParams = lp1
        val lp2 = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT)
        val view2 = TextView(activity)
        view2.layoutParams = lp2
        view2.text = LONG_TEXT
        flexContainer.addView(view1)
        flexContainer.addView(view2)
        flexContainer.flexWrap = FlexWrap.NOWRAP
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.AT_MOST)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.UNSPECIFIED)
        val result = FlexboxHelper.FlexLinesResult()
        flexboxHelper.calculateHorizontalFlexLines(result, widthMeasureSpec, heightMeasureSpec)
        flexContainer.flexLines = result.mFlexLines
        flexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec)

        // Container with WRAP_CONTENT and a max width forces resizable children to shrink
        // to avoid exceeding max available space.
        assertThat(view1.measuredWidth, `is`(100))
        assertThat(view2.measuredWidth, `is`(400))
    }

    @Test
    @Throws(Throwable::class)
    fun testDetermineMainSize_directionRow_twoFixedSizeViewsAndShrinkable_doNotExceedMaxMainSize() {
        val activity = activityRule.activity
        val lp1 = FlexboxLayout.LayoutParams(100, 100)
        val view1 = View(activity)
        lp1.flexShrink = 0f
        view1.layoutParams = lp1
        val lp2 = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT)
        val view2 = TextView(activity)
        view2.layoutParams = lp2
        view2.text = LONG_TEXT
        val lp3 = FlexboxLayout.LayoutParams(100, 100)
        val view3 = View(activity)
        lp3.flexShrink = 0f
        view3.layoutParams = lp3
        flexContainer.addView(view1)
        flexContainer.addView(view2)
        flexContainer.addView(view3)
        flexContainer.flexWrap = FlexWrap.NOWRAP
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.AT_MOST)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.UNSPECIFIED)
        val result = FlexboxHelper.FlexLinesResult()
        flexboxHelper.calculateHorizontalFlexLines(result, widthMeasureSpec, heightMeasureSpec)
        flexContainer.flexLines = result.mFlexLines
        flexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec)

        // Container with WRAP_CONTENT and a max width forces resizable children to shrink
        // to avoid exceeding max available space.
        assertThat(view1.measuredWidth, `is`(100))
        assertThat(view2.measuredWidth, `is`(300))
        assertThat(view3.measuredWidth, `is`(100))
    }

    @Test
    @Throws(Throwable::class)
    fun testDetermineMainSize_directionRow_considerCompoundButtonImplicitMinSizeWhenNotSpecified() {
        val containerWidth = 500
        val activity = activityRule.activity
        val lp1 = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT)
        val view1 = CheckBox(activity)
        view1.layoutParams = lp1
        val lp2 = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT)
        val view2 = TextView(activity)
        view2.layoutParams = lp2
        view2.text = LONG_TEXT
        flexContainer.addView(view1)
        flexContainer.addView(view2)
        flexContainer.flexWrap = FlexWrap.NOWRAP
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(containerWidth, View.MeasureSpec.AT_MOST)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.UNSPECIFIED)
        val result = FlexboxHelper.FlexLinesResult()
        flexboxHelper.calculateHorizontalFlexLines(result, widthMeasureSpec, heightMeasureSpec)
        flexContainer.flexLines = result.mFlexLines
        flexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec)

        // CompoundButton will use its ButtonDrawable minWidth to determine its size when
        // no minimum width is set on it.
        val drawableMinWidth = CompoundButtonCompat.getButtonDrawable(view1)!!.minimumWidth
        val expectedTextWidth = containerWidth - drawableMinWidth
        assertThat(view1.measuredWidth, `is`(drawableMinWidth))
        assertThat(view2.measuredWidth, `is`(expectedTextWidth))
    }

    @Test
    @Throws(Throwable::class)
    fun testDetermineMainSize_directionRow_considerCompoundButtonExplicitMinSizeWhenSpecified() {
        val containerWidth = 500
        val compoundButtonMinWidth = 150
        val activity = activityRule.activity
        val lp1 = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT)
        lp1.minWidth = compoundButtonMinWidth
        val view1 = CheckBox(activity)
        view1.layoutParams = lp1
        val lp2 = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT)
        val view2 = TextView(activity)
        view2.layoutParams = lp2
        view2.text = LONG_TEXT
        flexContainer.addView(view1)
        flexContainer.addView(view2)
        flexContainer.flexWrap = FlexWrap.NOWRAP
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(containerWidth, View.MeasureSpec.AT_MOST)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.UNSPECIFIED)
        val result = FlexboxHelper.FlexLinesResult()
        flexboxHelper.calculateHorizontalFlexLines(result, widthMeasureSpec, heightMeasureSpec)
        flexContainer.flexLines = result.mFlexLines
        flexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec)

        // CompoundButton will be measured based on its explicitly specified minWidth.
        val expectedTextWidth = containerWidth - compoundButtonMinWidth
        assertThat(view1.measuredWidth, `is`(compoundButtonMinWidth))
        assertThat(view2.measuredWidth, `is`(expectedTextWidth))
    }

    @Test
    @Throws(Throwable::class)
    fun testDetermineCrossSize_direction_row_alignContent_stretch() {
        val activity = activityRule.activity
        val lp1 = FlexboxLayout.LayoutParams(100, 100)
        val view1 = View(activity)
        view1.layoutParams = lp1
        val lp2 = FlexboxLayout.LayoutParams(200, 100)
        val view2 = View(activity)
        view2.layoutParams = lp2
        val lp3 = FlexboxLayout.LayoutParams(300, 100)
        val view3 = View(activity)
        view3.layoutParams = lp3
        val lp4 = FlexboxLayout.LayoutParams(400, 100)
        val view4 = View(activity)
        view4.layoutParams = lp4
        flexContainer.addView(view1)
        flexContainer.addView(view2)
        flexContainer.addView(view3)
        flexContainer.addView(view4)
        flexContainer.flexDirection = FlexDirection.ROW
        flexContainer.flexWrap = FlexWrap.WRAP
        flexContainer.alignContent = AlignContent.STRETCH
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY)
        val result = FlexboxHelper.FlexLinesResult()
        flexboxHelper.calculateHorizontalFlexLines(result, widthMeasureSpec, heightMeasureSpec)
        flexContainer.flexLines = result.mFlexLines
        flexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec)
        flexboxHelper.determineCrossSize(widthMeasureSpec, heightMeasureSpec, 0)
        flexboxHelper.stretchViews()

        // align content is set to Align.STRETCH, the cross size for each flex line is stretched
        // to distribute the remaining free space along the cross axis
        // (remaining height in this case)
        assertThat(view1.measuredHeight, isEqualAllowingError(333))
        assertThat(view2.measuredHeight, isEqualAllowingError(333))
        assertThat(view3.measuredHeight, isEqualAllowingError(333))
        assertThat(view4.measuredHeight, isEqualAllowingError(333))
    }

    @Test
    @Throws(Throwable::class)
    fun testDetermineCrossSize_direction_column_alignContent_stretch() {
        val activity = activityRule.activity
        val lp1 = FlexboxLayout.LayoutParams(100, 100)
        val view1 = View(activity)
        view1.layoutParams = lp1
        val lp2 = FlexboxLayout.LayoutParams(100, 200)
        val view2 = View(activity)
        view2.layoutParams = lp2
        val lp3 = FlexboxLayout.LayoutParams(100, 300)
        val view3 = View(activity)
        view3.layoutParams = lp3
        val lp4 = FlexboxLayout.LayoutParams(100, 400)
        val view4 = View(activity)
        view4.layoutParams = lp4
        flexContainer.addView(view1)
        flexContainer.addView(view2)
        flexContainer.addView(view3)
        flexContainer.addView(view4)
        flexContainer.flexDirection = FlexDirection.COLUMN
        flexContainer.flexWrap = FlexWrap.WRAP
        flexContainer.alignContent = AlignContent.STRETCH
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY)
        val result = FlexboxHelper.FlexLinesResult()
        flexboxHelper.calculateVerticalFlexLines(result, widthMeasureSpec, heightMeasureSpec)
        flexContainer.flexLines = result.mFlexLines
        flexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec)
        flexboxHelper.determineCrossSize(widthMeasureSpec, heightMeasureSpec, 0)
        flexboxHelper.stretchViews()

        // align content is set to Align.STRETCH, the cross size for each flex line is stretched
        // to distribute the remaining free space along the cross axis
        // (remaining width in this case)
        assertThat(view1.measuredWidth, isEqualAllowingError(333))
        assertThat(view2.measuredWidth, isEqualAllowingError(333))
        assertThat(view3.measuredWidth, isEqualAllowingError(333))
        assertThat(view4.measuredWidth, isEqualAllowingError(333))
    }

    @Test
    fun testMakeCombinedLong() {
        var higher = -1
        var lower = 10
        var combined = flexboxHelper.makeCombinedLong(lower, higher)
        assertThat(flexboxHelper.extractHigherInt(combined), `is`(higher))
        assertThat(flexboxHelper.extractLowerInt(combined), `is`(lower))

        higher = Integer.MAX_VALUE
        lower = Integer.MIN_VALUE
        combined = flexboxHelper.makeCombinedLong(lower, higher)
        assertThat(flexboxHelper.extractHigherInt(combined), `is`(higher))
        assertThat(flexboxHelper.extractLowerInt(combined), `is`(lower))

        higher = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY)
        lower = View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.UNSPECIFIED)
        combined = flexboxHelper.makeCombinedLong(lower, higher)
        assertThat(flexboxHelper.extractHigherInt(combined), `is`(higher))
        assertThat(flexboxHelper.extractLowerInt(combined), `is`(lower))
    }

    @Test
    fun testFlexLine_anyItemsHaveFlexGrow() {
        val activity = activityRule.activity
        val lp1 = FlexboxLayout.LayoutParams(100, 100).apply {
            flexGrow = 1.0f
        }
        val view1 = View(activity)
        view1.layoutParams = lp1
        val lp2 = FlexboxLayout.LayoutParams(100, 200)
        val view2 = View(activity)
        view2.layoutParams = lp2
        val lp3 = FlexboxLayout.LayoutParams(100, 300)
        val view3 = View(activity)
        view3.layoutParams = lp3
        val lp4 = FlexboxLayout.LayoutParams(100, 400).apply {
            flexGrow = 2.0f
        }
        val view4 = View(activity)
        view4.layoutParams = lp4
        flexContainer.apply {
            addView(view1)
            addView(view2)
            addView(view3)
            addView(view4)
            flexDirection = FlexDirection.COLUMN
            flexWrap = FlexWrap.WRAP
            alignContent = AlignContent.STRETCH
        }
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY)
        val result = FlexboxHelper.FlexLinesResult()
        flexboxHelper.calculateVerticalFlexLines(result, widthMeasureSpec, heightMeasureSpec)
        flexContainer.flexLines = result.mFlexLines
        assertThat(flexContainer.flexLines.size, `is`(3))
        assertThat(flexContainer.flexLines[0].mAnyItemsHaveFlexGrow, `is`(true))
        assertThat(flexContainer.flexLines[1].mAnyItemsHaveFlexGrow, `is`(false))
        assertThat(flexContainer.flexLines[2].mAnyItemsHaveFlexGrow, `is`(true))
    }
}
