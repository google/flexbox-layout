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
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
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
    public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        return lp instanceof LayoutParams;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }
        if (getChildCount() == 0 && state.isPreLayout()) {
            return;
        }

        mFlexboxHelper.createReorderedIndices();
        //TODO: Implement the rest of the method
    }

    /**
     * LayoutParams used by the {@link FlexboxLayoutManager}, which stores per-child information
     * required for the Flexbox.
     */
    public static class LayoutParams extends RecyclerView.LayoutParams {

        private static final int ORDER_DEFAULT = 1;

        private static final float FLEX_GROW_DEFAULT = 0f;

        private static final float FLEX_SHRINK_DEFAULT = 1f;

        public static final float FLEX_BASIS_PERCENT_DEFAULT = -1f;

        public static final int ALIGN_SELF_AUTO = -1;

        private static final int MAX_SIZE = Integer.MAX_VALUE & ViewCompat.MEASURED_SIZE_MASK;

        /**
         * This attribute can change the ordering of the children views are laid out.
         *
         * @see FlexboxLayout.LayoutParams#order
         */
        public int order = ORDER_DEFAULT;

        /**
         * This attribute determines how much this child will grow if positive free space is
         * distributed.
         *
         * @see FlexboxLayout.LayoutParams#flexGrow
         */
        public float flexGrow = FLEX_GROW_DEFAULT;

        /**
         * This attributes determines how much this child will shrink is negative free space is
         * distributed.
         *
         * @see FlexboxLayout.LayoutParams#flexShrink
         */
        public float flexShrink = FLEX_SHRINK_DEFAULT;

        /**
         * This attributes overrides the alignment along the cross axis (perpendicular to the
         * main axis).
         *
         * @see FlexboxLayout.LayoutParams#alignSelf
         */
        public int alignSelf = ALIGN_SELF_AUTO;

        /**
         * The initial flex item length in a fraction format relative to its parent.
         *
         * @see FlexboxLayout.LayoutParams#flexBasisPercent
         */
        public float flexBasisPercent = FLEX_BASIS_PERCENT_DEFAULT;

        /**
         * This attribute determines the minimum width the child can shrink to.
         *
         * @see FlexboxLayout.LayoutParams#minWidth
         */
        public int minWidth;

        /**
         * This attribute determines the minimum height the child can shrink to.
         *
         * @see FlexboxLayout.LayoutParams#minHeight
         */
        public int minHeight;

        /**
         * This attribute determines the maximum width the child can expand to.
         *
         * @see FlexboxLayout.LayoutParams#maxWidth
         */
        public int maxWidth = MAX_SIZE;

        /**
         * This attribute determines the maximum height the child can expand to.
         *
         * @see FlexboxLayout.LayoutParams#maxWidth
         */
        public int maxHeight = MAX_SIZE;

        /**
         * This attribute forces a flex line wrapping. i.e. if this is set to {@code true} for a
         * flex item, the item will become the first item of the new flex line.
         *
         * @see FlexboxLayout.LayoutParams#wrapBefore
         */
        public boolean wrapBefore;

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

            order = source.order;
            flexGrow = source.flexGrow;
            flexShrink = source.flexShrink;
            alignSelf = source.alignSelf;
            flexBasisPercent = source.flexBasisPercent;
            minWidth = source.minWidth;
            minHeight = source.minHeight;
            maxWidth = source.maxWidth;
            maxHeight = source.maxHeight;
            wrapBefore = source.wrapBefore;
        }
    }
}
