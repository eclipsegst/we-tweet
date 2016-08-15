package com.zhaolongzhong.wetweet.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.zhaolongzhong.wetweet.R;
import com.zhaolongzhong.wetweet.helpers.Helpers;
import com.zhaolongzhong.wetweet.home.EndlessRecyclerViewScrollListener;
import com.zhaolongzhong.wetweet.home.TweetsAdapter;
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
 * Created by Zhaolong Zhong on 8/10/16.
 */

public class UserTimelineFragment extends Fragment {
    private static final String TAG = UserTimelineFragment.class.getSimpleName();

    private static final String SCREEN_NAME = "screenName";

    private static final int TWEET_COUNT_PER_PAGE = 50;

    private RestClient client;
    private ArrayList<Tweet> tweets;
    private TweetsAdapter tweetsAdapter;
    private LinearLayoutManager linearLayoutManager;
    private boolean isRefresh = false;

    private String screenName;

    @BindView(R.id.user_timeline_fragment_swipe_refresh_layout_id) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.user_timeline_fragment_recycler_view_id) RecyclerView recyclerView;

    public static Fragment newInstance(String screenName) {
        UserTimelineFragment userTimelineFragment = new UserTimelineFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SCREEN_NAME, screenName);
        userTimelineFragment.setArguments(bundle);
        return userTimelineFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_timeline_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        screenName = getArguments().getString(SCREEN_NAME);
        client = RestApplication.getRestClient();
        tweets = new ArrayList<>();
        tweets.addAll(Tweet.getTweetsByScreenName(screenName));

        linearLayoutManager = new LinearLayoutManager(getActivity());
        setupRecyclerView(recyclerView);
        setupSwipeRefreshLayout();
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
        if (!Helpers.isOnline()) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        Log.d(TAG, "populateTimeline:" + screenName);
        client.getUserTimeline(count, page, screenName, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
                ArrayList<Tweet> moreTweets = Tweet.fromJSONArray(json);
                int currentSize = tweetsAdapter.getItemCount();

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
