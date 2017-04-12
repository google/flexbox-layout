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

import static android.support.v7.widget.LinearLayoutManager.INVALID_OFFSET;
import static android.support.v7.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * LayoutManager for the {@link RecyclerView}. This class is intended to be used within a
 * {@link RecyclerView} and offers the same capabilities of measure/layout its children
 * as the {@link FlexboxLayout}.
 */
public class FlexboxLayoutManager extends RecyclerView.LayoutManager implements FlexContainer {

    private static final String TAG = "FlexboxLayoutManager";

    /**
     * Temporary Rect instance to be passed to
     * {@link RecyclerView.LayoutManager#calculateItemDecorationsForChild}
     * to avoid creating a Rect instance every time.
     */
    private static final Rect TEMP_RECT = new Rect();

    private static final boolean DEBUG = false;

    /**
     * The current value of the {@link FlexDirection}, the default value is {@link
     * FlexDirection#ROW}.
     *
     * @see FlexContainer#getFlexDirection()
     */
    private int mFlexDirection;

    /**
     * The current value of the {@link FlexWrap}, the default value is {@link FlexWrap#WRAP}.
     *
     * @see FlexContainer#getFlexWrap()
     */
    private int mFlexWrap;

    /**
     * The current value of the {@link JustifyContent}, the default value is
     * {@link JustifyContent#FLEX_START}.
     *
     * @see FlexContainer#getJustifyContent()
     */
    private int mJustifyContent;

    /**
     * The current value of the {@link AlignItems}, the default value is
     * {@link AlignItems#STRETCH}.
     *
     * @see FlexContainer#getAlignItems()
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
     */
    private OrientationHelper mOrientationHelper;

    private SavedState mPendingSavedState;

    /**
     * The position to which the next layout should start from this adapter position.
     * This value is set either from the {@link #mPendingSavedState} when a configuration change
     * happens or programmatically such as when the {@link #scrollToPosition(int)} is called.
     */
    private int mPendingScrollPosition = NO_POSITION;

    /**
     * The offset by which the next layout should be offset.
     */
    private int mPendingScrollPositionOffset = INVALID_OFFSET;

    /**
     * The width value used in the last {@link #onLayoutChildren} method.
     */
    private int mLastWidth = Integer.MIN_VALUE;

    /**
     * The height value used in the last {@link #onLayoutChildren} method.
     */
    private int mLastHeight = Integer.MIN_VALUE;

    /**
     * If set to {@code true}, this LayoutManager tries to recycle the children when detached from
     * the RecyclerView so that recycled views can be reused using RecycledViewPool.
     */
    private boolean mRecycleChildrenOnDetach;

    /**
     * View cache within this LayoutManager. This is used to avoid the same ViewHolder is created
     * multiple times in the same layout pass (onLayoutChildren or scrollHorizontally or
     * scrollVertically).
     * The keys and values in this cache needs to be cleared at the end of each layout pass.
     */
    private SparseArray<View> mViewCache = new SparseArray<>();

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
        setAlignItems(AlignItems.STRETCH);
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
        setAlignItems(AlignItems.STRETCH);
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
            // Remove the existing views even if the direction changes from
            // row -> row_reverse or column -> column_reverse to make the item decorations dirty
            // state
            removeAllViews();
            clearFlexLines();
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
        for (int i = 0, size = mFlexLines.size(); i < size; i++) {
            FlexLine flexLine = mFlexLines.get(i);
            if (flexLine.getItemCount() == 0) {
                continue;
            }
            result.add(flexLine);
        }
        return result;
    }

    @Override
    public int getDecorationLengthMainAxis(View view, int index, int indexInFlexLine) {
        if (isMainAxisDirectionHorizontal()) {
            return getLeftDecorationWidth(view) + getRightDecorationWidth(view);
        } else {
            return getTopDecorationHeight(view) + getBottomDecorationHeight(view);
        }
    }

    @Override
    public int getDecorationLengthCrossAxis(View view) {
        if (isMainAxisDirectionHorizontal()) {
            return getTopDecorationHeight(view) + getBottomDecorationHeight(view);
        } else {
            return getLeftDecorationWidth(view) + getRightDecorationWidth(view);
        }
    }

    @Override
    public void onNewFlexItemAdded(View view, int index, int indexInFlexLine, FlexLine flexLine) {
        // To avoid creating a new Rect instance every time, passing the same Rect instance
        // since calculated decorations are assigned to view's LayoutParams inside the
        // calculateItemDecorationsForChild method anyway.
        calculateItemDecorationsForChild(view, TEMP_RECT);
        if (isMainAxisDirectionHorizontal()) {
            int decorationWidth = getLeftDecorationWidth(view) + getRightDecorationWidth(view);
            flexLine.mMainSize += decorationWidth;
            flexLine.mDividerLengthInMainSize += decorationWidth;
        } else {
            int decorationHeight = getTopDecorationHeight(view) + getBottomDecorationHeight(view);
            flexLine.mMainSize += decorationHeight;
            flexLine.mDividerLengthInMainSize += decorationHeight;
        }
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
        // Look up the cache within the LayoutManager first, since it's the most light operation.
        View cachedView = mViewCache.get(index);
        if (cachedView != null) {
            return cachedView;
        }

        // Look up from the scrap next if there is a matching recycled view
        // to avoid the same view holder is created from the adapter again
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
        for (int i = 0, size = mFlexLines.size(); i < size; i++) {
            FlexLine flexLine = mFlexLines.get(i);
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
    public void updateViewCache(int position, View view) {
        mViewCache.put(position, view);
    }

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
    public Parcelable onSaveInstanceState() {
        if (mPendingSavedState != null) {
            return new SavedState(mPendingSavedState);
        }
        SavedState savedState = new SavedState();
        if (getChildCount() > 0) {
            // TODO: Find the child from end if mFlexWrap == FlexWrap.WRAP_REVERSE
            View firstView = getChildClosestToStart();
            savedState.mAnchorPosition = getPosition(firstView);
            savedState.mAnchorOffset = mOrientationHelper.getDecoratedStart(firstView) -
                    mOrientationHelper.getStartAfterPadding();
        } else {
            savedState.invalidateAnchor();
        }
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            mPendingSavedState = (SavedState) state;
            requestLayout();
            if (DEBUG) {
                Log.d(TAG, "Loaded saved state. " + mPendingSavedState);
            }
        } else {
            if (DEBUG) {
                Log.w(TAG, "Invalid state was trying to be restored. " + state);
            }
        }
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
            Log.d(TAG, "onLayoutChildren started");
            Log.d(TAG, "getChildCount: " + getChildCount());
            Log.d(TAG, "State: " + state);
            Log.d(TAG, "PendingSavedState: " + mPendingSavedState);
            Log.d(TAG, "PendingScrollPosition: " + mPendingScrollPositionOffset);
            Log.d(TAG, "PendingScrollOffset: " + mPendingScrollPositionOffset);
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
        resolveLayoutDirection();
        ensureOrientationHelper();
        ensureLayoutState();
        mFlexboxHelper.ensureMeasureSpecCache(childCount);
        mFlexboxHelper.ensureMeasuredSizeCache(childCount);

        mFlexboxHelper.ensureIndexToFlexLine(childCount);

        mLayoutState.mShouldRecycle = false;

        if (mPendingSavedState != null && mPendingSavedState.hasValidAnchor(childCount)) {
            mPendingScrollPosition = mPendingSavedState.mAnchorPosition;
        }

        if (!mAnchorInfo.mValid || mPendingScrollPosition != NO_POSITION ||
                mPendingSavedState != null) {
            mAnchorInfo.reset();
            updateAnchorInfoForLayout(state, mAnchorInfo);
            mAnchorInfo.mValid = true;
        }
        detachAndScrapAttachedViews(recycler);

        if (mAnchorInfo.mLayoutFromEnd) {
            updateLayoutStateToFillStart(mAnchorInfo, false, true);
        } else {
            updateLayoutStateToFillEnd(mAnchorInfo, false, true);
        }
        if (DEBUG) {
            Log.d(TAG,
                    String.format("onLayoutChildren. recycler.getScrapList.size(): %s, state: %s",
                            recycler.getScrapList().size(), state));
        }

        updateFlexLines(childCount);
        if (DEBUG) {
            for (int i = 0, size = mFlexLines.size(); i < size; i++) {
                FlexLine flexLine = mFlexLines.get(i);
                Log.d(TAG, String.format("%d flex line. MainSize: %d, CrossSize: %d, itemCount: %d",
                        i, flexLine.getMainSize(), flexLine.getCrossSize(),
                        flexLine.getItemCount()));
            }
        }

        int startOffset;
        int endOffset;
        if (mAnchorInfo.mLayoutFromEnd) {
            int filledToEnd = fill(recycler, state, mLayoutState);
            if (DEBUG) {
                Log.d(TAG, String.format("filled: %d toward start", filledToEnd));
            }
            startOffset = mLayoutState.mOffset;
            updateLayoutStateToFillEnd(mAnchorInfo, true, false);
            int filledToStart = fill(recycler, state, mLayoutState);
            if (DEBUG) {
                Log.d(TAG, String.format("filled: %d toward end", filledToStart));
            }
            endOffset = mLayoutState.mOffset;
        } else {
            int filledToEnd = fill(recycler, state, mLayoutState);
            if (DEBUG) {
                Log.d(TAG, String.format("filled: %d toward end", filledToEnd));
            }
            endOffset = mLayoutState.mOffset;
            updateLayoutStateToFillStart(mAnchorInfo, true, false);
            int filledToStart = fill(recycler, state, mLayoutState);
            if (DEBUG) {
                Log.d(TAG, String.format("filled: %d toward start", filledToStart));
            }
            startOffset = mLayoutState.mOffset;
        }

        if (getChildCount() > 0) {
            if (mAnchorInfo.mLayoutFromEnd) {
                int fixOffset = fixLayoutEndGap(endOffset, recycler, state, true);
                startOffset += fixOffset;
                fixLayoutStartGap(startOffset, recycler, state, false);
            } else {
                int fixOffset = fixLayoutStartGap(startOffset, recycler, state, true);
                endOffset += fixOffset;
                fixLayoutEndGap(endOffset, recycler, state, false);
            }
        }
    }

    /**
     * Fill the gap the toward the start position if the gap to be filled is made.
     * Large part is copied from LinearLayoutManager#fixLayoutStartGap.
     */
    private int fixLayoutStartGap(int startOffset, RecyclerView.Recycler recycler,
            RecyclerView.State state, boolean canOffsetChildren) {

        int gap = startOffset - mOrientationHelper.getStartAfterPadding();
        int fixOffset;
        if (gap > 0) {
            // check if we should fix this gap.
            fixOffset = -handleScrolling(gap, recycler, state);
        } else {
            return 0; // nothing to fix
        }
        startOffset += fixOffset;
        if (canOffsetChildren) {
            // re-calculate gap, see if we could fix it
            gap = startOffset - mOrientationHelper.getStartAfterPadding();
            if (gap > 0) {
                mOrientationHelper.offsetChildren(-gap);
                return fixOffset - gap;
            }
        }
        return fixOffset;
    }

    /**
     * Fill the gap the toward the end position if the gap to be filled is made.
     * This process is necessary in a case like {@link #scrollToPosition(int)} is called
     * for the last item, otherwise the last item is placed as the first line.
     * Large part is copied from LinearLayoutManager#fixLayoutEndGap.
     */
    private int fixLayoutEndGap(int endOffset, RecyclerView.Recycler recycler,
            RecyclerView.State state, boolean canOffsetChildren) {
        int gap = mOrientationHelper.getEndAfterPadding() - endOffset;
        int fixOffset;
        if (gap > 0) {
            fixOffset = -handleScrolling(-gap, recycler, state);
        } else {
            return 0; // nothing to fix
        }
        // move offset according to scroll amount
        endOffset += fixOffset;
        if (canOffsetChildren) {
            // re-calculate gap, see if we could fix it
            gap = mOrientationHelper.getEndAfterPadding() - endOffset;
            if (gap > 0) {
                mOrientationHelper.offsetChildren(gap);
                return gap + fixOffset;
            }
        }
        return fixOffset;
    }

    private void updateFlexLines(int childCount) {
        //noinspection ResourceType
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(getWidth(), getWidthMode());
        //noinspection ResourceType
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(getHeight(), getHeightMode());
        FlexboxHelper.FlexLinesResult flexLinesResult;
        int width = getWidth();
        int height = getHeight();
        boolean isMainSizeChanged;
        // Clear the flex lines if the main size has changed from the last measurement.
        // For example this happens when the developer handles the configuration changes manually
        // or the user change the width boundary in the multi window mode.
        if (isMainAxisDirectionHorizontal()) {
            isMainSizeChanged = mLastWidth != Integer.MIN_VALUE && mLastWidth != width;
        } else {
            isMainSizeChanged = mLastHeight != Integer.MIN_VALUE && mLastHeight != height;
        }

        mLastWidth = width;
        mLastHeight = height;
        if (mPendingScrollPosition != NO_POSITION || isMainSizeChanged) {
            if (mAnchorInfo.mLayoutFromEnd) {
                // Prior flex lines should be already calculated, don't have to be updated
                return;
            }

            // TODO: This path may need another consideration to not calculate the entire flex
            // lines prior to the anchor position since it may cause noticeable amount of
            // skipped frames.
            // Another note: deciding anchor position for the anchor view assumes that prior
            // flex lines are calculated otherwise the position of the view can't be decided.
            // It may be possible that assumes the anchor view is always at the start of a flex
            // line and calculate the rest of flex lines as user scrolls to the top (toward the
            // start) incrementally, but that approach may lead to inconsistent anchor view
            // position
            mFlexLines.clear();
            assert mFlexboxHelper.mIndexToFlexLine != null;
            if (isMainAxisDirectionHorizontal()) {
                flexLinesResult = mFlexboxHelper
                        .calculateHorizontalFlexLinesToIndex(widthMeasureSpec, heightMeasureSpec,
                                mLayoutState.mAvailable, mAnchorInfo.mPosition, mFlexLines);
            } else {
                flexLinesResult = mFlexboxHelper
                        .calculateVerticalFlexLinesToIndex(widthMeasureSpec, heightMeasureSpec,
                                mLayoutState.mAvailable, mAnchorInfo.mPosition, mFlexLines);
            }
            mFlexLines = flexLinesResult.mFlexLines;
            mFlexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec);
            mFlexboxHelper.stretchViews();
            mAnchorInfo.mFlexLinePosition =
                    mFlexboxHelper.mIndexToFlexLine[mAnchorInfo.mPosition];
            mLayoutState.mFlexLinePosition = mAnchorInfo.mFlexLinePosition;
        } else {
            // Calculate the flex lines until the calculated cross size reaches the
            // LayoutState#mAvailable (or until the end of the flex container)
            // calculation can be done incrementally because the flex lines prior to the anchor
            // position haven't changed
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
                                    mLayoutState.mAvailable, 0, mFlexLines);
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
                                    mLayoutState.mAvailable, 0, mFlexLines);
                }
            }
            mFlexLines = flexLinesResult.mFlexLines;
            mFlexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec,
                    mAnchorInfo.mPosition);
            // Unlike the FlexboxLayout not calling FlexboxHelper#determineCrossSize because
            // the align content attribute (which is used to determine the cross size) is only
            // effective
            // when the size of flex line is equal or more than 2 and the parent height
            // (length along the cross size) is fixed. But in RecyclerView, these two conditions
            // can't
            // be true at the same time. Because it's scrollable along the cross axis
            // or even if not (when flex wrap is "nowrap") the size of the flex lines should be 1.
            mFlexboxHelper.stretchViews(mAnchorInfo.mPosition);
        }
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
        if (DEBUG) {
            Log.d(TAG, "onLayoutCompleted. " + state);
        }
        mPendingSavedState = null;
        mPendingScrollPosition = NO_POSITION;
        mPendingScrollPositionOffset = INVALID_OFFSET;
        mAnchorInfo.reset();
        mViewCache.clear();
    }

    boolean isLayoutRtl() {
        return mIsRtl;
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
        if (updateAnchorFromPendingState(state, anchorInfo, mPendingSavedState)) {
            if (DEBUG) {
                Log.d(TAG, "updated anchor from the pending state");
            }
            return;
        }
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

    private boolean updateAnchorFromPendingState(RecyclerView.State state, AnchorInfo anchorInfo,
            SavedState savedState) {
        assert mFlexboxHelper.mIndexToFlexLine != null;
        if (state.isPreLayout() || mPendingScrollPosition == NO_POSITION) {
            return false;
        }
        if (mPendingScrollPosition < 0 || mPendingScrollPosition >= state.getItemCount()) {
            mPendingScrollPosition = NO_POSITION;
            mPendingScrollPositionOffset = INVALID_OFFSET;
            if (DEBUG) {
                Log.e(TAG, "ignoring invalid scroll position " + mPendingScrollPosition);
            }
            return false;
        }

        anchorInfo.mPosition = mPendingScrollPosition;
        anchorInfo.mFlexLinePosition = mFlexboxHelper.mIndexToFlexLine[anchorInfo.mPosition];
        if (mPendingSavedState != null && mPendingSavedState.hasValidAnchor(state.getItemCount())) {
            anchorInfo.mCoordinate = mOrientationHelper.getStartAfterPadding() +
                    savedState.mAnchorOffset;
            anchorInfo.mAssignedFromSavedState = true;
            anchorInfo.mFlexLinePosition = NO_POSITION;
            return true;
        }

        if (mPendingScrollPositionOffset == INVALID_OFFSET) {
            View anchorView = findViewByPosition(mPendingScrollPosition);
            if (anchorView != null) {
                if (mOrientationHelper.getDecoratedMeasurement(anchorView) >
                        mOrientationHelper.getTotalSpace()) {
                    anchorInfo.assignCoordinateFromPadding();
                    return true;
                }
                int startGap = mOrientationHelper.getDecoratedStart(anchorView)
                        - mOrientationHelper.getStartAfterPadding();
                if (startGap < 0) {
                    anchorInfo.mCoordinate = mOrientationHelper.getStartAfterPadding();
                    anchorInfo.mLayoutFromEnd = false;
                    return true;
                }

                int endGap = mOrientationHelper.getEndAfterPadding() -
                        mOrientationHelper.getDecoratedEnd(anchorView);
                if (endGap < 0) {
                    anchorInfo.mCoordinate = mOrientationHelper.getEndAfterPadding();
                    anchorInfo.mLayoutFromEnd = true;
                    return true;
                }
                anchorInfo.mCoordinate = anchorInfo.mLayoutFromEnd ?
                        (mOrientationHelper.getDecoratedEnd(anchorView) +
                                mOrientationHelper.getTotalSpaceChange())
                        : mOrientationHelper.getDecoratedStart(anchorView);
            } else {
                if (getChildCount() > 0) {
                    int position = getPosition(getChildAt(0));
                    anchorInfo.mLayoutFromEnd = mPendingScrollPosition < position;
                }
                anchorInfo.assignCoordinateFromPadding();
            }
            return true;
        }

        // TODO: Support reverse layout when flex wrap == FlexWrap.WRAP_REVERSE
        anchorInfo.mCoordinate = mOrientationHelper.getStartAfterPadding()
                + mPendingScrollPositionOffset;
        return true;
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
                ? findLastReferenceChild(state.getItemCount())
                : findFirstReferenceChild(state.getItemCount());
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

    /**
     * Find the reference view to be used as an anchor. It tries to find the view who has the
     * maximum/minimum start/end (differs depending on if the container if RTL and the main axis
     * direction) coordinate in the first visible flex line.
     *
     * @param itemCount the number of the items in this layout including invisible items
     * @return the reference view
     */
    private View findFirstReferenceChild(int itemCount) {
        assert mFlexboxHelper.mIndexToFlexLine != null;
        View firstFound = findReferenceChild(0, getChildCount(), itemCount);
        if (firstFound == null) {
            return null;
        }
        int firstFoundPosition = getPosition(firstFound);
        int firstFoundLinePosition = mFlexboxHelper.mIndexToFlexLine[firstFoundPosition];
        FlexLine firstFoundLine = mFlexLines.get(firstFoundLinePosition);
        return findFirstReferenceViewInLine(firstFound, firstFoundLine);
    }

    /**
     * Find the reference view to be used as an anchor. It tries to find the view who has the
     * maximum/minimum start/end (differs depending on if the container if RTL and the main axis
     * direction) coordinate in the last visible flex line.
     *
     * @param itemCount the number of the items in this layout including invisible items
     * @return the reference view
     */
    private View findLastReferenceChild(int itemCount) {
        assert mFlexboxHelper.mIndexToFlexLine != null;
        View lastFound = findReferenceChild(getChildCount() - 1, -1, itemCount);
        if (lastFound == null) {
            return null;
        }
        int lastFoundPosition = getPosition(lastFound);
        int lastFoundLinePosition = mFlexboxHelper.mIndexToFlexLine[lastFoundPosition];
        FlexLine lastFoundLine = mFlexLines.get(lastFoundLinePosition);
        return findLastReferenceViewInLine(lastFound, lastFoundLine);
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

    private View getChildClosestToStart() {
        // TODO: Find from end when mFlexWrap == FlexWrap.WRAP_REVERSE
        return getChildAt(0);
    }

    private View getChildClosestToEnd() {
        // TODO: Find from end when mFlexWrap == FlexWrap.WRAP_REVERSE
        return getChildAt(getChildCount() - 1);
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
        while ((remainingSpace > 0 || mLayoutState.mInfinite) &&
                layoutState.hasMore(state, mFlexLines)) {
            FlexLine flexLine = mFlexLines.get(layoutState.mFlexLinePosition);
            layoutState.mPosition = flexLine.mFirstIndex;
            consumed += layoutFlexLine(flexLine, layoutState);
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
        if (layoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
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

    private int layoutFlexLine(FlexLine flexLine, LayoutState layoutState) {
        if (isMainAxisDirectionHorizontal()) {
            return layoutFlexLineMainAxisHorizontal(flexLine, layoutState);
        } else {
            return layoutFlexLineMainAxisVertical(flexLine, layoutState);
        }
    }

    private int layoutFlexLineMainAxisHorizontal(FlexLine flexLine, LayoutState layoutState) {
        assert mFlexboxHelper.mMeasureSpecCache != null;

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int parentWidth = getWidth();

        int childTop = layoutState.mOffset;
        if (layoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
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

        // Used only when mLayoutDirection == LayoutState.LAYOUT_START to remember the index
        // a flex item should be inserted
        int indexInFlexLine = 0;
        for (int i = startPosition, itemCount = flexLine.getItemCount();
                i < startPosition + itemCount; i++) {
            View view = getFlexItemAt(i);
            if (view == null) {
                continue;
            }

            if (layoutState.mLayoutDirection == LayoutState.LAYOUT_END) {
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
                view.measure(widthSpec, heightSpec);
            }

            childLeft += (lp.leftMargin + getLeftDecorationWidth(view));
            childRight -= (lp.rightMargin + getRightDecorationWidth(view));

            int topWithDecoration = childTop + getTopDecorationHeight(view);
            if (mIsRtl) {
                mFlexboxHelper.layoutSingleChildHorizontal(view, flexLine,
                        Math.round(childRight) - view.getMeasuredWidth(),
                        topWithDecoration, Math.round(childRight),
                        topWithDecoration + view.getMeasuredHeight());
            } else {
                mFlexboxHelper.layoutSingleChildHorizontal(view, flexLine,
                        Math.round(childLeft), topWithDecoration,
                        Math.round(childLeft) + view.getMeasuredWidth(),
                        topWithDecoration + view.getMeasuredHeight());
            }
            childLeft += (view.getMeasuredWidth() + lp.rightMargin + getRightDecorationWidth(view)
                    + spaceBetweenItem);
            childRight -= (view.getMeasuredWidth() + lp.leftMargin + getLeftDecorationWidth(view)
                    + spaceBetweenItem);

            flexLine.updatePositionFromView(view, getDecoratedLeft(view), getDecoratedTop(view),
                    getDecoratedRight(view), getDecoratedBottom(view));
        }
        layoutState.mFlexLinePosition += mLayoutState.mLayoutDirection;
        return flexLine.getCrossSize();
    }

    private int layoutFlexLineMainAxisVertical(FlexLine flexLine, LayoutState layoutState) {
        assert mFlexboxHelper.mMeasureSpecCache != null;

        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int parentHeight = getHeight();

        // Either childLeft or childRight is used depending on the layoutState.mLayoutDirection
        int childLeft = layoutState.mOffset;
        if (layoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
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

        // Used only when mLayoutDirection == LayoutState.LAYOUT_START to remember the index
        // a flex item should be inserted
        int indexInFlexLine = 0;
        for (int i = startPosition, itemCount = flexLine.getItemCount();
                i < startPosition + itemCount; i++) {
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
                view.measure(widthSpec, heightSpec);
            }

            childTop += (lp.topMargin + getTopDecorationHeight(view));
            childBottom -= (lp.rightMargin + getBottomDecorationHeight(view));

            if (layoutState.mLayoutDirection == LayoutState.LAYOUT_END) {
                addView(view);
            } else {
                addView(view, indexInFlexLine);
                indexInFlexLine++;
            }

            int leftWidthDecoration = childLeft + getLeftDecorationWidth(view);
            if (mFromBottomToTop) {
                mFlexboxHelper.layoutSingleChildVertical(view, flexLine, mIsRtl,
                        leftWidthDecoration, Math.round(childBottom) - view.getMeasuredHeight(),
                        leftWidthDecoration + view.getMeasuredWidth(), Math.round(childBottom));
            } else {
                mFlexboxHelper.layoutSingleChildVertical(view, flexLine, mIsRtl,
                        leftWidthDecoration, Math.round(childTop),
                        leftWidthDecoration + view.getMeasuredWidth(),
                        Math.round(childTop) + view.getMeasuredHeight());
            }
            childTop += (view.getMeasuredHeight() + lp.topMargin + getBottomDecorationHeight(view)
                    + spaceBetweenItem);
            childBottom -= (view.getMeasuredHeight() + lp.bottomMargin +
                    getTopDecorationHeight(view) + spaceBetweenItem);

            flexLine.updatePositionFromView(view, getDecoratedLeft(view), getDecoratedTop(view),
                    getDecoratedRight(view), getDecoratedBottom(view));
        }
        layoutState.mFlexLinePosition += mLayoutState.mLayoutDirection;
        return flexLine.getCrossSize();
    }

    @Override
    public boolean isMainAxisDirectionHorizontal() {
        return mFlexDirection == FlexDirection.ROW || mFlexDirection == FlexDirection.ROW_REVERSE;
    }


    /**
     * Update the layout state based on the anchor information.
     * The view holders are going to be filled toward the end position (bottom if the main axis
     * direction is horizontal, right if the main axis direction if vertical).
     *
     * @param anchorInfo   the anchor information where layout should start
     * @param fromNextLine if set to {@code true}, layout starts from the next flex line set to
     *                     the anchor information
     * @param considerInfinite if set to {@code true}, the judgement if the infinite available space
     *                         needs to be considered.
     */
    private void updateLayoutStateToFillEnd(AnchorInfo anchorInfo, boolean fromNextLine,
            boolean considerInfinite) {
        if (considerInfinite) {
            resolveInfiniteAmount();
        } else {
            mLayoutState.mInfinite = false;
        }
        mLayoutState.mAvailable = mOrientationHelper.getEndAfterPadding() - anchorInfo.mCoordinate;
        mLayoutState.mPosition = anchorInfo.mPosition;
        mLayoutState.mItemDirection = LayoutState.ITEM_DIRECTION_TAIL;
        mLayoutState.mLayoutDirection = LayoutState.LAYOUT_END;
        mLayoutState.mOffset = anchorInfo.mCoordinate;
        mLayoutState.mScrollingOffset = LayoutState.SCROLLING_OFFSET_NaN;
        mLayoutState.mFlexLinePosition = anchorInfo.mFlexLinePosition;

        if (fromNextLine
                && mFlexLines.size() > 1
                && anchorInfo.mFlexLinePosition >= 0
                && anchorInfo.mFlexLinePosition < mFlexLines.size() - 1) {
            FlexLine currentLine = mFlexLines.get(anchorInfo.mFlexLinePosition);
            mLayoutState.mFlexLinePosition++;
            mLayoutState.mPosition += currentLine.getItemCount();
        }
    }

    /**
     * Update the layout state based on the anchor information.
     * The view holders are going to be filled toward the start position (top if the main axis
     * direction is horizontal, left if the main axis direction if vertical).
     *
     * @param anchorInfo       the anchor information where layout should start
     * @param fromPreviousLine if set to {@code true}, layout starts from the next flex line set to
     *                         the anchor information
     * @param considerInfinite if set to {@code true}, the judgement if the infinite available space
     *                         needs to be considered.
     */
    private void updateLayoutStateToFillStart(AnchorInfo anchorInfo, boolean fromPreviousLine,
            boolean considerInfinite) {
        if (considerInfinite) {
            resolveInfiniteAmount();
        } else {
            mLayoutState.mInfinite = false;
        }
        mLayoutState.mAvailable = anchorInfo.mCoordinate - mOrientationHelper
                .getStartAfterPadding();
        mLayoutState.mPosition = anchorInfo.mPosition;
        mLayoutState.mItemDirection = LayoutState.ITEM_DIRECTION_TAIL;
        mLayoutState.mLayoutDirection = LayoutState.LAYOUT_START;
        mLayoutState.mOffset = anchorInfo.mCoordinate;
        mLayoutState.mScrollingOffset = LayoutState.SCROLLING_OFFSET_NaN;
        mLayoutState.mFlexLinePosition = anchorInfo.mFlexLinePosition;

        if (fromPreviousLine && anchorInfo.mFlexLinePosition > 0
                && mFlexLines.size() > anchorInfo.mFlexLinePosition) {
            FlexLine currentLine = mFlexLines.get(anchorInfo.mFlexLinePosition);
            mLayoutState.mFlexLinePosition--;
            mLayoutState.mPosition -= currentLine.getItemCount();
        }
    }

    private void resolveInfiniteAmount() {
        int crossMode;
        if (isMainAxisDirectionHorizontal()) {
            crossMode = getHeightMode();
        } else {
            crossMode = getWidthMode();
        }
        // Setting the infinite flag so that the LayoutManager tries to fill the available space
        // as much as possible. E.g. this is needed in the case RecyclerView is wrapped with another
        // scrollable container (another RecyclerView or ScrollView) on the condition
        // layout_height="wrap_content" and flexDirection="row". In such a case, the height of the
        // inner RecyclerView (attached RecyclerView for this LayoutManager) is set to 0 at this
        // moment, so the value of the mAvailable doesn't have enough value enough to put the
        // already calculated flex lines.
        mLayoutState.mInfinite =
                crossMode == View.MeasureSpec.UNSPECIFIED || crossMode == View.MeasureSpec.AT_MOST;
    }

    private void ensureOrientationHelper() {
        if (mOrientationHelper != null) {
            return;
        }
        // There are two cases for each of main axis direction. In either case the scroll happens
        // along the cross axis:
        // -- Scroll vertically when mFlexWrap != FlexWrap.NOWRAP. In this case scroll happens
        //    along the cross axis
        //
        // When scroll direction is vertical:
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
    public void scrollToPosition(int position) {
        mPendingScrollPosition = position;
        mPendingScrollPositionOffset = INVALID_OFFSET;
        if (mPendingSavedState != null) {
            mPendingSavedState.invalidateAnchor();
        }
        requestLayout();
    }

    /**
     * @return true if LayoutManager will recycle its children when it is detached from
     * RecyclerView.
     */
    @SuppressWarnings("UnusedDeclaration")
    public boolean getRecycleChildrenOnDetach() {
        return mRecycleChildrenOnDetach;
    }

    /**
     * Set whether this LayoutManager will recycle its children when it is detached from
     * RecyclerView.
     * <p>
     * If you are using a {@link RecyclerView.RecycledViewPool}, it might be a good idea to set
     * this flag to <code>true</code> so that views will be available to other RecyclerViews
     * immediately.
     * <p>
     * Note that, setting this flag will result in a performance drop if RecyclerView
     * is restored.
     *
     * @param recycleChildrenOnDetach Whether children should be recycled in detach or not.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setRecycleChildrenOnDetach(boolean recycleChildrenOnDetach) {
        mRecycleChildrenOnDetach = recycleChildrenOnDetach;
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        if (mRecycleChildrenOnDetach) {
            if (DEBUG) {
                Log.d(TAG, "onDetachedFromWindow. Recycling children in the recycler");
            }
            removeAndRecycleAllViews(recycler);
            recycler.clear();
        }
    }

    @Override
    public boolean canScrollHorizontally() {
        return !isMainAxisDirectionHorizontal();
    }

    @Override
    public boolean canScrollVertically() {
        return isMainAxisDirectionHorizontal();
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler,
            RecyclerView.State state) {
        int scrolled = handleScrolling(dx, recycler, state);
        mViewCache.clear();
        return scrolled;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler,
            RecyclerView.State state) {
        int scrolled = handleScrolling(dy, recycler, state);
        mViewCache.clear();
        return scrolled;
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
        int layoutDirection = delta > 0 ? LayoutState.LAYOUT_END : LayoutState.LAYOUT_START;
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

    private void updateLayoutState(int layoutDirection, int absDelta) {
        assert mFlexboxHelper.mIndexToFlexLine != null;
        // TODO: Consider updating LayoutState#mExtra to support better smooth scrolling
        mLayoutState.mLayoutDirection = layoutDirection;
        boolean mainAxisHorizontal = isMainAxisDirectionHorizontal();
        if (layoutDirection == LayoutState.LAYOUT_END) {
            View lastVisible = getChildAt(getChildCount() - 1);
            mLayoutState.mOffset = mOrientationHelper.getDecoratedEnd(lastVisible);
            int lastVisiblePosition = getPosition(lastVisible);
            int lastVisibleLinePosition = mFlexboxHelper.mIndexToFlexLine[lastVisiblePosition];
            FlexLine lastVisibleLine = mFlexLines.get(lastVisibleLinePosition);

            // The reference view which has the maximum end (or minimum if the layout is RTL and
            // the main axis direction is horizontal) coordinate in  the last visible flex line.
            View referenceView = findLastReferenceViewInLine(lastVisible, lastVisibleLine);

            mLayoutState.mOffset = mOrientationHelper.getDecoratedEnd(referenceView);
            mLayoutState.mItemDirection = LayoutState.ITEM_DIRECTION_TAIL;
            mLayoutState.mPosition = lastVisiblePosition + mLayoutState.mItemDirection;
            if (mFlexboxHelper.mIndexToFlexLine.length <= mLayoutState.mPosition) {
                mLayoutState.mFlexLinePosition = NO_POSITION;
            } else {
                mLayoutState.mFlexLinePosition
                        = mFlexboxHelper.mIndexToFlexLine[mLayoutState.mPosition];
            }
            mLayoutState.mScrollingOffset = mOrientationHelper.getDecoratedEnd(referenceView)
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

            // The reference view which has the minimum start (or maximum if the layout is RTL and
            // the main axis direction is horizontal) coordinate in the first visible flex line
            View referenceView = findFirstReferenceViewInLine(firstVisible, firstVisibleLine);

            mLayoutState.mOffset = mOrientationHelper.getDecoratedStart(referenceView);
            mLayoutState.mItemDirection = LayoutState.ITEM_DIRECTION_TAIL;
            int flexLinePosition = mFlexboxHelper.mIndexToFlexLine[firstVisiblePosition];
            if (flexLinePosition == NO_POSITION) {
                flexLinePosition = 0;
            }
            if (flexLinePosition > 0) {
                FlexLine previousLine = mFlexLines.get(flexLinePosition - 1);
                // The position of the next item toward start should be on the next flex line,
                // shifting the position by the number of the items in the previous line.
                mLayoutState.mPosition = firstVisiblePosition - previousLine.getItemCount();
            } else {
                mLayoutState.mPosition = NO_POSITION;
            }
            mLayoutState.mFlexLinePosition = flexLinePosition > 0 ? flexLinePosition - 1 : 0;
            mLayoutState.mScrollingOffset = -mOrientationHelper.getDecoratedStart(referenceView)
                    + mOrientationHelper.getStartAfterPadding();
        }
        mLayoutState.mAvailable = absDelta - mLayoutState.mScrollingOffset;
    }

    /**
     * Loop through the first visible flex line to find the reference view, which has the minimum
     * start (or maximum if the layout is RTL and main axis direction is horizontal) coordinate.
     *
     * @param firstView        the first visible view
     * @param firstVisibleLine the first visible flex line
     * @return the reference view
     */
    private View findFirstReferenceViewInLine(View firstView, FlexLine firstVisibleLine) {
        boolean mainAxisHorizontal = isMainAxisDirectionHorizontal();
        View referenceView = firstView;
        for (int i = 1, to = firstVisibleLine.mItemCount;
                i < to; i++) {
            View viewInSameLine = getChildAt(i);
            if (viewInSameLine == null || viewInSameLine.getVisibility() == View.GONE) {
                continue;
            }
            if (mIsRtl && !mainAxisHorizontal) {
                if (mOrientationHelper.getDecoratedStart(referenceView)
                        < mOrientationHelper.getDecoratedStart(viewInSameLine)) {
                    referenceView = viewInSameLine;
                }
            } else {
                if (mOrientationHelper.getDecoratedStart(referenceView)
                        > mOrientationHelper.getDecoratedStart(viewInSameLine)) {
                    referenceView = viewInSameLine;
                }
            }
        }
        return referenceView;
    }

    /**
     * Loop through the last visible flex line to find the reference view, which has the maximum
     * end (or minimum if the layout is RTL and main axis direction is horizontal) coordinate.
     *
     * @param lastView        the last visible view
     * @param lastVisibleLine the last visible flex line
     * @return the reference view
     */
    private View findLastReferenceViewInLine(View lastView, FlexLine lastVisibleLine) {
        boolean mainAxisHorizontal = isMainAxisDirectionHorizontal();
        View referenceView = lastView;
        for (int i = getChildCount() - 2, to = getChildCount() - lastVisibleLine.mItemCount - 1;
                i > to; i--) {
            View viewInSameLine = getChildAt(i);
            if (viewInSameLine == null || viewInSameLine.getVisibility() == View.GONE) {
                continue;
            }
            if (mIsRtl && !mainAxisHorizontal) {
                // The end edge of the view is left, should be the minimum left edge
                // where the next view should be placed
                if (mOrientationHelper.getDecoratedEnd(referenceView) >
                        mOrientationHelper.getDecoratedEnd(viewInSameLine)) {
                    referenceView = viewInSameLine;
                }
            } else {
                if (mOrientationHelper.getDecoratedEnd(referenceView) <
                        mOrientationHelper.getDecoratedEnd(viewInSameLine)) {
                    referenceView = viewInSameLine;
                }
            }
        }
        return referenceView;
    }

    /**
     * Copied from {@link android.support.v7.widget.RecyclerView.LayoutManager#shouldMeasureChild
     * (View,
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

    private int getChildLeft(View view) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedLeft(view) - params.leftMargin;
    }

    private int getChildRight(View view) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedRight(view) + params.rightMargin;
    }

    private int getChildTop(View view) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedTop(view) - params.topMargin;
    }

    private int getChildBottom(View view) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                view.getLayoutParams();
        return getDecoratedBottom(view) + params.bottomMargin;
    }

    /**
     * @param view the view to be examined if it's visible
     * @param completelyVisible when passed as {@code true}, this method checks if the view bounds
     *                          don't overlap the bounds of the RecyclerView. When passed as
     *                          {@code false}, this method checks if the view bounds are partially
     *                          visible within the RecyclerView.
     * @return if the view passed as an argument is visible (view bounds are within the parent
     *         RecyclerView)
     */
    private boolean isViewVisible(View view, boolean completelyVisible) {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getWidth() - getPaddingRight();
        int bottom = getHeight() - getPaddingBottom();
        int childLeft = getChildLeft(view);
        int childTop = getChildTop(view);
        int childRight = getChildRight(view);
        int childBottom = getChildBottom(view);

        boolean horizontalCompletelyVisible = false;
        boolean horizontalPartiallyVisible = false;
        boolean verticalCompletelyVisible = false;
        boolean verticalPartiallyVisible = false;
        if (left <= childLeft && right >= childRight) {
            horizontalCompletelyVisible = true;
        }
        if (childLeft >= right || childRight >= left) {
            horizontalPartiallyVisible = true;
        }

        if (top <= childTop && bottom >= childBottom) {
            verticalCompletelyVisible = true;
        }
        if (childTop >= bottom || childBottom >= top) {
            verticalPartiallyVisible = true;
        }
        if (completelyVisible) {
            return horizontalCompletelyVisible && verticalCompletelyVisible;
        } else {
            return horizontalPartiallyVisible && verticalPartiallyVisible;
        }
    }

    /**
     * Returns the adapter position of the first visible view. This position does not include
     * adapter changes that were dispatched after the last layout pass.
     *
     * If RecyclerView has item decorators, they will be considered in calculations as well.
     * LayoutManager may pre-cache some views that are not necessarily visible. Those views
     * are ignored in this method.
     *
     * @return The adapter position of the first visible item or {@link RecyclerView#NO_POSITION} if
     * there aren't any visible items.
     * @see #findFirstCompletelyVisibleItemPosition()
     * @see #findLastVisibleItemPosition()
     */
    @SuppressWarnings("WeakerAccess")
    public int findFirstVisibleItemPosition() {
        final View child = findOneVisibleChild(0, getChildCount(), false);
        return child == null ? NO_POSITION : getPosition(child);
    }

    /**
     * Returns the adapter position of the first fully visible view. This position does not include
     * adapter changes that were dispatched after the last layout pass.

     * @return The adapter position of the first fully visible item or
     * {@link RecyclerView#NO_POSITION} if there aren't any visible items.
     * @see #findFirstVisibleItemPosition()
     * @see #findLastCompletelyVisibleItemPosition()
     */
    @SuppressWarnings("WeakerAccess")
    public int findFirstCompletelyVisibleItemPosition() {
        final View child = findOneVisibleChild(0, getChildCount(), true);
        return child == null ? NO_POSITION : getPosition(child);
    }

    /**
     * Returns the adapter position of the last visible view. This position does not include
     * adapter changes that were dispatched after the last layout pass.

     * If RecyclerView has item decorators, they will be considered in calculations as well.
     * LayoutManager may pre-cache some views that are not necessarily visible. Those views
     * are ignored in this method.
     *
     * @return The adapter position of the last visible view or {@link RecyclerView#NO_POSITION} if
     * there aren't any visible items.
     * @see #findLastCompletelyVisibleItemPosition()
     * @see #findFirstVisibleItemPosition()
     */
    @SuppressWarnings("WeakerAccess")
    public int findLastVisibleItemPosition() {
        final View child = findOneVisibleChild(getChildCount() - 1, -1, false);
        return child == null ? NO_POSITION : getPosition(child);
    }

    /**
     * Returns the adapter position of the last fully visible view. This position does not include
     * adapter changes that were dispatched after the last layout pass.

     * @return The adapter position of the last fully visible view or
     * {@link RecyclerView#NO_POSITION} if there aren't any visible items.
     * @see #findLastVisibleItemPosition()
     * @see #findFirstCompletelyVisibleItemPosition()
     */
    @SuppressWarnings("WeakerAccess")
    public int findLastCompletelyVisibleItemPosition() {
        final View child = findOneVisibleChild(getChildCount() - 1, -1, true);
        return child == null ? NO_POSITION : getPosition(child);
    }

    /**
     * Returns the first child that is visible in the provided index range, i.e. either partially or
     * fully visible depending on the arguments provided.
     *
     * @param fromIndex the start index for searching the visible child
     * @param toIndex the last index for searching the visible child
     * @param completelyVisible when passed as {@code true}, this method checks if the view bounds
     *                          don't overlap the bounds of the RecyclerView. When passed as
     *                          {@code false}, this method checks if the view bounds are partially
     *                          visible within the RecyclerView.
     * @return the first child that is visible.
     */
    private View findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible) {
        int next = toIndex > fromIndex ? 1 : -1;
        for (int i = fromIndex; i != toIndex; i += next) {
            View view = getChildAt(i);
            if (isViewVisible(view, completelyVisible)) {
                return view;
            }
        }
        return null;
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
            // Unlike the FlexboxLayout, the order attribute is not supported, we don't calculate
            // the order attribute because preparing the order attribute requires all
            // view holders to be inflated at least once, which is inefficient if the number of
            // items in the adapter is large
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

        private boolean mValid;

        private boolean mAssignedFromSavedState;

        private void reset() {
            mPosition = NO_POSITION;
            mFlexLinePosition = NO_POSITION;
            mCoordinate = INVALID_OFFSET;
            mValid = false;
            mAssignedFromSavedState = false;
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
            mAssignedFromSavedState = false;
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
                    ", mValid=" + mValid +
                    ", mAssignedFromSavedState=" + mAssignedFromSavedState +
                    '}';
        }
    }

    /**
     * Helper class that keeps temporary state while the FlexboxLayoutManager is filling out the
     * empty space.
     */
    private static class LayoutState {

        private final static int SCROLLING_OFFSET_NaN = Integer.MIN_VALUE;

        private static final int LAYOUT_START = -1;
        private static final int LAYOUT_END = 1;

        private static final int ITEM_DIRECTION_TAIL = 1;

        /** Number of pixels that we should fill, in the layout direction. */
        private int mAvailable;

        /** If set to true, the value of {@link #mAvailable} is considered as infinite. */
        private boolean mInfinite;

        // TODO: Add mExtra to support better smooth scrolling

        /** Current position on the flex lines being laid out in the layout call */
        private int mFlexLinePosition;

        /** Current position on the adapter to get the next item. */
        private int mPosition;

        /** Pixel offset where layout should start */
        private int mOffset;

        /**
         * Used when LayoutState is constructed in a scrolling state.
         * It should be set the amount of scrolling we can make without creating a new view.
         * Settings this is required for efficient view recycling.
         */
        private int mScrollingOffset;

        /**
         * The most recent
         * {@link #scrollVerticallyBy(int, RecyclerView.Recycler, RecyclerView.State)} or
         * {@link #scrollHorizontallyBy(int, RecyclerView.Recycler, RecyclerView.State)} amount.
         */
        private int mLastScrollDelta;

        private int mItemDirection = LayoutState.ITEM_DIRECTION_TAIL;

        private int mLayoutDirection = LayoutState.LAYOUT_END;

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

    /**
     * The saved state that needs to be restored after the RecyclerView is recreated.
     */
    private static class SavedState implements Parcelable {

        /** The adapter position of the first visible view */
        private int mAnchorPosition;

        /**
         * The offset of the first visible view.
         * E.g. if this value is set as -30, the fist visible view's top is off screen by 30 pixels
         */
        private int mAnchorOffset;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mAnchorPosition);
            dest.writeInt(this.mAnchorOffset);
        }

        SavedState() {
        }

        private SavedState(Parcel in) {
            this.mAnchorPosition = in.readInt();
            this.mAnchorOffset = in.readInt();
        }

        private SavedState(SavedState savedState) {
            mAnchorPosition = savedState.mAnchorPosition;
            mAnchorOffset = savedState.mAnchorOffset;
        }

        private void invalidateAnchor() {
            mAnchorPosition = NO_POSITION;
        }

        private boolean hasValidAnchor(int itemCount) {
            return mAnchorPosition >= 0 && mAnchorPosition < itemCount;
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        @Override
        public String toString() {
            return "SavedState{" +
                    "mAnchorPosition=" + mAnchorPosition +
                    ", mAnchorOffset=" + mAnchorOffset +
                    '}';
        }
    }
}
