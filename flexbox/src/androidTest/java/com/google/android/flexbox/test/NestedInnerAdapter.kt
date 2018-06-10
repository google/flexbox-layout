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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for the tests for nested RecyclerViews.
 * This Adapter is used for the inner RecyclerView.
 */
internal class NestedInnerAdapter(private val innerPosition: Int, private val itemCount: Int)
    : RecyclerView.Adapter<NestedInnerAdapter.InnerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NestedInnerAdapter.InnerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_textview, parent, false)
        return InnerViewHolder(view)
    }

    override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
        holder.textView.text = holder.textView.resources.getString(R.string.item_description, innerPosition, position)
    }

    override fun getItemCount() = itemCount

    internal class InnerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textView: TextView = itemView.findViewById(R.id.textview)
    }
}
