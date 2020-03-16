/*
 * Copyright 2017 Google Inc. All rights reserved.
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

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * {@link RecyclerView.ItemDecoration} implementation that can be used as item decorations between
 * view holders within the {@link FlexboxLayoutManager}.
 *
 * Orientation for the decoration can be either of:
 * <ul>
 * <li>Horizontal (setOrientation(HORIZONTAL)</li>
 * <li>Vertical (setOrientation(VERTICAL)</li>
 * <li>Both orientation (setOrientation(BOTH)</li>
 * </ul>.
 * The default value is set to both.
 */
public class FlexboxItemDecoration extends RecyclerView.ItemDecoration {

    public static final int HORIZONTAL = 1;
    public static final int VERTICAL = 1 << 1;
    @SuppressWarnings("WeakerAccess")
    public static final int BOTH = HORIZONTAL | VERTICAL;

    private static final int[] LIST_DIVIDER_ATTRS = new int[]{android.R.attr.listDivider};

    private Drawable mDrawable;

    private int mOrientation;

    public FlexboxItemDecoration(Context context) {
        final TypedArray a = context.obtainStyledAttributes(LIST_DIVIDER_ATTRS);
        mDrawable = a.getDrawable(0);
        a.recycle();
        setOrientation(BOTH);
    }

    /**
     * Set the drawable used as the item decoration.
     * If the drawable is not set, the default list divider is used as the
     * item decoration.
     */
    public void setDrawable(Drawable drawable) {
        if (drawable == null) {
            throw new IllegalArgumentException("Drawable cannot be null.");
        }
        mDrawable = drawable;
    }

    /**
     * Set the orientation for the decoration.
     * Orientation for the decoration can be either of:
     * <ul>
     * <li>Horizontal (setOrientation(HORIZONTAL)</li>
     * <li>Vertical (setOrientation(VERTICAL)</li>
     * <li>Both orientation (setOrientation(BOTH)</li>
     * </ul>.
     */
    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    @Override
    public void onDraw(
            @NonNull Canvas canvas,
            @NonNull RecyclerView parent,
            @NonNull RecyclerView.State state) {
        drawHorizontalDecorations(canvas, parent);
        drawVerticalDecorations(canvas, parent);
    }

    @Override
    public void getItemOffsets(
            @NonNull Rect outRect,
            @NonNull View view,
            RecyclerView parent,
            @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position == 0) {
            return;
        }
        if (!needsHorizontalDecoration() && !needsVerticalDecoration()) {
            outRect.set(0, 0, 0, 0);
            return;
        }
        FlexboxLayoutManager layoutManager = (FlexboxLayoutManager) parent.getLayoutManager();
        List<FlexLine> flexLines = layoutManager.getFlexLines();
        int flexDirection = layoutManager.getFlexDirection();
        setOffsetAlongMainAxis(outRect, position, layoutManager, flexLines, flexDirection);
        setOffsetAlongCrossAxis(outRect, position, layoutManager, flexLines);
    }

    private void setOffsetAlongCrossAxis(Rect outRect, int position,
            FlexboxLayoutManager layoutManager, List<FlexLine> flexLines) {
        if (flexLines.size() == 0) {
            return;
        }
        int flexLineIndex = layoutManager.getPositionToFlexLineIndex(position);
        if (flexLineIndex == 0) {
            return;
        }

        if (layoutManager.isMainAxisDirectionHorizontal()) {
            if (!needsHorizontalDecoration()) {
                outRect.top = 0;
                outRect.bottom = 0;
                return;
            }
            outRect.top = mDrawable.getIntrinsicHeight();
            outRect.bottom = 0;
        } else {
            if (!needsVerticalDecoration()) {
                return;
            }
            if (layoutManager.isLayoutRtl()) {
                outRect.right = mDrawable.getIntrinsicWidth();
                outRect.left = 0;
            } else {
                outRect.left = mDrawable.getIntrinsicWidth();
                outRect.right = 0;
            }
        }

    }

    private void setOffsetAlongMainAxis(Rect outRect, int position,
            FlexboxLayoutManager layoutManager, List<FlexLine> flexLines, int flexDirection) {
        if (isFirstItemInLine(position, flexLines, layoutManager)) {
            return;
        }

        if (layoutManager.isMainAxisDirectionHorizontal()) {
            if (!needsVerticalDecoration()) {
                outRect.left = 0;
                outRect.right = 0;
                return;
            }
            if (layoutManager.isLayoutRtl()) {
                outRect.right = mDrawable.getIntrinsicWidth();
                outRect.left = 0;
            } else {
                outRect.left = mDrawable.getIntrinsicWidth();
                outRect.right = 0;
            }
        } else {
            if (!needsHorizontalDecoration()) {
                outRect.top = 0;
                outRect.bottom = 0;
                return;
            }
            if (flexDirection == FlexDirection.COLUMN_REVERSE) {
                outRect.bottom = mDrawable.getIntrinsicHeight();
                outRect.top = 0;
            } else {
                outRect.top = mDrawable.getIntrinsicHeight();
                outRect.bottom = 0;
            }
        }

    }

    private void drawVerticalDecorations(Canvas canvas, RecyclerView parent) {
        if (!needsVerticalDecoration()) {
            return;
        }
        FlexboxLayoutManager layoutManager = (FlexboxLayoutManager) parent.getLayoutManager();
        int parentTop = parent.getTop() - parent.getPaddingTop();
        int parentBottom = parent.getBottom() + parent.getPaddingBottom();
        int childCount = parent.getChildCount();
        int flexDirection = layoutManager.getFlexDirection();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();

            int left, right;
            if (layoutManager.isLayoutRtl()) {
                left = child.getRight() + lp.rightMargin;
                right = left + mDrawable.getIntrinsicWidth();
            } else {
                right = child.getLeft() - lp.leftMargin;
                left = right - mDrawable.getIntrinsicWidth();
            }

            int top, bottom;
            if (layoutManager.isMainAxisDirectionHorizontal()) {
                top = child.getTop() - lp.topMargin;
                bottom = child.getBottom() + lp.bottomMargin;
            } else {
                if (flexDirection == FlexDirection.COLUMN_REVERSE) {
                    bottom = child.getBottom() + lp.bottomMargin + mDrawable.getIntrinsicHeight();
                    bottom = Math.min(bottom, parentBottom);
                    top = child.getTop() - lp.topMargin;
                } else {
                    top = child.getTop() - lp.topMargin - mDrawable.getIntrinsicHeight();
                    top = Math.max(top, parentTop);
                    bottom = child.getBottom() + lp.bottomMargin;
                }
            }

            mDrawable.setBounds(left, top, right, bottom);
            mDrawable.draw(canvas);
        }
    }

    private void drawHorizontalDecorations(Canvas canvas, RecyclerView parent) {
        if (!needsHorizontalDecoration()) {
            return;
        }
        FlexboxLayoutManager layoutManager = (FlexboxLayoutManager) parent.getLayoutManager();
        int flexDirection = layoutManager.getFlexDirection();
        int parentLeft = parent.getLeft() - parent.getPaddingLeft();
        int parentRight = parent.getRight() + parent.getPaddingRight();
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top, bottom;
            if (flexDirection == FlexDirection.COLUMN_REVERSE) {
                top = child.getBottom() + lp.bottomMargin;
                bottom = top + mDrawable.getIntrinsicHeight();
            } else {
                bottom = child.getTop() - lp.topMargin;
                top = bottom - mDrawable.getIntrinsicHeight();
            }

            int left, right;
            if (layoutManager.isMainAxisDirectionHorizontal()) {
                if (layoutManager.isLayoutRtl()) {
                    right = child.getRight() + lp.rightMargin + mDrawable.getIntrinsicWidth();
                    right = Math.min(right, parentRight);
                    left = child.getLeft() - lp.leftMargin;
                } else {
                    left = child.getLeft() - lp.leftMargin - mDrawable.getIntrinsicWidth();
                    left = Math.max(left, parentLeft);
                    right = child.getRight() + lp.rightMargin;
                }
            } else {
                left = child.getLeft() - lp.leftMargin;
                right = child.getRight() + lp.rightMargin;
            }
            mDrawable.setBounds(left, top, right, bottom);
            mDrawable.draw(canvas);
        }
    }

    private boolean needsHorizontalDecoration() {
        return (mOrientation & HORIZONTAL) > 0;
    }

    private boolean needsVerticalDecoration() {
        return (mOrientation & VERTICAL) > 0;
    }

    /**
     * @return {@code true} if the given position is the first item in a flex line.
     */
    private boolean isFirstItemInLine(int position, List<FlexLine> flexLines,
            FlexboxLayoutManager layoutManager) {
        int flexLineIndex = layoutManager.getPositionToFlexLineIndex(position);
        if (flexLineIndex != NO_POSITION &&
                flexLineIndex < layoutManager.getFlexLinesInternal().size() &&
                layoutManager.getFlexLinesInternal().get(flexLineIndex).mFirstIndex == position) {
            return true;
        }
        if (position == 0) {
            return true;
        }
        if (flexLines.size() == 0) {
            return false;
        }
        // Check if the position is the "lastIndex + 1" of the last line in case the FlexLine which
        // has the View, whose index is position is not included in the flexLines. (E.g. flexLines
        // is being calculated
        FlexLine lastLine = flexLines.get(flexLines.size() - 1);
        return lastLine.mLastIndex == position - 1;
    }
}
