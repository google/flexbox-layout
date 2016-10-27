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

import java.util.List;

/**
 * Interface for a flex container whose methods are not exposed outside of the flexbox package.
 */
interface FlexContainerInternal extends FlexContainer {

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
