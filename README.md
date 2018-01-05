# FlexboxLayout
[ ![Circle CI](https://circleci.com/gh/google/flexbox-layout.svg?style=shield&circle-token=2a42716dfffab73d73c5ce7ed7b3ee620cfa137b) ](https://circleci.com/gh/google/flexbox-layout/tree/master)
[ ![Download](https://api.bintray.com/packages/google/flexbox-layout/flexbox/images/download.svg) ](https://bintray.com/google/flexbox-layout/flexbox/_latestVersion)

FlexboxLayout is a library project which brings the similar capabilities of
[CSS Flexible Box Layout Module](https://www.w3.org/TR/css-flexbox-1) to Android.

# Installation
Add the following dependency to your `build.gradle` file:

```
dependencies {
    compile 'com.google.android:flexbox:0.3.2'
}
```

# Usage
There are two ways of using Flexbox in your layout. 

## FlexboxLayout 
The first one is `FlexboxLayout` that extends the `ViewGroup` like `LinearLayout` and `RelativeLayout`.
You can specify the attributes from a layout XML like:
```xml
<com.google.android.flexbox.FlexboxLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:flexWrap="wrap"
    app:alignItems="stretch"
    app:alignContent="stretch" >

    <TextView
        android:id="@+id/textview1"
        android:layout_width="120dp"
        android:layout_height="80dp"
        app:layout_flexBasisPercent="50%"
        />

    <TextView
        android:id="@+id/textview2"
        android:layout_width="80dp"
        android:layout_height="80dp"
        app:layout_alignSelf="center"
        />

    <TextView
        android:id="@+id/textview3"
        android:layout_width="160dp"
        android:layout_height="80dp"
        app:layout_alignSelf="flex_end"
        />
</com.google.android.flexbox.FlexboxLayout>
```

Or from code like:
```java
FlexboxLayout flexboxLayout = (FlexboxLayout) findViewById(R.id.flexbox_layout);
flexboxLayout.setFlexDirection(FlexDirection.ROW);

View view = flexboxLayout.getChildAt(0);
FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) view.getLayoutParams();
lp.order = -1;
lp.flexGrow = 2;
view.setLayoutParams(lp);
```

## FlexboxLayoutManager (within RecyclerView)
The second one is `FlexboxLayoutManager` that can be used within `RecyclerView`.

```java
RecyclerView recyclerView = (RecyclerView) context.findViewById(R.id.recyclerview);
FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(context);
layoutManager.setFlexDirection(FlexDirection.COLUMN);
layoutManager.setJustifyContent(JustifyContent.FLEX_END);
recyclerView.setLayoutManager(layoutManager);
```

or for the attributes for the children of the `FlexboxLayoutManager` you can do like:

```java
mImageView.setImageDrawable(drawable);
ViewGroup.LayoutParams lp = mImageView.getLayoutParams();
if (lp instanceof FlexboxLayoutManager.LayoutParams) {
    FlexboxLayoutManager.LayoutParams flexboxLp = (FlexboxLayoutManager.LayoutParams) lp;
    flexboxLp.setFlexGrow(1.0f);
    flexboxLp.setAlignSelf(AlignSelf.FLEX_END);
}
```

The advantage of using `FlexboxLayoutManager` is that it recycles the views that go off the screen
for reuse for the views that are appearing as the user scrolls instead of inflating every individual view,
which consumes much less memory especially when the number of items contained in the Flexbox container is large.

![FlexboxLayoutManager in action](/assets/flexbox-layoutmanager.gif)


## Supported attributes / features comparison
Due to some characteristics of `RecyclerView`, some Flexbox attributes are not available/not implemented
to the `FlexboxLayoutManager`.
Here is a quick overview of the attributes/features comparison between the two implementations.

|Attribute / Feature|FlexboxLayout| FlexboxLayoutManager (RecyclerView)|
| ------- |:-----------:|:----------------------------------:|
|flexDirection|![Check](/assets/pngs/check_green_small.png)|![Check](/assets/pngs/check_green_small.png)|
|flexWrap|![Check](/assets/pngs/check_green_small.png)|![Check](/assets/pngs/check_green_small.png) (except `wrap_reverse`)|
|justifyContent|![Check](/assets/pngs/check_green_small.png)|![Check](/assets/pngs/check_green_small.png)|
|alignItems|![Check](/assets/pngs/check_green_small.png)|![Check](/assets/pngs/check_green_small.png)|
|alignContent|![Check](/assets/pngs/check_green_small.png)| - |
|layout_order|![Check](/assets/pngs/check_green_small.png)| - |
|layout_flexGrow|![Check](/assets/pngs/check_green_small.png)|![Check](/assets/pngs/check_green_small.png)|
|layout_flexShrink|![Check](/assets/pngs/check_green_small.png)|![Check](/assets/pngs/check_green_small.png)|
|layout_alignSelf|![Check](/assets/pngs/check_green_small.png)|![Check](/assets/pngs/check_green_small.png)|
|layout_flexBasisPercent|![Check](/assets/pngs/check_green_small.png)|![Check](/assets/pngs/check_green_small.png)|
|layout_(min/max)Width|![Check](/assets/pngs/check_green_small.png)|![Check](/assets/pngs/check_green_small.png)|
|layout_(min/max)Height|![Check](/assets/pngs/check_green_small.png)|![Check](/assets/pngs/check_green_small.png)|
|layout_wrapBefore|![Check](/assets/pngs/check_green_small.png)|![Check](/assets/pngs/check_green_small.png)|
|Divider|![Check](/assets/pngs/check_green_small.png)|![Check](/assets/pngs/check_green_small.png)|
|View recycling| - |![Check](/assets/pngs/check_green_small.png)|
|Scrolling| *1 |![Check](/assets/pngs/check_green_small.png)|

*1 Partially possible by wrapping it with `ScrollView`. But it isn't likely to work with large set
   of views inside the layout. Because it doesn't consider view recycling.

# Supported attributes

## Attributes for the FlexboxLayout:

* __flexDirection__
  * This attribute determines the direction of the main axis (and the cross axis, perpendicular to the main axis). The direction children items are placed inside the Flexbox layout.
  Possible values are:
    * row (default)
    * row_reverse
    * column
    * column_reverse

    ![Flex Direction explanation](/assets/flex-direction.gif)

* __flexWrap__
  * This attribute controls whether the flex container is single-line or multi-line, and the
  direction of the cross axis. Possible values are:
    * nowrap (default)
    * wrap
    * wrap_reverse

    ![Flex Wrap explanation](/assets/flex-wrap.gif)

* __justifyContent__
  * This attribute controls the alignment along the main axis. Possible values are:
    * flex_start (default)
    * flex_end
    * center
    * space_between
    * space_around

    ![Justify Content explanation](/assets/justify-content.gif)

* __alignItems__
  * This attribute controls the alignment along the cross axis. Possible values are:
    * stretch (default)
    * flex_start
    * flex_end
    * center
    * baseline

    ![Align Items explanation](/assets/align-items.gif)

* __alignContent__
  * This attribute controls the alignment of the flex lines in the flex container. Possible values
  are:
    * stretch (default)
    * flex_start
    * flex_end
    * center
    * space_between
    * space_around

    ![Align Content explanation](/assets/align-content.gif)

* __showDividerHorizontal__ (one or more of `none | beginning | middle | end`)
* __dividerDrawableHorizontal__ (reference to a drawable)
  * Puts horizontal dividers between flex lines (or flex items when flexDirection
  is set to `column` or `column_rebase`).
  
* __showDividerVertical__ (one or more of `none | beginning | middle | end`)
* __dividerDrawableVertical__ (reference to a drawable)
  * Puts vertical dividers between flex items (or flex lines when flexDirection
  is set to `column` or `column_rebase`).

* __showDivider__ (one or more of `none | beginning | middle | end`)
* __dividerDrawable__ (reference to a drawable)
  * Shorthand for setting both horizontal and vertical dividers. Note that if used with other attributes
  (such as `justifyContent="space_around"` or `alignContent="space_between"` ... etc) for putting 
  spaces between flex lines or flex items, you may see unexpected spaces. Please avoid using these
  at the same time.
  
  Example of putting both vertical and horizontal dividers.
  
  `res/drawable/divider.xml`
  ```xml
  <shape xmlns:android="http://schemas.android.com/apk/res/android">
    <size
        android:width="8dp"
        android:height="12dp" />
    <solid android:color="#44A444" />
  </shape> 
  ```
  
  `res/layout/content_main.xml`
  ```xml
  <com.google.android.flexbox.FlexboxLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:alignContent="flex_start"
    app:alignItems="flex_start"
    app:flexWrap="wrap"
    app:showDivider="beginning|middle"
    app:dividerDrawable="@drawable/divider" >

    <TextView
        style="@style/FlexItem"
        android:layout_width="220dp"
        android:layout_height="80dp"
        android:text="1" />
    <TextView
        style="@style/FlexItem"
        android:layout_width="120dp"
        android:layout_height="80dp"
        android:text="2" />
    <TextView
        style="@style/FlexItem"
        android:layout_width="160dp"
        android:layout_height="80dp"
        android:text="3" />
    <TextView
        style="@style/FlexItem"
        android:layout_width="80dp"
        android:layout_height="80dp"
        android:text="4" />
    <TextView
        style="@style/FlexItem"
        android:layout_width="100dp"
        android:layout_height="80dp"
        android:text="5" />
  ```
  
  ![Dividers beginning and middle](/assets/divider-beginning-middle.png)


## Attributes for the children of a FlexboxLayout

* __layout_order__ (integer)
  * This attribute can change how the ordering of the children views are laid out.
  By default, children are displayed and laid out in the same order as they appear in the
  layout XML. If not specified, `1` is set as a default value.

    ![Order explanation](/assets/layout_order.gif)

* __layout_flexGrow__ (float)
  * This attribute determines how much this child will grow if positive free space is
  distributed relative to the rest of other flex items included in the same flex line.
  If a flex item has a positive `layout_flexGrow` value, the item will take up the remaining
  space in the flex line. If multiple flex items in the same flex line have positive `layout_flexGrow`
  values, the remaining free space is distributed depending on the proportion of their declared
  `layout_flexGrow` value. (Similar to the `layout_weight` attribute in the `LinearLayout`)
  If not specified, `0` is set as a default value.

    ![Flex Grow explanation](/assets/layout_flexGrow.gif)

* __layout_flexShrink__ (float)
  * This attribute determines how much this child will shrink if negative free space is
  distributed relative to the rest of other flex items included in the same flex line.
  If not specified, `1` is set as a default value.

    ![Flex Shrink explanation](/assets/layout_flexShrink.gif)

* __layout_alignSelf__
  * This attribute determines the alignment along the cross axis (perpendicular to the
  main axis). The alignment in the same direction can be determined by the
  `alignItems` in the parent, but if this is set to other than
  `auto`, the cross axis alignment is overridden for this child. Possible values are:
    * auto (default)
    * flex_start
    * flex_end
    * center
    * baseline
    * stretch

    ![Align Self explanation](/assets/layout_alignSelf.gif)

* __layout_flexBasisPercent__ (fraction)
  * The initial flex item length in a fraction format relative to its parent.
  The initial main size of this child view is trying to be expanded as the specified
  fraction against the parent main size.
  If this value is set, the length specified from `layout_width`
  (or `layout_height`) is overridden by the calculated value from this attribute.
  This attribute is only effective when the parent's length is definite (MeasureSpec mode is
  `MeasureSpec.EXACTLY`). The default value is `-1`, which means not set.

    ![Flex basis percent explanation](/assets/layout_flexBasisPercent.gif)

* __layout_minWidth__ / __layout_minHeight__ (dimension)
  * These attributes impose minimum size constraints for the children of FlexboxLayout.
  A child view won't be shrank less than the value of these attributes (varies based on the
  `flexDirection` attribute as to which attribute imposes the size constraint along the
  main axis) regardless of the `layout_flexShrink` attribute.

    ![Min width explanation](/assets/layout_minWidth.gif)

* __layout_maxWidth__ / __layout_maxHeight__ (dimension)
  * These attributes impose maximum size constraints for the children of FlexboxLayout.
  A child view won't be expanded more than the value of these attributes (varies based on the
  `flexDirection` attribute as to which attribute imposes the size constraint along the
  main axis) regardless of the `layout_flexGrow` attribute.

    ![Max width explanation](/assets/layout_maxWidth.gif)

* __layout_wrapBefore__ (boolean)
  * This attribute forces a flex line wrapping, the default value is `false`.
  i.e. if this is set to `true` for a
  flex item, the item will become the first item of a flex line. (A wrapping happens
  regardless of the flex items being processed in the the previous flex line)
  This attribute is ignored if the `flex_wrap` attribute is set to `nowrap`.
  The equivalent attribute isn't defined in the original CSS Flexible Box Module
  specification, but having this attribute is useful for Android developers. For example, to flatten
  the layouts when building a grid like layout or for a situation where developers want
  to put a new flex line to make a semantic difference from the previous one, etc.

    ![Wrap before explanation](/assets/layout_wrapBefore.gif)

# Others

## Known differences from the original CSS specification
This library tries to achieve the same capabilities of the original
[Flexible Box specification](https://www.w3.org/TR/css-flexbox-1) as much as possible,
but due to some reasons such as the way specifying attributes can't be the same between
CSS and Android XML, there are some known differences from the original specification.

(1) There is no [flex-flow](https://www.w3.org/TR/css-flexbox-1/#flex-flow-property)
equivalent attribute
  * Because `flex-flow` is a shorthand for setting the `flex-direction` and `flex-wrap` properties,
  specifying two attributes from a single attribute is not practical in Android.

(2) There is no [flex](https://www.w3.org/TR/css-flexbox-1/#flex-property) equivalent attribute
  * Likewise `flex` is a shorthand for setting the `flex-grow`, `flex-shrink` and `flex-basis`,
  specifying those attributes from a single attribute is not practical.

(3) `layout_flexBasisPercent` is introduced instead of
  [flexBasis](https://www.w3.org/TR/css-flexbox-1/#flex-basis-property)
  * Both `layout_flexBasisPercent` in this library and `flex-basis` property in the CSS are used to
  determine the initial length of an individual flex item. The `flex-basis` property accepts width
  values such as `1em`, `10px`, and `content` as strings as well as percentage values such as
  `10%` and `30%`. `layout_flexBasisPercent` only accepts percentage values.
  However, specifying initial fixed width values can be done by specifying width (or height) values in
  layout_width (or layout_height, varies depending on the `flexDirection`). Also, the same
  effect can be done by specifying "wrap_content" in layout_width (or layout_height) if
  developers want to achieve the same effect as 'content'. Thus, `layout_flexBasisPercent` only
  accepts percentage values, which can't be done through layout_width (or layout_height) for
  simplicity.

(4) `layout_wrapBefore` is introduced.
  * The equivalent attribute doesn't exist in the CSS Flexible Box Module specification,
  but as explained above, Android developers will benefit by having this attribute for having
  more control over when a wrapping happens.

## Xamarin Binding
Xamarin binding is now available on [NuGet](https://www.nuget.org/packages/FlexboxLayoutXamarinBindingAndroid/) thanks to [@btripp](https://github.com/btripp)

## Demo apps
### Flexbox Playground demo app
The `demo-playground` module works as a playground demo app for trying various values for the supported attributes.
You can install it by
```
./gradlew demo-playground:installDebug
```

### Cat gallery demo app
The `demo-cat-gallery` module showcases the usage of the FlexboxLayoutManager inside the RecyclerView
that handles various sizes of views aligned nicely regardless of the device width like the
Google Photo app without loading all the images on the memory.
Thus compared to using the {@link FlexboxLayout}, it's much less likely to abuse the memory,
which some times leads to the OutOfMemoryError.
```
./gradlew demo-cat-gallery:installDebug
```

## How to make contributions
Please read and follow the steps in [CONTRIBUTING.md](/CONTRIBUTING.md)

## License
Please see [LICENSE](/LICENSE)
