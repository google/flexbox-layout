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

package com.google.android.apps.flexbox;

import com.google.android.libraries.flexbox.FlexboxLayout;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * DialogFragment that changes the properties for a flex item.
 */
public class FlexItemEditFragment extends DialogFragment {

    private static final String FLEX_ITEM_KEY = "flex_item";

    private String ALIGN_SELF_AUTO;
    private String ALIGN_SELF_FLEX_START;
    private String ALIGN_SELF_FLEX_END;
    private String ALIGN_SELF_CENTER;
    private String ALIGN_SELF_BASELINE;
    private String ALIGN_SELF_STRETCH;

    private FlexItem mFlexItem;
    private FlexItemChangedListener mFlexItemChangedListener;

    public static FlexItemEditFragment newInstance(FlexItem flexItem) {
        FlexItemEditFragment fragment = new FlexItemEditFragment();
        Bundle args = new Bundle();
        args.putParcelable(FLEX_ITEM_KEY, flexItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
        } else {
            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Dialog);
        }
        Bundle args = getArguments();
        mFlexItem = args.getParcelable(FLEX_ITEM_KEY);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        getDialog().setTitle(String.valueOf(mFlexItem.index + 1));

        final TextInputLayout orderTextInput = (TextInputLayout) view
                .findViewById(R.id.input_layout_order);
        EditText orderEdit = (EditText) view.findViewById(R.id.edit_text_order);
        orderEdit.setText(String.valueOf(mFlexItem.order));
        orderEdit.addTextChangedListener(
                new FlexEditTextWatcher(orderTextInput, new IntegerInputVerifier(),
                        R.string.must_be_integer));

        final TextInputLayout flexGrowInput = (TextInputLayout) view
                .findViewById(R.id.input_layout_flex_grow);
        EditText flexGrowEdit = (EditText) view.findViewById(R.id.edit_text_flex_grow);
        flexGrowEdit.setText(String.valueOf(mFlexItem.flexGrow));
        flexGrowEdit.addTextChangedListener(
                new FlexEditTextWatcher(flexGrowInput, new NonNegativeIntegerInputVerifier(),
                        R.string.must_be_non_negative_integer));

        final TextInputLayout flexShrinkInput = (TextInputLayout) view
                .findViewById(R.id.input_layout_flex_shrink);
        EditText flexShrinkEdit = (EditText) view.findViewById(
                R.id.edit_text_flex_shrink);
        flexShrinkEdit.setText(String.valueOf(mFlexItem.flexShrink));
        flexShrinkEdit.addTextChangedListener(
                new FlexEditTextWatcher(flexShrinkInput, new NonNegativeIntegerInputVerifier(),
                        R.string.must_be_non_negative_integer));

        final TextInputLayout minWidthInput = (TextInputLayout) view
                .findViewById(R.id.input_layout_min_width);
        EditText minWidthEdit = (EditText) view.findViewById(R.id.edit_text_min_width);
        minWidthEdit.setText(String.valueOf(mFlexItem.minWidth));
        minWidthEdit.addTextChangedListener(
                new FlexEditTextWatcher(minWidthInput, new SizeUnitInputVerifier(),
                        R.string.must_be_minus_one_or_minus_two_or_non_negative_integer));

        final TextInputLayout minHeightInput = (TextInputLayout) view
                .findViewById(R.id.input_layout_min_height);
        EditText minHeightEdit = (EditText) view.findViewById(
                R.id.edit_text_min_height);
        minHeightEdit.setText(String.valueOf(mFlexItem.minHeight));
        minHeightEdit.addTextChangedListener(
                new FlexEditTextWatcher(minHeightInput, new SizeUnitInputVerifier(),
                        R.string.must_be_minus_one_or_minus_two_or_non_negative_integer));

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
                    mFlexItem.alignSelf = FlexboxLayout.LayoutParams.ALIGN_SELF_AUTO;
                } else if (selected.equals(ALIGN_SELF_FLEX_START)) {
                    mFlexItem.alignSelf = FlexboxLayout.LayoutParams.ALIGN_SELF_FLEX_START;
                } else if (selected.equals(ALIGN_SELF_FLEX_END)) {
                    mFlexItem.alignSelf = FlexboxLayout.LayoutParams.ALIGN_SELF_FLEX_END;
                } else if (selected.equals(ALIGN_SELF_CENTER)) {
                    mFlexItem.alignSelf = FlexboxLayout.LayoutParams.ALIGN_SELF_CENTER;
                } else if (selected.equals(ALIGN_SELF_BASELINE)) {
                    mFlexItem.alignSelf = FlexboxLayout.LayoutParams.ALIGN_SELF_BASELINE;
                } else if (selected.equals(ALIGN_SELF_STRETCH)) {
                    mFlexItem.alignSelf = FlexboxLayout.LayoutParams.ALIGN_SELF_STRETCH;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No op
            }
        });
        int alignSelfPosition = arrayAdapter.getPosition(alignSelfAsString(mFlexItem.alignSelf));
        alignSelfSpinner.setSelection(alignSelfPosition);

        view.findViewById(
                R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
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
                        flexShrinkInput.isErrorEnabled() || minWidthInput.isErrorEnabled() ||
                        minHeightInput.isErrorEnabled()) {
                    Toast.makeText(getActivity(), R.string.invalid_values_exist, Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (mFlexItemChangedListener != null) {
                    mFlexItemChangedListener.onFlexItemChanged(mFlexItem);
                }
                dismiss();
            }
        });
        return view;
    }

    public void setFlexItemChangedListener(FlexItemChangedListener flexItemChangedListener) {
        mFlexItemChangedListener = flexItemChangedListener;
    }

    private String alignSelfAsString(int alignSelf) {
        switch (alignSelf) {
            case FlexboxLayout.LayoutParams.ALIGN_SELF_AUTO:
                return ALIGN_SELF_AUTO;
            case FlexboxLayout.LayoutParams.ALIGN_SELF_FLEX_START:
                return ALIGN_SELF_FLEX_START;
            case FlexboxLayout.LayoutParams.ALIGN_SELF_FLEX_END:
                return ALIGN_SELF_FLEX_END;
            case FlexboxLayout.LayoutParams.ALIGN_SELF_CENTER:
                return ALIGN_SELF_CENTER;
            case FlexboxLayout.LayoutParams.ALIGN_SELF_BASELINE:
                return ALIGN_SELF_BASELINE;
            case FlexboxLayout.LayoutParams.ALIGN_SELF_STRETCH:
                return ALIGN_SELF_STRETCH;
            default:
                return ALIGN_SELF_AUTO;
        }
    }

    private class FlexEditTextWatcher implements TextWatcher {

        TextInputLayout mTextInputLayout;
        InputVerifier mInputVerifier;
        int mErrorMessageId;

        FlexEditTextWatcher(TextInputLayout textInputLayout, InputVerifier inputVerifier,
                int errorMessageId) {
            mTextInputLayout = textInputLayout;
            mInputVerifier = inputVerifier;
            mErrorMessageId = errorMessageId;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // No op
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mInputVerifier.isValidInput(s)) {
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
            if (mTextInputLayout.isErrorEnabled() || TextUtils.isEmpty(editable)) {
                return;
            }
            int intValue = Integer.valueOf(editable.toString());
            switch (mTextInputLayout.getId()) {
                case R.id.input_layout_order:
                    mFlexItem.order = intValue;
                    break;
                case R.id.input_layout_flex_grow:
                    mFlexItem.flexGrow = intValue;
                    break;
                case R.id.input_layout_flex_shrink:
                    mFlexItem.flexShrink = intValue;
                    break;
                case R.id.input_layout_min_width:
                    mFlexItem.minWidth = intValue;
                    break;
                case R.id.input_layout_min_height:
                    mFlexItem.minHeight = intValue;
                    break;
            }
        }
    }

    /**
     * Verifies the input in a EditText
     */
    private interface InputVerifier {
        boolean isValidInput(CharSequence charSequence);
    }

    private static class IntegerInputVerifier implements InputVerifier {
        @Override
        public boolean isValidInput(CharSequence charSequence) {
            try {
                Integer.parseInt(charSequence.toString());
            } catch (NumberFormatException | NullPointerException e) {
                return false;
            }
            return true;
        }
    }

    private static class NonNegativeIntegerInputVerifier implements InputVerifier {
        @Override
        public boolean isValidInput(CharSequence charSequence) {
            return !TextUtils.isEmpty(charSequence) &&  TextUtils.isDigitsOnly(charSequence)
                    && Integer.valueOf(charSequence.toString()) >= 0;
        }
    }

    private static class SizeUnitInputVerifier implements InputVerifier {
        @Override
        public boolean isValidInput(CharSequence charSequence) {
            // -1 represents match_parent, -2 represents wrap_content
            return !TextUtils.isEmpty(charSequence) &&
                    (TextUtils.isDigitsOnly(charSequence) ||
                            charSequence.toString().equals("-1") ||
                            charSequence.toString().equals("-2"));
        }
    }

    /**
     * A listener that listens to the change of a flex item
     */
    public interface FlexItemChangedListener {
        void onFlexItemChanged(FlexItem flexItem);
    }
}
