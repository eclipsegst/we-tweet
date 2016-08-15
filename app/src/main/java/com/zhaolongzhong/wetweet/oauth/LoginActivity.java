package com.zhaolongzhong.wetweet.oauth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;

import com.codepath.oauth.OAuthLoginActionBarActivity;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.zhaolongzhong.wetweet.R;
import com.zhaolongzhong.wetweet.main.MainActivity;
import com.zhaolongzhong.wetweet.main.RestApplication;
import com.zhaolongzhong.wetweet.models.User;
import com.zhaolongzhong.wetweet.services.RestClient;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import io.realm.Realm;

/**
 * Created by Zhaolong Zhong on 8/3/16.
 */

public class LoginActivity extends OAuthLoginActionBarActivity<RestClient> {
    private static final String TAG = LoginActivity.class.getSimpleName();

    public static final String LOGIN_USER_ID = "loginUserId";

    @BindView(R.id.welcome_activity_progress_bar_id) ProgressBar progressBar;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        ButterKnife.bind(this);

        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.blueGray), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public void onLoginSuccess() {
        setupDefaultUser();//todo: remove after finish other features
        SharedPreferences sharedPref = getSharedPreferences(LOGIN_USER_ID, Context.MODE_PRIVATE);
        long loginUserId= sharedPref.getLong(LOGIN_USER_ID, -1);

        if (loginUserId == -1) {
            RestClient client = RestApplication.getRestClient();
            client.verifyCredentials(2, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                    Log.d(TAG, "Credentials : \n" + jsonObject);
                    try {
                        SharedPreferences sharedPref = getSharedPreferences(LOGIN_USER_ID, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putLong(LOGIN_USER_ID, jsonObject.getLong("id"));
                        editor.apply();

                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();

                        User user = new User();
                        user.fromJSON(jsonObject);
                        realm.copyToRealmOrUpdate(user);
                        realm.commitTransaction();
                        realm.close();

                        MainActivity.newInstance(LoginActivity.this);
                        finish();
                    } catch (JSONException e) {
                        Log.e(TAG, "Error in parsing credential object.", e);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                    Log.d(TAG, "onFailure: " + statusCode + ", " + jsonObject);
                }
            });
        } else {
            MainActivity.newInstance(LoginActivity.this);
            finish();
        }
    }

    /**
     * Set default in case of rate limit exceeded.
     */
    private void setupDefaultUser() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        User user = new User();
        user.setId(365265867L);
        user.setName("zhao");
        user.setScreenName("zhaolongz");
        user.setProfileImageUrl("https://pbs.twimg.com/profile_images/675862234266931200/Gqz94bZk_normal.jpg");
        user.setProfileBackgroundImageUrl("http://abs.twimg.com/images/themes/theme14/bg.gif");

        realm.copyToRealmOrUpdate(user);
        realm.commitTransaction();
        realm.close();

        SharedPreferences sharedPref = getSharedPreferences(LOGIN_USER_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(LOGIN_USER_ID, 365265867L);
        editor.apply();
    }

    @Override
    public void onLoginFailure(Exception e) {
        Log.e(TAG, "Login failed.", e);
    }

    public void loginToTwitter(View view) {
        progressBar.setVisibility(View.VISIBLE);
        getClient().connect();
    }
}