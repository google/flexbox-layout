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

package com.google.android.flexbox.apps.catgallery;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.google.android.flexbox.FlexboxLayoutManager;

/**
 * ViewHolder that represents a cat image.
 */
class CatViewHolder extends RecyclerView.ViewHolder {

    private ImageView mImageView;

    CatViewHolder(View itemView) {
        super(itemView);
        mImageView = (ImageView) itemView.findViewById(R.id.imageview);
    }

    void bindTo(Drawable drawable) {
        mImageView.setImageDrawable(drawable);
        FlexboxLayoutManager.LayoutParams lp = (FlexboxLayoutManager.LayoutParams)
                mImageView.getLayoutParams();
        lp.setFlexGrow(1.0f);
    }
}
