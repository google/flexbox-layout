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
import android.support.annotation.IntDef;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.LinearLayoutManager.INVALID_OFFSET;
import static android.support.v7.widget.RecyclerView.NO_POSITION;

/**
 * LayoutManager for the {@link RecyclerView}. This class is intended to be used within a
 * {@link RecyclerView} and offers the same capabilities of measure/layout its children
 * as the {@link FlexboxLayout}.
 */
public class FlexboxLayoutManager extends RecyclerView.LayoutManager implements FlexContainer {

    private static final String TAG = "FlexboxLayoutManager";

    private static final boolean DEBUG = false;

    /**
     * The current value of the {@link FlexDirection}, the default value is {@link
     * FlexDirection#ROW}.
     *
     * @see {@link FlexContainer#getFlexDirection()}
     */
    private int mFlexDirection;

    /**
     * The current value of the {@link FlexWrap}, the default value is {@link FlexWrap#WRAP}.
     *
     * @see {@link FlexContainer#getFlexWrap()}
     */
    private int mFlexWrap;

    /**
     * The current value of the {@link JustifyContent}, the default value is
     * {@link JustifyContent#FLEX_START}.
     *
     * @see {@link FlexContainer#getJustifyContent()}
     */
    private int mJustifyContent;

    /**
     * The current value of the {@link AlignItems}, the default value is
     * {@link AlignItems#STRETCH}.
     *
     * @see {@link FlexContainer#getAlignItems()}
     */
    private int mAlignItems;

    /**
     * True if the layout direction is right to left, false otherwise.
     */
    private boolean mIsRtl;

    /**
     * True if the layout direction is bottom to top, false otherwise.
     */
    private boolean mFromBottomToTop;

    private List<FlexLine> mFlexLines = new ArrayList<>();

    private final FlexboxHelper mFlexboxHelper = new FlexboxHelper(this);

    /**
     * A snapshot of the {@link RecyclerView.Recycler} instance at a given moment.
     * It's not guaranteed that this instance has a reference to the latest Recycler.
     * When you want to use the latest Recycler, use the one passed as an method argument
     * (such as the one in {@link #onLayoutChildren(RecyclerView.Recycler, RecyclerView.State)})
     */
    private RecyclerView.Recycler mRecycler;

    /**
     * A snapshot of the {@link RecyclerView.State} instance at a given moment.
     * It's not guaranteed that this instance has a reference to the latest State.
     * When you want to use the latest State, use the one passed as an method argument
     * (such as the one in {@link #onLayoutChildren(RecyclerView.Recycler, RecyclerView.State)})
     */
    private RecyclerView.State mState;

    private LayoutState mLayoutState;

    private AnchorInfo mAnchorInfo = new AnchorInfo();

    /**
     * {@link OrientationHelper} along the scrolling direction.
     * e.g. If the flex direction is set to {@link FlexDirection#ROW} and flex wrap is set to
     * {@link FlexWrap#WRAP}, the RecyclerView scrolls vertically (along the cross axis).
     *
     * TODO: We may need another OrientationHelper along the perpendicular direction because
     * if flex wrap is set to {@link FlexWrap#NOWRAP}, it scrolls perpendicular to the cross axis.
     */
    private OrientationHelper mOrientationHelper;

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

        setFlexWrap(FlexWrap.WRAP);
        setAutoMeasureEnabled(true);
    }

    // From here, methods from FlexContainer
    @FlexDirection
    @Override
    public int getFlexDirection() {
        return mFlexDirection;
    }

    @Override
    public void setFlexDirection(@FlexDirection int flexDirection) {
        if (mFlexDirection != flexDirection) {
            if (isMainAxisDirectionChanging(flexDirection)) {
                removeAllViews();
                clearFlexLines();
            }
            mFlexDirection = flexDirection;
            mOrientationHelper = null;
            requestLayout();
        }
    }

    @Override
    @FlexWrap
    public int getFlexWrap() {
        return mFlexWrap;
    }

    @Override
    public void setFlexWrap(@FlexWrap int flexWrap) {
        if (flexWrap == FlexWrap.WRAP_REVERSE) {
            throw new UnsupportedOperationException("wrap_reverse is not supported in "
                    + "FlexboxLayoutManager");
        }
        if (mFlexWrap != flexWrap) {
            if (mFlexWrap == FlexWrap.NOWRAP || flexWrap == FlexWrap.NOWRAP) {
                removeAllViews();
                clearFlexLines();
            }
            mFlexWrap = flexWrap;
            mOrientationHelper = null;
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
            if (mAlignItems == AlignItems.STRETCH || alignItems == AlignItems.STRETCH) {
                removeAllViews();
                clearFlexLines();
            }
            mAlignItems = alignItems;
            requestLayout();
        }
    }

    @AlignContent
    @Override
    public int getAlignContent() {
        return AlignContent.STRETCH;
    }

    @Override
    public void setAlignContent(@AlignContent int alignContent) {
        throw new UnsupportedOperationException("Setting the alignContent in the "
                + "FlexboxLayoutManager is not supported. Use FlexboxLayout "
                + "if you need to reorder using the attribute.");
    }

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
        // TODO: Implement the method
        return 0;
    }

    @Override
    public void onNewFlexItemAdded(int childAbsoluteIndex, int childRelativeIndexInFlexLine,
            FlexLine flexLine) {
        // TODO: Implement the method
    }

    /**
     * @return the number of flex items contained in the flex container.
     * This method doesn't always reflect the latest state of the adapter.
     * If you want to access the latest state of the adapter, use the {@link RecyclerView.State}
     * instance passed as an argument for some methods (such as
     * {@link #onLayoutChildren(RecyclerView.Recycler, RecyclerView.State)})
     *
     * This method is used to avoid the implementation of the similar method.
     * i.e. {@link FlexboxLayoutManager#getChildCount()} returns the child count, but it doesn't
     * include the children that are detached or scrapped.
     */
    @Override
    public int getFlexItemCount() {
        return mState.getItemCount();
    }

    /**
     * @return the flex item as a view specified as the index.
     * This method doesn't always return the latest state of the view in the adapter.
     * If you want to access the latest state, use the {@link RecyclerView.Recycler}
     * instance passed as an argument for some methods (such as
     * {@link #onLayoutChildren(RecyclerView.Recycler, RecyclerView.State)})
     *
     * This method is used to avoid the implementation of the similar method.
     * i.e. {@link FlexboxLayoutManager#getChildAt(int)} returns a view for the given index,
     * but the index is based on the layout position, not based on the adapter position, which
     * isn't desired given the usage of this method.
     */
    @Override
    public View getFlexItemAt(int index) {
        // Look up from the scrap first to avoid the same view holder is created from the adpater
        // again
        List<RecyclerView.ViewHolder> scrapList = mRecycler.getScrapList();
        for (int i = 0, scrapCount = scrapList.size(); i < scrapCount; i++) {
            RecyclerView.ViewHolder viewHolder = scrapList.get(i);
            if (viewHolder.getAdapterPosition() == index) {
                return viewHolder.itemView;
            }
        }
        return mRecycler.getViewForPosition(index);
    }

    /**
     * Returns a View for the given index.
     * The order attribute ({@link FlexItem#getOrder()}) is not supported by this class since
     * otherwise all view holders need to be inflated at least once even though only the visible
     * part of the layout is needed.
     * Implementing this method just to make this class conform to the
     * {@link FlexContainer} interface.
     *
     * @param index the index of the view
     * @return the view for the given index.
     * If the index is negative or out of bounds of the number of contained views,
     * returns {@code null}.
     */
    @Override
    public View getReorderedFlexItemAt(int index) {
        return getFlexItemAt(index);
    }

    @Override
    public void onNewFlexLineAdded(FlexLine flexLine) {
        // No op
    }

    @Override
    public int getChildWidthMeasureSpec(int widthSpec, int padding, int childDimension) {
        return getChildMeasureSpec(getWidth(), getWidthMode(), padding, childDimension,
                canScrollHorizontally());
    }

    @Override
    public int getChildHeightMeasureSpec(int heightSpec, int padding, int childDimension) {
        return getChildMeasureSpec(getHeight(), getHeightMode(), padding, childDimension,
                canScrollVertically());
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
            // TODO: Consider adding decorator between flex lines.
            sum += flexLine.mCrossSize;
        }
        return sum;
    }

    @Override
    public void setFlexLines(List<FlexLine> flexLines) {
        mFlexLines = flexLines;
    }

    @Override
    public List<FlexLine> getFlexLinesInternal() {
        return mFlexLines;
    }
    // The end of methods from FlexContainer

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
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        removeAllViews();
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        // Layout algorithm:
        // 1) Find an anchor coordinate and anchor flex line position. If not found, the coordinate
        //    starts from zero.
        // 2) From the anchor position to the visible area, calculate the flex lines that needs to
        //    be filled.
        // 3) Fill toward end from the anchor position
        // 4) Fill toward start from the anchor position
        if (DEBUG) {
            Log.d(TAG, String.format("getChildCount: %d", getChildCount()));
            Log.d(TAG, "State: " + state);
        }

        // Assign the Recycler and the State as the member variables so that
        // the method from FlexContainer (such as getFlexItemCount()) returns the number of
        // flex items from the adapter not the child count in the LayoutManager because
        // LayoutManager#getChildCount doesn't include the views that are detached or scrapped.
        mRecycler = recycler;
        mState = state;
        int childCount = state.getItemCount();
        if (childCount == 0 && state.isPreLayout()) {
            return;
        }
        ensureOrientationHelper();
        ensureLayoutState();
        mFlexboxHelper.ensureMeasureSpecCache(childCount);
        mFlexboxHelper.ensureMeasuredSizeCache(childCount);

        mFlexboxHelper.ensureIndexToFlexLine(childCount);

        mLayoutState.mShouldRecycle = false;
        mAnchorInfo.reset();
        updateAnchorInfoForLayout(state, mAnchorInfo);

        // Unlike the FlexboxLayout, the order attribute is not supported, we don't calculate the
        // order attribute because preparing the order attribute requires all
        // view holders to be inflated at least once, which is inefficient if the number of items
        // in the adapter is large

        resolveLayoutDirection();
        updateLayoutStateToFillEnd(mAnchorInfo);
        detachAndScrapAttachedViews(recycler);
        if (DEBUG) {
            Log.d(TAG,
                    String.format("onLayoutChildren. recycler.getScrapList.size(): %s, state: %s",
                            recycler.getScrapList().size(), state));
        }

        // Calculate the flex lines until the calculated cross size reaches the
        // LayoutState#mAvailable (or until the end of the flex container)
        //noinspection ResourceType
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(getWidth(), getWidthMode());
        //noinspection ResourceType
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(getHeight(), getHeightMode());
        FlexboxHelper.FlexLinesResult flexLinesResult;
        if (isMainAxisDirectionHorizontal()) {
            if (mFlexLines.size() > 0) {
                // Remove the already calculated flex lines from the anchor position and
                // calculate beyond the available amount (visible area that needs to be filled)
                mFlexboxHelper.clearFlexLines(mFlexLines, mAnchorInfo.mPosition);
                flexLinesResult = mFlexboxHelper
                        .calculateHorizontalFlexLines(widthMeasureSpec, heightMeasureSpec,
                                mLayoutState.mAvailable, mAnchorInfo.mPosition, mFlexLines);
            } else {
                mFlexboxHelper.ensureIndexToFlexLine(childCount);
                flexLinesResult = mFlexboxHelper
                        .calculateHorizontalFlexLines(widthMeasureSpec, heightMeasureSpec,
                                mLayoutState.mAvailable);
            }
        } else {
            if (mFlexLines.size() > 0) {
                // Remove the already calculated flex lines from the anchor position and
                // calculate beyond the available amount (visible area that needs to be filled)
                mFlexboxHelper.clearFlexLines(mFlexLines, mAnchorInfo.mPosition);
                flexLinesResult = mFlexboxHelper
                        .calculateVerticalFlexLines(widthMeasureSpec, heightMeasureSpec,
                                mLayoutState.mAvailable, mAnchorInfo.mPosition, mFlexLines);
            } else {
                mFlexboxHelper.ensureIndexToFlexLine(childCount);
                flexLinesResult = mFlexboxHelper
                        .calculateVerticalFlexLines(widthMeasureSpec, heightMeasureSpec,
                                mLayoutState.mAvailable);
            }
        }
        mFlexLines = flexLinesResult.mFlexLines;
        mFlexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec,
                mAnchorInfo.mPosition);
        // Unlike the FlexboxLayout not calling FlexboxHelper#determineCrossSize because
        // the align content attribute (which is used to determine the cross size) is only effective
        // when the size of flex line is equal or more than 2 and the parent height
        // (length along the cross size) is fixed. But in RecyclerView, these two conditions can't
        // be true at the same time. Because it's scrollable along the cross axis
        // or even if not (when flex wrap is "nowrap") the size of the flex lines should be 1.
        mFlexboxHelper.stretchViews(mAnchorInfo.mPosition);

        if (DEBUG) {
            for (int i = 0, size = mFlexLines.size(); i < size; i++) {
                FlexLine flexLine = mFlexLines.get(i);
                Log.d(TAG, String.format("%d flex line. MainSize: %d, CrossSize: %d, itemCount: %d",
                        i, flexLine.getMainSize(), flexLine.getCrossSize(),
                        flexLine.getItemCount()));
            }
        }

        int filledToEnd = fill(recycler, state, mLayoutState);
        if (DEBUG) {
            Log.d(TAG, String.format("filled: %d toward end", filledToEnd));
        }
        updateLayoutStateToFillStart(mAnchorInfo);
        int filledToStart = fill(recycler, state, mLayoutState);
        if (DEBUG) {
            Log.d(TAG, String.format("filled: %d toward start", filledToStart));
        }
    }

    private void resolveLayoutDirection() {
        int layoutDirection = getLayoutDirection();
        switch (mFlexDirection) {
            case FlexDirection.ROW:
                mIsRtl = layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL;
                mFromBottomToTop = mFlexWrap == FlexWrap.WRAP_REVERSE;
                break;
            case FlexDirection.ROW_REVERSE:
                mIsRtl = layoutDirection != ViewCompat.LAYOUT_DIRECTION_RTL;
                mFromBottomToTop = mFlexWrap == FlexWrap.WRAP_REVERSE;
                break;
            case FlexDirection.COLUMN:
                mIsRtl = layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL;
                if (mFlexWrap == FlexWrap.WRAP_REVERSE) {
                    mIsRtl = !mIsRtl;
                }
                mFromBottomToTop = false;
                break;
            case FlexDirection.COLUMN_REVERSE:
                mIsRtl = layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL;
                if (mFlexWrap == FlexWrap.WRAP_REVERSE) {
                    mIsRtl = !mIsRtl;
                }
                mFromBottomToTop = true;
                break;
            default:
                mIsRtl = false;
                mFromBottomToTop = false;
        }
    }

    private void updateAnchorInfoForLayout(RecyclerView.State state, AnchorInfo anchorInfo) {
        // TODO: Update anchor from the pending state is stored

        if (updateAnchorFromChildren(state, anchorInfo)) {
            if (DEBUG) {
                Log.d(TAG,
                        String.format("updated anchor info from existing children. AnchorInfo: %s",
                                anchorInfo));
            }
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "deciding anchor info for fresh state");
        }
        anchorInfo.assignCoordinateFromPadding();
        anchorInfo.mPosition = 0;
        anchorInfo.mFlexLinePosition = 0;
    }

    /**
     * Finds an anchor child from existing Views. Most of the time, this is the view closest to
     * start or end that has a valid position (e.g. not removed).
     * Large part refers to the same method from LinearLayout#updateAnchorFromChildren
     */
    private boolean updateAnchorFromChildren(RecyclerView.State state, AnchorInfo anchorInfo) {
        if (getChildCount() == 0) {
            return false;
        }
        // TODO: Consider the focused view

        View referenceChild = anchorInfo.mLayoutFromEnd
                ? findLastReferenceChild(state)
                : findFirstReferenceChild(state);
        if (referenceChild != null) {
            anchorInfo.assignFromView(referenceChild);
            // If all visible views are removed in 1 pass, reference child might be out of bounds.
            // If that is the case, offset it back to 0 so that we use these pre-layout children.
            if (!state.isPreLayout() && supportsPredictiveItemAnimations()) {
                // validate this child is at least partially visible. if not, offset it to start
                final boolean notVisible =
                        mOrientationHelper.getDecoratedStart(referenceChild) >= mOrientationHelper
                                .getEndAfterPadding()
                                || mOrientationHelper.getDecoratedEnd(referenceChild)
                                < mOrientationHelper.getStartAfterPadding();
                if (notVisible) {
                    anchorInfo.mCoordinate = anchorInfo.mLayoutFromEnd
                            ? mOrientationHelper.getEndAfterPadding()
                            : mOrientationHelper.getStartAfterPadding();
                }
            }
            return true;
        }
        return false;
    }

    private View findFirstReferenceChild(RecyclerView.State state) {
        return findReferenceChild(0, getChildCount(), state.getItemCount());
    }

    private View findLastReferenceChild(RecyclerView.State state) {
        return findReferenceChild(getChildCount() - 1, -1, state.getItemCount());
    }

    /**
     * Find a visible (or less preferred invisible) view within the given start and end index.
     * Large part refers to the same method in LinearLayoutManager#findReferenceChild
     *
     * @param start     the start index within the range to find a view
     * @param end       the end index within the range to find a view
     * @param itemCount the item count
     * @return the found view within the range of the given start and
     */
    private View findReferenceChild(int start, int end, int itemCount) {
        ensureOrientationHelper();
        ensureLayoutState();
        View invalidMatch = null;
        View outOfBoundsMatch = null;
        int boundStart = mOrientationHelper.getStartAfterPadding();
        int boundEnd = mOrientationHelper.getEndAfterPadding();
        int diff = end > start ? 1 : -1;
        for (int i = start; i != end; i += diff) {
            View view = getChildAt(i);
            int position = getPosition(view);
            if (position >= 0 && position < itemCount) {
                if (((RecyclerView.LayoutParams) view.getLayoutParams()).isItemRemoved()) {
                    if (invalidMatch == null) {
                        invalidMatch = view;
                    }
                } else if (mOrientationHelper.getDecoratedStart(view) < boundStart ||
                        mOrientationHelper.getDecoratedEnd(view) > boundEnd) {
                    if (outOfBoundsMatch == null) {
                        outOfBoundsMatch = view;
                    }
                } else {
                    return view;
                }
            }
        }
        return outOfBoundsMatch != null ? outOfBoundsMatch : invalidMatch;
    }

    /**
     * Fills the remaining space defined by the layoutState on
     * how many pixels should be filled (defined by {@link LayoutState#mAvailable}.
     * The large part refers to the LinearLayoutManager#fill method except for the fill direction.
     * Because FlexboxLayoutManager needs to care two scrolling directions:
     * <li>
     * <ul>Along the cross axis - When flex wrap is set to either FlexWrap.WRAP or
     * FlexWrap.WRAP_REVERSE, the layout needs to scroll along the cross axis.</ul>
     * <ul>Along the main axis - When flex wrap is set t FlexWrap.NOWRAP, the layout needs
     * to scroll along the main axis if there are overflowing flex items.</ul>
     * </li>
     *
     * @return the amount of pixels filled
     */
    private int fill(RecyclerView.Recycler recycler, RecyclerView.State state,
            LayoutState layoutState) {
        if (layoutState.mScrollingOffset != LayoutState.SCROLLING_OFFSET_NaN) {
            if (layoutState.mAvailable < 0) {
                layoutState.mScrollingOffset += layoutState.mAvailable;
            }
            recycleByLayoutState(recycler, layoutState);
        }
        int start = layoutState.mAvailable;
        int remainingSpace = layoutState.mAvailable;
        int consumed = 0;
        while (remainingSpace > 0 && layoutState.hasMore(state, mFlexLines)) {
            FlexLine flexLine = mFlexLines.get(layoutState.mFlexLinePosition);
            layoutState.mPosition = flexLine.mFirstIndex;
            consumed += layoutFlexLine(recycler, state, flexLine, layoutState);
            layoutState.mOffset += flexLine.getCrossSize() * layoutState.mLayoutDirection;
            remainingSpace -= flexLine.getCrossSize();
        }
        layoutState.mAvailable -= consumed;
        if (layoutState.mScrollingOffset != LayoutState.SCROLLING_OFFSET_NaN) {
            layoutState.mScrollingOffset += consumed;
            if (layoutState.mAvailable < 0) {
                layoutState.mScrollingOffset += layoutState.mAvailable;
            }
            recycleByLayoutState(recycler, layoutState);
        }
        return start - layoutState.mAvailable;
    }

    private void recycleByLayoutState(RecyclerView.Recycler recycler, LayoutState layoutState) {
        if (!layoutState.mShouldRecycle) {
            return;
        }
        if (layoutState.mLayoutDirection == LayoutDirection.START) {
            // TODO: Consider the case mFlexWrap is set to nowrap and view is recycled individually
            recycleFlexLinesFromEnd(recycler, layoutState);
        } else {
            recycleFlexLinesFromStart(recycler, layoutState);
        }
    }

    private void recycleFlexLinesFromStart(RecyclerView.Recycler recycler,
            LayoutState layoutState) {
        if (layoutState.mScrollingOffset < 0) {
            return;
        }
        assert mFlexboxHelper.mIndexToFlexLine != null;
        int childCount = getChildCount();

        View firstView = getChildAt(0);

        int currentLineIndex = mFlexboxHelper.mIndexToFlexLine[getPosition(firstView)];
        FlexLine flexLine = mFlexLines.get(currentLineIndex);
        int recycleTo = -1;
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (mOrientationHelper.getDecoratedEnd(view) <= layoutState.mScrollingOffset) {
                if (flexLine.mLastIndex == getPosition(view)) {
                    // Recycle the views in a flex line if all views end positions are lower than
                    // the scrolling offset because the views are laid out as a flex line unit.
                    // We need to also recycle the views as an unit of a flex line
                    recycleTo = i;
                    if (currentLineIndex >= mFlexLines.size() - 1) {
                        // Reached to the last line
                        break;
                    } else {
                        currentLineIndex += layoutState.mLayoutDirection;
                        flexLine = mFlexLines.get(currentLineIndex);
                    }
                }
            } else {
                break;
            }
        }
        recycleChildren(recycler, 0, recycleTo);
    }

    private void recycleFlexLinesFromEnd(RecyclerView.Recycler recycler, LayoutState layoutState) {
        if (layoutState.mScrollingOffset < 0) {
            return;
        }
        assert mFlexboxHelper.mIndexToFlexLine != null;
        int limit = mOrientationHelper.getEnd() - layoutState.mScrollingOffset;
        int childCount = getChildCount();

        View lastView = getChildAt(childCount - 1);
        int currentLineIndex = mFlexboxHelper.mIndexToFlexLine[getPosition(lastView)];
        int recycleTo = childCount - 1;
        int recycleFrom = childCount;
        FlexLine flexLine = mFlexLines.get(currentLineIndex);
        for (int i = childCount - 1; i >= 0; i--) {
            View view = getChildAt(i);
            if (mOrientationHelper.getDecoratedStart(view) >= limit) {
                if (flexLine.mFirstIndex == getPosition(view)) {
                    // Recycle the views in a flex line if all views start positions are beyond the
                    // limit because the views are laid out as a flex line unit. We need to also
                    // recycle the views as an unit of a flex line
                    recycleFrom = i;
                    if (currentLineIndex <= 0) {
                        // Reached to the first flex line
                        break;
                    } else {
                        currentLineIndex += layoutState.mLayoutDirection;
                        flexLine = mFlexLines.get(currentLineIndex);
                    }
                }
            } else {
                break;
            }
        }
        recycleChildren(recycler, recycleFrom, recycleTo);
    }

    /**
     * Recycles children between given indices.
     *
     * @param startIndex inclusive
     * @param endIndex   inclusive
     */
    private void recycleChildren(RecyclerView.Recycler recycler, int startIndex, int endIndex) {
        for (int i = endIndex; i >= startIndex; i--) {
            removeAndRecycleViewAt(i, recycler);
        }
    }

    private int layoutFlexLine(RecyclerView.Recycler recycler, RecyclerView.State state,
            FlexLine flexLine, LayoutState layoutState) {
        if (isMainAxisDirectionHorizontal()) {
            return layoutFlexLineMainAxisHorizontal(recycler, state, flexLine, layoutState);
        } else {
            return layoutFlexLineMainAxisVertical(recycler, state, flexLine, layoutState);
        }
    }

    private int layoutFlexLineMainAxisHorizontal(RecyclerView.Recycler recycler,
            RecyclerView.State state, FlexLine flexLine, LayoutState layoutState) {
        assert mFlexboxHelper.mMeasureSpecCache != null;

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int parentWidth = getWidth();

        int childTop = layoutState.mOffset;
        if (layoutState.mLayoutDirection == LayoutDirection.START) {
            childTop = childTop - flexLine.mCrossSize;
        }
        int startPosition = layoutState.mPosition;

        float childLeft;

        // Only used when mIsRtl is true
        float childRight;
        float spaceBetweenItem = 0f;
        switch (mJustifyContent) {
            case JustifyContent.FLEX_START:
                childLeft = paddingLeft;
                childRight = parentWidth - paddingRight;
                break;
            case JustifyContent.FLEX_END:
                childLeft = parentWidth - flexLine.mMainSize + paddingRight;
                childRight = flexLine.mMainSize - paddingLeft;
                break;
            case JustifyContent.CENTER:
                childLeft = paddingLeft + (parentWidth - flexLine.mMainSize) / 2f;
                childRight = parentWidth - paddingRight - (parentWidth - flexLine.mMainSize) / 2f;
                break;
            case JustifyContent.SPACE_AROUND:
                if (flexLine.mItemCount != 0) {
                    spaceBetweenItem = (parentWidth - flexLine.mMainSize)
                            / (float) flexLine.mItemCount;
                }
                childLeft = paddingLeft + spaceBetweenItem / 2f;
                childRight = parentWidth - paddingRight - spaceBetweenItem / 2f;
                break;
            case JustifyContent.SPACE_BETWEEN:
                childLeft = paddingLeft;
                float denominator = flexLine.mItemCount != 1 ? flexLine.mItemCount - 1 : 1f;
                spaceBetweenItem = (parentWidth - flexLine.mMainSize) / denominator;
                childRight = parentWidth - paddingRight;
                break;
            default:
                throw new IllegalStateException(
                        "Invalid justifyContent is set: " + mJustifyContent);
        }
        spaceBetweenItem = Math.max(spaceBetweenItem, 0);

        // Used only when mLayoutDirection == LayoutDirection.START to remember the index
        // a flex item should be inserted
        int indexInFlexLine = 0;
        for (int i = startPosition, itemCount = flexLine.getItemCount();
                i < startPosition + itemCount; i++) {
            if (!layoutState.hasMore(state, mFlexLines)) {
                break;
            }
            View view = getFlexItemAt(i);
            if (view == null) {
                continue;
            }

            if (layoutState.mLayoutDirection == LayoutDirection.END) {
                addView(view);
            } else {
                addView(view, indexInFlexLine);
                indexInFlexLine++;
            }

            // Retrieve the measure spec from the cache because the view may be re-created when
            // retrieved from Recycler, in that case measured width/height are set to 0 even
            // each visible child should be measured at least once in the FlexboxHelper
            long measureSpec = mFlexboxHelper.mMeasureSpecCache[i];
            int widthSpec = mFlexboxHelper.extractLowerInt(measureSpec);
            int heightSpec = mFlexboxHelper.extractHigherInt(measureSpec);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (shouldMeasureChild(view, widthSpec, heightSpec, lp)) {
                // TODO: Need to consider decorator length
                view.measure(widthSpec, heightSpec);
            }

            childLeft += (lp.leftMargin + getLeftDecorationWidth(view));
            childRight -= (lp.rightMargin + getRightDecorationWidth(view));

            if (mIsRtl) {
                mFlexboxHelper.layoutSingleChildHorizontal(view, flexLine,
                        Math.round(childRight) - view.getMeasuredWidth(),
                        childTop, Math.round(childRight),
                        childTop + view.getMeasuredHeight());
            } else {
                mFlexboxHelper.layoutSingleChildHorizontal(view, flexLine,
                        Math.round(childLeft), childTop,
                        Math.round(childLeft) + view.getMeasuredWidth(),
                        childTop + view.getMeasuredHeight());
            }
            childLeft += (view.getMeasuredWidth() + lp.rightMargin + getRightDecorationWidth(view)
                    + spaceBetweenItem);
            childRight -= (view.getMeasuredWidth() + lp.leftMargin + getLeftDecorationWidth(view)
                    + spaceBetweenItem);

            flexLine.updatePositionFromView(view, getDecoratedLeft(view), 0,
                    getDecoratedRight(view), 0);
        }
        layoutState.mFlexLinePosition += mLayoutState.mLayoutDirection;
        return flexLine.getCrossSize();
    }

    private int layoutFlexLineMainAxisVertical(RecyclerView.Recycler recycler,
            RecyclerView.State state, FlexLine flexLine, LayoutState layoutState) {
        assert mFlexboxHelper.mMeasureSpecCache != null;

        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int parentHeight = getHeight();

        // Either childLeft or childRight is used depending on the layoutState.mLayoutDirection
        int childLeft = layoutState.mOffset;
        if (layoutState.mLayoutDirection == LayoutDirection.START) {
            childLeft = childLeft - flexLine.mCrossSize;
        }
        int startPosition = layoutState.mPosition;

        float childTop;

        // Only used when mFromBottomToTop is true
        float childBottom;
        float spaceBetweenItem = 0f;
        switch (mJustifyContent) {
            case JustifyContent.FLEX_START:
                childTop = paddingTop;
                childBottom = parentHeight - paddingBottom;
                break;
            case JustifyContent.FLEX_END:
                childTop = parentHeight - flexLine.mMainSize + paddingBottom;
                childBottom = flexLine.mMainSize - paddingTop;
                break;
            case JustifyContent.CENTER:
                childTop = paddingTop + (parentHeight - flexLine.mMainSize) / 2f;
                childBottom = parentHeight - paddingBottom
                        - (parentHeight - flexLine.mMainSize) / 2f;
                break;
            case JustifyContent.SPACE_AROUND:
                if (flexLine.mItemCount != 0) {
                    spaceBetweenItem = (parentHeight - flexLine.mMainSize)
                            / (float) flexLine.mItemCount;
                }
                childTop = paddingTop + spaceBetweenItem / 2f;
                childBottom = parentHeight - paddingBottom - spaceBetweenItem / 2f;
                break;
            case JustifyContent.SPACE_BETWEEN:
                childTop = paddingTop;
                float denominator = flexLine.mItemCount != 1 ? flexLine.mItemCount - 1 : 1f;
                spaceBetweenItem = (parentHeight - flexLine.mMainSize) / denominator;
                childBottom = parentHeight - paddingBottom;
                break;
            default:
                throw new IllegalStateException(
                        "Invalid justifyContent is set: " + mJustifyContent);
        }
        spaceBetweenItem = Math.max(spaceBetweenItem, 0);

        // Used only when mLayoutDirection == LayoutDirection.START to remember the index
        // a flex item should be inserted
        int indexInFlexLine = 0;
        for (int i = startPosition, itemCount = flexLine.getItemCount();
                i < startPosition + itemCount; i++) {
            if (!layoutState.hasMore(state, mFlexLines)) {
                break;
            }
            View view = getFlexItemAt(i);

            if (view == null) {
                continue;
            }

            // Retrieve the measure spec from the cache because the view may be re-created when
            // retrieved from Recycler, in that case measured width/height are set to 0 even
            // each visible child should be measured at least once in the FlexboxHelper
            long measureSpec = mFlexboxHelper.mMeasureSpecCache[i];
            int widthSpec = mFlexboxHelper.extractLowerInt(measureSpec);
            int heightSpec = mFlexboxHelper.extractHigherInt(measureSpec);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (shouldMeasureChild(view, widthSpec, heightSpec, lp)) {
                // TODO: Need to consider decorator length
                view.measure(widthSpec, heightSpec);
            }

            childTop += (lp.topMargin + getTopDecorationHeight(view));
            childBottom -= (lp.rightMargin + getBottomDecorationHeight(view));

            if (layoutState.mLayoutDirection == LayoutDirection.END) {
                addView(view);
            } else {
                addView(view, indexInFlexLine);
                indexInFlexLine++;
            }

            if (mFromBottomToTop) {
                mFlexboxHelper.layoutSingleChildVertical(view, flexLine, mIsRtl,
                        childLeft, Math.round(childBottom) - view.getMeasuredHeight(),
                        childLeft + view.getMeasuredWidth(), Math.round(childBottom));
            } else {
                mFlexboxHelper.layoutSingleChildVertical(view, flexLine, mIsRtl,
                        childLeft, Math.round(childTop),
                        childLeft + view.getMeasuredWidth(),
                        Math.round(childTop) + view.getMeasuredHeight());
            }
            childTop += (view.getMeasuredHeight() + lp.topMargin + getTopDecorationHeight(view)
                    + spaceBetweenItem);
            childBottom -= (view.getMeasuredHeight() + lp.bottomMargin +
                    getBottomDecorationHeight(view) + spaceBetweenItem);

            flexLine.updatePositionFromView(view, 0, getDecoratedTop(view),
                    0, getDecoratedBottom(view));
        }
        layoutState.mFlexLinePosition += mLayoutState.mLayoutDirection;
        return flexLine.getCrossSize();
    }

    private boolean isMainAxisDirectionHorizontal() {
        return mFlexDirection == FlexDirection.ROW || mFlexDirection == FlexDirection.ROW_REVERSE;
    }

    private boolean isMainAxisDirectionVertical() {
        return mFlexDirection == FlexDirection.COLUMN
                || mFlexDirection == FlexDirection.COLUMN_REVERSE;
    }

    private boolean isMainAxisDirectionChanging(@FlexDirection int newDirection) {
        boolean mainAxisHorizontal = isMainAxisDirectionHorizontal();
        boolean mainAxisVertical = isMainAxisDirectionVertical();
        return (mainAxisHorizontal && newDirection == FlexDirection.COLUMN ||
                mainAxisHorizontal && newDirection == FlexDirection.COLUMN_REVERSE) ||
                (mainAxisVertical && newDirection == FlexDirection.ROW ||
                        mainAxisVertical && newDirection == FlexDirection.ROW_REVERSE);
    }

    private void updateLayoutStateToFillEnd(AnchorInfo anchorInfo) {
        mLayoutState.mAvailable = mOrientationHelper.getEndAfterPadding() - anchorInfo.mCoordinate;
        mLayoutState.mPosition = anchorInfo.mPosition;
        mLayoutState.mItemDirection = ItemDirection.TAIL;
        mLayoutState.mLayoutDirection = LayoutDirection.END;
        mLayoutState.mOffset = anchorInfo.mCoordinate;
        mLayoutState.mScrollingOffset = LayoutState.SCROLLING_OFFSET_NaN;
        mLayoutState.mFlexLinePosition = anchorInfo.mFlexLinePosition;
    }

    private void updateLayoutStateToFillStart(AnchorInfo anchorInfo) {
        mLayoutState.mAvailable = anchorInfo.mCoordinate - mOrientationHelper
                .getStartAfterPadding();
        mLayoutState.mPosition = anchorInfo.mPosition;
        mLayoutState.mItemDirection = ItemDirection.TAIL;
        mLayoutState.mLayoutDirection = LayoutDirection.START;
        mLayoutState.mOffset = anchorInfo.mCoordinate;
        mLayoutState.mScrollingOffset = LayoutState.SCROLLING_OFFSET_NaN;
        mLayoutState.mFlexLinePosition = anchorInfo.mFlexLinePosition;

        if (anchorInfo.mFlexLinePosition > 0 && mFlexLines.size() > anchorInfo.mFlexLinePosition) {
            FlexLine currentLine = mFlexLines.get(anchorInfo.mFlexLinePosition);
            mLayoutState.mFlexLinePosition--;
            mLayoutState.mPosition -= currentLine.getItemCount();
        }
    }

    private void ensureOrientationHelper() {
        if (mOrientationHelper != null) {
            return;
        }
        // There are two cases for each of main axis direction. When the main axis direction is
        // horizontal:
        // -- Scroll horizontally when mFlexWrap == FlexWrap.NOWRAP. In this case scroll happens
        //    along the main axis
        // -- Scroll vertically when mFlexWrap != FlexWrap.NOWRAP. In this case scroll happens
        //    along the cross axis
        //
        // When scroll direction is vertical:
        // -- Scroll vertically when mFlexWrap == FlexWrap.NOWRAP. In this case scroll happens
        //    along the main axis
        // -- Scroll horizontally when mFlexWrap != FlexWrap.NOWRAP. In this case scroll happens
        //    along the cross axis
        if (isMainAxisDirectionHorizontal()) {
            if (mFlexWrap == FlexWrap.NOWRAP) {
                mOrientationHelper = OrientationHelper.createHorizontalHelper(this);
            } else {
                mOrientationHelper = OrientationHelper.createVerticalHelper(this);
            }
        } else {
            if (mFlexWrap == FlexWrap.NOWRAP) {
                mOrientationHelper = OrientationHelper.createVerticalHelper(this);
            } else {
                mOrientationHelper = OrientationHelper.createHorizontalHelper(this);
            }
        }
    }

    private void ensureLayoutState() {
        if (mLayoutState == null) {
            mLayoutState = new LayoutState();
        }
    }

    @Override
    public boolean canScrollHorizontally() {
        if (mFlexWrap == FlexWrap.NOWRAP) {
            return isMainAxisDirectionHorizontal();
        }
        return !isMainAxisDirectionHorizontal();
    }

    @Override
    public boolean canScrollVertically() {
        if (mFlexWrap == FlexWrap.NOWRAP) {
            return !isMainAxisDirectionHorizontal();
        }
        return isMainAxisDirectionHorizontal();
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler,
            RecyclerView.State state) {
        return handleScrolling(dx, recycler, state);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler,
            RecyclerView.State state) {
        return handleScrolling(dy, recycler, state);
    }

    /**
     * @param delta    the delta for the amount that is being scrolled
     *                 (either horizontally or vertically)
     * @param recycler the Recycler instance
     * @param state    the Recycler.State instance
     * @return the amount actually scrolled
     */
    private int handleScrolling(int delta, RecyclerView.Recycler recycler,
            RecyclerView.State state) {
        if (getChildCount() == 0 || delta == 0) {
            return 0;
        }
        ensureOrientationHelper();
        mLayoutState.mShouldRecycle = true;
        int layoutDirection = delta > 0 ? LayoutDirection.END : LayoutDirection.START;
        int absDelta = Math.abs(delta);

        updateLayoutState(layoutDirection, absDelta);

        int freeScroll = mLayoutState.mScrollingOffset;
        int consumed = freeScroll + fill(recycler, state, mLayoutState);
        if (consumed < 0) {
            return 0;
        }
        int scrolled = absDelta > consumed ? layoutDirection * consumed : delta;
        mOrientationHelper.offsetChildren(-scrolled);
        mLayoutState.mLastScrollDelta = scrolled;
        return scrolled;
    }

    private void updateLayoutState(@LayoutDirection int layoutDirection, int absDelta) {
        assert mFlexboxHelper.mIndexToFlexLine != null;
        // TODO: Consider updating LayoutState#mExtra to support better smooth scrolling
        mLayoutState.mLayoutDirection = layoutDirection;
        boolean mainAxisHorizontal = isMainAxisDirectionHorizontal();
        if (layoutDirection == LayoutDirection.END) {
            View lastVisible = getChildAt(getChildCount() - 1);
            mLayoutState.mOffset = mOrientationHelper.getDecoratedEnd(lastVisible);
            int lastVisiblePosition = getPosition(lastVisible);
            int lastVisibleLinePosition = mFlexboxHelper.mIndexToFlexLine[lastVisiblePosition];
            FlexLine lastVisibleLine = mFlexLines.get(lastVisibleLinePosition);

            // Loop through the views in the same line of the last visible view because the
            // next view should be placed to the end of the flex line to which the last visible view
            // belongs
            for (int i = getChildCount() - 2, to = getChildCount() - lastVisibleLine.mItemCount - 1;
                    i > to; i--) {
                View viewInSameLine = getChildAt(i);
                if (viewInSameLine == null || viewInSameLine.getVisibility() == View.GONE) {
                    continue;
                }
                if (mIsRtl && !mainAxisHorizontal) {
                    // The end edge of the view is left, should be the minimum left edge
                    // where the next view should be placed
                    mLayoutState.mOffset = Math.min(mLayoutState.mOffset,
                            mOrientationHelper.getDecoratedEnd(viewInSameLine));
                } else {
                    mLayoutState.mOffset = Math.max(mLayoutState.mOffset,
                            mOrientationHelper.getDecoratedEnd(viewInSameLine));
                }
            }

            mLayoutState.mItemDirection = ItemDirection.TAIL;
            mLayoutState.mPosition = lastVisiblePosition + mLayoutState.mItemDirection;
            if (mFlexboxHelper.mIndexToFlexLine.length <= mLayoutState.mPosition) {
                mLayoutState.mFlexLinePosition = NO_POSITION;
            } else {
                mLayoutState.mFlexLinePosition
                        = mFlexboxHelper.mIndexToFlexLine[mLayoutState.mPosition];
            }
            mLayoutState.mScrollingOffset = mOrientationHelper.getDecoratedEnd(lastVisible)
                    - mOrientationHelper.getEndAfterPadding();

            // If the RecyclerView tries to scroll beyond the already calculated
            // flex container, need to calculate beyond the amount that needs to be filled
            if ((mLayoutState.mFlexLinePosition == NO_POSITION
                    || mLayoutState.mFlexLinePosition > mFlexLines.size() - 1) &&
                    mLayoutState.mPosition <= getFlexItemCount()) {
                //noinspection ResourceType
                int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(getWidth(), getWidthMode());
                //noinspection ResourceType
                int heightMeasureSpec = View.MeasureSpec
                        .makeMeasureSpec(getHeight(), getHeightMode());
                int needsToFill = absDelta - mLayoutState.mScrollingOffset;
                if (needsToFill > 0) {
                    if (mainAxisHorizontal) {
                        mFlexboxHelper.calculateHorizontalFlexLines(
                                widthMeasureSpec, heightMeasureSpec, needsToFill,
                                mLayoutState.mPosition, mFlexLines);
                    } else {
                        mFlexboxHelper.calculateVerticalFlexLines(
                                widthMeasureSpec, heightMeasureSpec, needsToFill,
                                mLayoutState.mPosition, mFlexLines);
                    }
                    mFlexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec,
                            mLayoutState.mPosition);
                    mFlexboxHelper.stretchViews(mLayoutState.mPosition);
                }
            }
        } else {
            View firstVisible = getChildAt(0);
            mLayoutState.mOffset = mOrientationHelper.getDecoratedStart(firstVisible);
            int firstVisiblePosition = getPosition(firstVisible);
            int firstVisibleLinePosition = mFlexboxHelper.mIndexToFlexLine[firstVisiblePosition];
            FlexLine firstVisibleLine = mFlexLines.get(firstVisibleLinePosition);

            // Loop through the views in the same line of the first visible view because the
            // next view should be placed to the start of the flex line to which the first visible
            // view belongs
            for (int i = 1, to = firstVisibleLine.mItemCount;
                    i < to; i++) {
                View viewInSameLine = getChildAt(i);
                if (mIsRtl && !mainAxisHorizontal) {
                    mLayoutState.mOffset = Math.max(mLayoutState.mOffset,
                            mOrientationHelper.getDecoratedStart(viewInSameLine));
                } else {
                    mLayoutState.mOffset = Math.min(mLayoutState.mOffset,
                            mOrientationHelper.getDecoratedStart(viewInSameLine));
                }
            }

            mLayoutState.mItemDirection = ItemDirection.TAIL;
            int flexLinePosition = mFlexboxHelper.mIndexToFlexLine[firstVisiblePosition];
            if (flexLinePosition == NO_POSITION) {
                flexLinePosition = 0;
            }
            FlexLine currentLine = mFlexLines.get(flexLinePosition);
            // The position of the next item toward start should be on the next flex line,
            // shifting the position by the number of the items in the current line.
            mLayoutState.mPosition = firstVisiblePosition - currentLine.getItemCount();
            mLayoutState.mFlexLinePosition = flexLinePosition > 0 ? flexLinePosition - 1 : 0;
            mLayoutState.mScrollingOffset = -mOrientationHelper.getDecoratedStart(firstVisible)
                    + mOrientationHelper.getStartAfterPadding();
        }
        mLayoutState.mAvailable = absDelta - mLayoutState.mScrollingOffset;
    }

    /**
     * Copied from {@link android.support.v7.widget.RecyclerView.LayoutManager#shouldMeasureChild(View,
     * int, int, RecyclerView.LayoutParams)}}
     */
    private boolean shouldMeasureChild(View child, int widthSpec, int heightSpec,
            RecyclerView.LayoutParams lp) {
        return child.isLayoutRequested()
                || !isMeasurementCacheEnabled()
                || !isMeasurementUpToDate(child.getWidth(), widthSpec, lp.width)
                || !isMeasurementUpToDate(child.getHeight(), heightSpec, lp.height);
    }

    /**
     * Copied from
     * {@link android.support.v7.widget.RecyclerView.LayoutManager#isMeasurementUpToDate(int, int,
     * int)}
     */
    private static boolean isMeasurementUpToDate(int childSize, int spec, int dimension) {
        final int specMode = View.MeasureSpec.getMode(spec);
        final int specSize = View.MeasureSpec.getSize(spec);
        if (dimension > 0 && childSize != dimension) {
            return false;
        }
        switch (specMode) {
            case View.MeasureSpec.UNSPECIFIED:
                return true;
            case View.MeasureSpec.AT_MOST:
                return specSize >= childSize;
            case View.MeasureSpec.EXACTLY:
                return specSize == childSize;
        }
        return false;
    }

    private void clearFlexLines() {
        mFlexLines.clear();
        mAnchorInfo.reset();
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
            return FlexItem.ORDER_DEFAULT;
        }

        @Override
        public void setOrder(int order) {
            throw new UnsupportedOperationException("Setting the order in the "
                    + "FlexboxLayoutManager is not supported. Use FlexboxLayout "
                    + "if you need to reorder using the attribute.");
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
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

    /**
     * A class that holds the information about an anchor position like from what pixels layout
     * should start.
     */
    private class AnchorInfo {

        private int mPosition;

        private int mFlexLinePosition;

        private int mCoordinate;

        private boolean mLayoutFromEnd;

        private void reset() {
            mPosition = NO_POSITION;
            mFlexLinePosition = NO_POSITION;
            mCoordinate = INVALID_OFFSET;
            if (isMainAxisDirectionHorizontal()) {
                if (mFlexWrap == FlexWrap.NOWRAP) {
                    mLayoutFromEnd = mFlexDirection == FlexDirection.ROW_REVERSE;
                } else {
                    mLayoutFromEnd = mFlexWrap == FlexWrap.WRAP_REVERSE;
                }
            } else {
                if (mFlexWrap == FlexWrap.NOWRAP) {
                    mLayoutFromEnd = mFlexDirection == FlexDirection.COLUMN_REVERSE;
                } else {
                    mLayoutFromEnd = mFlexWrap == FlexWrap.WRAP_REVERSE;
                }
            }
        }

        private void assignCoordinateFromPadding() {
            mCoordinate = mLayoutFromEnd ? mOrientationHelper.getEndAfterPadding()
                    : mOrientationHelper.getStartAfterPadding();
        }

        private void assignFromView(View view) {
            if (mLayoutFromEnd) {
                mCoordinate = mOrientationHelper.getDecoratedEnd(view) +
                        mOrientationHelper.getTotalSpaceChange();
            } else {
                mCoordinate = mOrientationHelper.getDecoratedStart(view);
            }
            mPosition = getPosition(view);
            assert mFlexboxHelper.mIndexToFlexLine != null;
            int flexLinePosition = mFlexboxHelper.mIndexToFlexLine[mPosition];
            mFlexLinePosition = flexLinePosition != NO_POSITION ? flexLinePosition : 0;
            // It's likely that the view is the first item in a flex line, but if not get the
            // index of the first item in the same line because the calculation of the flex lines
            // expects that it starts from the first item in a flex line
            if (mFlexLines.size() > mFlexLinePosition) {
                mPosition = mFlexLines.get(mFlexLinePosition).mFirstIndex;
            }
        }

        @Override
        public String toString() {
            return "AnchorInfo{" +
                    "mPosition=" + mPosition +
                    ", mFlexLinePosition=" + mFlexLinePosition +
                    ", mCoordinate=" + mCoordinate +
                    ", mLayoutFromEnd=" + mLayoutFromEnd +
                    '}';
        }
    }

    /** Defines the direction in which the layout is filled. */
    @IntDef({LayoutDirection.START, LayoutDirection.END})
    @Retention(RetentionPolicy.SOURCE)
    private @interface LayoutDirection {

        int START = -1;
        int END = 1;
    }

    /** Defines the direction in which the data adapter is traversed. */
    @IntDef({ItemDirection.HEAD, ItemDirection.TAIL})
    @Retention(RetentionPolicy.SOURCE)
    private @interface ItemDirection {

        int HEAD = -1;
        int TAIL = 1;
    }

    /**
     * Helper class that keeps temporary state while the FlexboxLayoutManager is filling out the
     * empty space.
     */
    private static class LayoutState {

        private final static int SCROLLING_OFFSET_NaN = Integer.MIN_VALUE;

        /** Number of pixels that we should fill, in the layout direction. */
        private int mAvailable;

        // TODO: Add mExtra to support better smooth scrolling

        /** Current position on the flex lines being laid out in the layout call */
        private int mFlexLinePosition;

        /** Current position on the adapter to get the next item. */
        private int mPosition;

        /** Pixel offset where layout should start */
        int mOffset;

        /**
         * Used when LayoutState is constructed in a scrolling state.
         * It should be set the amount of scrolling we can make without creating a new view.
         * Settings this is required for efficient view recycling.
         */
        int mScrollingOffset;

        /**
         * The most recent
         * {@link #scrollVerticallyBy(int, RecyclerView.Recycler, RecyclerView.State)} or
         * {@link #scrollHorizontallyBy(int, RecyclerView.Recycler, RecyclerView.State)} amount.
         */
        int mLastScrollDelta;

        @ItemDirection
        private int mItemDirection = ItemDirection.TAIL;

        @LayoutDirection
        private int mLayoutDirection = LayoutDirection.END;

        private boolean mShouldRecycle;

        /**
         * @return {@code true} if there are more items to layout
         */
        private boolean hasMore(RecyclerView.State state, List<FlexLine> flexLines) {
            return mPosition >= 0 && mPosition < state.getItemCount() &&
                    mFlexLinePosition >= 0 && mFlexLinePosition < flexLines.size();
        }

        @Override
        public String toString() {
            return "LayoutState{" +
                    "mAvailable=" + mAvailable +
                    ", mFlexLinePosition=" + mFlexLinePosition +
                    ", mPosition=" + mPosition +
                    ", mOffset=" + mOffset +
                    ", mScrollingOffset=" + mScrollingOffset +
                    ", mLastScrollDelta=" + mLastScrollDelta +
                    ", mItemDirection=" + mItemDirection +
                    ", mLayoutDirection=" + mLayoutDirection +
                    '}';
        }
    }
}
