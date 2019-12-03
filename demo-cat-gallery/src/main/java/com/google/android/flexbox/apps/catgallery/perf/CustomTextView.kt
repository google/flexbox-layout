package com.example.stretchplayground

import android.content.Context
import android.util.Log
import android.widget.TextView

class CustomTextView(context: Context?) : TextView(context) {

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    Log.e("CustomTextView specs",
        "MeasureSpecs from parent: Width" + MeasureSpec.toString(widthMeasureSpec) + " | Height" + MeasureSpec.toString(heightMeasureSpec))
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    Log.e("CustomTextView result", "Measure result: Width: $width | Height: $measuredHeight")
  }
}