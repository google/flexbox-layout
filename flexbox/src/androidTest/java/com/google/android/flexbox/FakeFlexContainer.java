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

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Fake implementation of {@link FlexContainer}.
 */
class FakeFlexContainer implements FlexContainer {

    private List<View> mViews = new ArrayList<>();

    private List<FlexLine> mFlexLines = new ArrayList<>();

    @FlexDirection
    private int mFlexDirection = FlexDirection.ROW;

    @FlexWrap
    private int mFlexWrap = FlexWrap.WRAP;

    @JustifyContent
    private int mJustifyContent = JustifyContent.FLEX_START;

    @AlignItems
    private int mAlignItems = AlignItems.STRETCH;

    @AlignContent
    private int mAlignContent = AlignContent.STRETCH;

    @Override
    public int getFlexItemCount() {
        return mViews.size();
    }

    @Override
    public View getFlexItemAt(int index) {
        return mViews.get(index);
    }

    @Override
    public View getReorderedFlexItemAt(int index) {
        return mViews.get(index);
    }

    @Override
    public void addView(View view) {
        mViews.add(view);
    }

    @Override
    public void addView(View view, int index) {
        mViews.add(index, view);
    }

    @Override
    public void removeAllViews() {
        mViews.clear();
    }

    @Override
    public void removeViewAt(int index) {
        mViews.remove(index);
    }

    @Override
    public int getFlexDirection() {
        return mFlexDirection;
    }

    @Override
    public void setFlexDirection(@FlexDirection int flexDirection) {
        mFlexDirection = flexDirection;
    }

    @Override
    public int getFlexWrap() {
        return mFlexWrap;
    }

    @Override
    public void setFlexWrap(@FlexWrap int flexWrap) {
        mFlexWrap = flexWrap;
    }

    @Override
    public int getJustifyContent() {
        return mJustifyContent;
    }

    @Override
    public void setJustifyContent(@JustifyContent int justifyContent) {
        mJustifyContent = justifyContent;
    }

    @Override
    public int getAlignContent() {
        return mAlignContent;
    }

    @Override
    public void setAlignContent(@AlignContent int alignContent) {
        mAlignContent = alignContent;
    }

    @Override
    public int getAlignItems() {
        return mAlignItems;
    }

    @Override
    public void setAlignItems(@AlignItems int alignItems) {
        mAlignItems = alignItems;
    }

    @Override
    public List<FlexLine> getFlexLines() {
        return mFlexLines;
    }

    @Override
    public int getDecorationLength(int childAbsoluteIndex, int childRelativeIndexInFlexLine,
            FlexItem flexItem) {
        return 0;
    }

    @Override
    public int getPaddingTop() {
        return 0;
    }

    @Override
    public int getPaddingLeft() {
        return 0;
    }

    @Override
    public int getPaddingRight() {
        return 0;
    }

    @Override
    public int getPaddingBottom() {
        return 0;
    }

    @Override
    public int getPaddingStart() {
        return 0;
    }

    @Override
    public int getPaddingEnd() {
        return 0;
    }

    @Override
    public int getChildWidthMeasureSpec(int widthSpec, int padding, int childDimension) {
        return ViewGroup.getChildMeasureSpec(widthSpec, padding, childDimension);
    }

    @Override
    public int getChildHeightMeasureSpec(int heightSpec, int padding, int childDimension) {
        return ViewGroup.getChildMeasureSpec(heightSpec, padding, childDimension);
    }

    @Override
    public int getLargestMainSize() {
        int largestSize = Integer.MIN_VALUE;
        for (FlexLine flexLine : mFlexLines) {
            largestSize = Math.max(largestSize, flexLine.mMainSize);
        }
        return largestSize;
    }

    @Override
    public int getSumOfCrossSize() {
        int sum = 0;
        for (int i = 0, size = mFlexLines.size(); i < size; i++) {
            FlexLine flexLine = mFlexLines.get(i);
            sum += flexLine.mCrossSize;
        }
        return sum;
    }

    @Override
    public void onNewFlexItemAdded(int childAbsoluteIndex, int childRelativeIndexInFlexLine,
            FlexLine flexLine) {
        // No op
    }

    @Override
    public void onNewFlexLineAdded(FlexLine flexLine) {
        // No op
    }

    @Override
    public void setFlexLines(List<FlexLine> flexLines) {
        mFlexLines = flexLines;
    }

    @Override
    public List<FlexLine> getFlexLinesInternal() {
        return mFlexLines;
    }
}
