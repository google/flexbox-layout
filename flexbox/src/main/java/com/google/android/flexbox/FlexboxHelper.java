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
import android.support.v4.view.ViewCompat;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Offers various calculations for Flexbox to use the common logic between the classes such as
 * {@link FlexboxLayout} and {@link FlexboxLayoutManager}.
 */
class FlexboxHelper {

    private final FlexContainer mFlexContainer;

    /**
     * Holds reordered indices, which {@link FlexItem#getOrder()} parameters are taken
     * into account
     */
    int[] mReorderedIndices;

    /**
     * Caches the {@link FlexItem#getOrder()} attributes for children views.
     * Key: the index of the view reordered indices using the {@link FlexItem#getOrder()}
     * isn't taken into account)
     * Value: the value for the order attribute
     */
    private SparseIntArray mOrderCache;

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
     * {0, 0}, {1, 0}, {2, 0}, {3, 1}, {4, 1}
     * </p>
     */
    SparseIntArray mIndexToFlexLine;

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
            ViewGroup.LayoutParams paramsForViewBeforeAdded) {
        int childCount = mFlexContainer.getFlexItemCount();
        List<Order> orders = createOrders(childCount);
        Order orderForViewToBeAdded = new Order();
        if (viewBeforeAdded != null
                && paramsForViewBeforeAdded instanceof FlexItem) {
            orderForViewToBeAdded.order = ((FlexItem)
                    paramsForViewBeforeAdded).getOrder();
        } else {
            orderForViewToBeAdded.order = FlexboxLayout.LayoutParams.ORDER_DEFAULT;
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

        return sortOrdersIntoReorderedIndices(childCount + 1, orders);
    }

    /**
     * Create an array, which indicates the reordered indices that
     * {@link FlexItem#getOrder()} attributes are taken into account.
     *
     * @return @return an array which have the reordered indices
     */
    int[] createReorderedIndices() {
        int childCount = mFlexContainer.getFlexItemCount();
        List<Order> orders = createOrders(childCount);
        return sortOrdersIntoReorderedIndices(childCount, orders);
    }

    @NonNull
    private List<Order> createOrders(int childCount) {
        List<Order> orders = new ArrayList<>(childCount);
        for (int i = 0; i < childCount; i++) {
            View child = mFlexContainer.getFlexItemAt(i);
            FlexItem flexItem = (FlexItem) child
                    .getLayoutParams();
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
    boolean isOrderChangedFromLastMeasurement() {
        int childCount = mFlexContainer.getFlexItemCount();
        if (mOrderCache == null) {
            mOrderCache = new SparseIntArray(childCount);
        }
        if (mOrderCache.size() != childCount) {
            return true;
        }
        for (int i = 0; i < childCount; i++) {
            View view = mFlexContainer.getFlexItemAt(i);
            if (view == null) {
                continue;
            }
            FlexItem flexItem = (FlexItem) view.getLayoutParams();
            if (flexItem.getOrder() != mOrderCache.get(i)) {
                return true;
            }
        }
        return false;
    }

    private int[] sortOrdersIntoReorderedIndices(int childCount, List<Order> orders) {
        Collections.sort(orders);
        if (mOrderCache == null) {
            mOrderCache = new SparseIntArray(childCount);
        }
        mOrderCache.clear();
        int[] reorderedIndices = new int[childCount];
        int i = 0;
        for (Order order : orders) {
            reorderedIndices[i] = order.index;
            mOrderCache.append(i, order.order);
            i++;
        }
        return reorderedIndices;
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
     * @return a instance of {@link FlexLinesResult} that contains a list of flex lines and the
     * child state used by {@link View#setMeasuredDimension(int, int)}.
     */
    FlexLinesResult calculateHorizontalFlexLines(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        FlexLinesResult result = new FlexLinesResult();
        List<FlexLine> flexLines = new ArrayList<>();
        result.mFlexLines = flexLines;
        int childCount = mFlexContainer.getFlexItemCount();
        int childState = 0;
        // These padding values are treated as agnostic of the RTL or LTR, using the left and
        // right padding values doesn't cause a problem
        int paddingLeft = mFlexContainer.getPaddingLeft();
        int paddingRight = mFlexContainer.getPaddingRight();
        int largestHeightInRow = Integer.MIN_VALUE;
        FlexLine flexLine = new FlexLine();

        // The index of the view in a same flex line.
        int indexInFlexLine = 0;
        flexLine.mMainSize = paddingLeft + paddingRight;
        for (int i = 0; i < childCount; i++) {
            View child = mFlexContainer.getReorderedFlexItemAt(i);
            if (child == null) {
                addFlexLineIfLastFlexItem(flexLines, i, childCount, flexLine);
                continue;
            } else if (child.getVisibility() == View.GONE) {
                flexLine.mGoneItemCount++;
                flexLine.mItemCount++;
                addFlexLineIfLastFlexItem(flexLines, i, childCount, flexLine);
                continue;
            }

            FlexItem flexItem = (FlexItem) child.getLayoutParams();
            if (flexItem.getAlignSelf() == AlignItems.STRETCH) {
                flexLine.mIndicesAlignSelfStretch.add(i);
            }
            int childWidth = flexItem.getWidth();
            if (flexItem.getFlexBasisPercent() != FlexItem.FLEX_BASIS_PERCENT_DEFAULT
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

            // Check the size constraint after the first measurement for the child
            // To prevent the child's width/height violate the size constraints imposed by the
            // {@link FlexItem#getMinWidth()}, {@link FlexItem#getMinHeight()},
            // {@link FlexItem#getMaxWidth()} and {@link FlexItem#getMaxHeight()} attributes.
            // E.g. When the child's layout_width is wrap_content the measured width may be
            // less than the min width after the first measurement.
            checkSizeConstraints(child);

            childState = ViewCompat
                    .combineMeasuredStates(childState, ViewCompat.getMeasuredState(child));
            largestHeightInRow = Math.max(largestHeightInRow,
                    child.getMeasuredHeight() + flexItem.getMarginTop() + flexItem
                            .getMarginBottom());

            if (isWrapRequired(widthMode, widthSize, flexLine.mMainSize,
                    child.getMeasuredWidth() + flexItem.getMarginLeft() + flexItem.getMarginRight(),
                    flexItem, i, indexInFlexLine)) {
                if (flexLine.getItemCountNotGone() > 0) {
                    addFlexLine(flexLines, flexLine, i - 1);
                }

                flexLine = new FlexLine();
                flexLine.mItemCount = 1;
                flexLine.mMainSize = paddingLeft + paddingRight;
                largestHeightInRow = child.getMeasuredHeight() + flexItem.getMarginTop()
                        + flexItem.getMarginBottom();
                indexInFlexLine = 0;
            } else {
                flexLine.mItemCount++;
                indexInFlexLine++;
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
            addFlexLineIfLastFlexItem(flexLines, i, childCount, flexLine);
        }
        result.mChildState = childState;
        return result;
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
     * @return a instance of {@link FlexLinesResult} that contains a list of flex lines and the
     * child state used by {@link View#setMeasuredDimension(int, int)}.
     */
    FlexLinesResult calculateVerticalFlexLines(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        FlexLinesResult result = new FlexLinesResult();
        List<FlexLine> flexLines = new ArrayList<>();
        result.mFlexLines = flexLines;
        int childCount = mFlexContainer.getFlexItemCount();
        int childState = 0;

        int paddingTop = mFlexContainer.getPaddingTop();
        int paddingBottom = mFlexContainer.getPaddingBottom();
        int largestWidthInColumn = Integer.MIN_VALUE;
        FlexLine flexLine = new FlexLine();
        flexLine.mMainSize = paddingTop + paddingBottom;
        // The index of the view in a same flex line.
        int indexInFlexLine = 0;
        for (int i = 0; i < childCount; i++) {
            View child = mFlexContainer.getReorderedFlexItemAt(i);
            if (child == null) {
                addFlexLineIfLastFlexItem(flexLines, i, childCount, flexLine);
                continue;
            } else if (child.getVisibility() == View.GONE) {
                flexLine.mGoneItemCount++;
                flexLine.mItemCount++;
                addFlexLineIfLastFlexItem(flexLines, i, childCount, flexLine);
                continue;
            }

            FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) child.getLayoutParams();
            if (lp.getAlignSelf() == AlignItems.STRETCH) {
                flexLine.mIndicesAlignSelfStretch.add(i);
            }

            int childHeight = lp.height;
            if (lp.getFlexBasisPercent() != FlexboxLayout.LayoutParams.FLEX_BASIS_PERCENT_DEFAULT
                    && heightMode == View.MeasureSpec.EXACTLY) {
                childHeight = Math.round(heightSize * lp.getFlexBasisPercent());
                // Use the dimension from the layout_height attribute if the heightMode is not
                // MeasureSpec.EXACTLY even if any fraction value is set to layout_flexBasisPercent.
                // There are likely quite few use cases where assigning any fraction values
                // with heightMode is not MeasureSpec.EXACTLY (e.g. FlexboxLayout's layout_height
                // is set to wrap_content)
            }

            int childWidthMeasureSpec = mFlexContainer
                    .getChildWidthMeasureSpec(widthMeasureSpec,
                            mFlexContainer.getPaddingLeft() + mFlexContainer.getPaddingRight()
                                    + lp.leftMargin + lp.rightMargin, lp.width);
            int childHeightMeasureSpec = mFlexContainer
                    .getChildHeightMeasureSpec(heightMeasureSpec,
                            mFlexContainer.getPaddingTop() + mFlexContainer.getPaddingBottom()
                                    + lp.topMargin + lp.bottomMargin, childHeight);
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

            // Check the size constraint after the first measurement for the child
            // To prevent the child's width/height violate the size constraints imposed by the
            // {@link LayoutParams#mMinWidth}, {@link LayoutParams#mMinHeight},
            // {@link LayoutParams#mMaxWidth} and {@link LayoutParams#mMaxHeight} attributes.
            // E.g. When the child's layout_height is wrap_content the measured height may be
            // less than the min height after the first measurement.
            checkSizeConstraints(child);

            childState = ViewCompat
                    .combineMeasuredStates(childState, ViewCompat.getMeasuredState(child));
            largestWidthInColumn = Math.max(largestWidthInColumn,
                    child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);

            if (isWrapRequired(heightMode, heightSize, flexLine.mMainSize,
                    child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin, lp,
                    i, indexInFlexLine)) {
                if (flexLine.getItemCountNotGone() > 0) {
                    addFlexLine(flexLines, flexLine, i - 1);
                }

                flexLine = new FlexLine();
                flexLine.mItemCount = 1;
                flexLine.mMainSize = paddingTop + paddingBottom;
                largestWidthInColumn = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                indexInFlexLine = 0;
            } else {
                flexLine.mItemCount++;
                indexInFlexLine++;
            }
            flexLine.mMainSize += child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            flexLine.mTotalFlexGrow += lp.getFlexGrow();
            flexLine.mTotalFlexShrink += lp.getFlexShrink();
            // Temporarily set the cross axis length as the largest child width in the column
            // Expand along the cross axis depending on the mAlignContent property if needed
            // later
            flexLine.mCrossSize = Math.max(flexLine.mCrossSize, largestWidthInColumn);

            mFlexContainer.onNewFlexItemAdded(i, indexInFlexLine, flexLine);
            addFlexLineIfLastFlexItem(flexLines, i, childCount, flexLine);
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

    private void addFlexLineIfLastFlexItem(List<FlexLine> flexLines, int childIndex, int childCount,
            FlexLine flexLine) {
        if (childIndex == childCount - 1 && flexLine.getItemCountNotGone() != 0) {
            // Add the flex line if this item is the last item
            addFlexLine(flexLines, flexLine, childIndex);
        }
    }

    private List<FlexLine> addFlexLine(List<FlexLine> flexLines, FlexLine flexLine, int index) {
        mFlexContainer.onNewFlexLineAdded(flexLine);
        if (mIndexToFlexLine == null) {
            mIndexToFlexLine = new SparseIntArray();
        }
        mIndexToFlexLine.append(index, flexLines.size());

        flexLines.add(flexLine);
        return flexLines;
    }

    /**
     * Checks if the view's width/height don't violate the minimum/maximum size constraints imposed
     * by the {@link FlexItem#getMinWidth()}, {@link FlexItem#getMinHeight()},
     * {@link FlexItem#getMaxWidth()} and {@link FlexItem#getMaxHeight()} attributes.
     *
     * @param view the view to be checked
     */
    private void checkSizeConstraints(View view) {
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
            view.measure(View.MeasureSpec.makeMeasureSpec(childWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(childHeight, View.MeasureSpec.EXACTLY));
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
