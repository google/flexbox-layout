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

package com.google.android.flexbox.test

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager

/**
 * [RecyclerView.Adapter] implementation for [TestViewHolder], which has multiple
 * view types.
 */
internal class TestAdapterMultiViewTypes : RecyclerView.Adapter<TestViewHolder>() {

    private val items = mutableListOf<Item>()

    init {
        for (i in 0 until ITEMS) {
            val item = Item()
            if (i == POSITION_MATCH_PARENT) {
                item.viewType = VIEW_TYPE_MATCH_PARENT
            }
            item.value = i + 1
            items.add(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_viewholder, parent, false)
        if (viewType == VIEW_TYPE_MATCH_PARENT) {
            val flexboxLp = view.layoutParams as FlexboxLayoutManager.LayoutParams
            flexboxLp.flexBasisPercent = 90f
            flexboxLp.flexGrow = 1f
        }
        return TestViewHolder(view)
    }

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        holder.textView.text = items[position].value.toString()
        holder.textView.setBackgroundResource(R.drawable.flex_item_background)
        holder.textView.gravity = Gravity.CENTER
    }

    override fun getItemViewType(position: Int) = items[position].viewType

    fun addItemAt(position: Int, item: Item) {
        items.add(position, item)
        notifyItemInserted(position)
    }

    override fun getItemCount() = items.size

    internal class Item {
        internal var viewType = VIEW_TYPE_NORMAL
        internal var value = 0
    }

    companion object {

        internal const val POSITION_MATCH_PARENT = 3
        private const val ITEMS = 50

        private const val VIEW_TYPE_NORMAL = 0
        private const val VIEW_TYPE_MATCH_PARENT = 1
    }
}
