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
import com.google.android.flexbox.validators.DimensionInputValidator;
import com.google.android.flexbox.validators.FlexBasisPercentInputValidator;
import com.google.android.flexbox.validators.InputValidator;
import com.google.android.flexbox.validators.IntegerInputValidator;
import com.google.android.flexbox.validators.NonNegativeDecimalInputValidator;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

public class SettingsActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();
    }

    /**
     * Fragment for settings.
     */
    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String s) {
            addPreferencesFromResource(R.xml.new_flex_item_preferences);

            EditTextPreference orderPreference = (EditTextPreference) findPreference(
                    getString(R.string.new_flex_item_order_key));
            orderPreference.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            InputValidator validator = new IntegerInputValidator();
                            if (!validator.isValidInput(newValue.toString())) {
                                Toast.makeText(getActivity(),
                                        R.string.must_be_integer,
                                        Toast.LENGTH_LONG).show();
                                return false;
                            }
                            return true;
                        }
                    });

            EditTextPreference flexGrowPreference = (EditTextPreference) findPreference(
                    getString(R.string.new_flex_grow_key));
            flexGrowPreference.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            InputValidator validator = new NonNegativeDecimalInputValidator();
                            if (!validator.isValidInput(newValue.toString())) {
                                Toast.makeText(getActivity(),
                                        R.string.must_be_non_negative_float,
                                        Toast.LENGTH_LONG).show();
                                return false;
                            }
                            return true;
                        }
                    });

            EditTextPreference flexShrinkPreference = (EditTextPreference) findPreference(
                    getString(R.string.new_flex_shrink_key));
            flexShrinkPreference.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            InputValidator validator = new NonNegativeDecimalInputValidator();
                            if (!validator.isValidInput(newValue.toString())) {
                                Toast.makeText(getActivity(),
                                        R.string.must_be_non_negative_float,
                                        Toast.LENGTH_LONG).show();
                                return false;
                            }
                            return true;
                        }
                    });

            EditTextPreference flexBasisPercentPreference = (EditTextPreference) findPreference(
                    getString(R.string.new_flex_basis_percent_key));
            flexBasisPercentPreference.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            InputValidator validator = new FlexBasisPercentInputValidator();
                            if (!validator.isValidInput(newValue.toString())) {
                                Toast.makeText(getActivity(),
                                        R.string.must_be_minus_one_or_non_negative_integer,
                                        Toast.LENGTH_LONG).show();
                                return false;
                            }
                            return true;
                        }
                    });

            EditTextPreference widthPreference = (EditTextPreference) findPreference(
                    getString(R.string.new_width_key));
            widthPreference.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            InputValidator validator = new DimensionInputValidator();
                            if (!validator.isValidInput(newValue.toString())) {
                                Toast.makeText(getActivity(),
                                        R.string.must_be_minus_one_or_minus_two_or_non_negative_integer,
                                        Toast.LENGTH_LONG).show();
                                return false;
                            }
                            return true;
                        }
                    });

            EditTextPreference heightPreference = (EditTextPreference) findPreference(
                    getString(R.string.new_height_key));
            heightPreference.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            InputValidator validator = new DimensionInputValidator();
                            if (!validator.isValidInput(newValue.toString())) {
                                Toast.makeText(getActivity(),
                                        R.string.must_be_minus_one_or_minus_two_or_non_negative_integer,
                                        Toast.LENGTH_LONG).show();
                                return false;
                            }
                            return true;
                        }
                    });
        }

    }
}
