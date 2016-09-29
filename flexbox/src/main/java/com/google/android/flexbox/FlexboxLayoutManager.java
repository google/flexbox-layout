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

    /**
     * A snapshot of the {@link RecyclerView.Recycler} instance at a given moment.
     * It's not guaranteed that this instance has a reference to the latest Recycler.
     * When you want to use the latest Recycler, use the one passed as an method argument
     * (such as the one in {@link #onLayoutChildren(RecyclerView.Recycler, RecyclerView.State)})
     */
    private RecyclerView.Recycler mRecycler;

    /**
     * A snapshot of the {@link RecyclerView.State} instance at a given moment.
     * It's not guaranteed that this instance has a reference to the latest State.
     * When you want to use the latest State, use the one passed as an method argument
     * (such as the one in {@link #onLayoutChildren(RecyclerView.Recycler, RecyclerView.State)})
     */
    private RecyclerView.State mState;

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

    // From here, methods from FlexContainer
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
    public void onNewFlexItemAdded(int childAbsoluteIndex, int childRelativeIndexInFlexLine,
            FlexLine flexLine) {
        // TODO: Implement the method
    }

    /**
     * @return the number of flex items contained in the flex container.
     * This method doesn't always reflect the latest state of the adapter.
     * If you want to access the latest state of the adapter, use the {@link RecyclerView.State}
     * instance passed as an argument for some methods (such as
     * {@link #onLayoutChildren(RecyclerView.Recycler, RecyclerView.State)})
     *
     * This method is used to avoid the implementation of the similar method.
     * i.e. {@link FlexboxLayoutManager#getChildCount()} returns the child count, but it doesn't
     * include the children that are detached or scrapped.
     */
    @Override
    public int getFlexItemCount() {
        return mState.getItemCount();
    }

    /**
     * @return the flex item as a view specified as the index.
     * This method doesn't always return the latest state of the view in the adapter.
     * If you want to access the latest state, use the {@link RecyclerView.Recycler}
     * instance passed as an argument for some methods (such as
     * {@link #onLayoutChildren(RecyclerView.Recycler, RecyclerView.State)})
     *
     * This method is used to avoid the implementation of the similar method.
     * i.e. {@link FlexboxLayoutManager#getChildAt(int)} returns a view for the given index,
     * but the index is based on the layout position, not based on the adapter position, which
     * isn't desired given the usage of this method.
     */
    @Override
    public View getFlexItemAt(int index) {
        return mRecycler.getViewForPosition(index);
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
    public View getReorderedChildAt(int index) {
        if (index < 0 || index >= mFlexboxHelper.mReorderedIndices.length) {
            return null;
        }
        return mRecycler.getViewForPosition(mFlexboxHelper.mReorderedIndices[index]);
    }

    @Override
    public View getReorderedFlexItemAt(int index) {
        return getReorderedChildAt(index);
    }

    @Override
    public void onNewFlexLineAdded(FlexLine flexLine) {
        // No op
    }

    @Override
    public int getChildWidthMeasureSpec(int widthSpec, int padding, int childDimension) {
        return getChildMeasureSpec(getWidth(), getWidthMode(), padding, childDimension,
                canScrollHorizontally());
    }

    @Override
    public int getChildHeightMeasureSpec(int heightSpec, int padding, int childDimension) {
        return getChildMeasureSpec(getHeight(), getHeightMode(), padding, childDimension,
                canScrollVertically());
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

        // Assign the Recycler and the State as the member variables so that
        // the method from FlexContainer (such as getFlexItemCount()) returns the number of
        // flex items from the adapter not the child count in the LayoutManager because
        // LayoutManager#getChildCount doesn't include the views that are detached or scrapped.
        mRecycler = recycler;
        mState = state;
        if (state.getItemCount() == 0) {
            return;
        }
        int childCount = state.getItemCount();
        if (childCount == 0 && state.isPreLayout()) {
            return;
        }

        // TODO: If we support the order attribute, we need to inflate the all ViewHolders in the
        // adapter instead of inflating only the visible ViewHolders, which is inefficient given
        // that this is part of RecyclerView
        if (mFlexboxHelper.isOrderChangedFromLastMeasurement()) {
            mFlexboxHelper.mReorderedIndices = mFlexboxHelper.createReorderedIndices();
        }

        //noinspection ResourceType
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(getWidth(), getWidthMode());
        //noinspection ResourceType
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(getHeight(), getHeightMode());
        FlexboxHelper.FlexLinesResult flexLinesResult;
        mFlexLines.clear();

        // TODO: Change the code to calculate only the visible area
        if (isMainAxisDirectionHorizontal()) {
            flexLinesResult = mFlexboxHelper
                    .calculateHorizontalFlexLines(widthMeasureSpec, heightMeasureSpec);
        } else {
            flexLinesResult = mFlexboxHelper
                    .calculateVerticalFlexLines(widthMeasureSpec, heightMeasureSpec);
        }
        mFlexLines = flexLinesResult.mFlexLines;
        if (DEBUG) {
            for (int i = 0, size = mFlexLines.size(); i < size; i++) {
                FlexLine flexLine = mFlexLines.get(i);
                Log.i(TAG, String.format("%d flex line. MainSize: %d, CrossSize: %d, itemCount: %d",
                        i, flexLine.getMainSize(), flexLine.getCrossSize(),
                        flexLine.getItemCount()));
            }
        }

        // TODO: Layout the visible ViewHolders
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        removeAllViews();
    }

    private boolean isMainAxisDirectionHorizontal() {
        return mFlexDirection == FlexDirection.ROW || mFlexDirection == FlexDirection.ROW_REVERSE;
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
        public int getMarginLeft() {
            return leftMargin;
        }

        @Override
        public int getMarginTop() {
            return topMargin;
        }

        @Override
        public int getMarginRight() {
            return rightMargin;
        }

        @Override
        public int getMarginBottom() {
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
