package com.google.android.apps.flexbox;

import com.google.android.apps.flexbox.recyclerview.FlexItemAdapter;
import com.google.android.flexbox.FlexItem;
import com.google.android.flexbox.FlexboxLayoutManager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment that contains the {@link RecyclerView} and the {@link FlexboxLayoutManager} as its
 * LayoutManager for the flexbox playground.
 */
public class RecyclerViewFragment extends Fragment {

    public static RecyclerViewFragment newInstance() {
        return new RecyclerViewFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        final FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager();
        recyclerView.setLayoutManager(flexboxLayoutManager);
        final FlexItemAdapter adapter = new FlexItemAdapter();
        recyclerView.setAdapter(adapter);

        final MainActivity activity = (MainActivity) getActivity();
        final FragmentHelper fragmentHelper = new FragmentHelper(activity, flexboxLayoutManager);
        fragmentHelper.initializeViews();
        FloatingActionButton addFab = (FloatingActionButton) activity.findViewById(R.id.add_fab);
        if (addFab != null) {
            addFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FlexItem flexItem = new FlexboxLayoutManager.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    fragmentHelper.setFlexItemAttributes(flexItem);
                    adapter.addFlexItem(flexItem);
                    // TODO: Specify index?
                    adapter.notifyDataSetChanged();
                }
            });
        }
        FloatingActionButton removeFab = (FloatingActionButton) activity.findViewById(
                R.id.remove_fab);
        if (removeFab != null) {
            removeFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (flexboxLayoutManager.getChildCount() == 0) {
                        return;
                    }
                    flexboxLayoutManager.removeViewAt(flexboxLayoutManager.getChildCount() - 1);
                }
            });
        }
    }
}
