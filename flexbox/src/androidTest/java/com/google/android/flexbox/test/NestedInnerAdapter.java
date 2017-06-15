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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Adapter for the tests for nested RecyclerViews.
 * This Adapter is used for the inner RecyclerView.
 */
class NestedInnerAdapter extends RecyclerView.Adapter<NestedInnerAdapter.InnerViewHolder> {

    private int mInnerPosition;

    private int mItemCount;

    NestedInnerAdapter(int innerPosition, int itemCount) {
        mInnerPosition = innerPosition;
        mItemCount = itemCount;
    }

    @Override
    public NestedInnerAdapter.InnerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_textview, parent, false);
        return new InnerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(InnerViewHolder holder, int position) {
        holder.mTextView.setText(mInnerPosition + "-" + position);
    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }

    static class InnerViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;

        InnerViewHolder(View itemView) {
            super(itemView);

            mTextView = (TextView) itemView.findViewById(R.id.textview);
        }
    }
}
