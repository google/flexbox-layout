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

package com.google.android.flexbox;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Implementation for the {@link FlexItemChangedListener}.
 * It expects RecyclerView as the underlying flex container implementation.
 */
public class FlexItemChangedListenerImplRecyclerView implements FlexItemChangedListener {

    private FlexContainer mFlexContainer;

    private RecyclerView.Adapter mAdapter;

    public FlexItemChangedListenerImplRecyclerView(FlexContainer flexContainer,
            RecyclerView.Adapter adapter) {
        mFlexContainer = flexContainer;
        mAdapter = adapter;
    }

    @Override
    public void onFlexItemChanged(FlexItem flexItem, int viewIndex) {
        View view = mFlexContainer.getFlexItemAt(viewIndex);
        view.setLayoutParams((ViewGroup.LayoutParams) flexItem);
        mAdapter.notifyDataSetChanged();
        // TODO: An Exception is thrown if notifyItemChanged(int) is used.
        // Investigate that, but using LinearLayoutManager also produces the same Exception
        // java.lang.IllegalArgumentException: Called attach on a child which is not detached:
    }
}
