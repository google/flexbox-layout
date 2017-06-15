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

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the tests for nested RecyclerViews.
 * This Adapter is used for the outer RecyclerView.
 */
class NestedOuterAdapter extends RecyclerView.Adapter<NestedOuterAdapter.OuterViewHolder> {

    private static final int ITEM_COUNT = 4;

    private final Context mContext;

    private final List<OuterViewHolder> mViewHolderList = new ArrayList<>();

    private final int mFlexDirection;

    private final int mInnerAdapterItemCount;

    private final int mViewHolderResId;

    NestedOuterAdapter(Context context, @FlexDirection int flexDirection, int innerAdapterItemCount,
            @LayoutRes int viewHolderResId) {
        mContext = context;
        mFlexDirection = flexDirection;
        mInnerAdapterItemCount = innerAdapterItemCount;
        mViewHolderResId = viewHolderResId;
    }

    @Override
    public NestedOuterAdapter.OuterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(mViewHolderResId, parent, false);
        OuterViewHolder holder = new OuterViewHolder(view);
        mViewHolderList.add(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(OuterViewHolder holder, int position) {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(mContext);
        layoutManager.setFlexDirection(mFlexDirection);
        holder.mInnerRecyclerView.setLayoutManager(layoutManager);
        holder.mInnerRecyclerView.setAdapter(new NestedInnerAdapter(position,
                mInnerAdapterItemCount));
    }

    OuterViewHolder getViewHolder(int position) {
        return mViewHolderList.get(position);
    }

    @Override
    public int getItemCount() {
        return ITEM_COUNT;
    }

    static class OuterViewHolder extends RecyclerView.ViewHolder {

        RecyclerView mInnerRecyclerView;

        OuterViewHolder(View itemView) {
            super(itemView);

            mInnerRecyclerView = (RecyclerView) itemView.findViewById(R.id.recyclerview_inner);
        }
    }
}
