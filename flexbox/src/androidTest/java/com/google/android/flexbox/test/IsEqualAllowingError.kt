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

package com.google.android.flexbox.test

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Factory
import org.hamcrest.Matcher
import java.util.*

/**
 * Custom [BaseMatcher] that expects [Number] value allowing some errors to allow
 * such as rounding errors.
 */
class IsEqualAllowingError<T : Number> private constructor(private val expected: Number, private val errorAllowed: Int) : BaseMatcher<T>() {

    override fun matches(item: Any): Boolean {
        if (item !is Number) {
            return false
        }
        return expected.toInt() - errorAllowed <= item.toInt() && item.toInt() <= expected.toInt() + errorAllowed
    }

    override fun describeTo(description: Description) {
        description.appendText(
                String.format(Locale.US, "expected value is <%s> allowing error of <%s>.", expected,
                        errorAllowed))
    }

    companion object {

        @Factory
        fun <T : Number> isEqualAllowingError(expected: T): Matcher<T> {
            return IsEqualAllowingError(expected, 2)
        }
    }
}
