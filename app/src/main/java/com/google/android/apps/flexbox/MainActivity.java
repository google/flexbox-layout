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

package com.google.android.apps.flexbox;

import com.google.android.flexbox.AlignContent;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String FLEX_ITEMS_KEY = "flex_items";

    private static final String EDIT_DIALOG_TAG = "edit_dialog_tag";

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

    private FlexboxLayout mFlexboxLayout;

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeStringResources();

        mFlexboxLayout = (FlexboxLayout) findViewById(R.id.flexbox_layout);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.addDrawerListener(toggle);
        }
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
            Menu navigationMenu = navigationView.getMenu();
            initializeFlexDirectionSpinner(navigationMenu);
            initializeFlexWrapSpinner(navigationMenu);
            initializeJustifyContentSpinner(navigationMenu);
            initializeAlignItemsSpinner(navigationMenu);
            initializeAlignContentSpinner(navigationMenu);
        }

        if (savedInstanceState != null) {
            ArrayList<FlexItem> flexItems = savedInstanceState
                    .getParcelableArrayList(FLEX_ITEMS_KEY);
            assert flexItems != null;
            mFlexboxLayout.removeAllViews();
            for (int i = 0; i < flexItems.size(); i++) {
                FlexItem flexItem = flexItems.get(i);
                FlexboxLayout.LayoutParams lp = flexItem.toLayoutParams(this);
                TextView textView = createBaseFlexItemTextView(i);
                ViewCompat.setPaddingRelative(textView, flexItem.paddingStart, flexItem.paddingTop,
                        flexItem.paddingEnd, flexItem.paddingBottom);
                textView.setLayoutParams(lp);
                mFlexboxLayout.addView(textView);
            }
        }

        for (int i = 0; i < mFlexboxLayout.getChildCount(); i++) {
            mFlexboxLayout.getChildAt(i).setOnClickListener(new FlexItemClickListener(i));
        }

        FloatingActionButton addFab = (FloatingActionButton) findViewById(R.id.add_fab);
        if (addFab != null) {
            addFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int viewIndex = mFlexboxLayout.getChildCount();
                    // index starts from 0. New View's index is N if N views ([0, 1, 2, ... N-1])
                    // exist.
                    TextView textView = createBaseFlexItemTextView(viewIndex);
                    textView.setLayoutParams(createDefaultLayoutParams());
                    textView.setOnClickListener(new FlexItemClickListener(viewIndex));
                    mFlexboxLayout.addView(textView);
                }
            });
        }
        FloatingActionButton removeFab = (FloatingActionButton) findViewById(
                R.id.remove_fab);
        if (removeFab != null) {
            removeFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mFlexboxLayout.getChildCount() == 0) {
                        return;
                    }
                    mFlexboxLayout.removeViewAt(mFlexboxLayout.getChildCount() - 1);
                }
            });
        }
    }

    private void initializeStringResources() {
        ROW = getString(R.string.row);
        COLUMN = getString(R.string.column);
        ROW_REVERSE = getString(R.string.row_reverse);
        COLUMN_REVERSE = getString(R.string.column_reverse);
        NOWRAP = getString(R.string.nowrap);
        WRAP = getString(R.string.wrap);
        WRAP_REVERSE = getString(R.string.wrap_reverse);
        FLEX_START = getString(R.string.flex_start);
        FLEX_END = getString(R.string.flex_end);
        CENTER = getString(R.string.center);
        BASELINE = getString(R.string.baseline);
        STRETCH = getString(R.string.stretch);
        SPACE_BETWEEN = getString(R.string.space_between);
        SPACE_AROUND = getString(R.string.space_around);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<FlexItem> flexItems = new ArrayList<>();
        for (int i = 0; i < mFlexboxLayout.getChildCount(); i++) {
            View child = mFlexboxLayout.getChildAt(i);
            FlexItem flexItem = FlexItem.fromFlexView(child, i);
            flexItems.add(flexItem);
        }
        outState.putParcelableArrayList(FLEX_ITEMS_KEY, flexItems);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }

    private TextView createBaseFlexItemTextView(int index) {
        TextView textView = new TextView(this);
        textView.setBackgroundResource(R.drawable.flex_item_background);
        textView.setText(String.valueOf(index + 1));
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    /**
     * Creates a new {@link FlexboxLayout.LayoutParams} based on the stored default values in
     * the SharedPreferences.
     *
     * @return a {@link FlexboxLayout.LayoutParams} instance
     */
    private FlexboxLayout.LayoutParams createDefaultLayoutParams() {
        FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                Util.dpToPixel(this,
                        readPreferenceAsInteger(getString(R.string.new_width_key), DEFAULT_WIDTH)),
                Util.dpToPixel(this, readPreferenceAsInteger(getString(R.string.new_height_key),
                        DEFAULT_HEIGHT)));
        lp.order = readPreferenceAsInteger(getString(R.string.new_flex_item_order_key), "1");
        lp.flexGrow = readPreferenceAsFloat(getString(R.string.new_flex_grow_key), "0.0");
        lp.flexShrink = readPreferenceAsFloat(getString(R.string.new_flex_shrink_key), "1.0");
        int flexBasisPercent = readPreferenceAsInteger(
                getString(R.string.new_flex_basis_percent_key), "-1");
        lp.flexBasisPercent = flexBasisPercent == -1 ? -1 : (float) (flexBasisPercent / 100.0);
        return lp;
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
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                arrayResourceId, R.layout.spinner_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(listener);
        String selectedAsString = converter.asString(currentValue);
        int position = adapter.getPosition(selectedAsString);
        spinner.setSelection(position);
    }

    private void initializeFlexDirectionSpinner(Menu navigationMenu) {
        initializeSpinner(mFlexboxLayout.getFlexDirection(), R.id.menu_item_flex_direction,
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
                        mFlexboxLayout.setFlexDirection(flexDirection);
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
        initializeSpinner(mFlexboxLayout.getFlexWrap(), R.id.menu_item_flex_wrap,
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
                        mFlexboxLayout.setFlexWrap(flexWrap);
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
        initializeSpinner(mFlexboxLayout.getJustifyContent(), R.id.menu_item_justify_content,
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
                        mFlexboxLayout.setJustifyContent(justifyContent);
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
        initializeSpinner(mFlexboxLayout.getAlignItems(), R.id.menu_item_align_items,
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
                        mFlexboxLayout.setAlignItems(alignItems);
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
        initializeSpinner(mFlexboxLayout.getAlignContent(), R.id.menu_item_align_content,
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
                        mFlexboxLayout.setAlignContent(alignContent);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class FlexItemClickListener implements View.OnClickListener {

        int mViewIndex;

        FlexItemClickListener(int viewIndex) {
            mViewIndex = viewIndex;
        }

        @Override
        public void onClick(View v) {
            FlexItem flexItem = FlexItem.fromFlexView(v, mViewIndex);
            FlexItemEditFragment fragment = FlexItemEditFragment.newInstance(flexItem);
            fragment.setFlexItemChangedListener(new FlexItemChangeListenerImpl());
            fragment.show(getSupportFragmentManager(), EDIT_DIALOG_TAG);
        }
    }

    private class FlexItemChangeListenerImpl
            implements FlexItemEditFragment.FlexItemChangedListener {

        @Override
        public void onFlexItemChanged(FlexItem flexItem) {
            View view = mFlexboxLayout.getChildAt(flexItem.index);
            FlexboxLayout.LayoutParams lp = flexItem.toLayoutParams(MainActivity.this);
            view.setLayoutParams(lp);
        }
    }

    /**
     * Converter for converting an int value for Flexbox properties to a String.
     */
    private interface ValueToStringConverter {

        String asString(int value);
    }
}
