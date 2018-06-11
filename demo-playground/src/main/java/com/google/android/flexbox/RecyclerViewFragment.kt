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

package com.google.android.flexbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.apps.flexbox.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Fragment that contains the [RecyclerView] and the [FlexboxLayoutManager] as its
 * LayoutManager for the flexbox playground.
 */
internal class RecyclerViewFragment : Fragment() {

    private lateinit var adapter: FlexItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerview)
        val activity = activity as MainActivity
        val flexboxLayoutManager = FlexboxLayoutManager(activity)
        recyclerView.layoutManager = flexboxLayoutManager
        adapter = FlexItemAdapter(activity, flexboxLayoutManager)
        recyclerView.adapter = adapter
        if (savedInstanceState != null) {
            val layoutParams : List<FlexboxLayoutManager.LayoutParams>? = savedInstanceState
                    .getParcelableArrayList(FLEX_ITEMS_KEY)
            layoutParams?.let {
                for (i in layoutParams.indices) {
                    adapter.addItem(layoutParams[i])
                }
            }
            adapter.notifyDataSetChanged()
        }
        val fragmentHelper = FragmentHelper(activity, flexboxLayoutManager)
        fragmentHelper.initializeViews()

        val addFab: FloatingActionButton = activity.findViewById(R.id.add_fab)
        addFab.setOnClickListener {
            val lp = FlexboxLayoutManager.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            fragmentHelper.setFlexItemAttributes(lp)
            adapter.addItem(lp)
        }
        val removeFab: FloatingActionButton = activity.findViewById(R.id.remove_fab)
        removeFab.setOnClickListener(View.OnClickListener {
            if (adapter.itemCount == 0) {
                return@OnClickListener
            }
            adapter.removeItem(adapter.itemCount - 1)
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(FLEX_ITEMS_KEY, ArrayList(adapter.items))
    }

    companion object {

        private const val FLEX_ITEMS_KEY = "flex_items"

        fun newInstance(): RecyclerViewFragment {
            return RecyclerViewFragment()
        }
    }
}
