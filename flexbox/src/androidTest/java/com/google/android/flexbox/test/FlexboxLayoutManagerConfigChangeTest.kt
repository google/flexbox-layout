/*
 * Copyright 2017 Google Inc. All rights reserved.
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
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.FlakyTest
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.IsNot.not
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for [FlexboxLayoutManager] with the Activity that handles configuration
 * changes manually.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class FlexboxLayoutManagerConfigChangeTest {

    @JvmField
    @Rule
    var activityRule = ActivityTestRule(ConfigChangeActivity::class.java)

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexLinesDiscardedOnOrientationChange_direction_row() {
        // Verifies the case that the calculated Flex lines are correctly discarded when a
        // orientation happens with an Activity that handles configuration changes manually
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()

        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            // This test assumes that the screen width and the height are different.
            recyclerView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            recyclerView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutManager.flexDirection = FlexDirection.ROW
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter

            for (i in 0..99) {
                val lp = createLayoutParams(activity, 90, 65)
                adapter.addItem(lp)
            }
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        val firstLine = layoutManager.flexLines[0]
        assertThat(layoutManager.flexDirection, `is`(FlexDirection.ROW))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER))

        activityRule.runOnUiThread {
            val orientation = activity.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        activityRule.runOnUiThread { layoutManager.scrollToPosition(0) }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val firstLineAfterRotation = layoutManager.flexLines[0]
        assertThat(firstLine.mainSize, `is`(not(firstLineAfterRotation.mainSize)))
    }

    @Test
    @FlakyTest
    @Throws(Throwable::class)
    fun testFlexLinesDiscardedOnOrientationChange_direction_column() {
        // Verifies the case that the calculated Flex lines are correctly discarded when a
        // orientation happens with an Activity that handles configuration changes manually
        val activity = activityRule.activity
        val layoutManager = FlexboxLayoutManager(activity)
        val adapter = TestAdapter()

        activityRule.runOnUiThread {
            activity.setContentView(R.layout.recyclerview)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerview)
            // This test assumes that the screen width and the height are different.
            recyclerView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            recyclerView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutManager.flexDirection = FlexDirection.COLUMN
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter

            for (i in 0..99) {
                val lp = createLayoutParams(activity, 90, 65)
                adapter.addItem(lp)
            }
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        val firstLine = layoutManager.flexLines[0]
        assertThat(layoutManager.flexDirection, `is`(FlexDirection.COLUMN))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))
        onView(withId(R.id.recyclerview)).perform(swipe(GeneralLocation.CENTER_RIGHT,
                GeneralLocation.CENTER_LEFT))

        activityRule.runOnUiThread {
            val orientation = activity.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        activityRule.runOnUiThread { layoutManager.scrollToPosition(0) }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val firstLineAfterRotation = layoutManager.flexLines[0]
        assertThat(firstLine.mainSize, `is`(not(firstLineAfterRotation.mainSize)))
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
