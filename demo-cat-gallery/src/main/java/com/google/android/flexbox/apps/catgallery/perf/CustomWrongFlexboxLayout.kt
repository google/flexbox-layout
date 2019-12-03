package com.example.stretchplayground

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.google.android.flexbox.FlexboxLayout

class CustomWrongFlexboxLayout(context: Context?) : FlexboxLayout(context) {

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    Log.e("CustomFlexbox specs",
        "MeasureSpecs from parent: Width" + MeasureSpec.toString(widthMeasureSpec) + " | Height" + MeasureSpec.toString(heightMeasureSpec))
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    Log.e("CustomFlexbox result", "Measure result: Width: $measuredWidth | Height: $measuredHeight")
  }
}