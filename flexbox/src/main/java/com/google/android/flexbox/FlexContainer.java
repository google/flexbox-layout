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

import android.view.View;

import java.util.List;

/**
 * An interface that has the common behavior as the flex container such as {@link FlexboxLayout}
 * and {@link FlexboxLayoutManager}.
 */
interface FlexContainer {

    /**
     * @return the number of flex items contained in the flex container.
     */
    int getFlexItemCount();

    /**
     * Returns a flex item as a View at the given index.
     *
     * @param index the index
     * @return the view at the index
     */
    View getFlexItemAt(int index);

    /**
     * Returns a flex item as a View, which is reordered by taking the order attribute into
     * account.
     *
     * @param index the index of the view
     * @return the reordered view, which order attribute is taken into account.
     * If the index is negative or out of bounds of the number of contained views,
     * returns {@code null}.
     * @see FlexItem#getOrder()
     */
    View getReorderedFlexItemAt(int index);

    /**
     * Adds the view to the flex container as a flex item.
     *
     * @param view the view to be added
     */
    void addView(View view);

    /**
     * Adds the view to the specified index of the flex container.
     *
     * @param view  the view to be added
     * @param index the index for the view to be added
     */
    void addView(View view, int index);

    /**
     * Removes all the views contained in the flex container.
     */
    void removeAllViews();

    /**
     * Removes the view at the specified index.
     *
     * @param index the index from which the view is removed.
     */
    void removeViewAt(int index);

    /**
     * @return the flex direction attribute of the flex container.
     * @see FlexDirection
     */
    @FlexDirection
    int getFlexDirection();

    /**
     * Sets the given flex direction attribute to the flex container.
     *
     * @param flexDirection the flex direction value
     * @see FlexDirection
     */
    void setFlexDirection(@FlexDirection int flexDirection);

    /**
     * @return the flex wrap attribute of the flex container.
     * @see FlexWrap
     */
    @FlexWrap
    int getFlexWrap();

    /**
     * Sets the given flex wrap attribute to the flex container.
     *
     * @param flexWrap the flex wrap value
     * @see FlexWrap
     */
    void setFlexWrap(@FlexWrap int flexWrap);

    /**
     * @return the justify content attribute of the flex container.
     * @see JustifyContent
     */
    @JustifyContent
    int getJustifyContent();

    /**
     * Sets the given justify content attribute to the flex container.
     *
     * @param justifyContent the justify content value
     * @see JustifyContent
     */
    void setJustifyContent(@JustifyContent int justifyContent);

    /**
     * @return the align content attribute of the flex container.
     * @see AlignContent
     */
    @AlignContent
    int getAlignContent();

    /**
     * Sets the given align content attribute to the flex container.
     *
     * @param alignContent the align content value
     */
    void setAlignContent(@AlignContent int alignContent);

    /**
     * @return the align items attribute of the flex container.
     * @see AlignItems
     */
    @AlignItems
    int getAlignItems();

    /**
     * Sets the given align items attribute to the flex container.
     *
     * @param alignItems the align items value
     * @see AlignItems
     */
    void setAlignItems(@AlignItems int alignItems);

    /**
     * @return the flex lines composing this flex container. The overridden method should return a
     * copy of the original list excluding a dummy flex line (flex line that doesn't have any flex
     * items in it but used for the alignment along the cross axis) so that any changes of the
     * returned list are not reflected to the original list.
     */
    List<FlexLine> getFlexLines();

    /**
     * Returns the length of decoration (such as dividers) of the flex item
     *
     * @param childAbsoluteIndex           the absolute index of the flex item within the flex
     *                                     container
     * @param childRelativeIndexInFlexLine the relative index of the flex item within the flex line
     * @param flexItem                     the flex item from which the lenght of the decoration is
     *                                     calculated
     * @return the length of the decoration. Note that the length of the flex item itself is not
     * included in the result.
     */
    int getDecorationLength(int childAbsoluteIndex,
            int childRelativeIndexInFlexLine, FlexItem flexItem);

    /**
     * @return the top padding of the flex container.
     */
    int getPaddingTop();

    /**
     * @return the left padding of the flex container.
     */
    int getPaddingLeft();

    /**
     * @return the right padding of the flex container.
     */
    int getPaddingRight();

    /**
     * @return the bottom padding of the flex container.
     */
    int getPaddingBottom();

    /**
     * @return the start padding of this view depending on its resolved layout direction.
     */
    int getPaddingStart();

    /**
     * @return the end padding of this view depending on its resolved layout direction.
     */
    int getPaddingEnd();

    /**
     * Returns the child measure spec for its width.
     *
     * @param widthSpec      the measure spec for the width imposed by the parent
     * @param padding        the padding along the width for the parent
     * @param childDimension the value of the child dimension
     */
    int getChildWidthMeasureSpec(int widthSpec, int padding, int childDimension);

    /**
     * Returns the child measure spec for its height.
     *
     * @param heightSpec     the measure spec for the height imposed by the parent
     * @param padding        the padding along the height for the parent
     * @param childDimension the value of the child dimension
     */
    int getChildHeightMeasureSpec(int heightSpec, int padding, int childDimension);

    /**
     * @return the largest main size of all flex lines including decorator lengths.
     */
    int getLargestMainSize();

    /**
     * @return the sum of the cross sizes of all flex lines including decorator lengths.
     */
    int getSumOfCrossSize();

    /**
     * Callback when a new flex item is added to the current container
     *
     * @param index           the absolute index of the flex item added
     * @param indexInFlexLine the relative index of the flex item added within the flex line
     * @param flexLine        the flex line where the new flex item is added
     */
    void onNewFlexItemAdded(int index, int indexInFlexLine, FlexLine flexLine);

    /**
     * Callback when a new flex line is added to the current container
     *
     * @param flexLine the new added flex line
     */
    void onNewFlexLineAdded(FlexLine flexLine);

    /**
     * Sets the list of the flex lines that compose the flex container to the one received as an
     * argument.
     *
     * @param flexLines the list of flex lines
     */
    void setFlexLines(List<FlexLine> flexLines);

    /**
     * @return the list of the flex lines including dummy flex lines (flex line that doesn't have
     * any flex items in it but used for the alignment along the cross axis), which aren't included
     * in the {@link FlexContainer#getFlexLines()}.
     */
    List<FlexLine> getFlexLinesInternal();
}
