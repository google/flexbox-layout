package com.example.stretchplayground

import android.content.Context
import android.util.Log
import android.widget.ImageView
import android.widget.TextView

class CustomImageView(context: Context?) : ImageView(context) {

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    Log.e("CustomImageView specs",
        "MeasureSpecs from parent: Width" + MeasureSpec.toString(widthMeasureSpec) + " | Height" + MeasureSpec.toString(heightMeasureSpec))
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//    super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))

    Log.e("CustomImageView result", "Measure result: Width: $measuredWidth | Height: $measuredHeight")
  }
}