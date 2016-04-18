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
            int paddingStart = getPaddingStart();
            int paddingEnd = getPaddingEnd();
            int largestHeightInRow = Integer.MIN_VALUE;
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
                    mFlexLines.add(flexLine);

                    flexLine = new FlexLine();
                    flexLine.itemCount = 1;
                    largestHeightInRow = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
                } else {
                    flexLine.itemCount++;
                }
                flexLine.mainSize += child.getMeasuredWidth() + lp.leftMargin
                        + lp.rightMargin;
                flexLine.totalFlexGrow += lp.flexGrow;
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
            largestWidthInColumn = Math.max(largestWidthInColumn,
                    child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);

            if (isWrapRequired(mFlexWrap, heightMode, heightSize, flexLine.mainSize,
                    child.getMeasuredHeight())) {
                flexLine.mainSize += paddingBottom;
                mFlexLines.add(flexLine);

                flexLine = new FlexLine();
                flexLine.itemCount = 1;
                largestWidthInColumn = child.getMeasuredWidth() + lp.leftMargin
                        + lp.rightMargin;
            } else {
                flexLine.itemCount++;
            }
            flexLine.mainSize += child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            flexLine.totalFlexGrow += lp.flexGrow;
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
     * Expand the view if the {@link #mAlignItems} attribute is set to {@link #ALIGN_ITEMS_STRETCH}
     * of {@link LayoutParams#ALIGN_SELF_STRETCH} is set to an individual child view.
     *
     * @param flexDirection the flex direction attribute
     * @param alignItems the align items attribute
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
     * Determines the cross size (Calculate the length along the cross axis).
     * Expand the cross size only if the height mode is MeasureSpec.EXACTLY, otherwise
     * use the sum of cross sizes of all flex lines.
     *
     * @param flexDirection the flex direction attribute
     * @param widthMeasureSpec the measure spec parameter for width
     * @param heightMeasureSpec the measure spec parameter for height
     */
    private void determineCrossSize(int flexDirection, int widthMeasureSpec, int heightMeasureSpec) {
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
                        int freeSpaceUnit = (size - totalCrossSize) / mFlexLines.size();
                        for (FlexLine flexLine : mFlexLines) {
                            flexLine.crossSize += freeSpaceUnit;
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
                        int spaceBetweenFlexLine = size - totalCrossSize;
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
     * Determine the main size by expanding an individual child in each flex line if any children's
     * flexGrow property is set to non-zero.
     * (Distribute the free remaining space to each flex line)
     *
     * @param flexDirection the value of the flex direction
     * @param widthMeasureSpec the width measure spec value
     * @param heightMeasureSpec the height measure spec value
     */
    private void determineMainSize(@FlexDirection int flexDirection, int widthMeasureSpec,
            int heightMeasureSpec) {
        // TODO: Take flexShrink attributes into account
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
                paddingAlongMainAxis = getPaddingStart() + getPaddingEnd();
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
            if (flexLine.totalFlexGrow <= 0 || mainSize < flexLine.mainSize) {
                childIndex += flexLine.itemCount;
                continue;
            }
            int unitSpace = (mainSize - flexLine.mainSize) / flexLine.totalFlexGrow;
            flexLine.mainSize = paddingAlongMainAxis;
            for (int i = 0; i < flexLine.itemCount; i++) {
                View child = getReorderedChildAt(childIndex);
                if (child == null || child.getVisibility() == View.GONE) {
                    childIndex++;
                    continue;
                }
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (flexDirection == FLEX_DIRECTION_ROW
                        || flexDirection == FLEX_DIRECTION_ROW_REVERSE) {
                    int newWidth = child.getMeasuredWidth() + unitSpace * lp.flexGrow;
                    child.measure(MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY),
                            MeasureSpec
                                    .makeMeasureSpec(child.getMeasuredHeight(),
                                            MeasureSpec.EXACTLY));
                    flexLine.mainSize += child.getMeasuredWidth() + lp.getMarginStart()
                            + lp.getMarginEnd();
                } else {
                    int newHeight = child.getMeasuredHeight() + unitSpace * lp.flexGrow;
                    child.measure(MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(),
                            MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY));
                    flexLine.mainSize += child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
                }
                childIndex++;
            }
        }
    }

    /**
     * Set this FlexboxLayouts' width and height depending on the calculated size of main axis and
     * cross axis.
     *
     * @param flexDirection the value of the flex direction
     * @param widthMeasureSpec the widthMeasureSpec of this FlexboxLayout
     * @param heightMeasureSpec the heightMeasureSpec of this FlexboxLayout
     * @param childState the child state of the View
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
                calculatedMaxHeight = getSumOfCrossSize();
                calculatedMaxWidth = getLargestMainSize();
                break;
            case FLEX_DIRECTION_COLUMN: // Intentional fall through
            case FLEX_DIRECTION_COLUMN_REVERSE:
                calculatedMaxHeight = getLargestMainSize();
                calculatedMaxWidth = getSumOfCrossSize();
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
     * Expand the view vertically to the size of the crossSize (considering the view margins)
     * @param view the View to be stretched
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
     * @param view the View to be stretched
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
                layoutVertical(changed, left, top, right, bottom);
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
                childStart += lp.getMarginStart();
                if (mFlexWrap == FLEX_WRAP_WRAP_REVERSE) {
                    layoutSingleChildHorizontal(child, flexLine, mFlexWrap, mAlignItems,
                            childStart, childBottom - child.getMeasuredHeight(),
                            childStart + child.getMeasuredWidth(),
                            childBottom);
                } else {
                    layoutSingleChildHorizontal(child, flexLine, mFlexWrap, mAlignItems,
                            childStart, childTop, childStart + child.getMeasuredWidth(),
                            childTop + child.getMeasuredHeight());
                }
                childStart += child.getMeasuredWidth() + spaceBetweenItem + lp.getMarginEnd();
                currentViewIndex++;
            }
            childTop += flexLine.crossSize;
            childBottom -= flexLine.crossSize;
        }
    }

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
                    view.layout(left, top + lp.topMargin, right,
                            bottom + lp.topMargin);
                } else {
                    view.layout(left, top - lp.bottomMargin, right,
                            bottom - lp.bottomMargin);
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

    private void layoutVertical(boolean changed, int left, int top, int right, int bottom) {
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int paddingEnd = getPaddingEnd();
        int childStart = getPaddingStart();
        int currentViewIndex = 0;

        int width = right - left;
        int height = bottom - top;
        // childEnd is used if the mFlexWrap is FLEX_WRAP_WRAP_REVERSE otherwise
        // childStart is used to align the horizontal position of the children views.
        int childEnd = width - paddingEnd;
        int childTop;

        for (FlexLine flexLine : mFlexLines) {
            int spaceBetweenItem = 0;
            switch (mJustifyContent) {
                case JUSTIFY_CONTENT_FLEX_START:
                    childTop = paddingTop;
                    break;
                case JUSTIFY_CONTENT_FLEX_END:
                    childTop = height - paddingBottom - flexLine.mainSize;
                    break;
                case JUSTIFY_CONTENT_CENTER:
                    childTop = paddingTop +
                            + (height - flexLine.mainSize) / 2;
                    break;
                case JUSTIFY_CONTENT_SPACE_AROUND:
                    if (flexLine.itemCount != 0) {
                        spaceBetweenItem = (height - paddingTop - paddingBottom
                                - flexLine.mainSize) / flexLine.itemCount;
                    }
                    childTop = paddingTop + spaceBetweenItem / 2;
                    break;
                case JUSTIFY_CONTENT_SPACE_BETWEEN:
                    childTop = paddingTop;
                    int denominator = flexLine.itemCount != 1 ? flexLine.itemCount - 1 : 1;
                    spaceBetweenItem = (height - paddingTop - paddingBottom
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
                childTop += lp.topMargin;
                if (mFlexWrap == FLEX_WRAP_WRAP_REVERSE) {
                    layoutSingleChildVertical(child, flexLine, mFlexWrap, mAlignItems,
                            childEnd - child.getMeasuredWidth(), childTop,
                            childEnd, childTop + child.getMeasuredHeight());
                } else {
                    layoutSingleChildVertical(child, flexLine, mFlexWrap, mAlignItems,
                            childStart, childTop, childStart + child.getMeasuredWidth(),
                            childTop + child.getMeasuredHeight());
                }
                childTop += child.getMeasuredHeight() + spaceBetweenItem + lp.bottomMargin;
                currentViewIndex++;
            }
            childStart += flexLine.crossSize;
            childEnd -= flexLine.crossSize;
        }
    }

    private void layoutSingleChildVertical(View view, FlexLine flexLine, @FlexWrap int flexWrap,
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
                if (flexWrap != FLEX_WRAP_WRAP_REVERSE) {
                    view.layout(left + lp.leftMargin, top, right + lp.leftMargin, bottom);
                } else {
                    view.layout(left - lp.rightMargin, top, right - lp.rightMargin, bottom);
                }
                break;
            case ALIGN_ITEMS_FLEX_END:
                if (flexWrap != FLEX_WRAP_WRAP_REVERSE) {
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
                if (flexWrap != FLEX_WRAP_WRAP_REVERSE) {
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
