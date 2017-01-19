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

package com.google.android.flexbox.recyclerview;

import com.google.android.apps.flexbox.R;
import com.google.android.flexbox.FlexItemChangedListenerImplRecyclerView;
import com.google.android.flexbox.FlexItemClickListener;
import com.google.android.flexbox.FlexboxLayoutManager;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} implementation for {@link FlexItemViewHolder}.
 */
public class FlexItemAdapter extends RecyclerView.Adapter<FlexItemViewHolder> {

    private AppCompatActivity mActivity;

    private FlexboxLayoutManager mLayoutManager;

    private List<FlexboxLayoutManager.LayoutParams> mLayoutParams;

    public FlexItemAdapter(AppCompatActivity activity, FlexboxLayoutManager layoutManager) {
        mActivity = activity;
        mLayoutManager = layoutManager;
        mLayoutParams = new ArrayList<>();
    }

    @Override
    public FlexItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_flex_item, parent, false);
        return new FlexItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FlexItemViewHolder holder, int position) {
        int adapterPosition = holder.getAdapterPosition();
        // TODO: More optimized set the click listener inside the view holder
        holder.itemView.setOnClickListener(new FlexItemClickListener(mActivity,
                new FlexItemChangedListenerImplRecyclerView(mLayoutManager, this),
                adapterPosition));
        holder.bindTo(mLayoutParams.get(position));
    }

    public void addItem(FlexboxLayoutManager.LayoutParams lp) {
        mLayoutParams.add(lp);
        notifyItemInserted(mLayoutParams.size() - 1);
    }

    public void removeItem(int position) {
        if (position < 0 || position >= mLayoutParams.size()) {
            return;
        }
        mLayoutParams.remove(position);
        notifyItemRemoved(mLayoutParams.size());
        notifyItemRangeChanged(position, mLayoutParams.size());
    }

    public List<FlexboxLayoutManager.LayoutParams> getItems() {
        return Collections.unmodifiableList(mLayoutParams);
    }

    @Override
    public int getItemCount() {
        return mLayoutParams.size();
    }
}
