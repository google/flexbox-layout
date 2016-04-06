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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Entity class representing a single flex item in the demo app.
 */
public class FlexItem implements Parcelable {

    public int index;
    /** minimum width in DP or -1 (MATCH_PARENT) or -2 (WRAP_CONTENT) */
    public int minWidth;
    /** minimum height in DP or -1 (MATCH_PARENT) or -2 (WRAP_CONTENT) */
    public int minHeight;

    public int topMargin;
    public int startMargin;
    public int endMargin;
    public int bottomMargin;

    public int paddingTop;
    public int paddingStart;
    public int paddingEnd;
    public int paddingBottom;

    public int order;
    public int flexGrow;
    public int flexShrink;
    public int alignSelf;

    public FlexItem() {}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.index);
        dest.writeInt(this.minWidth);
        dest.writeInt(this.minHeight);
        dest.writeInt(this.topMargin);
        dest.writeInt(this.startMargin);
        dest.writeInt(this.endMargin);
        dest.writeInt(this.bottomMargin);
        dest.writeInt(this.paddingTop);
        dest.writeInt(this.paddingStart);
        dest.writeInt(this.paddingEnd);
        dest.writeInt(this.paddingBottom);
        dest.writeInt(this.order);
        dest.writeInt(this.flexGrow);
        dest.writeInt(this.flexShrink);
        dest.writeInt(this.alignSelf);
    }

    protected FlexItem(Parcel in) {
        this.index = in.readInt();
        this.minWidth = in.readInt();
        this.minHeight = in.readInt();
        this.topMargin = in.readInt();
        this.startMargin = in.readInt();
        this.endMargin = in.readInt();
        this.bottomMargin = in.readInt();
        this.paddingTop = in.readInt();
        this.paddingStart = in.readInt();
        this.paddingEnd = in.readInt();
        this.paddingBottom = in.readInt();
        this.order = in.readInt();
        this.flexGrow = in.readInt();
        this.flexShrink = in.readInt();
        this.alignSelf = in.readInt();
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
