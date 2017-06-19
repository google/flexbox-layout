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

package com.google.android.flexbox.test;

import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} implementation for {@link TestViewHolder}, which has multiple
 * view types.
 */
class TestAdapterMultiViewTypes extends RecyclerView.Adapter<TestViewHolder> {

    static final int POSITION_MATCH_PARENT = 3;
    private static final int ITEMS = 50;

    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_MATCH_PARENT = 1;

    private final List<Item> mItems;

    TestAdapterMultiViewTypes() {
        mItems = new ArrayList<>();
        for (int i = 0; i < ITEMS; i++) {
            Item item = new Item();
            if (i == POSITION_MATCH_PARENT) {
                item.viewType = VIEW_TYPE_MATCH_PARENT;
            }
            item.value = i + 1;
            mItems.add(item);
        }
    }

    @Override
    public TestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_viewholder, parent, false);
        if (viewType == VIEW_TYPE_MATCH_PARENT) {
            FlexboxLayoutManager.LayoutParams flexboxLp = (FlexboxLayoutManager.LayoutParams)
                    view.getLayoutParams();
            flexboxLp.setFlexBasisPercent(90f);
            flexboxLp.setFlexGrow(1f);
        }
        return new TestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TestViewHolder holder, int position) {
        holder.mTextView.setText(String.valueOf(mItems.get(position).value));
        holder.mTextView.setBackgroundResource(R.drawable.flex_item_background);
        holder.mTextView.setGravity(Gravity.CENTER);
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).viewType;
    }

    void addItemAt(int position, Item item) {
        mItems.add(position, item);
        notifyItemInserted(position);
    }

    Item getItemAt(int position) {
        return mItems.get(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    static class Item {
        int viewType = VIEW_TYPE_NORMAL;
        int value;
    }
}
