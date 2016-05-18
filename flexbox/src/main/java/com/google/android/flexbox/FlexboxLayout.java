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
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
 * </ul>
 */
public class FlexboxLayout extends ViewGroup {

    @IntDef({FLEX_DIRECTION_ROW, FLEX_DIRECTION_ROW_REVERSE, FLEX_DIRECTION_COLUMN,
            FLEX_DIRECTION_COLUMN_REVERSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FlexDirection {

    }

    public static final int FLEX_DIRECTION_ROW = 0;

    public static final int FLEX_DIRECTION_ROW_REVERSE = 1;

    public static final int FLEX_DIRECTION_COLUMN = 2;

    public static final int FLEX_DIRECTION_COLUMN_REVERSE = 3;

    /**
     * The direction children items are placed inside the Flexbox layout, it determines the
     * direction of the main axis (and the cross axis, perpendicular to the main axis).
     * <ul>
     * <li>
     * {@link #FLEX_DIRECTION_ROW}: Main axis direction -> horizontal. Main start to
     * main end -> Left to right (in LTR languages).
     * Cross start to cross end -> Top to bottom
     * </li>
     * <li>
     * {@link #FLEX_DIRECTION_ROW_REVERSE}: Main axis direction -> horizontal. Main start
     * to main end -> Right to left (in LTR languages). Cross start to cross end ->
     * Top to bottom.
     * </li>
     * <li>
     * {@link #FLEX_DIRECTION_COLUMN}: Main axis direction -> vertical. Main start
     * to main end -> Top to bottom. Cross start to cross end ->
     * Left to right (In LTR languages).
     * </li>
     * <li>
     * {@link #FLEX_DIRECTION_COLUMN_REVERSE}: Main axis direction -> vertical. Main start
     * to main end -> Bottom to top. Cross start to cross end -> Left to right
     * (In LTR languages)
     * </li>
     * </ul>
     * The default value is {@link #FLEX_DIRECTION_ROW}.
     */
    private int mFlexDirection;


    @IntDef({FLEX_WRAP_NOWRAP, FLEX_WRAP_WRAP, FLEX_WRAP_WRAP_REVERSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FlexWrap {

    }

    public static final int FLEX_WRAP_NOWRAP = 0;

    public static final int FLEX_WRAP_WRAP = 1;

    public static final int FLEX_WRAP_WRAP_REVERSE = 2;

    /**
     * This attribute controls whether the flex container is single-line or multi-line, and the
     * direction of the cross axis.
     * <ul>
     * <li>{@link #FLEX_WRAP_NOWRAP}: The flex container is single-line.</li>
     * <li>{@link #FLEX_WRAP_WRAP}: The flex container is multi-line.</li>
     * <li>{@link #FLEX_WRAP_WRAP_REVERSE}: The flex container is multi-line. The direction of the
     * cross axis is opposed to the direction as the {@link #FLEX_WRAP_WRAP}</li>
     * </ul>
     * The default value is {@link #FLEX_WRAP_NOWRAP}.
     */
    private int mFlexWrap;


    @IntDef({JUSTIFY_CONTENT_FLEX_START, JUSTIFY_CONTENT_FLEX_END, JUSTIFY_CONTENT_CENTER,
            JUSTIFY_CONTENT_SPACE_BETWEEN, JUSTIFY_CONTENT_SPACE_AROUND})
    @Retention(RetentionPolicy.SOURCE)
    public @interface JustifyContent {

    }

    public static final int JUSTIFY_CONTENT_FLEX_START = 0;

    public static final int JUSTIFY_CONTENT_FLEX_END = 1;

    public static final int JUSTIFY_CONTENT_CENTER = 2;

    public static final int JUSTIFY_CONTENT_SPACE_BETWEEN = 3;

    public static final int JUSTIFY_CONTENT_SPACE_AROUND = 4;

    /**
     * This attribute controls the alignment along the main axis.
     * The default value is {@link #JUSTIFY_CONTENT_FLEX_START}.
     */
    private int mJustifyContent;


    @IntDef({ALIGN_ITEMS_FLEX_START, ALIGN_ITEMS_FLEX_END, ALIGN_ITEMS_CENTER,
            ALIGN_ITEMS_BASELINE, ALIGN_ITEMS_STRETCH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AlignItems {

    }

    public static final int ALIGN_ITEMS_FLEX_START = 0;

    public static final int ALIGN_ITEMS_FLEX_END = 1;

    public static final int ALIGN_ITEMS_CENTER = 2;

    public static final int ALIGN_ITEMS_BASELINE = 3;

    public static final int ALIGN_ITEMS_STRETCH = 4;

    /**
     * This attribute controls the alignment along the cross axis.
     * The default value is {@link #ALIGN_ITEMS_STRETCH}.
     */
    private int mAlignItems;


    @IntDef({ALIGN_CONTENT_FLEX_START, ALIGN_CONTENT_FLEX_END, ALIGN_CONTENT_CENTER,
            ALIGN_CONTENT_SPACE_BETWEEN, ALIGN_CONTENT_SPACE_AROUND, ALIGN_CONTENT_STRETCH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AlignContent {

    }

    public static final int ALIGN_CONTENT_FLEX_START = 0;

    public static final int ALIGN_CONTENT_FLEX_END = 1;

    public static final int ALIGN_CONTENT_CENTER = 2;

    public static final int ALIGN_CONTENT_SPACE_BETWEEN = 3;

    public static final int ALIGN_CONTENT_SPACE_AROUND = 4;

    public static final int ALIGN_CONTENT_STRETCH = 5;

    /**
     * This attribute controls the alignment of the flex lines in the flex container.
     * The default value is {@link #ALIGN_CONTENT_STRETCH}.
     */
    private int mAlignContent;

    /**
     * Holds reordered indices, which {@link LayoutParams#order} parameters are taken into account
     */
    private int[] mReorderedIndices;

    private List<FlexLine> mFlexLines = new ArrayList<>();

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
        mReorderedIndices = createReorderedIndices();
        // TODO: Only calculate the children views which are affected from the last measure.

        switch (mFlexDirection) {
            case FLEX_DIRECTION_ROW: // Intentional fall through
            case FLEX_DIRECTION_ROW_REVERSE:
                measureHorizontal(widthMeasureSpec, heightMeasureSpec);
                break;
            case FLEX_DIRECTION_COLUMN: // Intentional fall through
            case FLEX_DIRECTION_COLUMN_REVERSE:
                measureVertical(widthMeasureSpec, heightMeasureSpec);
                break;
            default:
                throw new IllegalStateException(
                        "Invalid value for the flex direction is set: " + mFlexDirection);
        }
    }

    /**
     * Returns a View, which is reordered by taking {@link LayoutParams#order} parameters
     * into account.
     *
     * @param index the index of the view
     * @return the reordered view, which {@link LayoutParams@order} is taken into account.
     * If the index is negative or out of bounds of the number of contained views,
     * returns {@code null}.
     */
    public View getReorderedChildAt(int index) {
        if (index < 0 || index >= mReorderedIndices.length) {
            return null;
        }
        return getChildAt(mReorderedIndices[index]);
    }

    /**
     * Create an array, which indicates the reordered indices that {@link LayoutParams#order}
     * attributes are taken into account.
     *
     * @return an array which have the reordered indices
     */
    private int[] createReorderedIndices() {
        int[] reorderedIndex = new int[getChildCount()];
        int count = getChildCount();
        SortedSet<Order> orderSet = new TreeSet<>();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            Order order = new Order();
            order.order = params.order;
            order.index = i;
            orderSet.add(order);
        }

        int i = 0;
        for (Order order : orderSet) {
            reorderedIndex[i] = order.index;
            i++;
        }
        return reorderedIndex;
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
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int childState = 0;

        mFlexLines.clear();

        // Determine how many flex lines are needed in this layout by measuring each child.
        // (Expand or shrink the view depending on the flexGrow and flexShrink attributes in a later
        // loop)
        {
            int childCount = getChildCount();
            int paddingStart = ViewCompat.getPaddingStart(this);
            int paddingEnd = ViewCompat.getPaddingEnd(this);
            int largestHeightInRow = Integer.MIN_VALUE;
            FlexLine flexLine = new FlexLine();
            flexLine.mainSize = paddingStart;
            for (int i = 0; i < childCount; i++) {
                View child = getReorderedChildAt(i);
                if (child == null) {
                    continue;
                } else if (child.getVisibility() == View.GONE) {
                    flexLine.itemCount++;
                    continue;
                }

                FlexboxLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.alignSelf == LayoutParams.ALIGN_SELF_STRETCH) {
                    flexLine.indicesAlignSelfStretch.add(i);
                }

                int childWidth = lp.width;
                if (lp.flexBasisPercent != LayoutParams.FLEX_BASIS_PERCENT_DEFAULT
                        && widthMode == MeasureSpec.EXACTLY) {
                    childWidth = Math.round(widthSize * lp.flexBasisPercent);
                    // Use the dimension from the layout_width attribute if the widthMode is not
                    // MeasureSpec.EXACTLY even if any fraction value is set to
                    // layout_flexBasisPercent.
                    // There are likely quite few use cases where assigning any fraction values
                    // with widthMode is not MeasureSpec.EXACTLY (e.g. FlexboxLayout's layout_width
                    // is set to wrap_content)
                }
                int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                        getPaddingLeft() + getPaddingRight() + lp.leftMargin
                                + lp.rightMargin, childWidth);
                int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                        getPaddingTop() + getPaddingBottom() + lp.topMargin
                                + lp.bottomMargin, lp.height);
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                largestHeightInRow = Math.max(largestHeightInRow,
                        child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);

                if (isWrapRequired(mFlexWrap, widthMode, widthSize, flexLine.mainSize,
                        child.getMeasuredWidth())) {
                    flexLine.mainSize += paddingEnd;
                    mFlexLines.add(flexLine);

                    flexLine = new FlexLine();
                    flexLine.itemCount = 1;
                    flexLine.mainSize = paddingStart;
                    largestHeightInRow = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
                } else {
                    flexLine.itemCount++;
                }
                flexLine.mainSize += child.getMeasuredWidth() + lp.leftMargin
                        + lp.rightMargin;
                flexLine.totalFlexGrow += lp.flexGrow;
                flexLine.totalFlexShrink += lp.flexShrink;
                // Temporarily set the cross axis length as the largest child in the row
                // Expand along the cross axis depending on the mAlignContent property if needed
                // later
                flexLine.crossSize = Math.max(flexLine.crossSize, largestHeightInRow);

                if (mFlexWrap != FLEX_WRAP_WRAP_REVERSE) {
                    flexLine.maxBaseline = Math
                            .max(flexLine.maxBaseline, child.getBaseline() + lp.topMargin);
                } else {
                    // if the flex wrap property is FLEX_WRAP_WRAP_REVERSE, calculate the
                    // baseline as the distance from the cross end and the baseline
                    // since the cross size calculation is based on the distance from the cross end
                    flexLine.maxBaseline = Math
                            .max(flexLine.maxBaseline,
                                    child.getMeasuredHeight() - child.getBaseline()
                                            + lp.bottomMargin);
                }
                if (i == childCount - 1 && flexLine.itemCount != 0) {
                    // Add the flex line if this item is the last item
                    flexLine.mainSize += paddingEnd;
                    mFlexLines.add(flexLine);
                }
            }
        }

        determineMainSize(mFlexDirection, widthMeasureSpec, heightMeasureSpec);

        // TODO: Consider the case any individual child's alignSelf is set to ALIGN_SELF_BASELINE
        if (mAlignItems == ALIGN_ITEMS_BASELINE) {
            int viewIndex = 0;
            for (FlexLine flexLine : mFlexLines) {
                // The largest height value that also take the baseline shift into account
                int largestHeightInLine = Integer.MIN_VALUE;
                for (int i = viewIndex; i < viewIndex + flexLine.itemCount; i++) {
                    View child = getReorderedChildAt(i);
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    if (mFlexWrap != FLEX_WRAP_WRAP_REVERSE) {
                        int marginTop = flexLine.maxBaseline - child.getBaseline();
                        marginTop = Math.max(marginTop, lp.topMargin);
                        largestHeightInLine = Math.max(largestHeightInLine,
                                child.getHeight() + marginTop + lp.bottomMargin);
                    } else {
                        int marginBottom = flexLine.maxBaseline - child.getMeasuredHeight() +
                                child.getBaseline();
                        marginBottom = Math.max(marginBottom, lp.bottomMargin);
                        largestHeightInLine = Math.max(largestHeightInLine,
                                child.getHeight() + lp.topMargin + marginBottom);
                    }
                }
                flexLine.crossSize = largestHeightInLine;
                viewIndex += flexLine.itemCount;
            }
        }

        determineCrossSize(mFlexDirection, widthMeasureSpec, heightMeasureSpec);
        // Now cross size for each flex line is determined.
        // Expand the views if alignItems (or alignSelf in each child view) is set to stretch
        stretchViews(mFlexDirection, mAlignItems);
        setMeasuredDimensionForFlex(mFlexDirection, widthMeasureSpec, heightMeasureSpec,
                childState);
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
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int childState = 0;

        mFlexLines.clear();

        // Determine how many flex lines are needed in this layout by measuring each child.
        // (Expand or shrink the view depending on the flexGrow and flexShrink attributes in a later
        // loop)
        int childCount = getChildCount();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int largestWidthInColumn = Integer.MIN_VALUE;
        FlexLine flexLine = new FlexLine();
        flexLine.mainSize = paddingTop;
        for (int i = 0; i < childCount; i++) {
            View child = getReorderedChildAt(i);
            if (child == null) {
                continue;
            } else if (child.getVisibility() == View.GONE) {
                flexLine.itemCount++;
                continue;
            }

            FlexboxLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.alignSelf == LayoutParams.ALIGN_SELF_STRETCH) {
                flexLine.indicesAlignSelfStretch.add(i);
            }

            int childHeight = lp.height;
            if (lp.flexBasisPercent != LayoutParams.FLEX_BASIS_PERCENT_DEFAULT
                    && heightMode == MeasureSpec.EXACTLY) {
                childHeight = Math.round(heightSize * lp.flexBasisPercent);
                // Use the dimension from the layout_height attribute if the heightMode is not
                // MeasureSpec.EXACTLY even if any fraction value is set to layout_flexBasisPercent.
                // There are likely quite few use cases where assigning any fraction values
                // with heightMode is not MeasureSpec.EXACTLY (e.g. FlexboxLayout's layout_height
                // is set to wrap_content)
            }

            int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                    getPaddingLeft() + getPaddingRight() + lp.leftMargin
                            + lp.rightMargin, lp.width);
            int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                    getPaddingTop() + getPaddingBottom() + lp.topMargin
                            + lp.bottomMargin, childHeight);
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            childState = combineMeasuredStates(childState, child.getMeasuredState());
            largestWidthInColumn = Math.max(largestWidthInColumn,
                    child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);

            if (isWrapRequired(mFlexWrap, heightMode, heightSize, flexLine.mainSize,
                    child.getMeasuredHeight())) {
                flexLine.mainSize += paddingBottom;
                mFlexLines.add(flexLine);

                flexLine = new FlexLine();
                flexLine.itemCount = 1;
                flexLine.mainSize = paddingTop;
                largestWidthInColumn = child.getMeasuredWidth() + lp.leftMargin
                        + lp.rightMargin;
            } else {
                flexLine.itemCount++;
            }
            flexLine.mainSize += child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            flexLine.totalFlexGrow += lp.flexGrow;
            flexLine.totalFlexShrink += lp.flexShrink;
            // Temporarily set the cross axis length as the largest child width in the column
            // Expand along the cross axis depending on the mAlignContent property if needed
            // later
            flexLine.crossSize = Math.max(flexLine.crossSize, largestWidthInColumn);

            if (i == childCount - 1 && flexLine.itemCount != 0) {
                // Add the flex line if this item is the last item
                flexLine.mainSize += paddingBottom;
                mFlexLines.add(flexLine);
            }
        }

        determineMainSize(mFlexDirection, widthMeasureSpec, heightMeasureSpec);
        determineCrossSize(mFlexDirection, widthMeasureSpec, heightMeasureSpec);
        // Now cross size for each flex line is determined.
        // Expand the views if alignItems (or alignSelf in each child view) is set to stretch
        stretchViews(mFlexDirection, mAlignItems);
        setMeasuredDimensionForFlex(mFlexDirection, widthMeasureSpec, heightMeasureSpec,
                childState);
    }

    /**
     * Determine the main size by expanding (shrinking if negative remaining free space is given)
     * an individual child in each flex line if any children's flexGrow (or flexShrink if remaining
     * space is negative) properties are set to non-zero.
     *
     * @param flexDirection     the value of the flex direction
     * @param widthMeasureSpec  horizontal space requirements as imposed by the parent
     * @param heightMeasureSpec vertical space requirements as imposed by the parent
     * @see #setFlexDirection(int)
     * @see #getFlexDirection()
     */
    private void determineMainSize(@FlexDirection int flexDirection, int widthMeasureSpec,
            int heightMeasureSpec) {
        int mainSize;
        int paddingAlongMainAxis;
        switch (flexDirection) {
            case FLEX_DIRECTION_ROW: // Intentional fall through
            case FLEX_DIRECTION_ROW_REVERSE:
                int widthMode = MeasureSpec.getMode(widthMeasureSpec);
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                if (widthMode == MeasureSpec.EXACTLY) {
                    mainSize = widthSize;
                } else {
                    mainSize = getLargestMainSize();
                }
                paddingAlongMainAxis = getPaddingLeft() + getPaddingRight();
                break;
            case FLEX_DIRECTION_COLUMN: // Intentional fall through
            case FLEX_DIRECTION_COLUMN_REVERSE:
                int heightMode = MeasureSpec.getMode(heightMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);
                if (heightMode == MeasureSpec.EXACTLY) {
                    mainSize = heightSize;
                } else {
                    mainSize = getLargestMainSize();
                }
                paddingAlongMainAxis = getPaddingTop() + getPaddingBottom();
                break;
            default:
                throw new IllegalArgumentException("Invalid flex direction: " + flexDirection);
        }

        int childIndex = 0;
        for (FlexLine flexLine : mFlexLines) {
            if (flexLine.mainSize < mainSize) {
                childIndex = expandFlexItems(flexLine, flexDirection, mainSize,
                        paddingAlongMainAxis, childIndex);
            } else {
                childIndex = shrinkFlexItems(flexLine, flexDirection, mainSize,
                        paddingAlongMainAxis, childIndex);
            }
        }
    }

    /**
     * Expand the flex items along the main axis based on the individual flexGrow attribute.
     *
     * @param flexLine             the flex line to which flex items belong
     * @param flexDirection        the flexDirection value for this FlexboxLayout
     * @param maxMainSize          the maximum main size. Expanded main size will be this size
     * @param paddingAlongMainAxis the padding value along the main axis
     * @param startIndex           the start index of the children views to be expanded. This index
     *                             needs to
     *                             be an absolute index in the flex container (FlexboxLayout),
     *                             not the relative index in the flex line.
     * @return the next index, the next flex line's first flex item starts from the returned index
     * @see #getFlexDirection()
     * @see #setFlexDirection(int)
     * @see LayoutParams#flexGrow
     */
    private int expandFlexItems(FlexLine flexLine, @FlexDirection int flexDirection,
            int maxMainSize, int paddingAlongMainAxis, int startIndex) {
        if (flexLine.totalFlexGrow <= 0 || maxMainSize < flexLine.mainSize) {
            startIndex += flexLine.itemCount;
            return startIndex;
        }
        float unitSpace = (maxMainSize - flexLine.mainSize) / flexLine.totalFlexGrow;
        flexLine.mainSize = paddingAlongMainAxis;
        float accumulatedRoundError = 0;
        for (int i = 0; i < flexLine.itemCount; i++) {
            View child = getReorderedChildAt(startIndex);
            if (child == null) {
                continue;
            } else if (child.getVisibility() == View.GONE) {
                startIndex++;
                continue;
            }
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (flexDirection == FLEX_DIRECTION_ROW
                    || flexDirection == FLEX_DIRECTION_ROW_REVERSE) {
                float rawCalculatedWidth = child.getMeasuredWidth() + unitSpace * lp.flexGrow;
                if (i == flexLine.itemCount - 1) {
                    rawCalculatedWidth += accumulatedRoundError;
                    accumulatedRoundError = 0;
                }
                int newWidth = Math.round(rawCalculatedWidth);
                accumulatedRoundError += (rawCalculatedWidth - newWidth);
                if (accumulatedRoundError > 1.0) {
                    newWidth += 1;
                    accumulatedRoundError -= 1.0;
                } else if (accumulatedRoundError < -1.0) {
                    newWidth -= 1;
                    accumulatedRoundError += 1.0;
                }
                child.measure(MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY),
                        MeasureSpec
                                .makeMeasureSpec(child.getMeasuredHeight(),
                                        MeasureSpec.EXACTLY));
                flexLine.mainSize += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            } else {
                float rawCalculatedHeight = child.getMeasuredHeight() + unitSpace * lp.flexGrow;
                if (i == flexLine.itemCount - 1) {
                    rawCalculatedHeight += accumulatedRoundError;
                    accumulatedRoundError = 0;
                }
                int newHeight = Math.round(rawCalculatedHeight);
                accumulatedRoundError += (rawCalculatedHeight - newHeight);
                if (accumulatedRoundError > 1.0) {
                    newHeight += 1;
                    accumulatedRoundError -= 1.0;
                } else if (accumulatedRoundError < -1.0) {
                    newHeight -= 1;
                    accumulatedRoundError += 1.0;
                }
                child.measure(MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(),
                        MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY));
                flexLine.mainSize += child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            }
            startIndex++;
        }
        return startIndex;
    }

    /**
     * Shrink the flex items along the main axis based on the individual flexShrink attribute.
     *
     * @param flexLine             the flex line to which flex items belong
     * @param flexDirection        the flexDirection value for this FlexboxLayout
     * @param maxMainSize          the maximum main size. Shrank main size will be this size
     * @param paddingAlongMainAxis the padding value along the main axis
     * @param startIndex           the start index of the children views to be shrank. This index
     *                             needs to
     *                             be an absolute index in the flex container (FlexboxLayout),
     *                             not the relative index in the flex line.
     * @return the next index, the next flex line's first flex item starts from the returned index
     * @see #getFlexDirection()
     * @see #setFlexDirection(int)
     * @see LayoutParams#flexShrink
     */
    private int shrinkFlexItems(FlexLine flexLine, @FlexDirection int flexDirection,
            int maxMainSize, int paddingAlongMainAxis, int startIndex) {
        int childIndex = startIndex;
        int sizeBeforeShrink = flexLine.mainSize;
        if (flexLine.totalFlexShrink <= 0 || maxMainSize > flexLine.mainSize) {
            childIndex += flexLine.itemCount;
            return childIndex;
        }
        boolean needsReshrink = false;
        float unitShrink = (flexLine.mainSize - maxMainSize) / flexLine.totalFlexShrink;
        float accumulatedRoundError = 0;
        flexLine.mainSize = paddingAlongMainAxis;
        for (int i = 0; i < flexLine.itemCount; i++) {
            View child = getReorderedChildAt(childIndex);
            if (child == null) {
                continue;
            } else if (child.getVisibility() == View.GONE) {
                childIndex++;
                continue;
            }
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (flexDirection == FLEX_DIRECTION_ROW
                    || flexDirection == FLEX_DIRECTION_ROW_REVERSE) {
                float rawCalculatedWidth = child.getMeasuredWidth() - unitShrink * lp.flexShrink;
                if (i == flexLine.itemCount - 1) {
                    rawCalculatedWidth += accumulatedRoundError;
                    accumulatedRoundError = 0;
                }
                int newWidth = Math.round(rawCalculatedWidth);
                if (newWidth < 0) {
                    // This means the child doesn't have enough space to distribute the negative
                    // free space. To adjust the flex line length down to the maxMainSize, remaining
                    // negative free space needs to be re-distributed to other flex items
                    // (children views). In that case, invoke this method again with the same
                    // startIndex.
                    needsReshrink = true;
                    newWidth = 0;
                } else {
                    accumulatedRoundError += (rawCalculatedWidth - newWidth);
                    if (accumulatedRoundError > 1.0) {
                        newWidth += 1;
                        accumulatedRoundError -= 1;
                    } else if (accumulatedRoundError < -1.0) {
                        newWidth -= 1;
                        accumulatedRoundError += 1;
                    }
                }
                child.measure(MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY),
                        MeasureSpec
                                .makeMeasureSpec(child.getMeasuredHeight(),
                                        MeasureSpec.EXACTLY));
                flexLine.mainSize += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            } else {
                float rawCalculatedHeight = child.getMeasuredHeight() - unitShrink * lp.flexShrink;
                if (i == flexLine.itemCount - 1) {
                    rawCalculatedHeight += accumulatedRoundError;
                    accumulatedRoundError = 0;
                }
                int newHeight = Math.round(rawCalculatedHeight);
                if (newHeight < 0) {
                    // Need to invoke this method again like the case flex direction is vertical
                    needsReshrink = true;
                    newHeight = 0;
                } else {
                    accumulatedRoundError += (rawCalculatedHeight - newHeight);
                    if (accumulatedRoundError > 1.0) {
                        newHeight += 1;
                        accumulatedRoundError -= 1;
                    } else if (accumulatedRoundError < -1.0) {
                        newHeight -= 1;
                        accumulatedRoundError += 1;
                    }
                }
                child.measure(MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(),
                        MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY));
                flexLine.mainSize += child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            }
            childIndex++;
        }

        if (needsReshrink && sizeBeforeShrink != flexLine.mainSize) {
            // Re-invoke the method with the same startIndex to distribute the negative free space
            // that wasn't fully distributed (because some views length were not enough)
            shrinkFlexItems(flexLine, flexDirection, maxMainSize, paddingAlongMainAxis, startIndex);
        }
        return childIndex;
    }

    /**
     * Determines the cross size (Calculate the length along the cross axis).
     * Expand the cross size only if the height mode is MeasureSpec.EXACTLY, otherwise
     * use the sum of cross sizes of all flex lines.
     *
     * @param flexDirection     the flex direction attribute
     * @param widthMeasureSpec  horizontal space requirements as imposed by the parent
     * @param heightMeasureSpec vertical space requirements as imposed by the parent
     * @see #getFlexDirection()
     * @see #setFlexDirection(int)
     * @see #getAlignContent()
     * @see #setAlignContent(int)
     */
    private void determineCrossSize(int flexDirection, int widthMeasureSpec,
            int heightMeasureSpec) {
        // The MeasureSpec mode along the cross axis
        int mode;
        // The MeasureSpec size along the cross axis
        int size;
        switch (flexDirection) {
            case FLEX_DIRECTION_ROW: // Intentional fall through
            case FLEX_DIRECTION_ROW_REVERSE:
                mode = MeasureSpec.getMode(heightMeasureSpec);
                size = MeasureSpec.getSize(heightMeasureSpec);
                break;
            case FLEX_DIRECTION_COLUMN: // Intentional fall through
            case FLEX_DIRECTION_COLUMN_REVERSE:
                mode = MeasureSpec.getMode(widthMeasureSpec);
                size = MeasureSpec.getSize(widthMeasureSpec);
                break;
            default:
                throw new IllegalArgumentException("Invalid flex direction: " + flexDirection);
        }
        if (mode == MeasureSpec.EXACTLY) {
            int totalCrossSize = getSumOfCrossSize();
            if (mFlexLines.size() == 1) {
                mFlexLines.get(0).crossSize = size;
                // alignContent property is valid only if the Flexbox has at least two lines
            } else if (mFlexLines.size() >= 2 && totalCrossSize < size) {
                switch (mAlignContent) {
                    case ALIGN_CONTENT_STRETCH: {
                        float freeSpaceUnit = (size - totalCrossSize) / (float) mFlexLines.size();
                        float accumulatedError = 0;
                        for (int i = 0; i < mFlexLines.size(); i++) {
                            FlexLine flexLine = mFlexLines.get(i);
                            float newCrossSizeAsFloat = flexLine.crossSize + freeSpaceUnit;
                            if (i == mFlexLines.size() - 1) {
                                newCrossSizeAsFloat += accumulatedError;
                                accumulatedError = 0;
                            }
                            int newCrossSize = Math.round(newCrossSizeAsFloat);
                            accumulatedError += (newCrossSizeAsFloat - newCrossSize);
                            if (accumulatedError > 1) {
                                newCrossSize += 1;
                                accumulatedError -= 1;
                            } else if (accumulatedError < -1) {
                                newCrossSize -= 1;
                                accumulatedError += 1;
                            }
                            flexLine.crossSize = newCrossSize;
                        }
                        break;
                    }
                    case ALIGN_CONTENT_SPACE_AROUND: {
                        // The value of free space along the cross axis which needs to be put on top
                        // and below the bottom of each flex line.
                        int spaceTopAndBottom = size - totalCrossSize;
                        // The number of spaces along the cross axis
                        int numberOfSpaces = mFlexLines.size() * 2;
                        spaceTopAndBottom = spaceTopAndBottom / numberOfSpaces;
                        List<FlexLine> newFlexLines = new ArrayList<>();
                        FlexLine dummySpaceFlexLine = new FlexLine();
                        dummySpaceFlexLine.crossSize = spaceTopAndBottom;
                        for (FlexLine flexLine : mFlexLines) {
                            newFlexLines.add(dummySpaceFlexLine);
                            newFlexLines.add(flexLine);
                            newFlexLines.add(dummySpaceFlexLine);
                        }
                        mFlexLines = newFlexLines;
                        break;
                    }
                    case ALIGN_CONTENT_SPACE_BETWEEN: {
                        // The value of free space along the cross axis between each flex line.
                        float spaceBetweenFlexLine = size - totalCrossSize;
                        int numberOfSpaces = mFlexLines.size() - 1;
                        spaceBetweenFlexLine = spaceBetweenFlexLine / (float) numberOfSpaces;
                        float accumulatedError = 0;
                        List<FlexLine> newFlexLines = new ArrayList<>();
                        for (int i = 0; i < mFlexLines.size(); i++) {
                            FlexLine flexLine = mFlexLines.get(i);
                            newFlexLines.add(flexLine);

                            if (i != mFlexLines.size() - 1) {
                                FlexLine dummySpaceFlexLine = new FlexLine();
                                if (i == mFlexLines.size() - 2) {
                                    // The last dummy space block in the flex container.
                                    // Adjust the cross size by the accumulated error.
                                    dummySpaceFlexLine.crossSize = Math
                                            .round(spaceBetweenFlexLine + accumulatedError);
                                    accumulatedError = 0;
                                } else {
                                    dummySpaceFlexLine.crossSize = Math.round(spaceBetweenFlexLine);
                                }
                                accumulatedError += (spaceBetweenFlexLine
                                        - dummySpaceFlexLine.crossSize);
                                if (accumulatedError > 1) {
                                    dummySpaceFlexLine.crossSize += 1;
                                    accumulatedError -= 1;
                                } else if (accumulatedError < -1) {
                                    dummySpaceFlexLine.crossSize -= 1;
                                    accumulatedError += 1;
                                }
                                newFlexLines.add(dummySpaceFlexLine);
                            }
                        }
                        mFlexLines = newFlexLines;
                        break;
                    }
                    case ALIGN_CONTENT_CENTER: {
                        int spaceAboveAndBottom = size - totalCrossSize;
                        spaceAboveAndBottom = spaceAboveAndBottom / 2;
                        List<FlexLine> newFlexLines = new ArrayList<>();
                        FlexLine dummySpaceFlexLine = new FlexLine();
                        dummySpaceFlexLine.crossSize = spaceAboveAndBottom;
                        for (int i = 0; i < mFlexLines.size(); i++) {
                            if (i == 0) {
                                newFlexLines.add(dummySpaceFlexLine);
                            }
                            FlexLine flexLine = mFlexLines.get(i);
                            newFlexLines.add(flexLine);
                            if (i == mFlexLines.size() - 1) {
                                newFlexLines.add(dummySpaceFlexLine);
                            }
                        }
                        mFlexLines = newFlexLines;
                        break;
                    }
                    case ALIGN_CONTENT_FLEX_END: {
                        int spaceTop = size - totalCrossSize;
                        FlexLine dummySpaceFlexLine = new FlexLine();
                        dummySpaceFlexLine.crossSize = spaceTop;
                        mFlexLines.add(0, dummySpaceFlexLine);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Expand the view if the {@link #mAlignItems} attribute is set to {@link #ALIGN_ITEMS_STRETCH}
     * or {@link LayoutParams#ALIGN_SELF_STRETCH} is set to an individual child view.
     *
     * @param flexDirection the flex direction attribute
     * @param alignItems    the align items attribute
     * @see #getFlexDirection()
     * @see #setFlexDirection(int)
     * @see #getAlignItems()
     * @see #setAlignItems(int)
     * @see LayoutParams#alignSelf
     */
    private void stretchViews(int flexDirection, int alignItems) {
        if (alignItems == ALIGN_ITEMS_STRETCH) {
            int viewIndex = 0;
            for (FlexLine flexLine : mFlexLines) {
                for (int i = 0; i < flexLine.itemCount; i++, viewIndex++) {
                    View view = getReorderedChildAt(viewIndex);
                    LayoutParams lp = (LayoutParams) view.getLayoutParams();
                    if (lp.alignSelf != LayoutParams.ALIGN_SELF_AUTO &&
                            lp.alignSelf != LayoutParams.ALIGN_SELF_STRETCH) {
                        continue;
                    }
                    switch (flexDirection) {
                        case FLEX_DIRECTION_ROW: // Intentional fall through
                        case FLEX_DIRECTION_ROW_REVERSE:
                            stretchViewVertically(view, flexLine.crossSize);
                            break;
                        case FLEX_DIRECTION_COLUMN:
                        case FLEX_DIRECTION_COLUMN_REVERSE:
                            stretchViewHorizontally(view, flexLine.crossSize);
                            break;
                        default:
                            throw new IllegalArgumentException(
                                    "Invalid flex direction: " + flexDirection);
                    }
                }
            }
        } else {
            for (FlexLine flexLine : mFlexLines) {
                for (Integer index : flexLine.indicesAlignSelfStretch) {
                    View view = getReorderedChildAt(index);
                    switch (flexDirection) {
                        case FLEX_DIRECTION_ROW: // Intentional fall through
                        case FLEX_DIRECTION_ROW_REVERSE:
                            stretchViewVertically(view, flexLine.crossSize);
                            break;
                        case FLEX_DIRECTION_COLUMN:
                        case FLEX_DIRECTION_COLUMN_REVERSE:
                            stretchViewHorizontally(view, flexLine.crossSize);
                            break;
                        default:
                            throw new IllegalArgumentException(
                                    "Invalid flex direction: " + flexDirection);
                    }
                }
            }
        }
    }

    /**
     * Expand the view vertically to the size of the crossSize (considering the view margins)
     *
     * @param view      the View to be stretched
     * @param crossSize the cross size
     */
    private void stretchViewVertically(View view, int crossSize) {
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        int newHeight = crossSize - lp.topMargin - lp.bottomMargin;
        newHeight = Math.max(newHeight, 0);
        view.measure(MeasureSpec
                        .makeMeasureSpec(view.getMeasuredWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY));
    }

    /**
     * Expand the view horizontally to the size of the crossSize (considering the view margins)
     *
     * @param view      the View to be stretched
     * @param crossSize the cross size
     */
    private void stretchViewHorizontally(View view, int crossSize) {
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        int newWidth = crossSize - lp.leftMargin - lp.rightMargin;
        newWidth = Math.max(newWidth, 0);
        view.measure(MeasureSpec
                        .makeMeasureSpec(newWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(view.getMeasuredHeight(), MeasureSpec.EXACTLY));
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
            case FLEX_DIRECTION_ROW: // Intentional fall through
            case FLEX_DIRECTION_ROW_REVERSE:
                calculatedMaxHeight = getSumOfCrossSize() + getPaddingTop() + getPaddingBottom();
                calculatedMaxWidth = getLargestMainSize();
                break;
            case FLEX_DIRECTION_COLUMN: // Intentional fall through
            case FLEX_DIRECTION_COLUMN_REVERSE:
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
                    childState = combineMeasuredStates(childState, MEASURED_STATE_TOO_SMALL);
                }
                widthSizeAndState = resolveSizeAndState(widthSize, widthMeasureSpec,
                        childState);
                break;
            case MeasureSpec.AT_MOST: {
                if (widthSize < calculatedMaxWidth) {
                    childState = combineMeasuredStates(childState, MEASURED_STATE_TOO_SMALL);
                } else {
                    widthSize = calculatedMaxWidth;
                }
                widthSizeAndState = resolveSizeAndState(widthSize, widthMeasureSpec,
                        childState);
                break;
            }
            case MeasureSpec.UNSPECIFIED: {
                widthSizeAndState = resolveSizeAndState(calculatedMaxWidth, widthMeasureSpec,
                        childState);
                break;
            }
            default:
                throw new IllegalStateException("Unknown width mode is set: " + widthMode);
        }
        int heightSizeAndState;
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                if (heightSize < calculatedMaxHeight) {
                    childState = combineMeasuredStates(childState,
                            MEASURED_STATE_TOO_SMALL >> MEASURED_HEIGHT_STATE_SHIFT);
                }
                heightSizeAndState = resolveSizeAndState(heightSize, heightMeasureSpec,
                        childState);
                break;
            case MeasureSpec.AT_MOST: {
                if (heightSize < calculatedMaxHeight) {
                    childState = combineMeasuredStates(childState,
                            MEASURED_STATE_TOO_SMALL >> MEASURED_HEIGHT_STATE_SHIFT);
                } else {
                    heightSize = calculatedMaxHeight;
                }
                heightSizeAndState = resolveSizeAndState(heightSize, heightMeasureSpec,
                        childState);
                break;
            }
            case MeasureSpec.UNSPECIFIED: {
                heightSizeAndState = resolveSizeAndState(calculatedMaxHeight,
                        heightMeasureSpec, childState);
                break;
            }
            default:
                throw new IllegalStateException("Unknown height mode is set: " + heightMode);
        }
        setMeasuredDimension(widthSizeAndState, heightSizeAndState);
    }

    /**
     * Determine if a wrap is required (add a new flex line).
     *
     * @param flexWrap      the flexWrap attribute
     * @param mode          the width or height mode along the main axis direction
     * @param maxSize       the max size along the main axis direction
     * @param currentLength the accumulated current length
     * @param childLength   the length of a child view which is to be collected to the flex line
     * @return {@code true} if a wrap is required, {@code false} otherwise
     * @see #getFlexWrap()
     * @see #setFlexWrap(int)
     */
    private boolean isWrapRequired(int flexWrap, int mode, int maxSize,
            int currentLength, int childLength) {
        return flexWrap != FLEX_WRAP_NOWRAP &&
                (mode == MeasureSpec.EXACTLY || mode == MeasureSpec.AT_MOST) &&
                maxSize < currentLength + childLength;
    }

    /**
     * Retrieve the largest main size of all flex lines.
     *
     * @return the largest main size
     */
    private int getLargestMainSize() {
        int largestSize = Integer.MIN_VALUE;
        for (FlexLine flexLine : mFlexLines) {
            largestSize = Math.max(largestSize, flexLine.mainSize);
        }
        return largestSize;
    }

    /**
     * Retrieve the sum of the cross sizes of all flex lines.
     *
     * @return the sum of the cross sizes
     */
    private int getSumOfCrossSize() {
        int sum = 0;
        for (FlexLine flexLine : mFlexLines) {
            sum += flexLine.crossSize;
        }
        return sum;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int layoutDirection = ViewCompat.getLayoutDirection(this);
        boolean isRtl;
        switch (mFlexDirection) {
            case FLEX_DIRECTION_ROW:
                isRtl = layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL;
                layoutHorizontal(isRtl, left, top, right, bottom);
                break;
            case FLEX_DIRECTION_ROW_REVERSE:
                isRtl = layoutDirection != ViewCompat.LAYOUT_DIRECTION_RTL;
                layoutHorizontal(isRtl, left, top, right, bottom);
                break;
            case FLEX_DIRECTION_COLUMN:
                isRtl = layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL;
                if (mFlexWrap == FLEX_WRAP_WRAP_REVERSE) {
                    isRtl = !isRtl;
                }
                layoutVertical(isRtl, false, left, top, right, bottom);
                break;
            case FLEX_DIRECTION_COLUMN_REVERSE:
                isRtl = layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL;
                if (mFlexWrap == FLEX_WRAP_WRAP_REVERSE) {
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
     * {@link #mFlexDirection} is either {@link #FLEX_DIRECTION_ROW} or
     * {@link #FLEX_DIRECTION_ROW_REVERSE}.
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
     * @see LayoutParams#alignSelf
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
        // childBottom is used if the mFlexWrap is FLEX_WRAP_WRAP_REVERSE otherwise
        // childTop is used to align the vertical position of the children views.
        int childBottom = height - getPaddingBottom();
        int childTop = getPaddingTop();

        // Used only for RTL layout
        // Use float to reduce the round error that may happen in when justifyContent ==
        // SPACE_BETWEEN or SPACE_AROUND
        float childRight;
        for (FlexLine flexLine : mFlexLines) {
            float spaceBetweenItem = 0f;
            switch (mJustifyContent) {
                case JUSTIFY_CONTENT_FLEX_START:
                    childLeft = paddingLeft;
                    childRight = width - paddingRight;
                    break;
                case JUSTIFY_CONTENT_FLEX_END:
                    childLeft = width - flexLine.mainSize + paddingRight;
                    childRight = flexLine.mainSize - paddingLeft;
                    break;
                case JUSTIFY_CONTENT_CENTER:
                    childLeft = paddingLeft + (width - flexLine.mainSize) / 2f;
                    childRight = width - paddingRight - (width - flexLine.mainSize) / 2f;
                    break;
                case JUSTIFY_CONTENT_SPACE_AROUND:
                    if (flexLine.itemCount != 0) {
                        spaceBetweenItem = (width - flexLine.mainSize) / (float) flexLine.itemCount;
                    }
                    childLeft = paddingLeft + spaceBetweenItem / 2f;
                    childRight = width - paddingRight - spaceBetweenItem / 2f;
                    break;
                case JUSTIFY_CONTENT_SPACE_BETWEEN:
                    childLeft = paddingLeft;
                    float denominator = flexLine.itemCount != 1 ? flexLine.itemCount - 1 : 1f;
                    spaceBetweenItem = (width - flexLine.mainSize) / denominator;
                    childRight = width - paddingRight;
                    break;
                default:
                    throw new IllegalStateException(
                            "Invalid justifyContent is set: " + mJustifyContent);
            }
            spaceBetweenItem = Math.max(spaceBetweenItem, 0);

            for (int i = 0; i < flexLine.itemCount; i++) {
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
                if (mFlexWrap == FLEX_WRAP_WRAP_REVERSE) {
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
            }
            childTop += flexLine.crossSize;
            childBottom -= flexLine.crossSize;
        }
    }

    /**
     * Place a single View when the layout direction is horizontal ({@link #mFlexDirection} is
     * either {@link #FLEX_DIRECTION_ROW} or {@link #FLEX_DIRECTION_ROW_REVERSE}).
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
     * @see LayoutParams#alignSelf
     */
    private void layoutSingleChildHorizontal(View view, FlexLine flexLine, @FlexWrap int flexWrap,
            int alignItems, int left, int top, int right, int bottom) {
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        if (lp.alignSelf != LayoutParams.ALIGN_SELF_AUTO) {
            // Expecting the values for alignItems and alignSelf match except for ALIGN_SELF_AUTO.
            // Assigning the alignSelf value as alignItems should work.
            alignItems = lp.alignSelf;
        }
        int crossSize = flexLine.crossSize;
        switch (alignItems) {
            case ALIGN_ITEMS_FLEX_START: // Intentional fall through
            case ALIGN_ITEMS_STRETCH:
                if (flexWrap != FLEX_WRAP_WRAP_REVERSE) {
                    view.layout(left, top + lp.topMargin, right, bottom + lp.topMargin);
                } else {
                    view.layout(left, top - lp.bottomMargin, right, bottom - lp.bottomMargin);
                }
                break;
            case ALIGN_ITEMS_BASELINE:
                if (flexWrap != FLEX_WRAP_WRAP_REVERSE) {
                    int marginTop = flexLine.maxBaseline - view.getBaseline();
                    marginTop = Math.max(marginTop, lp.topMargin);
                    view.layout(left, top + marginTop, right, bottom + marginTop);
                } else {
                    int marginBottom = flexLine.maxBaseline - view.getMeasuredHeight() + view
                            .getBaseline();
                    marginBottom = Math.max(marginBottom, lp.bottomMargin);
                    view.layout(left, top - marginBottom, right, bottom - marginBottom);
                }
                break;
            case ALIGN_ITEMS_FLEX_END:
                if (flexWrap != FLEX_WRAP_WRAP_REVERSE) {
                    view.layout(left,
                            top + crossSize - view.getMeasuredHeight() - lp.bottomMargin,
                            right, top + crossSize - lp.bottomMargin);
                } else {
                    // If the flexWrap == FLEX_WRAP_WRAP_REVERSE, the direction of the
                    // flexEnd is flipped (from top to bottom).
                    view.layout(left, top - crossSize + view.getMeasuredHeight() + lp.topMargin,
                            right, bottom - crossSize + view.getMeasuredHeight() + lp.topMargin);
                }
                break;
            case ALIGN_ITEMS_CENTER:
                int topFromCrossAxis = (crossSize - view.getMeasuredHeight()) / 2;
                if (flexWrap != FLEX_WRAP_WRAP_REVERSE) {
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
     * {@link #mFlexDirection} is either {@link #FLEX_DIRECTION_COLUMN} or
     * {@link #FLEX_DIRECTION_COLUMN_REVERSE}.
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
     * @see LayoutParams#alignSelf
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
        // childRight is used if the mFlexWrap is FLEX_WRAP_WRAP_REVERSE otherwise
        // childLeft is used to align the horizontal position of the children views.
        int childRight = width - paddingRight;

        // Use float to reduce the round error that may happen in when justifyContent ==
        // SPACE_BETWEEN or SPACE_AROUND
        float childTop;

        // Used only for if the direction is from bottom to top
        float childBottom;
        for (FlexLine flexLine : mFlexLines) {
            float spaceBetweenItem = 0f;
            switch (mJustifyContent) {
                case JUSTIFY_CONTENT_FLEX_START:
                    childTop = paddingTop;
                    childBottom = height - paddingBottom;
                    break;
                case JUSTIFY_CONTENT_FLEX_END:
                    childTop = height - flexLine.mainSize + paddingBottom;
                    childBottom = flexLine.mainSize - paddingTop;
                    break;
                case JUSTIFY_CONTENT_CENTER:
                    childTop = paddingTop + (height - flexLine.mainSize) / 2f;
                    childBottom = height - paddingBottom - (height - flexLine.mainSize) / 2f;
                    break;
                case JUSTIFY_CONTENT_SPACE_AROUND:
                    if (flexLine.itemCount != 0) {
                        spaceBetweenItem = (height - flexLine.mainSize)
                                / (float) flexLine.itemCount;
                    }
                    childTop = paddingTop + spaceBetweenItem / 2f;
                    childBottom = height - paddingBottom - spaceBetweenItem / 2f;
                    break;
                case JUSTIFY_CONTENT_SPACE_BETWEEN:
                    childTop = paddingTop;
                    float denominator = flexLine.itemCount != 1 ? flexLine.itemCount - 1 : 1f;
                    spaceBetweenItem = (height - flexLine.mainSize) / denominator;
                    childBottom = height - paddingBottom;
                    break;
                default:
                    throw new IllegalStateException(
                            "Invalid justifyContent is set: " + mJustifyContent);
            }
            spaceBetweenItem = Math.max(spaceBetweenItem, 0);

            for (int i = 0; i < flexLine.itemCount; i++) {
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
            }
            childLeft += flexLine.crossSize;
            childRight -= flexLine.crossSize;
        }
    }

    /**
     * Place a single View when the layout direction is vertical ({@link #mFlexDirection} is
     * either {@link #FLEX_DIRECTION_COLUMN} or {@link #FLEX_DIRECTION_COLUMN_REVERSE}).
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
     * @see LayoutParams#alignSelf
     */
    private void layoutSingleChildVertical(View view, FlexLine flexLine, boolean isRtl,
            int alignItems, int left, int top, int right, int bottom) {
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        if (lp.alignSelf != LayoutParams.ALIGN_SELF_AUTO) {
            // Expecting the values for alignItems and alignSelf match except for ALIGN_SELF_AUTO.
            // Assigning the alignSelf value as alignItems should work.
            alignItems = lp.alignSelf;
        }
        int crossSize = flexLine.crossSize;
        switch (alignItems) {
            case ALIGN_ITEMS_FLEX_START: // Intentional fall through
            case ALIGN_ITEMS_STRETCH: // Intentional fall through
            case ALIGN_ITEMS_BASELINE:
                if (!isRtl) {
                    view.layout(left + lp.leftMargin, top, right + lp.leftMargin, bottom);
                } else {
                    view.layout(left - lp.rightMargin, top, right - lp.rightMargin, bottom);
                }
                break;
            case ALIGN_ITEMS_FLEX_END:
                if (!isRtl) {
                    view.layout(left + crossSize - view.getMeasuredWidth() - lp.rightMargin,
                            top, right + crossSize - view.getMeasuredWidth() - lp.rightMargin,
                            bottom);
                } else {
                    // If the flexWrap == FLEX_WRAP_WRAP_REVERSE, the direction of the
                    // flexEnd is flipped (from left to right).
                    view.layout(left - crossSize + view.getMeasuredWidth() + lp.leftMargin, top,
                            right - crossSize + view.getMeasuredWidth() + lp.leftMargin,
                            bottom);
                }
                break;
            case ALIGN_ITEMS_CENTER:
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

    /**
     * Per child parameters for children views of the {@link FlexboxLayout}.
     */
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {

        private static final int ORDER_DEFAULT = 1;

        private static final float FLEX_GROW_DEFAULT = 0f;

        private static final float FLEX_SHRINK_DEFAULT = 1f;

        public static final float FLEX_BASIS_PERCENT_DEFAULT = -1f;

        public static final int ALIGN_SELF_AUTO = -1;

        public static final int ALIGN_SELF_FLEX_START = ALIGN_ITEMS_FLEX_START;

        public static final int ALIGN_SELF_FLEX_END = ALIGN_ITEMS_FLEX_END;

        public static final int ALIGN_SELF_CENTER = ALIGN_ITEMS_CENTER;

        public static final int ALIGN_SELF_BASELINE = ALIGN_ITEMS_BASELINE;

        public static final int ALIGN_SELF_STRETCH = ALIGN_ITEMS_STRETCH;

        /**
         * This attribute can change the ordering of the children views are laid out.
         * By default, children are displayed and laid out in the same order as they appear in the
         * layout XML. If not specified, {@link #ORDER_DEFAULT} is set as a default value.
         */
        public int order = ORDER_DEFAULT;

        /**
         * This attribute determines how much this child will grow if positive free space is
         * distributed relative to the rest of other flex items included in the same flex line.
         * If not specified, {@link #FLEX_GROW_DEFAULT} is set as a default value.
         */
        public float flexGrow = FLEX_GROW_DEFAULT;

        /**
         * This attributes determines how much this child will shrink is negative free space is
         * distributed relative to the rest of other flex items included in the same flex line.
         * If not specified, {@link #FLEX_SHRINK_DEFAULT} is set as a default value.
         */
        public float flexShrink = FLEX_SHRINK_DEFAULT;

        /**
         * This attributes determines the alignment along the cross axis (perpendicular to the
         * main axis). The alignment in the same direction can be determined by the
         * {@link #mAlignItems} in the parent, but if this is set to other than
         * {@link #ALIGN_SELF_AUTO}, the cross axis alignment is overridden for this child.
         * The value needs to be one of the values in ({@link #ALIGN_SELF_AUTO},
         * {@link #ALIGN_SELF_STRETCH}, {@link #ALIGN_SELF_FLEX_START}, {@link
         * #ALIGN_SELF_FLEX_END}, {@link #ALIGN_SELF_CENTER}, or {@link #ALIGN_SELF_BASELINE}).
         * If not specified, {@link #ALIGN_SELF_AUTO} is set as a default value.
         */
        public int alignSelf = ALIGN_SELF_AUTO;

        /**
         * The initial flex item length in a fraction format relative to its parent.
         * The initial main size of this child View is trying to be expanded as the specified
         * fraction against the parent main size.
         * If this value is set, the length specified from layout_width
         * (or layout_height) is overridden by the calculated value from this attribute.
         * This attribute is only effective when the parent's MeasureSpec mode is
         * MeasureSpec.EXACTLY. The default value is {@link #FLEX_BASIS_PERCENT_DEFAULT}, which
         * means not set.
         */
        public float flexBasisPercent = FLEX_BASIS_PERCENT_DEFAULT;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);

            TypedArray a = context
                    .obtainStyledAttributes(attrs, R.styleable.FlexboxLayout_Layout);
            order = a.getInt(R.styleable.FlexboxLayout_Layout_layout_order, ORDER_DEFAULT);
            flexGrow = a
                    .getFloat(R.styleable.FlexboxLayout_Layout_layout_flexGrow, FLEX_GROW_DEFAULT);
            flexShrink = a.getFloat(R.styleable.FlexboxLayout_Layout_layout_flexShrink,
                    FLEX_SHRINK_DEFAULT);
            alignSelf = a
                    .getInt(R.styleable.FlexboxLayout_Layout_layout_alignSelf, ALIGN_SELF_AUTO);
            flexBasisPercent = a
                    .getFraction(R.styleable.FlexboxLayout_Layout_layout_flexBasisPercent, 1, 1,
                            FLEX_BASIS_PERCENT_DEFAULT);
            a.recycle();
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(int width, int height) {
            super(new ViewGroup.LayoutParams(width, height));
        }
    }

    /**
     * A class that is used for calculating the view order which view's indices and order
     * properties from Flexbox are taken into account.
     */
    private static class Order implements Comparable<Order> {

        /** {@link View}'s index */
        int index;

        /** order property in the Flexbox */
        int order;

        @Override
        public int compareTo(@NonNull Order another) {
            if (order != another.order) {
                return order - another.order;
            }
            return index - another.index;
        }

        @Override
        public String toString() {
            return "Order{" +
                    "order=" + order +
                    ", index=" + index +
                    '}';
        }
    }

    /**
     * Holds properties related to a single Flex line.
     */
    private static class FlexLine {

        int mainSize;

        int crossSize;

        int itemCount;

        /** The sum of the flexGrow properties of the children included in this flex line */
        float totalFlexGrow;

        /** The sum of the flexShrink properties of the children included in this flex line */
        float totalFlexShrink;

        /**
         * The largest value of the individual child's baseline (obtained by View#getBaseline()
         * if the {@link #mAlignItems} value is not {@link #ALIGN_ITEMS_BASELINE} or the flex
         * direction is vertical, this value is not used.
         * If the alignment direction is from the bottom to top,
         * (e.g. flexWrap == FLEX_WRAP_WRAP_REVERSE and flexDirection == FLEX_DIRECTION_ROW)
         * store this value from the distance from the bottom of the view minus baseline.
         * (Calculated as view.getMeasuredHeight() - view.getBaseline - LayoutParams.bottomMargin)
         */
        int maxBaseline;

        /**
         * Store the indices of the children views whose alignSelf property is stretch.
         * The stored indices are the absolute indices including all children in the Flexbox,
         * not the relative indices in this flex line.
         */
        List<Integer> indicesAlignSelfStretch = new ArrayList<>();
    }
}
