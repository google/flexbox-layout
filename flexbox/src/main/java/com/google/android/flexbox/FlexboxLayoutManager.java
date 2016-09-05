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
import android.view.View;
import android.view.ViewGroup;

/**
 * LayoutManager for the {@link RecyclerView}. This class is intended to be used within a
 * {@link RecyclerView} and offers the same capabilities of measure/layout its children
 * as the {@link FlexboxLayout}.
 */
public class FlexboxLayoutManager extends RecyclerView.LayoutManager implements FlexContainer {

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

    private final FlexboxHelper mFlexboxHelper = new FlexboxHelper(this);

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

    @FlexDirection
    public int getFlexDirection() {
        return mFlexDirection;
    }

    public void setFlexDirection(@FlexDirection int flexDirection) {
        if (mFlexDirection != flexDirection) {
            mFlexDirection = flexDirection;
            requestLayout();
        }
    }

    @FlexWrap
    public int getFlexWrap() {
        return mFlexWrap;
    }

    public void setFlexWrap(@FlexWrap int flexWrap) {
        if (mFlexWrap != flexWrap) {
            mFlexWrap = flexWrap;
            requestLayout();
        }
    }

    @JustifyContent
    public int getJustifyContent() {
        return mJustifyContent;
    }

    public void setJustifyContent(@JustifyContent int justifyContent) {
        if (mJustifyContent != justifyContent) {
            mJustifyContent = justifyContent;
            requestLayout();
        }
    }

    @AlignItems
    public int getAlignItems() {
        return mAlignItems;
    }

    public void setAlignItems(@AlignItems int alignItems) {
        if (mAlignItems != alignItems) {
            mAlignItems = alignItems;
            requestLayout();
        }
    }

    @AlignContent
    public int getAlignContent() {
        return mAlignContent;
    }

    public void setAlignContent(@AlignContent int alignContent) {
        if (mAlignContent != alignContent) {
            mAlignContent = alignContent;
            requestLayout();
        }
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
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }
        int childCount = getChildCount();
        if (childCount == 0 && state.isPreLayout()) {
            return;
        }

        mFlexboxHelper.createReorderedIndices();
        //TODO: Implement the rest of the method
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
        return getChildAt(mFlexboxHelper.mReorderedIndices[index]);
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
