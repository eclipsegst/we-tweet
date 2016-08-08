package com.zhaolongzhong.wetweet.services;

import android.content.Context;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

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

    public void getMentionesTimeline(int count, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/mentions_timeline.json");
        RequestParams params = new RequestParams();
        params.put("include_entities", true);
        params.put("count", count);
        client.get(apiUrl, params, handler);
    }

    public void verifyCredentials(int page, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("account/verify_credentials.json");
        RequestParams params = new RequestParams();
        params.put("include_entities", "true");
        params.put("page", String.valueOf(page));
        client.get(apiUrl, params, handler);
    }
}