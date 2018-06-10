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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * This attribute controls the alignment along the cross axis.
 * The alignment in the same direction can be determined by the {@link AlignItems} attribute in the
 * parent, but if this is set to other than {@link AlignSelf#AUTO},
 * the cross axis alignment is overridden for this child.
 */
@IntDef({AlignItems.FLEX_START, AlignItems.FLEX_END, AlignItems.CENTER,
        AlignItems.BASELINE, AlignItems.STRETCH, AlignSelf.AUTO})
@Retention(RetentionPolicy.SOURCE)
public @interface AlignSelf {

    /**
     * The default value for the AlignSelf attribute, which means use the inherit
     * the {@link AlignItems} attribute from its parent.
     */
    int AUTO = -1;

    /** This item's edge is placed on the cross start line. */
    int FLEX_START = AlignItems.FLEX_START;

    /** This item's edge is placed on the cross end line. */
    int FLEX_END = AlignItems.FLEX_END;

    /** This item's edge is centered along the cross axis. */
    int CENTER = AlignItems.CENTER;

    /** This items is aligned based on their text's baselines. */
    int BASELINE = AlignItems.BASELINE;

    /** This item is stretched to fill the flex line's cross size. */
    int STRETCH = AlignItems.STRETCH;
}
