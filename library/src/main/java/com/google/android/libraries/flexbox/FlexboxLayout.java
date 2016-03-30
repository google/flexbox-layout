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

package com.google.android.libraries.flexbox;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A layout that arranges its children in a way its attributes can be specified like
 * CSS Flexible Box Layout Module.
 */
public class FlexboxLayout extends ViewGroup {

    @IntDef({FLEX_DIRECTION_ROW, FLEX_DIRECTION_ROW_REVERSE, FLEX_DIRECTION_COLUMN,
            FLEX_DIRECTION_COLUMN_REVERSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FlexDirection {}
    public static final int FLEX_DIRECTION_ROW = 0;
    public static final int FLEX_DIRECTION_ROW_REVERSE  = 1;
    public static final int FLEX_DIRECTION_COLUMN = 2;
    public static final int FLEX_DIRECTION_COLUMN_REVERSE  = 3;

    /**
     * The direction children items are placed inside the Flexbox layout.
     * Default value is {@link #FLEX_DIRECTION_ROW}.
     */
    private int mFlexDirection;


    @IntDef({FLEX_WRAP_NOWRAP, FLEX_WRAP_WRAP, FLEX_WRAP_WRAP_REVERSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FlexWrap {}
    public static final int FLEX_WRAP_NOWRAP = 0;
    public static final int FLEX_WRAP_WRAP  = 1;
    public static final int FLEX_WRAP_WRAP_REVERSE = 2;

    private int mFlexWrap;


    @IntDef({JUSTIFY_CONTENT_FLEX_START, JUSTIFY_CONTENT_FLEX_END, JUSTIFY_CONTENT_CENTER,
            JUSTIFY_CONTENT_SPACE_BETWEEN, JUSTIFY_CONTENT_SPACE_AROUND})
    @Retention(RetentionPolicy.SOURCE)
    public @interface JustifyContent {}
    public static final int JUSTIFY_CONTENT_FLEX_START = 0;
    public static final int JUSTIFY_CONTENT_FLEX_END = 1;
    public static final int JUSTIFY_CONTENT_CENTER = 2;
    public static final int JUSTIFY_CONTENT_SPACE_BETWEEN = 3;
    public static final int JUSTIFY_CONTENT_SPACE_AROUND = 4;

    private int mJustifyContent;


    @IntDef({ALIGN_ITEMS_FLEX_START, ALIGN_ITEMS_FLEX_END, ALIGN_ITEMS_CENTER,
            ALIGN_ITEMS_BASELINE, ALIGN_ITEMS_STRETCH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AlignItems {}
    public static final int ALIGN_ITEMS_FLEX_START = 0;
    public static final int ALIGN_ITEMS_FLEX_END = 1;
    public static final int ALIGN_ITEMS_CENTER = 2;
    public static final int ALIGN_ITEMS_BASELINE = 3;
    public static final int ALIGN_ITEMS_STRETCH = 4;

    private int mAlignItems;


    @IntDef({ALIGN_CONTENT_FLEX_START, ALIGN_CONTENT_FLEX_END, ALIGN_CONTENT_CENTER,
            ALIGN_CONTENT_SPACE_BETWEEN, ALIGN_CONTENT_SPACE_AROUND, ALIGN_CONTENT_STRETCH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AlignContent {}
    public static final int ALIGN_CONTENT_FLEX_START = 0;
    public static final int ALIGN_CONTENT_FLEX_END = 1;
    public static final int ALIGN_CONTENT_CENTER = 2;
    public static final int ALIGN_CONTENT_SPACE_BETWEEN = 3;
    public static final int ALIGN_CONTENT_SPACE_AROUND = 4;
    public static final int ALIGN_CONTENT_STRETCH = 5;

    private int mAlignContent;

    public FlexboxLayout(Context context) {
        this(context, null);
    }

    public FlexboxLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlexboxLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.FlexboxLayout, defStyleAttr, 0);
        mFlexDirection = a.getInt(R.styleable.FlexboxLayout_flexDirection, FLEX_DIRECTION_ROW);
        mFlexWrap = a.getInt(R.styleable.FlexboxLayout_flexWrap, FLEX_WRAP_NOWRAP);
        mJustifyContent = a
                .getInt(R.styleable.FlexboxLayout_justifyContent, JUSTIFY_CONTENT_FLEX_START);
        mAlignItems = a.getInt(R.styleable.FlexboxLayout_alignItems, ALIGN_ITEMS_STRETCH);
        mAlignContent = a.getInt(R.styleable.FlexboxLayout_alignContent, ALIGN_CONTENT_STRETCH);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // TODO: Implement onMeasure
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO: Implement onLayout
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof FlexboxLayout.LayoutParams;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new FlexboxLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
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

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {

        private static final int ORDER_DEFAULT = 1;
        private static final int FLEX_GROW_DEFAULT = 0;
        private static final int FLEX_SHRINK_DEFAULT = 1;

        public static final int ALIGN_SELF_AUTO = -1;
        public static final int ALIGN_SELF_FLEX_START = ALIGN_ITEMS_FLEX_START;
        public static final int ALIGN_SELF_FLEX_END = ALIGN_ITEMS_FLEX_END;
        public static final int ALIGN_SELF_CENTER = ALIGN_ITEMS_CENTER;
        public static final int ALIGN_SELF_BASELINE = ALIGN_ITEMS_BASELINE;
        public static final int ALIGN_SELF_STRETCH = ALIGN_ITEMS_STRETCH;

        public int order = ORDER_DEFAULT;
        public int flexGrow = FLEX_GROW_DEFAULT;
        public int flexShrink = FLEX_SHRINK_DEFAULT;
        public int alignSelf = ALIGN_SELF_AUTO;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);

            TypedArray a = context
                    .obtainStyledAttributes(attrs, R.styleable.FlexboxLayout_Layout);
            order = a.getInt(R.styleable.FlexboxLayout_Layout_layout_order, ORDER_DEFAULT);
            flexGrow = a
                    .getInt(R.styleable.FlexboxLayout_Layout_layout_flexGrow, FLEX_GROW_DEFAULT);
            flexShrink = a.getInt(R.styleable.FlexboxLayout_Layout_layout_flexShrink,
                    FLEX_SHRINK_DEFAULT);
            alignSelf = a
                    .getInt(R.styleable.FlexboxLayout_Layout_layout_alignSelf, ALIGN_SELF_AUTO);
            a.recycle();
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}
