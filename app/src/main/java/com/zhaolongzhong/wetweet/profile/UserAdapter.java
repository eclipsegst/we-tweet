package com.zhaolongzhong.wetweet.profile;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.zhaolongzhong.wetweet.R;
import com.zhaolongzhong.wetweet.helpers.Helpers;
import com.zhaolongzhong.wetweet.main.RestApplication;
import com.zhaolongzhong.wetweet.models.User;
import com.zhaolongzhong.wetweet.services.RestClient;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import io.realm.Realm;

/**
 * Created by Zhaolong Zhong on 8/3/16.
 */

public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG= UserAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_NORMAL = 0;
    private ArrayList<User> users;
    private Context context;
    private RestClient client;

    public UserAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
        this.client = RestApplication.getRestClient();
    }

    private Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        return this.users.size();
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
                View v1 = inflater.inflate(R.layout.user_item, parent, false);
                viewHolder = new ViewHolderNormal(v1);
                break;
            default:
                View view = inflater.inflate(R.layout.user_item, parent, false);
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
        User user = users.get(position);
        viewHolder.nameTextView.setText(user.getName());
        viewHolder.screenNameTextView.setText("@" + user.getScreenName());
        viewHolder.bodyTextView.setText(user.getDescription());

        viewHolder.followImageView.setVisibility(user.isFollowing() ? View.GONE : View.VISIBLE);
        viewHolder.followImageView.setOnClickListener(v -> follow(viewHolder, user));
        viewHolder.followingImageView.setVisibility(user.isFollowing() ? View.VISIBLE : View.GONE);
        viewHolder.followingImageView.setOnClickListener(v -> unfollow(viewHolder, user));

        Glide.with(getContext())
                .load(user.getProfileImageUrl())
                .fitCenter()
                .into(viewHolder.thumbnailImageView);

        viewHolder.mView.setOnClickListener(v -> {
            ProfileActivity.newInstance(context, user.getScreenName());
            ((Activity)getContext()).overridePendingTransition(R.anim.right_in, R.anim.stay);
        });
    }

    private void follow(ViewHolderNormal viewHolder, User user) {
        if (!Helpers.isOnline()) {
            Toast.makeText(getContext(), "Cannot follow " +  user.getName() + " at this time. Please try again later.", Toast.LENGTH_SHORT).show();
            return;
        }

        // update local data
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        user.setFollowing(true);
        realm.copyToRealmOrUpdate(user);
        realm.commitTransaction();
        realm.close();

        // update data on server
        client.follow(user.getScreenName(), null, false, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                Log.d(TAG, "follow onSuccess: " + statusCode + ", " + json);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                Log.d(TAG, "follow onFailure: " + statusCode + ", " + jsonObject);
            }
        });

        viewHolder.followImageView.setVisibility(View.GONE);
        viewHolder.followingImageView.setVisibility(View.VISIBLE);
    }

    private void unfollow(ViewHolderNormal viewHolder, User user) {
        if (!Helpers.isOnline()) {
            Toast.makeText(getContext(), "Cannot unfollow " +  user.getName() + " at this time. Please try again later.", Toast.LENGTH_SHORT).show();
            return;
        }

        String screenName = user.getScreenName();

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.unfollow)
                .setMessage("Stop following " + user.getName() + "?")
                .setPositiveButton(R.string.yes, (DialogInterface dialog, int which) -> {
                    // update local data
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    user.setFollowing(false);
                    realm.copyToRealmOrUpdate(user);
                    realm.commitTransaction();
                    realm.close();

                    // update data in server
                    client.unfollow(screenName, null, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                            Log.d(TAG, "unfollow onSuccess: " + statusCode + ", " + json);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.d(TAG, "unfollow onFailure: " + statusCode + ", " + throwable);
                            throwable.printStackTrace();}
                    });

                    viewHolder.followImageView.setVisibility(View.VISIBLE);
                    viewHolder.followingImageView.setVisibility(View.GONE);

                    dialog.dismiss();
                })
                .setNegativeButton(R.string.no, (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                    return;
                })
                .show();
    }

    public static class ViewHolderNormal extends RecyclerView.ViewHolder {
        @BindView(R.id.user_list_item_normal_thumbnail_image_view_id) ImageView thumbnailImageView;
        @BindView(R.id.user_list_item_normal_name_text_view_id) TextView nameTextView;
        @BindView(R.id.user_list_item_normal_screen_name_text_view_id) TextView screenNameTextView;
        @BindView(R.id.user_list_item_normal_following_image_view_id) ImageView followingImageView;
        @BindView(R.id.user_list_item_normal_follow_image_view_id) ImageView followImageView;
        @BindView(R.id.user_list_item_normal_body_text_view_id) TextView bodyTextView;;

        private View mView;

        public ViewHolderNormal(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;
            bodyTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    // Clear all users of the recycler
    public void clear() {
        users.clear();
        notifyDataSetChanged();
    }

    // Add a list of users
    public void addAll(List<User> list) {
        users.addAll(list);
        notifyDataSetChanged();
    }
}
