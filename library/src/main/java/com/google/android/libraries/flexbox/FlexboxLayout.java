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
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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

    /** Holds reordered indices, which {@link LayoutParams#order} parameter is taken into account */
    private int[] mReorderedIndex;

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
        mReorderedIndex = createReorderedIndex();
        // TODO: Only calculate the children views which are affected from the last measure.

        switch (mFlexDirection) {
            case FLEX_DIRECTION_ROW:
                measureHorizontal(widthMeasureSpec, heightMeasureSpec);
                break;
            case FLEX_DIRECTION_ROW_REVERSE:
                // TODO: Implement this
                break;
            case FLEX_DIRECTION_COLUMN:
                // TODO: Implement this
                break;
            case FLEX_DIRECTION_COLUMN_REVERSE:
                // TODO: Implement this
                break;
            default:
                throw new IllegalStateException(
                        "Invalid value for the flex direction is set: " + mFlexDirection);
        }
    }

    private void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int childState = 0;

        mFlexLines.clear();

        // Determine how many flex lines are needed in this layout by measuring each child.
        // (Expand or shrink the view depending on the flexGrow and flexShrink attributes in a later
        // loop)
        {
            int childCount = getChildCount();
            int paddingStart = getPaddingStart();
            int paddingEnd = getPaddingEnd();
            int largestHeightInRow = Integer.MIN_VALUE;
            int heightUsed = getPaddingTop();
            FlexLine flexLine = new FlexLine();
            flexLine.mainSize = paddingStart;
            for (int i = 0; i < childCount; i++) {
                View child = getReorderedChildAt(i);
                if (child == null || child.getVisibility() == View.GONE) {
                    continue;
                }

                FlexboxLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.alignSelf == LayoutParams.ALIGN_SELF_STRETCH) {
                    flexLine.indicesAlignSelfStretch.add(i);
                }
                int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                        child.getPaddingLeft() + child.getPaddingRight() + lp.leftMargin
                                + lp.rightMargin, lp.width);
                int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                        child.getPaddingTop() + child.getPaddingBottom() + lp.topMargin
                                + lp.bottomMargin, lp.height);
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                largestHeightInRow = Math.max(largestHeightInRow,
                        child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);

                if (isWrapRequired(mFlexWrap, widthMode, widthSize, flexLine.mainSize,
                        child.getMeasuredWidth())) {
                    flexLine.mainSize += paddingEnd;
                    heightUsed += flexLine.crossSize;
                    mFlexLines.add(flexLine);

                    flexLine = new FlexLine();
                    flexLine.mainSize = paddingStart + child
                            .getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                    flexLine.itemCount = 1;
                    flexLine.totalFlexGrow += lp.flexGrow;
                    largestHeightInRow = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
                    // Temporarily set the cross axis length as the largest child in the row
                    // Expand along the cross axis depending on the mAlignContent property if needed
                    // later
                    flexLine.crossSize = largestHeightInRow;
                } else {
                    flexLine.itemCount++;
                    flexLine.mainSize += child.getMeasuredWidth() + lp.leftMargin
                            + lp.rightMargin;
                    flexLine.totalFlexGrow += lp.flexGrow;
                    flexLine.crossSize = Math
                            .max(flexLine.crossSize, largestHeightInRow);
                }

                if (i == childCount - 1 && flexLine.itemCount != 0) {
                    // Add the flex line if this item is the last item
                    flexLine.mainSize += paddingEnd;
                    heightUsed += flexLine.crossSize;
                    mFlexLines.add(flexLine);
                }
            }
        }

        // Main size determination (Calculate the length along the main axis)
        // Distribute the free remaining space to each flex item
        // TODO: Take flexShrink attributes into account
        {
            int mainSize;
            if (widthMode == MeasureSpec.EXACTLY) {
                mainSize = widthSize;
            } else {
                mainSize = getLargestMainSize();
            }

            int childIndex = 0;
            for (FlexLine flexLine : mFlexLines) {
                if (flexLine.totalFlexGrow <= 0 || mainSize < flexLine.mainSize) {
                    childIndex += flexLine.itemCount;
                    continue;
                }
                int unitSpace = (mainSize - flexLine.mainSize) / flexLine.totalFlexGrow;
                flexLine.mainSize = getPaddingStart() + getPaddingEnd();
                for (int i = 0; i < flexLine.itemCount; i++) {
                    View child = getReorderedChildAt(childIndex);
                    if (child == null || child.getVisibility() == View.GONE) {
                        childIndex++;
                        continue;
                    }
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    int newWidth = child.getMeasuredWidth() + unitSpace * lp.flexGrow;
                    child.measure(MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY),
                            MeasureSpec
                                    .makeMeasureSpec(child.getMeasuredHeight(),
                                            MeasureSpec.EXACTLY));
                    flexLine.mainSize += child.getMeasuredWidth() + lp.getMarginStart()
                            + lp.getMarginEnd();
                    childIndex++;
                }
            }
        }

        // Cross size determination (Calculate the length along the cross axis)
        // Expand the cross size only if the height mode is MeasureSpec.EXACTLY, otherwise
        // use the sum of cross sizes of all flex lines.
        // TODO: Consider the case mAlignContent == ALIGN_CONTENT_BASELINE
        if (heightMode == MeasureSpec.EXACTLY) {
            int totalCrossSize = getSumOfCrossSize();
            if (mFlexLines.size() == 1) {
                mFlexLines.get(0).crossSize = heightSize;
                // alignContent property is valid only if the Flexbox has at least two lines
            } else if (mFlexLines.size() >= 2 && totalCrossSize < heightSize) {
                switch (mAlignContent) {
                    case ALIGN_CONTENT_STRETCH: {
                        int freeSpaceUnit = (heightSize - totalCrossSize)
                                / mFlexLines.size();
                        for (FlexLine flexLine : mFlexLines) {
                            flexLine.crossSize += freeSpaceUnit;
                        }
                        break;
                    }
                    case ALIGN_CONTENT_SPACE_AROUND: {
                        // The value of free space along the cross axis which needs to be put on top
                        // and below the bottom of each flex line.
                        int spaceTopAndBottom = heightSize - totalCrossSize;
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
                        int spaceBetweenFlexLine = heightSize - totalCrossSize;
                        int numberOfSpaces = mFlexLines.size() - 1;
                        spaceBetweenFlexLine = spaceBetweenFlexLine / numberOfSpaces;
                        List<FlexLine> newFlexLines = new ArrayList<>();
                        FlexLine dummySpaceFlexLine = new FlexLine();
                        dummySpaceFlexLine.crossSize = spaceBetweenFlexLine;
                        for (int i = 0; i < mFlexLines.size(); i++) {
                            FlexLine flexLine = mFlexLines.get(i);
                            newFlexLines.add(flexLine);
                            if (i != mFlexLines.size() - 1) {
                                newFlexLines.add(dummySpaceFlexLine);
                            }
                        }
                        mFlexLines = newFlexLines;
                        break;
                    }
                    case ALIGN_CONTENT_CENTER: {
                        int spaceAboveAndBottom = heightSize - totalCrossSize;
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
                        int spaceTop = heightSize - totalCrossSize;
                        FlexLine dummySpaceFlexLine = new FlexLine();
                        dummySpaceFlexLine.crossSize = spaceTop;
                        mFlexLines.add(0, dummySpaceFlexLine);
                        break;
                    }
                }
            }
        }

        // Now cross size for each flex line is determined.
        // Expand the views if alignItems (or alignSelf in each child view) is stretch
        if (mAlignItems == ALIGN_ITEMS_STRETCH) {
            int viewIndex = 0;
            for (FlexLine flexLine : mFlexLines) {
                for (int i = 0; i < flexLine.itemCount; i++, viewIndex++) {
                    stretchViewVerticallyAt(viewIndex, flexLine.crossSize);
                }
            }
        } else {
            for (FlexLine flexLine : mFlexLines) {
                for (Integer index : flexLine.indicesAlignSelfStretch) {
                    stretchViewVerticallyAt(index, flexLine.crossSize);
                }
            }
        }

        // Set this FlexboxLayout's width and height depending on the calculated length of main axis
        // and cross axis.
        {
            int totalCrossSize = getSumOfCrossSize();
            int widthSizeAndState;
            int largestMainSize = getLargestMainSize();
            switch (widthMode) {
                case MeasureSpec.EXACTLY:
                    if (widthSize < largestMainSize) {
                        childState = combineMeasuredStates(childState, MEASURED_STATE_TOO_SMALL);
                    }
                    widthSizeAndState = resolveSizeAndState(widthSize, widthMeasureSpec,
                            childState);
                    break;
                case MeasureSpec.AT_MOST: {
                    if (widthSize < largestMainSize) {
                        childState = combineMeasuredStates(childState, MEASURED_STATE_TOO_SMALL);
                    } else {
                        widthSize = largestMainSize;
                    }
                    widthSizeAndState = resolveSizeAndState(widthSize, widthMeasureSpec,
                            childState);
                    break;
                }
                case MeasureSpec.UNSPECIFIED: {
                    widthSizeAndState = resolveSizeAndState(largestMainSize, widthMeasureSpec,
                            childState);
                    break;
                }
                default:
                    throw new IllegalStateException("Unknown width mode is set: " + widthMode);
            }
            int heightSizeAndState;
            switch (heightMode) {
                case MeasureSpec.EXACTLY:
                    if (heightSize < totalCrossSize) {
                        childState = combineMeasuredStates(childState,
                                MEASURED_STATE_TOO_SMALL >> MEASURED_HEIGHT_STATE_SHIFT);
                    }
                    heightSizeAndState = resolveSizeAndState(heightSize, heightMeasureSpec,
                            childState);
                    break;
                case MeasureSpec.AT_MOST: {
                    if (heightSize < totalCrossSize) {
                        childState = combineMeasuredStates(childState,
                                MEASURED_STATE_TOO_SMALL >> MEASURED_HEIGHT_STATE_SHIFT);
                    } else {
                        heightSize = totalCrossSize;
                    }
                    heightSizeAndState = resolveSizeAndState(heightSize, heightMeasureSpec,
                            childState);
                    break;
                }
                case MeasureSpec.UNSPECIFIED: {
                    heightSizeAndState = resolveSizeAndState(totalCrossSize,
                            heightMeasureSpec,
                            childState);
                    break;
                }
                default:
                    throw new IllegalStateException("Unknown height mode is set: " + heightMode);
            }
            setMeasuredDimension(widthSizeAndState, heightSizeAndState);
        }
    }

    /**
     * Expand the view vertically to the size of the crossSize (considering the view margins)
     * @param index the absolute index of the view to be expanded
     * @param crossSize the cross size
     */
    private void stretchViewVerticallyAt(int index, int crossSize) {
        View child = getReorderedChildAt(index);
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        int newHeight = crossSize - lp.topMargin - lp.bottomMargin;
        newHeight = Math.max(newHeight, 0);
        child.measure(MeasureSpec
                        .makeMeasureSpec(child.getMeasuredWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY));
    }

    /**
     * Determine if a wrap is required (add a new flex line).
     *
     * @param flexWrap the flexWrap attribute
     * @param mode the width or height mode along the main axis direction
     * @param maxSize the max size along the main axis direction
     * @param currentLength the accumulated current length
     * @param childLength the length of a child view which is to be collected to the flex line
     * @return {@code true} if a wrap is required, {@code false} otherwise
     */
    private boolean isWrapRequired(int flexWrap, int mode, int maxSize,
            int currentLength, int childLength) {
        return flexWrap != FLEX_WRAP_NOWRAP &&
                (mode == MeasureSpec.EXACTLY || mode == MeasureSpec.AT_MOST) &&
                maxSize < currentLength + childLength;
    }

    private int getLargestMainSize() {
        int largestSize = Integer.MIN_VALUE;
        for (FlexLine flexLine : mFlexLines) {
            largestSize = Math.max(largestSize, flexLine.mainSize);
        }
        return largestSize;
    }

    private int getSumOfCrossSize() {
        int sum = 0;
        for (FlexLine flexLine : mFlexLines) {
            sum += flexLine.crossSize;
        }
        return sum;
    }

    /**
     * Returns a View, which is reordered by taking {@link LayoutParams#order} parameters
     * into account.
     *
     * @param index the index of the view
     * @return the reordered view, which {@link LayoutParams@order} is taken into account.
     *         If the index is negative or out of bounds of the number of contained views,
     *         returns {@code null}.
     */
    public View getReorderedChildAt(int index) {
        if (index < 0 || index >= mReorderedIndex.length) {
            return null;
        }
        return getChildAt(mReorderedIndex[index]);
    }

    private int[] createReorderedIndex() {
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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        switch (mFlexDirection) {
            case FLEX_DIRECTION_ROW:
                layoutHorizontal(changed, left, top, right, bottom);
                break;
            case FLEX_DIRECTION_ROW_REVERSE:
                // TODO: Implement this
                break;
            case FLEX_DIRECTION_COLUMN:
                // TODO: Implement this
                break;
            case FLEX_DIRECTION_COLUMN_REVERSE:
                // TODO: Implement this
                break;
            default:
                throw new IllegalStateException("Invalid flex direction is set: " + mFlexDirection);
        }
    }

    private void layoutHorizontal(boolean changed, int left, int top, int right, int bottom) {
        int paddingStart = getPaddingStart();
        int paddingEnd = getPaddingEnd();
        int childStart;
        int currentViewIndex = 0;

        int height = bottom - top;
        // childBottom is used if the mFlexWrap is FLEX_WRAP_WRAP_REVERSE otherwise
        // childTop is used to align the vertical position of the children views.
        int childBottom = height - getPaddingBottom();
        int childTop = getPaddingTop();

        for (FlexLine flexLine : mFlexLines) {
            int spaceBetweenItem = 0;
            switch (mJustifyContent) {
                case JUSTIFY_CONTENT_FLEX_START:
                    childStart = paddingStart;
                    break;
                case JUSTIFY_CONTENT_FLEX_END:
                    childStart = right - paddingEnd - flexLine.mainSize;
                    break;
                case JUSTIFY_CONTENT_CENTER:
                    childStart = paddingStart +
                            +(right - left - flexLine.mainSize) / 2;
                    break;
                case JUSTIFY_CONTENT_SPACE_AROUND:
                    if (flexLine.itemCount != 0) {
                        spaceBetweenItem = (right - left - paddingStart - paddingEnd
                                - flexLine.mainSize) / flexLine.itemCount;
                    }
                    childStart = paddingStart + spaceBetweenItem / 2;
                    break;
                case JUSTIFY_CONTENT_SPACE_BETWEEN:
                    childStart = paddingStart;
                    int denominator = flexLine.itemCount != 1 ? flexLine.itemCount - 1 : 1;
                    spaceBetweenItem = (right - left - paddingStart - paddingEnd
                            - flexLine.mainSize) / denominator;
                    break;
                default:
                    throw new IllegalStateException(
                            "Invalid justifyContent is set: " + mJustifyContent);
            }
            spaceBetweenItem = Math.max(spaceBetweenItem, 0);

            for (int i = 0; i < flexLine.itemCount; i++) {
                View child = getReorderedChildAt(currentViewIndex);
                if (child == null || child.getVisibility() == View.GONE) {
                    currentViewIndex++;
                    continue;
                }
                LayoutParams lp = ((LayoutParams) child.getLayoutParams());
                childStart += lp.leftMargin;
                if (mFlexWrap == FLEX_WRAP_WRAP_REVERSE) {
                    layoutSingleChildHorizontal(child, mFlexWrap, mAlignItems,
                            flexLine.crossSize,
                            childStart, childBottom - child.getMeasuredHeight() - lp.bottomMargin,
                            childStart + child.getMeasuredWidth(),
                            childBottom);
                } else {
                    layoutSingleChildHorizontal(child, mFlexWrap, mAlignItems,
                            flexLine.crossSize,
                            childStart, childTop, childStart + child.getMeasuredWidth(),
                            childTop + child.getMeasuredHeight());
                }
                childStart += child.getMeasuredWidth() + spaceBetweenItem + lp.rightMargin;
                currentViewIndex++;
            }
            childTop += flexLine.crossSize;
            childBottom -= flexLine.crossSize;
        }
    }

    private void layoutSingleChildHorizontal(View view, int flexWrap, int alignItems,
            int crossSize, int left, int top, int right, int bottom) {
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        if (lp.alignSelf != LayoutParams.ALIGN_SELF_AUTO) {
            // Expecting the values for alignItems and alignSelf match except for ALIGN_SELF_AUTO.
            // Assigning the alignSelf value as alignItems should work.
            alignItems = lp.alignSelf;
        }
        switch (alignItems) {
            case ALIGN_ITEMS_FLEX_START:
            case ALIGN_ITEMS_STRETCH: // Intentional fall through
            case ALIGN_ITEMS_BASELINE: // TODO: Change the case for BASELINE correctly
                view.layout(left, top + lp.topMargin, right,
                        bottom + lp.topMargin);
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
                            right, bottom - crossSize + view.getMeasuredHeight());
                }
                break;
            case ALIGN_ITEMS_CENTER:
                int topFromCrossAxis = (crossSize - view.getMeasuredHeight()) / 2;
                if (flexWrap != FLEX_WRAP_WRAP_REVERSE) {
                    view.layout(left, top + topFromCrossAxis + lp.topMargin - lp.bottomMargin,
                            right,
                            top + topFromCrossAxis + view.getMeasuredHeight() + lp.topMargin
                                    - lp.bottomMargin);
                } else {
                    view.layout(left, top - topFromCrossAxis + lp.topMargin - lp.bottomMargin,
                            right,
                            top - topFromCrossAxis + view.getMeasuredHeight() + lp.topMargin
                                    - lp.bottomMargin);
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

        public LayoutParams(int width, int height) {
            super(new ViewGroup.LayoutParams(width, height));
        }
    }

    /**
     * A class that is used for calculating the view order which view's indices and order properties
     * from Flexbox are taken into account.
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
        int totalFlexGrow;

        /**
         * Store the indices of the children views whose alignSelf property is stretch.
         * The stored indices are the absolute indices including all children in the Flexbox,
         * not the relative indices in this flex line.
         */
        List<Integer> indicesAlignSelfStretch = new ArrayList<>();
    }
}
