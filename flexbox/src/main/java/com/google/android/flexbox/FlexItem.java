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

import android.os.Parcelable;
import android.support.v4.view.ViewCompat;

/**
 * An interface that has the common behavior as a flex item contained in a flex container.
 * Known classes that implement this interface are {@link FlexboxLayout.LayoutParams} and
 * {@link FlexboxLayoutManager.LayoutParams}.
 */
interface FlexItem extends Parcelable {

    /** The default value for the order attribute */
    int ORDER_DEFAULT = 1;

    /** The default value for the flex grow attribute */
    float FLEX_GROW_DEFAULT = 0f;

    /** The default value for the flex shrink attribute */
    float FLEX_SHRINK_DEFAULT = 1f;

    /** The default value for the flex basis percent attribute */
    float FLEX_BASIS_PERCENT_DEFAULT = -1f;

    /** The maximum size of the max width and max height attributes */
    int MAX_SIZE = Integer.MAX_VALUE & ViewCompat.MEASURED_SIZE_MASK;

    /**
     * @return the width attribute of the flex item.
     *
     * The attribute is about how wide the view wants to be. Can be one of the
     * constants MATCH_PARENT(-1) or WRAP_CONTENT(-2), or an exact size.
     */
    int getWidth();

    /**
     * Sets the width attribute of the flex item.
     *
     * @param width the width attribute. Can be one of the
     * constants MATCH_PARENT(-1) or WRAP_CONTENT(-2), or an exact size.
     */
    void setWidth(int width);

    /**
     * @return the height attribute of the flex item.
     *
     * The attribute is about how wide the view wants to be. Can be one of the
     * constants MATCH_PARENT(-1) or WRAP_CONTENT(-2), or an exact size.
     */
    int getHeight();

    /**
     * Sets the height attribute of the flex item.
     *
     * @param height the height attribute. Can be one of the
     * constants MATCH_PARENT(-1) or WRAP_CONTENT(-2), or an exact size.
     */
    void setHeight(int height);

    /**
     * @return the order attribute of the flex item.
     *
     * The attribute can change the ordering of the children views are laid out.
     * By default, children are displayed and laid out in the same order as they appear in the
     * layout XML. If not specified, {@link #ORDER_DEFAULT} is set as a default value.
     */
    int getOrder();

    /**
     * Sets the order attribute to the flex item
     *
     * @param order the order attribute
     */
    void setOrder(int order);

    /**
     * @return the flex grow attribute of the flex item
     *
     * The attribute determines how much this child will grow if positive free space is
     * distributed relative to the rest of other flex items included in the same flex line.
     * If not specified, {@link #FLEX_GROW_DEFAULT} is set as a default value.
     */
    float getFlexGrow();

    /**
     * Sets the flex grow attribute to the flex item
     *
     * @param flexGrow the flex grow attribute
     */
    void setFlexGrow(float flexGrow);

    /**
     * @return the flex shrink attribute of the flex item
     *
     * The attributes determines how much this child will shrink is negative free space is
     * distributed relative to the rest of other flex items included in the same flex line.
     * If not specified, {@link #FLEX_SHRINK_DEFAULT} is set as a default value.
     */
    float getFlexShrink();

    /**
     * Sets the flex shrink attribute to the flex item
     *
     * @param flexShrink the flex shrink attribute
     */
    void setFlexShrink(float flexShrink);

    /**
     * @return the align self attribute of the flex item
     *
     * The attribute determines the alignment along the cross axis (perpendicular to the
     * main axis). The alignment in the same direction can be determined by the
     * align items attribute in the parent, but if this is set to other than
     * {@link AlignSelf#AUTO}, the cross axis alignment is overridden for this child.
     * The value needs to be one of the values in ({@link AlignSelf#AUTO},
     * {@link AlignItems#STRETCH}, {@link AlignItems#FLEX_START}, {@link
     * AlignItems#FLEX_END}, {@link AlignItems#CENTER}, or {@link AlignItems#BASELINE}).
     * If not specified, {@link AlignSelf#AUTO} is set as a default value.
     */
    @AlignSelf
    int getAlignSelf();

    /**
     * Sets the align self attribute to the flex item
     *
     * @param alignSelf the order attribute
     */
    void setAlignSelf(@AlignSelf int alignSelf);

    /**
     * @return the minimum width attribute of the flex item
     *
     * The attribute determines the minimum width the child can shrink to.
     */
    int getMinWidth();

    /**
     * Sets the minimum width attribute to the flex item
     *
     * @param minWidth the order attribute
     */
    void setMinWidth(int minWidth);

    /**
     * @return the minimum height attribute of the flex item
     *
     * The attribute determines the minimum height the child can shrink to.
     */
    int getMinHeight();

    /**
     * Sets the minimum height attribute to the flex item
     *
     * @param minHeight the order attribute
     */
    void setMinHeight(int minHeight);

    /**
     * @return the maximum width attribute of the flex item
     *
     * The attribute determines the maximum width the child can expand to.
     */
    int getMaxWidth();

    /**
     * Sets the maximum width attribute to the flex item
     *
     * @param maxWidth the order attribute
     */
    void setMaxWidth(int maxWidth);

    /**
     * @return the maximum height attribute of the flex item
     */
    int getMaxHeight();

    /**
     * Sets the maximum height attribute to the flex item
     *
     * @param maxHeight the order attribute
     */
    void setMaxHeight(int maxHeight);

    /**
     * @return the wrapBefore attribute of the flex item
     *
     * The attribute forces a flex line wrapping. i.e. if this is set to {@code true} for a
     * flex item, the item will become the first item of the new flex line. (A wrapping happens
     * regardless of the flex items being processed in the the previous flex line)
     * This attribute is ignored if the flex_wrap attribute is set as nowrap.
     * The equivalent attribute isn't defined in the original CSS Flexible Box Module
     * specification, but having this attribute is useful for Android developers to flatten
     * the layouts when building a grid like layout or for a situation where developers want
     * to put a new flex line to make a semantic difference from the previous one, etc.
     */
    boolean isWrapBefore();

    /**
     * Sets the wrapBefore attribute to the flex item
     *
     * @param wrapBefore the order attribute
     */
    void setWrapBefore(boolean wrapBefore);

    /**
     * @return the flexBasisPercent attribute of the flex item
     *
     * The attribute determines the initial flex item length in a fraction format relative to its
     * parent.
     * The initial main size of this child View is trying to be expanded as the specified
     * fraction against the parent main size.
     * If this value is set, the length specified from layout_width
     * (or layout_height) is overridden by the calculated value from this attribute.
     * This attribute is only effective when the parent's MeasureSpec mode is
     * MeasureSpec.EXACTLY. The de
     */
    float getFlexBasisPercent();

    /**
     * Sets the flex basis percent attribute to the flex item
     *
     * @param flexBasisPercent the order attribute
     */
    void setFlexBasisPercent(float flexBasisPercent);

    /**
     * @return the left margin of the flex item.
     */
    int getMarginLeft();

    /**
     * @return the top margin of the flex item.
     */
    int getMarginTop();

    /**
     * @return the right margin of the flex item.
     */
    int getMarginRight();

    /**
     * @return the bottom margin of the flex item.
     */
    int getMarginBottom();

    /**
     * @return the start margin of the flex item depending on its resolved layout direction.
     */
    int getMarginStart();

    /**
     * @return the end margin of the flex item depending on its resolved layout direction.
     */
    int getMarginEnd();
}
