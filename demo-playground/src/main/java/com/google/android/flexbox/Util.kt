/*
 * Copyright 2017 Google Inc. All rights reserved.
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

package com.google.android.flexbox

import android.content.Context

/**
 * Utilities class.
 */
internal object Util {

    /**
     * Convert pixel to dp. Preserve the negative value as it's used for representing
     * MATCH_PARENT(-1) and WRAP_CONTENT(-2).
     * Ignore the round error that might happen in dividing the pixel by the density.

     * @param context the context
     * *
     * @param pixel   the value in pixel
     * *
     * @return the converted value in dp
     */
    fun pixelToDp(context: Context, pixel: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return if (pixel < 0) pixel else Math.round(pixel / displayMetrics.density)
    }

    /**
     * Convert dp to pixel. Preserve the negative value as it's used for representing
     * MATCH_PARENT(-1) and WRAP_CONTENT(-2).

     * @param context the context
     * *
     * @param dp      the value in dp
     * *
     * @return the converted value in pixel
     */
    fun dpToPixel(context: Context, dp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return if (dp < 0) dp else Math.round(dp * displayMetrics.density)
    }
}
