package com.zhaolongzhong.wetweet.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.zhaolongzhong.wetweet.R;
import com.zhaolongzhong.wetweet.oauth.LoginActivity;

/**
 * Created by Zhaolong Zhong on 8/7/16.
 */

public class SplashActivity extends AppCompatActivity{
    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        LoginActivity.newInstance(this);
        finish();
    }
}
