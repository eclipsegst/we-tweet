package com.zhaolongzhong.wetweet.home.create;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.zhaolongzhong.wetweet.R;
import com.zhaolongzhong.wetweet.main.RestApplication;
import com.zhaolongzhong.wetweet.services.RestClient;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

/**
 * Created by Zhaolong Zhong on 8/4/16.
 */

public class NewTweetActivity extends AppCompatActivity {
    private static final String TAG = NewTweetActivity.class.getSimpleName();

    private RestClient client;

    @BindView(R.id.new_tweet_activity_toolbar_id) Toolbar toolbar;
    @BindView(R.id.new_tweet_activity_close_image_view_id) ImageView closeImageView;
    @BindView(R.id.new_tweet_activity_profile_image_view_id) ImageView profileImageView;

    @BindView(R.id.new_tweet_activity_edit_text_id) EditText tweetEditText;
    @BindView(R.id.new_tweet_activity_location_image_view_id) ImageView locationImageView;
    @BindView(R.id.new_tweet_activity_camera_image_view_id) ImageView photoImageView;
    @BindView(R.id.new_tweet_activity_gif_image_view_id) ImageView gifImageView;
    @BindView(R.id.new_tweet_activity_poll_image_view_id) ImageView pollImageView;
    @BindView(R.id.new_tweet_activity_remaining_character_text_view_id) TextView remainingCharacterTextView;
    @BindView(R.id.new_tweet_activity_tweet_button_id) Button tweetButton;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, NewTweetActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_tweet_activity);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        // Set status translucent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();

            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // finally change the color
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }

        client = RestApplication.getRestClient();

        closeImageView.setOnClickListener(closeOnClickListener);
        profileImageView.setOnClickListener(v -> Log.d(TAG, "profile icon clicked."));

        Glide.with(this)
                .load("https://pbs.twimg.com/profile_images/675862234266931200/Gqz94bZk_normal.jpg")
                .into(profileImageView);
        tweetEditText.addTextChangedListener(tweetTextWatcher);
        tweetButton.setOnClickListener(v -> {
            // todo: post tweet
            // step 1: get current user
            // step 2: get tweet body
            // step 3: post
            Log.d(TAG, "Post tweet.");
            // todo: add progress bar
            postTweet(tweetEditText.getText().toString());
        });
    }

    private TextWatcher tweetTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            int remainingNumber = 140 - tweetEditText.getText().length();
            remainingCharacterTextView.setText(String.valueOf(remainingNumber));
            remainingCharacterTextView.setTextColor(ContextCompat.getColor(NewTweetActivity.this,
                    remainingNumber >= 0 ? R.color.gray : R.color.red));
        }
    };

    private void postTweet(String body) {
        client.postTweet(body, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                Log.d(TAG, "onSuccess: " + statusCode + ", " + json);
                close();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                Log.d(TAG, "onFailure: " + statusCode + ", " + jsonObject);
                //todo: provide error message.
            }
        });
    }

    /**
     * Show alert to remind user unsaved changed.
     */
    private View.OnClickListener closeOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (tweetEditText.getText().length() == 0) {
                close();
                return;
            }

            checkUnsavedDraft();
        }
    };

    private void checkUnsavedDraft() {
        if (tweetEditText.getText().length() == 0) {
            close();
            return;
        }

        new AlertDialog.Builder(NewTweetActivity.this)
                .setTitle(R.string.save_draft)
                .setPositiveButton(R.string.save, (DialogInterface dialog, int which) -> {
                        //todo: save as draft
                        close();
                    })
                .setNegativeButton(R.string.delete, (DialogInterface dialog, int which) -> close())
                .show();
    }

    private void close() {
        finish();
        overridePendingTransition(R.anim.stay,R.anim.bottom_out);
    }

    @Override
    public void onBackPressed() {
        checkUnsavedDraft();
    }
}