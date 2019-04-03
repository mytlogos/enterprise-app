package com.mytlogos.enterprise;

import android.app.SearchManager;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mytlogos.enterprise.model.News;
import com.mytlogos.enterprise.ui.NewsFragment;
import com.mytlogos.enterprise.ui.NewsRecyclerViewAdapter;
import com.mytlogos.enterprise.viewmodel.NewsViewModel;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewsActivity extends AppCompatActivity implements NewsFragment.NewsClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_news);

        RecyclerView recyclerView = findViewById(R.id.list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration decoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        recyclerView.addItemDecoration(decoration);

        NewsRecyclerViewAdapter adapter = new NewsRecyclerViewAdapter(new ArrayList<>(), this, this);
        recyclerView.setAdapter(adapter);

        NewsViewModel newsViewModel = ViewModelProviders.of(this).get(NewsViewModel.class);
        LiveData<List<News>> newsLiveData = newsViewModel.getNews();

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swiper);
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

            TextView textView = findViewById(R.id.empty_view);

            if (news == null || news.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.GONE);
            }
            adapter.setValue(news);
        });
    }

    @Override
    public void onNewsFragmentInteraction(News item) {
        String url = item.getUrl();

        if (url == null || url.length() == 0) {
            Toast.makeText(this, "No Link available", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, url);
        startActivity(intent);
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
        void onNewsFragmentInteraction(News item);
    }
}
