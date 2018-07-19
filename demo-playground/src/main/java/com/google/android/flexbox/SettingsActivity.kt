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

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.apps.flexbox.R
import com.google.android.flexbox.validators.DimensionInputValidator
import com.google.android.flexbox.validators.FlexBasisPercentInputValidator
import com.google.android.flexbox.validators.IntegerInputValidator
import com.google.android.flexbox.validators.NonNegativeDecimalInputValidator

internal class SettingsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Display the fragment as the main content.
        supportFragmentManager.beginTransaction().replace(android.R.id.content,
                SettingsFragment()).commit()
    }

    /**
     * Fragment for settings.
     */
    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, s: String?) {
            addPreferencesFromResource(R.xml.new_flex_item_preferences)

            val orderPreference = findPreference(
                    getString(R.string.new_flex_item_order_key)) as EditTextPreference
            orderPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val validator = IntegerInputValidator()
                if (!validator.isValidInput(newValue.toString())) {
                    Toast.makeText(activity,
                            R.string.must_be_integer,
                            Toast.LENGTH_LONG).show()
                    return@OnPreferenceChangeListener false
                }
                true
            }

            val flexGrowPreference = findPreference(
                    getString(R.string.new_flex_grow_key)) as EditTextPreference
            flexGrowPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val validator = NonNegativeDecimalInputValidator()
                if (!validator.isValidInput(newValue.toString())) {
                    Toast.makeText(activity,
                            R.string.must_be_non_negative_float,
                            Toast.LENGTH_LONG).show()
                    return@OnPreferenceChangeListener false
                }
                true
            }

            val flexShrinkPreference = findPreference(
                    getString(R.string.new_flex_shrink_key)) as EditTextPreference
            flexShrinkPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val validator = NonNegativeDecimalInputValidator()
                if (!validator.isValidInput(newValue.toString())) {
                    Toast.makeText(activity,
                            R.string.must_be_non_negative_float,
                            Toast.LENGTH_LONG).show()
                    return@OnPreferenceChangeListener false
                }
                true
            }

            val flexBasisPercentPreference = findPreference(
                    getString(R.string.new_flex_basis_percent_key)) as EditTextPreference
            flexBasisPercentPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val validator = FlexBasisPercentInputValidator()
                if (!validator.isValidInput(newValue.toString())) {
                    Toast.makeText(activity,
                            R.string.must_be_minus_one_or_non_negative_integer,
                            Toast.LENGTH_LONG).show()
                    return@OnPreferenceChangeListener false
                }
                true
            }

            val widthPreference = findPreference(
                    getString(R.string.new_width_key)) as EditTextPreference
            widthPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val validator = DimensionInputValidator()
                if (!validator.isValidInput(newValue.toString())) {
                    Toast.makeText(activity,
                            R.string.must_be_minus_one_or_minus_two_or_non_negative_integer,
                            Toast.LENGTH_LONG).show()
                    return@OnPreferenceChangeListener false
                }
                true
            }

            val heightPreference = findPreference(
                    getString(R.string.new_height_key)) as EditTextPreference
            heightPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val validator = DimensionInputValidator()
                if (!validator.isValidInput(newValue.toString())) {
                    Toast.makeText(activity,
                            R.string.must_be_minus_one_or_minus_two_or_non_negative_integer,
                            Toast.LENGTH_LONG).show()
                    return@OnPreferenceChangeListener false
                }
                true
            }
        }

    }
}
