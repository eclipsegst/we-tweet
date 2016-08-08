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
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Created by Zhaolong Zhong on 8/7/16.
 */

//@Parcel//Replace using id to object
@RealmClass
public class Media implements RealmModel {
    private static final String TAG = Media.class.getSimpleName();

    @PrimaryKey
    private long id;
    private String mediaUrl;
    private String url;
    private String type;
    private String tweetId;
    private String videoUrl;

    public long getId() {
        return id;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    // Deserialize the JSON and build Media object.
    public void fromJSON(JSONObject json, String tweetId) {
        try {
            this.id = json.getLong("id");
            this.mediaUrl = json.getString("media_url");
            this.url = json.getString("url");
            this.type = json.getString("type");
            this.tweetId = tweetId;
        } catch (JSONException e) {
            Log.e(TAG, "Error in parsing Media.", e);
        }
    }

    public static ArrayList<Media> fromJSONArray(JSONArray jsonArray, String tweetId) {
        RealmList<Media> medias = new RealmList<>();
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject mediaJson = jsonArray.getJSONObject(i);
                Media media = new Media();
                media.fromJSON(mediaJson, tweetId);
                if (media != null) {
                    medias.add(media);
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error in parsing media json:" + medias, e);
                continue;
            }
        }

        realm.copyToRealmOrUpdate(medias);
        realm.commitTransaction();
        realm.close();

        return new ArrayList<>(medias);
    }

    /**
     * @param tweetId - Media Id
     * @return a list of media
     */
    public static RealmResults<Media> getMediaByTweetId(String tweetId) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Media> medias =  realm.where(Media.class).
                equalTo("tweetId", tweetId).findAll();
        realm.close();
        return medias;
    }
}
