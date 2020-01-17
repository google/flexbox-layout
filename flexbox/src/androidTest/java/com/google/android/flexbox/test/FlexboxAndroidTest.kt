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

package com.google.android.flexbox.test

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.res.ResourcesCompat
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.PositionAssertions.isBottomAlignedWith
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyLeftOf
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyRightOf
import androidx.test.espresso.assertion.PositionAssertions.isLeftAlignedWith
import androidx.test.espresso.assertion.PositionAssertions.isRightAlignedWith
import androidx.test.espresso.assertion.PositionAssertions.isTopAlignedWith
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.FlakyTest
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.google.android.flexbox.AlignContent
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import com.google.android.flexbox.test.IsEqualAllowingError.Companion.isEqualAllowingError
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNot.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for [FlexboxLayout].
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class FlexboxAndroidTest {

    @JvmField
    @Rule
    var activityRule = ActivityTestRule(FlexboxTestActivity::class.java)

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testLoadFromLayoutXml() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_simple)

        assertNotNull(flexboxLayout)
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW_REVERSE))
        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.CENTER))
        assertThat(flexboxLayout.alignContent, `is`(AlignContent.CENTER))
        assertThat(flexboxLayout.alignItems, `is`(AlignItems.CENTER))
        assertThat(flexboxLayout.childCount, `is`(1))

        val child = flexboxLayout.getChildAt(0)
        val lp = child.layoutParams as FlexboxLayout.LayoutParams
        assertThat(lp.order, `is`(2))
        assertThat(lp.flexGrow, `is`(1f))
        assertThat(lp.alignSelf, `is`(AlignItems.STRETCH))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testOrderAttribute_fromLayoutXml() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_order_test)

        assertNotNull(flexboxLayout)
        assertThat(flexboxLayout.childCount, `is`(4))
        // order: -1, index 1
        assertThat((flexboxLayout.getReorderedChildAt(0) as TextView).text.toString(), `is`("2"))
        // order: 0, index 2
        assertThat((flexboxLayout.getReorderedChildAt(1) as TextView).text.toString(), `is`("3"))
        // order: 1, index 3
        assertThat((flexboxLayout.getReorderedChildAt(2) as TextView).text.toString(), `is`("4"))
        // order: 2, index 0
        assertThat((flexboxLayout.getReorderedChildAt(3) as TextView).text.toString(), `is`("1"))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testOrderAttribute_fromCode() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_order_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        val fifth = createTextView(activity, "5", 0)
                        val sixth = createTextView(activity, "6", -10)
                        flexboxLayout.addView(fifth)
                        flexboxLayout.addView(sixth)
                    }
                })

        assertThat(flexboxLayout.childCount, `is`(6))
        // order: -10, index 5
        assertThat((flexboxLayout.getReorderedChildAt(0) as TextView).text.toString(), `is`("6"))
        // order: -1, index 1
        assertThat((flexboxLayout.getReorderedChildAt(1) as TextView).text.toString(), `is`("2"))
        // order: 0, index 2
        assertThat((flexboxLayout.getReorderedChildAt(2) as TextView).text.toString(), `is`("3"))
        // order: 0, index 4
        assertThat((flexboxLayout.getReorderedChildAt(3) as TextView).text.toString(), `is`("5"))
        // order: 1, index 3
        assertThat((flexboxLayout.getReorderedChildAt(4) as TextView).text.toString(), `is`("4"))
        // order: 2, index 0
        assertThat((flexboxLayout.getReorderedChildAt(5) as TextView).text.toString(), `is`("1"))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testChangeOrder_fromChildSetLayoutParams() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_order_test)

        assertThat(flexboxLayout.childCount, `is`(4))
        // order: -1, index 1
        assertThat((flexboxLayout.getReorderedChildAt(0) as TextView).text.toString(), `is`("2"))
        // order: 0, index 2
        assertThat((flexboxLayout.getReorderedChildAt(1) as TextView).text.toString(), `is`("3"))
        // order: 0, index 3
        assertThat((flexboxLayout.getReorderedChildAt(2) as TextView).text.toString(), `is`("4"))
        // order: 2, index 0
        assertThat((flexboxLayout.getReorderedChildAt(3) as TextView).text.toString(), `is`("1"))

        // By changing the order and calling the setLayoutParams, the reordered array in the
        // FlexboxLayout (mReorderedIndices) will be recreated without adding a new View.
        activityRule.runOnUiThread {
            val view1 = flexboxLayout.getChildAt(0)
            val lp = view1.layoutParams as FlexboxLayout.LayoutParams
            lp.order = -3
            view1.layoutParams = lp
        }
        // order: -3, index 0
        assertThat((flexboxLayout.getReorderedChildAt(3) as TextView).text.toString(), `is`("1"))
        // order: -1, index 1
        assertThat((flexboxLayout.getReorderedChildAt(0) as TextView).text.toString(), `is`("2"))
        // order: 0, index 2
        assertThat((flexboxLayout.getReorderedChildAt(1) as TextView).text.toString(), `is`("3"))
        // order: 1, index 3
        assertThat((flexboxLayout.getReorderedChildAt(2) as TextView).text.toString(), `is`("4"))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testOrderAttribute_addViewInMiddle() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_order_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        val fifth = createTextView(activity, "5", 0)
                        // Add the new TextView in the middle of the indices
                        flexboxLayout.addView(fifth, 2)
                    }
                })

        assertNotNull(flexboxLayout)
        assertThat(flexboxLayout.childCount, `is`(5))
        // order: -1, index 1
        assertThat((flexboxLayout.getReorderedChildAt(0) as TextView).text.toString(), `is`("2"))
        // order: 0, index 2
        assertThat((flexboxLayout.getReorderedChildAt(1) as TextView).text.toString(), `is`("5"))
        // order: 0, index 3
        assertThat((flexboxLayout.getReorderedChildAt(2) as TextView).text.toString(), `is`("3"))
        // order: 0, index 4
        assertThat((flexboxLayout.getReorderedChildAt(3) as TextView).text.toString(), `is`("4"))
        // order: 2, index 0
        assertThat((flexboxLayout.getReorderedChildAt(4) as TextView).text.toString(), `is`("1"))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testOrderAttribute_removeLastView() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_order_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.removeViewAt(flexboxLayout.childCount - 1)
                    }
                })

        assertNotNull(flexboxLayout)
        assertThat(flexboxLayout.childCount, `is`(3))
        // order: -1, index 1
        assertThat((flexboxLayout.getReorderedChildAt(0) as TextView).text.toString(), `is`("2"))
        // order: 0, index 2
        assertThat((flexboxLayout.getReorderedChildAt(1) as TextView).text.toString(), `is`("3"))
        // order: 2, index 0
        assertThat((flexboxLayout.getReorderedChildAt(2) as TextView).text.toString(), `is`("1"))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testOrderAttribute_removeViewInMiddle() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_order_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.removeViewAt(2)
                    }
                })

        assertNotNull(flexboxLayout)
        assertThat(flexboxLayout.childCount, `is`(3))
        // order: -1, index 1
        assertThat((flexboxLayout.getReorderedChildAt(0) as TextView).text.toString(), `is`("2"))
        // order: 0, index 3
        assertThat((flexboxLayout.getReorderedChildAt(1) as TextView).text.toString(), `is`("4"))
        // order: 2, index 0
        assertThat((flexboxLayout.getReorderedChildAt(2) as TextView).text.toString(), `is`("1"))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexWrap_wrap() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_flex_wrap_test)

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        // The width of the FlexboxLayout is not enough for placing the three text views.
        // The third text view should be placed below the first one
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        val flexLines = flexboxLayout.flexLines
        assertThat(flexLines.size, `is`(2))
        val flexLine1 = flexLines[0]
        val activity = activityRule.activity
        assertThat(flexLine1.mainSize, `is`(activity.dpToPixel(320)))
        assertThat(flexLine1.crossSize, `is`(activity.dpToPixel(120)))
        val flexLine2 = flexLines[1]
        assertThat(flexLine2.mainSize, `is`(activity.dpToPixel(160)))
        assertThat(flexLine2.crossSize, `is`(activity.dpToPixel(120)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexWrap_nowrap() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_flex_wrap_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexWrap = FlexWrap.NOWRAP
                        flexboxLayout.alignItems = AlignItems.FLEX_START
                    }
                })

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.NOWRAP))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        // The width of the FlexboxLayout is not enough for placing the three text views.
        // But the flexWrap attribute is set to NOWRAP, the third text view is placed
        // to the right of the second one and overflowing the parent FlexboxLayout.
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        assertThat(flexboxLayout.flexLines.size, `is`(1))
        val flexLines = flexboxLayout.flexLines
        assertThat(flexLines.size, `is`(1))
        val flexLine = flexLines[0]
        val activity = activityRule.activity
        assertThat(flexLine.mainSize, `is`(activity.dpToPixel(480)))
        assertThat(flexLine.crossSize, `is`(activity.dpToPixel(300)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexWrap_wrap_reverse() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_flex_wrap_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexWrap = FlexWrap.WRAP_REVERSE
                    }
                })

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP_REVERSE))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        // The width of the FlexboxLayout is not enough for placing the three text views.
        // There should be two flex lines same as WRAP, but the layout starts from bottom
        // to top in FlexWrap.WRAP_REVERSE
        onView(withId(R.id.text3)).check(isCompletelyAbove(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyAbove(withId(R.id.text2)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        assertThat(flexboxLayout.flexLines.size, `is`(2))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexWrap_wrap_flexDirection_column() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_flex_wrap_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        // The height of the FlexboxLayout is not enough for placing the three text views.
        // The third text view should be placed right of the first one
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        assertThat(flexboxLayout.flexLines.size, `is`(2))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexWrap_nowrap_flexDirection_column() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_flex_wrap_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                        flexboxLayout.flexWrap = FlexWrap.NOWRAP
                    }
                })

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.NOWRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))

        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        // The height of the FlexboxLayout is not enough for placing the three text views.
        // But the flexWrap attribute is set to NOWRAP, the third text view is placed
        // below the second one and overflowing the parent FlexboxLayout.
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        assertThat(flexboxLayout.flexLines.size, `is`(1))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexWrap_wrap_reverse_flexDirection_column() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_flex_wrap_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                        flexboxLayout.flexWrap = FlexWrap.WRAP_REVERSE
                    }
                })

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP_REVERSE))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isRightAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        // The width of the FlexboxLayout is not enough for placing the three text views.
        // There should be two flex lines same as WRAP, but the layout starts from right
        // to left in FlexWrap.WRAP_REVERSE
        onView(withId(R.id.text3)).check(isCompletelyLeftOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyLeftOf(withId(R.id.text2)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        assertThat(flexboxLayout.flexLines.size, `is`(2))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexItem_match_parent() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_flex_item_match_parent)
        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        val text3 = activity.findViewById<TextView>(R.id.text3)

        assertThat(text1.width, `is`(flexboxLayout.width))
        assertThat(text2.width, `is`(flexboxLayout.width))
        assertThat(text3.width, `is`(flexboxLayout.width))
        assertThat(flexboxLayout.height, `is`(text1.height + text2.height + text3.height))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexItem_match_parent_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_flex_item_match_parent_direction_column,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        val text3 = activity.findViewById<TextView>(R.id.text3)

        assertThat(text1.height, `is`(flexboxLayout.height))
        assertThat(text2.height, `is`(flexboxLayout.height))
        assertThat(text3.height, `is`(flexboxLayout.height))
        assertThat(flexboxLayout.width, `is`(text1.width + text2.width + text3.width))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexboxLayout_wrapContent() {
        createFlexboxLayout(R.layout.activity_flexbox_wrap_content)
        // The parent FlexboxLayout's layout_width and layout_height are set to wrap_content
        // The size of the FlexboxLayout is aligned with three text views.

        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))

        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))

        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexboxLayout_wrapped_with_ScrollView() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_flexbox_wrapped_with_scrollview)

        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))

        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))

        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))

        // The heightMode of the FlexboxLayout is set as MeasureSpec.UNSPECIFIED, the height of the
        // layout will be expanded to include the all children views
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(flexboxLayout.height, `is`(textView1.height + textView3.height))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexboxLayout_wrapped_with_HorizontalScrollView() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_flexbox_wrapped_with_horizontalscrollview)

        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))

        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))

        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))

        // The widthMode of the FlexboxLayout is set as MeasureSpec.UNSPECIFIED, the width of the
        // layout will be expanded to include the all children views
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(flexboxLayout.width, `is`(textView1.width + textView2.width + textView3.width))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_flexStart() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test)

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.FLEX_START))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_flexStart_withParentPadding() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_justify_content_with_parent_padding)

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.FLEX_START))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))
        val text1 = activity.findViewById<TextView>(R.id.text1)
        // Both the parent FrameLayout and the FlexboxLayout have different padding values
        // but the text1.getLeft should be the padding value for the FlexboxLayout, not including
        // the parent's padding value
        assertThat(text1.left, `is`(flexboxLayout.paddingLeft))
        assertThat(text1.top, `is`(flexboxLayout.paddingTop))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_flexEnd() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.FLEX_END
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.FLEX_END))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyLeftOf(withId(R.id.text3)))
        onView(withId(R.id.text1)).check(isCompletelyLeftOf(withId(R.id.text2)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_flexEnd_withParentPadding() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_justify_content_with_parent_padding,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.FLEX_END
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.FLEX_END))
        onView(withId(R.id.text2)).check(isCompletelyLeftOf(withId(R.id.text3)))
        onView(withId(R.id.text1)).check(isCompletelyLeftOf(withId(R.id.text2)))
        val text3 = activity.findViewById<TextView>(R.id.text3)
        // Both the parent FrameLayout and the FlexboxLayout have different padding values
        // but the text3.getRight should be the padding value for the FlexboxLayout, not including
        // the parent's padding value
        assertThat(flexboxLayout.width - text3.right, `is`(flexboxLayout.paddingRight))
        assertThat(text3.top, `is`(flexboxLayout.paddingTop))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_center() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.CENTER
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.CENTER))

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        val space = (flexboxLayout.width - textView1.width - textView2.width - textView3.width) / 2
        assertThat(textView1.left, isEqualAllowingError(space))
        assertThat(flexboxLayout.right - textView3.right, isEqualAllowingError(space))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_center_withParentPadding() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_justify_content_with_parent_padding,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.CENTER
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.CENTER))

        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        var space = flexboxLayout.width - textView1.width - textView2.width -
                textView3.width - flexboxLayout.paddingLeft - flexboxLayout.paddingRight
        space /= 2
        assertThat(textView1.left - flexboxLayout.paddingLeft, isEqualAllowingError(space))
        assertThat(flexboxLayout.width - textView3.right - flexboxLayout.paddingRight,
                isEqualAllowingError(space))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceBetween() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.SPACE_BETWEEN
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.SPACE_BETWEEN))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        var space = flexboxLayout.width - textView1.width - textView2.width - textView3.width
        space /= 2
        assertThat(textView2.left - textView1.right, isEqualAllowingError(space))
        assertThat(textView3.left - textView2.right, isEqualAllowingError(space))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceBetween_withPadding() {
        val activity = activityRule.activity
        val padding = 40
        val flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.SPACE_BETWEEN
                        flexboxLayout.setPadding(padding, padding, padding, padding)
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.SPACE_BETWEEN))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        var space = flexboxLayout.width - textView1.width - textView2.width - textView3.width -
                padding * 2
        space /= 2
        assertThat(textView1.left, `is`(padding))
        assertThat(flexboxLayout.right - textView3.right, `is`(padding))
        assertThat(textView2.left - textView1.right, isEqualAllowingError(space))
        assertThat(textView3.left - textView2.right, isEqualAllowingError(space))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceAround() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.SPACE_AROUND
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.SPACE_AROUND))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        var space = flexboxLayout.width - textView1.width - textView2.width - textView3.width
        space /= 6 // Divide by the number of children * 2
        assertTrue(space - 1 <= textView1.left && textView1.left <= space + 1)
        val spaceInMiddle = space * 2
        assertThat(textView2.left - textView1.right, isEqualAllowingError(spaceInMiddle))
        assertThat(textView3.left - textView2.right, isEqualAllowingError(spaceInMiddle))
        assertThat(flexboxLayout.right - textView3.right, isEqualAllowingError(space))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceEvenly() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.SPACE_EVENLY
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.SPACE_EVENLY))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        var space = flexboxLayout.width - textView1.width - textView2.width - textView3.width
        space /= 4 // Divide by the number of children + 1
        assertThat(textView1.left, isEqualAllowingError(space))
        assertThat(textView2.left - textView1.right, isEqualAllowingError(space))
        assertThat(textView3.left - textView2.right, isEqualAllowingError(space))
        assertThat(flexboxLayout.right - textView3.right, isEqualAllowingError(space))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceAround_withPadding() {
        val activity = activityRule.activity
        val padding = 40
        val flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.SPACE_AROUND
                        flexboxLayout.setPadding(padding, padding, padding, padding)
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.SPACE_AROUND))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        var space = flexboxLayout.width - textView1.width - textView2.width - textView3.width -
                padding * 2
        space /= 6 // Divide by the number of children * 2
        assertThat(textView1.left - padding, isEqualAllowingError(space))

        val spaceInMiddle = space * 2
        assertThat(textView2.left - textView1.right, isEqualAllowingError(spaceInMiddle))
        assertThat(textView3.left - textView2.right, isEqualAllowingError(spaceInMiddle))
        assertThat(flexboxLayout.right - textView3.right - padding, isEqualAllowingError(space))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceEvenly_withPadding() {
        val activity = activityRule.activity
        val padding = 40
        val flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.SPACE_EVENLY
                        flexboxLayout.setPadding(padding, padding, padding, padding)
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.SPACE_EVENLY))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        var space = flexboxLayout.width - textView1.width - textView2.width - textView3.width -
                padding * 2
        space /= 4 // Divide by the number of children + 1
        assertThat(textView1.left - padding, isEqualAllowingError(space))

        assertThat(textView2.left - textView1.right, isEqualAllowingError(space))
        assertThat(textView3.left - textView2.right, isEqualAllowingError(space))
        assertThat(flexboxLayout.right - textView3.right - padding, isEqualAllowingError(space))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_flexStart_flexDirection_column() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.FLEX_START))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_flexEnd_flexDirection_column() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.FLEX_END
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                        flexboxLayout.alignItems = AlignItems.STRETCH
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.FLEX_END))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyAbove(withId(R.id.text3)))
        onView(withId(R.id.text1)).check(isCompletelyAbove(withId(R.id.text2)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_center_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.CENTER
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.CENTER))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        var space = flexboxLayout.height - textView1.height - textView2.height - textView3.height
        space /= 2
        assertThat(textView1.top, isEqualAllowingError(space))
        assertThat(flexboxLayout.bottom - textView3.bottom, isEqualAllowingError(space))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceBetween_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.SPACE_BETWEEN
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.SPACE_BETWEEN))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        var space = flexboxLayout.height - textView1.height - textView2.height - textView3.height
        space /= 2
        assertThat(textView2.top - textView1.bottom, isEqualAllowingError(space))
        assertThat(textView3.top - textView2.bottom, isEqualAllowingError(space))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceBetween_flexDirection_column_withPadding() {
        val activity = activityRule.activity
        val padding = 40
        val flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.SPACE_BETWEEN
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                        flexboxLayout.setPadding(padding, padding, padding, padding)
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.SPACE_BETWEEN))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        var space = flexboxLayout.height - textView1.height - textView2.height - textView3.height - padding * 2
        space /= 2
        assertThat(textView1.top, `is`(padding))
        assertThat(flexboxLayout.bottom - textView3.bottom, `is`(padding))
        assertThat(textView2.top - textView1.bottom, isEqualAllowingError(space))
        assertThat(textView3.top - textView2.bottom, isEqualAllowingError(space))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceAround_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.SPACE_AROUND
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.SPACE_AROUND))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        var space = flexboxLayout.height - textView1.height - textView2.height - textView3.height
        space /= 6 // Divide by the number of children * 2
        assertThat(textView1.top, isEqualAllowingError(space))
        val spaceInMiddle = space * 2
        assertThat(textView2.top - textView1.bottom, isEqualAllowingError(spaceInMiddle))
        assertThat(textView3.top - textView2.bottom, isEqualAllowingError(spaceInMiddle))
        assertThat(flexboxLayout.bottom - textView3.bottom, isEqualAllowingError(space))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceEvenly_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.SPACE_EVENLY
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.SPACE_EVENLY))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        var space = flexboxLayout.height - textView1.height - textView2.height - textView3.height
        space /= 4 // Divide by the number of children + 1
        assertThat(textView1.top, isEqualAllowingError(space))
        assertThat(textView2.top - textView1.bottom, isEqualAllowingError(space))
        assertThat(textView3.top - textView2.bottom, isEqualAllowingError(space))
        assertThat(flexboxLayout.bottom - textView3.bottom, isEqualAllowingError(space))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceAround_flexDirection_column_withPadding() {
        val activity = activityRule.activity
        val padding = 40
        val flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.SPACE_AROUND
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                        flexboxLayout.setPadding(padding, padding, padding, padding)
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.SPACE_AROUND))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        var space = flexboxLayout.height - textView1.height - textView2.height - textView3.height -
                padding * 2
        space /= 6 // Divide by the number of children * 2
        assertThat(textView1.top - padding, isEqualAllowingError(space))
        val spaceInMiddle = space * 2
        assertThat(textView2.top - textView1.bottom, isEqualAllowingError(spaceInMiddle))
        assertThat(textView3.top - textView2.bottom, isEqualAllowingError(spaceInMiddle))
        assertThat(flexboxLayout.bottom - textView3.bottom - padding, isEqualAllowingError(space))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceEvenly_flexDirection_column_withPadding() {
        val activity = activityRule.activity
        val padding = 40
        val flexboxLayout = createFlexboxLayout(R.layout.activity_justify_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.SPACE_EVENLY
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                        flexboxLayout.setPadding(padding, padding, padding, padding)
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.SPACE_EVENLY))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        var space = flexboxLayout.height - textView1.height - textView2.height - textView3.height -
                padding * 2
        space /= 4 // Divide by the number of children + 1
        assertThat(textView1.top - padding, isEqualAllowingError(space))
        assertThat(textView2.top - textView1.bottom, isEqualAllowingError(space))
        assertThat(textView3.top - textView2.bottom, isEqualAllowingError(space))
        assertThat(flexboxLayout.bottom - textView3.bottom - padding,
                isEqualAllowingError(space))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceAround_including_gone_views() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_justify_content_with_gone,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.SPACE_AROUND
                    }
                }
        )

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.SPACE_AROUND))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        var space = flexboxLayout.width - textView1.width - textView3.width
        space /= 4 // Divide by the number of visible children * 2
        assertThat(textView1.left, isEqualAllowingError(space))
        val spaceInMiddle = space * 2
        assertThat(textView3.left - textView1.right, isEqualAllowingError(spaceInMiddle))
        assertThat(flexboxLayout.right - textView3.right, isEqualAllowingError(space))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceBetween_including_gone_views() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_justify_content_with_gone,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.justifyContent = JustifyContent.SPACE_BETWEEN
                    }
                })

        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.SPACE_BETWEEN))

        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)))
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        val space = flexboxLayout.width - textView1.width - textView3.width
        assertThat(textView3.left - textView1.right, isEqualAllowingError(space))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceAround_including_gone_views_direction_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_justify_content_with_gone,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                        flexboxLayout.justifyContent = JustifyContent.SPACE_AROUND
                    }
                })

        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.SPACE_AROUND))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        var space = flexboxLayout.height - textView1.height - textView3.height
        space /= 4 // Divide by the number of visible children * 2
        assertThat(textView1.top, isEqualAllowingError(space))
        val spaceInMiddle = space * 2
        assertThat(textView3.top - textView1.bottom, isEqualAllowingError(spaceInMiddle))
        assertThat(flexboxLayout.bottom - textView3.bottom, isEqualAllowingError(space))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceBetween_including_gone_views_direction_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_justify_content_with_gone,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                        flexboxLayout.justifyContent = JustifyContent.SPACE_BETWEEN
                    }
                })

        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.SPACE_BETWEEN))

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        val space = flexboxLayout.height - textView1.height - textView3.height
        assertThat(textView3.top - textView1.bottom, isEqualAllowingError(space))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexGrow_withExactParentLength() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_flex_grow_test)

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        // the third TextView is expanded to the right edge of the FlexboxLayout
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(textView3.width, `is`(flexboxLayout.width - textView1.width - textView2.width))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexGrow_withExactParentLength_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_flex_grow_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        // the third TextView is expanded to the bottom edge of the FlexboxLayout
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(textView3.height, `is`(flexboxLayout.height - textView1.height - textView2.height))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexGrow_including_view_gone() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_flex_grow_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        val textView2 = activity.findViewById<TextView>(R.id.text2)
                        textView2.visibility = View.GONE
                    }
                })

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        // the third TextView is expanded to the right edge of the FlexboxLayout
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text1)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(textView2.visibility, `is`(View.GONE))
        assertThat(textView3.width, `is`(flexboxLayout.width - textView1.width))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_stretch() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test)

        assertThat(flexboxLayout.alignContent, `is`(AlignContent.STRETCH))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        // the third TextView is wrapped to the next flex line
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))

        val textView3 = activity.findViewById<TextView>(R.id.text3)
        val flexLineCrossSize = flexboxLayout.height / 2
        // Two flex line's cross sizes are expanded to the half of the height of the FlexboxLayout.
        // The third textView's top should be aligned width the second flex line.
        assertThat(textView3.top, `is`(flexLineCrossSize))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_flexStart() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignContent = AlignContent.FLEX_START
                    }
                })

        assertThat(flexboxLayout.alignContent, `is`(AlignContent.FLEX_START))

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        // the third TextView is wrapped to the next flex line
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(textView3.top, `is`(textView1.height))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_flexEnd() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignContent = AlignContent.FLEX_END
                    }
                })

        assertThat(flexboxLayout.alignContent, `is`(AlignContent.FLEX_END))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isCompletelyAbove(withId(R.id.text3)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyAbove(withId(R.id.text3)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(textView1.bottom, `is`(flexboxLayout.bottom - textView3.height))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_flexEnd_parentPadding() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignContent = AlignContent.FLEX_END
                        flexboxLayout.setPadding(32, 32, 32, 32)
                    }
                })

        assertThat(flexboxLayout.alignContent, `is`(AlignContent.FLEX_END))
        onView(withId(R.id.text1)).check(isCompletelyAbove(withId(R.id.text3)))
        onView(withId(R.id.text2)).check(isCompletelyAbove(withId(R.id.text3)))

        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(textView3.bottom, `is`(flexboxLayout.bottom - flexboxLayout.paddingBottom))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_flexEnd_parentPadding_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignContent = AlignContent.FLEX_END
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                        flexboxLayout.setPadding(32, 32, 32, 32)
                    }
                })

        assertThat(flexboxLayout.alignContent, `is`(AlignContent.FLEX_END))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text1)).check(isCompletelyLeftOf(withId(R.id.text3)))
        onView(withId(R.id.text2)).check(isCompletelyLeftOf(withId(R.id.text3)))

        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(textView3.right, `is`(flexboxLayout.right - flexboxLayout.paddingRight))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_center() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignContent = AlignContent.CENTER
                    }
                })

        assertThat(flexboxLayout.alignContent, `is`(AlignContent.CENTER))

        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text1)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        var spaceAboveAndBottom = flexboxLayout.height - textView1.height - textView3.height
        spaceAboveAndBottom /= 2

        assertThat(textView1.top, isEqualAllowingError(spaceAboveAndBottom))
        assertThat(flexboxLayout.bottom - textView3.bottom, isEqualAllowingError(spaceAboveAndBottom))
        assertThat(flexboxLayout.flexLines.size, `is`(2))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_spaceBetween() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignContent = AlignContent.SPACE_BETWEEN
                    }
                })

        assertThat(flexboxLayout.alignContent, `is`(AlignContent.SPACE_BETWEEN))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        assertThat(flexboxLayout.flexLines.size, `is`(2))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_spaceBetween_withPadding() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignContent = AlignContent.SPACE_BETWEEN
                    }
                })

        assertThat(flexboxLayout.alignContent, `is`(AlignContent.SPACE_BETWEEN))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_spaceAround() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignContent = AlignContent.SPACE_AROUND
                    }
                })

        assertThat(flexboxLayout.alignContent, `is`(AlignContent.SPACE_AROUND))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView3 = activity.findViewById<TextView>(R.id.text3)

        var spaceAround = flexboxLayout.height - textView1.height - textView3.height
        spaceAround /= 4 // Divide by the number of flex lines * 2

        assertThat(textView1.top, isEqualAllowingError(spaceAround))
        val spaceInMiddle = textView1.bottom + spaceAround * 2
        assertThat(textView3.top, isEqualAllowingError(spaceInMiddle))
        assertThat(flexboxLayout.flexLines.size, `is`(2))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_stretch_parentWrapContent() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        val parentLp = flexboxLayout.layoutParams
                        parentLp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        flexboxLayout.layoutParams = parentLp
                    }
                })

        assertThat(flexboxLayout.alignContent, `is`(AlignContent.STRETCH))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        // the third TextView is wrapped to the next flex line
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))

        // alignContent is only effective if the parent's height/width mode is MeasureSpec.EXACTLY.
        // The size of the flex lines don't change even if the alignContent is set to
        // ALIGN_CONTENT_STRETCH
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(textView3.top, `is`(textView1.height))
        assertThat(flexboxLayout.flexLines.size, `is`(2))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_stretch_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        assertThat(flexboxLayout.alignContent, `is`(AlignContent.STRETCH))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        // the third TextView is wrapped to the next flex line
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))

        val textView3 = activity.findViewById<TextView>(R.id.text3)
        val flexLineCrossSize = flexboxLayout.width / 2
        // Two flex line's cross sizes are expanded to the half of the width of the FlexboxLayout.
        // The third textView's left should be aligned with the second flex line.
        assertThat(textView3.left, `is`(flexLineCrossSize))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_flexStart_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignContent = AlignContent.FLEX_START
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        assertThat(flexboxLayout.alignContent, `is`(AlignContent.FLEX_START))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        // the third TextView is wrapped to the next flex line
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(textView3.left, `is`(textView1.width))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_flexEnd_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignContent = AlignContent.FLEX_END
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        assertThat(flexboxLayout.alignContent, `is`(AlignContent.FLEX_END))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isCompletelyLeftOf(withId(R.id.text3)))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text3)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(textView1.right, `is`(flexboxLayout.right - textView3.width))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_center_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignContent = AlignContent.CENTER
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        assertThat(flexboxLayout.alignContent, `is`(AlignContent.CENTER))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text1)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        val spaceLeftAndRight = (flexboxLayout.width - textView1.width - textView3.width) / 2

        assertThat(textView1.left, isEqualAllowingError(spaceLeftAndRight))
        assertThat(textView3.right, isEqualAllowingError(flexboxLayout.right - spaceLeftAndRight))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_spaceBetween_flexDirection_column() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignContent = AlignContent.SPACE_BETWEEN
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        assertThat(flexboxLayout.alignContent, `is`(AlignContent.SPACE_BETWEEN))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_spaceAround_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignContent = AlignContent.SPACE_AROUND
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        assertThat(flexboxLayout.alignContent, `is`(AlignContent.SPACE_AROUND))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView3 = activity.findViewById<TextView>(R.id.text3)

        var spaceAround = flexboxLayout.width - textView1.width - textView3.width
        spaceAround /= 4 // Divide by the number of flex lines * 2

        assertThat(textView1.left, isEqualAllowingError(spaceAround))
        val spaceInMiddle = textView1.right + spaceAround * 2
        assertThat(textView3.left, isEqualAllowingError(spaceInMiddle))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_stretch_parentWrapContent_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        val parentLp = flexboxLayout.layoutParams
                        parentLp.width = ViewGroup.LayoutParams.WRAP_CONTENT
                        flexboxLayout.layoutParams = parentLp
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        assertThat(flexboxLayout.alignContent, `is`(AlignContent.STRETCH))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        // the third TextView is wrapped to the next flex line
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))

        // alignContent is only effective if the parent's height/width mode is MeasureSpec.EXACTLY.
        // The size of the flex lines don't change even if the alignContent is set to
        // ALIGN_CONTENT_STRETCH
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(textView3.left, `is`(textView1.width))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_flexEnd_wrapReverse_contentOverflowed() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test_overflowed,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignContent = AlignContent.FLEX_END
                        flexboxLayout.flexWrap = FlexWrap.WRAP_REVERSE
                    }
                })
        assertThat(flexboxLayout.alignContent, `is`(AlignContent.FLEX_END))
        onView(withId(R.id.text6)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text5)).check(isCompletelyLeftOf(withId(R.id.text6)))
        onView(withId(R.id.text4)).check(isCompletelyBelow(withId(R.id.text6)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_flexStart_wrapReverse_contentOverflowed() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test_overflowed,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignContent = AlignContent.FLEX_START
                        flexboxLayout.flexWrap = FlexWrap.WRAP_REVERSE
                    }
                })
        assertThat(flexboxLayout.alignContent, `is`(AlignContent.FLEX_START))
        onView(withId(R.id.text1)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyAbove(withId(R.id.text1)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_spaceBetween_wrapReverse_contentOverflowed() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test_overflowed,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignContent = AlignContent.SPACE_BETWEEN
                        flexboxLayout.flexWrap = FlexWrap.WRAP_REVERSE
                    }
                })
        assertThat(flexboxLayout.alignContent, `is`(AlignContent.SPACE_BETWEEN))
        onView(withId(R.id.text1)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyAbove(withId(R.id.text1)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_center_wrapReverse_contentOverflowed() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test_overflowed,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignContent = AlignContent.CENTER
                        flexboxLayout.flexWrap = FlexWrap.WRAP_REVERSE
                    }
                })
        assertThat(flexboxLayout.alignContent, `is`(AlignContent.CENTER))
        val textView6 = activity.findViewById<TextView>(R.id.text6)
        val textView4 = activity.findViewById<TextView>(R.id.text4)
        val textView2 = activity.findViewById<TextView>(R.id.text2)

        assertThat(textView6.top - flexboxLayout.top, isEqualAllowingError(
                (flexboxLayout.height - textView6.height - textView4.height - textView2.height) / 2))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignContent_spaceAround_wrapReverse_contentOverflowed() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_content_test_overflowed,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignContent = AlignContent.SPACE_AROUND
                        flexboxLayout.flexWrap = FlexWrap.WRAP_REVERSE
                    }
                })
        assertThat(flexboxLayout.alignContent, `is`(AlignContent.SPACE_AROUND))
        val textView6 = activity.findViewById<TextView>(R.id.text6)
        val textView4 = activity.findViewById<TextView>(R.id.text4)
        val textView2 = activity.findViewById<TextView>(R.id.text2)

        assertThat(textView6.top - flexboxLayout.top, isEqualAllowingError(
                (flexboxLayout.height - textView6.height - textView4.height - textView2.height) / 2))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignItems_stretch() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_stretch_test)

        assertThat(flexboxLayout.alignItems, `is`(AlignItems.STRETCH))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))

        // There should be 2 flex lines in the layout with the given layout.
        val flexLineSize = flexboxLayout.height / 2
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(textView1.height, isEqualAllowingError(flexLineSize))
        assertThat(textView2.height, isEqualAllowingError(flexLineSize))
        assertThat(textView3.height, isEqualAllowingError(flexLineSize))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignSelf_stretch() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_align_self_stretch_test)

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))

        // There should be 2 flex lines in the layout with the given layout.
        // Only the first TextView's alignSelf is set to ALIGN_SELF_STRETCH
        val flexLineSize = flexboxLayout.height / 2
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(textView1.height, isEqualAllowingError(flexLineSize))
        assertThat(textView2.height, not(flexLineSize))
        assertThat(textView3.height, not(flexLineSize))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignSelf_stretch_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_self_stretch_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))

        // There should be 2 flex lines in the layout with the given layout.
        // Only the first TextView's alignSelf is set to ALIGN_SELF_STRETCH
        val flexLineSize = flexboxLayout.width / 2
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(textView1.width, isEqualAllowingError(flexLineSize))
        assertThat(textView2.width, not(flexLineSize))
        assertThat(textView3.width, not(flexLineSize))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignItems_flexStart() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test)

        assertThat(flexboxLayout.alignItems, `is`(AlignItems.FLEX_START))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))

        // There should be 2 flex lines in the layout with the given layout.
        val flexLineSize = flexboxLayout.height / 2
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(textView1.height, not(flexLineSize))
        assertThat(textView2.height, not(flexLineSize))
        assertThat(textView3.height, not(flexLineSize))
        assertThat(textView3.top, isEqualAllowingError(flexLineSize))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignItems_flexEnd() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignItems = AlignItems.FLEX_END
                    }
                })

        assertThat(flexboxLayout.alignItems, `is`(AlignItems.FLEX_END))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))

        // There should be 2 flex lines in the layout with the given layout.
        val flexLineSize = flexboxLayout.height / 2
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(textView1.height, not(flexLineSize))
        assertThat(textView2.height, not(flexLineSize))
        assertThat(textView3.height, not(flexLineSize))
        assertThat(textView1.bottom, isEqualAllowingError(flexLineSize))
        assertThat(textView2.bottom, isEqualAllowingError(flexLineSize))
        assertThat(textView3.bottom, `is`(flexboxLayout.bottom))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignItems_flexEnd_parentPadding() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_align_items_parent_padding_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignItems = AlignItems.FLEX_END
                    }
                })

        assertThat(flexboxLayout.alignItems, `is`(AlignItems.FLEX_END))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        assertThat(textView1.bottom, `is`(flexboxLayout.bottom - flexboxLayout.paddingBottom))
        assertThat(textView2.bottom, `is`(flexboxLayout.bottom - flexboxLayout.paddingBottom))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignItems_flexEnd_parentPadding_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_align_items_parent_padding_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignItems = AlignItems.FLEX_END
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        assertThat(flexboxLayout.alignItems, `is`(AlignItems.FLEX_END))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        assertThat(textView1.right, `is`(flexboxLayout.right - flexboxLayout.paddingRight))
        assertThat(textView2.right, `is`(flexboxLayout.right - flexboxLayout.paddingRight))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignItems_center() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignItems = AlignItems.CENTER
                    }
                })

        assertThat(flexboxLayout.alignItems, `is`(AlignItems.CENTER))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))

        // There should be 2 flex lines in the layout with the given layout.
        val flexLineSize = flexboxLayout.height / 2
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        // All TextView's heights are the same. No issues should be found if using the first
        // TextView to calculate the space above and below
        val spaceAboveAndBelow = (flexLineSize - textView1.height) / 2
        assertThat(textView1.height, not(flexLineSize))
        assertThat(textView2.height, not(flexLineSize))
        assertThat(textView3.height, not(flexLineSize))
        assertThat(textView1.top, isEqualAllowingError(spaceAboveAndBelow))
        assertThat(textView2.top, isEqualAllowingError(spaceAboveAndBelow))
        assertThat(textView3.top, isEqualAllowingError(flexLineSize + spaceAboveAndBelow))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignItems_flexEnd_wrapReverse() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexWrap = FlexWrap.WRAP_REVERSE
                        flexboxLayout.alignItems = AlignItems.FLEX_END
                    }
                })

        assertThat(flexboxLayout.alignItems, `is`(AlignItems.FLEX_END))
        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP_REVERSE))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))

        // There should be 2 flex lines in the layout with the given layout.
        val flexLineSize = flexboxLayout.height / 2
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)

        assertThat(textView1.height, not(flexLineSize))
        assertThat(textView2.height, not(flexLineSize))
        assertThat(textView3.height, not(flexLineSize))
        assertThat(textView1.top, isEqualAllowingError(flexboxLayout.height - flexLineSize))
        assertThat(textView2.top, isEqualAllowingError(flexboxLayout.height - flexLineSize))
        assertThat(textView3.top, `is`(0))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignItems_center_wrapReverse() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexWrap = FlexWrap.WRAP_REVERSE
                        flexboxLayout.alignItems = AlignItems.CENTER
                    }
                })

        assertThat(flexboxLayout.alignItems, `is`(AlignItems.CENTER))
        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP_REVERSE))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))

        // There should be 2 flex lines in the layout with the given layout.
        val flexLineSize = flexboxLayout.height / 2
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)

        // All TextView's heights are the same. No issues should be found if using the first
        // TextView to calculate the space above and below
        val spaceAboveAndBelow = (flexLineSize - textView1.height) / 2
        assertThat(textView1.height, not(flexLineSize))
        assertThat(textView2.height, not(flexLineSize))
        assertThat(textView3.height, not(flexLineSize))
        assertThat(textView1.bottom, isEqualAllowingError(flexboxLayout.height - spaceAboveAndBelow))
        assertThat(textView2.bottom, isEqualAllowingError(flexboxLayout.height - spaceAboveAndBelow))
        assertThat(textView3.bottom,
                isEqualAllowingError(flexboxLayout.height - flexLineSize - spaceAboveAndBelow))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignItems_flexStart_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        assertThat(flexboxLayout.alignItems, `is`(AlignItems.FLEX_START))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))

        // There should be 2 flex lines in the layout with the given layout.
        val flexLineSize = flexboxLayout.width / 2
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(textView1.width, not(flexLineSize))
        assertThat(textView2.width, not(flexLineSize))
        assertThat(textView3.width, not(flexLineSize))
        assertThat(textView3.left, isEqualAllowingError(flexLineSize))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignItems_flexEnd_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignItems = AlignItems.FLEX_END
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        assertThat(flexboxLayout.alignItems, `is`(AlignItems.FLEX_END))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)))

        // There should be 2 flex lines in the layout with the given layout.
        val flexLineSize = flexboxLayout.height / 2
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(textView1.width, not(flexLineSize))
        assertThat(textView2.width, not(flexLineSize))
        assertThat(textView3.width, not(flexLineSize))
        assertThat(textView1.right, isEqualAllowingError(flexLineSize))
        assertThat(textView2.right, isEqualAllowingError(flexLineSize))
        assertThat(textView3.right, `is`(flexboxLayout.right))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignItems_center_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignItems = AlignItems.CENTER
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        assertThat(flexboxLayout.alignItems, `is`(AlignItems.CENTER))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))

        // There should be 2 flex lines in the layout with the given layout.
        val flexLineSize = flexboxLayout.width / 2
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        // All TextView's widths are the same. No issues should be found if using the first
        // TextView to calculate the space left and right
        val spaceLeftAndRight = (flexLineSize - textView1.width) / 2
        assertThat(textView1.width, not(flexLineSize))
        assertThat(textView2.width, not(flexLineSize))
        assertThat(textView3.width, not(flexLineSize))
        assertThat(textView1.left, isEqualAllowingError(spaceLeftAndRight))
        assertThat(textView2.left, isEqualAllowingError(spaceLeftAndRight))
        assertThat(textView3.left, isEqualAllowingError(flexLineSize + spaceLeftAndRight))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignItems_flexEnd_wrapReverse_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexWrap = FlexWrap.WRAP_REVERSE
                        flexboxLayout.alignItems = AlignItems.FLEX_END
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        assertThat(flexboxLayout.alignItems, `is`(AlignItems.FLEX_END))
        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP_REVERSE))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))

        // There should be 2 flex lines in the layout with the given layout.
        val flexLineSize = flexboxLayout.width / 2
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)

        assertThat(textView1.width, not(flexLineSize))
        assertThat(textView2.width, not(flexLineSize))
        assertThat(textView3.width, not(flexLineSize))
        assertThat(textView1.left, isEqualAllowingError(flexboxLayout.width - flexLineSize))
        assertThat(textView2.left, isEqualAllowingError(flexboxLayout.width - flexLineSize))
        assertThat(textView3.left, `is`(0))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignItems_center_wrapReverse_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_align_items_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexWrap = FlexWrap.WRAP_REVERSE
                        flexboxLayout.alignItems = AlignItems.CENTER
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        assertThat(flexboxLayout.alignItems, `is`(AlignItems.CENTER))
        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP_REVERSE))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))

        // There should be 2 flex lines in the layout with the given layout.
        val flexLineSize = flexboxLayout.width / 2
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)

        // All TextView's widths are the same. No issues should be found if using the first
        // TextView to calculate the space above and below
        val spaceLeftAndRight = (flexLineSize - textView1.width) / 2
        assertThat(textView1.width, not(flexLineSize))
        assertThat(textView2.width, not(flexLineSize))
        assertThat(textView3.width, not(flexLineSize))
        assertThat(textView1.right, isEqualAllowingError(flexboxLayout.width - spaceLeftAndRight))
        assertThat(textView2.right, isEqualAllowingError(flexboxLayout.width - spaceLeftAndRight))
        assertThat(textView3.right,
                isEqualAllowingError(flexboxLayout.width - flexLineSize - spaceLeftAndRight))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignItems_baseline() {
        val activity = activityRule.activity
        createFlexboxLayout(R.layout.activity_align_items_baseline_test)
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        val topPluBaseline1 = textView1.top + textView1.baseline
        val topPluBaseline2 = textView2.top + textView2.baseline
        val topPluBaseline3 = textView3.top + textView3.baseline

        assertThat(topPluBaseline1, `is`(topPluBaseline2))
        assertThat(topPluBaseline2, `is`(topPluBaseline3))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignItems_baseline_wrapContent() {
        // This test verifies the issue that baseline calculation is broken on API level +24
        // https://github.com/google/flexbox-layout/issues/341
        val activity = activityRule.activity
        val layout = createFlexboxLayout(R.layout.activity_align_items_baseline_wrap_content)
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        val topPluBaseline1 = textView1.top + textView1.baseline
        val topPluBaseline2 = textView2.top + textView2.baseline
        val topPluBaseline3 = textView3.top + textView3.baseline

        assertThat(topPluBaseline1, `is`(topPluBaseline2))
        assertThat(topPluBaseline2, `is`(topPluBaseline3))
        assertThat(layout.flexLines.size, `is`(1))
        assertTrue(layout.flexLines[0].crossSize > textView1.height)
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignItems_baseline_wrapReverse() {
        val activity = activityRule.activity
        createFlexboxLayout(R.layout.activity_align_items_baseline_test, object : Configuration {
            override fun apply(flexboxLayout: FlexboxLayout) {
                flexboxLayout.flexWrap = FlexWrap.WRAP_REVERSE
            }
        })
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        val bottomPluBaseline1 = textView1.bottom + textView1.baseline
        val bottomPluBaseline2 = textView2.bottom + textView2.baseline
        val bottomPluBaseline3 = textView3.bottom + textView3.baseline

        assertThat(bottomPluBaseline1, `is`(bottomPluBaseline2))
        assertThat(bottomPluBaseline2, `is`(bottomPluBaseline3))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexDirection_row_reverse() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_flex_wrap_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexDirection = FlexDirection.ROW_REVERSE
                    }
                })

        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW_REVERSE))

        // The layout direction should be right to left
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isRightAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyLeftOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexDirection_column_reverse() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_flex_wrap_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexDirection = FlexDirection.COLUMN_REVERSE
                    }
                })

        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN_REVERSE))
        onView(withId(R.id.text1)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyAbove(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexBasisPercent_wrap() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_flex_basis_percent_test)

        // The text1 length is 50%, the text2 length is 60% and the wrap property is WRAP,
        // the text2 should be on the second flex line.
        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val lp1 = textView1.layoutParams as FlexboxLayout.LayoutParams
        val lp2 = textView2.layoutParams as FlexboxLayout.LayoutParams
        assertThat(textView1.width, `is`(Math.round(flexboxLayout.width * lp1.flexBasisPercent)))
        assertThat(textView2.width, `is`(Math.round(flexboxLayout.width * lp2.flexBasisPercent)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexBasisPercent_nowrap() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_flex_basis_percent_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexWrap = FlexWrap.NOWRAP
                    }
                })

        // The text1 length is 50%, the text2 length is 60% and the text3 has the fixed width,
        // but the flex wrap attribute is NOWRAP, and flexShrink attributes for all
        // children are the default value (1), three text views are shrank to fit in a single flex
        // line.
        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.NOWRAP))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        val totalWidth = textView1.width + textView2.width + textView3.width
        // Allowing minor different length with the flex container since the sum of the three text
        // views width is not always the same as the flex container's main size caused by round
        // errors in calculating the percent lengths.
        assertThat(flexboxLayout.width, isEqualAllowingError(totalWidth))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexBasisPercent_wrap_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_flex_basis_percent_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        // The text1 length is 50%, the text2 length is 60% and the wrap property is WRAP,
        // the text2 should be on the second flex line.
        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val lp1 = textView1.layoutParams as FlexboxLayout.LayoutParams
        val lp2 = textView2.layoutParams as FlexboxLayout.LayoutParams
        assertThat(textView1.height, `is`(Math.round(flexboxLayout.height * lp1.flexBasisPercent)))
        assertThat(textView2.height, `is`(Math.round(flexboxLayout.height * lp2.flexBasisPercent)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexBasisPercent_nowrap_flexDirection_column() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_flex_basis_percent_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexWrap = FlexWrap.NOWRAP
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })

        // The text1 length is 50%, the text2 length is 60% and the text3 has the fixed height,
        // but the flex wrap attribute is NOWRAP, and flexShrink attributes for all
        // children are the default value (1), three text views are shrank to fit in a single
        // flex line.
        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.NOWRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        val totalHeight = textView1.height + textView2.height + textView3.height
        // Allowing minor different length with the flex container since the sum of the three text
        // views width is not always the same as the flex container's main size caused by round
        // errors in calculating the percent lengths.
        assertThat(flexboxLayout.height, isEqualAllowingError(totalHeight))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testMinWidth_initial_width_less_than_minWidth() {
        val activity = activityRule.activity

        // This test case verifies if the minWidth attribute works as a minimum constraint
        // If the initial view width is less than the value of minWidth.
        // The textView1's layout_width is set to wrap_content and its text is "1" apparently
        // the initial measured width is less than the value of layout_minWidth (100dp)
        val flexboxLayout = createFlexboxLayout(R.layout.activity_minwidth_test)
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val minWidth = (textView1.layoutParams as FlexboxLayout.LayoutParams).minWidth

        onView(withId(R.id.text1)).check(hasWidth(minWidth))
        onView(withId(R.id.text2)).check(hasWidth(flexboxLayout.width - minWidth))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testMinWidth_works_as_lower_bound_shrink_to() {
        val activity = activityRule.activity

        // This test case verifies if the minWidth attribute works as a lower bound
        // when the view would shrink less than the minWidth if the minWidth weren't set
        val flexboxLayout = createFlexboxLayout(R.layout.activity_minwidth_lower_bound_test)
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        val textView4 = activity.findViewById<TextView>(R.id.text4)
        val minWidth = (textView1.layoutParams as FlexboxLayout.LayoutParams).minWidth

        onView(withId(R.id.text1)).check(hasWidth(minWidth))
        assertEquals(flexboxLayout.width, textView1.width + textView2.width + textView3.width +
                textView4.width)
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testMinHeight_initial_height_less_than_minHeight() {
        val activity = activityRule.activity

        // This test case verifies if the minHeight attribute works as a minimum constraint
        // If the initial view height is less than the value of minHeight.
        // The textView1's layout_height is set to wrap_content and its text is "1" apparently
        // the initial measured height is less than the value of layout_minHeight (100dp)
        val flexboxLayout = createFlexboxLayout(R.layout.activity_minheight_test)
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val minHeight = (textView1.layoutParams as FlexboxLayout.LayoutParams).minHeight

        onView(withId(R.id.text1)).check(hasHeight(minHeight))
        onView(withId(R.id.text2)).check(hasHeight(flexboxLayout.height - minHeight))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testMinHeight_works_as_lower_bound_shrink_to() {
        val activity = activityRule.activity

        // This test case verifies if the minHeight attribute works as a lower bound
        // when the view would shrink less than the minHeight if the minHeight weren't set
        val flexboxLayout = createFlexboxLayout(R.layout.activity_minheight_lower_bound_test)
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        val textView4 = activity.findViewById<TextView>(R.id.text4)
        val minHeight = (textView1.layoutParams as FlexboxLayout.LayoutParams).minHeight

        onView(withId(R.id.text1)).check(hasHeight(minHeight))
        assertEquals(flexboxLayout.height, textView1.height + textView2.height + textView3.height +
                textView4.height)
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testMaxWidth_initial_width_more_than_maxWidth() {
        val activity = activityRule.activity

        // This test case verifies if the maxWidth attribute works as a maximum constraint
        // ff the initial view width is more than the value of maxWidth.
        val flexboxLayout = createFlexboxLayout(R.layout.activity_maxwidth_test)
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val maxWidth = (textView1.layoutParams as FlexboxLayout.LayoutParams).maxWidth

        onView(withId(R.id.text1)).check(hasWidth(maxWidth))
        onView(withId(R.id.text2)).check(hasWidth(flexboxLayout.width - maxWidth))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testMaxWidth_works_as_upper_bound_expand_to() {
        val activity = activityRule.activity

        // This test case verifies if the maxWidth attribute works as a upper bound
        // when the view would expand more than the maxWidth if the maxWidth weren't set
        val flexboxLayout = createFlexboxLayout(R.layout.activity_maxwidth_upper_bound_test)
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val maxWidth = (textView1.layoutParams as FlexboxLayout.LayoutParams).maxWidth

        onView(withId(R.id.text1)).check(hasWidth(maxWidth))
        assertEquals(flexboxLayout.width, textView1.width + textView2.width)
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testMaxHeight_initial_height_more_than_maxHeight() {
        val activity = activityRule.activity

        // This test case verifies if the maxHeight attribute works as a maximum constraint
        // ff the initial view height is more than the value of maxHeight.
        val flexboxLayout = createFlexboxLayout(R.layout.activity_maxheight_test)
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val maxHeight = (textView1.layoutParams as FlexboxLayout.LayoutParams).maxHeight

        onView(withId(R.id.text1)).check(hasHeight(maxHeight))
        onView(withId(R.id.text2)).check(hasHeight(flexboxLayout.height - maxHeight))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testMaxHeight_works_as_lower_bound_expand_to() {
        val activity = activityRule.activity

        // This test case verifies if the maxHeight attribute works as a upper bound
        // when the view would expand more than the maxHeight if the maxHeight weren't set
        val flexboxLayout = createFlexboxLayout(R.layout.activity_maxheight_upper_bound_test)
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val maxHeight = (textView1.layoutParams as FlexboxLayout.LayoutParams).maxHeight

        onView(withId(R.id.text1)).check(hasHeight(maxHeight))
        assertEquals(flexboxLayout.height, textView1.height + textView2.height)
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testView_visibility_gone() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_views_visibility_gone)

        // The text1 and text2's visibility are gone, so the visible view starts from text3
        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text4)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text4)).check(isCompletelyRightOf(withId(R.id.text3)))
        onView(withId(R.id.text5)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text5)).check(isCompletelyBelow(withId(R.id.text3)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        val textView4 = activity.findViewById<TextView>(R.id.text4)
        val textView5 = activity.findViewById<TextView>(R.id.text5)
        assertThat(textView1.visibility, `is`(View.GONE))
        assertThat(textView2.visibility, `is`(View.GONE))
        assertThat(textView4.left, `is`(textView3.right))
        assertThat(textView5.top, `is`(textView3.bottom))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testView_visibility_gone_first_item_in_flex_line_horizontal() {
        // This test verifies if the FlexboxLayout is visible when the visibility of the first
        // flex item in the second flex line (or arbitrary flex lines other than the first flex
        // line) is set to "gone"
        // There was an issue reported for that
        // https://github.com/google/flexbox-layout/issues/47
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_visibility_gone_first_item_in_flex_line_row)

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW))
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView3 = activity.findViewById<TextView>(R.id.text3)

        assertTrue(flexboxLayout.height > 0)
        assertThat(flexboxLayout.height, `is`(textView1.height + textView3.height))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testView_visibility_gone_first_item_in_flex_line_vertical() {
        // This test verifies if the FlexboxLayout is visible when the visibility of the first
        // flex item in the second flex line (or arbitrary flex lines other than the first flex
        // line) is set to "gone"
        // There was an issue reported for that
        // https://github.com/google/flexbox-layout/issues/47
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_visibility_gone_first_item_in_flex_line_column)

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView3 = activity.findViewById<TextView>(R.id.text3)

        assertTrue(flexboxLayout.width > 0)
        assertThat(flexboxLayout.width, `is`(textView1.width + textView3.width))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testView_visibility_invisible() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_views_visibility_invisible)

        // The text1 and text2's visibility are invisible, these views take space like visible views
        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text1)))

        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(textView1.visibility, `is`(View.INVISIBLE))
        assertThat(textView2.visibility, `is`(View.INVISIBLE))
        assertThat(textView3.top, `is`(textView1.bottom))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testWrapBefore() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_wrap_before_test)

        // layout_wrapBefore for the text2 and text3 are set to true, the text2 and text3 should
        // be the first item for each flex line.
        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))
        val textView1 = activity.findViewById<TextView>(R.id.text1)
        val textView2 = activity.findViewById<TextView>(R.id.text2)
        val textView3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(flexboxLayout.height, `is`(textView1.height + textView2.height + textView3.height))

        activityRule.runOnUiThread {
            val lp2 = textView2.layoutParams as FlexboxLayout.LayoutParams
            lp2.isWrapBefore = false
            textView2.layoutParams = lp2
        }

        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))
        assertThat(flexboxLayout.height, `is`(textView1.height + textView3.height))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testWrapBefore_nowrap() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_wrap_before_test,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexWrap = FlexWrap.NOWRAP
                    }
                })

        // layout_wrapBefore for the text2 and text3 are set to true, but the flexWrap is set to
        // NOWRAP, three text views should not be wrapped.
        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.NOWRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW))
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text2)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testWrap_parentPadding_horizontal() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_wrap_parent_padding_horizontal_test)

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW))
        // The sum of width of TextView1 and TextView2 is not enough for wrapping, but considering
        // parent padding, the second TextView should be wrapped
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        assertThat(flexboxLayout.height, `is`(flexboxLayout.paddingTop + flexboxLayout.paddingBottom +
                text1.height + text2.height))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testWrap_parentPadding_vertical() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_wrap_parent_padding_vertical_test)

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        // The sum of height of TextView1 and TextView2 is not enough for wrapping, but considering
        // parent padding, the second TextView should be wrapped
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        assertThat(flexboxLayout.width, `is`(flexboxLayout.paddingLeft + flexboxLayout.paddingRight +
                text1.width + text2.width))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testWrap_childMargin_horizontal() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_wrap_child_margin_horizontal_test)

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW))
        // The sum of width of TextView1 and TextView2 is not enough for wrapping, but considering
        // the margin for the TextView2, the second TextView should be wrapped
        onView(withId(R.id.text2)).check(isCompletelyBelow(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyRightOf(withId(R.id.text2)))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        val lp2 = text2.layoutParams as FlexboxLayout.LayoutParams
        assertThat(flexboxLayout.height, `is`(text1.height + text2.height + lp2.topMargin +
                lp2.bottomMargin))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFirstItemLarge_horizontal() {
        // This test verifies a empty flex line is not added when the first flex item is large
        // and judged wrapping is required with the first item.
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_first_item_large_horizontal_test)

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW))
        assertThat(flexboxLayout.alignItems, `is`(AlignItems.STRETCH))
        assertThat(flexboxLayout.alignContent, `is`(AlignContent.STRETCH))
        // The sum of width of TextView1 and TextView2 is not enough for wrapping, but considering
        // the margin for the TextView2, the second TextView should be wrapped
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        val text3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(flexboxLayout.height, `is`(text1.height + text2.height + text3.height))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFirstItemLarge_vertical() {
        // This test verifies a empty flex line is not added when the first flex item is large
        // and judged wrapping is required with the first item.
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_first_item_large_vertical_test)

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(flexboxLayout.alignItems, `is`(AlignItems.STRETCH))
        assertThat(flexboxLayout.alignContent, `is`(AlignContent.STRETCH))
        // The sum of width of TextView1 and TextView2 is not enough for wrapping, but considering
        // the margin for the TextView2, the second TextView should be wrapped
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        val text3 = activity.findViewById<TextView>(R.id.text3)
        assertThat(flexboxLayout.width, `is`(text1.width + text2.width + text3.width))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testWrap_childMargin_vertical() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_wrap_child_margin_vertical_test)

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        // The sum of height of TextView1 and TextView2 is not enough for wrapping, but considering
        // the margin of the TextView2, the second TextView should be wrapped
        onView(withId(R.id.text2)).check(isCompletelyRightOf(withId(R.id.text1)))
        onView(withId(R.id.text3)).check(isCompletelyBelow(withId(R.id.text2)))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        val lp2 = text2.layoutParams as FlexboxLayout.LayoutParams
        assertThat(flexboxLayout.width,
                `is`(text1.width + text2.width + lp2.leftMargin + lp2.rightMargin))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testEmptyChildren() {
        val flexboxLayout = createFlexboxLayout(R.layout.activity_empty_children)

        assertThat(flexboxLayout.childCount, `is`(0))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDivider_directionRow_verticalBeginning() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_divider_test_direction_row)
        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW))
        assertThat(flexboxLayout.showDividerVertical, `is`(FlexboxLayout.SHOW_DIVIDER_BEGINNING))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        val text3 = activity.findViewById<TextView>(R.id.text3)
        val divider = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        assertNotNull(divider)
        val widthSumFirstRow = text1.width + text2.width + text3.width + divider!!.intrinsicWidth
        assertThat(text3.right, `is`(widthSumFirstRow))
        assertThat(text1.left, `is`(not(flexboxLayout.left)))
        val flexLines = flexboxLayout.flexLines
        assertThat(flexLines.size, `is`(2))
        val flexLine1 = flexLines[0]
        // The right should be 90 * 3 + 10 (divider)
        assertThat(flexLine1.mainSize, isEqualAllowingError(activity.dpToPixel(280)))
        assertThat(flexLine1.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
        val flexLine2 = flexLines[1]
        // The right should be 140 * 2 + 10 (divider)
        assertThat(flexLine2.mainSize, isEqualAllowingError(activity.dpToPixel(290)))
        assertThat(flexLine2.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDivider_directionRow_verticalMiddle() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_row,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.showDividerVertical = FlexboxLayout.SHOW_DIVIDER_MIDDLE
                    }
                }
        )
        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW))
        assertThat(flexboxLayout.showDividerVertical, `is`(FlexboxLayout.SHOW_DIVIDER_MIDDLE))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        val text3 = activity.findViewById<TextView>(R.id.text3)
        val divider = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        assertNotNull(divider)
        // Three text views are placed in the first row, thus two vertical middle dividers should
        // be placed
        val widthSumFirstRow = text1.width + text2.width + text3.width + divider!!.intrinsicWidth * 2
        assertThat(text3.right, `is`(widthSumFirstRow))
        assertThat(text1.left, `is`(flexboxLayout.left))
        val flexLines = flexboxLayout.flexLines
        assertThat(flexLines.size, `is`(2))
        val flexLine1 = flexLines[0]
        // The right should be 90 * 3 + 10 * 2(divider)
        assertThat(flexLine1.mainSize, isEqualAllowingError(activity.dpToPixel(290)))
        assertThat(flexLine1.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
        val flexLine2 = flexLines[1]
        // The right should be 140 * 2 + 10 (divider)
        assertThat(flexLine2.mainSize, isEqualAllowingError(activity.dpToPixel(290)))
        assertThat(flexLine2.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDivider_directionRow_verticalEnd() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_row,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.showDividerVertical = FlexboxLayout.SHOW_DIVIDER_END
                    }
                }
        )

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW))
        assertThat(flexboxLayout.showDividerVertical, `is`(FlexboxLayout.SHOW_DIVIDER_END))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        val text3 = activity.findViewById<TextView>(R.id.text3)
        val divider = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        assertNotNull(divider)
        // Three text views are placed in the first row, thus two vertical middle dividers should
        // be placed
        val widthSumFirstRow = text1.width + text2.width + text3.width + divider!!.intrinsicWidth
        assertThat(text3.right + divider.intrinsicWidth, `is`(widthSumFirstRow))
        assertThat(text1.left, `is`(flexboxLayout.left))
        val flexLines = flexboxLayout.flexLines
        assertThat(flexLines.size, `is`(2))
        val flexLine1 = flexLines[0]
        // The right should be 90 * 3 + 10 (divider)
        assertThat(flexLine1.mainSize, isEqualAllowingError(activity.dpToPixel(280)))
        assertThat(flexLine1.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
        val flexLine2 = flexLines[1]
        // The right should be 140 * 2 + 10 (divider)
        assertThat(flexLine2.mainSize, isEqualAllowingError(activity.dpToPixel(290)))
        assertThat(flexLine2.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDivider_directionRow_verticalAll() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_row,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.showDividerVertical = FlexboxLayout.SHOW_DIVIDER_BEGINNING or FlexboxLayout.SHOW_DIVIDER_MIDDLE or FlexboxLayout.SHOW_DIVIDER_END
                    }
                }
        )

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW))
        assertThat(flexboxLayout.showDividerVertical,
                `is`(FlexboxLayout.SHOW_DIVIDER_BEGINNING or FlexboxLayout.SHOW_DIVIDER_MIDDLE or
                        FlexboxLayout.SHOW_DIVIDER_END))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        val text3 = activity.findViewById<TextView>(R.id.text3)
        val divider = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        assertNotNull(divider)
        // Three text views are placed in the first row, thus two vertical middle dividers should
        // be placed
        val widthSumFirstRow = text1.width + text2.width + text3.width + divider!!.intrinsicWidth * 4
        assertThat(text3.right + divider.intrinsicWidth, `is`(widthSumFirstRow))
        assertThat(text1.left, `is`(not(flexboxLayout.left)))
        val flexLines = flexboxLayout.flexLines
        assertThat(flexLines.size, `is`(2))
        val flexLine1 = flexLines[0]
        // The right should be 90 * 3 + 10 * 4 (divider)
        assertThat(flexLine1.mainSize, isEqualAllowingError(activity.dpToPixel(310)))
        assertThat(flexLine1.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
        val flexLine2 = flexLines[1]
        // The right should be 140 * 2 + 10 * 3 (divider)
        assertThat(flexLine2.mainSize, isEqualAllowingError(activity.dpToPixel(310)))
        assertThat(flexLine2.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDivider_directionRow_horizontalBeginning() {
        val activity = activityRule.activity
        val divider = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_row,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.dividerDrawableHorizontal = divider
                        flexboxLayout.showDividerHorizontal = FlexboxLayout.SHOW_DIVIDER_BEGINNING
                        flexboxLayout.showDividerVertical = FlexboxLayout.SHOW_DIVIDER_NONE
                    }
                }
        )

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW))
        assertThat(flexboxLayout.showDividerHorizontal, `is`(FlexboxLayout.SHOW_DIVIDER_BEGINNING))
        assertThat(flexboxLayout.showDividerVertical, `is`(FlexboxLayout.SHOW_DIVIDER_NONE))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text4 = activity.findViewById<TextView>(R.id.text4)
        assertNotNull(divider)
        val heightSum = text1.height + text4.height + divider!!.intrinsicHeight
        assertThat(text4.bottom, `is`(heightSum))
        assertThat(text1.top, `is`(not(flexboxLayout.top)))
        val flexLines = flexboxLayout.flexLines
        assertThat(flexLines.size, `is`(2))
        val flexLine1 = flexLines[0]
        // There is a horizontal divider at the beginning. Top and bottom coordinates are shifted
        // by the amount of 15
        // The right should be 90 * 3
        assertThat(flexLine1.mainSize, isEqualAllowingError(activity.dpToPixel(270)))
        assertThat(flexLine1.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
        val flexLine2 = flexLines[1]
        assertThat(flexLine2.mainSize, isEqualAllowingError(activity.dpToPixel(280)))
        assertThat(flexLine2.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDivider_directionRow_horizontalMiddle() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_row,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        val divider = ResourcesCompat
                                .getDrawable(activity.resources, R.drawable.divider, null)
                        flexboxLayout.dividerDrawableHorizontal = divider
                        flexboxLayout.showDividerHorizontal = FlexboxLayout.SHOW_DIVIDER_MIDDLE
                        flexboxLayout.showDividerVertical = FlexboxLayout.SHOW_DIVIDER_NONE
                    }
                }
        )

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW))
        assertThat(flexboxLayout.showDividerHorizontal, `is`(FlexboxLayout.SHOW_DIVIDER_MIDDLE))
        assertThat(flexboxLayout.showDividerVertical, `is`(FlexboxLayout.SHOW_DIVIDER_NONE))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text4 = activity.findViewById<TextView>(R.id.text4)
        val divider = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        assertNotNull(divider)
        val heightSum = text1.height + text4.height + divider!!.intrinsicHeight
        assertThat(text4.bottom, `is`(heightSum))
        assertThat(text1.top, `is`(flexboxLayout.top))
        val flexLines = flexboxLayout.flexLines
        assertThat(flexLines.size, `is`(2))
        val flexLine1 = flexLines[0]
        // The right should be 90 * 3
        assertThat(flexLine1.mainSize, isEqualAllowingError(activity.dpToPixel(270)))
        assertThat(flexLine1.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
        // There is a horizontal divider at the middle. Top and bottom coordinates are shifted
        // by the amount of 15
        val flexLine2 = flexLines[1]
        // The right should be 140
        assertThat(flexLine2.mainSize, isEqualAllowingError(activity.dpToPixel(280)))
        assertThat(flexLine2.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDivider_directionRow_horizontalEnd() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_row,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        val divider = ResourcesCompat
                                .getDrawable(activity.resources, R.drawable.divider, null)
                        flexboxLayout.dividerDrawableHorizontal = divider
                        flexboxLayout.showDividerHorizontal = FlexboxLayout.SHOW_DIVIDER_END
                        flexboxLayout.showDividerVertical = FlexboxLayout.SHOW_DIVIDER_NONE
                    }
                }
        )

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW))
        assertThat(flexboxLayout.showDividerHorizontal, `is`(FlexboxLayout.SHOW_DIVIDER_END))
        assertThat(flexboxLayout.showDividerVertical, `is`(FlexboxLayout.SHOW_DIVIDER_NONE))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text4 = activity.findViewById<TextView>(R.id.text4)
        val divider = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        assertNotNull(divider)
        val heightSum = text1.height + text4.height + divider!!.intrinsicHeight
        assertThat(text4.bottom + divider.intrinsicHeight, `is`(heightSum))
        assertThat(text1.top, `is`(flexboxLayout.top))
        val flexLines = flexboxLayout.flexLines
        assertThat(flexLines.size, `is`(2))
        val flexLine1 = flexLines[0]
        // The right should be 90 * 3
        assertThat(flexLine1.mainSize, isEqualAllowingError(activity.dpToPixel(270)))
        assertThat(flexLine1.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
        // There is a horizontal divider at the middle. Top and bottom coordinates are shifted
        // by the amount of 15
        val flexLine2 = flexLines[1]
        // The right should be 140
        assertThat(flexLine2.mainSize, isEqualAllowingError(activity.dpToPixel(280)))
        assertThat(flexLine2.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDivider_directionRow_horizontalAll() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_row,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        val divider = ResourcesCompat
                                .getDrawable(activity.resources, R.drawable.divider, null)
                        flexboxLayout.dividerDrawableHorizontal = divider
                        flexboxLayout.showDividerHorizontal = FlexboxLayout.SHOW_DIVIDER_BEGINNING or FlexboxLayout.SHOW_DIVIDER_MIDDLE or FlexboxLayout.SHOW_DIVIDER_END
                        flexboxLayout.showDividerVertical = FlexboxLayout.SHOW_DIVIDER_NONE
                    }
                }
        )

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW))
        assertThat(flexboxLayout.showDividerHorizontal,
                `is`(FlexboxLayout.SHOW_DIVIDER_BEGINNING or FlexboxLayout.SHOW_DIVIDER_MIDDLE or
                        FlexboxLayout.SHOW_DIVIDER_END))
        assertThat(flexboxLayout.showDividerVertical, `is`(FlexboxLayout.SHOW_DIVIDER_NONE))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text4 = activity.findViewById<TextView>(R.id.text4)
        val divider = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        assertNotNull(divider)
        val heightSum = text1.height + text4.height + divider!!.intrinsicHeight * 3
        assertThat(text4.bottom + divider.intrinsicHeight, `is`(heightSum))
        assertThat(text1.top, `is`(not(flexboxLayout.top)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDivider_directionRow_all_thickDivider() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_row,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        val thickDivider = ResourcesCompat
                                .getDrawable(activity.resources, R.drawable.divider_thick, null)
                        flexboxLayout.dividerDrawableVertical = thickDivider
                        flexboxLayout.showDividerVertical = FlexboxLayout.SHOW_DIVIDER_BEGINNING or FlexboxLayout.SHOW_DIVIDER_MIDDLE or FlexboxLayout.SHOW_DIVIDER_END
                    }
                }
        )

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW))
        assertThat(flexboxLayout.showDividerVertical,
                `is`(FlexboxLayout.SHOW_DIVIDER_BEGINNING or FlexboxLayout.SHOW_DIVIDER_MIDDLE or
                        FlexboxLayout.SHOW_DIVIDER_END))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        val text3 = activity.findViewById<TextView>(R.id.text3)
        val divider = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider_thick, null)
        // The sum of three text views and the sum of thick dividers don't fit in one line.
        // The last text view should be placed to the next line.
        assertNotNull(divider)
        val widthSumFirstRow = text1.width + text2.width + divider!!.intrinsicWidth * 3
        assertThat(text2.right + divider.intrinsicWidth, `is`(widthSumFirstRow))
        assertThat(text1.left, `is`(not(flexboxLayout.left)))
        assertThat(text3.bottom, `is`(text1.height + text2.height))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDivider_directionColumn_horizontalBeginning() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_divider_test_direction_column)

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(flexboxLayout.showDividerHorizontal, `is`(FlexboxLayout.SHOW_DIVIDER_BEGINNING))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        val text3 = activity.findViewById<TextView>(R.id.text3)
        val divider = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        assertNotNull(divider)
        val heightSumFirstRow = text1.height + text2.height + text3.height + divider!!.intrinsicHeight
        assertThat(text3.bottom, `is`(heightSumFirstRow))
        assertThat(text1.top, `is`(not(flexboxLayout.top)))
        val flexLines = flexboxLayout.flexLines
        assertThat(flexLines.size, `is`(2))
        val flexLine1 = flexLines[0]
        assertThat(flexLine1.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
        // The bottom should be 90 * 3 + 15
        assertThat(flexLine1.mainSize, isEqualAllowingError(activity.dpToPixel(285)))
        val flexLine2 = flexLines[1]
        assertThat(flexLine2.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
        // The bottom should be 140 * 2 + 15
        assertThat(flexLine2.mainSize, isEqualAllowingError(activity.dpToPixel(295)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDivider_directionColumn_horizontalMiddle() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_column,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.showDividerHorizontal = FlexboxLayout.SHOW_DIVIDER_MIDDLE
                    }
                }
        )

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(flexboxLayout.showDividerHorizontal, `is`(FlexboxLayout.SHOW_DIVIDER_MIDDLE))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        val text3 = activity.findViewById<TextView>(R.id.text3)
        val divider = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        assertNotNull(divider)
        val heightSumFirstRow = text1.height + text2.height + text3.height + divider!!.intrinsicHeight * 2
        assertThat(text3.bottom, `is`(heightSumFirstRow))
        assertThat(text1.top, `is`(flexboxLayout.top))
        val flexLines = flexboxLayout.flexLines
        assertThat(flexLines.size, `is`(2))
        val flexLine1 = flexLines[0]
        assertThat(flexLine1.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
        // The bottom should be 90 * 3 + 15 * 2
        assertThat(flexLine1.mainSize, isEqualAllowingError(activity.dpToPixel(300)))
        val flexLine2 = flexLines[1]
        assertThat(flexLine2.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
        // The bottom should be 140 * 2 + 15
        assertThat(flexLine2.mainSize, isEqualAllowingError(activity.dpToPixel(295)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDivider_directionColumn_horizontalEnd() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_column,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.showDividerHorizontal = FlexboxLayout.SHOW_DIVIDER_END
                    }
                }
        )

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(flexboxLayout.showDividerHorizontal, `is`(FlexboxLayout.SHOW_DIVIDER_END))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        val text3 = activity.findViewById<TextView>(R.id.text3)
        val divider = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        assertNotNull(divider)
        val heightSumFirstRow = text1.height + text2.height + text3.height + divider!!.intrinsicHeight
        assertThat(text3.bottom + divider.intrinsicHeight, `is`(heightSumFirstRow))
        assertThat(text1.top, `is`(flexboxLayout.top))
        val flexLines = flexboxLayout.flexLines
        assertThat(flexLines.size, `is`(2))
        val flexLine1 = flexLines[0]
        assertThat(flexLine1.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
        // The bottom should be 90 * 3 + 15
        assertThat(flexLine1.mainSize, isEqualAllowingError(activity.dpToPixel(285)))
        val flexLine2 = flexLines[1]
        assertThat(flexLine2.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
        // The bottom should be 140 * 2 + 15
        assertThat(flexLine2.mainSize, isEqualAllowingError(activity.dpToPixel(295)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDivider_directionColumn_horizontalAll() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_column,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.showDividerHorizontal = FlexboxLayout.SHOW_DIVIDER_BEGINNING or FlexboxLayout.SHOW_DIVIDER_MIDDLE or FlexboxLayout.SHOW_DIVIDER_END
                    }
                }
        )

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(flexboxLayout.showDividerHorizontal,
                `is`(FlexboxLayout.SHOW_DIVIDER_BEGINNING or FlexboxLayout.SHOW_DIVIDER_MIDDLE or
                        FlexboxLayout.SHOW_DIVIDER_END))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        val text3 = activity.findViewById<TextView>(R.id.text3)
        val divider = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        assertNotNull(divider)
        val heightSumFirstRow = text1.height + text2.height + text3.height + divider!!.intrinsicHeight * 4
        assertThat(text3.bottom + divider.intrinsicHeight, `is`(heightSumFirstRow))
        assertThat(text1.top, `is`(not(flexboxLayout.top)))
        val flexLines = flexboxLayout.flexLines
        assertThat(flexLines.size, `is`(2))
        val flexLine1 = flexLines[0]
        assertThat(flexLine1.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
        // The bottom should be 90 * 3 + 15 * 4
        assertThat(flexLine1.mainSize, isEqualAllowingError(activity.dpToPixel(330)))
        val flexLine2 = flexLines[1]
        assertThat(flexLine2.crossSize, isEqualAllowingError(activity.dpToPixel(80)))
        // The bottom should be 140 * 2 + 15 * 3
        assertThat(flexLine2.mainSize, isEqualAllowingError(activity.dpToPixel(325)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDivider_directionColumn_verticalBeginning() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_column,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        val divider = ResourcesCompat
                                .getDrawable(activity.resources, R.drawable.divider, null)
                        flexboxLayout.dividerDrawableVertical = divider
                        flexboxLayout.showDividerVertical = FlexboxLayout.SHOW_DIVIDER_BEGINNING
                        flexboxLayout.showDividerHorizontal = FlexboxLayout.SHOW_DIVIDER_NONE
                    }
                })

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(flexboxLayout.showDividerVertical, `is`(FlexboxLayout.SHOW_DIVIDER_BEGINNING))
        assertThat(flexboxLayout.showDividerHorizontal, `is`(FlexboxLayout.SHOW_DIVIDER_NONE))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text4 = activity.findViewById<TextView>(R.id.text4)
        val divider = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        assertNotNull(divider)
        val widthSum = text1.width + text4.width + divider!!.intrinsicWidth
        assertThat(text4.right, `is`(widthSum))
        assertThat(text1.left, `is`(not(flexboxLayout.left)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDivider_directionColumn_verticalMiddle() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_column,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        val divider = ResourcesCompat
                                .getDrawable(activity.resources, R.drawable.divider, null)
                        flexboxLayout.dividerDrawableVertical = divider
                        flexboxLayout.showDividerVertical = FlexboxLayout.SHOW_DIVIDER_MIDDLE
                        flexboxLayout.showDividerHorizontal = FlexboxLayout.SHOW_DIVIDER_NONE
                    }
                })

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(flexboxLayout.showDividerVertical, `is`(FlexboxLayout.SHOW_DIVIDER_MIDDLE))
        assertThat(flexboxLayout.showDividerHorizontal, `is`(FlexboxLayout.SHOW_DIVIDER_NONE))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text4 = activity.findViewById<TextView>(R.id.text4)
        val divider = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        assertNotNull(divider)
        val widthSum = text1.width + text4.width + divider!!.intrinsicWidth
        assertThat(text4.right, `is`(widthSum))
        assertThat(text1.left, `is`(flexboxLayout.left))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDivider_directionColumn_verticalEnd() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_column,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        val divider = ResourcesCompat
                                .getDrawable(activity.resources, R.drawable.divider, null)
                        flexboxLayout.dividerDrawableHorizontal = divider
                        flexboxLayout.showDividerVertical = FlexboxLayout.SHOW_DIVIDER_END
                        flexboxLayout.showDividerHorizontal = FlexboxLayout.SHOW_DIVIDER_NONE
                    }
                })

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(flexboxLayout.showDividerVertical, `is`(FlexboxLayout.SHOW_DIVIDER_END))
        assertThat(flexboxLayout.showDividerHorizontal, `is`(FlexboxLayout.SHOW_DIVIDER_NONE))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text4 = activity.findViewById<TextView>(R.id.text4)
        val divider = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        assertNotNull(divider)
        val widthSum = text1.width + text4.width + divider!!.intrinsicWidth
        assertThat(text4.right + divider.intrinsicWidth, `is`(widthSum))
        assertThat(text1.left, `is`(flexboxLayout.left))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDivider_directionColumn_verticalAll() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_column,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        val divider = ResourcesCompat
                                .getDrawable(activity.resources, R.drawable.divider, null)
                        flexboxLayout.dividerDrawableVertical = divider
                        flexboxLayout.showDividerVertical = FlexboxLayout.SHOW_DIVIDER_BEGINNING or FlexboxLayout.SHOW_DIVIDER_MIDDLE or FlexboxLayout.SHOW_DIVIDER_END
                        flexboxLayout.showDividerHorizontal = FlexboxLayout.SHOW_DIVIDER_NONE
                    }
                })

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(flexboxLayout.showDividerVertical,
                `is`(FlexboxLayout.SHOW_DIVIDER_BEGINNING or FlexboxLayout.SHOW_DIVIDER_MIDDLE or
                        FlexboxLayout.SHOW_DIVIDER_END))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text4 = activity.findViewById<TextView>(R.id.text4)
        val divider = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        assertNotNull(divider)
        val widthSum = text1.width + text4.width + divider!!.intrinsicWidth * 3
        assertThat(text4.right + divider.intrinsicWidth, `is`(widthSum))
        assertThat(text1.left, `is`(not(flexboxLayout.left)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDivider_directionColumn_vertical_horizontal_All() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_column,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        val divider = ResourcesCompat
                                .getDrawable(activity.resources, R.drawable.divider, null)
                        flexboxLayout.setDividerDrawable(divider)
                        flexboxLayout.setShowDivider(
                                FlexboxLayout.SHOW_DIVIDER_BEGINNING or FlexboxLayout.SHOW_DIVIDER_MIDDLE or FlexboxLayout.SHOW_DIVIDER_END)
                    }
                })

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(flexboxLayout.showDividerVertical,
                `is`(FlexboxLayout.SHOW_DIVIDER_BEGINNING or FlexboxLayout.SHOW_DIVIDER_MIDDLE or
                        FlexboxLayout.SHOW_DIVIDER_END))
        assertThat(flexboxLayout.showDividerHorizontal,
                `is`(FlexboxLayout.SHOW_DIVIDER_BEGINNING or FlexboxLayout.SHOW_DIVIDER_MIDDLE or
                        FlexboxLayout.SHOW_DIVIDER_END))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        val text3 = activity.findViewById<TextView>(R.id.text3)
        val text4 = activity.findViewById<TextView>(R.id.text4)
        val divider = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        assertNotNull(divider)
        val heightSum = text1.height + text2.height + text3.height + divider!!.intrinsicHeight * 4
        val widthSum = text1.width + text4.width + divider.intrinsicWidth * 3
        assertThat(text3.bottom + divider.intrinsicHeight, `is`(heightSum))
        assertThat(text4.right + divider.intrinsicWidth, `is`(widthSum))
        assertThat(text1.left, `is`(not(flexboxLayout.left)))
        assertThat(text1.top, `is`(not(flexboxLayout.top)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDivider_directionColumn_all_thickDivider() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_divider_test_direction_column,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        val thickDivider = ResourcesCompat
                                .getDrawable(activity.resources, R.drawable.divider_thick, null)
                        flexboxLayout.dividerDrawableHorizontal = thickDivider
                        flexboxLayout.showDividerHorizontal = FlexboxLayout.SHOW_DIVIDER_BEGINNING or FlexboxLayout.SHOW_DIVIDER_MIDDLE or FlexboxLayout.SHOW_DIVIDER_END
                    }
                })

        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP))
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(flexboxLayout.showDividerHorizontal,
                `is`(FlexboxLayout.SHOW_DIVIDER_BEGINNING or FlexboxLayout.SHOW_DIVIDER_MIDDLE or
                        FlexboxLayout.SHOW_DIVIDER_END))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        val text3 = activity.findViewById<TextView>(R.id.text3)
        val divider = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider_thick, null)
        // The sum of three text views and the sum of thick dividers don't fit in one line.
        // The last text view should be placed to the next line.
        assertNotNull(divider)
        val heightSum = text1.height + text2.height + divider!!.intrinsicHeight * 3
        assertThat(text2.bottom + divider.intrinsicHeight, `is`(heightSum))
        assertThat(text1.top, `is`(not(flexboxLayout.top)))
        assertThat(text3.right, `is`(text1.width + text3.width))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testZeroWidth_wrapContentHeight_positiveFlexGrow() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_zero_width_positive_flexgrow)

        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        // Both text view's layout_width is set to 0dp, layout_height is set to wrap_content and
        // layout_flexGrow is set to 1. And the text2 has a longer text than the text1.
        // So if the cross size calculation (height) is wrong, the height of two text view do not
        // match because text2 is trying to expand vertically.
        // This assertion verifies that isn't happening. Finally both text views expand horizontally
        // enough to contain their texts in one line.
        assertThat(text1.height, `is`(text2.height))
        assertThat(text1.width + text2.width, isEqualAllowingError(flexboxLayout.width))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testZeroHeight_wrapContentWidth_positiveFlexGrow() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_zero_height_positive_flexgrow)

        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        val text2 = activity.findViewById<TextView>(R.id.text2)
        assertThat(text1.width, `is`(not(text2.width)))
        assertThat(text1.height + text2.height, isEqualAllowingError(flexboxLayout.height))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexDirection_row_alignItems_center_margin_oneSide() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_direction_row_align_items_center_margin_oneside)

        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        assertThat(text1.top, isEqualAllowingError(activity.dpToPixel(30)))
        assertThat(flexboxLayout.bottom - text1.bottom, isEqualAllowingError(activity.dpToPixel(50)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexDirection_column_alignItems_center_margin_oneSide() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_direction_column_align_items_center_margin_oneside)

        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))

        val text1 = activity.findViewById<TextView>(R.id.text1)
        assertThat(text1.left, isEqualAllowingError(activity.dpToPixel(30)))
        assertThat(flexboxLayout.right - text1.right, isEqualAllowingError(activity.dpToPixel(50)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testChildBottomMarginIncluded_flexContainerWrapContent_directionRow_flexGrow() {
        // This test is to verify the case where:
        //   - layout_height is set to wrap_content for the FlexboxLayout
        //   - Bottom (or top) margin is set to a child
        //   - The child which the has the bottom (top) margin has the largest height in the
        //     same flex line (or only a single child exists)
        //   - The child has a positive layout_flexGrow attribute set
        //  If these conditions were met, the height of the FlexboxLayout didn't take the bottom
        //  margin on the child into account
        //  See https://github.com/google/flexbox-layout/issues/154
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_wrap_content_child_bottom_margin_row_grow)

        // layout_height for text1: 24dp, layout_marginBottom: 12dp
        assertThat(flexboxLayout.height, isEqualAllowingError(activityRule.activity.dpToPixel(36)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testChildEndMarginIncluded_flexContainerWrapContent_directionColumn_flexGrow() {
        // This test is to verify the case where:
        //   - layout_width is set to wrap_content for the FlexboxLayout
        //   - End (or start) margin is set to a child
        //   - The child which the has the end (start) margin has the largest width in the
        //     same flex line (or only a single child exists)
        //   - The child has a positive layout_flexGrow attribute set
        //  If these conditions were met, the width of the FlexboxLayout didn't take the bottom
        //  margin on the child into account
        //  See https://github.com/google/flexbox-layout/issues/154
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_wrap_content_child_bottom_margin_column_grow)

        // layout_width for text1: 24dp, layout_marginEnd: 12dp
        assertThat(flexboxLayout.width, isEqualAllowingError(activityRule.activity.dpToPixel(36)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testChildBottomMarginIncluded_flexContainerWrapContent_directionRow_flexShrink() {
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
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_wrap_content_child_bottom_margin_row_shrink)

        // layout_height for text1: 24dp, layout_marginBottom: 12dp
        assertThat(flexboxLayout.height, isEqualAllowingError(activityRule.activity.dpToPixel(36)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testChildBottomMarginIncluded_flexContainerWrapContent_directionColumn_flexShrink() {
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
        val flexboxLayout = createFlexboxLayout(
                R.layout.activity_wrap_content_child_bottom_margin_column_shrink)

        // layout_width for text1: 24dp, layout_marginEnd: 12dp
        assertThat(flexboxLayout.width, isEqualAllowingError(activityRule.activity.dpToPixel(36)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testChildNeedsRemeasure_row() {
        createFlexboxLayout(R.layout.activity_child_needs_remeasure_row)
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testChildNeedsRemeasure_column() {
        createFlexboxLayout(R.layout.activity_child_needs_remeasure_column)
        onView(withId(R.id.text1)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text1)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isRightAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFirstViewGone_firstLineSingleItem_row() {
        // This test verifies the case where the first view's visibility is gone and the second
        // view is in the next flex line. In that case, the second view's position is misplaced.
        // https://github.com/google/flexbox-layout/issues/283
        createFlexboxLayout(R.layout.activity_first_view_gone_first_line_single_item)
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFirstViewGone_firstLineSingleItem_column() {
        // This test verifies the case where the first view's visibility is gone and the second
        // view is in the next flex line. In that case, the second view's position is misplaced.
        // https://github.com/google/flexbox-layout/issues/283
        createFlexboxLayout(R.layout.activity_first_view_gone_first_line_single_item,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFirstViewGone_flexGrowSetForRestOfItems_row() {
        // This test verifies the case where the first view's visibility is gone and the second
        // view and third view have the layout_flexGrow attribute set. In that case, the second
        // view's position is misplaced and the third view becomes invisible .
        // https://github.com/google/flexbox-layout/issues/303
        createFlexboxLayout(R.layout.activity_first_view_gone_layout_grow_set_for_rest)
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isRightAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isRightAlignedWith(withId(R.id.flexbox_layout)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFirstViewGone_flexGrowSetForRestOfItems_column() {
        // This test verifies the case where the first view's visibility is gone and the second
        // view and third view have the layout_flexGrow attribute set. In that case, the second
        // view's position is misplaced and the third view becomes invisible .
        // https://github.com/google/flexbox-layout/issues/303
        createFlexboxLayout(R.layout.activity_first_view_gone_layout_grow_set_for_rest,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isBottomAlignedWith(withId(R.id.flexbox_layout)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFirstViewGone_flexShrinkSetForRestOfItems_row() {
        createFlexboxLayout(R.layout.activity_first_view_gone_layout_shrink_set_for_rest)
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFirstViewGone_flexShrinkSetForRestOfItems_column() {
        createFlexboxLayout(R.layout.activity_first_view_gone_layout_shrink_set_for_rest,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.flexDirection = FlexDirection.COLUMN
                    }
                })
        onView(withId(R.id.text2)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text2)).check(isLeftAlignedWith(withId(R.id.flexbox_layout)))
        onView(withId(R.id.text3)).check(isTopAlignedWith(withId(R.id.flexbox_layout)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAddItemProgrammatically_withMarginLayoutParams() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_empty_children,
        object : Configuration {
            override fun apply(flexboxLayout: FlexboxLayout) {
                flexboxLayout.alignItems = AlignItems.FLEX_START
                val first = createTextView(activity, "1", 0)
                val second = createTextView(activity, "2", 0)
                val lp1 = ViewGroup.MarginLayoutParams(100, 100)
                lp1.setMargins(10, 10, 10, 10)
                val lp2 = ViewGroup.MarginLayoutParams(100, 100)
                lp2.setMargins(20, 20, 20, 20)
                first.layoutParams = lp1
                second.layoutParams = lp2
                flexboxLayout.addView(first)
                flexboxLayout.addView(second)
            }
        })

        assertThat(flexboxLayout.childCount, `is`(2))
        val view1 = flexboxLayout.getChildAt(0)
        val view2 = flexboxLayout.getChildAt(1)
        // Assert the coordinates of the views added programmatically with margins
        assertThat(view1.left, `is`(10))
        assertThat(view1.top, `is`(10))
        assertThat(view1.bottom, `is`(110))
        assertThat(view1.right, `is`(110))
        assertThat(view2.left, `is`(140))
        assertThat(view2.top, `is`(20))
        assertThat(view2.bottom, `is`(120))
        assertThat(view2.right, `is`(240))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAddItemProgrammatically_withFlexboxLayoutLayoutParams() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_empty_children,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.alignItems = AlignItems.FLEX_START
                        val first = createTextView(activity, "1", 0)
                        val second = createTextView(activity, "2", 0)
                        val lp1 = FlexboxLayout.LayoutParams(100, 100)
                        lp1.setMargins(10, 10, 10, 10)
                        val lp2 = FlexboxLayout.LayoutParams(100, 100)
                        lp2.setMargins(20, 20, 20, 20)
                        first.layoutParams = lp1
                        second.layoutParams = lp2
                        flexboxLayout.addView(first)
                        flexboxLayout.addView(second)
                    }
                })

        assertThat(flexboxLayout.childCount, `is`(2))
        val view1 = flexboxLayout.getChildAt(0)
        val view2 = flexboxLayout.getChildAt(1)
        // Assert the coordinates of the views added programmatically with margins
        assertThat(view1.left, `is`(10))
        assertThat(view1.top, `is`(10))
        assertThat(view1.bottom, `is`(110))
        assertThat(view1.right, `is`(110))
        assertThat(view2.left, `is`(140))
        assertThat(view2.top, `is`(20))
        assertThat(view2.bottom, `is`(120))
        assertThat(view2.right, `is`(240))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testMaxLines() {
        val activity = activityRule.activity
        val flexboxLayout = createFlexboxLayout(R.layout.activity_empty_children,
                object : Configuration {
                    override fun apply(flexboxLayout: FlexboxLayout) {
                        flexboxLayout.maxLine = 3
                        for (i in 1..50) {
                            val textView = createTextView(activity, i.toString(), 0)
                            val lp = FlexboxLayout.LayoutParams(100, 100)
                            lp.flexShrink = 0f
                            textView.layoutParams = lp
                            flexboxLayout.addView(textView)
                        }
                    }
                })
        assertThat(flexboxLayout.childCount, `is`(50))
        assertThat(flexboxLayout.flexLines.size, `is`(3))
    }

    @Throws(Throwable::class)
    private fun createFlexboxLayout(@LayoutRes activityLayoutResId: Int,
                                    configuration: Configuration = Configuration.EMPTY): FlexboxLayout {
        val activity = activityRule.activity
        activityRule.runOnUiThread {
            activity.setContentView(activityLayoutResId)
            val flexboxLayout = activity.findViewById<FlexboxLayout>(R.id.flexbox_layout)
            configuration.apply(flexboxLayout)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        return activity.findViewById<View>(R.id.flexbox_layout) as FlexboxLayout
    }

    private fun createTextView(context: Context, text: String, order: Int): TextView {
        val textView = TextView(context)
        textView.text = text
        val lp = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.order = order
        textView.layoutParams = lp
        return textView
    }

    private fun hasWidth(width: Int): ViewAssertion {
        return matches(object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("expected width: $width")
            }

            override fun describeMismatchSafely(item: View, mismatchDescription: Description) {
                mismatchDescription.appendText("actual width: " + item.width)
            }

            override fun matchesSafely(item: View) = item.width == width
        })
    }

    private fun hasHeight(height: Int): ViewAssertion {
        return matches(object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("expected height: $height")
            }

            override fun describeMismatchSafely(item: View, mismatchDescription: Description) {
                mismatchDescription.appendText("actual height: " + item.height)
            }

            override fun matchesSafely(item: View) = item.height == height
        })
    }

    private interface Configuration {

        fun apply(flexboxLayout: FlexboxLayout)

        companion object {
            val EMPTY: Configuration = object : Configuration {
                override fun apply(flexboxLayout: FlexboxLayout) = Unit
            }
        }
    }
}
