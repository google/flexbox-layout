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

package com.google.android.apps.flexbox.test;

import com.google.android.flexbox.MainActivity;
import com.google.android.apps.flexbox.R;
import com.google.android.flexbox.AlignContent;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.pm.ActivityInfo;
import android.support.design.widget.NavigationView;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.FlakyTest;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


/**
 * Integration tests for {@link MainActivity}.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class MainActivityTest {


    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    @FlakyTest
    public void testAddFlexItem() {
        MainActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertNotNull(flexboxLayout);
        int beforeCount = flexboxLayout.getChildCount();
        onView(withId(R.id.add_fab)).perform(click());

        assertThat(flexboxLayout.getChildCount(), is(beforeCount + 1));
    }

    @Test
    @FlakyTest
    public void testRemoveFlexItem() {
        MainActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertNotNull(flexboxLayout);
        int beforeCount = flexboxLayout.getChildCount();
        onView(withId(R.id.remove_fab)).perform(click());

        assertThat(flexboxLayout.getChildCount(), is(beforeCount - 1));
    }

    @Test
    @FlakyTest
    public void testConfigurationChange() {
        MainActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertNotNull(flexboxLayout);
        onView(withId(R.id.add_fab)).perform(click());
        onView(withId(R.id.add_fab)).perform(click());
        onView(withId(R.id.add_fab)).perform(click());
        int beforeCount = flexboxLayout.getChildCount();

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // Verify the flex items are restored across the configuration change.
        assertThat(flexboxLayout.getChildCount(), is(beforeCount));
    }

    @Test
    @SuppressWarnings("unchecked")
    @FlakyTest
    public void testFlexDirectionSpinner() {
        MainActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertNotNull(flexboxLayout);
        NavigationView navigationView = (NavigationView) activity.findViewById(R.id.nav_view);
        assertNotNull(navigationView);
        Menu menu = navigationView.getMenu();
        final Spinner spinner = (Spinner) MenuItemCompat
                .getActionView(menu.findItem(R.id.menu_item_flex_direction));
        ArrayAdapter<CharSequence> spinnerAdapter = (ArrayAdapter<CharSequence>)
                spinner.getAdapter();

        final int columnPosition = spinnerAdapter.getPosition(activity.getString(R.string.column));
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setSelection(columnPosition);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.COLUMN));

        final int rowReversePosition = spinnerAdapter
                .getPosition(activity.getString(R.string.row_reverse));
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setSelection(rowReversePosition);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(flexboxLayout.getFlexDirection(), is(FlexDirection.ROW_REVERSE));
    }

    @Test
    @SuppressWarnings("unchecked")
    @FlakyTest
    public void testFlexWrapSpinner() {
        MainActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertNotNull(flexboxLayout);
        NavigationView navigationView = (NavigationView) activity.findViewById(R.id.nav_view);
        assertNotNull(navigationView);
        Menu menu = navigationView.getMenu();
        final Spinner spinner = (Spinner) MenuItemCompat
                .getActionView(menu.findItem(R.id.menu_item_flex_wrap));
        ArrayAdapter<CharSequence> spinnerAdapter = (ArrayAdapter<CharSequence>)
                spinner.getAdapter();

        final int wrapReversePosition = spinnerAdapter
                .getPosition(activity.getString(R.string.wrap_reverse));
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setSelection(wrapReversePosition);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.WRAP_REVERSE));

        final int noWrapPosition = spinnerAdapter
                .getPosition(activity.getString(R.string.nowrap));
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setSelection(noWrapPosition);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(flexboxLayout.getFlexWrap(), is(FlexWrap.NOWRAP));
    }

    @Test
    @SuppressWarnings("unchecked")
    @FlakyTest
    public void testJustifyContentSpinner() {
        MainActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertNotNull(flexboxLayout);
        NavigationView navigationView = (NavigationView) activity.findViewById(R.id.nav_view);
        assertNotNull(navigationView);
        Menu menu = navigationView.getMenu();
        final Spinner spinner = (Spinner) MenuItemCompat
                .getActionView(menu.findItem(R.id.menu_item_justify_content));
        ArrayAdapter<CharSequence> spinnerAdapter = (ArrayAdapter<CharSequence>)
                spinner.getAdapter();

        final int spaceBetweenPosition = spinnerAdapter
                .getPosition(activity.getString(R.string.space_between));
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setSelection(spaceBetweenPosition);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(flexboxLayout.getJustifyContent(),
                is(JustifyContent.SPACE_BETWEEN));

        final int centerPosition = spinnerAdapter
                .getPosition(activity.getString(R.string.center));
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setSelection(centerPosition);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(flexboxLayout.getJustifyContent(), is(JustifyContent.CENTER));
    }

    @Test
    @SuppressWarnings("unchecked")
    @FlakyTest
    public void testAlignItemsSpinner() {
        MainActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertNotNull(flexboxLayout);
        NavigationView navigationView = (NavigationView) activity.findViewById(R.id.nav_view);
        assertNotNull(navigationView);
        Menu menu = navigationView.getMenu();
        final Spinner spinner = (Spinner) MenuItemCompat
                .getActionView(menu.findItem(R.id.menu_item_align_items));
        ArrayAdapter<CharSequence> spinnerAdapter = (ArrayAdapter<CharSequence>)
                spinner.getAdapter();

        final int baselinePosition = spinnerAdapter
                .getPosition(activity.getString(R.string.baseline));
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setSelection(baselinePosition);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(flexboxLayout.getAlignItems(),
                is(AlignItems.BASELINE));

        final int flexEndPosition = spinnerAdapter
                .getPosition(activity.getString(R.string.flex_end));
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setSelection(flexEndPosition);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(flexboxLayout.getAlignItems(), is(AlignItems.FLEX_END));
    }

    @Test
    @SuppressWarnings("unchecked")
    @FlakyTest
    public void testAlignContentSpinner() {
        MainActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertNotNull(flexboxLayout);
        NavigationView navigationView = (NavigationView) activity.findViewById(R.id.nav_view);
        assertNotNull(navigationView);
        Menu menu = navigationView.getMenu();
        final Spinner spinner = (Spinner) MenuItemCompat
                .getActionView(menu.findItem(R.id.menu_item_align_content));
        ArrayAdapter<CharSequence> spinnerAdapter = (ArrayAdapter<CharSequence>)
                spinner.getAdapter();

        final int spaceAroundPosition = spinnerAdapter
                .getPosition(activity.getString(R.string.space_around));
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setSelection(spaceAroundPosition);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(flexboxLayout.getAlignContent(),
                is(AlignContent.SPACE_AROUND));

        final int stretchPosition = spinnerAdapter
                .getPosition(activity.getString(R.string.stretch));
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setSelection(stretchPosition);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertThat(flexboxLayout.getAlignContent(), is(AlignContent.STRETCH));
    }

    @Test
    @FlakyTest
    public void testEditFragment_changeOrder() {
        MainActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertNotNull(flexboxLayout);
        onView(withId(R.id.textview1)).perform(click());
        onView(withId(R.id.edit_text_order)).perform(replaceText("3"), closeSoftKeyboard());
        onView(withId(R.id.button_ok)).perform(click());
        TextView first = (TextView) flexboxLayout.getReorderedChildAt(0);
        TextView second = (TextView) flexboxLayout.getReorderedChildAt(1);
        TextView third = (TextView) flexboxLayout.getReorderedChildAt(2);

        assertThat(first.getText().toString(), is("2"));
        assertThat(second.getText().toString(), is("3"));
        assertThat(third.getText().toString(), is("1"));
    }

    @Test
    @FlakyTest
    public void testEditFragment_changeFlexGrow() {
        MainActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertNotNull(flexboxLayout);
        onView(withId(R.id.textview1)).perform(click());
        onView(withId(R.id.edit_text_flex_grow)).perform(replaceText("1"), closeSoftKeyboard());
        onView(withId(R.id.button_ok)).perform(click());
        TextView first = (TextView) activity.findViewById(R.id.textview1);
        TextView second = (TextView) activity.findViewById(R.id.textview2);
        TextView third = (TextView) activity.findViewById(R.id.textview3);
        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);

        assertThat(first.getWidth(),
                is(flexboxLayout.getWidth() - second.getWidth() - third.getWidth()));
    }

    @Test
    @FlakyTest
    public void testEditFragment_changeFlexGrowFloat() {
        MainActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertNotNull(flexboxLayout);
        onView(withId(R.id.textview1)).perform(click());
        onView(withId(R.id.edit_text_flex_grow)).perform(replaceText("1.0"), closeSoftKeyboard());
        onView(withId(R.id.button_ok)).perform(click());
        TextView first = (TextView) activity.findViewById(R.id.textview1);
        TextView second = (TextView) activity.findViewById(R.id.textview2);
        TextView third = (TextView) activity.findViewById(R.id.textview3);
        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);

        assertThat(first.getWidth(),
                is(flexboxLayout.getWidth() - second.getWidth() - third.getWidth()));
    }

    @Test
    @FlakyTest
    public void testEditFragment_changeFlexBasisPercent() {
        MainActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertNotNull(flexboxLayout);
        onView(withId(R.id.textview1)).perform(click());
        onView(withId(R.id.edit_text_flex_basis_percent))
                .perform(replaceText("50"), closeSoftKeyboard());
        onView(withId(R.id.button_ok)).perform(click());
        TextView first = (TextView) activity.findViewById(R.id.textview1);
        TextView second = (TextView) activity.findViewById(R.id.textview2);
        TextView third = (TextView) activity.findViewById(R.id.textview3);
        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);

        assertTrue(first.getWidth() - 1 <= flexboxLayout.getWidth() / 2
                || flexboxLayout.getWidth() / 2 <= first.getWidth() + 1);
    }

    @Test
    @FlakyTest
    public void testSwitchRecyclerViewFragment() {
        MainActivity activity = mActivityRule.getActivity();
        FlexboxLayout flexboxLayout = (FlexboxLayout) activity.findViewById(R.id.flexbox_layout);
        assertNotNull(flexboxLayout);
        NavigationView navigationView = (NavigationView) activity.findViewById(R.id.nav_view);
        assertNotNull(navigationView);
        assertNull(activity.findViewById(R.id.recyclerview));
        assertNotNull(activity.findViewById(R.id.flexbox_layout));

        final RadioGroup radioGroup = (RadioGroup) navigationView.getHeaderView(0)
                .findViewById(R.id.radiogroup_container_implementation);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                radioGroup.check(R.id.radiobutton_recyclerview);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertNotNull(activity.findViewById(R.id.recyclerview));
        assertNull(activity.findViewById(R.id.flexbox_layout));
    }
}
