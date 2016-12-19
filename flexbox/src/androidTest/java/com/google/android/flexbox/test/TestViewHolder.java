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

package com.google.android.flexbox.test;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * ViewHolder implementation for a flex item for testing.
 */
class TestViewHolder extends RecyclerView.ViewHolder {

    TextView mTextView;
    View mItemView;

    TestViewHolder(View itemView) {
        super(itemView);

        mItemView = itemView;
        mTextView = (TextView) itemView.findViewById(R.id.textview);
    }
}
