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

package com.google.android.apps.flexbox.test

import android.content.pm.ActivityInfo
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.FlakyTest
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.google.android.apps.flexbox.R
import com.google.android.flexbox.*
import com.google.android.material.navigation.NavigationView
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for [MainActivity].
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class MainActivityTest {

    @JvmField
    @Rule
    var activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    @FlakyTest
    fun testAddFlexItem() {
        val activity = activityRule.activity
        val flexboxLayout = activity.findViewById<FlexboxLayout>(R.id.flexbox_layout)
        assertNotNull(flexboxLayout)
        val beforeCount = flexboxLayout.childCount
        onView(withId(R.id.add_fab)).perform(click())

        assertThat(flexboxLayout.childCount, `is`(beforeCount + 1))
    }

    @Test
    @FlakyTest
    fun testRemoveFlexItem() {
        val activity = activityRule.activity
        val flexboxLayout = activity.findViewById<FlexboxLayout>(R.id.flexbox_layout)
        assertNotNull(flexboxLayout)
        val beforeCount = flexboxLayout.childCount
        onView(withId(R.id.remove_fab)).perform(click())

        assertThat(flexboxLayout.childCount, `is`(beforeCount - 1))
    }

    @Test
    @FlakyTest
    fun testConfigurationChange() {
        val activity = activityRule.activity
        val flexboxLayout = activity.findViewById<FlexboxLayout>(R.id.flexbox_layout)
        assertNotNull(flexboxLayout)
        onView(withId(R.id.add_fab)).perform(click())
        onView(withId(R.id.add_fab)).perform(click())
        onView(withId(R.id.add_fab)).perform(click())
        val beforeCount = flexboxLayout.childCount

        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // Verify the flex items are restored across the configuration change.
        assertThat(flexboxLayout.childCount, `is`(beforeCount))
    }

    @Test
    @FlakyTest
    fun testFlexDirectionSpinner() {
        val activity = activityRule.activity
        val flexboxLayout = activity.findViewById<FlexboxLayout>(R.id.flexbox_layout)
        assertNotNull(flexboxLayout)
        val navigationView = activity.findViewById<NavigationView>(R.id.nav_view)
        assertNotNull(navigationView)
        val menu = navigationView.menu
        val spinner = menu.findItem(R.id.menu_item_flex_direction).actionView as Spinner
        val spinnerAdapter = spinner.adapter as ArrayAdapter<CharSequence>

        val columnPosition = spinnerAdapter.getPosition(activity.getString(R.string.column))
        activity.runOnUiThread { spinner.setSelection(columnPosition) }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.COLUMN))

        val rowReversePosition = spinnerAdapter.getPosition(activity.getString(R.string.row_reverse))
        activity.runOnUiThread { spinner.setSelection(rowReversePosition) }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(flexboxLayout.flexDirection, `is`(FlexDirection.ROW_REVERSE))
    }

    @Test
    @FlakyTest
    fun testFlexWrapSpinner() {
        val activity = activityRule.activity
        val flexboxLayout = activity.findViewById<FlexboxLayout>(R.id.flexbox_layout)
        assertNotNull(flexboxLayout)
        val navigationView = activity.findViewById<NavigationView>(R.id.nav_view)
        assertNotNull(navigationView)
        val menu = navigationView.menu
        val spinner = menu.findItem(R.id.menu_item_flex_wrap).actionView as Spinner
        val spinnerAdapter = spinner.adapter as ArrayAdapter<CharSequence>

        val wrapReversePosition = spinnerAdapter.getPosition(activity.getString(R.string.wrap_reverse))
        activity.runOnUiThread { spinner.setSelection(wrapReversePosition) }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.WRAP_REVERSE))

        val noWrapPosition = spinnerAdapter.getPosition(activity.getString(R.string.nowrap))
        activity.runOnUiThread { spinner.setSelection(noWrapPosition) }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(flexboxLayout.flexWrap, `is`(FlexWrap.NOWRAP))
    }

    @Test
    @FlakyTest
    fun testJustifyContentSpinner() {
        val activity = activityRule.activity
        val flexboxLayout = activity.findViewById<View>(R.id.flexbox_layout) as FlexboxLayout
        assertNotNull(flexboxLayout)
        val navigationView = activity.findViewById<View>(R.id.nav_view) as NavigationView
        assertNotNull(navigationView)
        val menu = navigationView.menu
        val spinner = menu.findItem(R.id.menu_item_justify_content).actionView as Spinner
        val spinnerAdapter = spinner.adapter as ArrayAdapter<CharSequence>

        val spaceBetweenPosition = spinnerAdapter.getPosition(activity.getString(R.string.space_between))
        activity.runOnUiThread { spinner.setSelection(spaceBetweenPosition) }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.SPACE_BETWEEN))

        val centerPosition = spinnerAdapter.getPosition(activity.getString(R.string.center))
        activity.runOnUiThread { spinner.setSelection(centerPosition) }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(flexboxLayout.justifyContent, `is`(JustifyContent.CENTER))
    }

    @Test
    @FlakyTest
    fun testAlignItemsSpinner() {
        val activity = activityRule.activity
        val flexboxLayout = activity.findViewById<FlexboxLayout>(R.id.flexbox_layout)
        assertNotNull(flexboxLayout)
        val navigationView = activity.findViewById<NavigationView>(R.id.nav_view)
        assertNotNull(navigationView)
        val menu = navigationView.menu
        val spinner = menu.findItem(R.id.menu_item_align_items).actionView as Spinner
        val spinnerAdapter = spinner.adapter as ArrayAdapter<CharSequence>

        val baselinePosition = spinnerAdapter.getPosition(activity.getString(R.string.baseline))
        activity.runOnUiThread { spinner.setSelection(baselinePosition) }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(flexboxLayout.alignItems, `is`(AlignItems.BASELINE))

        val flexEndPosition = spinnerAdapter.getPosition(activity.getString(R.string.flex_end))
        activity.runOnUiThread { spinner.setSelection(flexEndPosition) }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(flexboxLayout.alignItems, `is`(AlignItems.FLEX_END))
    }

    @Test
    @FlakyTest
    fun testAlignContentSpinner() {
        val activity = activityRule.activity
        val flexboxLayout = activity.findViewById<FlexboxLayout>(R.id.flexbox_layout)
        assertNotNull(flexboxLayout)
        val navigationView = activity.findViewById<NavigationView>(R.id.nav_view)
        assertNotNull(navigationView)
        val menu = navigationView.menu
        val spinner = menu.findItem(R.id.menu_item_align_content).actionView as Spinner
        val spinnerAdapter = spinner.adapter as ArrayAdapter<CharSequence>

        val spaceAroundPosition = spinnerAdapter.getPosition(activity.getString(R.string.space_around))
        activity.runOnUiThread { spinner.setSelection(spaceAroundPosition) }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(flexboxLayout.alignContent, `is`(AlignContent.SPACE_AROUND))

        val stretchPosition = spinnerAdapter.getPosition(activity.getString(R.string.stretch))
        activity.runOnUiThread { spinner.setSelection(stretchPosition) }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(flexboxLayout.alignContent, `is`(AlignContent.STRETCH))
    }

    @Test
    @FlakyTest
    fun testEditFragment_changeOrder() {
        val activity = activityRule.activity
        val flexboxLayout = activity.findViewById<View>(R.id.flexbox_layout) as FlexboxLayout
        assertNotNull(flexboxLayout)
        onView(withId(R.id.textview1)).perform(click())
        onView(withId(R.id.edit_text_order)).perform(replaceText("3"), closeSoftKeyboard())
        onView(withId(R.id.button_ok)).perform(click())
        val first = flexboxLayout.getReorderedChildAt(0) as TextView
        val second = flexboxLayout.getReorderedChildAt(1) as TextView
        val third = flexboxLayout.getReorderedChildAt(2) as TextView

        assertThat(first.text.toString(), `is`("2"))
        assertThat(second.text.toString(), `is`("3"))
        assertThat(third.text.toString(), `is`("1"))
    }

    @Test
    @FlakyTest
    fun testEditFragment_changeFlexGrow() {
        val activity = activityRule.activity
        val flexboxLayout = activity.findViewById<View>(R.id.flexbox_layout) as FlexboxLayout
        assertNotNull(flexboxLayout)
        onView(withId(R.id.textview1)).perform(click())
        onView(withId(R.id.edit_text_flex_grow)).perform(replaceText("1"), closeSoftKeyboard())
        onView(withId(R.id.button_ok)).perform(click())
        val first = activity.findViewById<View>(R.id.textview1) as TextView
        val second = activity.findViewById<View>(R.id.textview2) as TextView
        val third = activity.findViewById<View>(R.id.textview3) as TextView
        assertNotNull(first)
        assertNotNull(second)
        assertNotNull(third)

        assertThat(first.width, `is`(flexboxLayout.width - second.width - third.width))
    }

    @Test
    @FlakyTest
    fun testEditFragment_changeFlexGrowFloat() {
        val activity = activityRule.activity
        val flexboxLayout = activity.findViewById<View>(R.id.flexbox_layout) as FlexboxLayout
        assertNotNull(flexboxLayout)
        onView(withId(R.id.textview1)).perform(click())
        onView(withId(R.id.edit_text_flex_grow)).perform(replaceText("1.0"), closeSoftKeyboard())
        onView(withId(R.id.button_ok)).perform(click())
        val first = activity.findViewById<View>(R.id.textview1) as TextView
        val second = activity.findViewById<View>(R.id.textview2) as TextView
        val third = activity.findViewById<View>(R.id.textview3) as TextView
        assertNotNull(first)
        assertNotNull(second)
        assertNotNull(third)

        assertThat(first.width, `is`(flexboxLayout.width - second.width - third.width))
    }

    @Test
    @FlakyTest
    fun testEditFragment_changeFlexBasisPercent() {
        val activity = activityRule.activity
        val flexboxLayout = activity.findViewById<View>(R.id.flexbox_layout) as FlexboxLayout
        assertNotNull(flexboxLayout)
        onView(withId(R.id.textview1)).perform(click())
        onView(withId(R.id.edit_text_flex_basis_percent))
                .perform(replaceText("50"), closeSoftKeyboard())
        onView(withId(R.id.button_ok)).perform(click())
        val first = activity.findViewById<TextView>(R.id.textview1)
        val second = activity.findViewById<TextView>(R.id.textview2)
        val third = activity.findViewById<TextView>(R.id.textview3)
        assertNotNull(first)
        assertNotNull(second)
        assertNotNull(third)

        assertTrue(first.width - 1 <= flexboxLayout.width / 2 || flexboxLayout.width / 2 <= first.width + 1)
    }

    @Test
    @FlakyTest
    fun testSwitchRecyclerViewFragment() {
        val activity = activityRule.activity
        val flexboxLayout = activity.findViewById<FlexboxLayout>(R.id.flexbox_layout)
        assertNotNull(flexboxLayout)
        val navigationView = activity.findViewById<NavigationView>(R.id.nav_view)
        assertNotNull(navigationView)
        assertNull(activity.findViewById(R.id.recyclerview))
        assertNotNull(activity.findViewById(R.id.flexbox_layout))

        val radioGroup = navigationView.getHeaderView(0)
                .findViewById<RadioGroup>(R.id.radiogroup_container_implementation)
        activity.runOnUiThread { radioGroup.check(R.id.radiobutton_recyclerview) }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertNotNull(activity.findViewById(R.id.recyclerview))
        assertNull(activity.findViewById(R.id.flexbox_layout))
    }
}
