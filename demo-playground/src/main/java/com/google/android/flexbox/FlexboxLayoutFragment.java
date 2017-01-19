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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.Gravity;
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
        final MainActivity activity = (MainActivity) getActivity();
        mFlexContainer = (FlexboxLayout) view.findViewById(R.id.flexbox_layout);

        final FragmentHelper fragmentHelper = new FragmentHelper(activity, mFlexContainer);
        fragmentHelper.initializeViews();
        if (savedInstanceState != null) {
            ArrayList<FlexItem> flexItems = savedInstanceState
                    .getParcelableArrayList(FLEX_ITEMS_KEY);
            assert flexItems != null;
            mFlexContainer.removeAllViews();
            for (int i = 0; i < flexItems.size(); i++) {
                FlexItem flexItem = flexItems.get(i);
                TextView textView = createBaseFlexItemTextView(activity, i);
                textView.setLayoutParams((FlexboxLayout.LayoutParams) flexItem);
                mFlexContainer.addView(textView);
            }
        } else {
            for (int i = 0; i < mFlexContainer.getFlexItemCount(); i++) {
                mFlexContainer.getFlexItemAt(i).setOnClickListener(
                        new FlexItemClickListener(activity,
                                new FlexItemChangedListenerImpl(mFlexContainer), i));
            }
        }

        FloatingActionButton addFab = (FloatingActionButton) activity.findViewById(R.id.add_fab);
        if (addFab != null) {
            addFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int viewIndex = mFlexContainer.getFlexItemCount();
                    // index starts from 0. New View's index is N if N views ([0, 1, 2, ... N-1])
                    // exist.
                    TextView textView = createBaseFlexItemTextView(activity, viewIndex);
                    FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    fragmentHelper.setFlexItemAttributes(lp);
                    textView.setLayoutParams(lp);
                    textView.setOnClickListener(new FlexItemClickListener(activity,
                            new FlexItemChangedListenerImpl(mFlexContainer), viewIndex));
                    mFlexContainer.addView(textView);
                }
            });
        }
        FloatingActionButton removeFab = (FloatingActionButton) activity.findViewById(
                R.id.remove_fab);
        if (removeFab != null) {
            removeFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mFlexContainer.getFlexItemCount() == 0) {
                        return;
                    }
                    mFlexContainer.removeViewAt(mFlexContainer.getFlexItemCount() - 1);
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<FlexItem> flexItems = new ArrayList<>();
        for (int i = 0; i < mFlexContainer.getFlexItemCount(); i++) {
            View child = mFlexContainer.getFlexItemAt(i);
            flexItems.add((FlexItem) child.getLayoutParams());
        }
        outState.putParcelableArrayList(FLEX_ITEMS_KEY, flexItems);
    }

    private TextView createBaseFlexItemTextView(Context context, int index) {
        TextView textView = new TextView(context);
        textView.setBackgroundResource(R.drawable.flex_item_background);
        textView.setText(String.valueOf(index + 1));
        textView.setGravity(Gravity.CENTER);
        return textView;
    }
}
