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

package com.google.android.flexbox

import android.view.View
import android.view.ViewGroup

/**
 * Fake implementation of [FlexContainer].
 */
internal class FakeFlexContainer : FlexContainer {

    private val views = mutableListOf<View>()

    private var flexLines = mutableListOf<FlexLine>()

    @FlexDirection
    private var flexDirection = FlexDirection.ROW

    @FlexWrap
    private var flexWrap = FlexWrap.WRAP

    @JustifyContent
    private var justifyContent = JustifyContent.FLEX_START

    @AlignItems
    private var alignItems = AlignItems.STRETCH

    @AlignContent
    private var alignContent = AlignContent.STRETCH

    override fun getFlexItemCount() = views.size

    override fun getFlexItemAt(index: Int) = views[index]

    override fun getReorderedFlexItemAt(index: Int) = views[index]

    override fun addView(view: View) {
        views.add(view)
    }

    override fun addView(view: View, index: Int) {
        views.add(index, view)
    }

    override fun removeAllViews() {
        views.clear()
    }

    override fun removeViewAt(index: Int) {
        views.removeAt(index)
    }

    override fun getFlexDirection() = flexDirection

    override fun setFlexDirection(@FlexDirection flexDirection: Int) {
        this.flexDirection = flexDirection
    }

    override fun getFlexWrap() = flexWrap

    override fun setFlexWrap(@FlexWrap flexWrap: Int) {
        this.flexWrap = flexWrap
    }

    override fun getJustifyContent() = justifyContent

    override fun setJustifyContent(@JustifyContent justifyContent: Int) {
        this.justifyContent = justifyContent
    }

    override fun getAlignContent() = alignContent

    override fun setAlignContent(@AlignContent alignContent: Int) {
        this.alignContent = alignContent
    }

    override fun getAlignItems() = alignItems

    override fun setAlignItems(@AlignItems alignItems: Int) {
        this.alignItems = alignItems
    }

    override fun getFlexLines() = flexLines

    override fun isMainAxisDirectionHorizontal(): Boolean {
        return flexDirection == FlexDirection.ROW || flexDirection == FlexDirection.ROW_REVERSE
    }

    override fun getDecorationLengthMainAxis(view: View, index: Int, indexInFlexLine: Int) = 0

    override fun getDecorationLengthCrossAxis(view: View) = 0

    override fun getPaddingTop() = 0

    override fun getPaddingLeft() = 0

    override fun getPaddingRight() = 0

    override fun getPaddingBottom() = 0

    override fun getPaddingStart() = 0

    override fun getPaddingEnd() = 0

    override fun getChildWidthMeasureSpec(widthSpec: Int, padding: Int, childDimension: Int): Int {
        return ViewGroup.getChildMeasureSpec(widthSpec, padding, childDimension)
    }

    override fun getChildHeightMeasureSpec(heightSpec: Int, padding: Int, childDimension: Int): Int {
        return ViewGroup.getChildMeasureSpec(heightSpec, padding, childDimension)
    }

    override fun getLargestMainSize() = flexLines.maxBy { it.mMainSize }?.mMainSize ?: Integer.MIN_VALUE

    override fun getSumOfCrossSize() = flexLines.sumBy { it.mCrossSize }

    override fun onNewFlexItemAdded(view: View, index: Int, indexInFlexLine: Int, flexLine: FlexLine) = Unit

    override fun onNewFlexLineAdded(flexLine: FlexLine) = Unit

    override fun setFlexLines(flexLines: List<FlexLine>) {
        this.flexLines = flexLines.toMutableList()
    }

    override fun getFlexLinesInternal() = flexLines

    override fun updateViewCache(position: Int, view: View) = Unit
}
