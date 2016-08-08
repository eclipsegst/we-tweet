package com.zhaolongzhong.wetweet.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Created by Zhaolong Zhong on 8/3/16.
 */

//@Parcel//Replace using id to object
@RealmClass
public class Tweet implements RealmModel {
    private static final String TAG = Tweet.class.getSimpleName();

    @PrimaryKey
    private long id;
    private String body;
    private String createdAt;
    private boolean favorited;
    private boolean retweeted;
    private int retweetCount;
    private int favoriteCount;

    private User user;
    private Media media;

    public long getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public int getRetweetCount() {
        return retweetCount;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public User getUser() {
        return user;
    }

    public ArrayList<Media> getMedias() {
        return new ArrayList<>(Media.getMediaByTweetId(String.valueOf(this.id)));
    }

    public void setRetweeted(boolean retweeted) {
        this.retweeted = retweeted;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public void setRetweetCount(int retweetCount) {
        this.retweetCount = retweetCount;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    // Deserialize the JSON and build Tweet object.
    public void fromJSON(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getLong("id");
            this.body = jsonObject.getString("text");
            this.createdAt = jsonObject.getString("created_at");
            this.favorited = jsonObject.getBoolean("favorited");
            this.retweeted = jsonObject.getBoolean("retweeted");
            this.retweetCount = jsonObject.getInt("retweet_count");
            this.favoriteCount = jsonObject.getInt("favorite_count");
            this.user = new User();
            this.user.fromJSON(jsonObject.getJSONObject("user"));

            this.media = new Media();
            if (jsonObject.has("entities") && jsonObject.getJSONObject("entities").has("media")) {
                JSONArray mediaArray = jsonObject.getJSONObject("entities").getJSONArray("media");
                if (mediaArray.length() > 0) {
                    // todo: we only get the first media for now
                    this.media.fromJSON(mediaArray.getJSONObject(0), String.valueOf(this.id));
                }
            }

            if (jsonObject.has("extended_entities")) {
                // handle other entry

                // handle media
                if (jsonObject.getJSONObject("extended_entities").has("media")) {
                    JSONArray extendedMediaArray = jsonObject.getJSONObject("extended_entities").getJSONArray("media");
                    if (extendedMediaArray.length() > 0) {
                        // todo: we only get the first extended media for now
                        JSONObject extendedMedia = extendedMediaArray.getJSONObject(0);
                        String type = extendedMedia.getString("type");

                        if (extendedMedia.has("video_info")) {
                            JSONArray variantsArray = extendedMedia.getJSONObject("video_info").getJSONArray("variants");

                            if (variantsArray.length() > 0) {
                                String videoUrl = variantsArray.getJSONObject(0).getString("url");
                                if (videoUrl.endsWith(".mp4")) {// only allow mp4 format
                                    String originalType = this.media.getType();
                                    this.media.setType(originalType + "," + type);
                                    this.media.setVideoUrl(videoUrl);
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error in parsing tweet:" + jsonObject.toString(), e);
        }
    }

    public void updateFromJSONInDetail(JSONObject jsonObject) {
        try {
            // Update media
            JSONObject extendedEntitiesJSON = jsonObject.getJSONObject("extended_entities");
            JSONArray mediaArray = extendedEntitiesJSON.getJSONArray("media");
            Media.fromJSONArray(mediaArray, String.valueOf(this.id));

        } catch (JSONException e) {
            Log.e(TAG, "Error in updating tweet detail.", e);
        }
    }

    public static ArrayList<Tweet> fromJSONArray(JSONArray jsonArray) {
        RealmList<Tweet> tweets = new RealmList<>();
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject tweetJson = jsonArray.getJSONObject(i);
                Tweet tweet = new Tweet();
                tweet.fromJSON(tweetJson);
                if (tweet != null) {
                    tweets.add(tweet);
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error in parsing tweet json:" + tweets, e);
                continue;
            }
        }

        realm.copyToRealmOrUpdate(tweets);
        realm.commitTransaction();
        realm.close();

        Log.d(TAG, "Total tweets: " + tweets.size() + "/" + jsonArray.length());
        return new ArrayList<>(tweets);
    }

    /**
     * @return all the tweets
     */
    public static RealmResults<Tweet> getAllTweets() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Tweet> tweets =  realm.where(Tweet.class).findAllSorted("createdAt", Sort.DESCENDING);
        realm.close();
        return tweets;
    }

    /**
     * @param tweetId - Tweet Id
     * @return a Tweet object
     */
    public static Tweet getTweetById(long tweetId) {
        Realm realm = Realm.getDefaultInstance();
        Tweet tweet =  realm.where(Tweet.class).
                equalTo("id", tweetId).findFirst();
        realm.close();
        return tweet;
    }
}
