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

import com.google.android.apps.flexbox.R;

import android.content.SharedPreferences;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Helper class that has the common logic for initializing the Fragment for the play ground demo
 * such as {@link FlexboxLayoutFragment} and a Fragment that uses RecyclerView in it.
 */
class FragmentHelper {

    private static final String DEFAULT_WIDTH = "120";

    private static final String DEFAULT_HEIGHT = "80";

    private String ROW;

    private String COLUMN;

    private String ROW_REVERSE;

    private String COLUMN_REVERSE;

    private String NOWRAP;

    private String WRAP;

    private String WRAP_REVERSE;

    private String FLEX_START;

    private String FLEX_END;

    private String CENTER;

    private String BASELINE;

    private String STRETCH;

    private String SPACE_BETWEEN;

    private String SPACE_AROUND;

    private MainActivity mActivity;

    private FlexContainer mFlexContainer;

    private SharedPreferences mSharedPreferences;

    FragmentHelper(MainActivity mainActivity, FlexContainer flexContainer) {
        mActivity = mainActivity;
        mFlexContainer = flexContainer;
    }

    void initializeViews() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        initializeStringResources();
        NavigationView navigationView = (NavigationView) mActivity.findViewById(
                R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(mActivity);
            Menu navigationMenu = navigationView.getMenu();
            initializeFlexDirectionSpinner(navigationMenu);
            initializeFlexWrapSpinner(navigationMenu);
            initializeJustifyContentSpinner(navigationMenu);
            initializeAlignItemsSpinner(navigationMenu);
            initializeAlignContentSpinner(navigationMenu);
        }
    }

    private void initializeStringResources() {
        ROW = mActivity.getString(R.string.row);
        COLUMN = mActivity.getString(R.string.column);
        ROW_REVERSE = mActivity.getString(R.string.row_reverse);
        COLUMN_REVERSE = mActivity.getString(R.string.column_reverse);
        NOWRAP = mActivity.getString(R.string.nowrap);
        WRAP = mActivity.getString(R.string.wrap);
        WRAP_REVERSE = mActivity.getString(R.string.wrap_reverse);
        FLEX_START = mActivity.getString(R.string.flex_start);
        FLEX_END = mActivity.getString(R.string.flex_end);
        CENTER = mActivity.getString(R.string.center);
        BASELINE = mActivity.getString(R.string.baseline);
        STRETCH = mActivity.getString(R.string.stretch);
        SPACE_BETWEEN = mActivity.getString(R.string.space_between);
        SPACE_AROUND = mActivity.getString(R.string.space_around);
    }

    /**
     * Sets the attributes for a {@link FlexItem} based on the stored default values in
     * the SharedPreferences.
     *
     * @param flexItem the FlexItem instance
     * @return a FlexItem instance, which attributes from the SharedPreferences are updated
     */
    FlexItem setFlexItemAttributes(FlexItem flexItem) {
        flexItem.setWidth(Util.dpToPixel(mActivity,
                readPreferenceAsInteger(mActivity.getString(R.string.new_width_key),
                        DEFAULT_WIDTH)));
        flexItem.setHeight(Util.dpToPixel(mActivity,
                readPreferenceAsInteger(mActivity.getString(R.string.new_height_key),
                        DEFAULT_HEIGHT)));
        flexItem.setOrder(
                readPreferenceAsInteger(mActivity.getString(R.string.new_flex_item_order_key),
                        "1"));
        flexItem.setFlexGrow(
                readPreferenceAsFloat(mActivity.getString(R.string.new_flex_grow_key), "0.0"));
        flexItem.setFlexShrink(
                readPreferenceAsFloat(mActivity.getString(R.string.new_flex_shrink_key), "1.0"));
        int flexBasisPercent = readPreferenceAsInteger(
                mActivity.getString(R.string.new_flex_basis_percent_key), "-1");
        flexItem.setFlexBasisPercent(
                flexBasisPercent == -1 ? -1 : (float) (flexBasisPercent / 100.0));
        return flexItem;
    }

    private int readPreferenceAsInteger(String key, String defValue) {
        if (mSharedPreferences.contains(key)) {
            return Integer.valueOf(mSharedPreferences.getString(key, defValue));
        } else {
            return Integer.valueOf(defValue);
        }
    }

    private float readPreferenceAsFloat(String key, String defValue) {
        if (mSharedPreferences.contains(key)) {
            return Float.valueOf(mSharedPreferences.getString(key, defValue));
        } else {
            return Float.valueOf(defValue);
        }
    }

    private void initializeSpinner(int currentValue, int menuItemId, Menu navigationMenu,
            int arrayResourceId, AdapterView.OnItemSelectedListener listener,
            ValueToStringConverter converter) {
        Spinner spinner = (Spinner) MenuItemCompat
                .getActionView(navigationMenu.findItem(menuItemId));
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mActivity,
                arrayResourceId, R.layout.spinner_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(listener);
        String selectedAsString = converter.asString(currentValue);
        int position = adapter.getPosition(selectedAsString);
        spinner.setSelection(position);
    }

    private void initializeFlexDirectionSpinner(Menu navigationMenu) {
        initializeSpinner(mFlexContainer.getFlexDirection(), R.id.menu_item_flex_direction,
                navigationMenu, R.array.array_flex_direction,
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position,
                            long id) {
                        int flexDirection = FlexDirection.ROW;
                        String selected = parent.getItemAtPosition(position).toString();
                        if (selected.equals(ROW)) {
                            flexDirection = FlexDirection.ROW;
                        } else if (selected.equals(ROW_REVERSE)) {
                            flexDirection = FlexDirection.ROW_REVERSE;
                        } else if (selected.equals(COLUMN)) {
                            flexDirection = FlexDirection.COLUMN;
                        } else if (selected.equals(COLUMN_REVERSE)) {
                            flexDirection = FlexDirection.COLUMN_REVERSE;
                        }
                        mFlexContainer.setFlexDirection(flexDirection);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // No op
                    }
                }, new ValueToStringConverter() {
                    @Override
                    public String asString(int value) {
                        switch (value) {
                            case FlexDirection.ROW:
                                return ROW;
                            case FlexDirection.ROW_REVERSE:
                                return ROW_REVERSE;
                            case FlexDirection.COLUMN:
                                return COLUMN;
                            case FlexDirection.COLUMN_REVERSE:
                                return COLUMN_REVERSE;
                            default:
                                return ROW;
                        }
                    }
                });
    }

    private void initializeFlexWrapSpinner(Menu navigationMenu) {
        initializeSpinner(mFlexContainer.getFlexWrap(), R.id.menu_item_flex_wrap,
                navigationMenu, R.array.array_flex_wrap,
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position,
                            long id) {
                        int flexWrap = FlexWrap.NOWRAP;
                        String selected = parent.getItemAtPosition(position).toString();
                        if (selected.equals(NOWRAP)) {
                            flexWrap = FlexWrap.NOWRAP;
                        } else if (selected.equals(WRAP)) {
                            flexWrap = FlexWrap.WRAP;
                        } else if (selected.equals(WRAP_REVERSE)) {
                            flexWrap = FlexWrap.WRAP_REVERSE;
                        }
                        mFlexContainer.setFlexWrap(flexWrap);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // No op
                    }
                }, new ValueToStringConverter() {
                    @Override
                    public String asString(int value) {
                        switch (value) {
                            case FlexWrap.NOWRAP:
                                return NOWRAP;
                            case FlexWrap.WRAP:
                                return WRAP;
                            case FlexWrap.WRAP_REVERSE:
                                return WRAP_REVERSE;
                            default:
                                return NOWRAP;
                        }
                    }
                });
    }

    private void initializeJustifyContentSpinner(Menu navigationMenu) {
        initializeSpinner(mFlexContainer.getJustifyContent(), R.id.menu_item_justify_content,
                navigationMenu, R.array.array_justify_content,
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position,
                            long id) {
                        int justifyContent = JustifyContent.FLEX_START;
                        String selected = parent.getItemAtPosition(position).toString();
                        if (selected.equals(FLEX_START)) {
                            justifyContent = JustifyContent.FLEX_START;
                        } else if (selected.equals(FLEX_END)) {
                            justifyContent = JustifyContent.FLEX_END;
                        } else if (selected.equals(CENTER)) {
                            justifyContent = JustifyContent.CENTER;
                        } else if (selected.equals(SPACE_BETWEEN)) {
                            justifyContent = JustifyContent.SPACE_BETWEEN;
                        } else if (selected.equals(SPACE_AROUND)) {
                            justifyContent = JustifyContent.SPACE_AROUND;
                        }
                        mFlexContainer.setJustifyContent(justifyContent);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // No op
                    }
                }, new ValueToStringConverter() {
                    @Override
                    public String asString(int value) {
                        switch (value) {
                            case JustifyContent.FLEX_START:
                                return FLEX_START;
                            case JustifyContent.FLEX_END:
                                return FLEX_END;
                            case JustifyContent.CENTER:
                                return CENTER;
                            case JustifyContent.SPACE_AROUND:
                                return SPACE_AROUND;
                            case JustifyContent.SPACE_BETWEEN:
                                return SPACE_BETWEEN;
                            default:
                                return FLEX_START;
                        }
                    }
                });
    }

    private void initializeAlignItemsSpinner(Menu navigationMenu) {
        initializeSpinner(mFlexContainer.getAlignItems(), R.id.menu_item_align_items,
                navigationMenu, R.array.array_align_items,
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position,
                            long id) {
                        int alignItems = AlignItems.STRETCH;
                        String selected = parent.getItemAtPosition(position).toString();
                        if (selected.equals(FLEX_START)) {
                            alignItems = AlignItems.FLEX_START;
                        } else if (selected.equals(FLEX_END)) {
                            alignItems = AlignItems.FLEX_END;
                        } else if (selected.equals(CENTER)) {
                            alignItems = AlignItems.CENTER;
                        } else if (selected.equals(BASELINE)) {
                            alignItems = AlignItems.BASELINE;
                        } else if (selected.equals(STRETCH)) {
                            alignItems = AlignItems.STRETCH;
                        }
                        mFlexContainer.setAlignItems(alignItems);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // No op
                    }
                }, new ValueToStringConverter() {
                    @Override
                    public String asString(int value) {
                        switch (value) {
                            case AlignItems.FLEX_START:
                                return FLEX_START;
                            case AlignItems.FLEX_END:
                                return FLEX_END;
                            case AlignItems.CENTER:
                                return CENTER;
                            case AlignItems.BASELINE:
                                return BASELINE;
                            case AlignItems.STRETCH:
                                return STRETCH;
                            default:
                                return STRETCH;
                        }
                    }
                });
    }

    private void initializeAlignContentSpinner(Menu navigationMenu) {
        initializeSpinner(mFlexContainer.getAlignContent(), R.id.menu_item_align_content,
                navigationMenu, R.array.array_align_content,
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position,
                            long id) {
                        int alignContent = AlignContent.STRETCH;
                        String selected = parent.getItemAtPosition(position).toString();
                        if (selected.equals(FLEX_START)) {
                            alignContent = AlignContent.FLEX_START;
                        } else if (selected.equals(FLEX_END)) {
                            alignContent = AlignContent.FLEX_END;
                        } else if (selected.equals(CENTER)) {
                            alignContent = AlignContent.CENTER;
                        } else if (selected.equals(SPACE_BETWEEN)) {
                            alignContent = AlignContent.SPACE_BETWEEN;
                        } else if (selected.equals(SPACE_AROUND)) {
                            alignContent = AlignContent.SPACE_AROUND;
                        } else if (selected.equals(STRETCH)) {
                            alignContent = AlignContent.STRETCH;
                        }
                        mFlexContainer.setAlignContent(alignContent);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // No op
                    }
                }, new ValueToStringConverter() {
                    @Override
                    public String asString(int value) {
                        switch (value) {
                            case AlignContent.FLEX_START:
                                return FLEX_START;
                            case AlignContent.FLEX_END:
                                return FLEX_END;
                            case AlignContent.CENTER:
                                return CENTER;
                            case AlignContent.SPACE_BETWEEN:
                                return SPACE_BETWEEN;
                            case AlignContent.SPACE_AROUND:
                                return SPACE_AROUND;
                            case AlignContent.STRETCH:
                                return STRETCH;
                            default:
                                return STRETCH;
                        }
                    }
                });
    }

    /**
     * Converter for converting an int value for Flexbox properties to a String.
     */
    private interface ValueToStringConverter {

        String asString(int value);
    }
}
