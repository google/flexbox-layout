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
     * Holds reordered indices, which {@link FlexboxLayout.LayoutParams#order} parameters are taken
     * into account
     */
    int[] mReorderedIndices;

    /**
     * Caches the {@link FlexboxLayout.LayoutParams#order} attributes for children views.
     * Key: the index of the view ({@link #mReorderedIndices} isn't taken into account)
     * Value: the value for the order attribute
     */
    SparseIntArray mOrderCache;

    FlexboxHelper(FlexContainer flexContainer) {
        mFlexContainer = flexContainer;
    }

    /**
     * Create an array, which indicates the reordered indices that
     * {@link FlexboxLayout.LayoutParams#order} attributes are taken into account.
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
        int childCount = mFlexContainer.getChildCount();
        List<Order> orders = createOrders(childCount);
        Order orderForViewToBeAdded = new Order();
        if (viewBeforeAdded != null
                && paramsForViewBeforeAdded instanceof FlexboxLayout.LayoutParams) {
            orderForViewToBeAdded.order = ((FlexboxLayout.LayoutParams)
                    paramsForViewBeforeAdded).order;
        } else {
            orderForViewToBeAdded.order = FlexboxLayout.LayoutParams.ORDER_DEFAULT;
        }

        if (indexForViewBeforeAdded == -1 || indexForViewBeforeAdded == childCount) {
            orderForViewToBeAdded.index = childCount;
        } else if (indexForViewBeforeAdded < mFlexContainer.getChildCount()) {
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
     * {@link FlexboxLayout.LayoutParams#order} attributes are taken into account.
     *
     * @return @return an array which have the reordered indices
     */
    int[] createReorderedIndices() {
        int childCount = mFlexContainer.getChildCount();
        List<Order> orders = createOrders(childCount);
        return sortOrdersIntoReorderedIndices(childCount, orders);
    }


    @NonNull
    private List<Order> createOrders(int childCount) {
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            View child = mFlexContainer.getChildAt(i);
            FlexboxLayout.LayoutParams params = (FlexboxLayout.LayoutParams) child
                    .getLayoutParams();
            Order order = new Order();
            order.order = params.order;
            order.index = i;
            orders.add(order);
        }
        return orders;
    }

    /**
     * Returns if any of the children's {@link FlexboxLayout.LayoutParams#order} attributes are
     * changed from the last measurement.
     *
     * @return {@code true} if changed from the last measurement, {@code false} otherwise.
     */
    boolean isOrderChangedFromLastMeasurement() {
        int childCount = mFlexContainer.getChildCount();
        if (mOrderCache == null) {
            mOrderCache = new SparseIntArray(childCount);
        }
        if (mOrderCache.size() != childCount) {
            return true;
        }
        for (int i = 0; i < childCount; i++) {
            View view = mFlexContainer.getChildAt(i);
            if (view == null) {
                continue;
            }
            FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) view.getLayoutParams();
            if (lp.order != mOrderCache.get(i)) {
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
}
