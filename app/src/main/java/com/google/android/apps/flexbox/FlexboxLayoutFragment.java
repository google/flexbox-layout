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

import com.google.android.apps.flexbox.listeners.FlexItemChangedListenerImpl;
import com.google.android.apps.flexbox.listeners.FlexItemClickListener;
import com.google.android.flexbox.FlexContainer;
import com.google.android.flexbox.FlexItem;
import com.google.android.flexbox.FlexboxLayout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Fragment that contains the {@link FlexboxLayout} as the playground.
 */
public class FlexboxLayoutFragment extends Fragment {

    private static final String FLEX_ITEMS_KEY = "flex_items_key";

    private FlexContainer mFlexContainer;

    public static FlexboxLayoutFragment newInstance() {
        return new FlexboxLayoutFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_flexboxlayout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        mFlexContainer = (FlexboxLayout) view.findViewById(R.id.flexbox_layout);

        FragmentHelper fragmentHelper = new FragmentHelper(activity, mFlexContainer);
        fragmentHelper.initializeViews();
        if (savedInstanceState != null) {
            ArrayList<FlexItem> flexItems = savedInstanceState
                    .getParcelableArrayList(FLEX_ITEMS_KEY);
            assert flexItems != null;
            mFlexContainer.removeAllViews();
            for (int i = 0; i < flexItems.size(); i++) {
                FlexItem flexItem = flexItems.get(i);
                TextView textView = fragmentHelper.createBaseFlexItemTextView(i);
                textView.setLayoutParams((FlexboxLayout.LayoutParams) flexItem);
                mFlexContainer.addView(textView);
            }
        } else {
            for (int i = 0; i < mFlexContainer.getChildCount(); i++) {
                mFlexContainer.getChildAt(i).setOnClickListener(
                        new FlexItemClickListener(activity,
                                new FlexItemChangedListenerImpl(mFlexContainer), i));
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<FlexItem> flexItems = new ArrayList<>();
        for (int i = 0; i < mFlexContainer.getChildCount(); i++) {
            View child = mFlexContainer.getChildAt(i);
            flexItems.add((FlexItem) child.getLayoutParams());
        }
        outState.putParcelableArrayList(FLEX_ITEMS_KEY, flexItems);
    }
}
