package com.zhaolongzhong.wetweet.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zhaolongzhong.wetweet.R;
import com.zhaolongzhong.wetweet.models.User;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zhaolong Zhong on 8/13/16.
 */

public class ProfileEditActivity extends AppCompatActivity{
    private static final String TAG = ProfileEditActivity.class.getSimpleName();

    private User user;

    @BindView(R.id.profile_edit_activity_toolbar_id) Toolbar toolbar;
    @BindView(R.id.profile_edit_activity_toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.profile_edit_activity_profile_image_view_id) ImageView profileImageView;
    @BindView(R.id.profile_edit_activity_profile_backdrop_image_view_id) ImageView backdropImageView;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, ProfileEditActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_edit_activity);
        ButterKnife.bind(this);

        user = User.getLoginUser();
        setupToolbar();

        invalidateViews();
        //todo: add other views and update logic
    }

    private void invalidateViews() {
        Glide.with(this)
                .load(user.getProfileBannerImageUrl())
                .centerCrop()
                .into(backdropImageView);

        Glide.with(this)
                .load(user.getProfileImageUrl())
                .into(profileImageView);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        backImageView.setOnClickListener(v -> close());
    }

    private void close() {
        finish();
        overridePendingTransition(R.anim.stay,R.anim.right_out);
    }

    @Override
    public void onBackPressed() {
        close();
    }
}
