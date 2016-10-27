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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A layout that arranges its children in a way its attributes can be specified like the
 * CSS Flexible Box Layout Module.
 * This class extends the {@link ViewGroup} like other layout classes such as {@link LinearLayout}
 * or {@link RelativeLayout}, the attributes can be specified from a layout XML or from code.
 *
 * The supported attributes that you can use are:
 * <ul>
 * <li>{@code flexDirection}</li>
 * <li>{@code flexWrap}</li>
 * <li>{@code justifyContent}</li>
 * <li>{@code alignItems}</li>
 * <li>{@code alignContent}</li>
 * <li>{@code showDivider}</li>
 * <li>{@code showDividerHorizontal}</li>
 * <li>{@code showDividerVertical}</li>
 * <li>{@code dividerDrawable}</li>
 * <li>{@code dividerDrawableHorizontal}</li>
 * <li>{@code dividerDrawableVertical}</li>
 * </ul>
 * for the FlexboxLayout.
 *
 * And for the children of the FlexboxLayout, you can use:
 * <ul>
 * <li>{@code layout_order}</li>
 * <li>{@code layout_flexGrow}</li>
 * <li>{@code layout_flexShrink}</li>
 * <li>{@code layout_flexBasisPercent}</li>
 * <li>{@code layout_alignSelf}</li>
 * <li>{@code layout_minWidth}</li>
 * <li>{@code layout_minHeight}</li>
 * <li>{@code layout_maxWidth}</li>
 * <li>{@code layout_maxHeight}</li>
 * <li>{@code layout_wrapBefore}</li>
 * </ul>
 */
public class FlexboxLayout extends ViewGroup implements FlexContainer {

    //TODO: Extracting the interfaces to independent classes is a breaking change. Update the
    //      document to notify that.

    /**
     * The current value of the {@link FlexDirection}, the default value is {@link
     * FlexDirection#ROW}.
     *
     * @see {@link FlexDirection}
     */
    private int mFlexDirection;

    /**
     * The current value of the {@link FlexWrap}, the default value is {@link FlexWrap#NOWRAP}.
     *
     * @see {@link FlexWrap}
     */
    private int mFlexWrap;

    /**
     * The current value of the {@link JustifyContent}, the default value is
     * {@link JustifyContent#FLEX_START}.
     *
     * @see {@link JustifyContent}
     */
    private int mJustifyContent;

    /**
     * The current value of the {@link AlignItems}, the default value is
     * {@link AlignItems#STRETCH}.
     *
     * @see {@link AlignItems}
     */
    private int mAlignItems;

    /**
     * The current value of the {@link AlignContent}, the default value is
     * {@link AlignContent#STRETCH}.
     *
     * @see {@link AlignContent}
     */
    private int mAlignContent;

    /**
     * The int definition to be used as the arguments for the {@link #setShowDivider(int)},
     * {@link #setShowDividerHorizontal(int)} or {@link #setShowDividerVertical(int)}.
     * One or more of the values (such as
     * {@link #SHOW_DIVIDER_BEGINNING} | {@link #SHOW_DIVIDER_MIDDLE}) can be passed to those set
     * methods.
     */
    @IntDef(flag = true,
            value = {
                    SHOW_DIVIDER_NONE,
                    SHOW_DIVIDER_BEGINNING,
                    SHOW_DIVIDER_MIDDLE,
                    SHOW_DIVIDER_END
            })
    @Retention(RetentionPolicy.SOURCE)
    public @interface DividerMode {

    }

    /** Constant to how no dividers */
    public static final int SHOW_DIVIDER_NONE = 0;

    /** Constant to show a divider at the beginning of the flex lines (or flex items). */
    public static final int SHOW_DIVIDER_BEGINNING = 1;

    /** Constant to show dividers between flex lines or flex items. */
    public static final int SHOW_DIVIDER_MIDDLE = 1 << 1;

    /** Constant to show a divider at the end of the flex lines or flex items. */
    public static final int SHOW_DIVIDER_END = 1 << 2;

    /** The drawable to be drawn for the horizontal dividers. */
    private Drawable mDividerDrawableHorizontal;

    /** The drawable to be drawn for the vertical dividers. */
    private Drawable mDividerDrawableVertical;

    /**
     * Indicates the divider mode for the {@link #mDividerDrawableHorizontal}. The value needs to
     * be the combination of the value of {@link #SHOW_DIVIDER_NONE},
     * {@link #SHOW_DIVIDER_BEGINNING}, {@link #SHOW_DIVIDER_MIDDLE} and {@link #SHOW_DIVIDER_END}
     */
    private int mShowDividerHorizontal;

    /**
     * Indicates the divider mode for the {@link #mDividerDrawableVertical}. The value needs to
     * be the combination of the value of {@link #SHOW_DIVIDER_NONE},
     * {@link #SHOW_DIVIDER_BEGINNING}, {@link #SHOW_DIVIDER_MIDDLE} and {@link #SHOW_DIVIDER_END}
     */
    private int mShowDividerVertical;

    /** The height of the {@link #mDividerDrawableHorizontal}. */
    private int mDividerHorizontalHeight;

    /** The width of the {@link #mDividerDrawableVertical}. */
    private int mDividerVerticalWidth;

    private FlexboxHelper mFlexboxHelper = new FlexboxHelper(this);

    private List<FlexLine> mFlexLines = new ArrayList<>();

    /**
     * Holds the 'frozen' state of children during measure. If a view is frozen it will no longer
     * expand or shrink regardless of mFlexGrow/mFlexShrink. Items are indexed by the child's
     * reordered index.
     */
    private boolean[] mChildrenFrozen;

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
        mFlexDirection = a
                .getInt(R.styleable.FlexboxLayout_flexDirection, FlexDirection.ROW);
        mFlexWrap = a.getInt(R.styleable.FlexboxLayout_flexWrap, FlexWrap.NOWRAP);
        mJustifyContent = a
                .getInt(R.styleable.FlexboxLayout_justifyContent, JustifyContent.FLEX_START);
        mAlignItems = a.getInt(R.styleable.FlexboxLayout_alignItems, AlignItems.STRETCH);
        mAlignContent = a.getInt(R.styleable.FlexboxLayout_alignContent, AlignContent.STRETCH);
        Drawable drawable = a.getDrawable(R.styleable.FlexboxLayout_dividerDrawable);
        if (drawable != null) {
            setDividerDrawableHorizontal(drawable);
            setDividerDrawableVertical(drawable);
        }
        Drawable drawableHorizontal = a
                .getDrawable(R.styleable.FlexboxLayout_dividerDrawableHorizontal);
        if (drawableHorizontal != null) {
            setDividerDrawableHorizontal(drawableHorizontal);
        }
        Drawable drawableVertical = a
                .getDrawable(R.styleable.FlexboxLayout_dividerDrawableVertical);
        if (drawableVertical != null) {
            setDividerDrawableVertical(drawableVertical);
        }
        int dividerMode = a.getInt(R.styleable.FlexboxLayout_showDivider, SHOW_DIVIDER_NONE);
        if (dividerMode != SHOW_DIVIDER_NONE) {
            mShowDividerVertical = dividerMode;
            mShowDividerHorizontal = dividerMode;
        }
        int dividerModeVertical = a
                .getInt(R.styleable.FlexboxLayout_showDividerVertical, SHOW_DIVIDER_NONE);
        if (dividerModeVertical != SHOW_DIVIDER_NONE) {
            mShowDividerVertical = dividerModeVertical;
        }
        int dividerModeHorizontal = a
                .getInt(R.styleable.FlexboxLayout_showDividerHorizontal, SHOW_DIVIDER_NONE);
        if (dividerModeHorizontal != SHOW_DIVIDER_NONE) {
            mShowDividerHorizontal = dividerModeHorizontal;
        }
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mFlexboxHelper.isOrderChangedFromLastMeasurement()) {
            mFlexboxHelper.mReorderedIndices = mFlexboxHelper.createReorderedIndices();
        }
        if (mChildrenFrozen == null
                || mChildrenFrozen.length < getChildCount()) {
            mChildrenFrozen = new boolean[getChildCount()];
        }

        // TODO: Only calculate the children views which are affected from the last measure.

        switch (mFlexDirection) {
            case FlexDirection.ROW: // Intentional fall through
            case FlexDirection.ROW_REVERSE:
                measureHorizontal(widthMeasureSpec, heightMeasureSpec);
                break;
            case FlexDirection.COLUMN: // Intentional fall through
            case FlexDirection.COLUMN_REVERSE:
                measureVertical(widthMeasureSpec, heightMeasureSpec);
                break;
            default:
                throw new IllegalStateException(
                        "Invalid value for the flex direction is set: " + mFlexDirection);
        }

        Arrays.fill(mChildrenFrozen, false);
    }

    @Override
    public int getFlexItemCount() {
        return getChildCount();
    }

    @Override
    public View getFlexItemAt(int index) {
        return getChildAt(index);
    }

    /**
     * Returns a View, which is reordered by taking {@link LayoutParams#mOrder} parameters
     * into account.
     *
     * @param index the index of the view
     * @return the reordered view, which {@link LayoutParams@order} is taken into account.
     * If the index is negative or out of bounds of the number of contained views,
     * returns {@code null}.
     */
    public View getReorderedChildAt(int index) {
        if (index < 0 || index >= mFlexboxHelper.mReorderedIndices.length) {
            return null;
        }
        return getChildAt(mFlexboxHelper.mReorderedIndices[index]);
    }

    @Override
    public View getReorderedFlexItemAt(int index) {
        return getReorderedChildAt(index);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        // Create an array for the reordered indices before the View is added in the parent
        // ViewGroup since otherwise reordered indices won't be in effect before the
        // FlexboxLayout's onMeasure is called.
        // Because requestLayout is requested in the super.addView method.
        mFlexboxHelper.mReorderedIndices = mFlexboxHelper
                .createReorderedIndices(child, index, params);
        super.addView(child, index, params);
    }

    /**
     * Sub method for {@link #onMeasure(int, int)}, when the main axis direction is horizontal
     * (either left to right or right to left).
     *
     * @param widthMeasureSpec  horizontal space requirements as imposed by the parent
     * @param heightMeasureSpec vertical space requirements as imposed by the parent
     * @see #onMeasure(int, int)
     * @see #setFlexDirection(int)
     * @see #setFlexWrap(int)
     * @see #setAlignItems(int)
     * @see #setAlignContent(int)
     */
    private void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
        mFlexLines.clear();
        FlexboxHelper.FlexLinesResult flexLinesResult = mFlexboxHelper
                .calculateHorizontalFlexLines(widthMeasureSpec, heightMeasureSpec);
        mFlexLines = flexLinesResult.mFlexLines;

        mFlexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec, mChildrenFrozen);

        // TODO: Consider the case any individual child's mAlignSelf is set to ALIGN_SELF_BASELINE
        if (mAlignItems == AlignItems.BASELINE) {
            int viewIndex = 0;
            for (FlexLine flexLine : mFlexLines) {
                // The largest height value that also take the baseline shift into account
                int largestHeightInLine = Integer.MIN_VALUE;
                for (int i = viewIndex; i < viewIndex + flexLine.mItemCount; i++) {
                    View child = getReorderedChildAt(i);
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    if (mFlexWrap != FlexWrap.WRAP_REVERSE) {
                        int marginTop = flexLine.mMaxBaseline - child.getBaseline();
                        marginTop = Math.max(marginTop, lp.topMargin);
                        largestHeightInLine = Math.max(largestHeightInLine,
                                child.getHeight() + marginTop + lp.bottomMargin);
                    } else {
                        int marginBottom = flexLine.mMaxBaseline - child.getMeasuredHeight() +
                                child.getBaseline();
                        marginBottom = Math.max(marginBottom, lp.bottomMargin);
                        largestHeightInLine = Math.max(largestHeightInLine,
                                child.getHeight() + lp.topMargin + marginBottom);
                    }
                }
                flexLine.mCrossSize = largestHeightInLine;
                viewIndex += flexLine.mItemCount;
            }
        }

        mFlexboxHelper.determineCrossSize(widthMeasureSpec, heightMeasureSpec,
                getPaddingTop() + getPaddingBottom());
        // Now cross size for each flex line is determined.
        // Expand the views if alignItems (or mAlignSelf in each child view) is set to stretch
        mFlexboxHelper.stretchViews();
        setMeasuredDimensionForFlex(mFlexDirection, widthMeasureSpec, heightMeasureSpec,
                flexLinesResult.mChildState);
    }

    /**
     * Sub method for {@link #onMeasure(int, int)} when the main axis direction is vertical
     * (either from top to bottom or bottom to top).
     *
     * @param widthMeasureSpec  horizontal space requirements as imposed by the parent
     * @param heightMeasureSpec vertical space requirements as imposed by the parent
     * @see #onMeasure(int, int)
     * @see #setFlexDirection(int)
     * @see #setFlexWrap(int)
     * @see #setAlignItems(int)
     * @see #setAlignContent(int)
     */
    private void measureVertical(int widthMeasureSpec, int heightMeasureSpec) {
        mFlexLines.clear();
        FlexboxHelper.FlexLinesResult flexLinesResult = mFlexboxHelper
                .calculateVerticalFlexLines(widthMeasureSpec, heightMeasureSpec);
        mFlexLines = flexLinesResult.mFlexLines;

        mFlexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec,
                mChildrenFrozen);
        mFlexboxHelper.determineCrossSize(widthMeasureSpec, heightMeasureSpec,
                getPaddingLeft() + getPaddingRight());
        // Now cross size for each flex line is determined.
        // Expand the views if alignItems (or mAlignSelf in each child view) is set to stretch
        mFlexboxHelper.stretchViews();
        setMeasuredDimensionForFlex(mFlexDirection, widthMeasureSpec, heightMeasureSpec,
                flexLinesResult.mChildState);
    }


    /**
     * Set this FlexboxLayouts' width and height depending on the calculated size of main axis and
     * cross axis.
     *
     * @param flexDirection     the value of the flex direction
     * @param widthMeasureSpec  horizontal space requirements as imposed by the parent
     * @param heightMeasureSpec vertical space requirements as imposed by the parent
     * @param childState        the child state of the View
     * @see #getFlexDirection()
     * @see #setFlexDirection(int)
     */
    private void setMeasuredDimensionForFlex(@FlexDirection int flexDirection, int widthMeasureSpec,
            int heightMeasureSpec, int childState) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int calculatedMaxHeight;
        int calculatedMaxWidth;
        switch (flexDirection) {
            case FlexDirection.ROW: // Intentional fall through
            case FlexDirection.ROW_REVERSE:
                calculatedMaxHeight = getSumOfCrossSize() + getPaddingTop()
                        + getPaddingBottom();
                calculatedMaxWidth = getLargestMainSize();
                break;
            case FlexDirection.COLUMN: // Intentional fall through
            case FlexDirection.COLUMN_REVERSE:
                calculatedMaxHeight = getLargestMainSize();
                calculatedMaxWidth = getSumOfCrossSize() + getPaddingLeft() + getPaddingRight();
                break;
            default:
                throw new IllegalArgumentException("Invalid flex direction: " + flexDirection);
        }

        int widthSizeAndState;
        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                if (widthSize < calculatedMaxWidth) {
                    childState = ViewCompat
                            .combineMeasuredStates(childState, ViewCompat.MEASURED_STATE_TOO_SMALL);
                }
                widthSizeAndState = ViewCompat.resolveSizeAndState(widthSize, widthMeasureSpec,
                        childState);
                break;
            case MeasureSpec.AT_MOST: {
                if (widthSize < calculatedMaxWidth) {
                    childState = ViewCompat
                            .combineMeasuredStates(childState, ViewCompat.MEASURED_STATE_TOO_SMALL);
                } else {
                    widthSize = calculatedMaxWidth;
                }
                widthSizeAndState = ViewCompat.resolveSizeAndState(widthSize, widthMeasureSpec,
                        childState);
                break;
            }
            case MeasureSpec.UNSPECIFIED: {
                widthSizeAndState = ViewCompat
                        .resolveSizeAndState(calculatedMaxWidth, widthMeasureSpec, childState);
                break;
            }
            default:
                throw new IllegalStateException("Unknown width mode is set: " + widthMode);
        }
        int heightSizeAndState;
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                if (heightSize < calculatedMaxHeight) {
                    childState = ViewCompat.combineMeasuredStates(childState,
                            ViewCompat.MEASURED_STATE_TOO_SMALL
                                    >> ViewCompat.MEASURED_HEIGHT_STATE_SHIFT);
                }
                heightSizeAndState = ViewCompat.resolveSizeAndState(heightSize, heightMeasureSpec,
                        childState);
                break;
            case MeasureSpec.AT_MOST: {
                if (heightSize < calculatedMaxHeight) {
                    childState = ViewCompat.combineMeasuredStates(childState,
                            ViewCompat.MEASURED_STATE_TOO_SMALL
                                    >> ViewCompat.MEASURED_HEIGHT_STATE_SHIFT);
                } else {
                    heightSize = calculatedMaxHeight;
                }
                heightSizeAndState = ViewCompat.resolveSizeAndState(heightSize, heightMeasureSpec,
                        childState);
                break;
            }
            case MeasureSpec.UNSPECIFIED: {
                heightSizeAndState = ViewCompat.resolveSizeAndState(calculatedMaxHeight,
                        heightMeasureSpec, childState);
                break;
            }
            default:
                throw new IllegalStateException("Unknown height mode is set: " + heightMode);
        }
        setMeasuredDimension(widthSizeAndState, heightSizeAndState);
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

            // Judge if the beginning or middle dividers are required
            if (hasDividerBeforeFlexLine(i)) {
                if (isMainAxisDirectionHorizontal()) {
                    sum += mDividerHorizontalHeight;
                } else {
                    sum += mDividerVerticalWidth;
                }
            }

            // Judge if the end divider is required
            if (hasEndDividerAfterFlexLine(i)) {
                if (isMainAxisDirectionHorizontal()) {
                    sum += mDividerHorizontalHeight;
                } else {
                    sum += mDividerVerticalWidth;
                }
            }
            sum += flexLine.mCrossSize;
        }
        return sum;
    }

    private boolean isMainAxisDirectionHorizontal() {
        return mFlexDirection == FlexDirection.ROW || mFlexDirection == FlexDirection.ROW_REVERSE;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int layoutDirection = ViewCompat.getLayoutDirection(this);
        boolean isRtl;
        switch (mFlexDirection) {
            case FlexDirection.ROW:
                isRtl = layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL;
                layoutHorizontal(isRtl, left, top, right, bottom);
                break;
            case FlexDirection.ROW_REVERSE:
                isRtl = layoutDirection != ViewCompat.LAYOUT_DIRECTION_RTL;
                layoutHorizontal(isRtl, left, top, right, bottom);
                break;
            case FlexDirection.COLUMN:
                isRtl = layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL;
                if (mFlexWrap == FlexWrap.WRAP_REVERSE) {
                    isRtl = !isRtl;
                }
                layoutVertical(isRtl, false, left, top, right, bottom);
                break;
            case FlexDirection.COLUMN_REVERSE:
                isRtl = layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL;
                if (mFlexWrap == FlexWrap.WRAP_REVERSE) {
                    isRtl = !isRtl;
                }
                layoutVertical(isRtl, true, left, top, right, bottom);
                break;
            default:
                throw new IllegalStateException("Invalid flex direction is set: " + mFlexDirection);
        }
    }

    /**
     * Sub method for {@link #onLayout(boolean, int, int, int, int)} when the
     * {@link #mFlexDirection} is either {@link FlexDirection#ROW} or
     * {@link FlexDirection#ROW_REVERSE}.
     *
     * @param isRtl  {@code true} if the horizontal layout direction is right to left, {@code
     *               false} otherwise.
     * @param left   the left position of this View
     * @param top    the top position of this View
     * @param right  the right position of this View
     * @param bottom the bottom position of this View
     * @see #getFlexWrap()
     * @see #setFlexWrap(int)
     * @see #getJustifyContent()
     * @see #setJustifyContent(int)
     * @see #getAlignItems()
     * @see #setAlignItems(int)
     * @see LayoutParams#mAlignSelf
     */
    private void layoutHorizontal(boolean isRtl, int left, int top, int right, int bottom) {
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        // Use float to reduce the round error that may happen in when justifyContent ==
        // SPACE_BETWEEN or SPACE_AROUND
        float childLeft;
        int currentViewIndex = 0;

        int height = bottom - top;
        int width = right - left;
        // childBottom is used if the mFlexWrap is WRAP_REVERSE otherwise
        // childTop is used to align the vertical position of the children views.
        int childBottom = height - getPaddingBottom();
        int childTop = getPaddingTop();

        // Used only for RTL layout
        // Use float to reduce the round error that may happen in when justifyContent ==
        // SPACE_BETWEEN or SPACE_AROUND
        float childRight;
        for (int i = 0, size = mFlexLines.size(); i < size; i++) {
            FlexLine flexLine = mFlexLines.get(i);
            if (hasDividerBeforeFlexLine(i)) {
                childBottom -= mDividerHorizontalHeight;
                childTop += mDividerHorizontalHeight;
            }
            float spaceBetweenItem = 0f;
            switch (mJustifyContent) {
                case JustifyContent.FLEX_START:
                    childLeft = paddingLeft;
                    childRight = width - paddingRight;
                    break;
                case JustifyContent.FLEX_END:
                    childLeft = width - flexLine.mMainSize + paddingRight;
                    childRight = flexLine.mMainSize - paddingLeft;
                    break;
                case JustifyContent.CENTER:
                    childLeft = paddingLeft + (width - flexLine.mMainSize) / 2f;
                    childRight = width - paddingRight - (width - flexLine.mMainSize) / 2f;
                    break;
                case JustifyContent.SPACE_AROUND:
                    if (flexLine.mItemCount != 0) {
                        spaceBetweenItem = (width - flexLine.mMainSize)
                                / (float) flexLine.mItemCount;
                    }
                    childLeft = paddingLeft + spaceBetweenItem / 2f;
                    childRight = width - paddingRight - spaceBetweenItem / 2f;
                    break;
                case JustifyContent.SPACE_BETWEEN:
                    childLeft = paddingLeft;
                    float denominator = flexLine.mItemCount != 1 ? flexLine.mItemCount - 1 : 1f;
                    spaceBetweenItem = (width - flexLine.mMainSize) / denominator;
                    childRight = width - paddingRight;
                    break;
                default:
                    throw new IllegalStateException(
                            "Invalid justifyContent is set: " + mJustifyContent);
            }
            spaceBetweenItem = Math.max(spaceBetweenItem, 0);

            for (int j = 0; j < flexLine.mItemCount; j++) {
                View child = getReorderedChildAt(currentViewIndex);
                if (child == null) {
                    continue;
                } else if (child.getVisibility() == View.GONE) {
                    currentViewIndex++;
                    continue;
                }
                LayoutParams lp = ((LayoutParams) child.getLayoutParams());
                childLeft += lp.leftMargin;
                childRight -= lp.rightMargin;
                int beforeDividerLength = 0;
                int endDividerLength = 0;
                if (hasDividerBeforeChildAtAlongMainAxis(currentViewIndex, j)) {
                    beforeDividerLength = mDividerVerticalWidth;
                    childLeft += beforeDividerLength;
                    childRight -= beforeDividerLength;
                }
                if (j == flexLine.mItemCount - 1 && (mShowDividerVertical & SHOW_DIVIDER_END) > 0) {
                    endDividerLength = mDividerVerticalWidth;
                }

                if (mFlexWrap == FlexWrap.WRAP_REVERSE) {
                    if (isRtl) {
                        layoutSingleChildHorizontal(child, flexLine, mFlexWrap, mAlignItems,
                                Math.round(childRight) - child.getMeasuredWidth(),
                                childBottom - child.getMeasuredHeight(), Math.round(childRight),
                                childBottom);
                    } else {
                        layoutSingleChildHorizontal(child, flexLine, mFlexWrap, mAlignItems,
                                Math.round(childLeft), childBottom - child.getMeasuredHeight(),
                                Math.round(childLeft) + child.getMeasuredWidth(),
                                childBottom);
                    }
                } else {
                    if (isRtl) {
                        layoutSingleChildHorizontal(child, flexLine, mFlexWrap, mAlignItems,
                                Math.round(childRight) - child.getMeasuredWidth(), childTop,
                                Math.round(childRight), childTop + child.getMeasuredHeight());
                    } else {
                        layoutSingleChildHorizontal(child, flexLine, mFlexWrap, mAlignItems,
                                Math.round(childLeft), childTop,
                                Math.round(childLeft) + child.getMeasuredWidth(),
                                childTop + child.getMeasuredHeight());
                    }
                }
                childLeft += child.getMeasuredWidth() + spaceBetweenItem + lp.rightMargin;
                childRight -= child.getMeasuredWidth() + spaceBetweenItem + lp.leftMargin;
                currentViewIndex++;

                if (isRtl) {
                    flexLine.updatePositionFromView(child, /*leftDecoration*/endDividerLength, 0,
                            /*rightDecoration*/ beforeDividerLength, 0);
                } else {
                    flexLine.updatePositionFromView(child, /*leftDecoration*/beforeDividerLength, 0,
                            /*rightDecoration*/ endDividerLength, 0);
                }
            }
            childTop += flexLine.mCrossSize;
            childBottom -= flexLine.mCrossSize;
        }
    }

    /**
     * Place a single View when the layout direction is horizontal ({@link #mFlexDirection} is
     * either {@link FlexDirection#ROW} or {@link FlexDirection#ROW_REVERSE}).
     *
     * @param view       the View to be placed
     * @param flexLine   the {@link FlexLine} where the View belongs to
     * @param flexWrap   the flex wrap attribute of this FlexboxLayout
     * @param alignItems the align items attribute of this FlexboxLayout
     * @param left       the left position of the View, which the View's margin is already taken
     *                   into account
     * @param top        the top position of the flex line where the View belongs to. The actual
     *                   View's top position is shifted depending on the flexWrap and alignItems
     *                   attributes
     * @param right      the right position of the View, which the View's margin is already taken
     *                   into account
     * @param bottom     the bottom position of the flex line where the View belongs to. The actual
     *                   View's bottom position is shifted depending on the flexWrap and alignItems
     *                   attributes
     * @see #getAlignItems()
     * @see #setAlignItems(int)
     * @see LayoutParams#mAlignSelf
     */
    private void layoutSingleChildHorizontal(View view, FlexLine flexLine, @FlexWrap int flexWrap,
            int alignItems, int left, int top, int right, int bottom) {
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        if (lp.mAlignSelf != AlignSelf.AUTO) {
            // Expecting the values for alignItems and mAlignSelf match except for ALIGN_SELF_AUTO.
            // Assigning the mAlignSelf value as alignItems should work.
            alignItems = lp.mAlignSelf;
        }
        int crossSize = flexLine.mCrossSize;
        switch (alignItems) {
            case AlignItems.FLEX_START: // Intentional fall through
            case AlignItems.STRETCH:
                if (flexWrap != FlexWrap.WRAP_REVERSE) {
                    view.layout(left, top + lp.topMargin, right, bottom + lp.topMargin);
                } else {
                    view.layout(left, top - lp.bottomMargin, right, bottom - lp.bottomMargin);
                }
                break;
            case AlignItems.BASELINE:
                if (flexWrap != FlexWrap.WRAP_REVERSE) {
                    int marginTop = flexLine.mMaxBaseline - view.getBaseline();
                    marginTop = Math.max(marginTop, lp.topMargin);
                    view.layout(left, top + marginTop, right, bottom + marginTop);
                } else {
                    int marginBottom = flexLine.mMaxBaseline - view.getMeasuredHeight() + view
                            .getBaseline();
                    marginBottom = Math.max(marginBottom, lp.bottomMargin);
                    view.layout(left, top - marginBottom, right, bottom - marginBottom);
                }
                break;
            case AlignItems.FLEX_END:
                if (flexWrap != FlexWrap.WRAP_REVERSE) {
                    view.layout(left,
                            top + crossSize - view.getMeasuredHeight() - lp.bottomMargin,
                            right, top + crossSize - lp.bottomMargin);
                } else {
                    // If the flexWrap == WRAP_REVERSE, the direction of the
                    // flexEnd is flipped (from top to bottom).
                    view.layout(left, top - crossSize + view.getMeasuredHeight() + lp.topMargin,
                            right, bottom - crossSize + view.getMeasuredHeight() + lp.topMargin);
                }
                break;
            case AlignItems.CENTER:
                int topFromCrossAxis = (crossSize - view.getMeasuredHeight()) / 2;
                if (flexWrap != FlexWrap.WRAP_REVERSE) {
                    view.layout(left, top + topFromCrossAxis + lp.topMargin - lp.bottomMargin,
                            right, top + topFromCrossAxis + view.getMeasuredHeight() + lp.topMargin
                                    - lp.bottomMargin);
                } else {
                    view.layout(left, top - topFromCrossAxis + lp.topMargin - lp.bottomMargin,
                            right, top - topFromCrossAxis + view.getMeasuredHeight() + lp.topMargin
                                    - lp.bottomMargin);
                }
                break;
        }
    }

    /**
     * Sub method for {@link #onLayout(boolean, int, int, int, int)} when the
     * {@link #mFlexDirection} is either {@link FlexDirection#COLUMN} or
     * {@link FlexDirection#COLUMN_REVERSE}.
     *
     * @param isRtl           {@code true} if the horizontal layout direction is right to left,
     *                        {@code false}
     *                        otherwise
     * @param fromBottomToTop {@code true} if the layout direction is bottom to top, {@code false}
     *                        otherwise
     * @param left            the left position of this View
     * @param top             the top position of this View
     * @param right           the right position of this View
     * @param bottom          the bottom position of this View
     * @see #getFlexWrap()
     * @see #setFlexWrap(int)
     * @see #getJustifyContent()
     * @see #setJustifyContent(int)
     * @see #getAlignItems()
     * @see #setAlignItems(int)
     * @see LayoutParams#mAlignSelf
     */
    private void layoutVertical(boolean isRtl, boolean fromBottomToTop, int left, int top,
            int right, int bottom) {
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        int paddingRight = getPaddingRight();
        int childLeft = getPaddingLeft();
        int currentViewIndex = 0;

        int width = right - left;
        int height = bottom - top;
        // childRight is used if the mFlexWrap is WRAP_REVERSE otherwise
        // childLeft is used to align the horizontal position of the children views.
        int childRight = width - paddingRight;

        // Use float to reduce the round error that may happen in when justifyContent ==
        // SPACE_BETWEEN or SPACE_AROUND
        float childTop;

        // Used only for if the direction is from bottom to top
        float childBottom;

        for (int i = 0, size = mFlexLines.size(); i < size; i++) {
            FlexLine flexLine = mFlexLines.get(i);
            if (hasDividerBeforeFlexLine(i)) {
                childLeft += mDividerVerticalWidth;
                childRight -= mDividerVerticalWidth;
            }
            float spaceBetweenItem = 0f;
            switch (mJustifyContent) {
                case JustifyContent.FLEX_START:
                    childTop = paddingTop;
                    childBottom = height - paddingBottom;
                    break;
                case JustifyContent.FLEX_END:
                    childTop = height - flexLine.mMainSize + paddingBottom;
                    childBottom = flexLine.mMainSize - paddingTop;
                    break;
                case JustifyContent.CENTER:
                    childTop = paddingTop + (height - flexLine.mMainSize) / 2f;
                    childBottom = height - paddingBottom - (height - flexLine.mMainSize) / 2f;
                    break;
                case JustifyContent.SPACE_AROUND:
                    if (flexLine.mItemCount != 0) {
                        spaceBetweenItem = (height - flexLine.mMainSize)
                                / (float) flexLine.mItemCount;
                    }
                    childTop = paddingTop + spaceBetweenItem / 2f;
                    childBottom = height - paddingBottom - spaceBetweenItem / 2f;
                    break;
                case JustifyContent.SPACE_BETWEEN:
                    childTop = paddingTop;
                    float denominator = flexLine.mItemCount != 1 ? flexLine.mItemCount - 1 : 1f;
                    spaceBetweenItem = (height - flexLine.mMainSize) / denominator;
                    childBottom = height - paddingBottom;
                    break;
                default:
                    throw new IllegalStateException(
                            "Invalid justifyContent is set: " + mJustifyContent);
            }
            spaceBetweenItem = Math.max(spaceBetweenItem, 0);

            for (int j = 0; j < flexLine.mItemCount; j++) {
                View child = getReorderedChildAt(currentViewIndex);
                if (child == null) {
                    continue;
                } else if (child.getVisibility() == View.GONE) {
                    currentViewIndex++;
                    continue;
                }
                LayoutParams lp = ((LayoutParams) child.getLayoutParams());
                childTop += lp.topMargin;
                childBottom -= lp.bottomMargin;
                int beforeDividerLength = 0;
                int endDividerLength = 0;
                if (hasDividerBeforeChildAtAlongMainAxis(currentViewIndex, j)) {
                    beforeDividerLength = mDividerHorizontalHeight;
                    childTop += beforeDividerLength;
                    childBottom -= beforeDividerLength;
                }
                if (j == flexLine.mItemCount - 1
                        && (mShowDividerHorizontal & SHOW_DIVIDER_END) > 0) {
                    endDividerLength = mDividerHorizontalHeight;
                }
                if (isRtl) {
                    if (fromBottomToTop) {
                        layoutSingleChildVertical(child, flexLine, true, mAlignItems,
                                childRight - child.getMeasuredWidth(),
                                Math.round(childBottom) - child.getMeasuredHeight(), childRight,
                                Math.round(childBottom));
                    } else {
                        layoutSingleChildVertical(child, flexLine, true, mAlignItems,
                                childRight - child.getMeasuredWidth(), Math.round(childTop),
                                childRight, Math.round(childTop) + child.getMeasuredHeight());
                    }
                } else {
                    if (fromBottomToTop) {
                        layoutSingleChildVertical(child, flexLine, false, mAlignItems,
                                childLeft, Math.round(childBottom) - child.getMeasuredHeight(),
                                childLeft + child.getMeasuredWidth(), Math.round(childBottom));
                    } else {
                        layoutSingleChildVertical(child, flexLine, false, mAlignItems,
                                childLeft, Math.round(childTop),
                                childLeft + child.getMeasuredWidth(),
                                Math.round(childTop) + child.getMeasuredHeight());
                    }
                }
                childTop += child.getMeasuredHeight() + spaceBetweenItem + lp.bottomMargin;
                childBottom -= child.getMeasuredHeight() + spaceBetweenItem + lp.topMargin;
                currentViewIndex++;

                if (fromBottomToTop) {
                    flexLine.updatePositionFromView(child, 0, /*topDecoration*/endDividerLength, 0,
                            /*bottomDecoration*/ beforeDividerLength);
                } else {
                    flexLine.updatePositionFromView(child, 0, /*topDecoration*/beforeDividerLength,
                            0, /*bottomDecoration*/endDividerLength);
                }
            }
            childLeft += flexLine.mCrossSize;
            childRight -= flexLine.mCrossSize;
        }
    }

    /**
     * Place a single View when the layout direction is vertical ({@link #mFlexDirection} is
     * either {@link FlexDirection#COLUMN} or {@link FlexDirection#COLUMN_REVERSE}).
     *
     * @param view       the View to be placed
     * @param flexLine   the {@link FlexLine} where the View belongs to
     * @param isRtl      {@code true} if the layout direction is right to left, {@code false}
     *                   otherwise
     * @param alignItems the align items attribute of this FlexboxLayout
     * @param left       the left position of the flex line where the View belongs to. The actual
     *                   View's left position is shifted depending on the isRtl and alignItems
     *                   attributes
     * @param top        the top position of the View, which the View's margin is already taken
     *                   into account
     * @param right      the right position of the flex line where the View belongs to. The actual
     *                   View's right position is shifted depending on the isRtl and alignItems
     *                   attributes
     * @param bottom     the bottom position of the View, which the View's margin is already taken
     *                   into account
     * @see #getAlignItems()
     * @see #setAlignItems(int)
     * @see LayoutParams#mAlignSelf
     */
    private void layoutSingleChildVertical(View view, FlexLine flexLine, boolean isRtl,
            int alignItems, int left, int top, int right, int bottom) {
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        if (lp.mAlignSelf != AlignSelf.AUTO) {
            // Expecting the values for alignItems and mAlignSelf match except for ALIGN_SELF_AUTO.
            // Assigning the mAlignSelf value as alignItems should work.
            alignItems = lp.mAlignSelf;
        }
        int crossSize = flexLine.mCrossSize;
        switch (alignItems) {
            case AlignItems.FLEX_START: // Intentional fall through
            case AlignItems.STRETCH: // Intentional fall through
            case AlignItems.BASELINE:
                if (!isRtl) {
                    view.layout(left + lp.leftMargin, top, right + lp.leftMargin, bottom);
                } else {
                    view.layout(left - lp.rightMargin, top, right - lp.rightMargin, bottom);
                }
                break;
            case AlignItems.FLEX_END:
                if (!isRtl) {
                    view.layout(left + crossSize - view.getMeasuredWidth() - lp.rightMargin,
                            top, right + crossSize - view.getMeasuredWidth() - lp.rightMargin,
                            bottom);
                } else {
                    // If the flexWrap == WRAP_REVERSE, the direction of the
                    // flexEnd is flipped (from left to right).
                    view.layout(left - crossSize + view.getMeasuredWidth() + lp.leftMargin, top,
                            right - crossSize + view.getMeasuredWidth() + lp.leftMargin,
                            bottom);
                }
                break;
            case AlignItems.CENTER:
                int leftFromCrossAxis = (crossSize - view.getMeasuredWidth()) / 2;
                if (!isRtl) {
                    view.layout(left + leftFromCrossAxis + lp.leftMargin - lp.rightMargin,
                            top, right + leftFromCrossAxis + lp.leftMargin - lp.rightMargin,
                            bottom);
                } else {
                    view.layout(left - leftFromCrossAxis + lp.leftMargin - lp.rightMargin,
                            top, right - leftFromCrossAxis + lp.leftMargin - lp.rightMargin,
                            bottom);
                }
                break;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDividerDrawableVertical == null && mDividerDrawableHorizontal == null) {
            return;
        }
        if (mShowDividerHorizontal == SHOW_DIVIDER_NONE
                && mShowDividerVertical == SHOW_DIVIDER_NONE) {
            return;
        }

        int layoutDirection = ViewCompat.getLayoutDirection(this);
        boolean isRtl;
        boolean fromBottomToTop = false;
        switch (mFlexDirection) {
            case FlexDirection.ROW:
                isRtl = layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL;
                if (mFlexWrap == FlexWrap.WRAP_REVERSE) {
                    fromBottomToTop = true;
                }
                drawDividersHorizontal(canvas, isRtl, fromBottomToTop);
                break;
            case FlexDirection.ROW_REVERSE:
                isRtl = layoutDirection != ViewCompat.LAYOUT_DIRECTION_RTL;
                if (mFlexWrap == FlexWrap.WRAP_REVERSE) {
                    fromBottomToTop = true;
                }
                drawDividersHorizontal(canvas, isRtl, fromBottomToTop);
                break;
            case FlexDirection.COLUMN:
                isRtl = layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL;
                if (mFlexWrap == FlexWrap.WRAP_REVERSE) {
                    isRtl = !isRtl;
                }
                fromBottomToTop = false;
                drawDividersVertical(canvas, isRtl, fromBottomToTop);
                break;
            case FlexDirection.COLUMN_REVERSE:
                isRtl = layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL;
                if (mFlexWrap == FlexWrap.WRAP_REVERSE) {
                    isRtl = !isRtl;
                }
                fromBottomToTop = true;
                drawDividersVertical(canvas, isRtl, fromBottomToTop);
                break;
        }
    }

    /**
     * Sub method for {@link #onDraw(Canvas)} when the main axis direction is horizontal
     * ({@link #mFlexDirection} is either of {@link FlexDirection#ROW} or
     * {@link FlexDirection#ROW_REVERSE}.
     *
     * @param canvas          the canvas on which the background will be drawn
     * @param isRtl           {@code true} when the horizontal layout direction is right to left,
     *                        {@code false} otherwise
     * @param fromBottomToTop {@code true} when the vertical layout direction is bottom to top,
     *                        {@code false} otherwise
     */
    private void drawDividersHorizontal(Canvas canvas, boolean isRtl, boolean fromBottomToTop) {
        int currentViewIndex = 0;
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int horizontalDividerLength = Math.max(0, getWidth() - paddingRight - paddingLeft);
        for (int i = 0, size = mFlexLines.size(); i < size; i++) {
            FlexLine flexLine = mFlexLines.get(i);
            for (int j = 0; j < flexLine.mItemCount; j++) {
                View view = getReorderedChildAt(currentViewIndex);
                LayoutParams lp = (LayoutParams) view.getLayoutParams();

                // Judge if the beginning or middle divider is needed
                if (hasDividerBeforeChildAtAlongMainAxis(currentViewIndex, j)) {
                    int dividerLeft;
                    if (isRtl) {
                        dividerLeft = view.getRight() + lp.rightMargin;
                    } else {
                        dividerLeft = view.getLeft() - lp.leftMargin - mDividerVerticalWidth;
                    }

                    drawVerticalDivider(canvas, dividerLeft, flexLine.mTop, flexLine.mCrossSize);
                }

                // Judge if the end divider is needed
                if (j == flexLine.mItemCount - 1) {
                    if ((mShowDividerVertical & SHOW_DIVIDER_END) > 0) {
                        int dividerLeft;
                        if (isRtl) {
                            dividerLeft = view.getLeft() - lp.leftMargin - mDividerVerticalWidth;
                        } else {
                            dividerLeft = view.getRight() + lp.rightMargin;
                        }

                        drawVerticalDivider(canvas, dividerLeft, flexLine.mTop,
                                flexLine.mCrossSize);
                    }
                }
                currentViewIndex++;
            }

            // Judge if the beginning or middle dividers are needed before the flex line
            if (hasDividerBeforeFlexLine(i)) {
                int horizontalDividerTop;
                if (fromBottomToTop) {
                    horizontalDividerTop = flexLine.mBottom;
                } else {
                    horizontalDividerTop = flexLine.mTop - mDividerHorizontalHeight;
                }
                drawHorizontalDivider(canvas, paddingLeft, horizontalDividerTop,
                        horizontalDividerLength);
            }
            // Judge if the end divider is needed before the flex line
            if (hasEndDividerAfterFlexLine(i)) {
                if ((mShowDividerHorizontal & SHOW_DIVIDER_END) > 0) {
                    int horizontalDividerTop;
                    if (fromBottomToTop) {
                        horizontalDividerTop = flexLine.mTop - mDividerHorizontalHeight;
                    } else {
                        horizontalDividerTop = flexLine.mBottom;
                    }
                    drawHorizontalDivider(canvas, paddingLeft, horizontalDividerTop,
                            horizontalDividerLength);
                }
            }
        }
    }

    /**
     * Sub method for {@link #onDraw(Canvas)} when the main axis direction is vertical
     * ({@link #mFlexDirection} is either of {@link FlexDirection#COLUMN} or
     * {@link FlexDirection#COLUMN_REVERSE}.
     *
     * @param canvas          the canvas on which the background will be drawn
     * @param isRtl           {@code true} when the horizontal layout direction is right to left,
     *                        {@code false} otherwise
     * @param fromBottomToTop {@code true} when the vertical layout direction is bottom to top,
     *                        {@code false} otherwise
     */
    private void drawDividersVertical(Canvas canvas, boolean isRtl, boolean fromBottomToTop) {
        int currentViewIndex = 0;
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int verticalDividerLength = Math.max(0, getHeight() - paddingBottom - paddingTop);
        for (int i = 0, size = mFlexLines.size(); i < size; i++) {
            FlexLine flexLine = mFlexLines.get(i);

            // Draw horizontal dividers if needed
            for (int j = 0; j < flexLine.mItemCount; j++) {
                View view = getReorderedChildAt(currentViewIndex);
                LayoutParams lp = (LayoutParams) view.getLayoutParams();

                // Judge if the beginning or middle divider is needed
                if (hasDividerBeforeChildAtAlongMainAxis(currentViewIndex, j)) {
                    int dividerTop;
                    if (fromBottomToTop) {
                        dividerTop = view.getBottom() + lp.bottomMargin;
                    } else {
                        dividerTop = view.getTop() - lp.topMargin - mDividerHorizontalHeight;
                    }

                    drawHorizontalDivider(canvas, flexLine.mLeft, dividerTop, flexLine.mCrossSize);
                }

                // Judge if the end divider is needed
                if (j == flexLine.mItemCount - 1) {
                    if ((mShowDividerHorizontal & SHOW_DIVIDER_END) > 0) {
                        int dividerTop;
                        if (fromBottomToTop) {
                            dividerTop = view.getTop() - lp.topMargin - mDividerHorizontalHeight;
                        } else {
                            dividerTop = view.getBottom() + lp.bottomMargin;
                        }

                        drawHorizontalDivider(canvas, flexLine.mLeft, dividerTop,
                                flexLine.mCrossSize);
                    }
                }
                currentViewIndex++;
            }

            // Judge if the beginning or middle dividers are needed before the flex line
            if (hasDividerBeforeFlexLine(i)) {
                int verticalDividerLeft;
                if (isRtl) {
                    verticalDividerLeft = flexLine.mRight;
                } else {
                    verticalDividerLeft = flexLine.mLeft - mDividerVerticalWidth;
                }
                drawVerticalDivider(canvas, verticalDividerLeft, paddingTop,
                        verticalDividerLength);
            }
            if (hasEndDividerAfterFlexLine(i)) {
                if ((mShowDividerVertical & SHOW_DIVIDER_END) > 0) {
                    int verticalDividerLeft;
                    if (isRtl) {
                        verticalDividerLeft = flexLine.mLeft - mDividerVerticalWidth;
                    } else {
                        verticalDividerLeft = flexLine.mRight;
                    }
                    drawVerticalDivider(canvas, verticalDividerLeft, paddingTop,
                            verticalDividerLength);
                }
            }
        }
    }

    private void drawVerticalDivider(Canvas canvas, int left, int top, int length) {
        if (mDividerDrawableVertical == null) {
            return;
        }
        mDividerDrawableVertical.setBounds(left, top, left + mDividerVerticalWidth, top + length);
        mDividerDrawableVertical.draw(canvas);
    }

    private void drawHorizontalDivider(Canvas canvas, int left, int top, int length) {
        if (mDividerDrawableHorizontal == null) {
            return;
        }
        mDividerDrawableHorizontal
                .setBounds(left, top, left + length, top + mDividerHorizontalHeight);
        mDividerDrawableHorizontal.draw(canvas);
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

    @FlexWrap
    @Override
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

    /**
     * @return the flex lines composing this flex container. This method returns a copy of the
     * original list excluding a dummy flex line (flex line that doesn't have any flex items in it
     * but used for the alignment along the cross axis).
     * Thus any changes of the returned list are not reflected to the original list.
     */
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
        int decorationLength = 0;
        if (isMainAxisDirectionHorizontal()) {
            if (hasDividerBeforeChildAtAlongMainAxis(childAbsoluteIndex,
                    childRelativeIndexInFlexLine)) {
                decorationLength += mDividerVerticalWidth;
            }
            if ((mShowDividerVertical & SHOW_DIVIDER_END) > 0) {
                decorationLength += mDividerVerticalWidth;
            }
        } else {
            if (hasDividerBeforeChildAtAlongMainAxis(childAbsoluteIndex,
                    childRelativeIndexInFlexLine)) {
                decorationLength += mDividerHorizontalHeight;
            }
            if ((mShowDividerHorizontal & SHOW_DIVIDER_END) > 0) {
                decorationLength += mDividerHorizontalHeight;
            }
        }
        return decorationLength;
    }

    @Override
    public void onNewFlexLineAdded(FlexLine flexLine) {
        // The size of the end divider isn't added until the flexLine is added to the flex container
        // take the divider width (or height) into account when adding the flex line.
        if (isMainAxisDirectionHorizontal()) {
            if ((mShowDividerVertical & SHOW_DIVIDER_END) > 0) {
                flexLine.mMainSize += mDividerVerticalWidth;
                flexLine.mDividerLengthInMainSize += mDividerVerticalWidth;
            }
        } else {
            if ((mShowDividerHorizontal & SHOW_DIVIDER_END) > 0) {
                flexLine.mMainSize += mDividerHorizontalHeight;
                flexLine.mDividerLengthInMainSize += mDividerHorizontalHeight;
            }
        }
    }

    @Override
    public int getChildWidthMeasureSpec(int widthSpec, int padding, int childDimension) {
        return getChildMeasureSpec(widthSpec, padding, childDimension);
    }

    @Override
    public int getChildHeightMeasureSpec(int heightSpec, int padding, int childDimension) {
        return getChildMeasureSpec(heightSpec, padding, childDimension);
    }

    @Override
    public void onNewFlexItemAdded(int childAbsoluteIndex, int childRelativeIndexInFlexLine,
            FlexLine flexLine) {
        // Check if the beginning or middle divider is required for the flex item
        if (hasDividerBeforeChildAtAlongMainAxis(childAbsoluteIndex,
                childRelativeIndexInFlexLine)) {
            if (isMainAxisDirectionHorizontal()) {
                flexLine.mMainSize += mDividerVerticalWidth;
                flexLine.mDividerLengthInMainSize += mDividerVerticalWidth;
            } else {
                flexLine.mMainSize += mDividerHorizontalHeight;
                flexLine.mDividerLengthInMainSize += mDividerHorizontalHeight;
            }
        }
    }

    // From FlexContainerInternal
    @Override
    public void setFlexLines(List<FlexLine> flexLines) {
        mFlexLines = flexLines;
    }

    @Override
    public List<FlexLine> getFlexLinesInternal() {
        return mFlexLines;
    }
    // End of FlexContainerInternal

    /**
     * @return the horizontal divider drawable that will divide each item.
     * @see #setDividerDrawable(Drawable)
     * @see #setDividerDrawableHorizontal(Drawable)
     */
    public Drawable getDividerDrawableHorizontal() {
        return mDividerDrawableHorizontal;
    }

    /**
     * @return the vertical divider drawable that will divide each item.
     * @see #setDividerDrawable(Drawable)
     * @see #setDividerDrawableVertical(Drawable)
     */
    public Drawable getDividerDrawableVertical() {
        return mDividerDrawableVertical;
    }

    /**
     * Set a drawable to be used as a divider between items. The drawable is used for both
     * horizontal and vertical dividers.
     *
     * @param divider Drawable that will divide each item for both horizontally and vertically.
     * @see #setShowDivider(int)
     */
    public void setDividerDrawable(Drawable divider) {
        setDividerDrawableHorizontal(divider);
        setDividerDrawableVertical(divider);
    }

    /**
     * Set a drawable to be used as a horizontal divider between items.
     *
     * @param divider Drawable that will divide each item.
     * @see #setDividerDrawable(Drawable)
     * @see #setShowDivider(int)
     * @see #setShowDividerHorizontal(int)
     */
    public void setDividerDrawableHorizontal(Drawable divider) {
        if (divider == mDividerDrawableHorizontal) {
            return;
        }
        mDividerDrawableHorizontal = divider;
        if (divider != null) {
            mDividerHorizontalHeight = divider.getIntrinsicHeight();
        } else {
            mDividerHorizontalHeight = 0;
        }
        setWillNotDrawFlag();
        requestLayout();
    }

    /**
     * Set a drawable to be used as a vertical divider between items.
     *
     * @param divider Drawable that will divide each item.
     * @see #setDividerDrawable(Drawable)
     * @see #setShowDivider(int)
     * @see #setShowDividerVertical(int)
     */
    public void setDividerDrawableVertical(Drawable divider) {
        if (divider == mDividerDrawableVertical) {
            return;
        }
        mDividerDrawableVertical = divider;
        if (divider != null) {
            mDividerVerticalWidth = divider.getIntrinsicWidth();
        } else {
            mDividerVerticalWidth = 0;
        }
        setWillNotDrawFlag();
        requestLayout();
    }

    @FlexboxLayout.DividerMode
    public int getShowDividerVertical() {
        return mShowDividerVertical;
    }

    @FlexboxLayout.DividerMode
    public int getShowDividerHorizontal() {
        return mShowDividerHorizontal;
    }

    /**
     * Set how dividers should be shown between items in this layout. This method sets the
     * divider mode for both horizontally and vertically.
     *
     * @param dividerMode One or more of {@link #SHOW_DIVIDER_BEGINNING},
     *                    {@link #SHOW_DIVIDER_MIDDLE}, or {@link #SHOW_DIVIDER_END},
     *                    or {@link #SHOW_DIVIDER_NONE} to show no dividers.
     * @see #setShowDividerVertical(int)
     * @see #setShowDividerHorizontal(int)
     */
    public void setShowDivider(@DividerMode int dividerMode) {
        setShowDividerVertical(dividerMode);
        setShowDividerHorizontal(dividerMode);
    }

    /**
     * Set how vertical dividers should be shown between items in this layout
     *
     * @param dividerMode One or more of {@link #SHOW_DIVIDER_BEGINNING},
     *                    {@link #SHOW_DIVIDER_MIDDLE}, or {@link #SHOW_DIVIDER_END},
     *                    or {@link #SHOW_DIVIDER_NONE} to show no dividers.
     * @see #setShowDivider(int)
     */
    public void setShowDividerVertical(@DividerMode int dividerMode) {
        if (dividerMode != mShowDividerVertical) {
            mShowDividerVertical = dividerMode;
            requestLayout();
        }
    }

    /**
     * Set how horizontal dividers should be shown between items in this layout.
     *
     * @param dividerMode One or more of {@link #SHOW_DIVIDER_BEGINNING},
     *                    {@link #SHOW_DIVIDER_MIDDLE}, or {@link #SHOW_DIVIDER_END},
     *                    or {@link #SHOW_DIVIDER_NONE} to show no dividers.
     * @see #setShowDivider(int)
     */
    public void setShowDividerHorizontal(@DividerMode int dividerMode) {
        if (dividerMode != mShowDividerHorizontal) {
            mShowDividerHorizontal = dividerMode;
            requestLayout();
        }
    }

    private void setWillNotDrawFlag() {
        if (mDividerDrawableHorizontal == null && mDividerDrawableVertical == null) {
            setWillNotDraw(true);
        } else {
            setWillNotDraw(false);
        }
    }

    /**
     * Check if a divider is needed before the view whose indices are passed as arguments.
     *
     * @param index           the absolute index of the view to be judged
     * @param indexInFlexLine the relative index in the flex line where the view
     *                        belongs
     * @return {@code true} if a divider is needed, {@code false} otherwise
     */
    private boolean hasDividerBeforeChildAtAlongMainAxis(int index, int indexInFlexLine) {
        if (allViewsAreGoneBefore(index, indexInFlexLine)) {
            if (isMainAxisDirectionHorizontal()) {
                return (mShowDividerVertical & SHOW_DIVIDER_BEGINNING) != 0;
            } else {
                return (mShowDividerHorizontal & SHOW_DIVIDER_BEGINNING) != 0;
            }
        } else {
            if (isMainAxisDirectionHorizontal()) {
                return (mShowDividerVertical & SHOW_DIVIDER_MIDDLE) != 0;
            } else {
                return (mShowDividerHorizontal & SHOW_DIVIDER_MIDDLE) != 0;
            }
        }
    }

    private boolean allViewsAreGoneBefore(int index, int indexInFlexLine) {
        for (int i = 1; i <= indexInFlexLine; i++) {
            View view = getReorderedChildAt(index - i);
            if (view != null && view.getVisibility() != View.GONE) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a divider is needed before the flex line whose index is passed as an argument.
     *
     * @param flexLineIndex the index of the flex line to be checked
     * @return {@code true} if a divider is needed, {@code false} otherwise
     */
    private boolean hasDividerBeforeFlexLine(int flexLineIndex) {
        if (flexLineIndex < 0 || flexLineIndex >= mFlexLines.size()) {
            return false;
        }
        if (allFlexLinesAreDummyBefore(flexLineIndex)) {
            if (isMainAxisDirectionHorizontal()) {
                return (mShowDividerHorizontal & SHOW_DIVIDER_BEGINNING) != 0;
            } else {
                return (mShowDividerVertical & SHOW_DIVIDER_BEGINNING) != 0;
            }
        } else {
            if (isMainAxisDirectionHorizontal()) {
                return (mShowDividerHorizontal & SHOW_DIVIDER_MIDDLE) != 0;
            } else {
                return (mShowDividerVertical & SHOW_DIVIDER_MIDDLE) != 0;
            }
        }
    }

    private boolean allFlexLinesAreDummyBefore(int flexLineIndex) {
        for (int i = 0; i < flexLineIndex; i++) {
            if (mFlexLines.get(i).mItemCount > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a end divider is needed after the flex line whose index is passed as an argument.
     *
     * @param flexLineIndex the index of the flex line to be checked
     * @return {@code true} if a divider is needed, {@code false} otherwise
     */
    private boolean hasEndDividerAfterFlexLine(int flexLineIndex) {
        if (flexLineIndex < 0 || flexLineIndex >= mFlexLines.size()) {
            return false;
        }

        for (int i = flexLineIndex + 1; i < mFlexLines.size(); i++) {
            if (mFlexLines.get(i).mItemCount > 0) {
                return false;
            }
        }
        if (isMainAxisDirectionHorizontal()) {
            return (mShowDividerHorizontal & SHOW_DIVIDER_END) != 0;
        } else {
            return (mShowDividerVertical & SHOW_DIVIDER_END) != 0;
        }

    }

    /**
     * Per child parameters for children views of the {@link FlexboxLayout}.
     *
     * Note that some parent fields (which are not primitive nor a class implements
     * {@link Parcelable}) are not included as the stored/restored fields after this class
     * is serialized/de-serialized as an {@link Parcelable}.
     */
    public static class LayoutParams extends ViewGroup.MarginLayoutParams implements FlexItem {

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

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);

            TypedArray a = context
                    .obtainStyledAttributes(attrs, R.styleable.FlexboxLayout_Layout);
            mOrder = a.getInt(R.styleable.FlexboxLayout_Layout_layout_order, ORDER_DEFAULT);
            mFlexGrow = a
                    .getFloat(R.styleable.FlexboxLayout_Layout_layout_flexGrow, FLEX_GROW_DEFAULT);
            mFlexShrink = a.getFloat(R.styleable.FlexboxLayout_Layout_layout_flexShrink,
                    FLEX_SHRINK_DEFAULT);
            mAlignSelf = a
                    .getInt(R.styleable.FlexboxLayout_Layout_layout_alignSelf, AlignSelf.AUTO);
            mFlexBasisPercent = a
                    .getFraction(R.styleable.FlexboxLayout_Layout_layout_flexBasisPercent, 1, 1,
                            FLEX_BASIS_PERCENT_DEFAULT);
            mMinWidth = a
                    .getDimensionPixelSize(R.styleable.FlexboxLayout_Layout_layout_minWidth, 0);
            mMinHeight = a
                    .getDimensionPixelSize(R.styleable.FlexboxLayout_Layout_layout_minHeight, 0);
            mMaxWidth = a.getDimensionPixelSize(R.styleable.FlexboxLayout_Layout_layout_maxWidth,
                    MAX_SIZE);
            mMaxHeight = a.getDimensionPixelSize(R.styleable.FlexboxLayout_Layout_layout_maxHeight,
                    MAX_SIZE);
            mWrapBefore = a.getBoolean(R.styleable.FlexboxLayout_Layout_layout_wrapBefore, false);
            a.recycle();
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

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(int width, int height) {
            super(new ViewGroup.LayoutParams(width, height));
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

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
        public int getOrder() {
            return mOrder;
        }

        @Override
        public void setOrder(int order) {
            mOrder = order;
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