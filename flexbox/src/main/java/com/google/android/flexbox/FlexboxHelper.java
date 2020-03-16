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

import static com.google.android.flexbox.FlexContainer.NOT_SET;
import static com.google.android.flexbox.FlexItem.FLEX_BASIS_PERCENT_DEFAULT;
import static com.google.android.flexbox.FlexItem.FLEX_GROW_DEFAULT;
import static com.google.android.flexbox.FlexItem.FLEX_SHRINK_NOT_SET;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.graphics.drawable.Drawable;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.widget.CompoundButtonCompat;

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
            orderCache.append(order.index, order.order);
            i++;
        }
        return reorderedIndices;
    }

    /**
     * Calculate how many flex lines are needed in the flex container.
     * This method should calculate all the flex lines from the existing flex items.
     *
     * @see #calculateFlexLines(FlexLinesResult, int, int, int, int, int, List)
     */
    void calculateHorizontalFlexLines(FlexLinesResult result, int widthMeasureSpec,
            int heightMeasureSpec) {
        calculateFlexLines(result, widthMeasureSpec, heightMeasureSpec, Integer.MAX_VALUE,
                0, NO_POSITION, null);
    }

    /**
     * Calculate how many flex lines are needed in the flex container.
     * Stop calculating it if the calculated amount along the cross size reaches the argument
     * as the needsCalcAmount.
     *
     * @param result            an instance of {@link FlexLinesResult} that is going to contain a
     *                          list of flex lines and the child state used by
     *                          {@link View#setMeasuredDimension(int, int)}.
     * @param widthMeasureSpec  the width measure spec imposed by the flex container
     * @param heightMeasureSpec the height measure spec imposed by the flex container
     * @param needsCalcAmount   the amount of pixels where flex line calculation should be stopped
     *                          this is needed to avoid the expensive calculation if the
     *                          calculation is needed only the small part of the entire flex
     *                          container. (E.g. If the flex container is the
     *                          {@link FlexboxLayoutManager}, the calculation only needs the
     *                          visible area, imposing the entire calculation may cause bad
     *                          performance
     * @param fromIndex         the index of the child from which the calculation starts
     * @param existingLines     If not null, calculated flex lines will be added to this instance
     */
    void calculateHorizontalFlexLines(FlexLinesResult result, int widthMeasureSpec,
            int heightMeasureSpec, int needsCalcAmount, int fromIndex,
            @Nullable List<FlexLine> existingLines) {
        calculateFlexLines(result, widthMeasureSpec, heightMeasureSpec, needsCalcAmount,
                fromIndex, NO_POSITION, existingLines);
    }

    /**
     * Calculate how many flex lines are needed in the flex container.
     * This method calculates the amount of pixels as the {@code needsCalcAmount} in addition to
     * the
     * flex lines which includes the view who has the index as the {@code toIndex} argument.
     * (First calculate to the toIndex, then calculate the amount of pixels as needsCalcAmount)
     *
     * @param result            an instance of {@link FlexLinesResult} that is going to contain a
     *                          list of flex lines and the child state used by
     *                          {@link View#setMeasuredDimension(int, int)}.
     * @param widthMeasureSpec  the width measure spec imposed by the flex container
     * @param heightMeasureSpec the height measure spec imposed by the flex container
     * @param needsCalcAmount   the amount of pixels where flex line calculation should be stopped
     *                          this is needed to avoid the expensive calculation if the
     *                          calculation is needed only the small part of the entire flex
     *                          container. (E.g. If the flex container is the
     *                          {@link FlexboxLayoutManager}, the calculation only needs the
     *                          visible area, imposing the entire calculation may cause bad
     *                          performance
     * @param toIndex           the index of the child to which the calculation ends (until the
     *                          flex line which include the which who has that index). If this
     *                          and needsCalcAmount are both set, first flex lines are calculated
     *                          to the index, calculate the amount of pixels as the needsCalcAmount
     *                          argument in addition to that
     */
    void calculateHorizontalFlexLinesToIndex(FlexLinesResult result, int widthMeasureSpec,
            int heightMeasureSpec, int needsCalcAmount, int toIndex, List<FlexLine> existingLines) {
        calculateFlexLines(result, widthMeasureSpec, heightMeasureSpec, needsCalcAmount,
                0, toIndex, existingLines);
    }

    /**
     * Calculate how many flex lines are needed in the flex container.
     * This method should calculate all the flex lines from the existing flex items.
     *
     * @param result            an instance of {@link FlexLinesResult} that is going to contain a
     *                          list of flex lines and the child state used by
     *                          {@link View#setMeasuredDimension(int, int)}.
     * @param widthMeasureSpec  the width measure spec imposed by the flex container
     * @param heightMeasureSpec the height measure spec imposed by the flex container
     * @see #calculateFlexLines(FlexLinesResult, int, int, int, int, int, List)
     */
    void calculateVerticalFlexLines(FlexLinesResult result, int widthMeasureSpec, int heightMeasureSpec) {
        calculateFlexLines(result, heightMeasureSpec, widthMeasureSpec, Integer.MAX_VALUE,
                0, NO_POSITION, null);
    }

    /**
     * Calculate how many flex lines are needed in the flex container.
     * Stop calculating it if the calculated amount along the cross size reaches the argument
     * as the needsCalcAmount.
     *
     * @param result            an instance of {@link FlexLinesResult} that is going to contain a
     *                          list of flex lines and the child state used by
     *                          {@link View#setMeasuredDimension(int, int)}.
     * @param widthMeasureSpec  the width measure spec imposed by the flex container
     * @param heightMeasureSpec the height measure spec imposed by the flex container
     * @param needsCalcAmount   the amount of pixels where flex line calculation should be stopped
     *                          this is needed to avoid the expensive calculation if the
     *                          calculation is needed only the small part of the entire flex
     *                          container. (E.g. If the flex container is the
     *                          {@link FlexboxLayoutManager}, the calculation only needs the
     *                          visible area, imposing the entire calculation may cause bad
     *                          performance
     * @param fromIndex         the index of the child from which the calculation starts
     * @param existingLines     If not null, calculated flex lines will be added to this instance
     */
    void calculateVerticalFlexLines(FlexLinesResult result, int widthMeasureSpec,
            int heightMeasureSpec, int needsCalcAmount, int fromIndex,
            @Nullable List<FlexLine> existingLines) {
        calculateFlexLines(result, heightMeasureSpec, widthMeasureSpec, needsCalcAmount,
                fromIndex, NO_POSITION, existingLines);
    }

    /**
     * Calculate how many flex lines are needed in the flex container.
     * This method calculates the amount of pixels as the {@code needsCalcAmount} in addition to
     * the
     * flex lines which includes the view who has the index as the {@code toIndex} argument.
     * (First calculate to the toIndex, then calculate the amount of pixels as needsCalcAmount)
     *
     * @param result            an instance of {@link FlexLinesResult} that is going to contain a
     *                          list of flex lines and the child state used by
     *                          {@link View#setMeasuredDimension(int, int)}.
     * @param widthMeasureSpec  the width measure spec imposed by the flex container
     * @param heightMeasureSpec the height measure spec imposed by the flex container
     * @param needsCalcAmount   the amount of pixels where flex line calculation should be stopped
     *                          this is needed to avoid the expensive calculation if the
     *                          calculation is needed only the small part of the entire flex
     *                          container. (E.g. If the flex container is the
     *                          {@link FlexboxLayoutManager}, the calculation only needs the
     *                          visible area, imposing the entire calculation may cause bad
     *                          performance
     * @param toIndex           the index of the child to which the calculation ends (until the
     *                          flex line which include the which who has that index). If this
     *                          and needsCalcAmount are both set, first flex lines are calculated
     *                          to the index, calculate the amount of pixels as the needsCalcAmount
     *                          argument in addition to that
     */
    void calculateVerticalFlexLinesToIndex(FlexLinesResult result, int widthMeasureSpec,
            int heightMeasureSpec, int needsCalcAmount, int toIndex, List<FlexLine> existingLines) {
        calculateFlexLines(result, heightMeasureSpec, widthMeasureSpec, needsCalcAmount,
                0, toIndex, existingLines);
    }


    /**
     * Calculates how many flex lines are needed in the flex container layout by measuring each
     * child.
     * Expanding or shrinking the flex items depending on the flex grow and flex shrink
     * attributes are done in a later procedure, so the views' measured width and measured
     * height may be changed in a later process.
     *
     * @param result           an instance of {@link FlexLinesResult} that is going to contain a
     *                         list of flex lines and the child state used by
     *                         {@link View#setMeasuredDimension(int, int)}.
     * @param mainMeasureSpec  the main axis measure spec imposed by the flex container,
     *                         width for horizontal direction, height otherwise
     * @param crossMeasureSpec the cross axis measure spec imposed by the flex container,
     *                         height for horizontal direction, width otherwise
     * @param needsCalcAmount  the amount of pixels where flex line calculation should be stopped
     *                         this is needed to avoid the expensive calculation if the
     *                         calculation is needed only the small part of the entire flex
     *                         container. (E.g. If the flex container is the
     *                         {@link FlexboxLayoutManager}, the calculation only needs the
     *                         visible area, imposing the entire calculation may cause bad
     *                         performance
     * @param fromIndex        the index of the child from which the calculation starts
     * @param toIndex          the index of the child to which the calculation ends (until the
     *                         flex line which include the which who has that index). If this
     *                         and needsCalcAmount are both set, first flex lines are calculated
     *                         to the index, calculate the amount of pixels as the needsCalcAmount
     *                         argument in addition to that
     * @param existingLines    If not null, calculated flex lines will be added to this instance
     */
    void calculateFlexLines(FlexLinesResult result, int mainMeasureSpec,
            int crossMeasureSpec, int needsCalcAmount, int fromIndex, int toIndex,
            @Nullable List<FlexLine> existingLines) {

        boolean isMainHorizontal = mFlexContainer.isMainAxisDirectionHorizontal();

        int mainMode = View.MeasureSpec.getMode(mainMeasureSpec);
        int mainSize = View.MeasureSpec.getSize(mainMeasureSpec);

        int childState = 0;

        List<FlexLine> flexLines;
        if (existingLines == null) {
            flexLines = new ArrayList<>();
        } else {
            flexLines = existingLines;
        }

        result.mFlexLines = flexLines;

        boolean reachedToIndex = toIndex == NO_POSITION;

        int mainPaddingStart = getPaddingStartMain(isMainHorizontal);
        int mainPaddingEnd = getPaddingEndMain(isMainHorizontal);
        int crossPaddingStart = getPaddingStartCross(isMainHorizontal);
        int crossPaddingEnd = getPaddingEndCross(isMainHorizontal);

        int largestSizeInCross = Integer.MIN_VALUE;

        // The amount of cross size calculated in this method call.
        int sumCrossSize = 0;

        // The index of the view in the flex line.
        int indexInFlexLine = 0;

        FlexLine flexLine = new FlexLine();
        flexLine.mFirstIndex = fromIndex;
        flexLine.mMainSize = mainPaddingStart + mainPaddingEnd;

        int childCount = mFlexContainer.getFlexItemCount();
        for (int i = fromIndex; i < childCount; i++) {
            View child = mFlexContainer.getReorderedFlexItemAt(i);

            if (child == null) {
                if (isLastFlexItem(i, childCount, flexLine)) {
                    addFlexLine(flexLines, flexLine, i, sumCrossSize);
                }
                continue;
            } else if (child.getVisibility() == View.GONE) {
                flexLine.mGoneItemCount++;
                flexLine.mItemCount++;
                if (isLastFlexItem(i, childCount, flexLine)) {
                    addFlexLine(flexLines, flexLine, i, sumCrossSize);
                }
                continue;
            } else if (child instanceof CompoundButton) {
                evaluateMinimumSizeForCompoundButton((CompoundButton) child);
            }

            FlexItem flexItem = (FlexItem) child.getLayoutParams();

            if (flexItem.getAlignSelf() == AlignItems.STRETCH) {
                flexLine.mIndicesAlignSelfStretch.add(i);
            }

            int childMainSize = getFlexItemSizeMain(flexItem, isMainHorizontal);

            if (flexItem.getFlexBasisPercent() != FLEX_BASIS_PERCENT_DEFAULT
                    && mainMode == View.MeasureSpec.EXACTLY) {
                childMainSize = Math.round(mainSize * flexItem.getFlexBasisPercent());
                // Use the dimension from the layout if the mainMode is not
                // MeasureSpec.EXACTLY even if any fraction value is set to
                // layout_flexBasisPercent.
            }

            int childMainMeasureSpec;
            int childCrossMeasureSpec;
            if (isMainHorizontal) {
                childMainMeasureSpec = mFlexContainer.getChildWidthMeasureSpec(mainMeasureSpec,
                        mainPaddingStart + mainPaddingEnd +
                                getFlexItemMarginStartMain(flexItem, true) +
                                getFlexItemMarginEndMain(flexItem, true),
                        childMainSize);
                childCrossMeasureSpec = mFlexContainer.getChildHeightMeasureSpec(crossMeasureSpec,
                        crossPaddingStart + crossPaddingEnd +
                                getFlexItemMarginStartCross(flexItem, true) +
                                getFlexItemMarginEndCross(flexItem, true)
                                + sumCrossSize,
                        getFlexItemSizeCross(flexItem, true));
                child.measure(childMainMeasureSpec, childCrossMeasureSpec);
                updateMeasureCache(i, childMainMeasureSpec, childCrossMeasureSpec, child);
            } else {
                childCrossMeasureSpec = mFlexContainer.getChildWidthMeasureSpec(crossMeasureSpec,
                        crossPaddingStart + crossPaddingEnd +
                                getFlexItemMarginStartCross(flexItem, false) +
                                getFlexItemMarginEndCross(flexItem, false) + sumCrossSize,
                        getFlexItemSizeCross(flexItem, false));
                childMainMeasureSpec = mFlexContainer.getChildHeightMeasureSpec(mainMeasureSpec,
                        mainPaddingStart + mainPaddingEnd +
                                getFlexItemMarginStartMain(flexItem, false) +
                                getFlexItemMarginEndMain(flexItem, false),
                        childMainSize);
                child.measure(childCrossMeasureSpec, childMainMeasureSpec);
                updateMeasureCache(i, childCrossMeasureSpec, childMainMeasureSpec, child);
            }
            mFlexContainer.updateViewCache(i, child);

            // Check the size constraint after the first measurement for the child
            // To prevent the child's width/height violate the size constraints imposed by the
            // {@link FlexItem#getMinWidth()}, {@link FlexItem#getMinHeight()},
            // {@link FlexItem#getMaxWidth()} and {@link FlexItem#getMaxHeight()} attributes.
            // E.g. When the child's layout_width is wrap_content the measured width may be
            // less than the min width after the first measurement.
            checkSizeConstraints(child, i);

            childState = View.combineMeasuredStates(
                    childState, child.getMeasuredState());

            if (isWrapRequired(child, mainMode, mainSize, flexLine.mMainSize,
                    getViewMeasuredSizeMain(child, isMainHorizontal)
                            + getFlexItemMarginStartMain(flexItem, isMainHorizontal) +
                            getFlexItemMarginEndMain(flexItem, isMainHorizontal),
                    flexItem, i, indexInFlexLine, flexLines.size())) {
                if (flexLine.getItemCountNotGone() > 0) {
                    addFlexLine(flexLines, flexLine, i > 0 ? i - 1 : 0, sumCrossSize);
                    sumCrossSize += flexLine.mCrossSize;
                }

                if (isMainHorizontal) {
                    if (flexItem.getHeight() == ViewGroup.LayoutParams.MATCH_PARENT) {
                        // This case takes care of the corner case where the cross size of the
                        // child is affected by the just added flex line.
                        // E.g. when the child's layout_height is set to match_parent, the height
                        // of that child needs to be determined taking the total cross size used
                        // so far into account. In that case, the height of the child needs to be
                        // measured again note that we don't need to judge if the wrapping occurs
                        // because it doesn't change the size along the main axis.
                        childCrossMeasureSpec = mFlexContainer.getChildHeightMeasureSpec(
                                crossMeasureSpec,
                                mFlexContainer.getPaddingTop() + mFlexContainer.getPaddingBottom()
                                        + flexItem.getMarginTop()
                                        + flexItem.getMarginBottom() + sumCrossSize,
                                flexItem.getHeight());
                        child.measure(childMainMeasureSpec, childCrossMeasureSpec);
                        checkSizeConstraints(child, i);
                    }
                } else {
                    if (flexItem.getWidth() == ViewGroup.LayoutParams.MATCH_PARENT) {
                        // This case takes care of the corner case where the cross size of the
                        // child is affected by the just added flex line.
                        // E.g. when the child's layout_width is set to match_parent, the width
                        // of that child needs to be determined taking the total cross size used
                        // so far into account. In that case, the width of the child needs to be
                        // measured again note that we don't need to judge if the wrapping occurs
                        // because it doesn't change the size along the main axis.
                        childCrossMeasureSpec = mFlexContainer.getChildWidthMeasureSpec(
                                crossMeasureSpec,
                                mFlexContainer.getPaddingLeft() + mFlexContainer.getPaddingRight()
                                        + flexItem.getMarginLeft()
                                        + flexItem.getMarginRight() + sumCrossSize,
                                flexItem.getWidth());
                        child.measure(childCrossMeasureSpec, childMainMeasureSpec);
                        checkSizeConstraints(child, i);
                    }
                }

                flexLine = new FlexLine();
                flexLine.mItemCount = 1;
                flexLine.mMainSize = mainPaddingStart + mainPaddingEnd;
                flexLine.mFirstIndex = i;
                indexInFlexLine = 0;
                largestSizeInCross = Integer.MIN_VALUE;
            } else {
                flexLine.mItemCount++;
                indexInFlexLine++;
            }
            flexLine.mAnyItemsHaveFlexGrow |= flexItem.getFlexGrow() != FLEX_GROW_DEFAULT;
            flexLine.mAnyItemsHaveFlexShrink |= flexItem.getFlexShrink() != FLEX_SHRINK_NOT_SET;

            if (mIndexToFlexLine != null) {
                mIndexToFlexLine[i] = flexLines.size();
            }
            flexLine.mMainSize += getViewMeasuredSizeMain(child, isMainHorizontal)
                    + getFlexItemMarginStartMain(flexItem, isMainHorizontal) +
                    getFlexItemMarginEndMain(flexItem, isMainHorizontal);
            flexLine.mTotalFlexGrow += flexItem.getFlexGrow();
            flexLine.mTotalFlexShrink += flexItem.getFlexShrink();

            mFlexContainer.onNewFlexItemAdded(child, i, indexInFlexLine, flexLine);

            largestSizeInCross = Math.max(largestSizeInCross,
                    getViewMeasuredSizeCross(child, isMainHorizontal) +
                            getFlexItemMarginStartCross(flexItem, isMainHorizontal) +
                            getFlexItemMarginEndCross(flexItem, isMainHorizontal) +
                            mFlexContainer.getDecorationLengthCrossAxis(child));
            // Temporarily set the cross axis length as the largest child in the flexLine
            // Expand along the cross axis depending on the mAlignContent property if needed
            // later
            flexLine.mCrossSize = Math.max(flexLine.mCrossSize, largestSizeInCross);

            if (isMainHorizontal) {
                if (mFlexContainer.getFlexWrap() != FlexWrap.WRAP_REVERSE) {
                    flexLine.mMaxBaseline = Math.max(flexLine.mMaxBaseline,
                            child.getBaseline() + flexItem.getMarginTop());
                } else {
                    // if the flex wrap property is WRAP_REVERSE, calculate the
                    // baseline as the distance from the cross end and the baseline
                    // since the cross size calculation is based on the distance from the cross end
                    flexLine.mMaxBaseline = Math.max(flexLine.mMaxBaseline,
                            child.getMeasuredHeight() - child.getBaseline()
                                    + flexItem.getMarginBottom());
                }
            }

            if (isLastFlexItem(i, childCount, flexLine)) {
                addFlexLine(flexLines, flexLine, i, sumCrossSize);
                sumCrossSize += flexLine.mCrossSize;
            }

            if (toIndex != NO_POSITION
                    && flexLines.size() > 0
                    && flexLines.get(flexLines.size() - 1).mLastIndex >= toIndex
                    && i >= toIndex
                    && !reachedToIndex) {
                // Calculated to include a flex line which includes the flex item having the
                // toIndex.
                // Let the sumCrossSize start from the negative value of the last flex line's
                // cross size because otherwise flex lines aren't calculated enough to fill the
                // visible area.
                sumCrossSize = -flexLine.getCrossSize();
                reachedToIndex = true;
            }
            if (sumCrossSize > needsCalcAmount && reachedToIndex) {
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
    }

    /**
     * Compound buttons (ex. {{@link android.widget.CheckBox}}, {@link android.widget.ToggleButton})
     * have a button drawable with minimum height and width specified for them.
     * To align the behavior with CSS Flexbox we want to respect these minimum measurement to avoid
     * these drawables from being cut off during calculation. When the compound button has a minimum
     * width or height already specified we will not make any change since we assume those were
     * voluntarily set by the user.
     *
     * @param compoundButton the compound button that need to be evaluated
     */
    private void evaluateMinimumSizeForCompoundButton(CompoundButton compoundButton) {
        FlexItem flexItem = (FlexItem) compoundButton.getLayoutParams();
        int minWidth = flexItem.getMinWidth();
        int minHeight = flexItem.getMinHeight();

        Drawable drawable = CompoundButtonCompat.getButtonDrawable(compoundButton);
        int drawableMinWidth = drawable == null ? 0 : drawable.getMinimumWidth();
        int drawableMinHeight = drawable == null ? 0 : drawable.getMinimumHeight();
        flexItem.setMinWidth(minWidth == NOT_SET ? drawableMinWidth : minWidth);
        flexItem.setMinHeight(minHeight == NOT_SET ? drawableMinHeight : minHeight);
    }

    /**
     * Returns the container's start padding in the main axis. Either start or top.
     *
     * @param isMainHorizontal is the main axis horizontal
     * @return the start padding in the main axis
     */
    private int getPaddingStartMain(boolean isMainHorizontal) {
        if (isMainHorizontal) {
            return mFlexContainer.getPaddingStart();
        }

        return mFlexContainer.getPaddingTop();
    }

    /**
     * Returns the container's end padding in the main axis. Either end or bottom.
     *
     * @param isMainHorizontal is the main axis horizontal
     * @return the end padding in the main axis
     */
    private int getPaddingEndMain(boolean isMainHorizontal) {
        if (isMainHorizontal) {
            return mFlexContainer.getPaddingEnd();
        }

        return mFlexContainer.getPaddingBottom();
    }

    /**
     * Returns the container's start padding in the cross axis. Either start or top.
     *
     * @param isMainHorizontal is the main axis horizontal.
     * @return the start padding in the cross axis
     */
    private int getPaddingStartCross(boolean isMainHorizontal) {
        if (isMainHorizontal) {
            return mFlexContainer.getPaddingTop();
        }

        return mFlexContainer.getPaddingStart();
    }

    /**
     * Returns the container's end padding in the cross axis. Either end or bottom.
     *
     * @param isMainHorizontal is the main axis horizontal
     * @return the end padding in the cross axis
     */
    private int getPaddingEndCross(boolean isMainHorizontal) {
        if (isMainHorizontal) {
            return mFlexContainer.getPaddingBottom();
        }

        return mFlexContainer.getPaddingEnd();
    }

    /**
     * Returns the view's measured size in the main axis. Either width or height.
     *
     * @param view             the view
     * @param isMainHorizontal is the main axis horizontal
     * @return the view's measured size in the main axis
     */
    private int getViewMeasuredSizeMain(View view, boolean isMainHorizontal) {
        if (isMainHorizontal) {
            return view.getMeasuredWidth();
        }

        return view.getMeasuredHeight();
    }

    /**
     * Returns the view's measured size in the cross axis. Either width or height.
     *
     * @param view             the view
     * @param isMainHorizontal is the main axis horizontal
     * @return the view's measured size in the cross axis
     */
    private int getViewMeasuredSizeCross(View view, boolean isMainHorizontal) {
        if (isMainHorizontal) {
            return view.getMeasuredHeight();
        }

        return view.getMeasuredWidth();
    }

    /**
     * Returns the flexItem's size in the main axis. Either width or height.
     *
     * @param flexItem         the flexItem
     * @param isMainHorizontal is the main axis horizontal
     * @return the flexItem's size in the main axis
     */
    private int getFlexItemSizeMain(FlexItem flexItem, boolean isMainHorizontal) {
        if (isMainHorizontal) {
            return flexItem.getWidth();
        }

        return flexItem.getHeight();
    }

    /**
     * Returns the flexItem's size in the cross axis. Either width or height.
     *
     * @param flexItem         the flexItem
     * @param isMainHorizontal is the main axis horizontal
     * @return the flexItem's size in the cross axis
     */
    private int getFlexItemSizeCross(FlexItem flexItem, boolean isMainHorizontal) {
        if (isMainHorizontal) {
            return flexItem.getHeight();
        }

        return flexItem.getWidth();
    }

    /**
     * Returns the flexItem's start margin in the main axis. Either start or top.
     * For the backward compatibility for API level < 17, the horizontal margin is returned using
     * {@link FlexItem#getMarginLeft} (ViewGroup.MarginLayoutParams#getMarginStart isn't available
     * in API level < 17). Thus this method needs to be used with {@link #getFlexItemMarginEndMain}
     * not to misuse the margin in RTL.
     *
     *
     * @param flexItem         the flexItem
     * @param isMainHorizontal is the main axis horizontal
     * @return the flexItem's start margin in the main axis
     */
    private int getFlexItemMarginStartMain(FlexItem flexItem, boolean isMainHorizontal) {
        if (isMainHorizontal) {
            return flexItem.getMarginLeft();
        }

        return flexItem.getMarginTop();
    }

    /**
     * Returns the flexItem's end margin in the main axis. Either end or bottom.
     * For the backward compatibility for API level < 17, the horizontal margin is returned using
     * {@link FlexItem#getMarginRight} (ViewGroup.MarginLayoutParams#getMarginEnd isn't available
     * in API level < 17). Thus this method needs to be used with
     * {@link #getFlexItemMarginStartMain} not to misuse the margin in RTL.
     *
     * @param flexItem         the flexItem
     * @param isMainHorizontal is the main axis horizontal
     * @return the flexItem's end margin in the main axis
     */
    private int getFlexItemMarginEndMain(FlexItem flexItem, boolean isMainHorizontal) {
        if (isMainHorizontal) {
            return flexItem.getMarginRight();
        }

        return flexItem.getMarginBottom();
    }

    /**
     * Returns the flexItem's start margin in the cross axis. Either start or top.
     * For the backward compatibility for API level < 17, the horizontal margin is returned using
     * {@link FlexItem#getMarginLeft} (ViewGroup.MarginLayoutParams#getMarginStart isn't available
     * in API level < 17). Thus this method needs to be used with
     * {@link #getFlexItemMarginEndCross} to not to misuse the margin in RTL.
     *
     * @param flexItem         the flexItem
     * @param isMainHorizontal is the main axis horizontal
     * @return the flexItem's start margin in the cross axis
     */
    private int getFlexItemMarginStartCross(FlexItem flexItem, boolean isMainHorizontal) {
        if (isMainHorizontal) {
            return flexItem.getMarginTop();
        }

        return flexItem.getMarginLeft();
    }

    /**
     * Returns the flexItem's end margin in the cross axis. Either end or bottom.
     * For the backward compatibility for API level < 17, the horizontal margin is returned using
     * {@link FlexItem#getMarginRight} (ViewGroup.MarginLayoutParams#getMarginEnd isn't available
     * in API level < 17). Thus this method needs to be used with
     * {@link #getFlexItemMarginStartCross} to not to misuse the margin in RTL.
     *
     * @param flexItem         the flexItem
     * @param isMainHorizontal is the main axis horizontal
     * @return the flexItem's end margin in the cross axis
     */
    private int getFlexItemMarginEndCross(FlexItem flexItem, boolean isMainHorizontal) {
        if (isMainHorizontal) {
            return flexItem.getMarginBottom();
        }

        return flexItem.getMarginRight();
    }

    /**
     * Determine if a wrap is required (add a new flex line).
     *
     * @param view          the view being judged if the wrap required
     * @param mode          the width or height mode along the main axis direction
     * @param maxSize       the max size along the main axis direction
     * @param currentLength the accumulated current length
     * @param childLength   the length of a child view which is to be collected to the flex line
     * @param flexItem      the LayoutParams for the view being determined whether a new flex line
     *                      is needed
     * @param index         the index of the view being added within the entire flex container
     * @param indexInFlexLine the index of the view being added within the current flex line
     * @param flexLinesSize the number of the existing flexlines size
     * @return {@code true} if a wrap is required, {@code false} otherwise
     * @see FlexContainer#getFlexWrap()
     * @see FlexContainer#setFlexWrap(int)
     */
    private boolean isWrapRequired(View view, int mode, int maxSize, int currentLength,
            int childLength, FlexItem flexItem, int index, int indexInFlexLine, int flexLinesSize) {
        if (mFlexContainer.getFlexWrap() == FlexWrap.NOWRAP) {
            return false;
        }
        if (flexItem.isWrapBefore()) {
            return true;
        }
        if (mode == View.MeasureSpec.UNSPECIFIED) {
            return false;
        }
        int maxLine = mFlexContainer.getMaxLine();
        // Judge the condition by adding 1 to the current flexLinesSize because the flex line
        // being computed isn't added to the flexLinesSize.
        if (maxLine != NOT_SET && maxLine <= flexLinesSize + 1) {
            return false;
        }
        int decorationLength =
                mFlexContainer.getDecorationLengthMainAxis(view, index, indexInFlexLine);
        if (decorationLength > 0) {
            childLength += decorationLength;
        }
        return maxSize < currentLength + childLength;
    }

    private boolean isLastFlexItem(int childIndex, int childCount,
            FlexLine flexLine) {
        return childIndex == childCount - 1 && flexLine.getItemCountNotGone() != 0;
    }

    private void addFlexLine(List<FlexLine> flexLines, FlexLine flexLine, int viewIndex,
            int usedCrossSizeSoFar) {
        flexLine.mSumCrossSizeBefore = usedCrossSizeSoFar;
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

        if (childWidth < flexItem.getMinWidth()) {
            needsMeasure = true;
            childWidth = flexItem.getMinWidth();
        } else if (childWidth > flexItem.getMaxWidth()) {
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
            mFlexContainer.updateViewCache(index, view);
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
                int largestMainSize = mFlexContainer.getLargestMainSize();
                if (widthMode == View.MeasureSpec.EXACTLY) {
                    mainSize = widthSize;
                } else {
                    mainSize = Math.min(largestMainSize, widthSize);
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
            if (flexLine.mMainSize < mainSize && flexLine.mAnyItemsHaveFlexGrow) {
                expandFlexItems(widthMeasureSpec, heightMeasureSpec, flexLine,
                        mainSize, paddingAlongMainAxis, false);
            } else if (flexLine.mMainSize > mainSize && flexLine.mAnyItemsHaveFlexShrink) {
                shrinkFlexItems(widthMeasureSpec, heightMeasureSpec, flexLine,
                        mainSize, paddingAlongMainAxis, false);
            }
        }
    }

    private void ensureChildrenFrozen(int size) {
        if (mChildrenFrozen == null) {
            mChildrenFrozen = new boolean[Math.max(size, INITIAL_CAPACITY)];
        } else if (mChildrenFrozen.length < size) {
            int newCapacity = mChildrenFrozen.length * 2;
            mChildrenFrozen = new boolean[Math.max(newCapacity, size)];
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
     * @param calledRecursively    true if this method is called recursively, false otherwise
     * @see FlexContainer#getFlexDirection()
     * @see FlexContainer#setFlexDirection(int)
     * @see FlexItem#getFlexGrow()
     */
    private void expandFlexItems(int widthMeasureSpec, int heightMeasureSpec, FlexLine flexLine,
            int maxMainSize, int paddingAlongMainAxis, boolean calledRecursively) {
        if (flexLine.mTotalFlexGrow <= 0 || maxMainSize < flexLine.mMainSize) {
            return;
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
            int index = flexLine.mFirstIndex + i;
            View child = mFlexContainer.getReorderedFlexItemAt(index);
            if (child == null || child.getVisibility() == View.GONE) {
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
                    childMeasuredWidth = extractLowerInt(mMeasuredSizeCache[index]);
                }
                int childMeasuredHeight = child.getMeasuredHeight();
                if (mMeasuredSizeCache != null) {
                    // Extract the measured height from the cache
                    childMeasuredHeight = extractHigherInt(mMeasuredSizeCache[index]);
                }
                if (!mChildrenFrozen[index] && flexItem.getFlexGrow() > 0f) {
                    float rawCalculatedWidth = childMeasuredWidth
                            + unitSpace * flexItem.getFlexGrow();
                    if (i == flexLine.mItemCount - 1) {
                        rawCalculatedWidth += accumulatedRoundError;
                        accumulatedRoundError = 0;
                    }
                    int newWidth = Math.round(rawCalculatedWidth);
                    if (newWidth > flexItem.getMaxWidth()) {
                        // This means the child can't expand beyond the value of the mMaxWidth
                        // attribute.
                        // To adjust the flex line length to the size of maxMainSize, remaining
                        // positive free space needs to be re-distributed to other flex items
                        // (children views). In that case, invoke this method again with the same
                        // fromIndex.
                        needsReexpand = true;
                        newWidth = flexItem.getMaxWidth();
                        mChildrenFrozen[index] = true;
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
                            heightMeasureSpec, flexItem, flexLine.mSumCrossSizeBefore);
                    int childWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(newWidth,
                            View.MeasureSpec.EXACTLY);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                    childMeasuredWidth = child.getMeasuredWidth();
                    childMeasuredHeight = child.getMeasuredHeight();
                    updateMeasureCache(index, childWidthMeasureSpec, childHeightMeasureSpec,
                            child);
                    mFlexContainer.updateViewCache(index, child);
                }
                largestCrossSize = Math.max(largestCrossSize, childMeasuredHeight
                        + flexItem.getMarginTop() + flexItem.getMarginBottom()
                        + mFlexContainer.getDecorationLengthCrossAxis(child));
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
                            extractHigherInt(mMeasuredSizeCache[index]);
                }
                int childMeasuredWidth = child.getMeasuredWidth();
                if (mMeasuredSizeCache != null) {
                    // Extract the measured width from the cache
                    childMeasuredWidth =
                            extractLowerInt(mMeasuredSizeCache[index]);
                }
                if (!mChildrenFrozen[index] && flexItem.getFlexGrow() > 0f) {
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
                        mChildrenFrozen[index] = true;
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
                            flexItem, flexLine.mSumCrossSizeBefore);
                    int childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(newHeight,
                            View.MeasureSpec.EXACTLY);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                    childMeasuredWidth = child.getMeasuredWidth();
                    childMeasuredHeight = child.getMeasuredHeight();
                    updateMeasureCache(index, childWidthMeasureSpec, childHeightMeasureSpec,
                            child);
                    mFlexContainer.updateViewCache(index, child);
                }
                largestCrossSize = Math.max(largestCrossSize, childMeasuredWidth
                        + flexItem.getMarginLeft() + flexItem.getMarginRight()
                        + mFlexContainer.getDecorationLengthCrossAxis(child));
                flexLine.mMainSize += childMeasuredHeight + flexItem.getMarginTop()
                        + flexItem.getMarginBottom();
            }
            flexLine.mCrossSize = Math.max(flexLine.mCrossSize, largestCrossSize);
        }

        if (needsReexpand && sizeBeforeExpand != flexLine.mMainSize) {
            // Re-invoke the method with the same flex line to distribute the positive free space
            // that wasn't fully distributed (because of maximum length constraint)
            expandFlexItems(widthMeasureSpec, heightMeasureSpec, flexLine, maxMainSize,
                    paddingAlongMainAxis, true);
        }
    }

    /**
     * Shrink the flex items along the main axis based on the individual mFlexShrink attribute.
     *
     * @param widthMeasureSpec     the horizontal space requirements as imposed by the parent
     * @param heightMeasureSpec    the vertical space requirements as imposed by the parent
     * @param flexLine             the flex line to which flex items belong
     * @param maxMainSize          the maximum main size. Shrank main size will be this size
     * @param paddingAlongMainAxis the padding value along the main axis
     * @param calledRecursively    true if this method is called recursively, false otherwise
     * @see FlexContainer#getFlexDirection()
     * @see FlexContainer#setFlexDirection(int)
     * @see FlexItem#getFlexShrink()
     */
    private void shrinkFlexItems(int widthMeasureSpec, int heightMeasureSpec, FlexLine flexLine,
            int maxMainSize, int paddingAlongMainAxis, boolean calledRecursively) {
        int sizeBeforeShrink = flexLine.mMainSize;
        if (flexLine.mTotalFlexShrink <= 0 || maxMainSize > flexLine.mMainSize) {
            return;
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
            int index = flexLine.mFirstIndex + i;
            View child = mFlexContainer.getReorderedFlexItemAt(index);
            if (child == null || child.getVisibility() == View.GONE) {
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
                    childMeasuredWidth = extractLowerInt(mMeasuredSizeCache[index]);
                }
                int childMeasuredHeight = child.getMeasuredHeight();
                if (mMeasuredSizeCache != null) {
                    // Extract the measured height from the cache
                    childMeasuredHeight = extractHigherInt(mMeasuredSizeCache[index]);
                }
                if (!mChildrenFrozen[index] && flexItem.getFlexShrink() > 0f) {
                    float rawCalculatedWidth = childMeasuredWidth
                            - unitShrink * flexItem.getFlexShrink();
                    if (i == flexLine.mItemCount - 1) {
                        rawCalculatedWidth += accumulatedRoundError;
                        accumulatedRoundError = 0;
                    }
                    int newWidth = Math.round(rawCalculatedWidth);
                    if (newWidth < flexItem.getMinWidth()) {
                        // This means the child doesn't have enough space to distribute the negative
                        // free space. To adjust the flex line length down to the maxMainSize,
                        // remaining
                        // negative free space needs to be re-distributed to other flex items
                        // (children views). In that case, invoke this method again with the same
                        // fromIndex.
                        needsReshrink = true;
                        newWidth = flexItem.getMinWidth();
                        mChildrenFrozen[index] = true;
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
                            heightMeasureSpec, flexItem, flexLine.mSumCrossSizeBefore);
                    int childWidthMeasureSpec =
                            View.MeasureSpec.makeMeasureSpec(newWidth, View.MeasureSpec.EXACTLY);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

                    childMeasuredWidth = child.getMeasuredWidth();
                    childMeasuredHeight = child.getMeasuredHeight();
                    updateMeasureCache(index, childWidthMeasureSpec, childHeightMeasureSpec,
                            child);
                    mFlexContainer.updateViewCache(index, child);
                }
                largestCrossSize = Math.max(largestCrossSize, childMeasuredHeight +
                        flexItem.getMarginTop() + flexItem.getMarginBottom() +
                        mFlexContainer.getDecorationLengthCrossAxis(child));
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
                            extractHigherInt(mMeasuredSizeCache[index]);
                }
                int childMeasuredWidth = child.getMeasuredWidth();
                if (mMeasuredSizeCache != null) {
                    // Extract the measured width from the cache
                    childMeasuredWidth =
                            extractLowerInt(mMeasuredSizeCache[index]);
                }
                if (!mChildrenFrozen[index] && flexItem.getFlexShrink() > 0f) {
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
                        mChildrenFrozen[index] = true;
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
                            flexItem, flexLine.mSumCrossSizeBefore);
                    int childHeightMeasureSpec =
                            View.MeasureSpec.makeMeasureSpec(newHeight, View.MeasureSpec.EXACTLY);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

                    childMeasuredWidth = child.getMeasuredWidth();
                    childMeasuredHeight = child.getMeasuredHeight();
                    updateMeasureCache(index, childWidthMeasureSpec, childHeightMeasureSpec,
                            child);
                    mFlexContainer.updateViewCache(index, child);
                }
                largestCrossSize = Math.max(largestCrossSize, childMeasuredWidth +
                        flexItem.getMarginLeft() + flexItem.getMarginRight() +
                        mFlexContainer.getDecorationLengthCrossAxis(child));
                flexLine.mMainSize += childMeasuredHeight + flexItem.getMarginTop()
                        + flexItem.getMarginBottom();
            }
            flexLine.mCrossSize = Math.max(flexLine.mCrossSize, largestCrossSize);
        }

        if (needsReshrink && sizeBeforeShrink != flexLine.mMainSize) {
            // Re-invoke the method with the same fromIndex to distribute the negative free space
            // that wasn't fully distributed (because some views length were not enough)
            shrinkFlexItems(widthMeasureSpec, heightMeasureSpec, flexLine,
                    maxMainSize, paddingAlongMainAxis, true);
        }
    }

    private int getChildWidthMeasureSpecInternal(int widthMeasureSpec, FlexItem flexItem,
            int padding) {
        int childWidthMeasureSpec = mFlexContainer.getChildWidthMeasureSpec(widthMeasureSpec,
                mFlexContainer.getPaddingLeft() + mFlexContainer.getPaddingRight() +
                        flexItem.getMarginLeft() + flexItem.getMarginRight() + padding,
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

    private int getChildHeightMeasureSpecInternal(int heightMeasureSpec, FlexItem flexItem,
            int padding) {
        int childHeightMeasureSpec = mFlexContainer.getChildHeightMeasureSpec(heightMeasureSpec,
                mFlexContainer.getPaddingTop() + mFlexContainer.getPaddingBottom()
                        + flexItem.getMarginTop() + flexItem.getMarginBottom() + padding,
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
            } else if (flexLines.size() >= 2) {
                switch (mFlexContainer.getAlignContent()) {
                    case AlignContent.STRETCH: {
                        if (totalCrossSize >= size) {
                            break;
                        }
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
                        if (totalCrossSize >= size) {
                            // If the size of the content is larger than the flex container, the
                            // Flex lines should be aligned center like ALIGN_CONTENT_CENTER
                            mFlexContainer.setFlexLines(
                                    constructFlexLinesForAlignContentCenter(flexLines, size,
                                            totalCrossSize));
                            break;
                        }
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
                        if (totalCrossSize >= size) {
                            break;
                        }
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
                        mFlexContainer.setFlexLines(
                                constructFlexLinesForAlignContentCenter(flexLines, size,
                                        totalCrossSize));
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

    private List<FlexLine> constructFlexLinesForAlignContentCenter(List<FlexLine> flexLines,
            int size, int totalCrossSize) {
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
        return newFlexLines;
    }

    void stretchViews() {
        stretchViews(0);
    }

    /**
     * Expand the view if the {@link FlexContainer#getAlignItems()} attribute is set to {@link
     * AlignItems#STRETCH} or {@link FlexItem#getAlignSelf()} is set as
     * {@link AlignItems#STRETCH}.
     *
     * @param fromIndex the index from which value, stretch is calculated
     * @see FlexContainer#getFlexDirection()
     * @see FlexContainer#setFlexDirection(int)
     * @see FlexContainer#getAlignItems()
     * @see FlexContainer#setAlignItems(int)
     * @see FlexItem#getAlignSelf()
     */
    void stretchViews(int fromIndex) {
        if (fromIndex >= mFlexContainer.getFlexItemCount()) {
            return;
        }
        int flexDirection = mFlexContainer.getFlexDirection();
        if (mFlexContainer.getAlignItems() == AlignItems.STRETCH) {
            int flexLineIndex = 0;
            if (mIndexToFlexLine != null) {
                flexLineIndex = mIndexToFlexLine[fromIndex];
            }
            List<FlexLine> flexLines = mFlexContainer.getFlexLinesInternal();
            for (int i = flexLineIndex, size = flexLines.size(); i < size; i++) {
                FlexLine flexLine = flexLines.get(i);
                for (int j = 0, itemCount = flexLine.mItemCount; j < itemCount; j++) {
                    int viewIndex = flexLine.mFirstIndex + j;
                    if (j >= mFlexContainer.getFlexItemCount()) {
                        continue;
                    }
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
        int newHeight = crossSize - flexItem.getMarginTop() - flexItem.getMarginBottom() -
                mFlexContainer.getDecorationLengthCrossAxis(view);
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
        mFlexContainer.updateViewCache(index, view);
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
        int newWidth = crossSize - flexItem.getMarginLeft() - flexItem.getMarginRight()
                - mFlexContainer.getDecorationLengthCrossAxis(view);
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
        mFlexContainer.updateViewCache(index, view);
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
     *                 View's left position is shifted depending on the isLayoutRtl and alignItems
     *                 attributes
     * @param top      the top position of the View, which the View's margin is already taken
     *                 into account
     * @param right    the right position of the flex line where the View belongs to. The actual
     *                 View's right position is shifted depending on the isLayoutRtl and alignItems
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
            mMeasuredSizeCache = new long[Math.max(size, INITIAL_CAPACITY)];
        } else if (mMeasuredSizeCache.length < size) {
            int newCapacity = mMeasuredSizeCache.length * 2;
            newCapacity = Math.max(newCapacity, size);
            mMeasuredSizeCache = Arrays.copyOf(mMeasuredSizeCache, newCapacity);
        }
    }

    void ensureMeasureSpecCache(int size) {
        if (mMeasureSpecCache == null) {
            mMeasureSpecCache = new long[Math.max(size, INITIAL_CAPACITY)];
        } else if (mMeasureSpecCache.length < size) {
            int newCapacity = mMeasureSpecCache.length * 2;
            newCapacity = Math.max(newCapacity, size);
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
            mIndexToFlexLine = new int[Math.max(size, INITIAL_CAPACITY)];
        } else if (mIndexToFlexLine.length < size) {
            int newCapacity = mIndexToFlexLine.length * 2;
            newCapacity = Math.max(newCapacity, size);
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
        if (fromFlexLine == NO_POSITION) {
            fromFlexLine = 0;
        }

        // Deleting from the last to avoid unneeded copy it happens when deleting the middle of the
        // item in the ArrayList
        if (flexLines.size() > fromFlexLine) {
            flexLines.subList(fromFlexLine, flexLines.size()).clear();
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

        @NonNull
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

        void reset() {
            mFlexLines = null;
            mChildState = 0;
        }
    }
}
