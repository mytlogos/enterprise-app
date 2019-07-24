package com.mytlogos.enterprise.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mytlogos.enterprise.R;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class ReadHistoryFragment extends BaseFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 2;
    private ReadHistoryClickListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ReadHistoryFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ReadHistoryFragment newInstance(int columnCount) {
        ReadHistoryFragment fragment = new ReadHistoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.normal_list, container, false);

        Context context = view.getContext();
        RecyclerView recyclerView = view.findViewById(R.id.list);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        recyclerView.setAdapter(new ReadHistoryRecyclerViewAdapter(new ArrayList<>(), mListener));

        // TODO: 22.07.2019 implement functionality and remove this block
        //  as soon as there is a livedata observer, paste the commented block as empty list guard
        TextView textView = view.findViewById(R.id.empty_view);
        recyclerView.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);

/*
            if (checkEmptyList(unreadEpisodes, view, recyclerView)) {
                return;
            }
*/

        this.setTitle("Read History");
        return view;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ReadHistoryClickListener) {
            mListener = (ReadHistoryClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface ReadHistoryClickListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Object item);
    }
}
