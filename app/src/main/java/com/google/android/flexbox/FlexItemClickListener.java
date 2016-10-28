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

import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Implementation of the {@link android.view.View.OnClickListener} when a flex item is clicked in
 * the Flexbox Playground demo app.
 */
public class FlexItemClickListener implements View.OnClickListener {

    private static final String EDIT_DIALOG_TAG = "edit_dialog_tag";

    private int mViewIndex;

    private AppCompatActivity mActivity;

    private FlexItemChangedListener mFlexItemChangedListener;

    public FlexItemClickListener(AppCompatActivity activity, FlexItemChangedListener listener,
            int viewIndex) {
        mActivity = activity;
        mFlexItemChangedListener = listener;
        mViewIndex = viewIndex;
    }

    @Override
    public void onClick(View v) {
        FlexItemEditFragment fragment = FlexItemEditFragment
                .newInstance((FlexItem) v.getLayoutParams(), mViewIndex);
        fragment.setFlexItemChangedListener(mFlexItemChangedListener);
        fragment.show(mActivity.getSupportFragmentManager(), EDIT_DIALOG_TAG);
    }
}
