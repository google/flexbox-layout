# RecyclerView integration

With the latest alpha version of the release, Flexbox can now be used inside the `RecyclerView`
as a `LayoutManager` (`FlexboxLayoutManager`).
That means now you can use Flexbox with large number of items in a scrollable container!

![FlexboxLayoutManager in action](/assets/flexbox-layoutmanager.gif)


## Supported attributes / features comparison
Due to some characteristics of the RecyclerView, some Flexbox attributes are not avaiable/not implemented
to the `FlexboxLayoutManager`.
Here is a quick overview of the attributes/features comparison between the two containers.

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


## Backward-imcompatible changes from the 0.2.x versions
`FlexboxLayout` can still be used as the same way, but there are some backward-imcompatible
changes introduced.

* Now Flexbox specific constants are now defined in each individual class such as:
  * `FlexboxLayout.FLEX_DIRECTION_ROW` -> `FlexDirection.ROW`
  * `FlexboxLayout.FLEX_WRAP_WRAP` -> `FlexWrap.WRAP`
  * `FlexboxLayout.JUSTIFY_CONTENT_FLEX_START` -> `JustifyContent.FLEX_START`
  * `FlexboxLayout.ALIGN_ITEMS_FLEX_START` -> `AlignItems.FLEX_START`
  * `FlexboxLayout.ALIGN_CONTENT_FLEX_START` -> `AlignContent.FLEX_START`

  including other values (such as FLEX_END, STRETCH) are now moved to each individual class.

## Sample code
The code for the new `FlexboxLayoutManager` hasn't merged to the master branch yet, since
it's not as stable as the existing `FlexboxLayout`.
But you can still reference some sample code using the `FlexboxLayoutManager` inside the
`RecyclerView` in the [dev_recyclerview](https://github.com/google/flexbox-layout/tree/dev_recyclerview) branch
such as:
  -  [Playground demo app](https://github.com/google/flexbox-layout/tree/dev_recyclerview/demo-playground)
  -  [Cat Gallery demo app](https://github.com/google/flexbox-layout/tree/dev_recyclerview/demo-cat-gallery)

