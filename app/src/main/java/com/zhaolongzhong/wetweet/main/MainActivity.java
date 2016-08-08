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

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

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

        SharedPreferences sharedPref = getSharedPreferences(LoginActivity.LOGIN_USER_ID, Context.MODE_PRIVATE);
        long loginUserId= sharedPref.getLong(LoginActivity.LOGIN_USER_ID, -1);
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
            Glide.with(MainActivity.this)
                    .load(currentUser.getProfileImageUrl())
                    .into(navHeaderCircleImageView);
        }

        // View pager
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabLayout(tabLayout);
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
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener((MenuItem menuItem) -> {
            menuItem.setChecked(true);
            drawerLayout.closeDrawers();
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
                drawerLayout.openDrawer(GravityCompat.START);
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
