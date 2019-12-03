package com.google.android.flexbox.apps.catgallery.perf

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.example.stretchplayground.CustomImageView
import com.example.stretchplayground.CustomTextView
import com.example.stretchplayground.CustomWrongFlexboxLayout
import com.google.android.flexbox.AlignSelf
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout

class TextViewMeasurementBuilder(
    private val context: Context) {

  private val imageBase64 =
      "iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQ" +
          "UAAABqSURBVHgB7ZCxDYBADAMtsRAbfjZgBDwKm8EjUXyTwsgRBX+Su+SsBJh8zQI/7Fl7DhTAnvNJwAwH+Z0dRvgPeUBrpypXTiTEtygL" +
          "yqy8GG/lWcnmlGcl4ZRnJdlV9pIGM61SPpaUySf1XBgQU+UofKBZAAAAAElFTkSuQmCC"

  fun build(container: ViewGroup) {
    val wrapper = defaultContainer()
    val root = defaultContainer(FlexDirection.ROW, true)
    val textView = textview()

    root.addView(imageview())
    root.addView(textView)
//    root.addView(imageview())

    wrapper.addView(edittext(textView))
    wrapper.addView(root)

    container.addView(wrapper)
  }

  private fun edittext(textViewOne: TextView): EditText {
    val edittext = EditText(context)
    val flexboxParams = FlexboxLayout.LayoutParams(
        FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT)
    flexboxParams.setMargins(32, 32, 32, 64)
    flexboxParams.alignSelf = AlignSelf.STRETCH
    flexboxParams.flexGrow = 1f
    edittext.layoutParams = flexboxParams
    edittext.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable) {
        textViewOne.text = "$s"
      }
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
    return edittext
  }

  private fun textview(): TextView {
    val textview = CustomTextView(context)
    val flexboxParams = FlexboxLayout.LayoutParams(
            FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT)
    textview.layoutParams = flexboxParams
    textview.setBackgroundColor(Color.LTGRAY)
    return textview
  }

  private fun imageview(): ImageView {
    val imageView = CustomImageView(context)
    val flexboxParams = FlexboxLayout.LayoutParams(88, 88)
    flexboxParams.flexShrink = 0f
    imageView.layoutParams = flexboxParams

    val decoded = Base64.decode(imageBase64, 0)
    val image = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
    imageView.setImageBitmap(image)
    return imageView
  }

  private fun defaultContainer(flexDirection: Int = FlexDirection.COLUMN, isCustom: Boolean = false): FlexboxLayout {
    val container = if (isCustom) CustomWrongFlexboxLayout(context) else FlexboxLayout(context)
    val flexboxParams = FlexboxLayout.LayoutParams(
        FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT)
    flexboxParams.flexGrow = 1f
    container.layoutParams = flexboxParams
    container.flexDirection = flexDirection
    return container
  }
}