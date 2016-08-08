package com.zhaolongzhong.wetweet.home;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.zhaolongzhong.wetweet.R;
import com.zhaolongzhong.wetweet.home.detail.TweetDetailActivity;
import com.zhaolongzhong.wetweet.models.Media;
import com.zhaolongzhong.wetweet.models.Tweet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Created by Zhaolong Zhong on 8/3/16.
 */

public class TweetsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG= TweetsAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_NORMAL = 0;
    private ArrayList<Tweet> tweets;
    private Context context;

    public TweetsAdapter(Context context, ArrayList<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    private Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        return this.tweets.size();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_NORMAL:
                View v1 = inflater.inflate(R.layout.tweet_item_normal, parent, false);
                viewHolder = new ViewHolderNormal(v1);
                break;
            default:
                View view = inflater.inflate(R.layout.tweet_item_normal, parent, false);
                viewHolder = new ViewHolderNormal(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case VIEW_TYPE_NORMAL:
                ViewHolderNormal v1 = (ViewHolderNormal) viewHolder;
                configureViewHolderNormal(v1, position);
                break;
            default:
                break;
        }
    }

    private void configureViewHolderNormal(ViewHolderNormal viewHolder, int position) {
        Tweet tweet = tweets.get(position);
        viewHolder.nameTextView.setText(tweet.getUser().getName());
        viewHolder.screenNameTextView.setText(" @" + tweet.getUser().getScreenName());
        viewHolder.bodyTextView.setText(tweet.getBody());

        viewHolder.retweetCountTextView.setText(String.valueOf(tweet.getRetweetCount()));
        viewHolder.likeCountTextView.setText(String.valueOf(tweet.getFavoriteCount()));

        Glide.with(getContext())
                .load(tweet.getUser().getProfileImageUrl())
                .fitCenter()
                .into(viewHolder.thumbnailImageView);

        viewHolder.relativeTimestampTextView.setText(getRelativeTimeAgo(tweet.getCreatedAt()));
        viewHolder.mView.setOnClickListener(v -> {
            TweetDetailActivity.newInstance(context, tweets.get(position).getId());
            ((Activity)getContext()).overridePendingTransition(R.anim.right_in, R.anim.stay);
        });

        setupActionOnClickListener(viewHolder, tweet);

        if (tweet.getMedias().size() > 0) {

            Media media = tweet.getMedias().get(0);

            viewHolder.mediaPhotoImageView.setVisibility(View.GONE);
            viewHolder.mediaVideoView.setVisibility(View.GONE);

            if (media.getType().toLowerCase().contains("video")) {
                //        viewHolder.mediaVideoView.setVideoPath("http://techslides.com/demos/sample-videos/small.mp4");
                viewHolder.mediaVideoView.setVisibility(View.VISIBLE);
//                viewHolder.mediaVideoView.setVideoPath("https://pbs.twimg.com/tweet_video/CpRzHAKWgAAY0p6.mp4");
                viewHolder.mediaVideoView.setVideoPath(media.getVideoUrl());
                MediaController mediaController = new MediaController(getContext());
                mediaController.setAnchorView(viewHolder.mediaVideoView);
//                viewHolder.mediaVideoView.setMediaController(mediaController);
                viewHolder.mediaVideoView.requestFocus();
                viewHolder.mediaVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    // Close the progress bar and play the video
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.setVolume(0f, 0f);
                        mediaPlayer.setLooping(true);
                        //todo: handle sound properly
                        viewHolder.mediaVideoView.start();
                        viewHolder.mediaVideoView.canPause();
                    }
                });

            } else if(media.getType().toLowerCase().contains("animated_gif")) {
                viewHolder.mediaVideoView.setVisibility(View.VISIBLE);
//                viewHolder.mediaVideoView.setVideoPath("https://pbs.twimg.com/tweet_video/CpRzHAKWgAAY0p6.mp4");
                viewHolder.mediaVideoView.setVideoPath(media.getVideoUrl());
                MediaController mediaController = new MediaController(getContext());
                mediaController.setAnchorView(viewHolder.mediaVideoView);
//                viewHolder.mediaVideoView.setMediaController(mediaController);
                viewHolder.mediaVideoView.requestFocus();
                viewHolder.mediaVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    // Close the progress bar and play the video
                    public void onPrepared(MediaPlayer mp) {
                        viewHolder.mediaVideoView.start();
                    }
                });

            } else if (media.getType().equals("photo")) {
                viewHolder.mediaPhotoImageView.setVisibility(View.VISIBLE);
                Glide.with(getContext()).load(media.getMediaUrl()).into(viewHolder.mediaPhotoImageView);
            } else {
                viewHolder.mediaPhotoImageView.setVisibility(View.GONE);
                viewHolder.mediaVideoView.setVisibility(View.GONE);
            }
        } else {
            viewHolder.mediaPhotoImageView.setVisibility(View.GONE);
            viewHolder.mediaVideoView.setVisibility(View.GONE);
        }
    }

    private void setupActionOnClickListener(ViewHolderNormal viewHolder, final Tweet tweet) {
        viewHolder.replyRelativeLayout.setOnClickListener(v -> {
            Log.d(TAG, "Reply clicked.");
            Toast.makeText(getContext(), "Reply clicked.", Toast.LENGTH_SHORT).show();
            // todo: send network request to update date on server
        });

        viewHolder.retweetRelativeLayout.setOnClickListener(v -> {
            Log.d(TAG, "Reweet clicked.");
            Toast.makeText(getContext(), "Reweet clicked.", Toast.LENGTH_SHORT).show();
            // todo: send network request to update date on server
        });

        viewHolder.likeRelativeLayout.setOnClickListener(v -> {
            boolean isFavorited = tweet.isFavorited();

            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_favorite_black_24dp);
            drawable.setColorFilter(ContextCompat.getColor(getContext(), isFavorited? R.color.blueGray : R.color.red), PorterDuff.Mode.SRC_ATOP);
            viewHolder.likeImageView.setImageDrawable(drawable);

            int flag = isFavorited? -1 : 1;
            int count = Integer.valueOf(viewHolder.likeCountTextView.getText().toString()) + flag;
            viewHolder.likeCountTextView.setText(String.valueOf(count));

            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            tweet.setFavorited(!isFavorited);
            tweet.setFavoriteCount(count);
            realm.commitTransaction();
            realm.close();

            Log.d(TAG, "Like clicked:" + tweet.isFavorited());
            // todo: send network request to update date on server
        });

        viewHolder.messageRelativeLayout.setOnClickListener(v -> {
            Log.d(TAG, "Message clicked.");
            Toast.makeText(getContext(), "Message clicked.", Toast.LENGTH_SHORT).show();
            // todo: send network request to update date on server
        });
    }

    public static class ViewHolderNormal extends RecyclerView.ViewHolder {
        @BindView(R.id.tweet_item_normal_thumbnail_image_view_id) ImageView thumbnailImageView;
        @BindView(R.id.tweet_item_normal_name_text_view_id) TextView nameTextView;
        @BindView(R.id.tweet_item_normal_screen_name_text_view_id) TextView screenNameTextView;
        @BindView(R.id.tweet_item_normal_relative_timestamp_text_view_id) TextView relativeTimestampTextView;
        @BindView(R.id.tweet_item_normal_body_text_view_id) TextView bodyTextView;
        @BindView(R.id.tweet_item_normal_media_photo_image_view_id) ImageView mediaPhotoImageView;
        @BindView(R.id.tweet_item_normal_media_video_video_view_id) VideoView mediaVideoView;

        @BindView(R.id.tweet_item_normal_action_retweet_image_view_id) ImageView retweetImageView;
        @BindView(R.id.tweet_item_normal_action_retweet_count_text_view_id) TextView retweetCountTextView;
        @BindView(R.id.tweet_item_normal_action_like_image_view_id) ImageView likeImageView;
        @BindView(R.id.tweet_item_normal_action_like_count_text_view_id) TextView likeCountTextView;
        @BindView(R.id.tweet_item_normal_action_reply_relative_layout_id) RelativeLayout replyRelativeLayout;
        @BindView(R.id.tweet_item_normal_action_retweet_relative_layout_id) RelativeLayout retweetRelativeLayout;
        @BindView(R.id.tweet_item_normal_action_like_relative_layout_id) RelativeLayout likeRelativeLayout;
        @BindView(R.id.tweet_item_normal_action_message_relative_layout_id) RelativeLayout messageRelativeLayout;

        private View mView;

        public ViewHolderNormal(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;
            bodyTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    // Clean all tweets of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of tweets
    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * Return relative time string.
     *
     * Example input string: Mon Apr 01 04:01:00 +0000 2016
     */
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat simpleDateFormate = new SimpleDateFormat(twitterFormat, Locale.US);
        simpleDateFormate.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = simpleDateFormate.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
        } catch (ParseException e) {
            Log.e(TAG, "Error in parsing raw date string.", e);
        }

        if (relativeDate.contains(" minutes ago")) {
            relativeDate = relativeDate.replace("minutes ago", "m");
        }

        if (relativeDate.contains(" minute ago")) {
            relativeDate = relativeDate.replace("minute ago", "m");
        }

        if (relativeDate.contains(" hours ago")) {
            relativeDate = relativeDate.replace("hours ago", "h");
        }

        if (relativeDate.contains(" hour ago")) {
            relativeDate = relativeDate.replace("hour ago", "h");
        }

        if (relativeDate.contains(" days ago")) {
            relativeDate = relativeDate.replace("days ago", "d");
        }

        // todo: handle year ago
        return relativeDate;
    }
}
