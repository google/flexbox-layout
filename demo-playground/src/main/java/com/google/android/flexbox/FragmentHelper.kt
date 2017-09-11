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
        val navigationView: NavigationView = activity.findViewById(R.id.nav_view)
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
        flexItem.width = activity.dpToPixel(readPreferenceAsInteger(activity.getString(R.string.new_width_key), DEFAULT_WIDTH))
        flexItem.height = activity.dpToPixel(readPreferenceAsInteger(activity.getString(R.string.new_height_key), DEFAULT_HEIGHT))
        // Order is not supported in the FlexboxLayoutManager
        if (flexItem !is FlexboxLayoutManager.LayoutParams) {
            flexItem.order = readPreferenceAsInteger(activity.getString(R.string.new_flex_item_order_key), "1")
        }
        flexItem.flexGrow = readPreferenceAsFloat(activity.getString(R.string.new_flex_grow_key), "0.0")
        flexItem.flexShrink = readPreferenceAsFloat(activity.getString(R.string.new_flex_shrink_key), "1.0")
        val flexBasisPercent = readPreferenceAsInteger(
                activity.getString(R.string.new_flex_basis_percent_key), "-1")
        flexItem.flexBasisPercent = if (flexBasisPercent == -1) -1f else (flexBasisPercent / 100.0).toFloat()
        return flexItem
    }

    private fun readPreferenceAsInteger(key: String, defValue: String): Int {
        return if (sharedPreferences.contains(key)) {
            sharedPreferences.getString(key, defValue).toIntOrNull() ?: defValue.toInt()
        } else {
            defValue.toInt()
        }
    }

    private fun readPreferenceAsFloat(key: String, defValue: String): Float {
        return if (sharedPreferences.contains(key)) {
            sharedPreferences.getString(key, defValue).toFloatOrNull() ?: defValue.toFloat()
        } else {
            defValue.toFloat()
        }
    }

    private fun initializeSpinner(currentValue: Int, menuItemId: Int, navigationMenu: Menu,
                                  arrayResourceId: Int, listener: AdapterView.OnItemSelectedListener,
                                  converter: ValueToStringConverter) {
        val spinner = navigationMenu.findItem(menuItemId).actionView as Spinner
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
                        flexContainer.flexDirection = when (parent.getItemAtPosition(position).toString()) {
                            ROW -> FlexDirection.ROW
                            ROW_REVERSE -> FlexDirection.ROW_REVERSE
                            COLUMN -> FlexDirection.COLUMN
                            COLUMN_REVERSE -> FlexDirection.COLUMN_REVERSE
                            else -> return
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // No op
                    }
                }, object : ValueToStringConverter {
            override fun asString(value: Int): String {
                return when (value) {
                    FlexDirection.ROW -> ROW
                    FlexDirection.ROW_REVERSE -> ROW_REVERSE
                    FlexDirection.COLUMN -> COLUMN
                    FlexDirection.COLUMN_REVERSE -> COLUMN_REVERSE
                    else -> ROW
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
                        flexContainer.flexWrap = when (parent.getItemAtPosition(position).toString()) {
                            NOWRAP -> FlexWrap.NOWRAP
                            WRAP -> FlexWrap.WRAP
                            WRAP_REVERSE -> if (flexContainer is FlexboxLayoutManager) {
                                Toast.makeText(activity,
                                        R.string.wrap_reverse_not_supported,
                                        Toast.LENGTH_SHORT).show()
                                return
                            } else {
                                FlexWrap.WRAP_REVERSE
                            }
                            else -> return
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // No op
                    }
                }, object : ValueToStringConverter {
            override fun asString(value: Int): String {
                return when (value) {
                    FlexWrap.NOWRAP -> NOWRAP
                    FlexWrap.WRAP -> WRAP
                    FlexWrap.WRAP_REVERSE -> WRAP_REVERSE
                    else -> NOWRAP
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
                        flexContainer.justifyContent = when (parent.getItemAtPosition(position).toString()) {
                            FLEX_START -> JustifyContent.FLEX_START
                            FLEX_END -> JustifyContent.FLEX_END
                            CENTER -> JustifyContent.CENTER
                            SPACE_BETWEEN -> JustifyContent.SPACE_BETWEEN
                            SPACE_AROUND -> JustifyContent.SPACE_AROUND
                            else -> return
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // No op
                    }
                }, object : ValueToStringConverter {
            override fun asString(value: Int): String {
                return when (value) {
                    JustifyContent.FLEX_START -> FLEX_START
                    JustifyContent.FLEX_END -> FLEX_END
                    JustifyContent.CENTER -> CENTER
                    JustifyContent.SPACE_AROUND -> SPACE_AROUND
                    JustifyContent.SPACE_BETWEEN -> SPACE_BETWEEN
                    else -> FLEX_START
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
                        flexContainer.alignItems = when (parent.getItemAtPosition(position).toString()) {
                            FLEX_START -> AlignItems.FLEX_START
                            FLEX_END -> AlignItems.FLEX_END
                            CENTER -> AlignItems.CENTER
                            BASELINE -> AlignItems.BASELINE
                            STRETCH -> AlignItems.STRETCH
                            else -> return
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // No op
                    }
                }, object : ValueToStringConverter {
            override fun asString(value: Int): String {
                return when (value) {
                    AlignItems.FLEX_START -> FLEX_START
                    AlignItems.FLEX_END -> FLEX_END
                    AlignItems.CENTER -> CENTER
                    AlignItems.BASELINE -> BASELINE
                    AlignItems.STRETCH -> STRETCH
                    else -> STRETCH
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
                        flexContainer.alignContent = when (parent.getItemAtPosition(position).toString()) {
                            FLEX_START -> AlignContent.FLEX_START
                            FLEX_END -> AlignContent.FLEX_END
                            CENTER -> AlignContent.CENTER
                            SPACE_BETWEEN -> AlignContent.SPACE_BETWEEN
                            SPACE_AROUND -> AlignContent.SPACE_AROUND
                            STRETCH -> AlignContent.STRETCH
                            else -> return
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

        private const val DEFAULT_WIDTH = "120"

        private const val DEFAULT_HEIGHT = "80"
    }
}
