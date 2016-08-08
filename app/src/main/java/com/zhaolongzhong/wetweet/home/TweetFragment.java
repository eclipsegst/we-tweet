package com.zhaolongzhong.wetweet.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.zhaolongzhong.wetweet.R;
import com.zhaolongzhong.wetweet.main.RestApplication;
import com.zhaolongzhong.wetweet.models.Tweet;
import com.zhaolongzhong.wetweet.services.RestClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

/**
 * Created by Zhaolong Zhong on 8/4/16.
 */

public class TweetFragment extends Fragment {
    private static final String TAG = TweetFragment.class.getSimpleName();

    private static final int TWEET_COUNT_PER_PAGE = 50;

    private RestClient client;
    private ArrayList<Tweet> tweets;
    private TweetsAdapter tweetsAdapter;
    private LinearLayoutManager linearLayoutManager;
    private boolean isRefresh = false;

    @BindView(R.id.tweet_fragment_swipe_refresh_layout_id) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.tweet_fragment_recycler_view_id) RecyclerView recyclerView;

    public static Fragment newInstance() {
        return new TweetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tweet_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        client = RestApplication.getRestClient();
        tweets = new ArrayList<>();
        tweets.addAll(Tweet.getAllTweets());

        linearLayoutManager = new LinearLayoutManager(getActivity());
        setupToolbar(linearLayoutManager);
        setupRecyclerView(recyclerView);
        setupSwipeRefreshLayout();
    }

    private void setupToolbar(LinearLayoutManager linearLayoutManager) {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.main_activity_content_toolbar_id);
        TextView titleTextView = (TextView) toolbar.findViewById(R.id.main_activity_content_toolbar_title_text_view_id);
        titleTextView.setOnClickListener(v -> linearLayoutManager.scrollToPositionWithOffset(0, 0));
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            isRefresh = true;
            populateTimeline(TWEET_COUNT_PER_PAGE, 0);
        });

        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        tweetsAdapter = new TweetsAdapter(getActivity(), tweets);
        recyclerView.setAdapter(tweetsAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                populateTimeline(TWEET_COUNT_PER_PAGE, page);
            }
        });
    }

    private void populateTimeline(int count, int page) {
        client.getHomeTimeline(count, page, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
                ArrayList<Tweet> moreTweets = Tweet.fromJSONArray(json);
                int currentSize = tweetsAdapter.getItemCount();

//                if (page == 1) {
//                    tweets.clear(); // todo: figure the issue in zero page.
//                }

                if (isRefresh) {
                    tweetsAdapter.clear();
                    tweetsAdapter.addAll(moreTweets);
                } else {
                    tweetsAdapter.addAll(moreTweets);
                    tweetsAdapter.notifyItemRangeInserted(currentSize, tweets.size() - 1);
                }

                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                Log.d(TAG, "onFailure: " + statusCode + ", " + jsonObject);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        isRefresh = true;
        populateTimeline(TWEET_COUNT_PER_PAGE, 0);
    }
}
