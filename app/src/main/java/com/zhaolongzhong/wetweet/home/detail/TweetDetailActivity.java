package com.zhaolongzhong.wetweet.home.detail;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.zhaolongzhong.wetweet.R;
import com.zhaolongzhong.wetweet.main.RestApplication;
import com.zhaolongzhong.wetweet.models.Media;
import com.zhaolongzhong.wetweet.models.Tweet;
import com.zhaolongzhong.wetweet.search.SearchActivity;
import com.zhaolongzhong.wetweet.services.RestClient;

import org.json.JSONObject;
import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import static com.activeandroid.Cache.getContext;

/**
 * Created by Zhaolong Zhong on 8/4/16.
 */

public class TweetDetailActivity extends AppCompatActivity {
    private static final String TAG = TweetDetailActivity.class.getSimpleName();

    private static final String TWEET_ID = "tweetId";
    private static final String EXTRA_TWEET = "extraTweet";

    private RestClient client;
    private Tweet tweet;

    @BindView(R.id.tweet_detail_activity_toolbar_id) Toolbar toolbar;
    @BindView(R.id.tweet_detail_activity_toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.tweet_detail_activity_toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.tweet_detail_activity_toolbar_search_image_view_id) ImageView searchImageView;

    @BindView(R.id.tweet_detail_activity_profile_image_view_id) ImageView profileImageView;
    @BindView(R.id.tweet_detail_activity_name_text_view_id) TextView nameTextView;
    @BindView(R.id.tweet_detail_activity_screen_name_text_view_id) TextView screenNameTextView;
    @BindView(R.id.tweet_detail_activity_tweet_text_view_id) TextView tweetTextView;
    @BindView(R.id.tweet_detail_activity_media_image_view_id) ImageView mediaImageView;
    @BindView(R.id.tweet_detail_activity_media_video_view_id) VideoView mediaVideoView;
    @BindView(R.id.tweet_detail_activity_created_at_text_view_id) TextView createdAtTextView;

    @BindView(R.id.tweet_detail_activity_retweet_count_text_view_id) TextView retweetCountTextView;
    @BindView(R.id.tweet_detail_activity_likes_count_text_view_id) TextView likesCountTextView;

    @BindView(R.id.tweet_detail_activity_action_reply_image_view_id) ImageView replayImageView;
    @BindView(R.id.tweet_detail_activity_action_retweet_image_view_id) ImageView retweetImageView;
    @BindView(R.id.tweet_detail_activity_action_like_image_view_id) ImageView likeImageView;
    @BindView(R.id.tweet_detail_activity_reply_edit_text_id) EditText replyEditText;
    @BindView(R.id.tweet_detail_activity_bottom_relative_layout_id) RelativeLayout replyBottomRelativeLayout;
    @BindView(R.id.tweet_detail_activity_remaining_character_text_view_id) TextView remainingCountTextView;
    @BindView(R.id.tweet_detail_activity_reply_button_id) Button replayButton;

    public static void newInstance(Context context, Tweet tweet) {
        Intent intent = new Intent(context, TweetDetailActivity.class);
        intent.putExtra(EXTRA_TWEET, Parcels.wrap(tweet));
        context.startActivity(intent);
    }

    public static void newInstance(Context context, long tweetId) {
        Intent intent = new Intent(context, TweetDetailActivity.class);
        intent.putExtra(TWEET_ID, tweetId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tweet_detail_activity);
        ButterKnife.bind(this);

        setupToolbar();

//        tweet = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_TWEET));
        tweet = Tweet.getTweetById(getIntent().getLongExtra(TWEET_ID, 0));
        client = RestApplication.getRestClient();

        invalidateViews();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        titleTextView.setText(getString(R.string.tweet));
        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
        searchImageView.setOnClickListener(v -> {
            SearchActivity.newInstance(this);
            overridePendingTransition(R.anim.right_in, R.anim.stay);
        });
    }

    private void invalidateViews() {
        Glide.with(this).load(tweet.getUser().getProfileImageUrl()).into(profileImageView);
        nameTextView.setText(tweet.getUser().getName());
        screenNameTextView.setText("@" + tweet.getUser().getScreenName());
        tweetTextView.setText(tweet.getBody());
        retweetCountTextView.setText(String.valueOf(tweet.getRetweetCount()));
        likesCountTextView.setText(String.valueOf(tweet.getFavoriteCount()));

        //Example input string: Mon Apr 01 04:01:00 +0000 2016
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat simpleDateFormate = new SimpleDateFormat(twitterFormat, Locale.US);
        simpleDateFormate.setLenient(true);

        try {
            Date createAtDate = simpleDateFormate.parse(tweet.getCreatedAt());
            SimpleDateFormat newFormat = new SimpleDateFormat("hh:mm a dd MMM yyyy", Locale.US);
            createdAtTextView.setText(newFormat.format(createAtDate));
        } catch (ParseException e) {
            Log.e(TAG, "Error in parsing raw date string.", e);
        }

        // media
        setupMedia();

        replyEditText.setHint("Reply to " + tweet.getUser().getName());
        replyEditText.addTextChangedListener(tweetTextWatcher);
        replyEditText.setOnClickListener(editTextOnClickListener);
        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (replyEditText.getText().length() <= 140 && replyEditText.getText().toString().contains("@" + tweet.getUser().getScreenName().toLowerCase())) {
                    client.replyTweet(String.valueOf(tweet.getId()), replyEditText.getText().toString(), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                            Log.d(TAG, "onSuccess: " + statusCode + ", " + json);
                            closeSoftKeyboard();
                            //invalidateViews();
                            //todo: add reply list
                            close();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                            Log.d(TAG, "onFailure: " + statusCode + ", " + jsonObject);
                        }
                    });
                } else {
                    //todo: handle incorrect input
                }
            }
        });
    }

    private void setupMedia() {
        mediaImageView.setVisibility(View.GONE);
        mediaVideoView.setVisibility(View.GONE);

        if (tweet.getMedias().size() == 0) {
            return;
        }

        Media media = tweet.getMedias().get(0);

        if (media.getType().toLowerCase().contains("video")) {
            mediaVideoView.setVisibility(View.VISIBLE);
            mediaVideoView.setVideoPath(media.getVideoUrl());
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(mediaVideoView);
            mediaVideoView.setMediaController(mediaController);

            mediaVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                // Close the progress bar and play the video
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaVideoView.start();
                    mediaVideoView.canPause();
                    mediaController.show(2000);
                }
            });
            mediaVideoView.requestFocus();

        } else if(media.getType().toLowerCase().contains("animated_gif")) {
            mediaVideoView.setVisibility(View.VISIBLE);
            mediaVideoView.setVideoPath(media.getVideoUrl());
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(mediaVideoView);
            mediaVideoView.setMediaController(mediaController);

            mediaVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                // Close the progress bar and play the video
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });

            mediaVideoView.requestFocus();

        } else if (media.getType().equals("photo")) {
            mediaImageView.setVisibility(View.VISIBLE);
            Glide.with(getContext()).load(media.getMediaUrl()).into(mediaImageView);
        } else {
            mediaImageView.setVisibility(View.GONE);
            mediaVideoView.setVisibility(View.GONE);
        }
    }

    private View.OnClickListener editTextOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            replyEditText.setFocusable(true);
            replyEditText.setFocusableInTouchMode(true);
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null){
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
            }
            replyEditText.requestFocus();
            replyEditText.setText("@" + tweet.getUser().getScreenName() + " ");
            replyEditText.setSelection(replyEditText.getText().length());
            replyBottomRelativeLayout.setVisibility(View.VISIBLE);

            int currentLength = replyEditText.getText().length();
            remainingCountTextView.setText(String.valueOf(140 - currentLength));
        }
    };

    private TextWatcher tweetTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            int remainingNumber = 140 - replyEditText.getText().length();
            remainingCountTextView.setText(String.valueOf(remainingNumber));
            remainingCountTextView.setTextColor(ContextCompat.getColor(TweetDetailActivity.this,
                    remainingNumber >= 0 ? R.color.gray : R.color.red));
        }
    };

    private void getDetailAsync(String tweetId) {
        client.getTweetById(tweetId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                Log.d(TAG, "onSuccess: " + statusCode + ", " + json);
                tweet.updateFromJSONInDetail(json);
                invalidateViews();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                Log.d(TAG, "onFailure: " + statusCode + ", " + jsonObject);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tweet_detail, menu);
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
        close();
    }

    public void close() {
        closeSoftKeyboard();
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
