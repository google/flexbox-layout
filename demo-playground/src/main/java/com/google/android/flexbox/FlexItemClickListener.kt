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

import android.support.v7.app.AppCompatActivity
import android.view.View

/**
 * Implementation of the [android.view.View.OnClickListener] when a flex item is clicked in
 * the Flexbox Playground demo app.
 */
internal class FlexItemClickListener(private val activity: AppCompatActivity, private val flexItemChangedListener: FlexItemChangedListener,
                                     private val viewIndex: Int) : View.OnClickListener {

    override fun onClick(v: View) =
            FlexItemEditFragment.newInstance(v.layoutParams as FlexItem, viewIndex).apply {
                setFlexItemChangedListener(flexItemChangedListener)
            }.show(activity.supportFragmentManager, EDIT_DIALOG_TAG)

    companion object {

        private const val EDIT_DIALOG_TAG = "edit_dialog_tag"
    }
}
