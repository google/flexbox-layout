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
 * This attribute controls whether the flex container is single-line or multi-line, and the
 * direction of the cross axis.
 */
@IntDef({FlexWrap.NOWRAP, FlexWrap.WRAP, FlexWrap.WRAP_REVERSE})
@Retention(RetentionPolicy.SOURCE)
public @interface FlexWrap {

    /** The flex container is single-line. */
    int NOWRAP = 0;

    /** The flex container is multi-line. */
    int WRAP = 1;

    /**
     * The flex container is multi-line. The direction of the
     * cross axis is opposed to the direction as the {@link #WRAP}
     */
    int WRAP_REVERSE = 2;
}
