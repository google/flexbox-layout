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

package com.google.android.flexbox.test

import android.app.Activity
import android.content.res.Configuration
import android.util.Log

import com.google.android.flexbox.FlexboxLayout

/**
 * Activity for testing the [FlexboxLayout] that handles configuration changes by itself
 * instead of letting the system take care of those.
 */
class ConfigChangeActivity : Activity() {
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        Log.d(TAG, "onConfigurationChanged: $newConfig")
    }

    companion object {
        private const val TAG = "ConfigChangeActivity"
    }
}
