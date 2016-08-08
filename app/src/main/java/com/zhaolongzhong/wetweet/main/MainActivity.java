package com.zhaolongzhong.wetweet.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zhaolongzhong.wetweet.R;
import com.zhaolongzhong.wetweet.home.TweetFragment;
import com.zhaolongzhong.wetweet.home.create.NewTweetActivity;
import com.zhaolongzhong.wetweet.messages.MessagesFragment;
import com.zhaolongzhong.wetweet.models.User;
import com.zhaolongzhong.wetweet.moments.MomentsFragment;
import com.zhaolongzhong.wetweet.notifications.NotificationsFragment;
import com.zhaolongzhong.wetweet.oauth.LoginActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

//    @BindView(R.id.toolbar) Toolbar toolbar;
//    @BindView(R.id.main_activity_logo_image_view_id) ImageView logoImageView;
//    @BindView(R.id.main_activity_recycler_view_id) RecyclerView recyclerView;

    private final int[] tabTitles = {
            R.string.fragment_title_home,
            R.string.fragment_title_moments,
            R.string.fragment_title_notifications,
            R.string.fragment_title_messages
    };

    private final int[] tabIcons = {
            R.drawable.ic_home_black_24dp,
            R.drawable.ic_flash_on_black_24dp,
            R.drawable.ic_notifications_black_24dp,
            R.drawable.ic_mail_black_24dp
    };

    private ActionBar actionBar;

    private DrawerLayout mDrawerLayout;
    private TabLayout tabLayout;
    private CircleImageView circleImageView;
    private CircleImageView navHeaderCircleImageView;
    private TextView titleTextView;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tweet_detail_activity_toolbar_id);
        setSupportActionBar(toolbar);
        circleImageView = (CircleImageView) toolbar.findViewById(R.id.toolbar_avatar_circle_image_view_id);
        circleImageView.setOnClickListener(v -> mDrawerLayout.openDrawer(GravityCompat.START));
        titleTextView = (TextView) toolbar.findViewById(R.id.toolbar_title_text_view_id);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        if (navigationView != null) {
            setupDrawerContent(navigationView);
            View headView = navigationView.getHeaderView(0);
            navHeaderCircleImageView =  (CircleImageView) headView.findViewById(R.id.nav_header_avatar_circle_image_view_id);
            Glide.with(MainActivity.this)
                    .load("https://pbs.twimg.com/profile_images/675862234266931200/Gqz94bZk_normal.jpg")
                    .into(navHeaderCircleImageView);
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            NewTweetActivity.newInstance(MainActivity.this);
            overridePendingTransition(R.anim.bottom_in, R.anim.stay);
        });

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        setUpTabLayout(tabLayout);

        SharedPreferences sharedPref = getSharedPreferences(LoginActivity.LOGIN_USER_ID, Context.MODE_PRIVATE);
        long loginUserId= sharedPref.getLong(LoginActivity.LOGIN_USER_ID, -1);
        User currentUser = User.getUserById(loginUserId);

        Log.d(TAG, "zhao loginUserId:" + loginUserId);

        Glide.with(MainActivity.this)
                .load(currentUser.getProfileImageUrl())
//                .load("https://pbs.twimg.com/profile_images/675862234266931200/Gqz94bZk_normal.jpg")
                .into(circleImageView);

//        client.verifyCredentials(2, new JsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
//                Log.d(TAG, "Credentials : \n" + jsonObject);
//                //todo: add to local db to avoid rate limit exceeded
//                try {
//                    String profileImageUrl = jsonObject.getString("profile_image_url");
//                    Glide.with(MainActivity.this)
//                            .load(profileImageUrl)
//                            .into(circleImageView);
//                } catch (JSONException e) {
//                    Log.e(TAG, "Error in parsing credential object.", e);
//                }
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
//                Log.d(TAG, "onFailure: " + statusCode + ", " + jsonObject);
//            }
//        });
    }

    /**
     * Set up tab layout
     */
    private void setUpTabLayout(TabLayout tabLayout) {
        tabLayout.addOnTabSelectedListener(tabSelectedListener);
        setUpTabLayoutDefaultTabIcon(tabLayout);

        // Set title when loading for the for time
        titleTextView.setText(getString(R.string.fragment_title_home));
        Drawable drawable = ContextCompat.getDrawable(MainActivity.this, tabIcons[0]);
        drawable.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.blue), PorterDuff.Mode.SRC_ATOP);
        tabLayout.getTabAt(0).setIcon(drawable);
    }

    /**
     * Set tab selected listener to change title and tab icon color.
     */
    private TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            titleTextView.setText(getString(tabTitles[tab.getPosition()]));
            setUpTabLayoutDefaultTabIcon(tabLayout);
            Drawable drawable = ContextCompat.getDrawable(MainActivity.this, tabIcons[tab.getPosition()]);
            drawable.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.blue), PorterDuff.Mode.SRC_ATOP);
            tabLayout.getTabAt(tab.getPosition()).setIcon(drawable);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };

    /**
     * Set default tab icon drawable
     */
    private void setUpTabLayoutDefaultTabIcon(TabLayout tabLayout) {
        for (int i = 0; i < 4; i++) {
            Drawable drawable = ContextCompat.getDrawable(this, tabIcons[i]);
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
            tabLayout.getTabAt(i).setIcon(drawable);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerFragmentAdapter adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(TweetFragment.newInstance(), null);
        adapter.addFragment(MomentsFragment.newInstance(), null);
        adapter.addFragment(NotificationsFragment.newInstance(), null);
        adapter.addFragment(MessagesFragment.newInstance(), null);
        viewPager.setAdapter(adapter);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener((MenuItem menuItem) -> {
            menuItem.setChecked(true);
            mDrawerLayout.closeDrawers();
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        switch (AppCompatDelegate.getDefaultNightMode()) {
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                menu.findItem(R.id.menu_night_mode_system).setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_AUTO:
                menu.findItem(R.id.menu_night_mode_auto).setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                menu.findItem(R.id.menu_night_mode_night).setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                menu.findItem(R.id.menu_night_mode_day).setChecked(true);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_night_mode_system:
                setNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case R.id.menu_night_mode_day:
                setNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case R.id.menu_night_mode_night:
                setNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case R.id.menu_night_mode_auto:
                setNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setNightMode(@AppCompatDelegate.NightMode int nightMode) {
        AppCompatDelegate.setDefaultNightMode(nightMode);

        if (Build.VERSION.SDK_INT >= 11) {
            recreate();
        }
    }

    static class ViewPagerFragmentAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public ViewPagerFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
}
