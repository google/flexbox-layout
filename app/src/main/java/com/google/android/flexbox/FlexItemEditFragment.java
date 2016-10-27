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

package com.google.android.flexbox;

import com.google.android.apps.flexbox.R;
import com.google.android.flexbox.validators.DimensionInputValidator;
import com.google.android.flexbox.validators.FixedDimensionInputValidator;
import com.google.android.flexbox.validators.FlexBasisPercentInputValidator;
import com.google.android.flexbox.validators.InputValidator;
import com.google.android.flexbox.validators.IntegerInputValidator;
import com.google.android.flexbox.validators.NonNegativeDecimalInputValidator;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * DialogFragment that changes the properties for a flex item.
 */
public class FlexItemEditFragment extends DialogFragment {

    private static final String FLEX_ITEM_KEY = "flex_item";

    private static final String VIEW_INDEX_KEY = "view_index";

    private String ALIGN_SELF_AUTO;

    private String ALIGN_SELF_FLEX_START;

    private String ALIGN_SELF_FLEX_END;

    private String ALIGN_SELF_CENTER;

    private String ALIGN_SELF_BASELINE;

    private String ALIGN_SELF_STRETCH;

    private int mViewIndex;

    private FlexItem mFlexItem;

    private FlexItemChangedListener mFlexItemChangedListener;

    private Context mContext;

    public static FlexItemEditFragment newInstance(FlexItem flexItem, int viewIndex) {
        FlexItemEditFragment fragment = new FlexItemEditFragment();
        Bundle args = new Bundle();
        args.putParcelable(FLEX_ITEM_KEY, flexItem);
        args.putInt(VIEW_INDEX_KEY, viewIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        mContext = null;
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
        } else {
            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Dialog);
        }
        Bundle args = getArguments();
        mFlexItem = args.getParcelable(FLEX_ITEM_KEY);
        mViewIndex = args.getInt(VIEW_INDEX_KEY);

        Activity activity = getActivity();
        ALIGN_SELF_AUTO = activity.getString(R.string.auto);
        ALIGN_SELF_FLEX_START = activity.getString(R.string.flex_start);
        ALIGN_SELF_FLEX_END = activity.getString(R.string.flex_end);
        ALIGN_SELF_CENTER = activity.getString(R.string.center);
        ALIGN_SELF_BASELINE = activity.getString(R.string.baseline);
        ALIGN_SELF_STRETCH = activity.getString(R.string.stretch);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_flex_item_edit, container, false);
        getDialog().setTitle(String.valueOf(mViewIndex + 1));

        final TextInputLayout orderTextInput = (TextInputLayout) view
                .findViewById(R.id.input_layout_order);
        EditText orderEdit = (EditText) view.findViewById(R.id.edit_text_order);
        orderEdit.setText(String.valueOf(mFlexItem.getOrder()));
        orderEdit.addTextChangedListener(
                new FlexEditTextWatcher(orderTextInput, new IntegerInputValidator(),
                        R.string.must_be_integer));

        final TextInputLayout flexGrowInput = (TextInputLayout) view
                .findViewById(R.id.input_layout_flex_grow);
        final EditText flexGrowEdit = (EditText) view.findViewById(R.id.edit_text_flex_grow);
        flexGrowEdit.setText(String.valueOf(mFlexItem.getFlexGrow()));
        flexGrowEdit.addTextChangedListener(
                new FlexEditTextWatcher(flexGrowInput, new NonNegativeDecimalInputValidator(),
                        R.string.must_be_non_negative_float));

        final TextInputLayout flexShrinkInput = (TextInputLayout) view
                .findViewById(R.id.input_layout_flex_shrink);
        EditText flexShrinkEdit = (EditText) view.findViewById(
                R.id.edit_text_flex_shrink);
        flexShrinkEdit.setText(String.valueOf(mFlexItem.getFlexShrink()));
        flexShrinkEdit.addTextChangedListener(
                new FlexEditTextWatcher(flexShrinkInput, new NonNegativeDecimalInputValidator(),
                        R.string.must_be_non_negative_float));

        final TextInputLayout flexBasisPercentInput = (TextInputLayout)
                view.findViewById(R.id.input_layout_flex_basis_percent);
        EditText flexBasisPercentEdit = (EditText) view.findViewById(
                R.id.edit_text_flex_basis_percent);
        if (mFlexItem.getFlexBasisPercent()
                != FlexboxLayout.LayoutParams.FLEX_BASIS_PERCENT_DEFAULT) {
            flexBasisPercentEdit
                    .setText(String.valueOf(Math.round(mFlexItem.getFlexBasisPercent() * 100)));
        } else {
            flexBasisPercentEdit.setText(String.valueOf((int) mFlexItem.getFlexBasisPercent()));
        }
        flexBasisPercentEdit.addTextChangedListener(
                new FlexEditTextWatcher(flexBasisPercentInput, new FlexBasisPercentInputValidator(),
                        R.string.must_be_minus_one_or_non_negative_integer));

        final TextInputLayout widthInput = (TextInputLayout) view
                .findViewById(R.id.input_layout_width);
        EditText widthEdit = (EditText) view.findViewById(R.id.edit_text_width);
        widthEdit.setText(String.valueOf(Util.pixelToDp(mContext, mFlexItem.getWidth())));
        widthEdit.addTextChangedListener(
                new FlexEditTextWatcher(widthInput, new DimensionInputValidator(),
                        R.string.must_be_minus_one_or_minus_two_or_non_negative_integer));

        final TextInputLayout heightInput = (TextInputLayout) view
                .findViewById(R.id.input_layout_height);
        EditText heightEdit = (EditText) view.findViewById(
                R.id.edit_text_height);
        heightEdit.setText(String.valueOf(Util.pixelToDp(mContext, mFlexItem.getHeight())));
        heightEdit.addTextChangedListener(
                new FlexEditTextWatcher(heightInput, new DimensionInputValidator(),
                        R.string.must_be_minus_one_or_minus_two_or_non_negative_integer));

        final TextInputLayout minWidthInput = (TextInputLayout) view
                .findViewById(R.id.input_layout_min_width);
        EditText minWidthEdit = (EditText) view.findViewById(R.id.edit_text_min_width);
        minWidthEdit.setText(String.valueOf(Util.pixelToDp(mContext, mFlexItem.getMinWidth())));
        minWidthEdit.addTextChangedListener(
                new FlexEditTextWatcher(minWidthInput, new FixedDimensionInputValidator(),
                        R.string.must_be_non_negative_integer));

        final TextInputLayout minHeightInput = (TextInputLayout) view
                .findViewById(R.id.input_layout_min_height);
        EditText minHeightEdit = (EditText) view.findViewById(
                R.id.edit_text_min_height);
        minHeightEdit.setText(String.valueOf(Util.pixelToDp(mContext, mFlexItem.getMinHeight())));
        minHeightEdit.addTextChangedListener(
                new FlexEditTextWatcher(minHeightInput, new FixedDimensionInputValidator(),
                        R.string.must_be_non_negative_integer));

        final TextInputLayout maxWidthInput = (TextInputLayout) view
                .findViewById(R.id.input_layout_max_width);
        EditText maxWidthEdit = (EditText) view.findViewById(R.id.edit_text_max_width);
        maxWidthEdit.setText(String.valueOf(Util.pixelToDp(mContext, mFlexItem.getMaxWidth())));
        maxWidthEdit.addTextChangedListener(
                new FlexEditTextWatcher(maxWidthInput, new FixedDimensionInputValidator(),
                        R.string.must_be_non_negative_integer));

        final TextInputLayout maxHeightInput = (TextInputLayout) view
                .findViewById(R.id.input_layout_max_height);
        EditText maxHeightEdit = (EditText) view.findViewById(
                R.id.edit_text_max_height);
        maxHeightEdit.setText(String.valueOf(Util.pixelToDp(mContext, mFlexItem.getMaxHeight())));
        maxHeightEdit.addTextChangedListener(
                new FlexEditTextWatcher(maxHeightInput, new FixedDimensionInputValidator(),
                        R.string.must_be_non_negative_integer));

        setNextFocusesOnEnterDown(orderEdit, flexGrowEdit, flexShrinkEdit, flexBasisPercentEdit,
                widthEdit, heightEdit, minWidthEdit, minHeightEdit, maxWidthEdit, maxHeightEdit);

        Spinner alignSelfSpinner = (Spinner) view.findViewById(
                R.id.spinner_align_self);
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.array_align_self, R.layout.spinner_item);
        alignSelfSpinner.setAdapter(arrayAdapter);
        alignSelfSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.equals(ALIGN_SELF_AUTO)) {
                    mFlexItem.setAlignSelf(AlignSelf.AUTO);
                } else if (selected.equals(ALIGN_SELF_FLEX_START)) {
                    mFlexItem.setAlignSelf(AlignItems.FLEX_START);
                } else if (selected.equals(ALIGN_SELF_FLEX_END)) {
                    mFlexItem.setAlignSelf(AlignItems.FLEX_END);
                } else if (selected.equals(ALIGN_SELF_CENTER)) {
                    mFlexItem.setAlignSelf(AlignItems.CENTER);
                } else if (selected.equals(ALIGN_SELF_BASELINE)) {
                    mFlexItem.setAlignSelf(AlignItems.BASELINE);
                } else if (selected.equals(ALIGN_SELF_STRETCH)) {
                    mFlexItem.setAlignSelf(AlignItems.STRETCH);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No op
            }
        });

        CheckBox wrapBeforeCheckBox = (CheckBox) view.findViewById(
                R.id.checkbox_wrap_before);
        wrapBeforeCheckBox.setChecked(mFlexItem.isWrapBefore());
        wrapBeforeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mFlexItem.setWrapBefore(isChecked);
            }
        });
        int alignSelfPosition = arrayAdapter
                .getPosition(alignSelfAsString(mFlexItem.getAlignSelf()));
        alignSelfSpinner.setSelection(alignSelfPosition);

        view.findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        final Button okButton = (Button) view.findViewById(R.id.button_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (orderTextInput.isErrorEnabled() || flexGrowInput.isErrorEnabled() ||
                        flexBasisPercentInput.isErrorEnabled() || widthInput.isErrorEnabled() ||
                        heightInput.isErrorEnabled() || minWidthInput.isErrorEnabled() ||
                        minHeightInput.isErrorEnabled() || maxWidthInput.isErrorEnabled() ||
                        maxHeightInput.isErrorEnabled()) {
                    Toast.makeText(getActivity(), R.string.invalid_values_exist, Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (mFlexItemChangedListener != null) {
                    mFlexItemChangedListener.onFlexItemChanged(mFlexItem, mViewIndex);
                }
                dismiss();
            }
        });
        return view;
    }

    public void setFlexItemChangedListener(FlexItemChangedListener flexItemChangedListener) {
        mFlexItemChangedListener = flexItemChangedListener;
    }

    private void setNextFocusesOnEnterDown(final TextView... textViews) {
        // This can be done by setting android:nextFocus* as in 
        // https://developer.android.com/training/keyboard-input/navigation.html
        // But it requires API level 11 as a minimum sdk version. To support the lower level devices,
        // doing it programmatically.
        for (int i = 0; i < textViews.length; i++) {
            final int index = i;
            textViews[index].setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_NEXT ||
                            actionId == EditorInfo.IME_ACTION_DONE ||
                            (actionId == EditorInfo.IME_NULL
                                    && event.getAction() == KeyEvent.ACTION_DOWN
                                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                        if (index + 1 < textViews.length) {
                            textViews[index + 1].requestFocus();
                        } else if (index == textViews.length - 1) {
                            InputMethodManager inputMethodManager
                                    = (InputMethodManager) getActivity()
                                    .getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                    }
                    return true;
                }
            });

            // Suppress the key focus change by KeyEvent.ACTION_UP of the enter key
            textViews[index].setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    return keyCode == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_UP;
                }
            });
        }

    }

    private String alignSelfAsString(int alignSelf) {
        switch (alignSelf) {
            case AlignSelf.AUTO:
                return ALIGN_SELF_AUTO;
            case AlignItems.FLEX_START:
                return ALIGN_SELF_FLEX_START;
            case AlignItems.FLEX_END:
                return ALIGN_SELF_FLEX_END;
            case AlignItems.CENTER:
                return ALIGN_SELF_CENTER;
            case AlignItems.BASELINE:
                return ALIGN_SELF_BASELINE;
            case AlignItems.STRETCH:
                return ALIGN_SELF_STRETCH;
            default:
                return ALIGN_SELF_AUTO;
        }
    }

    private class FlexEditTextWatcher implements TextWatcher {

        TextInputLayout mTextInputLayout;

        InputValidator mInputValidator;

        int mErrorMessageId;

        FlexEditTextWatcher(TextInputLayout textInputLayout,
                InputValidator inputValidator, int errorMessageId) {
            mTextInputLayout = textInputLayout;
            mInputValidator = inputValidator;
            mErrorMessageId = errorMessageId;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // No op
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mInputValidator.isValidInput(s)) {
                mTextInputLayout.setErrorEnabled(false);
                mTextInputLayout.setError("");
            } else {
                mTextInputLayout.setErrorEnabled(true);
                mTextInputLayout.setError(getActivity().getResources()
                        .getString(mErrorMessageId));
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (mTextInputLayout.isErrorEnabled() || TextUtils.isEmpty(editable) ||
                    !mInputValidator.isValidInput(editable.toString())) {
                return;
            }
            final Number value;
            switch (mTextInputLayout.getId()) {
                case R.id.input_layout_flex_grow:
                case R.id.input_layout_flex_shrink:
                    try {
                        value = Float.valueOf(editable.toString());
                    } catch (NumberFormatException | NullPointerException ignore) {
                        return;
                    }
                    break;
                default:
                    try {
                        value = Integer.valueOf(editable.toString());
                    } catch (NumberFormatException | NullPointerException ignore) {
                        return;
                    }
            }

            switch (mTextInputLayout.getId()) {
                case R.id.input_layout_order:
                    mFlexItem.setOrder(value.intValue());
                    break;
                case R.id.input_layout_flex_grow:
                    mFlexItem.setFlexGrow(value.floatValue());
                    break;
                case R.id.input_layout_flex_shrink:
                    mFlexItem.setFlexShrink(value.floatValue());
                    break;
                case R.id.input_layout_width:
                    mFlexItem.setWidth(Util.dpToPixel(mContext, value.intValue()));
                    break;
                case R.id.input_layout_height:
                    mFlexItem.setHeight(Util.dpToPixel(mContext, value.intValue()));
                    break;
                case R.id.input_layout_flex_basis_percent:
                    if (value.intValue() != FlexboxLayout.LayoutParams.FLEX_BASIS_PERCENT_DEFAULT) {
                        mFlexItem.setFlexBasisPercent((float) (value.intValue() / 100.0));
                    } else {
                        mFlexItem.setFlexBasisPercent(FlexItem.FLEX_BASIS_PERCENT_DEFAULT);
                    }
                    break;
                case R.id.input_layout_min_width:
                    mFlexItem.setMinWidth(Util.dpToPixel(mContext, value.intValue()));
                    break;
                case R.id.input_layout_min_height:
                    mFlexItem.setMinHeight(Util.dpToPixel(mContext, value.intValue()));
                    break;
                case R.id.input_layout_max_width:
                    mFlexItem.setMaxWidth(Util.dpToPixel(mContext, value.intValue()));
                    break;
                case R.id.input_layout_max_height:
                    mFlexItem.setMaxHeight(Util.dpToPixel(mContext, value.intValue()));
                    break;
            }
        }
    }
}
