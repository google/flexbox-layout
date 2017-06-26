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

package com.google.android.flexbox.test;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import java.util.Locale;

/**
 * Custom {@link BaseMatcher} that expects {@link Number} value allowing some errors to allow
 * such as rounding errors.
 */
public class IsEqualAllowingError<T extends Number> extends BaseMatcher<T> {

    private Number expected;

    private Integer errorAllowed;

    private IsEqualAllowingError(Number expected) {
        this(expected, 2);
    }

    private IsEqualAllowingError(Number expected, int errorAllowed) {
        this.expected = expected;
        this.errorAllowed = errorAllowed;
    }

    @Override
    public boolean matches(Object item) {
        if (!(item instanceof Number)) {
            return false;
        }
        Number other = (Number) item;
        return expected.intValue() - errorAllowed <= other.intValue() &&
                other.intValue() <= expected.intValue() + errorAllowed;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(
                String.format(Locale.US, "expected value is <%s> allowing error of <%s>.", expected,
                        errorAllowed));
    }

    @Factory
    public static <T extends Number> Matcher<T> isEqualAllowingError(T expected) {
        return new IsEqualAllowingError<>(expected);
    }

    @Factory
    public static <T extends Number> Matcher<T> isEqualAllowingError(T expected, int errorAllowed) {
        return new IsEqualAllowingError<>(expected, errorAllowed);
    }
}

