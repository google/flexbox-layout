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

import java.util.ArrayList;
import java.util.List;

/**
 * Holds properties related to a single flex line. This class is not expected to be changed outside
 * of the {@link FlexboxLayout}, thus only exposing the getter methods that may be useful for
 * other classes using the {@link FlexboxLayout}.
 */
public class FlexLine {

    FlexLine() {
    }

    int mLeft = Integer.MAX_VALUE;

    int mTop = Integer.MAX_VALUE;

    int mRight = Integer.MIN_VALUE;

    int mBottom = Integer.MIN_VALUE;

    /** @see #getMainSize() */
    int mMainSize;

    /**
     * The sum of the lengths of dividers along the main axis. This value should be lower
     * than the value of {@link #mMainSize}.
     */
    int mDividerLengthInMainSize;

    /** @see #getCrossSize() */
    int mCrossSize;

    /** @see #getItemCount() */
    int mItemCount;

    /** Holds the count of the views whose visibilities are gone */
    int mGoneItemCount;

    /** @see #getTotalFlexGrow() */
    float mTotalFlexGrow;

    /** @see #getTotalFlexShrink() */
    float mTotalFlexShrink;

    /**
     * The largest value of the individual child's baseline (obtained by View#getBaseline()
     * if the {@link FlexContainer#getAlignItems()} value is not {@link AlignItems#BASELINE}
     * or the flex direction is vertical, this value is not used.
     * If the alignment direction is from the bottom to top,
     * (e.g. flexWrap == WRAP_REVERSE and flexDirection == ROW)
     * store this value from the distance from the bottom of the view minus baseline.
     * (Calculated as view.getMeasuredHeight() - view.getBaseline - LayoutParams.bottomMargin)
     */
    int mMaxBaseline;

    /**
     * The sum of the cross size used before this flex line.
     */
    int mSumCrossSizeBefore;

    /**
     * Store the indices of the children views whose alignSelf property is stretch.
     * The stored indices are the absolute indices including all children in the Flexbox,
     * not the relative indices in this flex line.
     */
    List<Integer> mIndicesAlignSelfStretch = new ArrayList<>();

    int mFirstIndex;

    int mLastIndex;

    /**
     * Set to true if any {@link FlexItem}s in this line have {@link FlexItem#getFlexGrow()}
     * attributes set (have the value other than {@link FlexItem#FLEX_GROW_DEFAULT})
     */
    boolean mAnyItemsHaveFlexGrow;

    /**
     * Set to true if any {@link FlexItem}s in this line have {@link FlexItem#getFlexShrink()}
     * attributes set (have the value other than {@link FlexItem#FLEX_SHRINK_NOT_SET})
     */
    boolean mAnyItemsHaveFlexShrink;

    /**
     * @return the size of the flex line in pixels along the main axis of the flex container.
     */
    public int getMainSize() {
        return mMainSize;
    }

    /**
     * @return the size of the flex line in pixels along the cross axis of the flex container.
     */
    @SuppressWarnings("WeakerAccess")
    public int getCrossSize() {
        return mCrossSize;
    }

    /**
     * @return the count of the views contained in this flex line.
     */
    @SuppressWarnings("WeakerAccess")
    public int getItemCount() {
        return mItemCount;
    }

    /**
     * @return the count of the views whose visibilities are not gone in this flex line.
     */
    @SuppressWarnings("WeakerAccess")
    public int getItemCountNotGone() {
        return mItemCount - mGoneItemCount;
    }

    /**
     * @return the sum of the flexGrow properties of the children included in this flex line
     */
    @SuppressWarnings("WeakerAccess")
    public float getTotalFlexGrow() {
        return mTotalFlexGrow;
    }

    /**
     * @return the sum of the flexShrink properties of the children included in this flex line
     */
    @SuppressWarnings("WeakerAccess")
    public float getTotalFlexShrink() {
        return mTotalFlexShrink;
    }

    /**
     * @return the first view's index included in this flex line.
     */
    public int getFirstIndex() {
        return mFirstIndex;
    }

    /**
     * Updates the position of the flex line from the contained view.
     *
     * @param view             the view contained in this flex line
     * @param leftDecoration   the length of the decoration on the left of the view
     * @param topDecoration    the length of the decoration on the top of the view
     * @param rightDecoration  the length of the decoration on the right of the view
     * @param bottomDecoration the length of the decoration on the bottom of the view
     */
    void updatePositionFromView(View view, int leftDecoration, int topDecoration,
            int rightDecoration, int bottomDecoration) {
        FlexItem flexItem = (FlexItem) view.getLayoutParams();
        mLeft = Math.min(mLeft, view.getLeft() - flexItem.getMarginLeft() - leftDecoration);
        mTop = Math.min(mTop, view.getTop() - flexItem.getMarginTop() - topDecoration);
        mRight = Math.max(mRight, view.getRight() + flexItem.getMarginRight() + rightDecoration);
        mBottom = Math
                .max(mBottom, view.getBottom() + flexItem.getMarginBottom() + bottomDecoration);
    }
}
