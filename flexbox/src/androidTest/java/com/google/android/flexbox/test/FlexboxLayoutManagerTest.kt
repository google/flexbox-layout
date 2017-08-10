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
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.*
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.FlakyTest
import android.support.test.filters.MediumTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.flexbox.*
import com.google.android.flexbox.FlexboxItemDecoration.HORIZONTAL
import com.google.android.flexbox.FlexboxItemDecoration.VERTICAL
import com.google.android.flexbox.test.IsEqualAllowingError.Companion.isEqualAllowingError
import junit.framework.Assert.assertTrue
import org.hamcrest.Matchers.*
import org.hamcrest.core.IsNot.not
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for [FlexboxLayoutManager].
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class FlexboxLayoutManagerTest {

    @JvmField
    @Rule
    var activityRule = ActivityTestRule(FlexboxTestActivity::class.java)

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testLoadFromXml() {
        val activity = activityRule.activity
        activityRule.runOnUiThread { activity.setContentView(R.layout.recyclerview_reverse) }
        val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
        val layoutManager = recyclerView.layoutManager
        assertThat(recyclerView, `is`(notNullValue()))
        assertThat(layoutManager, `is`(instanceOf<Any>(FlexboxLayoutManager::class.java)))
        val flexboxLayoutManager = layoutManager as FlexboxLayoutManager
        assertThat(flexboxLayoutManager.flexDirection, `is`(FlexDirection.ROW_REVERSE))
        assertThat(flexboxLayoutManager.flexWrap, `is`(FlexWrap.WRAP))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testChangeAttributesFromCode() {
        val activity = activityRule.activity
        activityRule.runOnUiThread { activity.setContentView(R.layout.recyclerview_reverse) }
        val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
        val layoutManager = recyclerView.layoutManager
        assertThat(recyclerView, `is`(notNullValue()))
        assertThat(layoutManager, `is`(instanceOf<Any>(FlexboxLayoutManager::class.java)))
        val flexboxLayoutManager = layoutManager as FlexboxLayoutManager
        assertThat(flexboxLayoutManager.flexDirection, `is`(FlexDirection.ROW_REVERSE))
        assertThat(flexboxLayoutManager.flexWrap, `is`(FlexWrap.WRAP))

        flexboxLayoutManager.flexDirection = FlexDirection.COLUMN
        flexboxLayoutManager.flexWrap = FlexWrap.NOWRAP
        flexboxLayoutManager.justifyContent = JustifyContent.CENTER
        flexboxLayoutManager.alignItems = AlignItems.FLEX_END
        assertThat(flexboxLayoutManager.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(flexboxLayoutManager.flexWrap, `is`(FlexWrap.NOWRAP))
        assertThat(flexboxLayoutManager.justifyContent, `is`(JustifyContent.CENTER))
        assertThat(flexboxLayoutManager.alignItems, `is`(AlignItems.FLEX_END))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAddViewHolders_direction_row_not_wrapped() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            adapter.addItem(createLayoutParams(activity, 120, 80))
            adapter.addItem(createLayoutParams(activity, 120, 80))
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW))
        assertThat(layoutManager.flexWrap, `is`(FlexWrap.WRAP))
        // Only 2 items are added and all items should be attached (visible). So both
        // getChildCount and getFlexItemCount(including detached items) should return the same value
        assertThat(layoutManager.flexItemCount, `is`(2))
        assertThat(layoutManager.childCount, `is`(2))
        assertThat(layoutManager.flexLines.size, `is`(1))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAddViewHolders_direction_row_wrapped() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            adapter.addItem(createLayoutParams(activity, 120, 80))
            adapter.addItem(createLayoutParams(activity, 120, 80))
            adapter.addItem(createLayoutParams(activity, 120, 80))
            adapter.addItem(createLayoutParams(activity, 120, 80))
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // 4 items are added and all items should be attached (visible). So both
        // getChildCount and getFlexItemCount(including detached items) should return the same value
        assertThat(layoutManager.flexItemCount, `is`(4))
        assertThat(layoutManager.childCount, `is`(4))
        assertThat(layoutManager.flexLines.size, `is`(2))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAddViewHolders_direction_row_partOfItems_detached() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            adapter.addItem(createLayoutParams(activity, 150, 90))
            adapter.addItem(createLayoutParams(activity, 150, 90))
            adapter.addItem(createLayoutParams(activity, 150, 90))
            adapter.addItem(createLayoutParams(activity, 150, 90))
            adapter.addItem(createLayoutParams(activity, 150, 90))
            adapter.addItem(createLayoutParams(activity, 150, 90))
            adapter.addItem(createLayoutParams(activity, 150, 90))
            adapter.addItem(createLayoutParams(activity, 150, 90))
            adapter.addItem(createLayoutParams(activity, 150, 90))
            // RecyclerView width: 320, height: 240.
            // Computed FlexContainer width: 320, height: 450
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // In total 9 items are added but the seventh item and the items follow aren't attached
        // (invisible) so are not included in the count of of the getChildCount.
        assertThat(layoutManager.flexItemCount, `is`(9))
        assertThat(layoutManager.childCount, `is`(6))

        // At first only the visible area of the flex lines are calculated
        assertThat(layoutManager.flexLines.size, `is`(3))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAddViewHolders_direction_row_scrollVertically() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            adapter.addItem(createLayoutParams(activity, 150, 90))
            adapter.addItem(createLayoutParams(activity, 150, 90))
            adapter.addItem(createLayoutParams(activity, 150, 90))
            adapter.addItem(createLayoutParams(activity, 150, 90))
            adapter.addItem(createLayoutParams(activity, 150, 90))
            adapter.addItem(createLayoutParams(activity, 150, 90))
            adapter.addItem(createLayoutParams(activity, 150, 90))
            adapter.addItem(createLayoutParams(activity, 150, 90))
            adapter.addItem(createLayoutParams(activity, 150, 90))
            // RecyclerView width: 320, height: 240.
            // Computed FlexContainer width: 320, height: 450
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // In total 9 items are added but the seventh item and the items follow aren't attached
        // (invisible) so are not included in the count of of the getChildCount.
        assertThat(layoutManager.flexItemCount, `is`(9))
        assertThat(layoutManager.childCount, `is`(6))
        // At first only the visible area of the flex lines are calculated
        assertThat(layoutManager.flexLines.size, `is`(3))

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        assertThat(layoutManager.flexItemCount, `is`(9))
        // The RecyclerView is swiped to top until it reaches the bottom of the view.
        // The number of the visible views should be 5
        assertThat(layoutManager.childCount, `is`(5))
        // Since the RecyclerView is swiped to the bottom, all flex lines should be calculated
        // by now
        assertThat(layoutManager.flexLines.size, `is`(5))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexGrow() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 150, 130)
            lp1.flexGrow = 1.0f
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 150, 130)
            lp2.flexGrow = 1.0f
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 150, 130)
            lp3.flexGrow = 1.0f
            adapter.addItem(lp3)
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // The flexGrow parameters for all LayoutParams are set to 1.0, expecting each child to
        // fill the horizontal remaining space
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(2))
        assertThat(layoutManager.getChildAt(0).width, isEqualAllowingError(activity.dpToPixel(160)))
        assertThat(layoutManager.getChildAt(1).width, isEqualAllowingError(activity.dpToPixel(160)))
        assertThat(layoutManager.getChildAt(2).width, isEqualAllowingError(activity.dpToPixel(320)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAddViewHolders_direction_column_partOfItems_detached() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            adapter.addItem(createLayoutParams(activity, 120, 100))
            adapter.addItem(createLayoutParams(activity, 120, 100))
            adapter.addItem(createLayoutParams(activity, 120, 100))
            adapter.addItem(createLayoutParams(activity, 120, 100))
            adapter.addItem(createLayoutParams(activity, 120, 100))
            adapter.addItem(createLayoutParams(activity, 120, 100))
            adapter.addItem(createLayoutParams(activity, 120, 100))
            adapter.addItem(createLayoutParams(activity, 120, 100))
            adapter.addItem(createLayoutParams(activity, 120, 100))

            layoutManager.flexDirection = FlexDirection.COLUMN
            // RecyclerView width: 320, height: 240.
            // Computed FlexContainer width: 450, height: 240
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // In total 9 items are added but the seventh item and the items after aren't attached
        // (invisible) so are not included in the count of of the getChildCount.
        assertThat(layoutManager.flexItemCount, `is`(9))
        assertThat(layoutManager.childCount, `is`(6))

        // At first only the visible area of the flex lines are calculated
        assertThat(layoutManager.flexLines.size, `is`(3))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAddViewHolders_direction_column_scrollHorizontally() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            adapter.addItem(createLayoutParams(activity, 120, 100))
            adapter.addItem(createLayoutParams(activity, 120, 100))
            adapter.addItem(createLayoutParams(activity, 120, 100))
            adapter.addItem(createLayoutParams(activity, 120, 100))
            adapter.addItem(createLayoutParams(activity, 120, 100))
            adapter.addItem(createLayoutParams(activity, 120, 100))
            adapter.addItem(createLayoutParams(activity, 120, 100))
            adapter.addItem(createLayoutParams(activity, 120, 100))
            adapter.addItem(createLayoutParams(activity, 120, 100))

            layoutManager.flexDirection = FlexDirection.COLUMN
            // RecyclerView width: 320, height: 240.
            // Computed FlexContainer width: 500, height: 240
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // In total 9 items are added but the seventh item and the items follow aren't attached
        // (invisible) so are not included in the count of of the getChildCount.
        assertThat(layoutManager.flexItemCount, `is`(9))
        assertThat(layoutManager.childCount, `is`(6))
        // At first only the visible area of the flex lines are calculated
        assertThat(layoutManager.flexLines.size, `is`(3))

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        assertThat(layoutManager.flexItemCount, `is`(9))
        // The RecyclerView is swiped to top until it reaches the right edge of the view.
        // The number of the visible views should be 5
        assertThat(layoutManager.childCount, `is`(5))
        // Since the RecyclerView is swiped to the bottom, all flex lines should be calculated
        // by now
        assertThat(layoutManager.flexLines.size, `is`(5))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_flexStart_direction_row() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp3)
            layoutManager.justifyContent = JustifyContent.FLEX_START
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.justifyContent, `is`(JustifyContent.FLEX_START))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).left, isEqualAllowingError(activity.dpToPixel(0)))
        assertThat(layoutManager.getChildAt(0).right, isEqualAllowingError(activity.dpToPixel(50)))
        assertThat(layoutManager.getChildAt(1).left, isEqualAllowingError(activity.dpToPixel(50)))
        assertThat(layoutManager.getChildAt(1).right, isEqualAllowingError(activity.dpToPixel(100)))
        assertThat(layoutManager.getChildAt(2).left, isEqualAllowingError(activity.dpToPixel(100)))
        assertThat(layoutManager.getChildAt(2).right, isEqualAllowingError(activity.dpToPixel(150)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_flexEnd_direction_row() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp3)
            layoutManager.justifyContent = JustifyContent.FLEX_END
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.justifyContent, `is`(JustifyContent.FLEX_END))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).left, isEqualAllowingError(activity.dpToPixel(170)))
        assertThat(layoutManager.getChildAt(0).right, isEqualAllowingError(activity.dpToPixel(220)))
        assertThat(layoutManager.getChildAt(1).left, isEqualAllowingError(activity.dpToPixel(220)))
        assertThat(layoutManager.getChildAt(1).right, isEqualAllowingError(activity.dpToPixel(270)))
        assertThat(layoutManager.getChildAt(2).left, isEqualAllowingError(activity.dpToPixel(270)))
        assertThat(layoutManager.getChildAt(2).right, isEqualAllowingError(activity.dpToPixel(320)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_center_direction_row() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp3)
            layoutManager.justifyContent = JustifyContent.CENTER
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.justifyContent, `is`(JustifyContent.CENTER))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).left, isEqualAllowingError(activity.dpToPixel(85)))
        assertThat(layoutManager.getChildAt(0).right, isEqualAllowingError(activity.dpToPixel(135)))
        assertThat(layoutManager.getChildAt(1).left, isEqualAllowingError(activity.dpToPixel(135)))
        assertThat(layoutManager.getChildAt(1).right, isEqualAllowingError(activity.dpToPixel(185)))
        assertThat(layoutManager.getChildAt(2).left, isEqualAllowingError(activity.dpToPixel(185)))
        assertThat(layoutManager.getChildAt(2).right, isEqualAllowingError(activity.dpToPixel(235)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceAround_direction_row() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp3)
            layoutManager.justifyContent = JustifyContent.SPACE_AROUND
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.justifyContent, `is`(JustifyContent.SPACE_AROUND))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).left, isEqualAllowingError(activity.dpToPixel(28)))
        assertThat(layoutManager.getChildAt(0).right, isEqualAllowingError(activity.dpToPixel(78)))
        assertThat(layoutManager.getChildAt(1).left, isEqualAllowingError(activity.dpToPixel(135)))
        assertThat(layoutManager.getChildAt(1).right, isEqualAllowingError(activity.dpToPixel(185)))
        assertThat(layoutManager.getChildAt(2).left, isEqualAllowingError(activity.dpToPixel(242)))
        assertThat(layoutManager.getChildAt(2).right, isEqualAllowingError(activity.dpToPixel(292)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceBetween_direction_row() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp3)
            layoutManager.justifyContent = JustifyContent.SPACE_BETWEEN
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.justifyContent, `is`(JustifyContent.SPACE_BETWEEN))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).left, isEqualAllowingError(activity.dpToPixel(0)))
        assertThat(layoutManager.getChildAt(0).right, isEqualAllowingError(activity.dpToPixel(50)))
        assertThat(layoutManager.getChildAt(1).left, isEqualAllowingError(activity.dpToPixel(135)))
        assertThat(layoutManager.getChildAt(1).right, isEqualAllowingError(activity.dpToPixel(185)))
        assertThat(layoutManager.getChildAt(2).left, isEqualAllowingError(activity.dpToPixel(270)))
        assertThat(layoutManager.getChildAt(2).right, isEqualAllowingError(activity.dpToPixel(320)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_flexStart_direction_rowReverse() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp3)
            layoutManager.flexDirection = FlexDirection.ROW_REVERSE
            layoutManager.justifyContent = JustifyContent.FLEX_START
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW_REVERSE))
        assertThat(layoutManager.justifyContent, `is`(JustifyContent.FLEX_START))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).left, isEqualAllowingError(activity.dpToPixel(270)))
        assertThat(layoutManager.getChildAt(0).right, isEqualAllowingError(activity.dpToPixel(320)))
        assertThat(layoutManager.getChildAt(1).left, isEqualAllowingError(activity.dpToPixel(220)))
        assertThat(layoutManager.getChildAt(1).right, isEqualAllowingError(activity.dpToPixel(270)))
        assertThat(layoutManager.getChildAt(2).left, isEqualAllowingError(activity.dpToPixel(170)))
        assertThat(layoutManager.getChildAt(2).right, isEqualAllowingError(activity.dpToPixel(220)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_flexEnd_direction_rowReverse() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp3)
            layoutManager.flexDirection = FlexDirection.ROW_REVERSE
            layoutManager.justifyContent = JustifyContent.FLEX_END
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW_REVERSE))
        assertThat(layoutManager.justifyContent, `is`(JustifyContent.FLEX_END))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).left, isEqualAllowingError(activity.dpToPixel(100)))
        assertThat(layoutManager.getChildAt(0).right, isEqualAllowingError(activity.dpToPixel(150)))
        assertThat(layoutManager.getChildAt(1).left, isEqualAllowingError(activity.dpToPixel(50)))
        assertThat(layoutManager.getChildAt(1).right, isEqualAllowingError(activity.dpToPixel(100)))
        assertThat(layoutManager.getChildAt(2).left, isEqualAllowingError(activity.dpToPixel(0)))
        assertThat(layoutManager.getChildAt(2).right, isEqualAllowingError(activity.dpToPixel(50)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_center_direction_rowReverse() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp3)
            layoutManager.flexDirection = FlexDirection.ROW_REVERSE
            layoutManager.justifyContent = JustifyContent.CENTER
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW_REVERSE))
        assertThat(layoutManager.justifyContent, `is`(JustifyContent.CENTER))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).left, isEqualAllowingError(activity.dpToPixel(185)))
        assertThat(layoutManager.getChildAt(0).right, isEqualAllowingError(activity.dpToPixel(235)))
        assertThat(layoutManager.getChildAt(1).left, isEqualAllowingError(activity.dpToPixel(135)))
        assertThat(layoutManager.getChildAt(1).right, isEqualAllowingError(activity.dpToPixel(185)))
        assertThat(layoutManager.getChildAt(2).left, isEqualAllowingError(activity.dpToPixel(85)))
        assertThat(layoutManager.getChildAt(2).right, isEqualAllowingError(activity.dpToPixel(135)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceAround_direction_rowReverse() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp3)
            layoutManager.flexDirection = FlexDirection.ROW_REVERSE
            layoutManager.justifyContent = JustifyContent.SPACE_AROUND
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW_REVERSE))
        assertThat(layoutManager.justifyContent, `is`(JustifyContent.SPACE_AROUND))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).left, isEqualAllowingError(activity.dpToPixel(242)))
        assertThat(layoutManager.getChildAt(0).right, isEqualAllowingError(activity.dpToPixel(292)))
        assertThat(layoutManager.getChildAt(1).left, isEqualAllowingError(activity.dpToPixel(135)))
        assertThat(layoutManager.getChildAt(1).right, isEqualAllowingError(activity.dpToPixel(185)))
        assertThat(layoutManager.getChildAt(2).left, isEqualAllowingError(activity.dpToPixel(28)))
        assertThat(layoutManager.getChildAt(2).right, isEqualAllowingError(activity.dpToPixel(78)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceBetween_direction_rowReverse() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 50, 100)
            adapter.addItem(lp3)
            layoutManager.flexDirection = FlexDirection.ROW_REVERSE
            layoutManager.justifyContent = JustifyContent.SPACE_BETWEEN
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW_REVERSE))
        assertThat(layoutManager.justifyContent, `is`(JustifyContent.SPACE_BETWEEN))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).left, isEqualAllowingError(activity.dpToPixel(270)))
        assertThat(layoutManager.getChildAt(0).right, isEqualAllowingError(activity.dpToPixel(320)))
        assertThat(layoutManager.getChildAt(1).left, isEqualAllowingError(activity.dpToPixel(135)))
        assertThat(layoutManager.getChildAt(1).right, isEqualAllowingError(activity.dpToPixel(185)))
        assertThat(layoutManager.getChildAt(2).left, isEqualAllowingError(activity.dpToPixel(0)))
        assertThat(layoutManager.getChildAt(2).right, isEqualAllowingError(activity.dpToPixel(50)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_flexStart_direction_column() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp3)
            layoutManager.flexDirection = FlexDirection.COLUMN
            layoutManager.justifyContent = JustifyContent.FLEX_START
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(layoutManager.justifyContent, `is`(JustifyContent.FLEX_START))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).top, isEqualAllowingError(activity.dpToPixel(0)))
        assertThat(layoutManager.getChildAt(0).bottom, isEqualAllowingError(activity.dpToPixel(50)))
        assertThat(layoutManager.getChildAt(1).top, isEqualAllowingError(activity.dpToPixel(50)))
        assertThat(layoutManager.getChildAt(1).bottom, isEqualAllowingError(activity.dpToPixel(100)))
        assertThat(layoutManager.getChildAt(2).top, isEqualAllowingError(activity.dpToPixel(100)))
        assertThat(layoutManager.getChildAt(2).bottom, isEqualAllowingError(activity.dpToPixel(150)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_flexEnd_direction_column() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp3)
            layoutManager.flexDirection = FlexDirection.COLUMN
            layoutManager.justifyContent = JustifyContent.FLEX_END
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(layoutManager.justifyContent, `is`(JustifyContent.FLEX_END))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).top, isEqualAllowingError(activity.dpToPixel(90)))
        assertThat(layoutManager.getChildAt(0).bottom, isEqualAllowingError(activity.dpToPixel(140)))
        assertThat(layoutManager.getChildAt(1).top, isEqualAllowingError(activity.dpToPixel(140)))
        assertThat(layoutManager.getChildAt(1).bottom, isEqualAllowingError(activity.dpToPixel(190)))
        assertThat(layoutManager.getChildAt(2).top, isEqualAllowingError(activity.dpToPixel(190)))
        assertThat(layoutManager.getChildAt(2).bottom, isEqualAllowingError(activity.dpToPixel(240)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_center_direction_column() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp3)
            layoutManager.flexDirection = FlexDirection.COLUMN
            layoutManager.justifyContent = JustifyContent.CENTER
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(layoutManager.justifyContent, `is`(JustifyContent.CENTER))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).top, isEqualAllowingError(activity.dpToPixel(45)))
        assertThat(layoutManager.getChildAt(0).bottom, isEqualAllowingError(activity.dpToPixel(95)))
        assertThat(layoutManager.getChildAt(1).top, isEqualAllowingError(activity.dpToPixel(95)))
        assertThat(layoutManager.getChildAt(1).bottom, isEqualAllowingError(activity.dpToPixel(145)))
        assertThat(layoutManager.getChildAt(2).top, isEqualAllowingError(activity.dpToPixel(145)))
        assertThat(layoutManager.getChildAt(2).bottom, isEqualAllowingError(activity.dpToPixel(195)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceAround_direction_column() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp3)
            layoutManager.flexDirection = FlexDirection.COLUMN
            layoutManager.justifyContent = JustifyContent.SPACE_AROUND
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(layoutManager.justifyContent, `is`(JustifyContent.SPACE_AROUND))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).top, isEqualAllowingError(activity.dpToPixel(15)))
        assertThat(layoutManager.getChildAt(0).bottom, isEqualAllowingError(activity.dpToPixel(65)))
        assertThat(layoutManager.getChildAt(1).top, isEqualAllowingError(activity.dpToPixel(95)))
        assertThat(layoutManager.getChildAt(1).bottom, isEqualAllowingError(activity.dpToPixel(145)))
        assertThat(layoutManager.getChildAt(2).top, isEqualAllowingError(activity.dpToPixel(175)))
        assertThat(layoutManager.getChildAt(2).bottom, isEqualAllowingError(activity.dpToPixel(225)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceBetween_direction_column() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp3)
            layoutManager.flexDirection = FlexDirection.COLUMN
            layoutManager.justifyContent = JustifyContent.SPACE_BETWEEN
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(layoutManager.justifyContent, `is`(JustifyContent.SPACE_BETWEEN))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).top, isEqualAllowingError(activity.dpToPixel(0)))
        assertThat(layoutManager.getChildAt(0).bottom, isEqualAllowingError(activity.dpToPixel(50)))
        assertThat(layoutManager.getChildAt(1).top, isEqualAllowingError(activity.dpToPixel(95)))
        assertThat(layoutManager.getChildAt(1).bottom, isEqualAllowingError(activity.dpToPixel(145)))
        assertThat(layoutManager.getChildAt(2).top, isEqualAllowingError(activity.dpToPixel(190)))
        assertThat(layoutManager.getChildAt(2).bottom, isEqualAllowingError(activity.dpToPixel(240)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_flexStart_direction_columnReverse() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp3)
            layoutManager.flexDirection = FlexDirection.COLUMN_REVERSE
            layoutManager.justifyContent = JustifyContent.FLEX_START
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN_REVERSE))
        assertThat(layoutManager.justifyContent, `is`(JustifyContent.FLEX_START))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).top, isEqualAllowingError(activity.dpToPixel(190)))
        assertThat(layoutManager.getChildAt(0).bottom, isEqualAllowingError(activity.dpToPixel(240)))
        assertThat(layoutManager.getChildAt(1).top, isEqualAllowingError(activity.dpToPixel(140)))
        assertThat(layoutManager.getChildAt(1).bottom, isEqualAllowingError(activity.dpToPixel(190)))
        assertThat(layoutManager.getChildAt(2).top, isEqualAllowingError(activity.dpToPixel(90)))
        assertThat(layoutManager.getChildAt(2).bottom, isEqualAllowingError(activity.dpToPixel(140)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_flexEnd_direction_columnReverse() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp3)
            layoutManager.flexDirection = FlexDirection.COLUMN_REVERSE
            layoutManager.justifyContent = JustifyContent.FLEX_END
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN_REVERSE))
        assertThat(layoutManager.justifyContent, `is`(JustifyContent.FLEX_END))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).top, isEqualAllowingError(activity.dpToPixel(100)))
        assertThat(layoutManager.getChildAt(0).bottom, isEqualAllowingError(activity.dpToPixel(150)))
        assertThat(layoutManager.getChildAt(1).top, isEqualAllowingError(activity.dpToPixel(50)))
        assertThat(layoutManager.getChildAt(1).bottom, isEqualAllowingError(activity.dpToPixel(100)))
        assertThat(layoutManager.getChildAt(2).top, isEqualAllowingError(activity.dpToPixel(0)))
        assertThat(layoutManager.getChildAt(2).bottom, isEqualAllowingError(activity.dpToPixel(50)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_center_direction_columnReverse() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp3)
            layoutManager.flexDirection = FlexDirection.COLUMN_REVERSE
            layoutManager.justifyContent = JustifyContent.CENTER
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN_REVERSE))
        assertThat(layoutManager.justifyContent, `is`(JustifyContent.CENTER))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).top, isEqualAllowingError(activity.dpToPixel(145)))
        assertThat(layoutManager.getChildAt(0).bottom, isEqualAllowingError(activity.dpToPixel(195)))
        assertThat(layoutManager.getChildAt(1).top, isEqualAllowingError(activity.dpToPixel(95)))
        assertThat(layoutManager.getChildAt(1).bottom, isEqualAllowingError(activity.dpToPixel(145)))
        assertThat(layoutManager.getChildAt(2).top, isEqualAllowingError(activity.dpToPixel(45)))
        assertThat(layoutManager.getChildAt(2).bottom, isEqualAllowingError(activity.dpToPixel(95)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceAround_direction_columnReverse() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp3)
            layoutManager.flexDirection = FlexDirection.COLUMN_REVERSE
            layoutManager.justifyContent = JustifyContent.SPACE_AROUND
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN_REVERSE))
        assertThat(layoutManager.justifyContent, `is`(JustifyContent.SPACE_AROUND))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).top, isEqualAllowingError(activity.dpToPixel(175)))
        assertThat(layoutManager.getChildAt(0).bottom, isEqualAllowingError(activity.dpToPixel(225)))
        assertThat(layoutManager.getChildAt(1).top, isEqualAllowingError(activity.dpToPixel(95)))
        assertThat(layoutManager.getChildAt(1).bottom, isEqualAllowingError(activity.dpToPixel(145)))
        assertThat(layoutManager.getChildAt(2).top, isEqualAllowingError(activity.dpToPixel(15)))
        assertThat(layoutManager.getChildAt(2).bottom, isEqualAllowingError(activity.dpToPixel(65)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testJustifyContent_spaceBetween_direction_columnReverse() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 100, 50)
            adapter.addItem(lp3)
            layoutManager.flexDirection = FlexDirection.COLUMN_REVERSE
            layoutManager.justifyContent = JustifyContent.SPACE_BETWEEN
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN_REVERSE))
        assertThat(layoutManager.justifyContent, `is`(JustifyContent.SPACE_BETWEEN))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        assertThat(layoutManager.getChildAt(0).top, isEqualAllowingError(activity.dpToPixel(190)))
        assertThat(layoutManager.getChildAt(0).bottom, isEqualAllowingError(activity.dpToPixel(240)))
        assertThat(layoutManager.getChildAt(1).top, isEqualAllowingError(activity.dpToPixel(95)))
        assertThat(layoutManager.getChildAt(1).bottom, isEqualAllowingError(activity.dpToPixel(145)))
        assertThat(layoutManager.getChildAt(2).top, isEqualAllowingError(activity.dpToPixel(0)))
        assertThat(layoutManager.getChildAt(2).bottom, isEqualAllowingError(activity.dpToPixel(50)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testLargeItem_scrollFast_direction_row() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            for (i in 0..199) {
                val lp = createLayoutParams(activity, 100, 50)
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW))
        assertThat(layoutManager.flexItemCount, `is`(200))
        // Only the visible items
        assertThat(layoutManager.childCount, `is`(not(200)))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        // Should be scrolled to the bottom by now
        assertThat(layoutManager.flexItemCount, `is`(200))
        // Only the visible items
        assertThat(layoutManager.childCount, `is`(not(200)))

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        // Should be scrolled to the top
        assertThat(layoutManager.flexItemCount, `is`(200))
        // Only the visible items
        assertThat(layoutManager.childCount, `is`(not(200)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testLargeItem_scrollFast_direction_column() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.COLUMN
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            for (i in 0..199) {
                val lp = createLayoutParams(activity, 70, 80)
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(layoutManager.flexItemCount, `is`(200))
        // Only the visible items
        assertThat(layoutManager.childCount, `is`(not(200)))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))

        // Should be scrolled to the right edge by now
        assertThat(layoutManager.flexItemCount, `is`(200))
        // Only the visible items
        assertThat(layoutManager.childCount, `is`(not(200)))

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        // Should be scrolled to the left edge by now
        assertThat(layoutManager.flexItemCount, `is`(200))
        // Only the visible items
        assertThat(layoutManager.childCount, `is`(not(200)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignItems_stretch_direction_row() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.alignItems = AlignItems.STRETCH
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 70, 80)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 70, 50)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 70, 30)
            adapter.addItem(lp3)
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW))
        assertThat(layoutManager.alignItems, `is`(AlignItems.STRETCH))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        // Verify all items heights are stretched
        assertThat(layoutManager.getChildAt(0).height, isEqualAllowingError(activity.dpToPixel(80)))
        assertThat(layoutManager.getChildAt(1).height, isEqualAllowingError(activity.dpToPixel(80)))
        assertThat(layoutManager.getChildAt(2).height, isEqualAllowingError(activity.dpToPixel(80)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignItems_stretch_direction_column() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.COLUMN
            layoutManager.alignItems = AlignItems.STRETCH
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 80, 70)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 50, 70)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 30, 70)
            adapter.addItem(lp3)
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(layoutManager.alignItems, `is`(AlignItems.STRETCH))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        // Verify all items widths are stretched
        assertThat(layoutManager.getChildAt(0).width, isEqualAllowingError(activity.dpToPixel(80)))
        assertThat(layoutManager.getChildAt(1).width, isEqualAllowingError(activity.dpToPixel(80)))
        assertThat(layoutManager.getChildAt(2).width, isEqualAllowingError(activity.dpToPixel(80)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignSelf_stretch_direction_row() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.alignItems = AlignItems.FLEX_START
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 70, 80)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 70, 50)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 70, 30)
            lp3.alignSelf = AlignSelf.STRETCH
            adapter.addItem(lp3)
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW))
        assertThat(layoutManager.alignItems, `is`(AlignItems.FLEX_START))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        // Verify the item whose align self is set to stretch is stretched
        assertThat(layoutManager.getChildAt(0).height, isEqualAllowingError(activity.dpToPixel(80)))
        assertThat(layoutManager.getChildAt(1).height, isEqualAllowingError(activity.dpToPixel(50)))
        assertThat(layoutManager.getChildAt(2).height, isEqualAllowingError(activity.dpToPixel(80)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testAlignSelf_stretch_direction_column() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.COLUMN
            layoutManager.alignItems = AlignItems.FLEX_START
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 80, 70)
            adapter.addItem(lp1)
            val lp2 = createLayoutParams(activity, 50, 70)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 30, 70)
            lp3.alignSelf = AlignSelf.STRETCH
            adapter.addItem(lp3)
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(layoutManager.alignItems, `is`(AlignItems.FLEX_START))
        assertThat(layoutManager.flexItemCount, `is`(3))
        assertThat(layoutManager.flexLines.size, `is`(1))
        // Verify the item whose align self is set to stretch is stretched
        assertThat(layoutManager.getChildAt(0).width, isEqualAllowingError(activity.dpToPixel(80)))
        assertThat(layoutManager.getChildAt(1).width, isEqualAllowingError(activity.dpToPixel(50)))
        assertThat(layoutManager.getChildAt(2).width, isEqualAllowingError(activity.dpToPixel(80)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testStretchViews_from_middle_direction_row() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.alignItems = AlignItems.STRETCH
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            for (i in 0..49) {
                val lp = createLayoutParams(activity, 70, 80)
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW))
        assertThat(layoutManager.alignItems, `is`(AlignItems.STRETCH))

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        activityRule.runOnUiThread {
            layoutManager.alignItems = AlignItems.STRETCH
            val lp = createLayoutParams(activity, 70, 20)
            // Add an item whose height is less than the other items.
            // But with alignItems set to stretch, the height of the item should be stretched
            adapter.addItem(lp)
            adapter.notifyDataSetChanged()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.getChildAt(layoutManager.childCount - 1).height,
                isEqualAllowingError(activity.dpToPixel(80)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testStretchViews_from_middle_direction_column() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.COLUMN
            layoutManager.alignItems = AlignItems.STRETCH
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            for (i in 0..49) {
                val lp = createLayoutParams(activity, 70, 50)
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(layoutManager.alignItems, `is`(AlignItems.STRETCH))

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_RIGHT,
                GeneralLocation.TOP_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_RIGHT,
                GeneralLocation.TOP_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_RIGHT,
                GeneralLocation.TOP_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_RIGHT,
                GeneralLocation.TOP_LEFT))
        activityRule.runOnUiThread {
            layoutManager.alignItems = AlignItems.STRETCH
            val lp = createLayoutParams(activity, 20, 50)
            // Add an item whose width is less than the other items.
            // But with alignItems set to stretch, the width of the item should be stretched
            adapter.addItem(lp)
            adapter.notifyDataSetChanged()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.getChildAt(layoutManager.childCount - 1).width,
                isEqualAllowingError(activity.dpToPixel(70)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testScrollToTop_middleItem_as_anchorPosition() {
        // There was an issue that the anchor position was based on the first item in the first
        // visible flex line when scrolling to top. But the anchor position should be based on the
        // flex line position (view which has the minimum top position in the same flex line)
        // This test verifies the issue is fixed.
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        val positionInSecondLine = 6
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.alignItems = AlignItems.FLEX_START
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            for (i in 0..49) {
                var lp = createLayoutParams(activity, 70, 80)
                if (i == positionInSecondLine) {
                    // Change the height from other items in the second line, not the first item
                    // in the second line
                    lp = createLayoutParams(activity, 70, 130)
                }
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
            // Each line has 4 (320 / 70) flex items and 12 (50 / 4) lines in total
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW))

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        // By this moment reached to the bottom

        // Now scrolling to the top to see if the views in the first flex line is correctly placed
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))

        assertThat(layoutManager.getChildAt(positionInSecondLine).bottom,
                isEqualAllowingError(activity.dpToPixel(210))) // 80 + 130
        // Verify that the view in the same line's cross axis position is correct
        assertThat(layoutManager.getChildAt(positionInSecondLine - 1).bottom,
                isEqualAllowingError(activity.dpToPixel(160))) // 80 + 80
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testScrollToLeft_middleItem_as_anchorPosition() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        val positionInSecondLine = 6
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.COLUMN
            layoutManager.alignItems = AlignItems.FLEX_START
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            for (i in 0..49) {
                var lp = createLayoutParams(activity, 80, 50)
                if (i == positionInSecondLine) {
                    // Change the width from other items in the second line, not the first item
                    // in the second line
                    lp = createLayoutParams(activity, 130, 50)
                }
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
            // Each line has 4 (240 / 50) flex items and 12 (50 / 4) lines in total
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN))

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        // By this moment reached to the right edge

        // Now scrolling to the left to see if the views in the first flex line is correctly placed
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))

        assertThat(layoutManager.getChildAt(positionInSecondLine).right,
                isEqualAllowingError(activity.dpToPixel(210))) // 80 + 130
        // Verify that the view in the same line's cross axis position is correct
        assertThat(layoutManager.getChildAt(positionInSecondLine - 1).right,
                isEqualAllowingError(activity.dpToPixel(160))) // 80 + 80
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testScrollToBottom_middleItem_as_anchorPosition() {
        // There was an issue that the anchor position was based on the last item in the last
        // visible flex line when scrolling to bottom. But the anchor position should be based on
        // the flex line position (view which has the maximum bottom position in the same flex line)
        // This test verifies the issue is fixed.
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        val positionInSecondBottomLine = 45
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            for (i in 0..49) {
                var lp = createLayoutParams(activity, 70, 80)
                if (i == positionInSecondBottomLine) {
                    // Change the height from other items in the second bottom line
                    lp = createLayoutParams(activity, 70, 130)
                }
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
            // Each line has 4 (320 / 70) flex items and 12 (50 / 4) lines in total
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW))

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        // By this moment reached to the bottom

        // 4 comes from the number of flex items - positionInSecondBottomLine
        val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
        val anchorView = layoutManager.getChildAt(layoutManager.childCount - 4)
        assertThat(recyclerView.bottom - anchorView.top,
                isEqualAllowingError(activity.dpToPixel(210))) // 80 + 130
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testScrollToTop_direction_rowReverse() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW_REVERSE
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            for (i in 0..49) {
                val lp = createLayoutParams(activity, 70, 80)
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
            // Each line has 4 (320 / 70) flex items and 12 (50 / 4) lines in total
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW_REVERSE))

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        // By this moment reached to the bottom

        // Now scrolling to the top to see if the views in the first flex line is correctly placed
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))

        assertThat((layoutManager.getChildAt(0) as TextView).text.toString(), `is`("1"))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexGrow_only_oneItem_has_positive_direction_row() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            for (i in 0..3) {
                val lp = createLayoutParams(activity, 100, 80)
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
            // Flex line 1: 3 items
            // Flex line 2: 1 item
            // Give the second item in the first line a positive flex grow
            adapter.getItemAt(0).setHeight(activity.dpToPixel(140))
            adapter.getItemAt(1).flexGrow = 1.0f
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW))
        // Verify the vertical position (cross size) of the second line is correctly positioned
        assertThat(layoutManager.getChildAt(3).top, isEqualAllowingError(activity.dpToPixel(140)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexGrow_only_oneItem_has_positive_direction_column() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.COLUMN
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            for (i in 0..3) {
                val lp = createLayoutParams(activity, 70, 70)
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
            // Flex line 1: 3 items
            // Flex line 2: 1 item
            // Give the second item in the first line a positive flex grow
            adapter.getItemAt(0).setWidth(activity.dpToPixel(120))
            adapter.getItemAt(1).flexGrow = 1.0f
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN))
        // Verify the horizontal position (cross size) of the second line is correctly positioned
        assertThat(layoutManager.getChildAt(3).left, isEqualAllowingError(activity.dpToPixel(120)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFirstReferenceView_middleOf_line_used_as_anchor() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.alignItems = AlignItems.FLEX_END
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp1 = createLayoutParams(activity, 100, 80)
            adapter.addItem(lp1)
            // The second view in the first line has the maximum height in the same line
            val lp2 = createLayoutParams(activity, 100, 180)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 100, 80)
            adapter.addItem(lp3)
            for (i in 0..29) {
                val lp = createLayoutParams(activity, 100, 80)
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        // By this moment reached to the bottom

        // Now scrolling to the top to see if the views in the first flex line is correctly placed
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW))

        // The top coordinate of the first view should be the height of the second view minus the
        // height of the first view (180 - 80)
        assertThat(layoutManager.getChildAt(0).top, isEqualAllowingError(activity.dpToPixel(100)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testLastReferenceView_middleOf_line_used_as_anchor() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.alignItems = AlignItems.FLEX_START
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter

            for (i in 0..29) {
                val lp = createLayoutParams(activity, 100, 80)
                adapter.addItem(lp)
            }
            val lp1 = createLayoutParams(activity, 100, 80)
            adapter.addItem(lp1)
            // The second view in the last line has the maximum height in the same line
            val lp2 = createLayoutParams(activity, 100, 180)
            adapter.addItem(lp2)
            val lp3 = createLayoutParams(activity, 100, 80)
            adapter.addItem(lp3)
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        // By this moment reached to the bottom

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW))

        // The bottom coordinate of the first view in the last line should be the height of the
        // second view in the last line minus the height of the first view in the last line
        // (180 - 80)
        assertThat(layoutManager.getChildAt(layoutManager.childCount - 2).bottom - layoutManager.getChildAt(layoutManager.childCount - 3).bottom,
                isEqualAllowingError(activity.dpToPixel(100)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testRotateScreen_direction_row() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.alignItems = AlignItems.STRETCH
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter

            for (i in 0..29) {
                val lp = createLayoutParams(activity, 100, 100)
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.CENTER))

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW))
        val anchorView = layoutManager.getChildAt(0)
        val offset = anchorView.top
        assertTrue(offset < 0)

        activityRule.runOnUiThread {
            val orientation = activity.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // Verify that offset position is preserved for the first visible view after the rotation
        val anchorAfterRotate = layoutManager.getChildAt(0)
        assertTrue(anchorAfterRotate.top < 0)
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testRotateScreen_direction_column() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.COLUMN
            layoutManager.alignItems = AlignItems.STRETCH
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter

            for (i in 0..29) {
                val lp = createLayoutParams(activity, 100, 100)
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER))

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN))
        val anchorView = layoutManager.getChildAt(0)
        val offset = anchorView.left
        assertTrue(offset < 0)

        activityRule.runOnUiThread {
            val orientation = activity.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // Verify that offset position is preserved for the first visible view after the rotation
        val anchorAfterRotate = layoutManager.getChildAt(0)
        assertTrue(anchorAfterRotate.left < 0)
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDecoration_direction_row() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        val drawable = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        val itemDecoration = FlexboxItemDecoration(activity)
        itemDecoration.setDrawable(drawable)

        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW
            recyclerView.layoutManager = layoutManager
            recyclerView.addItemDecoration(itemDecoration)
            recyclerView.adapter = adapter

            for (i in 0..9) {
                val lp = createLayoutParams(activity, 90, 100)
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW))
        var view2 = layoutManager.getChildAt(1)
        // 90 (view width) + 10 (divider width)
        assertThat(view2.left, isEqualAllowingError(activity.dpToPixel(100)))
        var view3 = layoutManager.getChildAt(2)
        // 90 (view width) + 10 (divider width) + 90(view width) + 10 (divider width)
        assertThat(view3.left, isEqualAllowingError(activity.dpToPixel(200)))
        var view4 = layoutManager.getChildAt(3)
        // 100 (view height) + 15 (divider height)
        assertThat(view4.top, isEqualAllowingError(activity.dpToPixel(115)))
        var view7 = layoutManager.getChildAt(6)
        // 100 (view height) + 15 (divider height) + 100 (view height) + 15 (divider height)
        assertThat(view7.top, isEqualAllowingError(activity.dpToPixel(230)))

        activityRule.runOnUiThread {
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.removeItemDecoration(itemDecoration)
            itemDecoration.setOrientation(HORIZONTAL)
            recyclerView.addItemDecoration(itemDecoration)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        view2 = layoutManager.getChildAt(1)
        // 90 (view width)
        assertThat(view2.left, isEqualAllowingError(activity.dpToPixel(90)))
        view3 = layoutManager.getChildAt(2)
        // 90 (view width) + 90(view width)
        assertThat(view3.left, isEqualAllowingError(activity.dpToPixel(180)))
        view4 = layoutManager.getChildAt(3)
        // 100 (view height) + 15 (divider height)
        assertThat(view4.top, isEqualAllowingError(activity.dpToPixel(115)))
        view7 = layoutManager.getChildAt(6)
        // 100 (view height) + 15 (divider height) + 100 (view height) + 15 (divider height)
        assertThat(view7.top, isEqualAllowingError(activity.dpToPixel(230)))

        activityRule.runOnUiThread {
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.removeItemDecoration(itemDecoration)
            itemDecoration.setOrientation(VERTICAL)
            recyclerView.addItemDecoration(itemDecoration)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        view2 = layoutManager.getChildAt(1)
        // 90 (view width) + 10 (divider width)
        assertThat(view2.left, isEqualAllowingError(activity.dpToPixel(100)))
        view3 = layoutManager.getChildAt(2)
        // 90 (view width) + 10 (divider width) + 90(view width) + 10 (divider width)
        assertThat(view3.left, isEqualAllowingError(activity.dpToPixel(200)))
        view4 = layoutManager.getChildAt(3)
        // 100 (view height)
        assertThat(view4.top, isEqualAllowingError(activity.dpToPixel(100)))
        view7 = layoutManager.getChildAt(6)
        // 100 (view height) + 100 (view height)
        assertThat(view7.top, isEqualAllowingError(activity.dpToPixel(200)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDecoration_direction_rowReverse() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        val drawable = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        val itemDecoration = FlexboxItemDecoration(activity)
        itemDecoration.setDrawable(drawable)

        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW_REVERSE
            recyclerView.layoutManager = layoutManager
            recyclerView.addItemDecoration(itemDecoration)
            recyclerView.adapter = adapter

            for (i in 0..9) {
                val lp = createLayoutParams(activity, 90, 100)
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW_REVERSE))
        var view1 = layoutManager.getChildAt(0)
        var view2 = layoutManager.getChildAt(1)
        // 90 (view width) + 10 (divider width)
        assertThat(view1.right - view2.right, isEqualAllowingError(activity.dpToPixel(100)))
        var view3 = layoutManager.getChildAt(2)
        // 90 (view width) + 10 (divider width) + 90(view width) + 10 (divider width)
        assertThat(view1.right - view3.right, isEqualAllowingError(activity.dpToPixel(200)))
        var view4 = layoutManager.getChildAt(3)
        // 100 (view height) + 15 (divider height)
        assertThat(view4.top, isEqualAllowingError(activity.dpToPixel(115)))
        var view7 = layoutManager.getChildAt(6)
        // 100 (view height) + 15 (divider height) + 100 (view height) + 15 (divider height)
        assertThat(view7.top, isEqualAllowingError(activity.dpToPixel(230)))

        activityRule.runOnUiThread {
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.removeItemDecoration(itemDecoration)
            itemDecoration.setOrientation(HORIZONTAL)
            recyclerView.addItemDecoration(itemDecoration)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        view1 = layoutManager.getChildAt(0)
        view2 = layoutManager.getChildAt(1)
        // 90 (view width)
        assertThat(view1.right - view2.right, isEqualAllowingError(activity.dpToPixel(90)))
        view3 = layoutManager.getChildAt(2)
        // 90 (view width) + 90(view width)
        assertThat(view1.right - view3.right, isEqualAllowingError(activity.dpToPixel(180)))
        view4 = layoutManager.getChildAt(3)
        // 100 (view height) + 15 (divider height)
        assertThat(view4.top, isEqualAllowingError(activity.dpToPixel(115)))
        view7 = layoutManager.getChildAt(6)
        // 100 (view height) + 15 (divider height) + 100 (view height) + 15 (divider height)
        assertThat(view7.top, isEqualAllowingError(activity.dpToPixel(230)))

        activityRule.runOnUiThread {
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.removeItemDecoration(itemDecoration)
            itemDecoration.setOrientation(VERTICAL)
            recyclerView.addItemDecoration(itemDecoration)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        view1 = layoutManager.getChildAt(0)
        view2 = layoutManager.getChildAt(1)
        // 90 (view width) + 10 (divider width)
        assertThat(view1.right - view2.right,
                isEqualAllowingError(activity.dpToPixel(100)))
        view3 = layoutManager.getChildAt(2)
        // 90 (view width) + 10 (divider width) + 90(view width) + 10 (divider width)
        assertThat(view1.right - view3.right,
                isEqualAllowingError(activity.dpToPixel(200)))
        view4 = layoutManager.getChildAt(3)
        // 100 (view height)
        assertThat(view4.top, isEqualAllowingError(activity.dpToPixel(100)))
        view7 = layoutManager.getChildAt(6)
        // 100 (view height) + 100 (view height)
        assertThat(view7.top, isEqualAllowingError(activity.dpToPixel(200)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDecoration_direction_column() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        val drawable = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        val itemDecoration = FlexboxItemDecoration(activity)
        itemDecoration.setDrawable(drawable)

        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.COLUMN
            recyclerView.layoutManager = layoutManager
            recyclerView.addItemDecoration(itemDecoration)
            recyclerView.adapter = adapter

            for (i in 0..9) {
                val lp = createLayoutParams(activity, 90, 65)
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN))
        var view2 = layoutManager.getChildAt(1)
        // 65 (view height) + 15 (divider height)
        assertThat(view2.top, isEqualAllowingError(activity.dpToPixel(80)))
        var view3 = layoutManager.getChildAt(2)
        // 65 (view height) + 15 (divider height) + 65 (view height) + 15 (divider height)
        assertThat(view3.top, isEqualAllowingError(activity.dpToPixel(160)))
        var view4 = layoutManager.getChildAt(3)
        // 90 (view width) + 10 (divider width)
        assertThat(view4.left, isEqualAllowingError(activity.dpToPixel(100)))
        var view7 = layoutManager.getChildAt(6)
        // 90 (view width) + 10 (divider width) + 90 (view width) + 10 (divider width)
        assertThat(view7.left, isEqualAllowingError(activity.dpToPixel(200)))

        activityRule.runOnUiThread {
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.removeItemDecoration(itemDecoration)
            itemDecoration.setOrientation(HORIZONTAL)
            recyclerView.addItemDecoration(itemDecoration)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        view2 = layoutManager.getChildAt(1)
        // 65 (view height) + 15 (divider height)
        assertThat(view2.top, isEqualAllowingError(activity.dpToPixel(80)))
        view3 = layoutManager.getChildAt(2)
        // 65 (view height) + 15 (divider height) + 65 (view height) + 15 (divider height)
        assertThat(view3.top, isEqualAllowingError(activity.dpToPixel(160)))
        view4 = layoutManager.getChildAt(3)
        // 90 (view width)
        assertThat(view4.left, isEqualAllowingError(activity.dpToPixel(90)))
        view7 = layoutManager.getChildAt(6)
        // 90 (view width) + 90 (view width)
        assertThat(view7.left, isEqualAllowingError(activity.dpToPixel(180)))

        activityRule.runOnUiThread {
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.removeItemDecoration(itemDecoration)
            itemDecoration.setOrientation(VERTICAL)
            recyclerView.addItemDecoration(itemDecoration)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        view2 = layoutManager.getChildAt(1)
        // 65 (view height)
        assertThat(view2.top, isEqualAllowingError(activity.dpToPixel(65)))
        view3 = layoutManager.getChildAt(2)
        // 65 (view height) + 65 (view height)
        assertThat(view3.top, isEqualAllowingError(activity.dpToPixel(130)))
        view4 = layoutManager.getChildAt(3)
        // 90 (view width) + 10 (divider width)
        assertThat(view4.left, isEqualAllowingError(activity.dpToPixel(100)))
        view7 = layoutManager.getChildAt(6)
        // 90 (view width) + 10 (divider width) + 90 (view width) + 10 (divider width)
        assertThat(view7.left, isEqualAllowingError(activity.dpToPixel(200)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDecoration_direction_columnReverse() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        val drawable = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        val itemDecoration = FlexboxItemDecoration(activity)
        itemDecoration.setDrawable(drawable)

        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.COLUMN_REVERSE
            recyclerView.layoutManager = layoutManager
            recyclerView.addItemDecoration(itemDecoration)
            recyclerView.adapter = adapter

            for (i in 0..9) {
                val lp = createLayoutParams(activity, 90, 65)
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN_REVERSE))
        var view1 = layoutManager.getChildAt(0)
        var view2 = layoutManager.getChildAt(1)
        // 65 (view height) + 15 (divider height)
        assertThat(view1.top - view2.top, isEqualAllowingError(activity.dpToPixel(80)))
        var view3 = layoutManager.getChildAt(2)
        // 65 (view height) + 15 (divider height) + 65 (view height) + 15 (divider height)
        assertThat(view1.top - view3.top, isEqualAllowingError(activity.dpToPixel(160)))
        var view4 = layoutManager.getChildAt(3)
        // 90 (view width) + 10 (divider width)
        assertThat(view4.left, isEqualAllowingError(activity.dpToPixel(100)))
        var view7 = layoutManager.getChildAt(6)
        // 90 (view width) + 10 (divider width) + 90 (view width) + 10 (divider width)
        assertThat(view7.left, isEqualAllowingError(activity.dpToPixel(200)))

        activityRule.runOnUiThread {
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.removeItemDecoration(itemDecoration)
            itemDecoration.setOrientation(HORIZONTAL)
            recyclerView.addItemDecoration(itemDecoration)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        view1 = layoutManager.getChildAt(0)
        view2 = layoutManager.getChildAt(1)
        // 65 (view height) + 15 (divider height)
        assertThat(view1.top - view2.top, isEqualAllowingError(activity.dpToPixel(80)))
        view3 = layoutManager.getChildAt(2)
        // 65 (view height) + 15 (divider height) + 65 (view height) + 15 (divider height)
        assertThat(view1.top - view3.top, isEqualAllowingError(activity.dpToPixel(160)))
        view4 = layoutManager.getChildAt(3)
        // 90 (view width)
        assertThat(view4.left, isEqualAllowingError(activity.dpToPixel(90)))
        view7 = layoutManager.getChildAt(6)
        // 90 (view width) + 90 (view width)
        assertThat(view7.left, isEqualAllowingError(activity.dpToPixel(180)))

        activityRule.runOnUiThread {
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.removeItemDecoration(itemDecoration)
            itemDecoration.setOrientation(VERTICAL)
            recyclerView.addItemDecoration(itemDecoration)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        view1 = layoutManager.getChildAt(0)
        view2 = layoutManager.getChildAt(1)
        // 65 (view height)
        assertThat(view1.top - view2.top, isEqualAllowingError(activity.dpToPixel(65)))
        view3 = layoutManager.getChildAt(2)
        // 65 (view height) + 65 (view height)
        assertThat(view1.top - view3.top, isEqualAllowingError(activity.dpToPixel(130)))
        view4 = layoutManager.getChildAt(3)
        // 90 (view width) + 10 (divider width)
        assertThat(view4.left, isEqualAllowingError(activity.dpToPixel(100)))
        view7 = layoutManager.getChildAt(6)
        // 90 (view width) + 10 (divider width) + 90 (view width) + 10 (divider width)
        assertThat(view7.left, isEqualAllowingError(activity.dpToPixel(200)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testScrollToPosition_direction_row() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()

        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            for (i in 0..149) {
                val lp = createLayoutParams(activity, 100, 70)
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
            // Flex line 1: 3 items
            // Flex line 2: 3 items
            // ....
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW))

        val scrollTo = 42
        activityRule.runOnUiThread { layoutManager.scrollToPosition(scrollTo) }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // Each flex line should have 3 items in this test's configuration.
        // After scrolling to the position of 42 (% 3 == 0), the first visible item should
        // be the 42'th item
        assertThat((layoutManager.getChildAt(0) as TextView).text.toString(),
                `is`((scrollTo + 1).toString()))

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        // Scroll enough that 42'th item becomes off screen to the top

        activityRule.runOnUiThread { layoutManager.scrollToPosition(scrollTo) }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // The 42'th item should be at the bottom of the screen.
        // The last visible item should be 42 + 3 since the last visible item is at the last
        // of the bottom flex line
        assertThat((layoutManager.getChildAt(layoutManager.childCount - 1) as TextView).text.toString(),
                `is`((scrollTo + 3).toString()))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testScrollToPosition_direction_column() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()

        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.COLUMN
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            for (i in 0..149) {
                val lp = createLayoutParams(activity, 100, 70)
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
            // Flex line 1: 3 items
            // Flex line 2: 3 items
            // ....
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN))

        val scrollTo = 42
        activityRule.runOnUiThread { layoutManager.scrollToPosition(scrollTo) }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // Each flex line should have 3 items in this test's configuration.
        // After scrolling to the position of 42 (% 3 == 0), the first visible item should
        // be the 42'th item
        assertThat((layoutManager.getChildAt(0) as TextView).text.toString(),
                `is`((scrollTo + 1).toString()))

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        // Scroll enough that 42'th item becomes off screen to the left

        activityRule.runOnUiThread { layoutManager.scrollToPosition(scrollTo) }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // The 42'th item should be at the bottom of the screen.
        // The last item should be the (42 + 3)'th item since it should be also the last item in the
        // bottom flex line
        assertThat((layoutManager.getChildAt(layoutManager.childCount - 1) as TextView).text.toString(),
                `is`((scrollTo + 3).toString()))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testScrollToPosition_scrollToNewItem_direction_row() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()

        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            for (i in 0..5) {
                val lp = createLayoutParams(activity, 100, 70)
                adapter.addItem(lp)
            }
            // There should be 2 lines
            // RecyclerView width: 320, height: 240.
            // Flex line 1: 3 items
            // Flex line 2: 3 items
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW))

        activityRule.runOnUiThread {
            val lp = createLayoutParams(activity, 100, 70)
            adapter.addItem(lp)
            layoutManager.scrollToPosition(adapter.itemCount - 1)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // ChildCount (visible views) should be 6 + 1,
        // which before fixing https://github.com/google/flexbox-layout/issues/206, only the new
        // item was visible
        assertThat(layoutManager.childCount, `is`(7))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testScrollToPosition_scrollToNewItem_direction_column() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()

        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.COLUMN
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            for (i in 0..5) {
                val lp = createLayoutParams(activity, 100, 70)
                adapter.addItem(lp)
            }
            // There should be 2 lines
            // RecyclerView width: 320, height: 240.
            // Flex line 1: 3 items
            // Flex line 2: 3 items
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN))

        activityRule.runOnUiThread {
            val lp = createLayoutParams(activity, 100, 70)
            adapter.addItem(lp)
            layoutManager.scrollToPosition(adapter.itemCount - 1)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // ChildCount (visible views) should be 6 + 1,
        // which before fixing https://github.com/google/flexbox-layout/issues/206, only the new
        // item was visible
        assertThat(layoutManager.childCount, `is`(7))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testScrollToStart_secondLineHasMoreItemThanFirst() {
        // This test verifies the case that the first line disappears as the user first scrolls to
        // the bottom enough that the first line becomes invisible then the user scrolls toward
        // start on the condition that the second line has more items than the first line
        // https://github.com/google/flexbox-layout/issues/228

        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()

        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val first = FlexboxLayoutManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, activity.dpToPixel(70))
            adapter.addItem(first)
            for (i in 0..49) {
                val lp = createLayoutParams(activity, 100, 70)
                adapter.addItem(lp)
            }
            // The first line has 1 item, the following lines have more than 1 items
            // RecyclerView width: 320, height: 240.
            // Flex line 1: 1 items
            // Flex line 2: 3 items
            // Flex line 3: 3 items
            // ...
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW))

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        // At this moment, the first item should become invisible
        var firstView = layoutManager.getChildAt(0)
        assertThat((firstView as TextView).text.toString(), `is`(not("1")))

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))

        // The first visible item should be "1", which before fixing the issue
        // https://github.com/google/flexbox-layout/issues/228, the first line disappeared.
        firstView = layoutManager.getChildAt(0)
        assertThat((firstView as TextView).text.toString(), `is`("1"))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testNestedRecyclerViews_direction_row() {
        // This test verifies the nested RecyclerViews.
        // The outer RecyclerView scrolls vertical using LinearLayoutManager.
        // The inner RecyclerViews use FlexboxLayoutManager with flexDirection == ROW and
        // height of the RecyclerView is set to "wrap_content", which before fixing
        // https://github.com/google/flexbox-layout/issues/208, the height of the inner
        // RecyclerViews were set to 0.
        val activity = activityRule.activity
        val outerLayoutManager = LinearLayoutManager(activity)

        // Give the inner adapter item count enough so that inner RecyclerView with
        // FlexboxLayoutManager wraps its items
        val innerAdapterItemCount = 20
        val adapter = NestedOuterAdapter(FlexDirection.ROW,
                innerAdapterItemCount, R.layout.viewholder_inner_recyclerview)
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            outerLayoutManager.orientation = LinearLayoutManager.VERTICAL
            recyclerView.layoutManager = outerLayoutManager
            recyclerView.adapter = adapter
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val viewHolder = adapter.getViewHolder(0)
        val innerRecyclerView = viewHolder.innerRecyclerView
        assertThat(innerRecyclerView.height, `is`(not(0)))

        // This assertion verifies that inner RecyclerView displays the entire items including
        // wrapped lines to verify the issue that nested RecyclerView with FlexboxLayoutManager
        // only displayed one line https://github.com/google/flexbox-layout/issues/290
        assertThat((innerRecyclerView.layoutManager as FlexboxLayoutManager).findLastVisibleItemPosition(),
                `is`(innerAdapterItemCount - 1))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testNestedRecyclerViews_direction_column() {
        // This test verifies the nested RecyclerViews.
        // The outer RecyclerView scrolls horizontally using LinearLayoutManager.
        // The inner RecyclerViews use FlexboxLayoutManager with flexDirection == COLUMN and
        // width of the RecyclerView is set to "wrap_content", which before fixing
        // https://github.com/google/flexbox-layout/issues/208, the width of the inner
        // RecyclerViews were set to 0.
        val activity = activityRule.activity
        val outerLayoutManager = LinearLayoutManager(activity)

        // Give the inner adapter item count enough so that inner RecyclerView with
        // FlexboxLayoutManager wraps its items
        val innerAdapterItemCount = 20
        val adapter = NestedOuterAdapter(FlexDirection.COLUMN,
                innerAdapterItemCount, R.layout.viewholder_inner_recyclerview_wrap_horizontally)
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            outerLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
            recyclerView.layoutManager = outerLayoutManager
            recyclerView.adapter = adapter
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val viewHolder = adapter.getViewHolder(0)
        val innerRecyclerView = viewHolder.innerRecyclerView
        assertThat(innerRecyclerView.width, `is`(not(0)))

        // This assertion verifies that inner RecyclerView displays the entire items including
        // wrapped lines to verify the issue that nested RecyclerView with FlexboxLayoutManager
        // only displayed one line https://github.com/google/flexbox-layout/issues/290
        assertThat((innerRecyclerView.layoutManager as FlexboxLayoutManager).findLastVisibleItemPosition(),
                `is`(innerAdapterItemCount - 1))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFindVisibleChild_direction_row() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter

            for (i in 0..49) {
                val lp = createLayoutParams(activity, 100, 75)
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
            // At first three completely visible lines.
            // Flex line 1, item count 3 (0, 1, 2)
            // Flex line 2, item count 3 (3, 4, 5)
            // Flex line 3, item count 3 (6, 7, 8)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.findFirstCompletelyVisibleItemPosition(), `is`(0))
        assertThat(layoutManager.findFirstVisibleItemPosition(), `is`(0))
        assertThat(layoutManager.findLastCompletelyVisibleItemPosition(), `is`(8))
        assertThat(layoutManager.findLastVisibleItemPosition(), `is`(11))

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.findFirstCompletelyVisibleItemPosition(), `is`(42))
        assertThat(layoutManager.findFirstVisibleItemPosition(), `is`(39))
        assertThat(layoutManager.findLastCompletelyVisibleItemPosition(), `is`(49))
        assertThat(layoutManager.findLastVisibleItemPosition(), `is`(49))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFindVisibleChild_direction_column() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.COLUMN
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter

            for (i in 0..49) {
                val lp = createLayoutParams(activity, 100, 75)
                adapter.addItem(lp)
            }
            // RecyclerView width: 320, height: 240.
            // At first three completely visible lines.
            // Flex line 1, item count 3 (0, 1, 2)
            // Flex line 2, item count 3 (3, 4, 5)
            // Flex line 3, item count 3 (6, 7, 8)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.findFirstCompletelyVisibleItemPosition(), `is`(0))
        assertThat(layoutManager.findFirstVisibleItemPosition(), `is`(0))
        assertThat(layoutManager.findLastCompletelyVisibleItemPosition(), `is`(8))
        assertThat(layoutManager.findLastVisibleItemPosition(), `is`(11))

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(layoutManager.findFirstCompletelyVisibleItemPosition(), `is`(42))
        assertThat(layoutManager.findFirstVisibleItemPosition(), `is`(39))
        assertThat(layoutManager.findLastCompletelyVisibleItemPosition(), `is`(49))
        assertThat(layoutManager.findLastVisibleItemPosition(), `is`(49))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDrawDirtyFlexLine_direction_row() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter

            for (i in 0..29) {
                val lp = createLayoutParams(activity, 100, 75)
                adapter.addItem(lp)
            }
        }
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // First scroll to the bottom, then add a new item that isn't visible at this moment.
        activityRule.runOnUiThread {
            val lp = createLayoutParams(activity, 40, 75)
            adapter.addItem(0, lp)
        }
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        val firstVisible = layoutManager.getChildAt(0)
        assertThat(firstVisible.width, isEqualAllowingError(activity.dpToPixel(40)))
        assertThat(firstVisible.height, isEqualAllowingError(activity.dpToPixel(75)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDrawDirtyFlexLine_direction_column() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.COLUMN
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter

            for (i in 0..29) {
                val lp = createLayoutParams(activity, 100, 75)
                adapter.addItem(lp)
            }
        }
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // First scroll to the bottom, then add a new item that isn't visible at this moment.
        activityRule.runOnUiThread {
            val lp = createLayoutParams(activity, 100, 120)
            adapter.addItem(0, lp)
        }
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        val firstVisible = layoutManager.getChildAt(0)
        assertThat(firstVisible.width, isEqualAllowingError(activity.dpToPixel(100)))
        assertThat(firstVisible.height, isEqualAllowingError(activity.dpToPixel(120)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testDrawDirtyFlexLine_multi_viewTypes_direction_row() {
        // This test verifies https://github.com/google/flexbox-layout/issues/280
        // the position of the view type is shifted if a new item is inserted before the
        // view which has the special viewType and that view isn't visible at the time the item
        // was inserted.
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapterMultiViewTypes()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val parent = activity.findViewById<RecyclerView>(R.id.recyclerview)
        var matchParentText = layoutManager
                .getChildAt(TestAdapterMultiViewTypes.POSITION_MATCH_PARENT) as TextView
        assertThat(matchParentText.width, `is`(parent.width))
        assertThat(matchParentText.text.toString(),
                `is`<String>((TestAdapterMultiViewTypes.POSITION_MATCH_PARENT + 1).toString()))

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))

        val insertedValue = 10
        activityRule.runOnUiThread {
            val item = TestAdapterMultiViewTypes.Item()
            item.value = insertedValue
            // Insert an item before the position that has a special viewType
            adapter.addItemAt(TestAdapterMultiViewTypes.POSITION_MATCH_PARENT - 1, item)
        }
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // Since a new item is inserted before the position, the index at the view who has the
        // special viewType should be shifted.
        matchParentText = layoutManager
                .getChildAt(TestAdapterMultiViewTypes.POSITION_MATCH_PARENT + 1) as TextView
        assertThat(matchParentText.width, `is`(parent.width))
        assertThat(matchParentText.text.toString(),
                `is`<String>((TestAdapterMultiViewTypes.POSITION_MATCH_PARENT + 1).toString()))

        // The position of this view is the old position who had the special viewType, but
        // now the viewType should be a normal one
        val textView = layoutManager
                .getChildAt(TestAdapterMultiViewTypes.POSITION_MATCH_PARENT - 1) as TextView
        assertThat(textView.width, lessThan(parent.width))
        assertThat(textView.text.toString(), `is`(insertedValue.toString()))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testChildrenSizeWithMargin() {
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp = createLayoutParams(activity, 100, 30)
            lp.setMargins(10, 30, 20, 40)
            adapter.addItem(lp)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val text = layoutManager.getChildAt(0) as TextView
        val lp = text.layoutParams as FlexboxLayoutManager.LayoutParams
        assertThat(text.height, isEqualAllowingError(activity.dpToPixel(30)))
        assertThat(text.width, isEqualAllowingError(activity.dpToPixel(100)))
        assertThat(lp.marginLeft, isEqualAllowingError(10))
        assertThat(lp.marginRight, isEqualAllowingError(20))
        assertThat(lp.marginTop, isEqualAllowingError(30))
        assertThat(lp.marginBottom, isEqualAllowingError(40))
        activityRule.runOnUiThread { layoutManager.flexDirection = FlexDirection.COLUMN }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val text2 = layoutManager.getChildAt(0) as TextView
        assertThat(text2.height, isEqualAllowingError(activity.dpToPixel(30)))
        assertThat(text2.width, isEqualAllowingError(activity.dpToPixel(100)))

    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testItemDecoration_withScrolling_direction_row() {
        // This test verifies the case that the item decoration set through FlexboxItemDecoration
        // is misplaced after the user scrolls the RecyclerView
        // https://github.com/google/flexbox-layout/issues/285

        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        val decorationDrawable = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val decoration = FlexboxItemDecoration(activity)
            decoration.setDrawable(decorationDrawable)
            recyclerView.addItemDecoration(decoration)
            for (i in 0..49) {
                val lp = createLayoutParams(activity, 100, 70)
                adapter.addItem(lp)
            }
            // The first line has 1 item, the following lines have more than 1 items
            // RecyclerView width: 320, height: 240.
            // Flex line 1: 1 items
            // Flex line 2: 3 items
            // Flex line 3: 3 items
            // ...
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW))
        assertThat(layoutManager.getTopDecorationHeight(layoutManager.getChildAt(0)), `is`(0))
        assertThat(layoutManager.getTopDecorationHeight(layoutManager.getChildAt(1)), `is`(0))
        assertThat(layoutManager.getTopDecorationHeight(layoutManager.getChildAt(2)), `is`(0))
        layoutManager.flexLines
                .mapNotNull { layoutManager.getChildAt(it.firstIndex) }
                .forEach { assertThat(layoutManager.getLeftDecorationWidth(it), `is`(0)) }

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))

        // Verify even after the scrolling, decoration values are set correctly
        assertThat(layoutManager.getTopDecorationHeight(layoutManager.getChildAt(0)), `is`(0))
        assertThat(layoutManager.getTopDecorationHeight(layoutManager.getChildAt(1)), `is`(0))
        assertThat(layoutManager.getTopDecorationHeight(layoutManager.getChildAt(2)), `is`(0))
        layoutManager.flexLines
                .mapNotNull { layoutManager.getChildAt(it.firstIndex) }
                .forEach { assertThat(layoutManager.getLeftDecorationWidth(it), `is`(0)) }
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testItemDecoration_withScrolling_direction_column() {
        // This test verifies the case that the item decoration set through FlexboxItemDecoration
        // is misplaced after the user scrolls the RecyclerView
        // https://github.com/google/flexbox-layout/issues/285

        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        val decorationDrawable = ResourcesCompat.getDrawable(activity.resources, R.drawable.divider, null)
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.COLUMN
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val decoration = FlexboxItemDecoration(activity)
            decoration.setDrawable(decorationDrawable)
            recyclerView.addItemDecoration(decoration)
            for (i in 0..49) {
                val lp = createLayoutParams(activity, 100, 70)
                adapter.addItem(lp)
            }
            // The first line has 1 item, the following lines have more than 1 items
            // RecyclerView width: 320, height: 240.
            // Flex line 1: 1 items
            // Flex line 2: 3 items
            // Flex line 3: 3 items
            // ...
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN))
        assertThat(layoutManager.getLeftDecorationWidth(layoutManager.getChildAt(0)), `is`(0))
        assertThat(layoutManager.getLeftDecorationWidth(layoutManager.getChildAt(1)), `is`(0))
        assertThat(layoutManager.getLeftDecorationWidth(layoutManager.getChildAt(2)), `is`(0))
        layoutManager.flexLines
                .mapNotNull { layoutManager.getChildAt(it.firstIndex) }
                .forEach { assertThat(layoutManager.getTopDecorationHeight(it), `is`(0)) }

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))

        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))

        // Verify even after the scrolling, decoration values are set correctly
        assertThat(layoutManager.getLeftDecorationWidth(layoutManager.getChildAt(0)), `is`(0))
        assertThat(layoutManager.getLeftDecorationWidth(layoutManager.getChildAt(1)), `is`(0))
        assertThat(layoutManager.getLeftDecorationWidth(layoutManager.getChildAt(2)), `is`(0))
        layoutManager.flexLines
                .mapNotNull { layoutManager.getChildAt(it.firstIndex) }
                .forEach { assertThat(layoutManager.getTopDecorationHeight(it), `is`(0)) }
    }


    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testNotifyItemChange_withPayload() {
        // This test verifies the payload is correctly passed to the Adapter in the case
        // that notifying an item with payload
        // https://github.com/google/flexbox-layout/issues/297

        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.COLUMN
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            val lp = createLayoutParams(activity, 100, 70)
            adapter.addItem(lp)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(adapter.payloads.size, `is`(0))

        val payload = "payload"
        activityRule.runOnUiThread { adapter.changeItemWithPayload(0, payload) }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertThat(adapter.payloads.size, `is`(1))
        assertThat(adapter.payloads[0] as String, `is`(payload))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testScrollAlongManAxis_direction_row() {
        // This test verifies the scroll along the main axis if the width of the RecyclerView is
        // larger than its parent when the main axis direction is horizontal (row)
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.wrapped_recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.ROW
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            for (i in 0..49) {
                val lp = createLayoutParams(activity, 100, 70)
                adapter.addItem(lp)
            }
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(layoutManager.getChildAt(0).left, `is`(0))
        onView(withId(R.id.container)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.container)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.container)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        assertThat(layoutManager.getChildAt(0).left, `is`(not(0)))

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        onView(withId(R.id.container)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.container)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.container)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        onView(withId(R.id.container)).perform(swipe(GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT))
        assertThat(layoutManager.getChildAt(0).left, `is`(0))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testScrollAlongManAxis_direction_column() {
        // This test verifies the scroll along the main axis if the height of the RecyclerView is
        // larger than its parent when the main axis direction is vertical (column).
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()
        activityRule.runOnUiThread {
            activity.setContentView(R.layout.wrapped_recyclerview_scroll_vertical)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            layoutManager.flexDirection = FlexDirection.COLUMN
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            for (i in 0..49) {
                val lp = createLayoutParams(activity, 70, 100)
                adapter.addItem(lp)
            }
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(layoutManager.getChildAt(0).top, `is`(0))
        onView(withId(R.id.container)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.container)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.container)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        assertThat(layoutManager.getChildAt(0).top, `is`(not(0)))

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        onView(withId(R.id.container)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.container)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.container)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        onView(withId(R.id.container)).perform(swipe(GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER))
        assertThat(layoutManager.getChildAt(0).top, `is`(0))
    }

    /**
     * Creates a new flex item.
     *
     * @param context the context
     * @param width   in DP
     * @param height  in DP
     * @return the created [FlexboxLayoutManager.LayoutParams] instance
     */
    private fun createLayoutParams(context: Context, width: Int,
                                   height: Int): FlexboxLayoutManager.LayoutParams {
        return FlexboxLayoutManager.LayoutParams(context.dpToPixel(width), context.dpToPixel(height))
    }

    private fun swipe(from: CoordinatesProvider, to: CoordinatesProvider): ViewAction {
        return GeneralSwipeAction(Swipe.FAST, from, to, Press.FINGER)
    }
}
