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

package com.google.android.flexbox.apps.catgallery

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * Adapter class that handles the data set with the {@link RecyclerView.LayoutManager}
 */
internal class CatAdapter : RecyclerView.Adapter<CatViewHolder>() {

    companion object {
        private val CAT_IMAGE_IDS = intArrayOf(
                R.drawable.cat_1,
                R.drawable.cat_2,
                R.drawable.cat_3,
                R.drawable.cat_4,
                R.drawable.cat_5,
                R.drawable.cat_6,
                R.drawable.cat_7,
                R.drawable.cat_8,
                R.drawable.cat_9,
                R.drawable.cat_10,
                R.drawable.cat_11,
                R.drawable.cat_12,
                R.drawable.cat_13,
                R.drawable.cat_14,
                R.drawable.cat_15,
                R.drawable.cat_16,
                R.drawable.cat_17,
                R.drawable.cat_18,
                R.drawable.cat_19
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.viewholder_cat, parent, false)
        return CatViewHolder(view)
    }

    override fun onBindViewHolder(holder: CatViewHolder, position: Int) {
        val pos = position % CAT_IMAGE_IDS.size
        holder.bindTo(CAT_IMAGE_IDS[pos])
    }

    override fun getItemCount() = CAT_IMAGE_IDS.size * 4
}
