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

package com.google.android.apps.flexbox;

import com.google.android.flexbox.FlexboxLayout;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v4.view.ViewCompat;
import android.view.View;

/**
 * Entity class representing a single flex item in the demo app.
 */
public class FlexItem implements Parcelable {

    public int index;

    /** Initial width in DP or -1 (MATCH_PARENT) or -2 (WRAP_CONTENT) */
    public int width;

    /** Initial height in DP or -1 (MATCH_PARENT) or -2 (WRAP_CONTENT) */
    public int height;

    public int topMargin;

    public int startMargin;

    public int endMargin;

    public int bottomMargin;

    public int paddingTop;

    public int paddingStart;

    public int paddingEnd;

    public int paddingBottom;

    public int order;

    public float flexGrow;

    public float flexShrink;

    public int alignSelf;

    public float flexBasisPercent;

    /** Minimum width in DP */
    public int minWidth;

    /** Minimum height in DP */
    public int minHeight;

    /** Maximum width in DP */
    public int maxWidth;

    /** Maximum height in DP */
    public int maxHeight;

    public boolean wrapBefore;

    public FlexItem() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.index);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeInt(this.topMargin);
        dest.writeInt(this.startMargin);
        dest.writeInt(this.endMargin);
        dest.writeInt(this.bottomMargin);
        dest.writeInt(this.paddingTop);
        dest.writeInt(this.paddingStart);
        dest.writeInt(this.paddingEnd);
        dest.writeInt(this.paddingBottom);
        dest.writeInt(this.order);
        dest.writeFloat(this.flexGrow);
        dest.writeFloat(this.flexShrink);
        dest.writeInt(this.alignSelf);
        dest.writeFloat(this.flexBasisPercent);
        dest.writeInt(this.minWidth);
        dest.writeInt(this.minHeight);
        dest.writeInt(this.maxWidth);
        dest.writeInt(this.maxHeight);
        dest.writeByte((byte) (wrapBefore ? 1 : 0));
    }

    protected FlexItem(Parcel in) {
        this.index = in.readInt();
        this.width = in.readInt();
        this.height = in.readInt();
        this.topMargin = in.readInt();
        this.startMargin = in.readInt();
        this.endMargin = in.readInt();
        this.bottomMargin = in.readInt();
        this.paddingTop = in.readInt();
        this.paddingStart = in.readInt();
        this.paddingEnd = in.readInt();
        this.paddingBottom = in.readInt();
        this.order = in.readInt();
        this.flexGrow = in.readFloat();
        this.flexShrink = in.readFloat();
        this.alignSelf = in.readInt();
        this.flexBasisPercent = in.readFloat();
        this.minWidth = in.readInt();
        this.minHeight = in.readInt();
        this.maxWidth = in.readInt();
        this.maxHeight = in.readInt();
        this.wrapBefore = in.readByte() != 0;
    }

    public FlexboxLayout.LayoutParams toLayoutParams(Context context) {
        FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                Util.dpToPixel(context, width),
                Util.dpToPixel(context, height));
        lp.order = order;
        lp.flexGrow = flexGrow;
        lp.flexShrink = flexShrink;
        lp.alignSelf = alignSelf;
        lp.flexBasisPercent = flexBasisPercent;
        lp.topMargin = topMargin;
        MarginLayoutParamsCompat.setMarginStart(lp, startMargin);
        MarginLayoutParamsCompat.setMarginEnd(lp, endMargin);
        lp.bottomMargin = bottomMargin;
        lp.minWidth = Util.dpToPixel(context, minWidth);
        lp.minHeight = Util.dpToPixel(context, minHeight);
        lp.maxWidth = Util.dpToPixel(context, maxWidth);
        lp.maxHeight = Util.dpToPixel(context, maxHeight);
        lp.wrapBefore = wrapBefore;
        return lp;
    }

    public static FlexItem fromFlexView(View view, int index) {
        FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) view.getLayoutParams();
        FlexItem flexItem = new FlexItem();
        flexItem.index = index;
        flexItem.order = lp.order;
        flexItem.flexGrow = lp.flexGrow;
        flexItem.flexShrink = lp.flexShrink;
        flexItem.alignSelf = lp.alignSelf;
        flexItem.flexBasisPercent = lp.flexBasisPercent;
        flexItem.width = Util.pixelToDp(view.getContext(), lp.width);
        flexItem.height = Util.pixelToDp(view.getContext(), lp.height);
        flexItem.topMargin = lp.topMargin;
        flexItem.startMargin = MarginLayoutParamsCompat.getMarginStart(lp);
        flexItem.endMargin = MarginLayoutParamsCompat.getMarginEnd(lp);
        flexItem.bottomMargin = lp.bottomMargin;
        flexItem.paddingTop = view.getPaddingTop();
        flexItem.paddingStart = ViewCompat.getPaddingStart(view);
        flexItem.paddingEnd = ViewCompat.getPaddingEnd(view);
        flexItem.paddingBottom = view.getPaddingBottom();
        flexItem.minWidth = Util.pixelToDp(view.getContext(), lp.minWidth);
        flexItem.minHeight = Util.pixelToDp(view.getContext(), lp.minHeight);
        flexItem.maxWidth = Util.pixelToDp(view.getContext(), lp.maxWidth);
        flexItem.maxHeight = Util.pixelToDp(view.getContext(), lp.maxHeight);
        flexItem.wrapBefore = lp.wrapBefore;
        return flexItem;
    }

    public static final Creator<FlexItem> CREATOR = new Creator<FlexItem>() {
        @Override
        public FlexItem createFromParcel(Parcel source) {
            return new FlexItem(source);
        }

        @Override
        public FlexItem[] newArray(int size) {
            return new FlexItem[size];
        }
    };
}
