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
public interface FlexContainer {

    /**
     * @return the number of child count contained in the flex container.
     */
    int getChildCount();

    /**
     * Returns the view at the given index.
     *
     * @param i the index
     * @return the view at the index
     */
    View getChildAt(int i);


    /**
     * Returns a View, which is reordered by taking the order attribute into account.
     *
     * @param i the index of the view
     * @return the reordered view, which order attribute is taken into account.
     * If the index is negative or out of bounds of the number of contained views,
     * returns {@code null}.
     * @see FlexItem#getOrder()
     */
    View getReorderedChildAt(int i);

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
}
