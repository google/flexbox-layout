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

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.apps.flexbox.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

/**
 * Fragment that contains the [FlexboxLayout] as the playground.
 */
class FlexboxLayoutFragment : Fragment() {

    private lateinit var flexContainer: FlexboxLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_flexboxlayout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = activity as MainActivity
        flexContainer = view.findViewById(R.id.flexbox_layout)

        val fragmentHelper = FragmentHelper(activity, flexContainer)
        fragmentHelper.initializeViews()
        if (savedInstanceState != null) {
            val flexItems = savedInstanceState
                    .getParcelableArrayList<FlexItem>(FLEX_ITEMS_KEY)!!
            flexContainer.removeAllViews()
            for (i in flexItems.indices) {
                val flexItem = flexItems[i]
                val textView = createBaseFlexItemTextView(activity, i)
                textView.layoutParams = flexItem as FlexboxLayout.LayoutParams
                flexContainer.addView(textView)
            }
        }
        for (i in 0 until flexContainer.flexItemCount) {
            flexContainer.getFlexItemAt(i).setOnClickListener(
                    FlexItemClickListener(activity,
                            FlexItemChangedListenerImpl(flexContainer), i))
        }

        val addFab: FloatingActionButton = activity.findViewById(R.id.add_fab)
        addFab.setOnClickListener {
            val viewIndex = flexContainer.flexItemCount
            // index starts from 0. New View's index is N if N views ([0, 1, 2, ... N-1])
            // exist.
            val textView = createBaseFlexItemTextView(activity, viewIndex)
            val lp = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            fragmentHelper.setFlexItemAttributes(lp)
            textView.layoutParams = lp
            textView.setOnClickListener(FlexItemClickListener(activity,
                    FlexItemChangedListenerImpl(flexContainer), viewIndex))
            flexContainer.addView(textView)
        }
        val removeFab: FloatingActionButton = activity.findViewById(R.id.remove_fab)
        removeFab.setOnClickListener(View.OnClickListener {
            if (flexContainer.flexItemCount == 0) {
                return@OnClickListener
            }
            flexContainer.removeViewAt(flexContainer.flexItemCount - 1)
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val flexItems = (0 until flexContainer.flexItemCount)
                .map { flexContainer.getFlexItemAt(it) }
                .mapTo(ArrayList()) { it.layoutParams as FlexItem }
        outState.putParcelableArrayList(FLEX_ITEMS_KEY, flexItems)
    }

    private fun createBaseFlexItemTextView(context: Context, index: Int): TextView {
        return TextView(context).apply {
            setBackgroundResource(R.drawable.flex_item_background)
            text = (index + 1).toString()
            gravity = Gravity.CENTER
        }
    }

    companion object {

        private const val FLEX_ITEMS_KEY = "flex_items_key"

        fun newInstance(): FlexboxLayoutFragment {
            return FlexboxLayoutFragment()
        }
    }
}
