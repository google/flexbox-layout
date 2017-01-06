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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v4.view.ViewCompat;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static com.google.android.flexbox.FlexItem.FLEX_BASIS_PERCENT_DEFAULT;

/**
 * Offers various calculations for Flexbox to use the common logic between the classes such as
 * {@link FlexboxLayout} and {@link FlexboxLayoutManager}.
 */
class FlexboxHelper {

    private static final int INITIAL_CAPACITY = 10;

    private static final long MEASURE_SPEC_WIDTH_MASK = 0xffffffffL;

    private final FlexContainer mFlexContainer;

    /**
     * Holds the 'frozen' state of children during measure. If a view is frozen it will no longer
     * expand or shrink regardless of flex grow/flex shrink attributes.
     */
    private boolean[] mChildrenFrozen;

    /**
     * Map the view index to the flex line which contains the view represented by the index to
     * look for a flex line from a given view index in a constant time.
     * Key: index of the view
     * Value: index of the flex line that contains the given view
     *
     * E.g. if we have following flex lines,
     * <p>
     * FlexLine(0): itemCount 3
     * FlexLine(1): itemCount 2
     * </p>
     * this instance should have following entries
     * <p>
     * [0, 0, 0, 1, 1, ...]
     * </p>
     */
    @Nullable
    int[] mIndexToFlexLine;

    /**
     * Cache the measured spec. The first 32 bit represents the height measure spec, the last
     * 32 bit represents the width measure spec of each flex item.
     * E.g. an entry is created like {@code (long) heightMeasureSpec << 32 | widthMeasureSpec}
     *
     * To retrieve a widthMeasureSpec, call {@link #extractLowerInt(long)} or
     * {@link #extractHigherInt(long)} for a heightMeasureSpec.
     */
    @Nullable
    long[] mMeasureSpecCache;

    /**
     * Cache a flex item's measured width and height. The first 32 bit represents the height, the
     * last 32 bit represents the width of each flex item.
     * E.g. an entry is created like the following code.
     * {@code (long) view.getMeasuredHeight() << 32 | view.getMeasuredWidth()}
     *
     * To retrieve a width value, call {@link #extractLowerInt(long)} or
     * {@link #extractHigherInt(long)} for a height value.
     */
    @Nullable
    private long[] mMeasuredSizeCache;

    FlexboxHelper(FlexContainer flexContainer) {
        mFlexContainer = flexContainer;
    }

    /**
     * Create an array, which indicates the reordered indices that
     * {@link FlexItem#getOrder()} attributes are taken into account.
     * This method takes a View before that is added as the parent ViewGroup's children.
     *
     * @param viewBeforeAdded          the View instance before added to the array of children
     *                                 Views of the parent ViewGroup
     * @param indexForViewBeforeAdded  the index for the View before added to the array of the
     *                                 parent ViewGroup
     * @param paramsForViewBeforeAdded the layout parameters for the View before added to the array
     *                                 of the parent ViewGroup
     * @return an array which have the reordered indices
     */
    int[] createReorderedIndices(View viewBeforeAdded, int indexForViewBeforeAdded,
            ViewGroup.LayoutParams paramsForViewBeforeAdded, SparseIntArray orderCache) {
        int childCount = mFlexContainer.getFlexItemCount();
        List<Order> orders = createOrders(childCount);
        Order orderForViewToBeAdded = new Order();
        if (viewBeforeAdded != null
                && paramsForViewBeforeAdded instanceof FlexItem) {
            orderForViewToBeAdded.order = ((FlexItem)
                    paramsForViewBeforeAdded).getOrder();
        } else {
            orderForViewToBeAdded.order = FlexItem.ORDER_DEFAULT;
        }

        if (indexForViewBeforeAdded == -1 || indexForViewBeforeAdded == childCount) {
            orderForViewToBeAdded.index = childCount;
        } else if (indexForViewBeforeAdded < mFlexContainer.getFlexItemCount()) {
            orderForViewToBeAdded.index = indexForViewBeforeAdded;
            for (int i = indexForViewBeforeAdded; i < childCount; i++) {
                orders.get(i).index++;
            }
        } else {
            // This path is not expected since OutOfBoundException will be thrown in the ViewGroup
            // But setting the index for fail-safe
            orderForViewToBeAdded.index = childCount;
        }
        orders.add(orderForViewToBeAdded);

        return sortOrdersIntoReorderedIndices(childCount + 1, orders, orderCache);
    }

    /**
     * Create an array, which indicates the reordered indices that
     * {@link FlexItem#getOrder()} attributes are taken into account.
     *
     * @return @return an array which have the reordered indices
     */
    int[] createReorderedIndices(SparseIntArray orderCache) {
        int childCount = mFlexContainer.getFlexItemCount();
        List<Order> orders = createOrders(childCount);
        return sortOrdersIntoReorderedIndices(childCount, orders, orderCache);
    }

    @NonNull
    private List<Order> createOrders(int childCount) {
        List<Order> orders = new ArrayList<>(childCount);
        for (int i = 0; i < childCount; i++) {
            View child = mFlexContainer.getFlexItemAt(i);
            FlexItem flexItem = (FlexItem) child.getLayoutParams();
            Order order = new Order();
            order.order = flexItem.getOrder();
            order.index = i;
            orders.add(order);
        }
        return orders;
    }

    /**
     * Returns if any of the children's {@link FlexItem#getOrder()} attributes are
     * changed from the last measurement.
     *
     * @return {@code true} if changed from the last measurement, {@code false} otherwise.
     */
    boolean isOrderChangedFromLastMeasurement(SparseIntArray orderCache) {
        int childCount = mFlexContainer.getFlexItemCount();
        if (orderCache.size() != childCount) {
            return true;
        }
        for (int i = 0; i < childCount; i++) {
            View view = mFlexContainer.getFlexItemAt(i);
            if (view == null) {
                continue;
            }
            FlexItem flexItem = (FlexItem) view.getLayoutParams();
            if (flexItem.getOrder() != orderCache.get(i)) {
                return true;
            }
        }
        return false;
    }

    private int[] sortOrdersIntoReorderedIndices(int childCount, List<Order> orders,
            SparseIntArray orderCache) {
        Collections.sort(orders);
        orderCache.clear();
        int[] reorderedIndices = new int[childCount];
        int i = 0;
        for (Order order : orders) {
            reorderedIndices[i] = order.index;
            orderCache.append(i, order.order);
            i++;
        }
        return reorderedIndices;
    }

    /**
     * Calculate how many flex lines are needed in the flex container.
     * This method should calculate all the flex lines from the existing flex items.
     *
     * @see #calculateHorizontalFlexLines(int, int, int, int, List)
     */
    FlexLinesResult calculateHorizontalFlexLines(int widthMeasureSpec, int heightMeasureSpec) {
        return calculateHorizontalFlexLines(widthMeasureSpec, heightMeasureSpec, Integer.MAX_VALUE,
                0, null);
    }

    /**
     * Calculate how many flex lines are needed in the flex container.
     * Stop calculating it if the calculated amount along the cross size reaches the argument
     * as the needsCalcAmount.
     *
     * @param widthMeasureSpec  the width measure spec imposed by the flex container
     * @param heightMeasureSpec the height measure spec imposed by the flex container
     * @param needsCalcAmount   the amount of pixels where flex line calculation should be stopped
     *                          this is needed to avoid the expensive calculation if the
     *                          calculation is needed only the small part of the entire flex
     *                          container. (E.g. If the flex container is the
     *                          {@link FlexboxLayoutManager}, the calculation only needs the
     *                          visible area, imposing the entire calculation may cause bad
     *                          performance
     * @see #calculateHorizontalFlexLines(int, int, int, int, List)
     */
    FlexLinesResult calculateHorizontalFlexLines(int widthMeasureSpec, int heightMeasureSpec,
            int needsCalcAmount) {
        return calculateHorizontalFlexLines(widthMeasureSpec, heightMeasureSpec, needsCalcAmount,
                0, null);
    }

    /**
     * Calculate how many flex lines are needed in the flex container layout by measuring each
     * child when the direction of the flex line is horizontal (left to right or right to left).
     * Expand or shrink the flex items depending on the flex grow and flex shrink
     * attributes in a later procedure, so views measured width may be changed in a later process
     * or calculating the flex container.
     *
     * @param widthMeasureSpec  the width measure spec imposed by the flex container
     * @param heightMeasureSpec the height measure spec imposed by the flex container
     * @param needsCalcAmount   the amount of pixels where flex line calculation should be stopped
     *                          this is needed to avoid the expensive calculation if the
     *                          calculation is needed only the small part of the entire flex
     *                          container. (E.g. If the flex container is the
     *                          {@link FlexboxLayoutManager}, the calculation only needs the
     *                          visible area, imposing the entire calculation may cause bad
     *                          performance
     * @param fromIndex         the index from which the calculation starts
     * @param existingLines     If not null, calculated flex lines will be added to this instance
     * @return a instance of {@link FlexLinesResult} that contains a list of flex lines and the
     * child state used by {@link View#setMeasuredDimension(int, int)}.
     */
    FlexLinesResult calculateHorizontalFlexLines(int widthMeasureSpec,
            int heightMeasureSpec, int needsCalcAmount, int fromIndex,
            @Nullable List<FlexLine> existingLines) {
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        FlexLinesResult result = new FlexLinesResult();
        List<FlexLine> flexLines;
        FlexLine flexLine = new FlexLine();
        flexLine.mFirstIndex = fromIndex;
        if (existingLines == null) {
            flexLines = new ArrayList<>();
        } else {
            flexLines = existingLines;
        }

        result.mFlexLines = flexLines;
        int childCount = mFlexContainer.getFlexItemCount();
        int childState = 0;
        // These padding values are treated as agnostic of the RTL or LTR, using the left and
        // right padding values doesn't cause a problem
        int paddingLeft = mFlexContainer.getPaddingLeft();
        int paddingRight = mFlexContainer.getPaddingRight();
        int largestHeightInRow = Integer.MIN_VALUE;

        // The amount of cross size calculated in this method call
        int sumCrossSize = 0;
        // The index of the view in a same flex line.
        int indexInFlexLine = 0;
        flexLine.mMainSize = paddingLeft + paddingRight;
        for (int i = fromIndex; i < childCount; i++) {
            View child = mFlexContainer.getReorderedFlexItemAt(i);
            if (child == null) {
                if (isLastFlexItem(i, childCount, flexLine)) {
                    addFlexLine(flexLines, flexLine, i);
                }
                continue;
            } else if (child.getVisibility() == View.GONE) {
                flexLine.mGoneItemCount++;
                flexLine.mItemCount++;
                if (isLastFlexItem(i, childCount, flexLine)) {
                    addFlexLine(flexLines, flexLine, i);
                }
                continue;
            }

            FlexItem flexItem = (FlexItem) child.getLayoutParams();
            if (flexItem.getAlignSelf() == AlignItems.STRETCH) {
                flexLine.mIndicesAlignSelfStretch.add(i);
            }
            int childWidth = flexItem.getWidth();
            if (flexItem.getFlexBasisPercent() != FLEX_BASIS_PERCENT_DEFAULT
                    && widthMode == View.MeasureSpec.EXACTLY) {
                childWidth = Math.round(widthSize * flexItem.getFlexBasisPercent());
                // Use the dimension from the layout_width attribute if the widthMode is not
                // MeasureSpec.EXACTLY even if any fraction value is set to
                // layout_flexBasisPercent.
                // There are likely quite few use cases where assigning any fraction values
                // with widthMode is not MeasureSpec.EXACTLY (e.g. FlexboxLayout's layout_width
                // is set to wrap_content)
            }
            int childWidthMeasureSpec = mFlexContainer
                    .getChildWidthMeasureSpec(widthMeasureSpec,
                            paddingLeft + paddingRight + flexItem.getMarginLeft()
                                    + flexItem.getMarginRight(), childWidth);

            int childHeightMeasureSpec = mFlexContainer
                    .getChildHeightMeasureSpec(heightMeasureSpec,
                            mFlexContainer.getPaddingTop() + mFlexContainer.getPaddingBottom()
                                    + flexItem.getMarginTop()
                                    + flexItem.getMarginBottom(), flexItem.getHeight());
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            updateMeasureCache(i, childWidthMeasureSpec, childHeightMeasureSpec, child);

            // Check the size constraint after the first measurement for the child
            // To prevent the child's width/height violate the size constraints imposed by the
            // {@link FlexItem#getMinWidth()}, {@link FlexItem#getMinHeight()},
            // {@link FlexItem#getMaxWidth()} and {@link FlexItem#getMaxHeight()} attributes.
            // E.g. When the child's layout_width is wrap_content the measured width may be
            // less than the min width after the first measurement.
            checkSizeConstraints(child, i);

            childState = ViewCompat
                    .combineMeasuredStates(childState, ViewCompat.getMeasuredState(child));
            largestHeightInRow = Math.max(largestHeightInRow,
                    child.getMeasuredHeight() + flexItem.getMarginTop() + flexItem
                            .getMarginBottom());

            if (isWrapRequired(widthMode, widthSize, flexLine.mMainSize,
                    child.getMeasuredWidth() + flexItem.getMarginLeft() + flexItem.getMarginRight(),
                    flexItem, i, indexInFlexLine)) {
                if (flexLine.getItemCountNotGone() > 0) {
                    addFlexLine(flexLines, flexLine,  i > 0 ? i - 1 : 0);
                    sumCrossSize += flexLine.mCrossSize;
                }

                flexLine = new FlexLine();
                flexLine.mItemCount = 1;
                flexLine.mMainSize = paddingLeft + paddingRight;
                flexLine.mFirstIndex = i;
                largestHeightInRow = child.getMeasuredHeight() + flexItem.getMarginTop()
                        + flexItem.getMarginBottom();
                indexInFlexLine = 0;
            } else {
                flexLine.mItemCount++;
                indexInFlexLine++;
            }
            if (mIndexToFlexLine != null) {
                mIndexToFlexLine[i] = flexLines.size();
            }
            flexLine.mMainSize += child.getMeasuredWidth() + flexItem.getMarginLeft()
                    + flexItem.getMarginRight();
            flexLine.mTotalFlexGrow += flexItem.getFlexGrow();
            flexLine.mTotalFlexShrink += flexItem.getFlexShrink();
            // Temporarily set the cross axis length as the largest child in the row
            // Expand along the cross axis depending on the mAlignContent property if needed
            // later
            flexLine.mCrossSize = Math.max(flexLine.mCrossSize, largestHeightInRow);

            mFlexContainer.onNewFlexItemAdded(i, indexInFlexLine, flexLine);
            if (mFlexContainer.getFlexWrap() != FlexWrap.WRAP_REVERSE) {
                flexLine.mMaxBaseline = Math
                        .max(flexLine.mMaxBaseline, child.getBaseline() + flexItem.getMarginTop());
            } else {
                // if the flex wrap property is WRAP_REVERSE, calculate the
                // baseline as the distance from the cross end and the baseline
                // since the cross size calculation is based on the distance from the cross end
                flexLine.mMaxBaseline = Math
                        .max(flexLine.mMaxBaseline,
                                child.getMeasuredHeight() - child.getBaseline()
                                        + flexItem.getMarginBottom());
            }
            if (isLastFlexItem(i, childCount, flexLine)) {
                addFlexLine(flexLines, flexLine, i);
                sumCrossSize += flexLine.mCrossSize;
            }

            if (sumCrossSize > needsCalcAmount) {
                // Stop the calculation if the sum of cross size calculated reached to the point
                // beyond the needsCalcAmount value to avoid unneeded calculation in a
                // RecyclerView.
                // To be precise, the decoration length may be added to the sumCrossSize,
                // but we omit adding the decoration length because even without the decorator
                // length, it's guaranteed that calculation is done at least beyond the
                // needsCalcAmount
                break;
            }
        }
        result.mChildState = childState;
        return result;
    }

    /**
     * Calculate how many flex lines are needed in the flex container.
     * This method should calculate all the flex lines from the existing flex items.
     *
     * @param widthMeasureSpec  the width measure spec imposed by the flex container
     * @param heightMeasureSpec the height measure spec imposed by the flex container
     * @see #calculateVerticalFlexLines(int, int, int, int, List)
     */
    FlexLinesResult calculateVerticalFlexLines(int widthMeasureSpec, int heightMeasureSpec) {
        return calculateVerticalFlexLines(widthMeasureSpec, heightMeasureSpec, Integer.MAX_VALUE,
                0, null);
    }

    /**
     * Calculate how many flex lines are needed in the flex container.
     * Stop calculating it if the calculated amount along the cross size reaches the argument
     * as the needsCalcAmount.
     *
     * @param widthMeasureSpec  the width measure spec imposed by the flex container
     * @param heightMeasureSpec the height measure spec imposed by the flex container
     * @param needsCalcAmount   the amount of pixels where flex line calculation should be stopped
     *                          this is needed to avoid the expensive calculation if the
     *                          calculation is needed only the small part of the entire flex
     *                          container. (E.g. If the flex container is the
     *                          {@link FlexboxLayoutManager}, the calculation only needs the
     *                          visible area, imposing the entire calculation may cause bad
     *                          performance
     * @see #calculateVerticalFlexLines(int, int, int, int, List)
     */
    FlexLinesResult calculateVerticalFlexLines(int widthMeasureSpec, int heightMeasureSpec,
            int needsCalcAmount) {
        return calculateVerticalFlexLines(widthMeasureSpec, heightMeasureSpec, needsCalcAmount,
                0, null);
    }

    /**
     * Calculate how many flex lines are needed in the flex container layout by measuring each
     * child when the direction of the flex line is vertical (top to bottom or bottom to top).
     * Expand or shrink the flex items depending on the flex grow and flex shrink
     * attributes in a later procedure, so views measured width may be changed in a later process
     * or calculating the flex container.
     *
     * @param widthMeasureSpec  the width measure spec imposed by the flex container
     * @param heightMeasureSpec the height measure spec imposed by the flex container
     * @param needsCalcAmount   the amount of pixels where flex line calculation should be stopped
     *                          this is needed to avoid the expensive calculation if the
     *                          calculation is needed only the small part of the entire flex
     *                          container. (E.g. If the flex container is the
     *                          {@link FlexboxLayoutManager}, the calculation only needs the
     *                          visible area, imposing the entire calculation may cause bad
     *                          performance
     * @param existingLines     If not null, calculated flex lines will be added to this instance
     * @return a instance of {@link FlexLinesResult} that contains a list of flex lines and the
     * child state used by {@link View#setMeasuredDimension(int, int)}.
     */
    FlexLinesResult calculateVerticalFlexLines(int widthMeasureSpec, int heightMeasureSpec,
            int needsCalcAmount, int fromIndex, @Nullable List<FlexLine> existingLines) {
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        FlexLinesResult result = new FlexLinesResult();
        List<FlexLine> flexLines;
        FlexLine flexLine = new FlexLine();
        flexLine.mFirstIndex = fromIndex;
        if (existingLines == null) {
            flexLines = new ArrayList<>();
        } else {
            flexLines = existingLines;
        }

        result.mFlexLines = flexLines;
        int childCount = mFlexContainer.getFlexItemCount();
        int childState = 0;

        int paddingTop = mFlexContainer.getPaddingTop();
        int paddingBottom = mFlexContainer.getPaddingBottom();
        int largestWidthInColumn = Integer.MIN_VALUE;
        flexLine.mMainSize = paddingTop + paddingBottom;

        // The amount of cross size calculated in this method call
        int sumCrossSize = 0;
        // The index of the view in a same flex line.
        int indexInFlexLine = 0;
        for (int i = fromIndex; i < childCount; i++) {
            View child = mFlexContainer.getReorderedFlexItemAt(i);
            if (child == null) {
                if (isLastFlexItem(i, childCount, flexLine)) {
                    addFlexLine(flexLines, flexLine, i);
                }
                continue;
            } else if (child.getVisibility() == View.GONE) {
                flexLine.mGoneItemCount++;
                flexLine.mItemCount++;
                if (isLastFlexItem(i, childCount, flexLine)) {
                    addFlexLine(flexLines, flexLine, i);
                }
                continue;
            }

            FlexItem flexItem = (FlexItem) child.getLayoutParams();
            if (flexItem.getAlignSelf() == AlignItems.STRETCH) {
                flexLine.mIndicesAlignSelfStretch.add(i);
            }

            int childHeight = flexItem.getHeight();
            if (flexItem.getFlexBasisPercent()
                    != FlexItem.FLEX_BASIS_PERCENT_DEFAULT
                    && heightMode == View.MeasureSpec.EXACTLY) {
                childHeight = Math.round(heightSize * flexItem.getFlexBasisPercent());
                // Use the dimension from the layout_height attribute if the heightMode is not
                // MeasureSpec.EXACTLY even if any fraction value is set to layout_flexBasisPercent.
                // There are likely quite few use cases where assigning any fraction values
                // with heightMode is not MeasureSpec.EXACTLY (e.g. FlexboxLayout's layout_height
                // is set to wrap_content)
            }

            int childWidthMeasureSpec = mFlexContainer
                    .getChildWidthMeasureSpec(widthMeasureSpec,
                            mFlexContainer.getPaddingLeft() + mFlexContainer.getPaddingRight()
                                    + flexItem.getMarginLeft() + flexItem.getMarginRight(),
                            flexItem.getWidth());
            int childHeightMeasureSpec = mFlexContainer
                    .getChildHeightMeasureSpec(heightMeasureSpec,
                            mFlexContainer.getPaddingTop() + mFlexContainer.getPaddingBottom()
                                    + flexItem.getMarginTop() + flexItem.getMarginBottom(),
                            childHeight);
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            updateMeasureCache(i, childWidthMeasureSpec, childHeightMeasureSpec, child);

            // Check the size constraint after the first measurement for the child
            // To prevent the child's width/height violate the size constraints imposed by the
            // {@link FlexItem#getMinWidth()}, {@link FlexItem#getMinHeight()},
            // {@link FlexItem#getMaxWidth()} and {@link FlexItem#getMaxHeight()} attributes.
            // E.g. When the child's layout_height is wrap_content the measured height may be
            // less than the min height after the first measurement.
            checkSizeConstraints(child, i);

            childState = ViewCompat
                    .combineMeasuredStates(childState, ViewCompat.getMeasuredState(child));
            largestWidthInColumn = Math.max(largestWidthInColumn,
                    child.getMeasuredWidth() + flexItem.getMarginLeft() + flexItem
                            .getMarginRight());

            if (isWrapRequired(heightMode, heightSize, flexLine.mMainSize,
                    child.getMeasuredHeight() + flexItem.getMarginTop() + flexItem
                            .getMarginBottom(),
                    flexItem,
                    i, indexInFlexLine)) {
                if (flexLine.getItemCountNotGone() > 0) {
                    addFlexLine(flexLines, flexLine, i > 0 ? i - 1 : 0);
                    sumCrossSize += flexLine.mCrossSize;
                }

                flexLine = new FlexLine();
                flexLine.mItemCount = 1;
                flexLine.mMainSize = paddingTop + paddingBottom;
                flexLine.mFirstIndex = i;
                largestWidthInColumn = child.getMeasuredWidth() + flexItem.getMarginLeft()
                        + flexItem.getMarginRight();
                indexInFlexLine = 0;
            } else {
                flexLine.mItemCount++;
                indexInFlexLine++;
            }
            if (mIndexToFlexLine != null) {
                mIndexToFlexLine[i] = flexLines.size();
            }

            flexLine.mMainSize += child.getMeasuredHeight() + flexItem.getMarginTop()
                    + flexItem.getMarginBottom();
            flexLine.mTotalFlexGrow += flexItem.getFlexGrow();
            flexLine.mTotalFlexShrink += flexItem.getFlexShrink();
            // Temporarily set the cross axis length as the largest child width in the column
            // Expand along the cross axis depending on the mAlignContent property if needed
            // later
            flexLine.mCrossSize = Math.max(flexLine.mCrossSize, largestWidthInColumn);

            mFlexContainer.onNewFlexItemAdded(i, indexInFlexLine, flexLine);
            if (isLastFlexItem(i, childCount, flexLine)) {
                addFlexLine(flexLines, flexLine, i);
                sumCrossSize += flexLine.mCrossSize;
            }

            if (sumCrossSize > needsCalcAmount) {
                // Stop the calculation if the sum of cross size calculated reached to the point
                // beyond the needsCalcAmount value to avoid unneeded calculation in a
                // RecyclerView.
                // To be precise, the decoration length may be added to the sumCrossSize,
                // but we omit adding the decoration length because even without the decorator
                // length, it's guaranteed that calculation is done at least beyond the
                // needsCalcAmount
                break;
            }
        }
        result.mChildState = childState;
        return result;
    }

    /**
     * Determine if a wrap is required (add a new flex line).
     *
     * @param mode          the width or height mode along the main axis direction
     * @param maxSize       the max size along the main axis direction
     * @param currentLength the accumulated current length
     * @param childLength   the length of a child view which is to be collected to the flex line
     * @param flexItem      the LayoutParams for the view being determined whether a new flex line
     *                      is needed
     * @return {@code true} if a wrap is required, {@code false} otherwise
     * @see FlexContainer#getFlexWrap()
     * @see FlexContainer#setFlexWrap(int)
     */
    private boolean isWrapRequired(int mode, int maxSize, int currentLength, int childLength,
            FlexItem flexItem, int childAbsoluteIndex, int childRelativeIndexInFlexLine) {
        if (mFlexContainer.getFlexWrap() == FlexWrap.NOWRAP) {
            return false;
        }
        if (flexItem.isWrapBefore()) {
            return true;
        }
        if (mode == View.MeasureSpec.UNSPECIFIED) {
            return false;
        }
        int decorationLength = mFlexContainer
                .getDecorationLength(childAbsoluteIndex, childRelativeIndexInFlexLine, flexItem);
        if (decorationLength > 0) {
            childLength += decorationLength;
        }
        return maxSize < currentLength + childLength;
    }

    private boolean isLastFlexItem(int childIndex, int childCount,
            FlexLine flexLine) {
        return childIndex == childCount - 1 && flexLine.getItemCountNotGone() != 0;
    }

    private void addFlexLine(List<FlexLine> flexLines, FlexLine flexLine, int viewIndex) {
        mFlexContainer.onNewFlexLineAdded(flexLine);
        flexLine.mLastIndex = viewIndex;
        flexLines.add(flexLine);
    }

    /**
     * Checks if the view's width/height don't violate the minimum/maximum size constraints imposed
     * by the {@link FlexItem#getMinWidth()}, {@link FlexItem#getMinHeight()},
     * {@link FlexItem#getMaxWidth()} and {@link FlexItem#getMaxHeight()} attributes.
     *
     * @param view  the view to be checked
     * @param index index of the view
     */
    private void checkSizeConstraints(View view, int index) {
        boolean needsMeasure = false;
        FlexItem flexItem = (FlexItem) view.getLayoutParams();
        int childWidth = view.getMeasuredWidth();
        int childHeight = view.getMeasuredHeight();

        if (view.getMeasuredWidth() < flexItem.getMinWidth()) {
            needsMeasure = true;
            childWidth = flexItem.getMinWidth();
        } else if (view.getMeasuredWidth() > flexItem.getMaxWidth()) {
            needsMeasure = true;
            childWidth = flexItem.getMaxWidth();
        }

        if (childHeight < flexItem.getMinHeight()) {
            needsMeasure = true;
            childHeight = flexItem.getMinHeight();
        } else if (childHeight > flexItem.getMaxHeight()) {
            needsMeasure = true;
            childHeight = flexItem.getMaxHeight();
        }
        if (needsMeasure) {
            int widthSpec = View.MeasureSpec.makeMeasureSpec(childWidth, View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec
                    .makeMeasureSpec(childHeight, View.MeasureSpec.EXACTLY);
            view.measure(widthSpec, heightSpec);
            updateMeasureCache(index, widthSpec, heightSpec, view);
        }
    }

    /**
     * @see #determineMainSize(int, int, int)
     */
    void determineMainSize(int widthMeasureSpec, int heightMeasureSpec) {
        determineMainSize(widthMeasureSpec, heightMeasureSpec, 0);
    }

    /**
     * Determine the main size by expanding (shrinking if negative remaining free space is given)
     * an individual child in each flex line if any children's mFlexGrow (or mFlexShrink if
     * remaining
     * space is negative) properties are set to non-zero.
     *
     * @param widthMeasureSpec  horizontal space requirements as imposed by the parent
     * @param heightMeasureSpec vertical space requirements as imposed by the parent
     * @see FlexContainer#setFlexDirection(int)
     * @see FlexContainer#getFlexDirection()
     */
    void determineMainSize(int widthMeasureSpec, int heightMeasureSpec, int fromIndex) {
        ensureChildrenFrozen(mFlexContainer.getFlexItemCount());
        if (fromIndex >= mFlexContainer.getFlexItemCount()) {
            return;
        }
        int mainSize;
        int paddingAlongMainAxis;
        int flexDirection = mFlexContainer.getFlexDirection();
        switch (mFlexContainer.getFlexDirection()) {
            case FlexDirection.ROW: // Intentional fall through
            case FlexDirection.ROW_REVERSE:
                int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
                int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
                if (widthMode == View.MeasureSpec.EXACTLY) {
                    mainSize = widthSize;
                } else {
                    mainSize = mFlexContainer.getLargestMainSize();
                }
                paddingAlongMainAxis = mFlexContainer.getPaddingLeft()
                        + mFlexContainer.getPaddingRight();
                break;
            case FlexDirection.COLUMN: // Intentional fall through
            case FlexDirection.COLUMN_REVERSE:
                int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
                int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
                if (heightMode == View.MeasureSpec.EXACTLY) {
                    mainSize = heightSize;
                } else {
                    mainSize = mFlexContainer.getLargestMainSize();
                }
                paddingAlongMainAxis = mFlexContainer.getPaddingTop()
                        + mFlexContainer.getPaddingBottom();
                break;
            default:
                throw new IllegalArgumentException("Invalid flex direction: " + flexDirection);
        }

        int flexLineIndex = 0;
        if (mIndexToFlexLine != null) {
            flexLineIndex = mIndexToFlexLine[fromIndex];
        }
        List<FlexLine> flexLines = mFlexContainer.getFlexLinesInternal();
        for (int i = flexLineIndex, size = flexLines.size(); i < size; i++) {
            FlexLine flexLine = flexLines.get(i);
            if (flexLine.mMainSize < mainSize) {
                fromIndex = expandFlexItems(widthMeasureSpec, heightMeasureSpec, flexLine,
                        mainSize, paddingAlongMainAxis, fromIndex, false);
            } else {
                fromIndex = shrinkFlexItems(widthMeasureSpec, heightMeasureSpec, flexLine,
                        mainSize, paddingAlongMainAxis, fromIndex, false);
            }
        }
    }

    private void ensureChildrenFrozen(int size) {
        if (mChildrenFrozen == null) {
            mChildrenFrozen = new boolean[size < INITIAL_CAPACITY ? INITIAL_CAPACITY : size];
        } else if (mChildrenFrozen.length < size) {
            int newCapacity = mChildrenFrozen.length * 2;
            mChildrenFrozen = new boolean[newCapacity >= size ? newCapacity : size];
        } else {
            Arrays.fill(mChildrenFrozen, false);
        }
    }

    /**
     * Expand the flex items along the main axis based on the individual mFlexGrow attribute.
     *
     * @param widthMeasureSpec     the horizontal space requirements as imposed by the parent
     * @param heightMeasureSpec    the vertical space requirements as imposed by the parent
     * @param flexLine             the flex line to which flex items belong
     * @param maxMainSize          the maximum main size. Expanded main size will be this size
     * @param paddingAlongMainAxis the padding value along the main axis
     * @param fromIndex            the start index of the children views to be expanded. This index
     *                             needs to
     *                             be an absolute index in the flex container (FlexboxLayout),
     *                             not the relative index in the flex line.
     * @param calledRecursively    true if this method is called recursively, false otherwise
     * @return the next index, the next flex line's first flex item starts from the returned index
     * @see FlexContainer#getFlexDirection()
     * @see FlexContainer#setFlexDirection(int)
     * @see FlexItem#getFlexGrow()
     */
    private int expandFlexItems(int widthMeasureSpec, int heightMeasureSpec, FlexLine flexLine,
            int maxMainSize, int paddingAlongMainAxis, int fromIndex, boolean calledRecursively) {
        int childIndex = fromIndex;
        if (flexLine.mTotalFlexGrow <= 0 || maxMainSize < flexLine.mMainSize) {
            childIndex += flexLine.mItemCount;
            return childIndex;
        }
        int sizeBeforeExpand = flexLine.mMainSize;
        boolean needsReexpand = false;
        float unitSpace = (maxMainSize - flexLine.mMainSize) / flexLine.mTotalFlexGrow;
        flexLine.mMainSize = paddingAlongMainAxis + flexLine.mDividerLengthInMainSize;

        // Setting the cross size of the flex line as the temporal value since the cross size of
        // each flex item may be changed from the initial calculation
        // (in the measureHorizontal/measureVertical method) even this method is part of the main
        // size determination.
        // E.g. If a TextView's layout_width is set to 0dp, layout_height is set to wrap_content,
        // and layout_flexGrow is set to 1, the TextView is trying to expand to the vertical
        // direction to enclose its content (in the measureHorizontal method), but
        // the width will be expanded in this method. In that case, the height needs to be measured
        // again with the expanded width.
        int largestCrossSize = 0;
        if (!calledRecursively) {
            flexLine.mCrossSize = Integer.MIN_VALUE;
        }
        float accumulatedRoundError = 0;
        for (int i = 0; i < flexLine.mItemCount; i++) {
            View child = mFlexContainer.getReorderedFlexItemAt(childIndex);
            if (child == null) {
                continue;
            } else if (child.getVisibility() == View.GONE) {
                childIndex++;
                continue;
            }
            FlexItem flexItem = (FlexItem) child.getLayoutParams();
            int flexDirection = mFlexContainer.getFlexDirection();
            if (flexDirection == FlexDirection.ROW || flexDirection == FlexDirection.ROW_REVERSE) {
                // The direction of the main axis is horizontal

                int childMeasuredWidth = child.getMeasuredWidth();
                if (mMeasuredSizeCache != null) {
                    // Retrieve the measured width from the cache because there
                    // are some cases that the view is re-created from the last measure, thus
                    // View#getMeasuredWidth returns 0.
                    // E.g. if the flex container is FlexboxLayoutManager, the case happens
                    // frequently
                    childMeasuredWidth = extractLowerInt(mMeasuredSizeCache[childIndex]);
                }
                int childMeasuredHeight = child.getMeasuredHeight();
                if (mMeasuredSizeCache != null) {
                    // Extract the measured height from the cache
                    childMeasuredHeight = extractHigherInt(mMeasuredSizeCache[childIndex]);
                }
                if (!mChildrenFrozen[childIndex] && flexItem.getFlexGrow() > 0f) {
                    float rawCalculatedWidth = childMeasuredWidth
                            + unitSpace * flexItem.getFlexGrow();
                    if (i == flexLine.mItemCount - 1) {
                        rawCalculatedWidth += accumulatedRoundError;
                        accumulatedRoundError = 0;
                    }
                    int newWidth = Math.round(rawCalculatedWidth);
                    if (newWidth > flexItem.getMaxWidth()) {
                        // This means the child can't expand beyond the value of the mMaxWidth attribute.
                        // To adjust the flex line length to the size of maxMainSize, remaining
                        // positive free space needs to be re-distributed to other flex items
                        // (children views). In that case, invoke this method again with the same
                        // fromIndex.
                        needsReexpand = true;
                        newWidth = flexItem.getMaxWidth();
                        mChildrenFrozen[childIndex] = true;
                        flexLine.mTotalFlexGrow -= flexItem.getFlexGrow();
                    } else {
                        accumulatedRoundError += (rawCalculatedWidth - newWidth);
                        if (accumulatedRoundError > 1.0) {
                            newWidth += 1;
                            accumulatedRoundError -= 1.0;
                        } else if (accumulatedRoundError < -1.0) {
                            newWidth -= 1;
                            accumulatedRoundError += 1.0;
                        }
                    }
                    int childHeightMeasureSpec = getChildHeightMeasureSpecInternal(
                            heightMeasureSpec, flexItem);
                    int childWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(newWidth,
                            View.MeasureSpec.EXACTLY);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                    childMeasuredWidth = child.getMeasuredWidth();
                    childMeasuredHeight = child.getMeasuredHeight();
                    updateMeasureCache(childIndex, childWidthMeasureSpec, childHeightMeasureSpec,
                            child);
                }
                largestCrossSize = Math.max(largestCrossSize, childMeasuredHeight
                        + flexItem.getMarginTop() + flexItem.getMarginBottom());
                flexLine.mMainSize += childMeasuredWidth + flexItem.getMarginLeft()
                        + flexItem.getMarginRight();
            } else {
                // The direction of the main axis is vertical

                int childMeasuredHeight = child.getMeasuredHeight();
                if (mMeasuredSizeCache != null) {
                    // Retrieve the measured height from the cache because there
                    // are some cases that the view is re-created from the last measure, thus
                    // View#getMeasuredHeight returns 0.
                    // E.g. if the flex container is FlexboxLayoutManager, that case happens
                    // frequently
                    childMeasuredHeight =
                            extractHigherInt(mMeasuredSizeCache[childIndex]);
                }
                int childMeasuredWidth = child.getMeasuredWidth();
                if (mMeasuredSizeCache != null) {
                    // Extract the measured width from the cache
                    childMeasuredWidth =
                            extractLowerInt(mMeasuredSizeCache[childIndex]);
                }
                if (!mChildrenFrozen[childIndex] && flexItem.getFlexGrow() > 0f) {
                    float rawCalculatedHeight = childMeasuredHeight
                            + unitSpace * flexItem.getFlexGrow();
                    if (i == flexLine.mItemCount - 1) {
                        rawCalculatedHeight += accumulatedRoundError;
                        accumulatedRoundError = 0;
                    }
                    int newHeight = Math.round(rawCalculatedHeight);
                    if (newHeight > flexItem.getMaxHeight()) {
                        // This means the child can't expand beyond the value of the mMaxHeight
                        // attribute.
                        // To adjust the flex line length to the size of maxMainSize, remaining
                        // positive free space needs to be re-distributed to other flex items
                        // (children views). In that case, invoke this method again with the same
                        // fromIndex.
                        needsReexpand = true;
                        newHeight = flexItem.getMaxHeight();
                        mChildrenFrozen[childIndex] = true;
                        flexLine.mTotalFlexGrow -= flexItem.getFlexGrow();
                    } else {
                        accumulatedRoundError += (rawCalculatedHeight - newHeight);
                        if (accumulatedRoundError > 1.0) {
                            newHeight += 1;
                            accumulatedRoundError -= 1.0;
                        } else if (accumulatedRoundError < -1.0) {
                            newHeight -= 1;
                            accumulatedRoundError += 1.0;
                        }
                    }
                    int childWidthMeasureSpec = getChildWidthMeasureSpecInternal(widthMeasureSpec,
                            flexItem);
                    int childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(newHeight,
                            View.MeasureSpec.EXACTLY);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                    childMeasuredWidth = child.getMeasuredWidth();
                    childMeasuredHeight = child.getMeasuredHeight();
                    updateMeasureCache(childIndex, childWidthMeasureSpec, childHeightMeasureSpec,
                            child);
                }
                largestCrossSize = Math.max(largestCrossSize, childMeasuredWidth
                        + flexItem.getMarginLeft() + flexItem.getMarginRight());
                flexLine.mMainSize += childMeasuredHeight + flexItem.getMarginTop()
                        + flexItem.getMarginBottom();
            }
            flexLine.mCrossSize = Math.max(flexLine.mCrossSize, largestCrossSize);
            childIndex++;
        }

        if (needsReexpand && sizeBeforeExpand != flexLine.mMainSize) {
            // Re-invoke the method with the same fromIndex to distribute the positive free space
            // that wasn't fully distributed (because of maximum length constraint)
            expandFlexItems(widthMeasureSpec, heightMeasureSpec, flexLine, maxMainSize,
                    paddingAlongMainAxis, fromIndex, true);
        }
        return childIndex;
    }

    /**
     * Shrink the flex items along the main axis based on the individual mFlexShrink attribute.
     *
     * @param widthMeasureSpec     the horizontal space requirements as imposed by the parent
     * @param heightMeasureSpec    the vertical space requirements as imposed by the parent
     * @param flexLine             the flex line to which flex items belong
     * @param maxMainSize          the maximum main size. Shrank main size will be this size
     * @param paddingAlongMainAxis the padding value along the main axis
     * @param fromIndex            the start index of the children views to be shrank. This index
     *                             needs to
     *                             be an absolute index in the flex container (FlexboxLayout),
     *                             not the relative index in the flex line.
     * @param calledRecursively    true if this method is called recursively, false otherwise
     * @return the next index, the next flex line's first flex item starts from the returned index
     * @see FlexContainer#getFlexDirection()
     * @see FlexContainer#setFlexDirection(int)
     * @see FlexItem#getFlexShrink()
     */
    private int shrinkFlexItems(int widthMeasureSpec, int heightMeasureSpec, FlexLine flexLine,
            int maxMainSize, int paddingAlongMainAxis, int fromIndex, boolean calledRecursively) {
        int childIndex = fromIndex;
        int sizeBeforeShrink = flexLine.mMainSize;
        if (flexLine.mTotalFlexShrink <= 0 || maxMainSize > flexLine.mMainSize) {
            childIndex += flexLine.mItemCount;
            return childIndex;
        }
        boolean needsReshrink = false;
        float unitShrink = (flexLine.mMainSize - maxMainSize) / flexLine.mTotalFlexShrink;
        float accumulatedRoundError = 0;
        flexLine.mMainSize = paddingAlongMainAxis + flexLine.mDividerLengthInMainSize;

        // Setting the cross size of the flex line as the temporal value since the cross size of
        // each flex item may be changed from the initial calculation
        // (in the measureHorizontal/measureVertical method) even this method is part of the main
        // size determination.
        // E.g. If a TextView's layout_width is set to 0dp, layout_height is set to wrap_content,
        // and layout_flexGrow is set to 1, the TextView is trying to expand to the vertical
        // direction to enclose its content (in the measureHorizontal method), but
        // the width will be expanded in this method. In that case, the height needs to be measured
        // again with the expanded width.
        int largestCrossSize = 0;
        if (!calledRecursively) {
            flexLine.mCrossSize = Integer.MIN_VALUE;
        }
        for (int i = 0; i < flexLine.mItemCount; i++) {
            View child = mFlexContainer.getReorderedFlexItemAt(childIndex);
            if (child == null) {
                continue;
            } else if (child.getVisibility() == View.GONE) {
                childIndex++;
                continue;
            }
            FlexItem flexItem = (FlexItem) child.getLayoutParams();
            int flexDirection = mFlexContainer.getFlexDirection();
            if (flexDirection == FlexDirection.ROW || flexDirection == FlexDirection.ROW_REVERSE) {
                // The direction of main axis is horizontal

                int childMeasuredWidth = child.getMeasuredWidth();
                if (mMeasuredSizeCache != null) {
                    // Retrieve the measured width from the cache because there
                    // are some cases that the view is re-created from the last measure, thus
                    // View#getMeasuredWidth returns 0.
                    // E.g. if the flex container is FlexboxLayoutManager, the case happens
                    // frequently
                    childMeasuredWidth = extractLowerInt(mMeasuredSizeCache[childIndex]);
                }
                int childMeasuredHeight = child.getMeasuredHeight();
                if (mMeasuredSizeCache != null) {
                    // Extract the measured height from the cache
                    childMeasuredHeight = extractHigherInt(mMeasuredSizeCache[childIndex]);
                }
                if (!mChildrenFrozen[childIndex] && flexItem.getFlexShrink() > 0f) {
                    float rawCalculatedWidth = childMeasuredWidth
                            - unitShrink * flexItem.getFlexShrink();
                    if (i == flexLine.mItemCount - 1) {
                        rawCalculatedWidth += accumulatedRoundError;
                        accumulatedRoundError = 0;
                    }
                    int newWidth = Math.round(rawCalculatedWidth);
                    if (newWidth < flexItem.getMinWidth()) {
                        // This means the child doesn't have enough space to distribute the negative
                        // free space. To adjust the flex line length down to the maxMainSize, remaining
                        // negative free space needs to be re-distributed to other flex items
                        // (children views). In that case, invoke this method again with the same
                        // fromIndex.
                        needsReshrink = true;
                        newWidth = flexItem.getMinWidth();
                        mChildrenFrozen[childIndex] = true;
                        flexLine.mTotalFlexShrink -= flexItem.getFlexShrink();
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
                    int childHeightMeasureSpec = getChildHeightMeasureSpecInternal(
                            heightMeasureSpec, flexItem);
                    int childWidthMeasureSpec =
                            View.MeasureSpec.makeMeasureSpec(newWidth, View.MeasureSpec.EXACTLY);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                    childMeasuredWidth = child.getMeasuredWidth();
                    childMeasuredHeight = child.getMeasuredHeight();
                    updateMeasureCache(childIndex, childWidthMeasureSpec, childHeightMeasureSpec,
                            child);
                }
                largestCrossSize = Math.max(largestCrossSize, childMeasuredHeight +
                        flexItem.getMarginTop() + flexItem.getMarginBottom());
                flexLine.mMainSize += childMeasuredWidth + flexItem.getMarginLeft()
                        + flexItem.getMarginRight();
            } else {
                // The direction of main axis is vertical

                int childMeasuredHeight = child.getMeasuredHeight();
                if (mMeasuredSizeCache != null) {
                    // Retrieve the measured height from the cache because there
                    // are some cases that the view is re-created from the last measure, thus
                    // View#getMeasuredHeight returns 0.
                    // E.g. if the flex container is FlexboxLayoutManager, that case happens
                    // frequently
                    childMeasuredHeight =
                            extractHigherInt(mMeasuredSizeCache[childIndex]);
                }
                int childMeasuredWidth = child.getMeasuredWidth();
                if (mMeasuredSizeCache != null) {
                    // Extract the measured width from the cache
                    childMeasuredWidth =
                            extractLowerInt(mMeasuredSizeCache[childIndex]);
                }
                if (!mChildrenFrozen[childIndex] && flexItem.getFlexShrink() > 0f) {
                    float rawCalculatedHeight = childMeasuredHeight
                            - unitShrink * flexItem.getFlexShrink();
                    if (i == flexLine.mItemCount - 1) {
                        rawCalculatedHeight += accumulatedRoundError;
                        accumulatedRoundError = 0;
                    }
                    int newHeight = Math.round(rawCalculatedHeight);
                    if (newHeight < flexItem.getMinHeight()) {
                        // Need to invoke this method again like the case flex direction is vertical
                        needsReshrink = true;
                        newHeight = flexItem.getMinHeight();
                        mChildrenFrozen[childIndex] = true;
                        flexLine.mTotalFlexShrink -= flexItem.getFlexShrink();
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
                    int childWidthMeasureSpec = getChildWidthMeasureSpecInternal(widthMeasureSpec,
                            flexItem);
                    int childHeightMeasureSpec =
                            View.MeasureSpec.makeMeasureSpec(newHeight, View.MeasureSpec.EXACTLY);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                    childMeasuredWidth = child.getMeasuredWidth();
                    childMeasuredHeight = child.getMeasuredHeight();
                    updateMeasureCache(childIndex, childWidthMeasureSpec, childHeightMeasureSpec,
                            child);
                }
                largestCrossSize = Math.max(largestCrossSize, childMeasuredWidth +
                        flexItem.getMarginLeft() + flexItem.getMarginRight());
                flexLine.mMainSize += childMeasuredHeight + flexItem.getMarginTop()
                        + flexItem.getMarginBottom();
            }
            flexLine.mCrossSize = Math.max(flexLine.mCrossSize, largestCrossSize);
            childIndex++;
        }

        if (needsReshrink && sizeBeforeShrink != flexLine.mMainSize) {
            // Re-invoke the method with the same fromIndex to distribute the negative free space
            // that wasn't fully distributed (because some views length were not enough)
            shrinkFlexItems(widthMeasureSpec, heightMeasureSpec, flexLine,
                    maxMainSize, paddingAlongMainAxis, fromIndex, true);
        }
        return childIndex;
    }

    private int getChildWidthMeasureSpecInternal(int widthMeasureSpec, FlexItem flexItem) {
        int childWidthMeasureSpec = mFlexContainer.getChildWidthMeasureSpec(widthMeasureSpec,
                mFlexContainer.getPaddingLeft() + mFlexContainer.getPaddingRight() +
                        flexItem.getMarginLeft() + flexItem.getMarginRight(),
                flexItem.getWidth());
        int childWidth = View.MeasureSpec.getSize(childWidthMeasureSpec);
        if (childWidth > flexItem.getMaxWidth()) {
            childWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(flexItem.getMaxWidth(),
                    View.MeasureSpec.getMode(childWidthMeasureSpec));
        } else if (childWidth < flexItem.getMinWidth()) {
            childWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(flexItem.getMinWidth(),
                    View.MeasureSpec.getMode(childWidthMeasureSpec));
        }
        return childWidthMeasureSpec;
    }

    private int getChildHeightMeasureSpecInternal(int heightMeasureSpec, FlexItem flexItem) {
        int childHeightMeasureSpec = mFlexContainer.getChildHeightMeasureSpec(heightMeasureSpec,
                mFlexContainer.getPaddingTop() + mFlexContainer.getPaddingBottom()
                        + flexItem.getMarginTop() + flexItem.getMarginBottom(),
                flexItem.getHeight());
        int childHeight = View.MeasureSpec.getSize(childHeightMeasureSpec);
        if (childHeight > flexItem.getMaxHeight()) {
            childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(flexItem.getMaxHeight(),
                    View.MeasureSpec.getMode(childHeightMeasureSpec));
        } else if (childHeight < flexItem.getMinHeight()) {
            childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(flexItem.getMinHeight(),
                    View.MeasureSpec.getMode(childHeightMeasureSpec));
        }
        return childHeightMeasureSpec;
    }

    /**
     * Determines the cross size (Calculate the length along the cross axis).
     * Expand the cross size only if the height mode is MeasureSpec.EXACTLY, otherwise
     * use the sum of cross sizes of all flex lines.
     *
     * @param widthMeasureSpec      horizontal space requirements as imposed by the parent
     * @param heightMeasureSpec     vertical space requirements as imposed by the parent
     * @param paddingAlongCrossAxis the padding value for the FlexboxLayout along the cross axis
     * @see FlexContainer#getFlexDirection()
     * @see FlexContainer#setFlexDirection(int)
     * @see FlexContainer#getAlignContent()
     * @see FlexContainer#setAlignContent(int)
     */
    void determineCrossSize(int widthMeasureSpec, int heightMeasureSpec,
            int paddingAlongCrossAxis) {
        // The MeasureSpec mode along the cross axis
        int mode;
        // The MeasureSpec size along the cross axis
        int size;
        int flexDirection = mFlexContainer.getFlexDirection();
        switch (flexDirection) {
            case FlexDirection.ROW: // Intentional fall through
            case FlexDirection.ROW_REVERSE:
                mode = View.MeasureSpec.getMode(heightMeasureSpec);
                size = View.MeasureSpec.getSize(heightMeasureSpec);
                break;
            case FlexDirection.COLUMN: // Intentional fall through
            case FlexDirection.COLUMN_REVERSE:
                mode = View.MeasureSpec.getMode(widthMeasureSpec);
                size = View.MeasureSpec.getSize(widthMeasureSpec);
                break;
            default:
                throw new IllegalArgumentException("Invalid flex direction: " + flexDirection);
        }
        List<FlexLine> flexLines = mFlexContainer.getFlexLinesInternal();
        if (mode == View.MeasureSpec.EXACTLY) {
            int totalCrossSize = mFlexContainer.getSumOfCrossSize() + paddingAlongCrossAxis;
            if (flexLines.size() == 1) {
                flexLines.get(0).mCrossSize = size - paddingAlongCrossAxis;
                // alignContent property is valid only if the Flexbox has at least two lines
            } else if (flexLines.size() >= 2 && totalCrossSize < size) {
                switch (mFlexContainer.getAlignContent()) {
                    case AlignContent.STRETCH: {
                        float freeSpaceUnit = (size - totalCrossSize) / (float) flexLines.size();
                        float accumulatedError = 0;
                        for (int i = 0, flexLinesSize = flexLines.size(); i < flexLinesSize; i++) {
                            FlexLine flexLine = flexLines.get(i);
                            float newCrossSizeAsFloat = flexLine.mCrossSize + freeSpaceUnit;
                            if (i == flexLines.size() - 1) {
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
                            flexLine.mCrossSize = newCrossSize;
                        }
                        break;
                    }
                    case AlignContent.SPACE_AROUND: {
                        // The value of free space along the cross axis which needs to be put on top
                        // and below the bottom of each flex line.
                        int spaceTopAndBottom = size - totalCrossSize;
                        // The number of spaces along the cross axis
                        int numberOfSpaces = flexLines.size() * 2;
                        spaceTopAndBottom = spaceTopAndBottom / numberOfSpaces;
                        List<FlexLine> newFlexLines = new ArrayList<>();
                        FlexLine dummySpaceFlexLine = new FlexLine();
                        dummySpaceFlexLine.mCrossSize = spaceTopAndBottom;
                        for (FlexLine flexLine : flexLines) {
                            newFlexLines.add(dummySpaceFlexLine);
                            newFlexLines.add(flexLine);
                            newFlexLines.add(dummySpaceFlexLine);
                        }
                        mFlexContainer.setFlexLines(newFlexLines);
                        break;
                    }
                    case AlignContent.SPACE_BETWEEN: {
                        // The value of free space along the cross axis between each flex line.
                        float spaceBetweenFlexLine = size - totalCrossSize;
                        int numberOfSpaces = flexLines.size() - 1;
                        spaceBetweenFlexLine = spaceBetweenFlexLine / (float) numberOfSpaces;
                        float accumulatedError = 0;
                        List<FlexLine> newFlexLines = new ArrayList<>();
                        for (int i = 0, flexLineSize = flexLines.size(); i < flexLineSize; i++) {
                            FlexLine flexLine = flexLines.get(i);
                            newFlexLines.add(flexLine);

                            if (i != flexLines.size() - 1) {
                                FlexLine dummySpaceFlexLine = new FlexLine();
                                if (i == flexLines.size() - 2) {
                                    // The last dummy space block in the flex container.
                                    // Adjust the cross size by the accumulated error.
                                    dummySpaceFlexLine.mCrossSize = Math
                                            .round(spaceBetweenFlexLine + accumulatedError);
                                    accumulatedError = 0;
                                } else {
                                    dummySpaceFlexLine.mCrossSize = Math
                                            .round(spaceBetweenFlexLine);
                                }
                                accumulatedError += (spaceBetweenFlexLine
                                        - dummySpaceFlexLine.mCrossSize);
                                if (accumulatedError > 1) {
                                    dummySpaceFlexLine.mCrossSize += 1;
                                    accumulatedError -= 1;
                                } else if (accumulatedError < -1) {
                                    dummySpaceFlexLine.mCrossSize -= 1;
                                    accumulatedError += 1;
                                }
                                newFlexLines.add(dummySpaceFlexLine);
                            }
                        }
                        mFlexContainer.setFlexLines(newFlexLines);
                        break;
                    }
                    case AlignContent.CENTER: {
                        int spaceAboveAndBottom = size - totalCrossSize;
                        spaceAboveAndBottom = spaceAboveAndBottom / 2;
                        List<FlexLine> newFlexLines = new ArrayList<>();
                        FlexLine dummySpaceFlexLine = new FlexLine();
                        dummySpaceFlexLine.mCrossSize = spaceAboveAndBottom;
                        for (int i = 0, flexLineSize = flexLines.size(); i < flexLineSize; i++) {
                            if (i == 0) {
                                newFlexLines.add(dummySpaceFlexLine);
                            }
                            FlexLine flexLine = flexLines.get(i);
                            newFlexLines.add(flexLine);
                            if (i == flexLines.size() - 1) {
                                newFlexLines.add(dummySpaceFlexLine);
                            }
                        }
                        mFlexContainer.setFlexLines(newFlexLines);
                        break;
                    }
                    case AlignContent.FLEX_END: {
                        int spaceTop = size - totalCrossSize;
                        FlexLine dummySpaceFlexLine = new FlexLine();
                        dummySpaceFlexLine.mCrossSize = spaceTop;
                        flexLines.add(0, dummySpaceFlexLine);
                        break;
                    }
                    case AlignContent.FLEX_START:
                        // No op. Just to cover the available switch statement options
                        break;
                }
            }
        }
    }

    void stretchViews() {
        stretchViews(0);
    }

    /**
     * Expand the view if the {@link FlexContainer#getAlignItems()} attribute is set to {@link
     * AlignItems#STRETCH} or {@link FlexboxLayout.LayoutParams#mAlignSelf} is set as
     * {@link AlignItems#STRETCH}.
     *
     * @param fromIndex the index from which value, stretch is calculated
     * @see FlexContainer#getFlexDirection()
     * @see FlexContainer#setFlexDirection(int)
     * @see FlexContainer#getAlignItems()
     * @see FlexContainer#setAlignItems(int)
     * @see FlexboxLayout.LayoutParams#mAlignSelf
     */
    void stretchViews(int fromIndex) {
        if (fromIndex >= mFlexContainer.getFlexItemCount()) {
            return;
        }
        int flexDirection = mFlexContainer.getFlexDirection();
        if (mFlexContainer.getAlignItems() == AlignItems.STRETCH) {
            int viewIndex = fromIndex;
            int flexLineIndex = 0;
            if (mIndexToFlexLine != null) {
                flexLineIndex = mIndexToFlexLine[fromIndex];
            }
            List<FlexLine> flexLines = mFlexContainer.getFlexLinesInternal();
            for (int i = flexLineIndex, size = flexLines.size(); i < size; i++) {
                FlexLine flexLine = flexLines.get(i);
                for (int j = 0, itemCount = flexLine.mItemCount; j < itemCount;
                        j++, viewIndex++) {
                    View view = mFlexContainer.getReorderedFlexItemAt(viewIndex);
                    if (view == null || view.getVisibility() == View.GONE) {
                        continue;
                    }
                    FlexItem flexItem = (FlexItem) view.getLayoutParams();
                    if (flexItem.getAlignSelf() != AlignSelf.AUTO &&
                            flexItem.getAlignSelf() != AlignItems.STRETCH) {
                        continue;
                    }
                    switch (flexDirection) {
                        case FlexDirection.ROW: // Intentional fall through
                        case FlexDirection.ROW_REVERSE:
                            stretchViewVertically(view, flexLine.mCrossSize, viewIndex);
                            break;
                        case FlexDirection.COLUMN:
                        case FlexDirection.COLUMN_REVERSE:
                            stretchViewHorizontally(view, flexLine.mCrossSize, viewIndex);
                            break;
                        default:
                            throw new IllegalArgumentException(
                                    "Invalid flex direction: " + flexDirection);
                    }
                }
            }
        } else {
            for (FlexLine flexLine : mFlexContainer.getFlexLinesInternal()) {
                for (Integer index : flexLine.mIndicesAlignSelfStretch) {
                    View view = mFlexContainer.getReorderedFlexItemAt(index);
                    switch (flexDirection) {
                        case FlexDirection.ROW: // Intentional fall through
                        case FlexDirection.ROW_REVERSE:
                            stretchViewVertically(view, flexLine.mCrossSize, index);
                            break;
                        case FlexDirection.COLUMN:
                        case FlexDirection.COLUMN_REVERSE:
                            stretchViewHorizontally(view, flexLine.mCrossSize, index);
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
     * @param index     the index of the view
     */
    private void stretchViewVertically(View view, int crossSize, int index) {
        FlexItem flexItem = (FlexItem) view.getLayoutParams();
        int newHeight = crossSize - flexItem.getMarginTop() - flexItem.getMarginBottom();
        newHeight = Math.max(newHeight, flexItem.getMinHeight());
        newHeight = Math.min(newHeight, flexItem.getMaxHeight());
        int childWidthSpec;
        int measuredWidth;
        if (mMeasuredSizeCache != null) {
            // Retrieve the measured height from the cache because there
            // are some cases that the view is re-created from the last measure, thus
            // View#getMeasuredHeight returns 0.
            // E.g. if the flex container is FlexboxLayoutManager, that case happens
            // frequently
            measuredWidth = extractLowerInt(mMeasuredSizeCache[index]);
        } else {
            measuredWidth = view.getMeasuredWidth();
        }
        childWidthSpec = View.MeasureSpec.makeMeasureSpec(measuredWidth,
                View.MeasureSpec.EXACTLY);

        int childHeightSpec = View.MeasureSpec.makeMeasureSpec(newHeight, View.MeasureSpec.EXACTLY);
        view.measure(childWidthSpec, childHeightSpec);

        updateMeasureCache(index, childWidthSpec, childHeightSpec, view);
    }

    /**
     * Expand the view horizontally to the size of the crossSize (considering the view margins)
     *
     * @param view      the View to be stretched
     * @param crossSize the cross size
     * @param index     the index of the view
     */
    private void stretchViewHorizontally(View view, int crossSize, int index) {
        FlexItem flexItem = (FlexItem) view.getLayoutParams();
        int newWidth = crossSize - flexItem.getMarginLeft() - flexItem.getMarginRight();
        newWidth = Math.max(newWidth, flexItem.getMinWidth());
        newWidth = Math.min(newWidth, flexItem.getMaxWidth());
        int childHeightSpec;
        int measuredHeight;
        if (mMeasuredSizeCache != null) {
            // Retrieve the measured height from the cache because there
            // are some cases that the view is re-created from the last measure, thus
            // View#getMeasuredHeight returns 0.
            // E.g. if the flex container is FlexboxLayoutManager, that case happens
            // frequently
            measuredHeight = extractHigherInt(mMeasuredSizeCache[index]);
        } else {
            measuredHeight = view.getMeasuredHeight();
        }
        childHeightSpec = View.MeasureSpec.makeMeasureSpec(measuredHeight,
                View.MeasureSpec.EXACTLY);
        int childWidthSpec = View.MeasureSpec.makeMeasureSpec(newWidth, View.MeasureSpec.EXACTLY);
        view.measure(childWidthSpec, childHeightSpec);
        updateMeasureCache(index, childWidthSpec, childHeightSpec, view);
    }

    /**
     * Place a single View when the layout direction is horizontal
     * ({@link FlexContainer#getFlexDirection()} is either {@link FlexDirection#ROW} or
     * {@link FlexDirection#ROW_REVERSE}).
     *
     * @param view     the View to be placed
     * @param flexLine the {@link FlexLine} where the View belongs to
     * @param left     the left position of the View, which the View's margin is already taken
     *                 into account
     * @param top      the top position of the flex line where the View belongs to. The actual
     *                 View's top position is shifted depending on the flexWrap and alignItems
     *                 attributes
     * @param right    the right position of the View, which the View's margin is already taken
     *                 into account
     * @param bottom   the bottom position of the flex line where the View belongs to. The actual
     *                 View's bottom position is shifted depending on the flexWrap and alignItems
     *                 attributes
     * @see FlexContainer#getAlignItems()
     * @see FlexContainer#setAlignItems(int)
     * @see FlexItem#getAlignSelf()
     */
    void layoutSingleChildHorizontal(View view, FlexLine flexLine, int left, int top, int right,
            int bottom) {
        FlexItem flexItem = (FlexItem) view.getLayoutParams();
        int alignItems = mFlexContainer.getAlignItems();
        if (flexItem.getAlignSelf() != AlignSelf.AUTO) {
            // Expecting the values for alignItems and mAlignSelf match except for ALIGN_SELF_AUTO.
            // Assigning the mAlignSelf value as alignItems should work.
            alignItems = flexItem.getAlignSelf();
        }
        int crossSize = flexLine.mCrossSize;
        switch (alignItems) {
            case AlignItems.FLEX_START: // Intentional fall through
            case AlignItems.STRETCH:
                if (mFlexContainer.getFlexWrap() != FlexWrap.WRAP_REVERSE) {
                    view.layout(left, top + flexItem.getMarginTop(), right,
                            bottom + flexItem.getMarginTop());
                } else {
                    view.layout(left, top - flexItem.getMarginBottom(), right,
                            bottom - flexItem.getMarginBottom());
                }
                break;
            case AlignItems.BASELINE:
                if (mFlexContainer.getFlexWrap() != FlexWrap.WRAP_REVERSE) {
                    int marginTop = flexLine.mMaxBaseline - view.getBaseline();
                    marginTop = Math.max(marginTop, flexItem.getMarginTop());
                    view.layout(left, top + marginTop, right, bottom + marginTop);
                } else {
                    int marginBottom = flexLine.mMaxBaseline - view.getMeasuredHeight() + view
                            .getBaseline();
                    marginBottom = Math.max(marginBottom, flexItem.getMarginBottom());
                    view.layout(left, top - marginBottom, right, bottom - marginBottom);
                }
                break;
            case AlignItems.FLEX_END:
                if (mFlexContainer.getFlexWrap() != FlexWrap.WRAP_REVERSE) {
                    view.layout(left,
                            top + crossSize - view.getMeasuredHeight() - flexItem.getMarginBottom(),
                            right, top + crossSize - flexItem.getMarginBottom());
                } else {
                    // If the flexWrap == WRAP_REVERSE, the direction of the
                    // flexEnd is flipped (from top to bottom).
                    view.layout(left,
                            top - crossSize + view.getMeasuredHeight() + flexItem.getMarginTop(),
                            right, bottom - crossSize + view.getMeasuredHeight() + flexItem
                                    .getMarginTop());
                }
                break;
            case AlignItems.CENTER:
                int topFromCrossAxis = (crossSize - view.getMeasuredHeight()
                        + flexItem.getMarginTop() - flexItem.getMarginBottom()) / 2;
                if (mFlexContainer.getFlexWrap() != FlexWrap.WRAP_REVERSE) {
                    view.layout(left, top + topFromCrossAxis,
                            right, top + topFromCrossAxis + view.getMeasuredHeight());
                } else {
                    view.layout(left, top - topFromCrossAxis,
                            right, top - topFromCrossAxis + view.getMeasuredHeight());
                }
                break;
        }
    }

    /**
     * Place a single View when the layout direction is vertical
     * ({@link FlexContainer#getFlexDirection()} is either {@link FlexDirection#COLUMN} or
     * {@link FlexDirection#COLUMN_REVERSE}).
     *
     * @param view     the View to be placed
     * @param flexLine the {@link FlexLine} where the View belongs to
     * @param isRtl    {@code true} if the layout direction is right to left, {@code false}
     *                 otherwise
     * @param left     the left position of the flex line where the View belongs to. The actual
     *                 View's left position is shifted depending on the isRtl and alignItems
     *                 attributes
     * @param top      the top position of the View, which the View's margin is already taken
     *                 into account
     * @param right    the right position of the flex line where the View belongs to. The actual
     *                 View's right position is shifted depending on the isRtl and alignItems
     *                 attributes
     * @param bottom   the bottom position of the View, which the View's margin is already taken
     *                 into account
     * @see FlexContainer#getAlignItems()
     * @see FlexContainer#setAlignItems(int)
     * @see FlexItem#getAlignSelf()
     */
    void layoutSingleChildVertical(View view, FlexLine flexLine, boolean isRtl,
            int left, int top, int right, int bottom) {
        FlexItem flexItem = (FlexItem) view.getLayoutParams();
        int alignItems = mFlexContainer.getAlignItems();
        if (flexItem.getAlignSelf() != AlignSelf.AUTO) {
            // Expecting the values for alignItems and mAlignSelf match except for ALIGN_SELF_AUTO.
            // Assigning the mAlignSelf value as alignItems should work.
            alignItems = flexItem.getAlignSelf();
        }
        int crossSize = flexLine.mCrossSize;
        switch (alignItems) {
            case AlignItems.FLEX_START: // Intentional fall through
            case AlignItems.STRETCH: // Intentional fall through
            case AlignItems.BASELINE:
                if (!isRtl) {
                    view.layout(left + flexItem.getMarginLeft(), top,
                            right + flexItem.getMarginLeft(), bottom);
                } else {
                    view.layout(left - flexItem.getMarginRight(), top,
                            right - flexItem.getMarginRight(), bottom);
                }
                break;
            case AlignItems.FLEX_END:
                if (!isRtl) {
                    view.layout(
                            left + crossSize - view.getMeasuredWidth() - flexItem.getMarginRight(),
                            top,
                            right + crossSize - view.getMeasuredWidth() - flexItem.getMarginRight(),
                            bottom);
                } else {
                    // If the flexWrap == WRAP_REVERSE, the direction of the
                    // flexEnd is flipped (from left to right).
                    view.layout(
                            left - crossSize + view.getMeasuredWidth() + flexItem.getMarginLeft(),
                            top,
                            right - crossSize + view.getMeasuredWidth() + flexItem.getMarginLeft(),
                            bottom);
                }
                break;
            case AlignItems.CENTER:
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)
                        view.getLayoutParams();
                int leftFromCrossAxis = (crossSize - view.getMeasuredWidth()
                        + MarginLayoutParamsCompat.getMarginStart(lp)
                        - MarginLayoutParamsCompat.getMarginEnd(lp)) / 2;
                if (!isRtl) {
                    view.layout(left + leftFromCrossAxis, top, right + leftFromCrossAxis, bottom);
                } else {
                    view.layout(left - leftFromCrossAxis, top, right - leftFromCrossAxis, bottom);
                }
                break;
        }
    }

    void ensureMeasuredSizeCache(int size) {
        if (mMeasuredSizeCache == null) {
            mMeasuredSizeCache = new long[size < INITIAL_CAPACITY ? INITIAL_CAPACITY : size];
        } else if (mMeasuredSizeCache.length < size) {
            int newCapacity = mMeasuredSizeCache.length * 2;
            newCapacity = newCapacity >= size ? newCapacity : size;
            mMeasuredSizeCache = Arrays.copyOf(mMeasuredSizeCache, newCapacity);
        }
    }

    void ensureMeasureSpecCache(int size) {
        if (mMeasureSpecCache == null) {
            mMeasureSpecCache = new long[size < INITIAL_CAPACITY ? INITIAL_CAPACITY : size];
        } else if (mMeasureSpecCache.length < size) {
            int newCapacity = mMeasureSpecCache.length * 2;
            newCapacity = newCapacity >= size ? newCapacity : size;
            mMeasureSpecCache = Arrays.copyOf(mMeasureSpecCache, newCapacity);
        }
    }

    /**
     * @param longValue the long value that consists of width and height measure specs
     * @return the int value which consists from the lower 8 bits
     * @see #makeCombinedLong(int, int)
     */
    int extractLowerInt(long longValue) {
        return (int) longValue;
    }

    /**
     * @param longValue the long value that consists of width and height measure specs
     * @return the int value which consists from the higher 8 bits
     * @see #makeCombinedLong(int, int)
     */
    int extractHigherInt(long longValue) {
        return (int) (longValue >> 32);
    }

    /**
     * Make a long value from the a width measure spec and a height measure spec.
     * The first 32 bit is used for the height measure spec and the last 32 bit is used for the
     * width measure spec.
     *
     * @param widthMeasureSpec  the width measure spec to consist the result long value
     * @param heightMeasureSpec the height measure spec to consist the result long value
     * @return the combined long value
     * @see #extractLowerInt(long)
     * @see #extractHigherInt(long)
     */
    @VisibleForTesting
    long makeCombinedLong(int widthMeasureSpec, int heightMeasureSpec) {
        // Suppress sign extension for the low bytes
        return (long) heightMeasureSpec << 32 | (long) widthMeasureSpec & MEASURE_SPEC_WIDTH_MASK;
    }

    private void updateMeasureCache(int index, int widthMeasureSpec, int heightMeasureSpec,
            View view) {
        if (mMeasureSpecCache != null) {
            mMeasureSpecCache[index] = makeCombinedLong(
                    widthMeasureSpec,
                    heightMeasureSpec);
        }
        if (mMeasuredSizeCache != null) {
            mMeasuredSizeCache[index] = makeCombinedLong(
                    view.getMeasuredWidth(),
                    view.getMeasuredHeight());
        }
    }

    void ensureIndexToFlexLine(int size) {
        if (mIndexToFlexLine == null) {
            mIndexToFlexLine = new int[size < INITIAL_CAPACITY ? INITIAL_CAPACITY : size];
        } else if (mIndexToFlexLine.length < size) {
            int newCapacity = mIndexToFlexLine.length * 2;
            newCapacity = newCapacity >= size ? newCapacity : size;
            mIndexToFlexLine = Arrays.copyOf(mIndexToFlexLine, newCapacity);
        }
    }

    /**
     * Clear the from flex lines and the caches from the index passed as an argument.
     *
     * @param flexLines    the flex lines to be cleared
     * @param fromFlexItem the index from which, flex lines are cleared
     */
    void clearFlexLines(List<FlexLine> flexLines, int fromFlexItem) {
        assert mIndexToFlexLine != null;
        assert mMeasureSpecCache != null;

        int fromFlexLine = mIndexToFlexLine[fromFlexItem];
        // Deleting from the last to avoid unneeded copy it happens when deleting the middle of the
        // item in the ArrayList
        for (int i = flexLines.size() - 1; i >= fromFlexLine; i--) {
            flexLines.remove(i);
        }

        int fillTo = mIndexToFlexLine.length - 1;
        if (fromFlexItem > fillTo) {
            Arrays.fill(mIndexToFlexLine, NO_POSITION);
        } else {
            Arrays.fill(mIndexToFlexLine, fromFlexItem, fillTo, NO_POSITION);
        }

        fillTo = mMeasureSpecCache.length - 1;
        if (fromFlexItem > fillTo) {
            Arrays.fill(mMeasureSpecCache, 0);
        } else {
            Arrays.fill(mMeasureSpecCache, fromFlexItem, fillTo, 0);
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

    static class FlexLinesResult {

        List<FlexLine> mFlexLines;

        int mChildState;
    }
}
