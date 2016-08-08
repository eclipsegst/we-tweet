package com.zhaolongzhong.wetweet.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zhaolongzhong.wetweet.R;
import com.zhaolongzhong.wetweet.home.TweetFragment;
import com.zhaolongzhong.wetweet.home.create.NewTweetActivity;
import com.zhaolongzhong.wetweet.messages.MessagesFragment;
import com.zhaolongzhong.wetweet.models.User;
import com.zhaolongzhong.wetweet.moments.MomentsFragment;
import com.zhaolongzhong.wetweet.nav.ConnectActivity;
import com.zhaolongzhong.wetweet.nav.HighlightsActivity;
import com.zhaolongzhong.wetweet.nav.ListsActivity;
import com.zhaolongzhong.wetweet.nav.ProfileActivity;
import com.zhaolongzhong.wetweet.notifications.NotificationsFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.zhaolongzhong.wetweet.oauth.LoginActivity.LOGIN_USER_ID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SWITCH_COMPAT = "switchCompat";

    private static final int[] tabTitles = {
            R.string.fragment_title_home,
            R.string.fragment_title_moments,
            R.string.fragment_title_notifications,
            R.string.fragment_title_messages
    };

    private static final int[] tabIcons = {
            R.drawable.ic_home_black_24dp,
            R.drawable.ic_flash_on_black_24dp,
            R.drawable.ic_notifications_black_24dp,
            R.drawable.ic_mail_black_24dp
    };

    private User currentUser;
    private ActionBarDrawerToggle drawerToggle;

    @BindView(R.id.main_activity_drawer_layout_id) DrawerLayout drawerLayout;
    @BindView(R.id.main_activity_navigation_view_id) NavigationView navigationView;
    @BindView(R.id.main_activity_content_toolbar_id) Toolbar toolbar;
    @BindView(R.id.main_activity_content_toolbar_profile_circle_image_view_id) CircleImageView toolbarCircleImageView;
    @BindView(R.id.main_activity_content_toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.main_activity_content_view_pager_id) ViewPager viewPager;
    @BindView(R.id.main_activity_content_tab_layout_id) TabLayout tabLayout;
    @BindView(R.id.main_activity_content_fab_id) FloatingActionButton fab;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        SharedPreferences sharedPref = getSharedPreferences(LOGIN_USER_ID, Context.MODE_PRIVATE);
        long loginUserId= sharedPref.getLong(LOGIN_USER_ID, -1);
        currentUser = User.getUserById(loginUserId);

        setupToolbarDrawerPager();

        fab.setOnClickListener(v -> {
            NewTweetActivity.newInstance(this);
            overridePendingTransition(R.anim.bottom_in, R.anim.stay);
        });
    }

    private void setupToolbarDrawerPager() {
        // Toolbar
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!= null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        titleTextView = (TextView) toolbar.findViewById(R.id.main_activity_content_toolbar_title_text_view_id);
        toolbarCircleImageView.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        Glide.with(MainActivity.this)
                .load(currentUser.getProfileImageUrl())
                .into(toolbarCircleImageView);

        // Drawer layout
        if (navigationView != null) {
            setupDrawerContent(navigationView);
            View headView = navigationView.getHeaderView(0);
            CircleImageView navHeaderCircleImageView =  (CircleImageView) headView.findViewById(R.id.nav_header_avatar_circle_image_view_id);
            ImageView backdropImageView = (ImageView) headView.findViewById(R.id.nav_header_backdrop_image_view_id);
            Glide.with(MainActivity.this)
                    .load(currentUser.getProfileImageUrl())
                    .into(navHeaderCircleImageView);
            Glide.with(this)
                    .load("https://pbs.twimg.com//profile_background_images//458377954972532736//xJrn-hCj.jpeg")//todo: replace with live data
                    .into(backdropImageView);
            backdropImageView.setVisibility(View.VISIBLE);
        }

        drawerToggle = setupDrawerToggle();
        drawerToggle.setDrawerIndicatorEnabled(false);
        drawerLayout.addDrawerListener(drawerToggle);

        // View pager
        setupViewPager(viewPager);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Set up tab layout
     */
    private void setupTabLayout(TabLayout tabLayout) {
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

        tabLayout.setupWithViewPager(viewPager);
        setupTabLayout(tabLayout);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        Menu menu = navigationView.getMenu();
        setupSwitchCompat(menu);

        navigationView.setNavigationItemSelectedListener((MenuItem menuItem) -> {
            selectDrawerItem(menuItem);
            return true;
        });
    }

    private void setupSwitchCompat(Menu menu) {
        MenuItem item = menu.findItem(R.id.nav_night_mode_id);
        View actionView = MenuItemCompat.getActionView(item);
        SwitchCompat switchCompat = (SwitchCompat) actionView.findViewById(R.id.action_view_switch_id);

        SharedPreferences sharedPref = getSharedPreferences(SWITCH_COMPAT, Context.MODE_PRIVATE);
        boolean isChecked = sharedPref.getBoolean(SWITCH_COMPAT, false);
        switchCompat.setChecked(isChecked);
//        setNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);// result in infinite loop

        switchCompat.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(SWITCH_COMPAT, switchCompat.isChecked());
            editor.apply();
            setNightMode(switchCompat.isChecked() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.nav_profile:
                ProfileActivity.newInstance(this);
                break;
            case R.id.nav_highlights:
                HighlightsActivity.newInstance(this);
                break;
            case R.id.nav_lists:
                ListsActivity.newInstance(this);
                break;
            case R.id.nav_discussion:
                ConnectActivity.newInstance(this);
                break;
            case R.id.nav_twitter_ads_id:
                Log.d(TAG, "Twitter Ads clicked.");
                break;
            case R.id.nav_night_mode_id:
                Log.d(TAG, "Night mode clicked.");
                break;
            case R.id.nav_settings_id:
                Log.d(TAG, "Settings clicked.");
                break;
            case R.id.nav_help_id:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://support.twitter.com/"));
                startActivity(browserIntent);
                break;
            default:
                break;
        }

        menuItem.setChecked(true);
        drawerLayout.closeDrawers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setNightMode(@AppCompatDelegate.NightMode int nightMode) {
        AppCompatDelegate.setDefaultNightMode(nightMode);

        if (Build.VERSION.SDK_INT >= 11) {
            recreate();
        }
    }

    private static class ViewPagerFragmentAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragments = new ArrayList<>();
        private final List<String> fragmentTitles = new ArrayList<>();

        public ViewPagerFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            fragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }
    }
}
