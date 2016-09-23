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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * LayoutManager for the {@link RecyclerView}. This class is intended to be used within a
 * {@link RecyclerView} and offers the same capabilities of measure/layout its children
 * as the {@link FlexboxLayout}.
 */
public class FlexboxLayoutManager extends RecyclerView.LayoutManager implements FlexContainer {

    private static final String TAG = "FlexboxLayoutManager";

    private static final int POSITION_INVALID = -1;

    private static final boolean DEBUG = false;

    /**
     * The current value of the {@link FlexDirection}, the default value is {@link
     * FlexDirection#ROW}.
     */
    private int mFlexDirection;

    /**
     * The current value of the {@link FlexWrap}, the default value is {@link FlexWrap#WRAP}.
     */
    private int mFlexWrap;

    /**
     * The current value of the {@link JustifyContent}, the default value is
     * {@link JustifyContent#FLEX_START}.
     */
    private int mJustifyContent;

    /**
     * The current value of the {@link AlignItems}, the default value is
     * {@link AlignItems#STRETCH}.
     */
    private int mAlignItems;

    /**
     * The current value of the {@link AlignContent}, the default value is
     * {@link AlignContent#STRETCH}.
     */
    private int mAlignContent;

    private List<FlexLine> mFlexLines = new ArrayList<>();

    private final FlexboxHelper mFlexboxHelper = new FlexboxHelper(this);

    private int mFirstVisibleX = 0;

    private int mFirstVisibleY = 0;

    /**
     * Creates a default FlexboxLayoutManager.
     */
    public FlexboxLayoutManager() {
        this(FlexDirection.ROW, FlexWrap.WRAP);
    }

    /**
     * Creates a FlexboxLayoutManager with the flexDirection specified.
     *
     * @param flexDirection the flex direction attribute
     */
    public FlexboxLayoutManager(@FlexDirection int flexDirection) {
        this(flexDirection, FlexWrap.WRAP);
    }

    /**
     * Creates a FlexboxLayoutManager with the flexDirection and flexWrap attributes specified.
     *
     * @param flexDirection the flex direction attribute
     * @param flexWrap      the flex wrap attribute
     */
    public FlexboxLayoutManager(@FlexDirection int flexDirection,
            @FlexWrap int flexWrap) {
        setFlexDirection(flexDirection);
        setFlexWrap(flexWrap);
        setAutoMeasureEnabled(true);
    }

    /**
     * Constructor used when layout manager is set in XML by RecyclerView attribute
     * "layoutManager". No corresponding attributes for the {@code orientation},
     * {@code reverseLayout} and {@code stackFromEnd} exist in Flexbox, thus map the similar
     * attributes from Flexbox that behave similarly for each of them.
     *
     * {@code android:orientation} maps to the {@link FlexDirection},
     * HORIZONTAL -> {@link FlexDirection#ROW}, VERTICAL -> {@link FlexDirection#COLUMN}.
     *
     * {@code android.support.v7.recyclerview:reverseLayout} reverses the direction of the
     * {@link FlexDirection}, i.e. if reverseLayout is {@code true}, {@link FlexDirection#ROW} is
     * changed to {@link FlexDirection#ROW_REVERSE}. Similarly {@link FlexDirection#COLUMN} is
     * changed to {@link FlexDirection#COLUMN_REVERSE}.
     *
     * {@code android.support.v7.recyclerview:stackFromEnd} maps to the {@link FlexWrap},
     * if stackFromEnd is set to {@code true} -> {@link FlexWrap#WRAP_REVERSE} otherwise ->
     * {@link FlexWrap#WRAP}.
     */
    public FlexboxLayoutManager(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        Properties properties = getProperties(context, attrs, defStyleAttr, defStyleRes);
        switch (properties.orientation) {
            case LinearLayoutManager.HORIZONTAL:
                if (properties.reverseLayout) {
                    setFlexDirection(FlexDirection.ROW_REVERSE);
                } else {
                    setFlexDirection(FlexDirection.ROW);
                }
                break;
            case LinearLayoutManager.VERTICAL:
                if (properties.reverseLayout) {
                    setFlexDirection(FlexDirection.COLUMN_REVERSE);
                } else {
                    setFlexDirection(FlexDirection.COLUMN);
                }
                break;
        }
        if (properties.stackFromEnd) {
            setFlexWrap(FlexWrap.WRAP_REVERSE);
        } else {
            setFlexWrap(FlexWrap.WRAP);
        }
        setAutoMeasureEnabled(true);
    }

    // Methods from FlexContainer
    @FlexDirection
    @Override
    public int getFlexDirection() {
        return mFlexDirection;
    }

    @Override
    public void setFlexDirection(@FlexDirection int flexDirection) {
        if (mFlexDirection != flexDirection) {
            mFlexDirection = flexDirection;
            requestLayout();
        }
    }

    @Override
    @FlexWrap
    public int getFlexWrap() {
        return mFlexWrap;
    }

    @Override
    public void setFlexWrap(@FlexWrap int flexWrap) {
        if (mFlexWrap != flexWrap) {
            mFlexWrap = flexWrap;
            requestLayout();
        }
    }

    @JustifyContent
    @Override
    public int getJustifyContent() {
        return mJustifyContent;
    }

    @Override
    public void setJustifyContent(@JustifyContent int justifyContent) {
        if (mJustifyContent != justifyContent) {
            mJustifyContent = justifyContent;
            requestLayout();
        }
    }

    @AlignItems
    @Override
    public int getAlignItems() {
        return mAlignItems;
    }

    @Override
    public void setAlignItems(@AlignItems int alignItems) {
        if (mAlignItems != alignItems) {
            mAlignItems = alignItems;
            requestLayout();
        }
    }

    @AlignContent
    @Override
    public int getAlignContent() {
        return mAlignContent;
    }

    @Override
    public void setAlignContent(@AlignContent int alignContent) {
        if (mAlignContent != alignContent) {
            mAlignContent = alignContent;
            requestLayout();
        }
    }

    @Override
    public List<FlexLine> getFlexLines() {
        List<FlexLine> result = new ArrayList<>(mFlexLines.size());
        for (FlexLine flexLine : mFlexLines) {
            if (flexLine.getItemCount() == 0) {
                continue;
            }
            result.add(flexLine);
        }
        return result;
    }

    @Override
    public int getDecorationLength(int childAbsoluteIndex, int childRelativeIndexInFlexLine,
            FlexItem flexItem) {
        // TODO: Implement the method
        return 0;
    }

    @Override
    public int getHorizontalChildMeasureSpec(int widthSpec, int padding, int childDimension) {
        return getChildMeasureSpec(getWidth(), widthSpec, padding, childDimension,
                canScrollHorizontally());
    }

    @Override
    public int getVerticalChildMeasureSpec(int heightSpec, int padding, int childDimension) {
        return getChildMeasureSpec(getHeight(), heightSpec, padding, childDimension,
                canScrollVertically());
    }

    @Override
    public void onNewFlexItemAdded(int childAbsoluteIndex, int childRelativeIndexInFlexLine,
            FlexLine flexLine) {
        // TODO: Implement the method
    }

    @Override
    public void onNewFlexLineAdded(FlexLine flexLine) {
        // No op
    }
    // The end of methods from FlexContainer

    private int getLargestMainSize() {
        int largest = 0;
        for (int i = 0, size = mFlexLines.size(); i < size; i++) {
            FlexLine flexLine = mFlexLines.get(i);
            largest = Math.max(largest, flexLine.getMainSize());
        }
        return largest;
    }

    private int getSumOfCrossSize() {
        int sum = 0;
        for (int i = 0, size = mFlexLines.size(); i < size; i++) {
            FlexLine flexLine = mFlexLines.get(i);
            sum += flexLine.mCrossSize;
        }
        return sum;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return new LayoutParams(c, attrs);
    }

    @Override
    public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        return lp instanceof LayoutParams;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (DEBUG) {
            Log.i(TAG,
                    String.format("onLayoutChildren. recycler.getScrapList.size(): %s, state: %s",
                            recycler.getScrapList().size(), state));
            Log.i(TAG, String.format("getChildCount: %d", getChildCount()));
        }

        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }
        int childCount = getChildCount();
        if (childCount == 0 && state.isPreLayout()) {
            return;
        }

        mFlexLines.clear();
        mFlexboxHelper.mReorderedIndices = mFlexboxHelper.createReorderedIndices();

        if (getChildCount() == 0) {
            View view = recycler.getViewForPosition(0);
            Log.i(TAG, String.format("recycler.getViewForPosition(0): %s",
                    recycler.getViewForPosition(0)));
            addView(view);
            measureChildWithMargins(view, 0, 0);
            mFirstVisibleX = 0;
            mFirstVisibleY = 0;
        }
        detachAndScrapAttachedViews(recycler);

        fill(recycler, state, 0);

        //TODO: Implement the rest of the method
    }

    private void fill(RecyclerView.Recycler recycler, RecyclerView.State state, int startOffset) {
        Log.i(TAG, String.format("mFirstVisibleX: %d", mFirstVisibleX));

        int childLeft = 0;
        int childTop = 0;
        if (getChildCount() != 0) {
            View firstView = recycler.getViewForPosition(0);
            childLeft = getDecoratedLeft(firstView);
        }

        // TODO: Consider multiple flex lines.
        int recycleStartAdapterIndex = getPositionFor(0, startOffset);
        int recycleConsumed = 0;
        while (recycleConsumed >= getWidth()) {
            // TODO: Consider vertical direction
            View view = getReorderedChildAt(recycleStartAdapterIndex);
            recycleConsumed += view.getWidth();
            detachView(view);
            recycleStartAdapterIndex++;
        }

        FlexLine flexLine = new FlexLine();
        for (int i = 0; i < state.getItemCount(); i++) {
            View view = recycler.getViewForPosition(i);
            measureChildWithMargins(view, 0, 0);
            addView(view);

            flexLine.mMainSize += view.getMeasuredWidth();
            flexLine.mCrossSize = Math.max(flexLine.mCrossSize, view.getMeasuredHeight());

            layoutDecorated(view, childLeft, childTop, childLeft + view.getMeasuredWidth(),
                    childTop + view.getMeasuredHeight());
            childLeft += view.getMeasuredWidth();
        }
        mFlexLines.add(flexLine);

        Log.i(TAG, String.format("FlexLine mainSize: %d", mFlexLines.get(0).getMainSize()));
    }

    /**
     * Returns a View, which is reordered by taking {@link LayoutParams#mOrder} parameters
     * into account.
     *
     * @param index the index of the view
     * @return the reordered view, which {@link LayoutParams@mOrder} is taken into account.
     * If the index is negative or out of bounds of the number of contained views,
     * returns {@code null}.
     */
    @Override
    public View getReorderedChildAt(int index) {
        if (index < 0 || index >= mFlexboxHelper.mReorderedIndices.length) {
            return null;
        }
        return getChildAt(mFlexboxHelper.mReorderedIndices[index]);
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler,
            RecyclerView.State state) {
        Log.i(TAG, String.format("scrollHorizontallyBy dx: %d, state: %s", dx, state));
        if (getChildCount() == 0 || dx == 0) {
            return 0;
        }

        int horizontalSize;
        int width = getWidth() - getPaddingRight() - getPaddingLeft();
        if (isMainAxisDirectionHorizontal()) {
            horizontalSize = getLargestMainSize();
        } else {
            horizontalSize = getSumOfCrossSize();
        }

        int deltaX = dx;
        if (dx > 0) {
            // Scrolling to left
            if (horizontalSize - mFirstVisibleX - dx < width) {
                deltaX = Math.max(0, horizontalSize - mFirstVisibleX - width);
            }
            mFirstVisibleX += deltaX;
            offsetChildrenHorizontal(-deltaX);
            fill(recycler, state, mFirstVisibleX);
        } else {
            // Scrolling to right
            mFirstVisibleX = Math.max(mFirstVisibleX + dx, 0);
            if (mFirstVisibleX + dx < 0) {
                deltaX = mFirstVisibleX;
            }
            offsetChildrenHorizontal(-deltaX);
            fill(recycler, state, mFirstVisibleX);
        }
        Log.i(TAG, String.format("mFirstVisibleX: %d", mFirstVisibleX));
        return deltaX;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler,
            RecyclerView.State state) {
        Log.i(TAG, String.format("scrollVerticallyBy dy: %d, state: %s", dy, state));

        return dy;
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        removeAllViews();
    }

    private boolean isMainAxisDirectionHorizontal() {
        return mFlexDirection == FlexDirection.ROW || mFlexDirection == FlexDirection.ROW_REVERSE;
    }

    /**
     * Returns the adapter position for the given flexLineIndex and the startOffSet.
     */
    private int getPositionFor(int flexLineIndex, int startOffset) {
        if (flexLineIndex < 0 || flexLineIndex >= mFlexLines.size()) {
            return POSITION_INVALID;
        }
        FlexLine flexLine = mFlexLines.get(flexLineIndex);
        if (flexLine.getItemCount() == 0) {
            return POSITION_INVALID;
        }

        int adapterIndex = 0;
        for (int i = 0; i < flexLineIndex - 1; i++) {
            adapterIndex += mFlexLines.get(i).getItemCount();
        }

        for (int i = 0, itemCount = flexLine.getItemCount(); i < itemCount; i++) {
            View view = getReorderedChildAt(adapterIndex + i);
            // TODO: Consider the vertical direction
            if (view.getWidth() >= startOffset) {
                return adapterIndex + i;
            }
            startOffset -= view.getWidth();
        }
        return POSITION_INVALID;
    }

    /**
     * LayoutParams used by the {@link FlexboxLayoutManager}, which stores per-child information
     * required for the Flexbox.
     *
     * Note that some parent fields (which are not primitive nor a class implements
     * {@link Parcelable}) are not included as the stored/restored fields after this class
     * is serialized/de-serialized as an {@link Parcelable}.
     */
    public static class LayoutParams extends RecyclerView.LayoutParams implements FlexItem {

        /**
         * @see FlexItem#getOrder()
         */
        private int mOrder = FlexItem.ORDER_DEFAULT;

        /**
         * @see FlexItem#getFlexGrow()
         */
        private float mFlexGrow = FlexItem.FLEX_GROW_DEFAULT;

        /**
         * @see FlexItem#getFlexShrink()
         */
        private float mFlexShrink = FlexItem.FLEX_SHRINK_DEFAULT;

        /**
         * @see FlexItem#getAlignSelf()
         */
        private int mAlignSelf = AlignSelf.AUTO;

        /**
         * @see FlexItem#getFlexBasisPercent()
         */
        private float mFlexBasisPercent = FlexItem.FLEX_BASIS_PERCENT_DEFAULT;

        /**
         * @see FlexItem#getMinWidth()
         */
        private int mMinWidth;

        /**
         * @see FlexItem#getMinHeight()
         */
        private int mMinHeight;

        /**
         * @see FlexItem#getMaxWidth()
         */
        private int mMaxWidth = MAX_SIZE;

        /**
         * @see FlexItem#getMaxHeight()
         */
        private int mMaxHeight = MAX_SIZE;

        /**
         * @see FlexItem#isWrapBefore()
         */
        private boolean mWrapBefore;

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public void setWidth(int width) {
            this.width = width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public void setHeight(int height) {
            this.height = height;
        }

        @Override
        public float getFlexGrow() {
            return mFlexGrow;
        }

        @Override
        public void setFlexGrow(float flexGrow) {
            this.mFlexGrow = flexGrow;
        }

        @Override
        public float getFlexShrink() {
            return mFlexShrink;
        }

        @Override
        public void setFlexShrink(float flexShrink) {
            this.mFlexShrink = flexShrink;
        }

        @AlignSelf
        @Override
        public int getAlignSelf() {
            return mAlignSelf;
        }

        @Override
        public void setAlignSelf(@AlignSelf int alignSelf) {
            this.mAlignSelf = alignSelf;
        }

        @Override
        public int getMinWidth() {
            return mMinWidth;
        }

        @Override
        public void setMinWidth(int minWidth) {
            this.mMinWidth = minWidth;
        }

        @Override
        public int getMinHeight() {
            return mMinHeight;
        }

        @Override
        public void setMinHeight(int minHeight) {
            this.mMinHeight = minHeight;
        }

        @Override
        public int getMaxWidth() {
            return mMaxWidth;
        }

        @Override
        public void setMaxWidth(int maxWidth) {
            this.mMaxWidth = maxWidth;
        }

        @Override
        public int getMaxHeight() {
            return mMaxHeight;
        }

        @Override
        public void setMaxHeight(int maxHeight) {
            this.mMaxHeight = maxHeight;
        }

        @Override
        public boolean isWrapBefore() {
            return mWrapBefore;
        }

        @Override
        public void setWrapBefore(boolean wrapBefore) {
            this.mWrapBefore = wrapBefore;
        }

        @Override
        public float getFlexBasisPercent() {
            return mFlexBasisPercent;
        }

        @Override
        public void setFlexBasisPercent(float flexBasisPercent) {
            this.mFlexBasisPercent = flexBasisPercent;
        }

        @Override
        public int getLeftMargin() {
            return leftMargin;
        }

        @Override
        public int getTopMargin() {
            return topMargin;
        }

        @Override
        public int getRightMargin() {
            return rightMargin;
        }

        @Override
        public int getBottomMargin() {
            return bottomMargin;
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(RecyclerView.LayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super(source);

            mOrder = source.mOrder;
            mFlexGrow = source.mFlexGrow;
            mFlexShrink = source.mFlexShrink;
            mAlignSelf = source.mAlignSelf;
            mFlexBasisPercent = source.mFlexBasisPercent;
            mMinWidth = source.mMinWidth;
            mMinHeight = source.mMinHeight;
            mMaxWidth = source.mMaxWidth;
            mMaxHeight = source.mMaxHeight;
            mWrapBefore = source.mWrapBefore;
        }

        @Override
        public int getOrder() {
            return mOrder;
        }

        @Override
        public void setOrder(int order) {
            mOrder = order;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mOrder);
            dest.writeFloat(this.mFlexGrow);
            dest.writeFloat(this.mFlexShrink);
            dest.writeInt(this.mAlignSelf);
            dest.writeFloat(this.mFlexBasisPercent);
            dest.writeInt(this.mMinWidth);
            dest.writeInt(this.mMinHeight);
            dest.writeInt(this.mMaxWidth);
            dest.writeInt(this.mMaxHeight);
            dest.writeByte(this.mWrapBefore ? (byte) 1 : (byte) 0);
            dest.writeInt(this.bottomMargin);
            dest.writeInt(this.leftMargin);
            dest.writeInt(this.rightMargin);
            dest.writeInt(this.topMargin);
            dest.writeInt(this.height);
            dest.writeInt(this.width);
        }

        protected LayoutParams(Parcel in) {
            super(WRAP_CONTENT, WRAP_CONTENT);
            this.mOrder = in.readInt();
            this.mFlexGrow = in.readFloat();
            this.mFlexShrink = in.readFloat();
            this.mAlignSelf = in.readInt();
            this.mFlexBasisPercent = in.readFloat();
            this.mMinWidth = in.readInt();
            this.mMinHeight = in.readInt();
            this.mMaxWidth = in.readInt();
            this.mMaxHeight = in.readInt();
            this.mWrapBefore = in.readByte() != 0;
            this.bottomMargin = in.readInt();
            this.leftMargin = in.readInt();
            this.rightMargin = in.readInt();
            this.topMargin = in.readInt();
            this.height = in.readInt();
            this.width = in.readInt();
        }

        public static final Parcelable.Creator<LayoutParams> CREATOR
                = new Parcelable.Creator<LayoutParams>() {
            @Override
            public LayoutParams createFromParcel(Parcel source) {
                return new LayoutParams(source);
            }

            @Override
            public LayoutParams[] newArray(int size) {
                return new LayoutParams[size];
            }
        };
    }
}
