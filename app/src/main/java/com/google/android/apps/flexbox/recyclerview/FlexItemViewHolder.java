package com.google.android.apps.flexbox.recyclerview;

import com.google.android.apps.flexbox.R;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * ViewHolder implementation for a flex item.
 */
public class FlexItemViewHolder extends RecyclerView.ViewHolder {

    TextView mTextView;

    public FlexItemViewHolder(View itemView) {
        super(itemView);

        mTextView = (TextView) itemView.findViewById(R.id.textview);
    }
}
