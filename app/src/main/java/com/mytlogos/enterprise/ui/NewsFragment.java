package com.mytlogos.enterprise.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mytlogos.enterprise.R;
import com.mytlogos.enterprise.model.News;
import com.mytlogos.enterprise.viewmodel.NewsViewModel;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link NewsClickListener}
 * interface.
 */
public class NewsFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private NewsClickListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NewsFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static NewsFragment newInstance(int columnCount) {
        NewsFragment fragment = new NewsFragment();
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
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        // Set the adapter
        Context context = view.getContext();

        RecyclerView recyclerView = view.findViewById(R.id.list);

        if (mColumnCount <= 1) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);

            DividerItemDecoration decoration = new DividerItemDecoration(context, layoutManager.getOrientation());
            recyclerView.addItemDecoration(decoration);
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        NewsRecyclerViewAdapter adapter = new NewsRecyclerViewAdapter(new ArrayList<>(), mListener, this.getContext());
        recyclerView.setAdapter(adapter);

        NewsViewModel newsViewModel = ViewModelProviders.of(this).get(NewsViewModel.class);
        LiveData<List<News>> newsLiveData = newsViewModel.getNews();

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swiper);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            List<News> news = newsLiveData.getValue();
            DateTime latest = null;

            if (news != null) {
                News latestNews = Collections.max(news, (o1, o2) -> o1.getTimeStamp().compareTo(o2.getTimeStamp()));
                latest = latestNews == null ? null : latestNews.getTimeStamp();
            }
            newsViewModel.refresh(latest).observe(this, loadingComplete -> {
                if (loadingComplete != null && loadingComplete) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        });

        newsLiveData.observe(this, news -> {

            TextView textView = view.findViewById(R.id.empty_view);

            if (news == null || news.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.GONE);
            }
            adapter.setValue(news);
        });

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NewsClickListener) {
            mListener = (NewsClickListener) context;
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
    public interface NewsClickListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(News item);
    }
}
