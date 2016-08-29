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

/**
 * An interface that has the common behavior as the flex container such as {@link FlexboxLayout}
 * and {@link FlexboxLayoutManager}.
 */
interface FlexContainer {

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
}
