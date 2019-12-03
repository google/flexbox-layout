package com.google.android.flexbox.apps.catgallery.perf

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.apps.catgallery.R

class MainActivity : AppCompatActivity() {

  private lateinit var container: ViewGroup
  private lateinit var textViewMeasurementBuilder: TextViewMeasurementBuilder

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    container = findViewById(R.id.container)
    val now = System.currentTimeMillis()
    Log.e("stretch", "Started building Flexbox ui")
    textViewMeasurementBuilder = TextViewMeasurementBuilder(this)
    textViewMeasurementBuilder.build(container)
    Log.e("stretch", "Finished building Flexbox ui in ${(System.currentTimeMillis() - now)} ms")
  }
}
