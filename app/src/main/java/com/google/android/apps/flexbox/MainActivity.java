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

import com.google.android.libraries.flexbox.FlexboxLayout;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.security.SecureRandom;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String FLEX_ITEMS_KEY = "flex_items";

    private FlexboxLayout mFlexboxLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.setDrawerListener(toggle);
        }
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
            Menu navigationMenu = navigationView.getMenu();
        }

        mFlexboxLayout = (FlexboxLayout) findViewById(R.id.flexbox_layout);

        if (savedInstanceState != null) {
            ArrayList<FlexItem> flexItems = savedInstanceState
                    .getParcelableArrayList(FLEX_ITEMS_KEY);
            assert flexItems != null;
            mFlexboxLayout.removeAllViews();
            for (int i = 0; i < flexItems.size(); i++) {
                FlexItem flexItem = flexItems.get(i);
                FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                        Util.dpToPixel(this, flexItem.minWidth),
                        Util.dpToPixel(this, flexItem.minHeight));
                lp.order = flexItem.order;
                lp.flexGrow = flexItem.flexGrow;
                lp.flexShrink = flexItem.flexShrink;
                lp.alignSelf = flexItem.alignSelf;
                lp.topMargin = flexItem.topMargin;
                lp.setMarginStart(flexItem.startMargin);
                lp.setMarginEnd(flexItem.endMargin);
                lp.bottomMargin = flexItem.bottomMargin;
                TextView textView = createBaseFlexItemTextView(i);
                ViewCompat.setPaddingRelative(textView, flexItem.paddingStart, flexItem.paddingTop,
                        flexItem.paddingEnd, flexItem.paddingBottom);
                textView.setLayoutParams(lp);
                mFlexboxLayout.addView(textView);
            }
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
                    int height = (int) getResources().getDimension(
                            R.dimen.flex_item_length);
                    int width = getRandomFlexItemWidth(getResources(), height);
                    FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                            width, height);
                    textView.setLayoutParams(lp);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<FlexItem> flexItems = new ArrayList<>();
        for (int i = 0; i < mFlexboxLayout.getChildCount(); i++) {
            View child = mFlexboxLayout.getChildAt(i);
            FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) child.getLayoutParams();
            FlexItem flexItem = new FlexItem();
            flexItem.index = i;
            flexItem.order = lp.order;
            flexItem.flexGrow = lp.flexGrow;
            flexItem.flexShrink = lp.flexShrink;
            flexItem.alignSelf = lp.alignSelf;
            flexItem.minWidth = Util.pixelToDp(this, lp.width);
            flexItem.minHeight = Util.pixelToDp(this, lp.height);
            flexItem.topMargin = lp.topMargin;
            flexItem.startMargin = lp.getMarginStart();
            flexItem.endMargin = lp.getMarginEnd();
            flexItem.bottomMargin = lp.bottomMargin;
            flexItem.paddingTop = child.getPaddingTop();
            flexItem.paddingStart = ViewCompat.getPaddingStart(child);
            flexItem.paddingEnd = ViewCompat.getPaddingEnd(child);
            flexItem.paddingBottom = child.getPaddingBottom();
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

    private int getRandomFlexItemWidth(Resources resources, float defaultValue) {
        SecureRandom random = new SecureRandom();
        TypedArray candidates = resources.obtainTypedArray(R.array.flex_item_width_candidates);
        int width = (int) candidates
                .getDimension(random.nextInt(candidates.length()), defaultValue);
        candidates.recycle();
        return width;
    }

    private class FlexItemClickListener implements View.OnClickListener {

        int mViewIndex;

        FlexItemClickListener(int viewIndex) {
            mViewIndex = viewIndex;
        }

        @Override
        public void onClick(View v) {
            // TODO: Implement the onClick listener
        }
    }
}
