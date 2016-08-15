package com.zhaolongzhong.wetweet.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.zhaolongzhong.wetweet.R;
import com.zhaolongzhong.wetweet.home.TweetsAdapter;
import com.zhaolongzhong.wetweet.main.RestApplication;
import com.zhaolongzhong.wetweet.models.Tweet;
import com.zhaolongzhong.wetweet.services.RestClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

/**
 * Created by Zhaolong Zhong on 8/5/16.
 */

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = SearchActivity.class.getSimpleName();

    private RestClient client;
    private ArrayList<Tweet> tweets;
    private TweetsAdapter tweetsAdapter;
    private LinearLayoutManager linearLayoutManager;
    private boolean isRefresh = false;

    @BindView(R.id.search_activity_toolbar_id) Toolbar toolbar;
    @BindView(R.id.search_activity_toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.search_activity_toolbar_search_edit_text_id) EditText searchEditText;
    @BindView(R.id.search_activity_close_image_view_id) ImageView closeImageView;
    @BindView(R.id.search_activity_recycler_view_id) RecyclerView recyclerView;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, SearchActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        ButterKnife.bind(this);

        client = RestApplication.getRestClient();

        setupToolbar();

        searchEditText.addTextChangedListener(searchTextWatcher);
        closeImageView.setOnClickListener(v -> {
            searchEditText.setText("");
        });

        client = RestApplication.getRestClient();
        tweets = new ArrayList<>();
        tweets.addAll(Tweet.getAllTweets());

        linearLayoutManager = new LinearLayoutManager(this);
        setupRecyclerView(recyclerView);
//        setupSwipeRefreshLayout();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }
        });
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        tweetsAdapter = new TweetsAdapter(this, tweets);
        recyclerView.setAdapter(tweetsAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
//            @Override
//            public void onLoadMore(int page, int totalItemsCount) {
//                populateTimeline(TWEET_COUNT_PER_PAGE, page);
//            }
//        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        backImageView.setOnClickListener(v -> close());
    }

    private TextWatcher searchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            search(searchEditText.getText().toString());
        }

        @Override
        public void afterTextChanged(Editable editable) {
            closeImageView.setVisibility(searchEditText.getText().length() == 0 ? View.GONE : View.VISIBLE);
            // todo: add search logic
        }
    };

    private void search(String query) {
        client.search(query, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                Log.d(TAG, "onSuccess: " + statusCode + ", " + jsonObject);
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("statuses");
                    ArrayList<Tweet> moreTweets = Tweet.fromJSONArray(jsonArray);
                    int currentSize = tweetsAdapter.getItemCount();

//                if (page == 1) {
//                    tweets.clear(); // todo: figure the issue in zero page.
//                }

                    tweetsAdapter.clear();
                    tweetsAdapter.addAll(moreTweets);

//                swipeRefreshLayout.setRefreshing(false);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                Log.d(TAG, "onFailure: " + statusCode + ", " + jsonObject);
            }
        });
    }

    @Override
    public void onBackPressed() {
        close();
    }

    public void close() {
        finish();
        overridePendingTransition(0, R.anim.right_out);
    }

    public void closeSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        View view = this.getCurrentFocus();
        if (inputMethodManager != null && view != null){
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
