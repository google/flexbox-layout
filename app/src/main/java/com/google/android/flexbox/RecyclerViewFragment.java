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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.apps.flexbox.R;
import com.google.android.flexbox.recyclerview.FlexItemAdapter;
import java.util.ArrayList;

/**
 * Fragment that contains the {@link RecyclerView} and the {@link FlexboxLayoutManager} as its
 * LayoutManager for the flexbox playground.
 */
public class RecyclerViewFragment extends Fragment {

    private static final String FLEX_ITEMS_KEY = "flex_items";

    public static RecyclerViewFragment newInstance() {
        return new RecyclerViewFragment();
    }

    private FlexItemAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(
                R.id.recyclerview);
        final FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager();
        final MainActivity activity = (MainActivity) getActivity();
        recyclerView.setLayoutManager(flexboxLayoutManager);
        if (mAdapter == null) {
            mAdapter = new FlexItemAdapter(activity, flexboxLayoutManager);
        }
        recyclerView.setAdapter(mAdapter);
        if (savedInstanceState != null) {
            ArrayList<FlexboxLayoutManager.LayoutParams> layoutParams = savedInstanceState
                    .getParcelableArrayList(FLEX_ITEMS_KEY);
            assert layoutParams != null;
            for (int i = 0; i < layoutParams.size(); i++) {
                mAdapter.addItem(layoutParams.get(i));
            }
            mAdapter.notifyDataSetChanged();
        }
        final FragmentHelper fragmentHelper = new FragmentHelper(activity, flexboxLayoutManager);
        fragmentHelper.initializeViews();

        FloatingActionButton addFab = (FloatingActionButton) activity.findViewById(
                R.id.add_fab);
        if (addFab != null) {
            addFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FlexboxLayoutManager.LayoutParams lp = new FlexboxLayoutManager.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    fragmentHelper.setFlexItemAttributes(lp);
                    mAdapter.addItem(lp);
                }
            });
        }
        FloatingActionButton removeFab = (FloatingActionButton) activity.findViewById(
                R.id.remove_fab);
        if (removeFab != null) {
            removeFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAdapter.getItemCount() == 0) {
                        return;
                    }
                    mAdapter.removeItem(mAdapter.getItemCount() - 1);
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(FLEX_ITEMS_KEY, new ArrayList<>(mAdapter.getItems()));
    }
}
