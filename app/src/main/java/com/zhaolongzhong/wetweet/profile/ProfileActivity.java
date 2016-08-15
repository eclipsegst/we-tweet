package com.zhaolongzhong.wetweet.profile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.zhaolongzhong.wetweet.R;
import com.zhaolongzhong.wetweet.helpers.Helpers;
import com.zhaolongzhong.wetweet.helpers.ViewPagerFragmentAdapter;
import com.zhaolongzhong.wetweet.main.RestApplication;
import com.zhaolongzhong.wetweet.messages.MessagesFragment;
import com.zhaolongzhong.wetweet.models.Tweet;
import com.zhaolongzhong.wetweet.models.User;
import com.zhaolongzhong.wetweet.notifications.NotificationsFragment;
import com.zhaolongzhong.wetweet.search.SearchActivity;
import com.zhaolongzhong.wetweet.services.RestClient;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import io.realm.Realm;
import io.realm.RealmResults;

import static com.zhaolongzhong.wetweet.oauth.LoginActivity.LOGIN_USER_ID;

/**
 * Created by Zhaolong Zhong on 8/7/16.
 */

public class ProfileActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {
    private static final String TAG = ProfileActivity.class.getSimpleName();

    private static final String SCREEN_NAME = "SCREEN_NAME";

    private User currentUser;
    private String screenName;
    private RestClient client;
    private boolean isLoginUser = false;

    @BindView(R.id.profile_activity_collapsing_toolbar_layout_id) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.profile_activity_app_bar_layout_id) AppBarLayout appBarLayout;
    @BindView(R.id.profile_activity_toolbar_id) Toolbar toolbar;
    @BindView(R.id.profile_activity_toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.profile_activity_toolbar_search_image_view_id) ImageView searchImageView;
    @BindView(R.id.profile_activity_profile_backdrop_image_view_id) ImageView backdropImageView;

    @BindView(R.id.profile_activity_profile_image_view_id) ImageView profileImageView;
    @BindView(R.id.profile_activity_edit_profile_text_view_id) TextView editProfileTextView;
    @BindView(R.id.profile_activity_follow_relative_layout_id) RelativeLayout followRelativeLayout;
    @BindView(R.id.profile_activity_push_notification_following_relative_layout_id) RelativeLayout followingRelativeLayout;
    @BindView(R.id.profile_activity_push_notification_image_view_id) ImageView pushNotificationImageView;
    @BindView(R.id.profile_activity_following_image_view_id) ImageView followingImageView;
    @BindView(R.id.profile_activity_name_text_view_id) TextView nameTextView;
    @BindView(R.id.profile_activity_screen_name_text_view_id) TextView screenNameTextView;
    @BindView(R.id.profile_activity_location_relative_layout_id) RelativeLayout locationRelativeLayout;
    @BindView(R.id.profile_activity_location_text_view_id) TextView locationTextView;
    @BindView(R.id.profile_activity_following_count_text_view_id) TextView followingCountTextView;
    @BindView(R.id.profile_activity_following_label_text_view_id) TextView followingLabelTextView;
    @BindView(R.id.profile_activity_followers_count_text_view_id) TextView followersCountTextView;
    @BindView(R.id.profile_activity_followers_label_text_view_id) TextView followersLabelTextView;

    @BindView(R.id.profile_activity_tab_layout_id) TabLayout tabLayout;
    @BindView(R.id.profile_activity_view_pager_id) ViewPager viewPager;
//    @BindView(R.id.profile_activity_user_timeline_recycler_view_id) RecyclerView recyclerView;

    public static void newInstance(Context context, String screenName) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(SCREEN_NAME, screenName);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        ButterKnife.bind(this);

        client = RestApplication.getRestClient();
        this.screenName = getIntent().getStringExtra(SCREEN_NAME).replace("@", "");
        getUserByScreenName();

        Log.d(TAG, "screenName: " + screenName);

        setupToolbar();
//        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//
//        CollapsingToolbarLayout collapsingToolbarLayout =
//                (CollapsingToolbarLayout) findViewById(R.id.main_collapsing);
//
//        collapsingToolbarLayout.setTitle(getString(R.string.app_name));
//        loadBackdrop();
        invalidateViews();

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        backImageView.setOnClickListener(v -> close());
        searchImageView.setOnClickListener(v -> {
            SearchActivity.newInstance(this);
            overridePendingTransition(R.anim.right_in, R.anim.stay);
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerFragmentAdapter adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(UserTimelineFragment.newInstance(screenName), "Tweets");
        adapter.addFragment(NotificationsFragment.newInstance(), "Media");
        adapter.addFragment(MessagesFragment.newInstance(), "Likes");
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
        setupTabLayout(tabLayout);
    }

    /**
     * Set up tab layout
     */
    private void setupTabLayout(TabLayout tabLayout) {
        tabLayout.addOnTabSelectedListener(tabSelectedListener);

//        Drawable drawable = ContextCompat.getDrawable(MainActivity.this, tabIcons[0]);
//        drawable.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.blue), PorterDuff.Mode.SRC_ATOP);
//        tabLayout.getTabAt(0).setIcon(drawable);
    }

    /**
     * Set tab selected listener to change title and tab icon color.
     */
    private TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
//            titleTextView.setText(getString(tabTitles[tab.getPosition()]));
//            setUpTabLayoutDefaultTabIcon(tabLayout);
//            Drawable drawable = ContextCompat.getDrawable(MainActivity.this, tabIcons[tab.getPosition()]);
//            drawable.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.blue), PorterDuff.Mode.SRC_ATOP);
//            tabLayout.getTabAt(tab.getPosition()).setIcon(drawable);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {

    }

    /**
     * Invalidate all views
     */
    private void invalidateViews() {
        if (!Helpers.isOnline()) {
            Toast.makeText(this, "Cannot load the current user's info. Please try again later.", Toast.LENGTH_SHORT).show();
        }

        currentUser = User.getUserByScreenName(screenName);

        if (currentUser == null) {
            return;
        }

        SharedPreferences sharedPref = getSharedPreferences(LOGIN_USER_ID, Context.MODE_PRIVATE);
        long loginUserId= sharedPref.getLong(LOGIN_USER_ID, -1);
        isLoginUser = loginUserId == currentUser.getId();

        String backdropImageUrl;
        if (currentUser.getProfileBannerImageUrl() != null && !currentUser.getProfileBannerImageUrl().isEmpty()) {
            backdropImageUrl = currentUser.getProfileBannerImageUrl();
        } else {
            backdropImageUrl = currentUser.getProfileBackgroundImageUrl();
        }

        if (backdropImageUrl != null && !backdropImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(backdropImageUrl)
                    .centerCrop()
                    .into(backdropImageView);
        }

        Glide.with(this)
                .load(currentUser.getProfileImageUrl())
                .into(profileImageView);

        // top right action buttons
        editProfileTextView.setVisibility(View.GONE);
        followRelativeLayout.setVisibility(View.GONE);
        followingRelativeLayout.setVisibility(View.GONE);
        if (!isLoginUser) {
            if (currentUser.isFollowing()) {
                setupNotificationsFollowingStatus();
            } else {
                followRelativeLayout.setVisibility(View.VISIBLE);
                followRelativeLayout.setOnClickListener(v -> follow());
            }
        } else {
            editProfileTextView.setVisibility(View.VISIBLE);
            editProfileTextView.setOnClickListener(v -> {
                ProfileEditActivity.newInstance(ProfileActivity.this);
                overridePendingTransition(R.anim.right_in, R.anim.stay);
            });
        }

        // name
        nameTextView.setText(currentUser.getName());
        screenNameTextView.setText("@" + currentUser.getScreenName());

        // location
        String location = currentUser.getLocation();
        if (location != null && !location.isEmpty()) {
            locationTextView.setText(location);
            locationRelativeLayout.setVisibility(View.VISIBLE);
        } else {
            locationRelativeLayout.setVisibility(View.GONE);
        }

        // following and followers
        followingCountTextView.setText(String.valueOf(currentUser.getFriendsCount()));
        followersCountTextView.setText(String.valueOf(currentUser.getFollowersCount()));
        followingLabelTextView.setOnClickListener(v -> startUserListActivity(true));
        followersLabelTextView.setOnClickListener(v -> startUserListActivity(false));
    }

    private void getUserByScreenName() {
        client.getUserByScreenName(screenName, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                Log.d(TAG, "getUserByScreenName onSuccess: " + statusCode + ", " + json);

                User user = User.getUserByScreenName(screenName);
                if (user != null) {
                    User.updateFromJSON(user, json);
                } else {
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();

                    user = new User();
                    user.fromJSON(json);

                    realm.copyToRealmOrUpdate(user);
                    realm.commitTransaction();
                    realm.close();
                }
                invalidateViews();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                Log.d(TAG, "getUserByScreenName onFailure: " + statusCode + ", " + jsonObject);

                try {
                    JSONObject messageObject = jsonObject.getJSONArray("errors").getJSONObject(0);
                    Toast.makeText(ProfileActivity.this, messageObject.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Log.e(TAG, "Error getting errors JSONArray.", e);
                }
            }
        });
    }
    private void startUserListActivity(boolean isFollowing) {
        UserListActivity.newInstance(this, currentUser.getScreenName(), isFollowing);
        overridePendingTransition(R.anim.right_in, R.anim.stay);
    }

    private void follow() {
        updateFollowingStatus(true);
        followRelativeLayout.setVisibility(View.GONE);
        setupNotificationsFollowingStatus();
    }

    private void updateFollowingStatus(boolean following) {
        // Update local data
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        currentUser.setFollowing(following);
        realm.copyToRealmOrUpdate(currentUser);
        realm.commitTransaction();
        realm.close();

        // Update data in server
        if (following) {
            client.follow(currentUser.getScreenName(), null, false, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                    Log.d(TAG, "follow onSuccess: " + statusCode + ", " + json);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                    Log.d(TAG, "follow onFailure: " + statusCode + ", " + jsonObject);
                }
            });
        } else {
            client.unfollow(currentUser.getScreenName(), null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                    Log.d(TAG, "unfollow onSuccess: " + statusCode + ", " + json);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d(TAG, "unfollow onFailure: " + statusCode + ", " + throwable);
                    throwable.printStackTrace();}
            });
        }
    }

    private void unfollow() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.unfollow)
                .setMessage("Stop following " + currentUser.getName() + " ?")
                .setPositiveButton(R.string.yes, (DialogInterface dialog, int which) -> {
                    followingRelativeLayout.setVisibility(View.GONE);
                    followRelativeLayout.setVisibility(View.VISIBLE);
                    followRelativeLayout.setOnClickListener(v -> follow());

                    updateFollowingStatus(false);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.no, (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                    return;
                })
                .show();

    }

    private void setupNotificationsFollowingStatus() {
        followingRelativeLayout.setVisibility(View.VISIBLE);
        followingImageView.setOnClickListener(v -> unfollow());
        pushNotificationImageView.setOnClickListener(v -> setPushNotification(false));
        setPushNotification(true);
    }

    private void setPushNotification(boolean isInitialSetup) {
        boolean isNotifications = currentUser.isNotifications();

        if (!isInitialSetup) {
            isNotifications = !isNotifications;

            final boolean notifications = isNotifications;
            if (isNotifications) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.tweet_notifications)
                        .setMessage(R.string.tweet_notifications_message)
                        .setPositiveButton(R.string.ok, (DialogInterface dialog, int which) -> {
                            updateNotifications(notifications, true);
                            dialog.dismiss();
                        })
                        .show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.tweet_notifications_turn_off)
                        .setMessage("You will stop receiving notifications when " + currentUser.getName() + " Tweets.")
                        .setPositiveButton(R.string.ok, (DialogInterface dialog, int which) -> {
                            updateNotifications(notifications, true);
                            dialog.dismiss();
                        })
                        .setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> {
                            dialog.dismiss();
                            return;
                        })
                        .show();
            }
        } else {
            updateNotifications(isNotifications, false);
        }
    }

    private void updateNotifications(boolean isNotifications, boolean updateDataOnServer) {
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_star_black_24dp);
        drawable.setColorFilter(ContextCompat.getColor(this, isNotifications ? R.color.amber : R.color.blueGray), PorterDuff.Mode.SRC_ATOP);

        pushNotificationImageView.setImageDrawable(drawable);
        pushNotificationImageView.setBackgroundResource(isNotifications ? R.drawable.background_round_border_only_amber : R.drawable.background_round_border_only_blue_gray);

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        currentUser.setNotifications(isNotifications);
        realm.copyToRealmOrUpdate(currentUser);
        realm.commitTransaction();
        realm.close();

        if (updateDataOnServer) {
            client.follow(currentUser.getScreenName(), null, isNotifications, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                    Log.d(TAG, "setup Notifications onSuccess: " + statusCode + ", " + json);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                    Log.d(TAG, "setup Notifications  onFailure: " + statusCode + ", " + jsonObject);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int menuRes = isLoginUser ? R.menu.menu_profile_login  : R.menu.menu_profile;
        getMenuInflater().inflate(menuRes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                close();
                return true;
            case R.id.menu_item_copy_link_to_tweet_id:
                Toast.makeText(this, getString(R.string.menu_copy_link_to_tweet), Toast.LENGTH_SHORT).show();
                //todo: add logic
                return true;
            case R.id.menu_item_block_id:
                Toast.makeText(this, getString(R.string.menu_block), Toast.LENGTH_SHORT).show();
                //todo: add logic
                return true;
            case R.id.menu_item_report_ad_id:
                Toast.makeText(this, getString(R.string.menu_report_ad), Toast.LENGTH_SHORT).show();
                //todo: add logic
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        close();
    }

    public void close() {
        User user = User.getUserByScreenName(screenName);
        if (!user.isFollowing()) {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            RealmResults<Tweet> tweets = Tweet.getTweetsByScreenName(screenName);
            tweets.deleteAllFromRealm();
            realm.commitTransaction();
            realm.close();
        }

        finish();
        overridePendingTransition(0, R.anim.right_out);
    }
}
