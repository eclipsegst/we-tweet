package com.zhaolongzhong.wetweet.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.zhaolongzhong.wetweet.R;
import com.zhaolongzhong.wetweet.helpers.Helpers;
import com.zhaolongzhong.wetweet.home.EndlessRecyclerViewScrollListener;
import com.zhaolongzhong.wetweet.main.RestApplication;
import com.zhaolongzhong.wetweet.models.User;
import com.zhaolongzhong.wetweet.services.RestClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

/**
 * Created by Zhaolong Zhong on 8/7/16.
 */

public class UserListActivity extends AppCompatActivity{
    private static final String TAG = UserListActivity.class.getSimpleName();

    private static final String SCREEN_NAME = "SCREEN_NAME";
    private static final String FOLLOWING = "FOLLOWING";
    private static final int USER_IDS_COUNT = 100;
    private static final int USER_COUNT_PER_PAGE = 50;

    private User currentUser;
    private RestClient client;
    private ArrayList<User> users;
    private UserAdapter userAdapter;
    private LinearLayoutManager linearLayoutManager;
    private boolean isRefresh = false;
    private boolean isFollowing = true;
    private String screenName;

    @BindView(R.id.user_list_activity_toolbar_id) Toolbar toolbar;
    @BindView(R.id.user_list_activity_toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.user_list_activity_toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.user_list_activity_swipe_refresh_layout_id) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.user_list_activity_recycler_view_id) RecyclerView recyclerView;

    public static void newInstance(Context context, String screenName, boolean isFollowing) {
        Intent intent = new Intent(context, UserListActivity.class);
        intent.putExtra(SCREEN_NAME, screenName);
        intent.putExtra(FOLLOWING, isFollowing);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_list_activity);
        ButterKnife.bind(this);

        isFollowing = getIntent().getBooleanExtra(FOLLOWING, true);
        screenName = getIntent().getStringExtra(SCREEN_NAME);
        client = RestApplication.getRestClient();
        currentUser = User.getUserByScreenName(screenName);
        users = new ArrayList<>();
        Log.d(TAG, "zhao isFollowing:" + isFollowing);
        Log.d(TAG, "zhao ids:" + currentUser.getFriendsIds());

        User loginUser = User.getLoginUser();
        Log.d(TAG, "zhao login user: " + loginUser.getName());
        Log.d(TAG, "zhao login user friends:" + loginUser.getFriendsCount());


        if (isFollowing) {
            users.addAll(currentUser.getFriends());
        } else {
            users.addAll(currentUser.getFollowers());
        }

        linearLayoutManager = new LinearLayoutManager(this);
        setupRecyclerView(recyclerView);
        setupSwipeRefreshLayout();

        populateUserIds(USER_IDS_COUNT);
        invalidateViews();
    }

    private void invalidateViews() {
        if (!Helpers.isOnline()) {
            Toast.makeText(this, "Cannot get users at this time. Please try again later.", Toast.LENGTH_SHORT).show();
        }

        setupToolbar();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        String title = getString(isFollowing ? R.string.following : R.string.followers);
        titleTextView.setText(title);
        backImageView.setOnClickListener(v -> close());
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            isRefresh = true;
            populateUser(USER_COUNT_PER_PAGE, 0);
            if (!Helpers.isOnline()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        userAdapter = new UserAdapter(this, users);
        recyclerView.setAdapter(userAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                populateUser(USER_COUNT_PER_PAGE, page);
            }
        });
    }

    // todo: use bolts to handle populateUserIds and populateUser, like when to dismiss swipe refresh
    private void populateUserIds(int count) {
        if (!Helpers.isOnline()) {
            return;
        }

        if (isFollowing) {
            client.getFriendsIds(screenName, count, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                    Log.d(TAG, "onSuccess friends ids json:" + jsonObject);
                    updateUserIds(jsonObject, true);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                    Log.d(TAG, "onFailure: " + statusCode + ", " + jsonObject);
                }
            });
        } else {
            client.getFollowersIds(screenName, count, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                    Log.d(TAG, "onSuccess followers ids json:" + jsonObject);
                    updateUserIds(jsonObject, false);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                    Log.d(TAG, "onFailure: " + statusCode + ", " + jsonObject);
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }

    private void updateUserIds(JSONObject jsonObject, boolean isFriend) {
        try {
            JSONArray idsArray = jsonObject.getJSONArray("ids");
            currentUser.fromIdsJSONArray(idsArray, isFriend);
        } catch (JSONException e) {
            Log.d(TAG, "Error in parsing ids.", e);
        }

        userAdapter.clear();
        userAdapter.addAll(isFriend ? currentUser.getFriends() : currentUser.getFollowers());
    }

    private void populateUser(int count, int page) {
        if (!Helpers.isOnline()) {
            return;
        }

        if (isFollowing) {
            client.getFriends(screenName, null, count, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                    Log.d(TAG, "onSuccess json:" + jsonObject);
                    updateUserAdapter(jsonObject);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                    Log.d(TAG, "onFailure: " + statusCode + ", " + jsonObject);
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        } else {
            client.getFollowers(screenName, null, count, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                    Log.d(TAG, "onSuccess json:" + jsonObject);
                    updateUserAdapter(jsonObject);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                    Log.d(TAG, "onFailure: " + statusCode + ", " + jsonObject);
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }

    private void updateUserAdapter(JSONObject jsonObject) {
        try {
            JSONArray userArray = jsonObject.getJSONArray("users");
            ArrayList<User> moreUsers = User.fromJSONArray(userArray);
            int currentSize = userAdapter.getItemCount();
            if (isRefresh) {
                userAdapter.clear();
                userAdapter.addAll(moreUsers);
            } else {
                userAdapter.addAll(moreUsers);
                userAdapter.notifyItemRangeInserted(currentSize, users.size() - 1);
            }

            swipeRefreshLayout.setRefreshing(false);
        } catch (JSONException e) {
            Log.e(TAG, "Error in parsing users JSONObject." + jsonObject, e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isRefresh = true;
        populateUser(USER_COUNT_PER_PAGE, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        close();
    }

    public void close() {
        finish();
        overridePendingTransition(0, R.anim.right_out);
    }
}
