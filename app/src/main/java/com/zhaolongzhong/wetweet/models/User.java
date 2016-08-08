package com.zhaolongzhong.wetweet.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Created by Zhaolong Zhong on 8/3/16.
 */

//@Parcel//Replace using id to object
@RealmClass
public class User implements RealmModel {
    private static final String TAG = User.class.getSimpleName();

    @PrimaryKey
    private long id;
    private String name;
    private String screenName;
    private String profileImageUrl;
    private String profileBackgroundImageUrl;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getScreenName() {
        return screenName;
    }

    // Deserialize the JSON and build User object.
    public void fromJSON(JSONObject json) {
        try {
            this.id = json.getLong("id");
            this.name = json.getString("name");
            this.screenName = json.getString("screen_name");
            this.profileImageUrl = json.getString("profile_image_url");
            this.profileBackgroundImageUrl = json.getString("profile_background_image_url");
        } catch (JSONException e) {
            Log.e(TAG, "Error in parsing user.", e);
        }
    }

    /**
     * @param userId - User Id
     * @return a User object
     */
    public static User getUserById(long userId) {
        Realm realm = Realm.getDefaultInstance();
        User user =  realm.where(User.class).
                equalTo("id", userId).findFirst();
        realm.close();
        return user;
    }
}
