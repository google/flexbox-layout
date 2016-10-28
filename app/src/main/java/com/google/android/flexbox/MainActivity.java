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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String FLEXBOXLAYOUT_FRAGMENT = "flexboxlayout_fragment";

    private static final String RECYCLERVIEW_FRAGMENT = "recyclerview_fragment";

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
            drawer.addDrawerListener(toggle);
        }
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(
                R.id.nav_view);
        RadioGroup radioGroup = (RadioGroup) navigationView.getHeaderView(0)
                .findViewById(R.id.radiogroup_container_implementation);
        final FragmentManager fragmentManager = getSupportFragmentManager();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.radiobutton_viewgroup) {
                    replaceToFlexboxLayoutFragment(fragmentManager);
                } else {
                    RecyclerViewFragment fragment = (RecyclerViewFragment)
                            fragmentManager.findFragmentByTag(RECYCLERVIEW_FRAGMENT);
                    if (fragment == null) {
                        fragment = RecyclerViewFragment.newInstance();
                    }
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, fragment, RECYCLERVIEW_FRAGMENT).commit();
                }
            }
        });

        replaceToFlexboxLayoutFragment(fragmentManager);
    }

    private void replaceToFlexboxLayoutFragment(FragmentManager fragmentManager) {
        FlexboxLayoutFragment fragment = (FlexboxLayoutFragment)
                fragmentManager.findFragmentByTag(FLEXBOXLAYOUT_FRAGMENT);
        if (fragment == null) {
            fragment = FlexboxLayoutFragment.newInstance();
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, FLEXBOXLAYOUT_FRAGMENT).commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
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
}
