package com.zhaolongzhong.wetweet.services;

import android.content.Context;
import android.text.TextUtils;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.zhaolongzhong.wetweet.models.User;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

/**
 * Created by Zhaolong Zhong on 8/3/16.
 */

public class RestClient extends OAuthBaseClient {
    private static final String TAG = RestClient.class.getSimpleName();

    public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class;
    public static final String REST_URL = "https://api.twitter.com/1.1";
    public static final String REST_CONSUMER_KEY = "xmnbz8aV49rka1EsbaLPphqTS";
    public static final String REST_CONSUMER_SECRET = "IUKtVsuOHAoCspH8teZzcp2MXTYK3DMZO13FX7JRFbU1fvih3I";
    public static final String REST_CALLBACK_URL = "oauth://zhaolongzhong.com"; // Change this (here and in manifest)

    public RestClient(Context context) {
        super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
    }

    public void getHomeTimeline(int count, int page, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/home_timeline.json");
        RequestParams params = new RequestParams();
        params.put("include_entities", true);
        params.put("page", String.valueOf(page));
        params.put("count", count);
        client.get(apiUrl, params, handler);
    }

    public void getMentionsTimeline(int count, int page, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/mentions_timeline.json");
        RequestParams params = new RequestParams();
        params.put("include_entities", true);
        params.put("page", String.valueOf(page));
        params.put("count", count);
        client.get(apiUrl, params, handler);
    }

    public void getUserTimeline(int count, int page, String screenName, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/user_timeline.json");
        RequestParams params = new RequestParams();
        params.put("include_entities", true);
        params.put("page", String.valueOf(page));
        params.put("count", count);
        params.put("screen_name", screenName);
        client.get(apiUrl, params, handler);
    }

    public void getUserByScreenName(String screenName, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("users/show.json");
        RequestParams params = new RequestParams();
        params.put("include_entities", true);
        params.put("screen_name", screenName);
        client.get(apiUrl, params, handler);
    }

    public void postTweet(String body, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/update.json");
        RequestParams params = new RequestParams();
        params.put("status", body);
        getClient().post(apiUrl, params, handler);
    }

    public void replyTweet(String tweetId, String body, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/update.json");
        RequestParams params = new RequestParams();

        params.put("status", body);
        params.put("in_reply_to_status_id", tweetId);
        getClient().post(apiUrl, params, handler);
    }

    public void getTweetById(String id, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("/statuses/show.json");
        RequestParams params = new RequestParams();
        params.put("id", id);
        client.get(apiUrl, params, handler);
    }

    public void verifyCredentials(int page, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("account/verify_credentials.json");
        RequestParams params = new RequestParams();
        params.put("include_entities", "true");
        params.put("page", String.valueOf(page));
        client.get(apiUrl, params, handler);
    }

    public void updateProfile(RequestParams params, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("account/update_profile.json");
        getClient().post(apiUrl, params, handler);
    }

    /**
     * Follow a user
     * @param screenName
     * @param userId
     * @param follow - Enable notifications for the target user.
     * @param handler
     */
    public void follow(String screenName, String userId, boolean follow, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("friendships/create.json");

        RequestParams params = new RequestParams();
        if (!TextUtils.isEmpty(screenName)) {
            params.put(User.JSON_KEY_SCREEN_NAME, screenName);
        } else {
            params.put("user_id", userId);
        }

        params.put("follow", follow);
        getClient().post(apiUrl, params, handler);
    }

    /**
     * Unfollow a user
     * @param screenName
     * @param userId
     * @param handler
     */
    public void unfollow(String screenName, String userId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("friendships/destroy.json");

        RequestParams params = new RequestParams();
        if (!TextUtils.isEmpty(screenName)) {
            params.put(User.JSON_KEY_SCREEN_NAME, screenName);
        } else {
            params.put("user_id", userId);
        }
        getClient().post(apiUrl, params, handler);
    }

    /**
     * Returns a cursored collection of user objects for every user the specified user is following
     * @param screenName
     * @param userId
     * @param count
     * @param handler
     *
     * Reference:
     * https://dev.twitter.com/rest/reference/get/friends/list
     */
    public void getFriends(String screenName, String userId, int count, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("friends/list.json");

        RequestParams params = new RequestParams();
        if (!TextUtils.isEmpty(screenName)) {
            params.put(User.JSON_KEY_SCREEN_NAME, screenName);
        } else {
            params.put("user_id", userId);
        }

        params.put("include_user_entities", true);
        params.put("count", count);
        params.put("cursor", -1);
        params.put("skip_status", true);

        getClient().get(apiUrl, params, handler);
    }

    /**
     * Returns a cursored collection of user objects for users following the specified user.
     * @param screenName
     * @param userId
     * @param count
     * @param handler
     *
     * Reference:
     * https://dev.twitter.com/rest/reference/get/followers/list
     */
    public void getFollowers(String screenName, String userId, int count, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("followers/list.json");

        RequestParams params = new RequestParams();
        if (!TextUtils.isEmpty(screenName)) {
            params.put(User.JSON_KEY_SCREEN_NAME, screenName);
        } else {
            params.put("user_id", userId);
        }

        params.put("include_user_entities", true);
        params.put("count", count);
        params.put("cursor", -1);
        params.put("skip_status", true);

        getClient().get(apiUrl, params, handler);
    }

    /**
     * Get friends Ids
     * @param screenName
     * @param count
     * @param handler
     */
    public void getFriendsIds(String screenName, int count, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("friends/ids.json");

        RequestParams params = new RequestParams();
        params.put(User.JSON_KEY_SCREEN_NAME, screenName);
        params.put("count", count);
        params.put("cursor", -1);

        getClient().get(apiUrl, params, handler);
    }

    /**
     * Get followers Ids
     * @param screenName
     * @param count
     * @param handler
     */
    public void getFollowersIds(String screenName, int count, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("followers/ids.json");

        RequestParams params = new RequestParams();
        params.put(User.JSON_KEY_SCREEN_NAME, screenName);
        params.put("count", count);
        params.put("cursor", -1);

        getClient().get(apiUrl, params, handler);
    }

    /**
     * Favorite a tweet
     * @param tweetId
     * @param handler
     */
    public void favorite(String tweetId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("favorites/create.json");

        RequestParams params = new RequestParams();
        params.put("id", tweetId);
        getClient().post(apiUrl, params, handler);
    }

    /**
     * Unfavorite a tweet
     * @param tweetId
     * @param handler
     */
    public void unfavorite(String tweetId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("favorites/destroy.json");

        RequestParams params = new RequestParams();
        params.put("id", tweetId);
        getClient().post(apiUrl, params, handler);
    }

    /**
     * Get a list of favorite tweets
     * @param screenName
     * @param userId
     * @param count
     * @param handler
     */
    public void getFavorites(String screenName, String userId, int count, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("favorites/list.json");

        RequestParams params = new RequestParams();
        if (!TextUtils.isEmpty(screenName)) {
            params.put(User.JSON_KEY_SCREEN_NAME, screenName);
        } else {
            params.put("user_id", userId);
        }

        params.put("include_entities", true);
        params.put("count", count);

        getClient().get(apiUrl, params, handler);
    }

    /**
     * Retweet
     * @param tweetId
     * @param handler
     */
    public void retweet(String tweetId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/retweet/" + tweetId + ".json");
        getClient().post(apiUrl, null, handler);
    }

    /**
     * Unretweet a tweet
     * @param tweetId
     * @param handler
     */
    public void unretweet(String tweetId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/unretweet" + tweetId + ".json");
        getClient().post(apiUrl, null, handler);
    }

    public void search(String query, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("search/tweets.json");
        RequestParams params = new RequestParams();
        params.put("result_type", "mixed");
        params.put("q", query);

        client.get(apiUrl, params, handler);
    }
}