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

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import com.google.android.apps.flexbox.R
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val radioGroup: RadioGroup = navigationView.getHeaderView(0)
                .findViewById(R.id.radiogroup_container_implementation)
        val fragmentManager = supportFragmentManager

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radiobutton_viewgroup) {
                replaceToFlexboxLayoutFragment(fragmentManager)
            } else {
                replaceToRecyclerViewFragment(fragmentManager)
            }
        }

        if (savedInstanceState == null) {
            replaceToFlexboxLayoutFragment(fragmentManager)
        }
    }

    private fun replaceToFlexboxLayoutFragment(fragmentManager: FragmentManager) {
        var fragment: FlexboxLayoutFragment? = fragmentManager.findFragmentByTag(FLEXBOXLAYOUT_FRAGMENT) as FlexboxLayoutFragment?
        if (fragment == null) {
            fragment = FlexboxLayoutFragment.newInstance()
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, FLEXBOXLAYOUT_FRAGMENT).commit()
    }

    private fun replaceToRecyclerViewFragment(fragmentManager: FragmentManager) {
        var fragment: RecyclerViewFragment? = fragmentManager.findFragmentByTag(RECYCLERVIEW_FRAGMENT) as RecyclerViewFragment?
        if (fragment == null) {
            fragment = RecyclerViewFragment.newInstance()
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, RECYCLERVIEW_FRAGMENT).commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        private const val FLEXBOXLAYOUT_FRAGMENT = "flexboxlayout_fragment"

        private const val RECYCLERVIEW_FRAGMENT = "recyclerview_fragment"
    }
}
