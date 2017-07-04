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

package com.google.android.flexbox

import android.content.SharedPreferences
import android.support.design.widget.NavigationView
import android.support.v4.view.MenuItemCompat
import android.support.v7.preference.PreferenceManager
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.google.android.apps.flexbox.R

/**
 * Helper class that has the common logic for initializing the Fragment for the play ground demo
 * such as [FlexboxLayoutFragment] and a Fragment that uses RecyclerView in it.
 */
internal class FragmentHelper(private val activity: MainActivity, private val flexContainer: FlexContainer) {

    private lateinit var ROW: String

    private lateinit var COLUMN: String

    private lateinit var ROW_REVERSE: String

    private lateinit var COLUMN_REVERSE: String

    private lateinit var NOWRAP: String

    private lateinit var WRAP: String

    private lateinit var WRAP_REVERSE: String

    private lateinit var FLEX_START: String

    private lateinit var FLEX_END: String

    private lateinit var CENTER: String

    private lateinit var BASELINE: String

    private lateinit var STRETCH: String

    private lateinit var SPACE_BETWEEN: String

    private lateinit var SPACE_AROUND: String

    private lateinit var sharedPreferences: SharedPreferences

    fun initializeViews() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        initializeStringResources()
        val navigationView = activity.findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(activity)
        val navigationMenu = navigationView.menu
        initializeFlexDirectionSpinner(navigationMenu)
        initializeFlexWrapSpinner(navigationMenu)
        initializeJustifyContentSpinner(navigationMenu)
        initializeAlignItemsSpinner(navigationMenu)
        initializeAlignContentSpinner(navigationMenu)
    }

    private fun initializeStringResources() {
        ROW = activity.getString(R.string.row)
        COLUMN = activity.getString(R.string.column)
        ROW_REVERSE = activity.getString(R.string.row_reverse)
        COLUMN_REVERSE = activity.getString(R.string.column_reverse)
        NOWRAP = activity.getString(R.string.nowrap)
        WRAP = activity.getString(R.string.wrap)
        WRAP_REVERSE = activity.getString(R.string.wrap_reverse)
        FLEX_START = activity.getString(R.string.flex_start)
        FLEX_END = activity.getString(R.string.flex_end)
        CENTER = activity.getString(R.string.center)
        BASELINE = activity.getString(R.string.baseline)
        STRETCH = activity.getString(R.string.stretch)
        SPACE_BETWEEN = activity.getString(R.string.space_between)
        SPACE_AROUND = activity.getString(R.string.space_around)
    }

    /**
     * Sets the attributes for a [FlexItem] based on the stored default values in
     * the SharedPreferences.

     * @param flexItem the FlexItem instance
     * *
     * @return a FlexItem instance, which attributes from the SharedPreferences are updated
     */
    fun setFlexItemAttributes(flexItem: FlexItem): FlexItem {
        flexItem.width = Util.dpToPixel(activity,
                readPreferenceAsInteger(activity.getString(R.string.new_width_key),
                        DEFAULT_WIDTH))
        flexItem.height = Util.dpToPixel(activity,
                readPreferenceAsInteger(activity.getString(R.string.new_height_key),
                        DEFAULT_HEIGHT))
        // Order is not supported in the FlexboxLayoutManager
        if (flexItem !is FlexboxLayoutManager.LayoutParams) {
            flexItem.order = readPreferenceAsInteger(activity.getString(R.string.new_flex_item_order_key),
                    "1")
        }
        flexItem.flexGrow = readPreferenceAsFloat(activity.getString(R.string.new_flex_grow_key), "0.0")
        flexItem.flexShrink = readPreferenceAsFloat(activity.getString(R.string.new_flex_shrink_key), "1.0")
        val flexBasisPercent = readPreferenceAsInteger(
                activity.getString(R.string.new_flex_basis_percent_key), "-1")
        flexItem.flexBasisPercent = if (flexBasisPercent == -1) -1f else (flexBasisPercent / 100.0).toFloat()
        return flexItem
    }

    private fun readPreferenceAsInteger(key: String, defValue: String): Int {
        if (sharedPreferences.contains(key)) {
            return Integer.valueOf(sharedPreferences.getString(key, defValue))!!
        } else {
            return Integer.valueOf(defValue)!!
        }
    }

    private fun readPreferenceAsFloat(key: String, defValue: String): Float {
        if (sharedPreferences.contains(key)) {
            return java.lang.Float.valueOf(sharedPreferences.getString(key, defValue))
        } else {
            return java.lang.Float.valueOf(defValue)
        }
    }

    private fun initializeSpinner(currentValue: Int, menuItemId: Int, navigationMenu: Menu,
                                  arrayResourceId: Int, listener: AdapterView.OnItemSelectedListener,
                                  converter: ValueToStringConverter) {
        val spinner = MenuItemCompat
                .getActionView(navigationMenu.findItem(menuItemId)) as Spinner
        val adapter = ArrayAdapter.createFromResource(activity,
                arrayResourceId, R.layout.spinner_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = listener
        val selectedAsString = converter.asString(currentValue)
        val position = adapter.getPosition(selectedAsString)
        spinner.setSelection(position)
    }

    private fun initializeFlexDirectionSpinner(navigationMenu: Menu) {
        initializeSpinner(flexContainer.flexDirection, R.id.menu_item_flex_direction,
                navigationMenu, R.array.array_flex_direction,
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, ignored: View?, position: Int,
                                                id: Long) {
                        val selected = parent.getItemAtPosition(position).toString()
                        when (selected) {
                            ROW -> flexContainer.flexDirection = FlexDirection.ROW
                            ROW_REVERSE -> flexContainer.flexDirection = FlexDirection.ROW_REVERSE
                            COLUMN -> flexContainer.flexDirection = FlexDirection.COLUMN
                            COLUMN_REVERSE -> flexContainer.flexDirection = FlexDirection.COLUMN_REVERSE
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // No op
                    }
                }, object : ValueToStringConverter {
            override fun asString(value: Int): String {
                when (value) {
                    FlexDirection.ROW -> return ROW
                    FlexDirection.ROW_REVERSE -> return ROW_REVERSE
                    FlexDirection.COLUMN -> return COLUMN
                    FlexDirection.COLUMN_REVERSE -> return COLUMN_REVERSE
                    else -> return ROW
                }
            }
        })
    }

    private fun initializeFlexWrapSpinner(navigationMenu: Menu) {
        initializeSpinner(flexContainer.flexWrap, R.id.menu_item_flex_wrap,
                navigationMenu, R.array.array_flex_wrap,
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, ignored: View?, position: Int,
                                                id: Long) {
                        val selected = parent.getItemAtPosition(position).toString()
                        when (selected) {
                            NOWRAP -> flexContainer.flexWrap = FlexWrap.NOWRAP
                            WRAP -> flexContainer.flexWrap = FlexWrap.WRAP
                            WRAP_REVERSE -> if (flexContainer is FlexboxLayoutManager) {
                                Toast.makeText(activity,
                                        R.string.wrap_reverse_not_supported,
                                        Toast.LENGTH_SHORT).show()
                            } else {
                                flexContainer.flexWrap = FlexWrap.WRAP_REVERSE
                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // No op
                    }
                }, object : ValueToStringConverter {
            override fun asString(value: Int): String {
                when (value) {
                    FlexWrap.NOWRAP -> return NOWRAP
                    FlexWrap.WRAP -> return WRAP
                    FlexWrap.WRAP_REVERSE -> return WRAP_REVERSE
                    else -> return NOWRAP
                }
            }
        })
    }

    private fun initializeJustifyContentSpinner(navigationMenu: Menu) {
        initializeSpinner(flexContainer.justifyContent, R.id.menu_item_justify_content,
                navigationMenu, R.array.array_justify_content,
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, ignored: View?, position: Int,
                                                id: Long) {
                        val selected = parent.getItemAtPosition(position).toString()
                        when (selected) {
                            FLEX_START -> flexContainer.justifyContent = JustifyContent.FLEX_START
                            FLEX_END -> flexContainer.justifyContent = JustifyContent.FLEX_END
                            CENTER -> flexContainer.justifyContent = JustifyContent.CENTER
                            SPACE_BETWEEN -> flexContainer.justifyContent = JustifyContent.SPACE_BETWEEN
                            SPACE_AROUND -> flexContainer.justifyContent = JustifyContent.SPACE_AROUND
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // No op
                    }
                }, object : ValueToStringConverter {
            override fun asString(value: Int): String {
                when (value) {
                    JustifyContent.FLEX_START -> return FLEX_START
                    JustifyContent.FLEX_END -> return FLEX_END
                    JustifyContent.CENTER -> return CENTER
                    JustifyContent.SPACE_AROUND -> return SPACE_AROUND
                    JustifyContent.SPACE_BETWEEN -> return SPACE_BETWEEN
                    else -> return FLEX_START
                }
            }
        })
    }

    private fun initializeAlignItemsSpinner(navigationMenu: Menu) {
        initializeSpinner(flexContainer.alignItems, R.id.menu_item_align_items,
                navigationMenu, R.array.array_align_items,
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, ignored: View?, position: Int,
                                                id: Long) {
                        val selected = parent.getItemAtPosition(position).toString()
                        when (selected) {
                            FLEX_START -> flexContainer.alignItems = AlignItems.FLEX_START
                            FLEX_END -> flexContainer.alignItems = AlignItems.FLEX_END
                            CENTER -> flexContainer.alignItems = AlignItems.CENTER
                            BASELINE -> flexContainer.alignItems = AlignItems.BASELINE
                            STRETCH -> flexContainer.alignItems = AlignItems.STRETCH
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // No op
                    }
                }, object : ValueToStringConverter {
            override fun asString(value: Int): String {
                when (value) {
                    AlignItems.FLEX_START -> return FLEX_START
                    AlignItems.FLEX_END -> return FLEX_END
                    AlignItems.CENTER -> return CENTER
                    AlignItems.BASELINE -> return BASELINE
                    AlignItems.STRETCH -> return STRETCH
                    else -> return STRETCH
                }
            }
        })
    }

    private fun initializeAlignContentSpinner(navigationMenu: Menu) {
        initializeSpinner(flexContainer.alignContent, R.id.menu_item_align_content,
                navigationMenu, R.array.array_align_content,
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, ignored: View?, position: Int,
                                                id: Long) {
                        if (flexContainer is FlexboxLayoutManager) {
                            Toast.makeText(activity, R.string.align_content_not_supported,
                                    Toast.LENGTH_SHORT).show()
                            return
                        }
                        val selected = parent.getItemAtPosition(position).toString()
                        when (selected) {
                            FLEX_START -> flexContainer.alignContent = AlignContent.FLEX_START
                            FLEX_END -> flexContainer.alignContent = AlignContent.FLEX_END
                            CENTER -> flexContainer.alignContent = AlignContent.CENTER
                            SPACE_BETWEEN -> flexContainer.alignContent = AlignContent.SPACE_BETWEEN
                            SPACE_AROUND -> flexContainer.alignContent = AlignContent.SPACE_AROUND
                            STRETCH -> flexContainer.alignContent = AlignContent.STRETCH
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // No op
                    }
                }, object : ValueToStringConverter {
            override fun asString(value: Int): String {
                when (value) {
                    AlignContent.FLEX_START -> return FLEX_START
                    AlignContent.FLEX_END -> return FLEX_END
                    AlignContent.CENTER -> return CENTER
                    AlignContent.SPACE_BETWEEN -> return SPACE_BETWEEN
                    AlignContent.SPACE_AROUND -> return SPACE_AROUND
                    AlignContent.STRETCH -> return STRETCH
                    else -> return STRETCH
                }
            }
        })
    }

    /**
     * Converter for converting an int value for Flexbox properties to a String.
     */
    private interface ValueToStringConverter {

        fun asString(value: Int): String
    }

    companion object {

        private val DEFAULT_WIDTH = "120"

        private val DEFAULT_HEIGHT = "80"
    }
}
